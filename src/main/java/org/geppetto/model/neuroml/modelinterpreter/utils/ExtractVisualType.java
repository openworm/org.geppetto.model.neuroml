/*******************************************************************************
 * The MIT License (MIT)
 * 
 * Copyright (c) 2011 - 2015 OpenWorm.
 * http://openworm.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT License
 * which accompanies this distribution, and is available at
 * http://opensource.org/licenses/MIT
 *
 * Contributors:
 *     	OpenWorm - http://openworm.org/people.html
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights 
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
 * copies of the Software, and to permit persons to whom the Software is 
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE 
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package org.geppetto.model.neuroml.modelinterpreter.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.geppetto.core.model.GeppettoModelAccess;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.model.neuroml.utils.Resources;
import org.geppetto.model.types.CompositeVisualType;
import org.geppetto.model.types.TypesFactory;
import org.geppetto.model.types.TypesPackage;
import org.geppetto.model.types.VisualType;
import org.geppetto.model.util.GeppettoVisitingException;
import org.geppetto.model.values.Cylinder;
import org.geppetto.model.values.Point;
import org.geppetto.model.values.Sphere;
import org.geppetto.model.values.ValuesFactory;
import org.geppetto.model.values.VisualGroup;
import org.geppetto.model.values.VisualGroupElement;
import org.geppetto.model.values.VisualValue;
import org.geppetto.model.variables.Variable;
import org.geppetto.model.variables.VariablesFactory;
import org.lemsml.jlems.core.sim.LEMSException;
import org.lemsml.jlems.core.type.Component;
import org.neuroml.export.utils.Utils;
import org.neuroml.model.Cell;
import org.neuroml.model.Include;
import org.neuroml.model.Member;
import org.neuroml.model.Morphology;
import org.neuroml.model.Point3DWithDiam;
import org.neuroml.model.Segment;
import org.neuroml.model.SegmentGroup;
import org.neuroml.model.Standalone;
import org.neuroml.model.util.NeuroMLException;

/**
 * Helper class to populate visualization tree for neuroml models
 * 
 */
public class ExtractVisualType
{

	Component cellComponent;
	Cell cell;

	TypesFactory typeFactory = TypesFactory.eINSTANCE;
	ValuesFactory valuesFactory = ValuesFactory.eINSTANCE;
	VariablesFactory variablesFactory = VariablesFactory.eINSTANCE;

	Map<String, List<VisualGroupElement>> segmentsMap = new HashMap<String, List<VisualGroupElement>>();
	GeppettoModelAccess access;

	LinkedHashMap<SegmentGroup, List<Segment>> segmentGroupSegMap;

	List<Variable> visualObjectsSegments;

	public ExtractVisualType(Component cellComponent, GeppettoModelAccess access) throws LEMSException, NeuroMLException
	{
		super();

		this.cellComponent = cellComponent;
		this.access = access;

		LinkedHashMap<String, Standalone> cellMap = Utils.convertLemsComponentToNeuroML(cellComponent);
		this.cell = (Cell) cellMap.get(cellComponent.getID());
		

		// AQP Maybe we can initialise cellutils here and pass this variable to the create density class
		CellUtils cellUtils = new CellUtils(cell);
		segmentGroupSegMap = cellUtils.getSegmentGroupsVsSegs();
	}

	public VisualType createTypeFromCellMorphology() throws GeppettoVisitingException, LEMSException, NeuroMLException, ModelInterpreterException
	{
		CompositeVisualType visualCompositeType = typeFactory.createCompositeVisualType();
		ModelInterpreterUtils.initialiseNodeFromString(visualCompositeType, cell.getMorphology().getId());
		visualCompositeType.getVisualGroups().add(createCellPartsVisualGroups());

		visualObjectsSegments = getVisualObjectsFromListOfSegments();

		if(cell.getMorphology().getSegmentGroup().isEmpty())
		{
			visualCompositeType.getVariables().addAll(visualObjectsSegments);
		}
		else
		{
			visualCompositeType.getVariables().addAll(createNodesFromMorphologyBySegmentGroup());

			// create density groups for each cell, if it has some
			PopulateChannelDensityVisualGroups populateChannelDensityVisualGroups = new PopulateChannelDensityVisualGroups(cell);
			visualCompositeType.getVisualGroups().addAll(populateChannelDensityVisualGroups.createChannelDensities());

			if(populateChannelDensityVisualGroups.getChannelDensityTag() != null) access.addTag(populateChannelDensityVisualGroups.getChannelDensityTag());
		}

		return visualCompositeType;
	}

