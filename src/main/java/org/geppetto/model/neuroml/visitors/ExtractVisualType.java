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
package org.geppetto.model.neuroml.visitors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.geppetto.core.model.GeppettoModelAccess;
import org.geppetto.model.neuroml.services.ModelInterpreterUtils;
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

	private String axonsColor = "0XFF6600";
	private String dendritesColor = "0X99CC00";
	private String somaColor = "0X0066FF";
	private String SOMA = "soma_group";
	private String AXONS = "axon_group";
	private String DENDRITES = "dendrite_group";

	Component cellComponent;
	Cell cell;
	TypesFactory typeFactory = TypesFactory.eINSTANCE;
	ValuesFactory valuesFactory = ValuesFactory.eINSTANCE;
	VariablesFactory variablesFactory = VariablesFactory.eINSTANCE;

	Map<String, List<VisualGroupElement>> segmentsMap;

	GeppettoModelAccess access;

	// AQP Maybe we can initialise cellutils here and pass this variable to the create density class
	CellUtils cellUtils;

	public ExtractVisualType(Component cellComponent, GeppettoModelAccess access) throws LEMSException, NeuroMLException
	{
		super();

		this.cellComponent = cellComponent;
		LinkedHashMap<String, Standalone> cellMap = Utils.convertLemsComponentToNeuroML(cellComponent);
		this.cell = (Cell) cellMap.get(cellComponent.getID());

		this.access = access;

		segmentsMap = new HashMap<String, List<VisualGroupElement>>();

		cellUtils = new CellUtils(cell);
	}

	/**
	 * Creates Node objects by reading neuroml document.
	 * 
	 * @param neuroml
	 * @param targetComponents
	 * @return
	 */
	// public void createNodesFromNeuroMLDocument(AspectSubTreeNode visualizationTree, NeuroMLDocument neuroml, List<String> targetCells, Map<String, List<ANode>> visualizationNodes)
	// {
	// // Commented until we have a proper model library
	// // Find morphologies inside neuroml document
	// // List<Morphology> morphologies = neuroml.getMorphology();
	// // if(morphologies != null)
	// // {
	// // for(Morphology m : morphologies)
	// // {
	// // if (targetMorphologies == null || targetMorphologies.contains(m.getId())){
	// // processMorphology(m, visualizationTree);
	// // }
	// // }
	// // }
	//
	// // find networks inside neuroml document
	// List<Network> networks = neuroml.getNetwork();
	// for(Network n : networks)
	// {
	// addNetworkTo(n, visualizationTree, (AspectNode) visualizationTree.getParent(), targetCells);
	// }
	//
	// // Business rule: If there is a network in the NeuroML file we don't visualize spurious cells which
	// // "most likely" are just included types in NeuroML and are instantiated as part of the network
	// // populations
	// if(networks.size() == 0)
	// {
	// // find cells inside neuroml document
	// List<Cell> cells = neuroml.getCell();
	// if(cells != null)
	// {
	// for(Cell c : cells)
	// {
	// if(targetCells == null || targetCells.contains(c.getId()))
	// {
	// List<ANode> visualizationNodesItem = new ArrayList<ANode>();
	// if(!c.getMorphology().getSegmentGroup().isEmpty())
	// {
	// visualizationNodesItem.addAll(processMorphologyFromGroup(c, visualizationTree));
	// }
	// else
	// {
	// visualizationNodesItem.add(processMorphology(c.getMorphology(), visualizationTree));
	// }
	// visualizationNodes.put(c.getId(), visualizationNodesItem);
	// }
	// }
	// }
	// }
	//
	// }

	/**
	 * @param m
	 * @param visualizationTree
	 */
	// private ANode processMorphology(Morphology m, AspectSubTreeNode visualizationTree)
	// {
	// // create visual groups for regions, and creates a map with
	// // objects pointing to groups they are part of
	// Map<String, List<String>> segmentsMap = this.createCellPartsVisualGroups(m.getSegmentGroup(), visualizationTree);
	// ANode node = getVisualObjectsFromListOfSegments(m.getSegment(), segmentsMap, m.getId());
	// return node;
	// }

	/**
	 * @param c
	 * @param visualizationTree
	 */
	// public List<ANode> processMorphologyFromGroup(Cell c, AspectSubTreeNode visualizationTree)
	// {
	// List<ANode> visualizationNodes = new ArrayList<ANode>();
	//
	// // create nodes for visual objects, segments of cell
	// Map<String, List<String>> segmentsMap = this.createCellPartsVisualGroups(c.getMorphology().getSegmentGroup(), visualizationTree);
	// visualizationNodes.addAll(createNodesFromMorphologyBySegmentGroup(segmentsMap, c));
	//
	// // create density groups for each cell, if it has some
	// PopulateChannelDensityVisualGroups populateChannelDensityVisualGroups = new PopulateChannelDensityVisualGroups(c);
	// CompositeNode densities = populateChannelDensityVisualGroups.createChannelDensities();
	// // add density groups to visualization tree
	// if(densities != null)
	// {
	// visualizationNodes.add(densities);
	// }
	//
	// return visualizationNodes;
	// }

	/**
	 * @param c
	 * @param id
	 * @param location
	 * @return
	 */
	// public List<ANode> getVisualObjectForCell(BaseCell c, String id, AspectSubTreeNode visualizationTree, Point location)
	// {
	// List<ANode> visObject = new ArrayList<ANode>();
	// if(c instanceof Cell)
	// {
	// Cell cell = (Cell) c;
	// if(!cell.getMorphology().getSegmentGroup().isEmpty())
	// {
	// visObject.addAll(processMorphologyFromGroup(cell, visualizationTree));
	// }
	// else
	// {
	// visObject.add(processMorphology(cell.getMorphology(), visualizationTree));
	// }
	// }
	// else
	// {
	// SphereNode sphereNode = new SphereNode(id);
	// sphereNode.setRadius(1.2d);
	// Point origin = null;
	// if(location == null)
	// {
	// origin = new Point();
	// origin.setX(0d);
	// origin.setY(0d);
	// origin.setZ(0d);
	// sphereNode.setPosition(origin);
	// }
	// else
	// {
	// sphereNode.setPosition(location);
	// }
	// visObject.add(sphereNode);
	// }
	//
	// return visObject;
	// }

	/**
	 * @param n
	 * @param composite
	 * @param visualizationTree
	 */
	// private void addNetworkTo(Network n, ACompositeNode parent, AspectNode aspect, List<String> targetCells)
	// {
	// for(Population p : n.getPopulation())
	// {
	// ModelWrapper model = (ModelWrapper) aspect.getModel();
	// // the components have already been read by the model interpreter and stored inside a map in the ModelWrapper
	// BaseCell cell = getNeuroMLComponent(p.getComponent(), model);
	//
	// if(p.getType() != null && p.getType().equals(PopulationTypes.POPULATION_LIST))
	// {
	//
	// int i = 0;
	// for(Instance instance : p.getInstance())
	// {
	// if(targetCells == null || targetCells.contains(p.getComponent()))
	// {
	// Point location = null;
	// if(instance.getLocation() != null)
	// {
	// location = getPoint(instance.getLocation());
	// }
	// AspectSubTreeNode visualizationTree = aspect.getSubTree(AspectTreeType.VISUALIZATION_TREE);
	//
	// // create visual object for this instance
	// List<ANode> visualObject = getVisualObjectForCell(cell, p.getId(), visualizationTree, location);
	//
	// // add visual object to appropriate sub entity
	// addVisualObjectToVizTree(VariablePathSerializer.getArrayName(p.getId(), i), visualObject, parent, aspect, model);
	//
	// if(targetCells != null)
	// {
	// targetCells.remove(cell.getId());
	// }
	// }
	//
	// i++;
	// }
	// }
	// else
	// {
	// int size = p.getSize().intValue();
	//
	// for(int i = 0; i < size; i++)
	// {
	// if(targetCells == null || targetCells.contains(cell.getId()))
	// {
	// // FIXME the position of the population within the network needs to be specified in neuroml
	// AspectSubTreeNode visualizationTree = aspect.getSubTree(AspectTreeType.VISUALIZATION_TREE);
	// List<ANode> visualObject = getVisualObjectForCell(cell, cell.getId(), visualizationTree, null);
	// addVisualObjectToVizTree(VariablePathSerializer.getArrayName(p.getId(), i), visualObject, parent, aspect, model);
	//
	// if(targetCells != null)
	// {
	// targetCells.remove(cell.getId());
	// }
	// }
	// }
	// }
	// }
	// }

	public VisualType createTypeFromCellMorphology() throws GeppettoVisitingException, LEMSException, NeuroMLException
	{
		CompositeVisualType visualCompositeType = typeFactory.createCompositeVisualType();
		ModelInterpreterUtils.initialiseNodeFromString(visualCompositeType, cell.getMorphology().getId());
		visualCompositeType.getVisualGroups().add(createCellPartsVisualGroups());

		// List<VisualValue> visualizationNodes = new ArrayList<VisualValue>();

		if(cell.getMorphology().getSegmentGroup().isEmpty())
		{
			visualCompositeType.getVariables().addAll(getVisualObjectsFromListOfSegments(cell.getMorphology()));
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
	public List<Variable> getVisualObjectsFromListOfSegments(Morphology morphology) throws GeppettoVisitingException
	{
		List<Variable> visualObjectVariables = new ArrayList<Variable>();

		Map<String, Point3DWithDiam> distalPoints = new HashMap<String, Point3DWithDiam>();
		for(Segment segment : morphology.getSegment())
		{
			Variable variable = variablesFactory.createVariable();
			variable.setId("vo" + segment.getId().toString());
			variable.setName("vo" + segment.getId().toString());
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
			//variable.getInitialValues().put(this.access.getType(TypesPackage.Literals.VISUAL_TYPE), visualObject);

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
	public List<Variable> createNodesFromMorphologyBySegmentGroup() throws GeppettoVisitingException
	{
		List<Variable> visualObjectVariables = new ArrayList<Variable>();

		Morphology morphology = cell.getMorphology();
		List<Variable> allSegments = getVisualObjectsFromListOfSegments(morphology);

		Map<String, List<Variable>> segmentGeometries = new HashMap<String, List<Variable>>();

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
					segmentGeometries.put(sg.getId(), getVisualObjectsForGroup(sg, allSegments));
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
	public VisualGroup createCellPartsVisualGroups()
	{
		VisualGroup cellParts = valuesFactory.createVisualGroup();
		cellParts.setName("Cell Regions");

		// Create map with segment ids, keeping track of groups they correspond to
		// Map<String, List<String>> segmentsGroupsMap = new HashMap<String, List<String>>();

		// Get all the segment groups from morphology
		for(SegmentGroup segmentGroup : this.cell.getMorphology().getSegmentGroup())
		{
			// segment found
			String segmentGroupID = segmentGroup.getId();

			// create visual groups for cell regions
			if(segmentGroupID.equals(SOMA) || segmentGroupID.equals(DENDRITES) || segmentGroupID.equals(AXONS))
			{
				VisualGroupElement visualGroupElement = valuesFactory.createVisualGroupElement();
				visualGroupElement.setId(segmentGroupID);

				if(segmentGroupID.equals(SOMA))
				{
					visualGroupElement.setName("Soma");
					visualGroupElement.setDefaultColor(somaColor);
				}
				else if(segmentGroupID.equals(DENDRITES))
				{
					visualGroupElement.setName("Dendrites");
					visualGroupElement.setDefaultColor(dendritesColor);
				}
				else if(segmentGroupID.equals(AXONS))
				{
					visualGroupElement.setName("Axons");
					visualGroupElement.setDefaultColor(axonsColor);
				}
				cellParts.getVisualGroupElements().add(visualGroupElement);

				for(Integer segmentId : cellUtils.getSegmentIdsInGroup(segmentGroup))
				{
					String segmentID = getVisualObjectIdentifier(segmentId.toString());
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

		// // AQP: Let's see if we can get all this information from the cellutils
		// // segment not in map, add with new list for groups
		// if(!segmentsGroupsMap.containsKey(segmentGroupID)) segmentsGroupsMap.put(segmentGroupID, new ArrayList<String>());
		//
		// // traverse through group segments finding segments inside
		// for(Member member : segmentGroup.getMember())
		// {
		// // segment found
		// String segmentID = getVisualObjectIdentifier(member.getSegment().toString());
		//
		// if(visualGroupElement != null)
		// {
		// // segment not in map, add with new list for groups
		// if(!segmentsMap.containsKey(segmentID))
		// {
		// List<VisualGroupElement> groups = new ArrayList<VisualGroupElement>();
		// groups.add(visualGroupElement);
		// segmentsMap.put(segmentID, groups);
		// }
		// // segment in map, get list and put with updated one for groups
		// else
		// {
		// List<VisualGroupElement> groups = segmentsMap.get(segmentID);
		// groups.add(visualGroupElement);
		// segmentsMap.put(segmentID, groups);
		// }
		// }
		//
		// List<String> groups = segmentsGroupsMap.get(segmentGroupID);
		// groups.add(segmentID);
		// segmentsGroupsMap.put(segmentGroupID, groups);
		// }
		//
		// if(visualGroupElement != null)
		// {
		// // traverse through group segments finding segments inside
		// for(Include i : segmentGroup.getInclude())
		// {
		// // segment found
		// String sg = i.getSegmentGroup();
		// // segment not in map, add with new list for groups
		// if(segmentsGroupsMap.containsKey(sg))
		// {
		// List<String> segmentsMembers = segmentsGroupsMap.get(sg);
		// for(String key : segmentsMembers)
		// {
		// List<VisualGroupElement> groups = segmentsMap.get(key);
		// groups.add(visualGroupElement);
		// segmentsMap.put(key, groups);
		// }
		// }
		// }
		// }
		// }

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
		for(Member m : sg.getMember())
		{
			for(Variable g : allSegments)
			{
				if(g.getId().equals(getVisualObjectIdentifier(m.getSegment().toString())))
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
			// SphereNode sphere = new SphereNode(s.getName());
			Sphere sphere = valuesFactory.createSphere();
			sphere.setRadius(proximal.getDiameter() / 2);
			sphere.setPosition(getPoint(proximal));
			// sphere.setId(getVisualObjectIdentifier(s.getId().toString()));
			return sphere;
		}
		else
		{
			// CylinderNode cyl = new CylinderNode(s.getName());
			Cylinder cylinder = valuesFactory.createCylinder();
			// cylinder.setId(getVisualObjectIdentifier(s.getId().toString()));
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

	/**
	 * @param neuromlID
	 * @return
	 */
	private String getVisualObjectIdentifier(String neuromlID)
	{
		return "vo" + neuromlID;
	}

}