	/**
	 * @param allSegments
	 * @param list
	 * @param list2
	 * @param id
	 * @return
	 * @throws GeppettoVisitingException
	 */
	private List<Variable> getVisualObjectsFromListOfSegments() throws GeppettoVisitingException
	{
		List<Variable> visualObjectVariables = new ArrayList<Variable>();

		Map<String, Point3DWithDiam> distalPoints = new HashMap<String, Point3DWithDiam>();
		for(Segment segment : cell.getMorphology().getSegment())
		{
			Variable variable = variablesFactory.createVariable();

			ModelInterpreterUtils.initialiseNodeFromString(variable, ModelInterpreterUtils.getVisualObjectIdentifier(segment));

			// variable.setId(getVisualObjectIdentifier(segment.getId().toString()));
			// variable.setName((segment.getName() != null && !segment.getName().equals(""))?segment.getName(): "compartment_" + segment.getId());

			variable.getTypes().add(this.access.getType(TypesPackage.Literals.VISUAL_TYPE));

			String idSegmentParent = null;
			Point3DWithDiam parentDistal = null;
			if(segment.getParent() != null)
			{
				idSegmentParent = segment.getParent().getSegment().toString();
			}
			if(distalPoints.containsKey(idSegmentParent))
			{
				parentDistal = distalPoints.get(idSegmentParent);
			}
			VisualValue visualObject = getVisualObjectFromSegment(segment, parentDistal);
			if(segmentsMap.containsKey(variable.getId()))
			{
				// get groups list for segment and put it in visual objects
				visualObject.getGroupElements().addAll(segmentsMap.get(variable.getId()));
			}
			variable.getInitialValues().put(this.access.getType(TypesPackage.Literals.VISUAL_TYPE), visualObject);

			distalPoints.put(segment.getId().toString(), segment.getDistal());
			visualObjectVariables.add(variable);
		}

		return visualObjectVariables;
	}

	/**
	 * @param location
	 * @param visualizationTree
	 * @param list
	 * @return
	 * @throws GeppettoVisitingException
	 */
	private List<Variable> createNodesFromMorphologyBySegmentGroup() throws GeppettoVisitingException
	{
		List<Variable> visualObjectVariables = new ArrayList<Variable>();

		Map<String, List<Variable>> segmentGeometries = new HashMap<String, List<Variable>>();

		Morphology morphology = cell.getMorphology();
		if(!morphology.getSegmentGroup().isEmpty())
		{
			Map<String, List<String>> subgroupsMap = new HashMap<String, List<String>>();
			for(SegmentGroup sg : morphology.getSegmentGroup())
			{
				for(Include include : sg.getInclude())
				{
					// the map is <containedGroup,containerGroup>
					if(!subgroupsMap.containsKey(include.getSegmentGroup()))
					{
						subgroupsMap.put(include.getSegmentGroup(), new ArrayList<String>());
					}
					subgroupsMap.get(include.getSegmentGroup()).add(sg.getId());
				}
				if(!sg.getMember().isEmpty())
				{
					segmentGeometries.put(sg.getId(), getVisualObjectsForGroup(sg, visualObjectsSegments));
				}
			}

			// this adds all segment groups not contained in the macro groups if any
			for(String sgId : segmentGeometries.keySet())
			{
				visualObjectVariables.addAll(segmentGeometries.get(sgId));
			}
		}

		return visualObjectVariables;
	}

	/**
	 * Gets all segments group from cell. Creates a map with segments as key of map, and list of groups it belongs as value. Creates visual groups for cell regions while looping through segment
	 * groups.
	 * 
	 * @param segmentsGroup
	 * @param visualizationTree
	 * @return
	 */
	private VisualGroup createCellPartsVisualGroups()
	{
		VisualGroup cellParts = valuesFactory.createVisualGroup();
		ModelInterpreterUtils.initialiseNodeFromString(cellParts, Resources.CELL_REGIONS.get());

		// Get all the segment groups from morphology
		for(SegmentGroup segmentGroup : this.cell.getMorphology().getSegmentGroup())
		{
			// segment found
			String segmentGroupID = segmentGroup.getId();

			// create visual groups for cell regions
			if(segmentGroupID.equals(Resources.SOMA.getId()) || segmentGroupID.equals(Resources.DENDRITES.getId()) || segmentGroupID.equals(Resources.AXONS.getId()))
			{
				VisualGroupElement visualGroupElement = valuesFactory.createVisualGroupElement();
				ModelInterpreterUtils.initialiseNodeFromString(visualGroupElement, segmentGroupID);

				if(segmentGroupID.equals(Resources.SOMA.getId()))
				{
					visualGroupElement.setDefaultColor(ModelInterpreterConstants.SOMA_COLOR);
				}
				else if(segmentGroupID.equals(Resources.DENDRITES.getId()))
				{
					visualGroupElement.setDefaultColor(ModelInterpreterConstants.DENDRITES_COLOR);
				}
				else if(segmentGroupID.equals(Resources.AXONS.getId()))
				{
					visualGroupElement.setDefaultColor(ModelInterpreterConstants.AXONS_COLOR);
				}
				cellParts.getVisualGroupElements().add(visualGroupElement);

				for(Segment segment : segmentGroupSegMap.get(segmentGroup))
				{
					String segmentID = ModelInterpreterUtils.getVisualObjectIdentifier(segment);
					List<VisualGroupElement> groups;
					// segment not in map, add with new list for groups
					if(!segmentsMap.containsKey(segmentID))
					{
						groups = new ArrayList<VisualGroupElement>();
						groups.add(visualGroupElement);
						segmentsMap.put(segmentID, groups);
					}
					// segment in map, get list and put with updated one for groups
					else
					{
						groups = segmentsMap.get(segmentID);
						groups.add(visualGroupElement);
						segmentsMap.put(segmentID, groups);
					}
					segmentsMap.put(segmentID, groups);
				}
			}
		}

		return cellParts;
	}

	/**
	 * @param sg
	 * @param allSegments
	 * @return
	 */
	private List<Variable> getVisualObjectsForGroup(SegmentGroup sg, List<Variable> allSegments)
	{
		List<Variable> geometries = new ArrayList<Variable>();
		
		for (Segment segment : segmentGroupSegMap.get(sg)){
			for(Variable g : allSegments)
			{
				if(g.getId().equals(ModelInterpreterUtils.getVisualObjectIdentifier(segment)))
				{
					geometries.add(g);
				}
			}
		}
		
		return geometries;
	}

	/**
	 * @param p1
	 * @param p2
	 * @return
	 */
	private boolean samePoint(Point3DWithDiam p1, Point3DWithDiam p2)
	{
		return p1.getX() == p2.getX() && p1.getY() == p2.getY() && p1.getZ() == p2.getZ() && p1.getDiameter() == p2.getDiameter();
	}

	/**
	 * @param s
	 * @param parentDistal
	 * @param visualGroupNode
	 * @return
	 */
	private VisualValue getVisualObjectFromSegment(Segment s, Point3DWithDiam parentDistal)
	{
		Point3DWithDiam proximal = (s.getProximal() == null) ? parentDistal : s.getProximal();
		Point3DWithDiam distal = s.getDistal();

		if(samePoint(proximal, distal)) // ideally an equals but the objects
										// are generated. hassle postponed.
		{
			Sphere sphere = valuesFactory.createSphere();
			sphere.setRadius(proximal.getDiameter() / 2);
			sphere.setPosition(getPoint(proximal));
			return sphere;
		}
		else
		{
			Cylinder cylinder = valuesFactory.createCylinder();
			if(proximal != null)
			{
				cylinder.setPosition(getPoint(proximal));
				cylinder.setBottomRadius(proximal.getDiameter() / 2);
			}
			if(distal != null)
			{
				cylinder.setTopRadius(s.getDistal().getDiameter() / 2);
				cylinder.setDistal(getPoint(distal));
				cylinder.setHeight(0d);
			}
			return cylinder;
		}
	}

	/**
	 * @param distal
	 * @return
	 */
	private Point getPoint(Point3DWithDiam distal)
	{
		Point point = valuesFactory.createPoint();
		point.setX(distal.getX());
		point.setY(distal.getY());
		point.setZ(distal.getZ());
		return point;
	}

	public List<Variable> getVisualObjectsSegments()
	{
		return visualObjectsSegments;
	}

}