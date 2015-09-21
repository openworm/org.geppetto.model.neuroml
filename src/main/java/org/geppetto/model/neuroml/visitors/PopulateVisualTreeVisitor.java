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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.model.quantities.PhysicalQuantity;
import org.geppetto.core.model.quantities.Unit;
import org.geppetto.core.model.runtime.ACompositeNode;
import org.geppetto.core.model.runtime.ANode;
import org.geppetto.core.model.runtime.AVisualObjectNode;
import org.geppetto.core.model.runtime.AspectNode;
import org.geppetto.core.model.runtime.AspectSubTreeNode;
import org.geppetto.core.model.runtime.AspectSubTreeNode.AspectTreeType;
import org.geppetto.core.model.runtime.CompositeNode;
import org.geppetto.core.model.runtime.CylinderNode;
import org.geppetto.core.model.runtime.EntityNode;
import org.geppetto.core.model.runtime.SphereNode;
import org.geppetto.core.model.runtime.VisualGroupElementNode;
import org.geppetto.core.model.runtime.VisualGroupNode;
import org.geppetto.core.model.values.FloatValue;
import org.geppetto.core.utilities.VariablePathSerializer;
import org.geppetto.core.visualisation.model.Point;
import org.neuroml.model.Base;
import org.neuroml.model.BaseCell;
import org.neuroml.model.Cell;
import org.neuroml.model.ChannelDensity;
import org.neuroml.model.Include;
import org.neuroml.model.Instance;
import org.neuroml.model.Location;
import org.neuroml.model.Member;
import org.neuroml.model.Morphology;
import org.neuroml.model.Network;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.Point3DWithDiam;
import org.neuroml.model.Population;
import org.neuroml.model.PopulationTypes;
import org.neuroml.model.Segment;
import org.neuroml.model.SegmentGroup;

/**
 * Helper class to populate visualization tree for neuroml models
 * 
 */
public class PopulateVisualTreeVisitor
{
	private String type = "static";
	private String highSpectrum = "0XFF0000";
	private String lowSpectrum = "0XFFFF00";
	private String defaultColor = "0XFF3300";
	private String axonsColor = "0XFF6600";
	private String dendritesColor = "0X99CC00";
	private String somaColor = "0X0066FF";
	private String SOMA = "soma_group";
	private String AXONS = "axon_group";
	private String DENDRITES = "dendrite_group";

	/**
	 * @param allSegments
	 * @param list
	 * @param list2
	 * @param id
	 * @return
	 */
	private CompositeNode getVisualObjectsFromListOfSegments(List<Segment> segments, Map<String, List<String>> segmentsMap, String id)
	{
		CompositeNode groupNode = new CompositeNode(id);
		Map<String, Point3DWithDiam> distalPoints = new HashMap<String, Point3DWithDiam>();
		for(Segment s : segments)
		{
			String idSegmentParent = null;
			Point3DWithDiam parentDistal = null;
			if(s.getParent() != null)
			{
				idSegmentParent = s.getParent().getSegment().toString();
			}
			if(distalPoints.containsKey(idSegmentParent))
			{
				parentDistal = distalPoints.get(idSegmentParent);
			}
			groupNode.setName(idSegmentParent);
			AVisualObjectNode cyl = getCylinderFromSegment(s, parentDistal);

			if(segmentsMap.containsKey(cyl.getId()))
			{
				// get groups list for segment and put it in visual objects
				cyl.setGroupElementsMap(segmentsMap.get(cyl.getId()));
			}

			groupNode.addChild(cyl);
			distalPoints.put(s.getId().toString(), s.getDistal());
		}

		return groupNode;
	}

	/**
	 * Creates Node objects by reading neuroml document.
	 * 
	 * @param neuroml
	 * @param targetComponents
	 * @return
	 */
	public void createNodesFromNeuroMLDocument(AspectSubTreeNode visualizationTree, NeuroMLDocument neuroml, List<String> targetCells, Map<String, List<ANode>> visualizationNodes)
	{
		// Commented until we have a proper model library
		// Find morphologies inside neuroml document
		// List<Morphology> morphologies = neuroml.getMorphology();
		// if(morphologies != null)
		// {
		// for(Morphology m : morphologies)
		// {
		// if (targetMorphologies == null || targetMorphologies.contains(m.getId())){
		// processMorphology(m, visualizationTree);
		// }
		// }
		// }

		// find networks inside neuroml document
		List<Network> networks = neuroml.getNetwork();
		for(Network n : networks)
		{
			addNetworkTo(n, visualizationTree, (AspectNode) visualizationTree.getParent(), targetCells);
		}

		//Business rule: If there is a network in the NeuroML file we don't visualize spurious cells which 
		//"most likely" are just included types in NeuroML and are instantiated as part of the network
		//populations
		if(networks.size() == 0)
		{
			// find cells inside neuroml document
			List<Cell> cells = neuroml.getCell();
			if(cells != null)
			{
				for(Cell c : cells)
				{
					if(targetCells == null || targetCells.contains(c.getId()))
					{
						List<ANode> visualizationNodesItem = new ArrayList<ANode>();
						if(!c.getMorphology().getSegmentGroup().isEmpty())
						{
							visualizationNodesItem.addAll(processMorphologyFromGroup(c, visualizationTree));
						}
						else
						{
							visualizationNodesItem.add(processMorphology(c.getMorphology(), visualizationTree));
						}
						visualizationNodes.put(c.getId(), visualizationNodesItem);
					}
				}
			}
		}

	}

	/**
	 * @param m
	 * @param visualizationTree
	 */
	private ANode processMorphology(Morphology m, AspectSubTreeNode visualizationTree)
	{
		// create visual groups for regions, and creates a map with
		// objects pointing to groups they are part of
		Map<String, List<String>> segmentsMap = this.createCellPartsVisualGroups(m.getSegmentGroup(), visualizationTree);
		ANode node = getVisualObjectsFromListOfSegments(m.getSegment(), segmentsMap, m.getId());
		return node;
	}

	/**
	 * @param c
	 * @param visualizationTree
	 */
	public List<ANode> processMorphologyFromGroup(Cell c, AspectSubTreeNode visualizationTree)
	{
		List<ANode> visualizationNodes = new ArrayList<ANode>();

		// create nodes for visual objects, segments of cell
		Map<String, List<String>> segmentsMap = this.createCellPartsVisualGroups(c.getMorphology().getSegmentGroup(), visualizationTree);
		visualizationNodes.addAll(createNodesFromMorphologyBySegmentGroup(segmentsMap, c));

		// create density groups for each cell, if it has some
		CompositeNode densities = this.createChannelDensities(c);
		// add density groups to visualization tree
		if(densities != null)
		{
			visualizationNodes.add(densities);
		}

		return visualizationNodes;
	}

	/**
	 * @param c
	 * @param id
	 * @param location
	 * @return
	 */
	public List<ANode> getVisualObjectForCell(BaseCell c, String id, AspectSubTreeNode visualizationTree, Point location)
	{
		List<ANode> visObject = new ArrayList<ANode>();
		if(c instanceof Cell)
		{
			Cell cell = (Cell) c;
			if(!cell.getMorphology().getSegmentGroup().isEmpty())
			{
				visObject.addAll(processMorphologyFromGroup(cell, visualizationTree));
			}
			else
			{
				visObject.add(processMorphology(cell.getMorphology(), visualizationTree));
			}
		}
		else
		{
			SphereNode sphereNode = new SphereNode(id);
			sphereNode.setRadius(1.2d);
			Point origin = null;
			if(location == null)
			{
				origin = new Point();
				origin.setX(0d);
				origin.setY(0d);
				origin.setZ(0d);
				sphereNode.setPosition(origin);
			}
			else
			{
				sphereNode.setPosition(location);
			}
			visObject.add(sphereNode);
		}

		return visObject;
	}

	/**
	 * @param n
	 * @param composite
	 * @param visualizationTree
	 */
	private void addNetworkTo(Network n, ACompositeNode parent, AspectNode aspect, List<String> targetCells)
	{
		for(Population p : n.getPopulation())
		{
			ModelWrapper model = (ModelWrapper) aspect.getModel();
			// the components have already been read by the model interpreter and stored inside a map in the ModelWrapper
			BaseCell cell = getNeuroMLComponent(p.getComponent(), model);

			if(p.getType() != null && p.getType().equals(PopulationTypes.POPULATION_LIST))
			{

				int i = 0;
				for(Instance instance : p.getInstance())
				{
					if(targetCells == null || targetCells.contains(p.getComponent()))
					{
						Point location = null;
						if(instance.getLocation() != null)
						{
							location = getPoint(instance.getLocation());
						}
						AspectSubTreeNode visualizationTree = aspect.getSubTree(AspectTreeType.VISUALIZATION_TREE);

						// create visual object for this instance
						List<ANode> visualObject = getVisualObjectForCell(cell, p.getId(), visualizationTree, location);

						// add visual object to appropriate sub entity
						addVisualObjectToVizTree(VariablePathSerializer.getArrayName(p.getId(), i), visualObject, parent, aspect, model);

						if(targetCells != null)
						{
							targetCells.remove(cell.getId());
						}
					}

					i++;
				}
			}
			else
			{
				int size = p.getSize().intValue();

				for(int i = 0; i < size; i++)
				{
					if(targetCells == null || targetCells.contains(cell.getId()))
					{
						// FIXME the position of the population within the network needs to be specified in neuroml
						AspectSubTreeNode visualizationTree = aspect.getSubTree(AspectTreeType.VISUALIZATION_TREE);
						List<ANode> visualObject = getVisualObjectForCell(cell, cell.getId(), visualizationTree, null);
						addVisualObjectToVizTree(VariablePathSerializer.getArrayName(p.getId(), i), visualObject, parent, aspect, model);

						if(targetCells != null)
						{
							targetCells.remove(cell.getId());
						}
					}
				}
			}
		}
	}

	/**
	 * @param componentId
	 * @param model
	 * @return
	 */
	private BaseCell getNeuroMLComponent(String componentId, ModelWrapper model)
	{
		Map<String, Base> discoveredComponents = (Map<String, Base>) model.getModel("discoveredComponents");
		if(discoveredComponents.containsKey(componentId))
		{
			return (BaseCell) discoveredComponents.get(componentId);
		}
		return null;
	}

	/**
	 * @param id
	 * @param visualObject
	 * @param composite
	 * @param aspect
	 * @param model
	 */
	public void addVisualObjectToVizTree(String id, List<ANode> visualObjects, ACompositeNode composite, AspectNode aspect, ModelWrapper model)
	{

		Map<String, EntityNode> entitiesMapping = (Map<String, EntityNode>) model.getModel("entitiesMapping");
		if(entitiesMapping.containsKey(id))
		{
			EntityNode e = entitiesMapping.get(id);
			for(AspectNode a : e.getAspects())
			{
				if(a.getId().equals(aspect.getId()))
				{
					// we are in the same aspect of the subentity, now we can fetch the visualization tree
					AspectSubTreeNode subEntityVizTree = a.getSubTree(AspectTreeType.VISUALIZATION_TREE);
					if(composite instanceof AspectSubTreeNode)
					{
						for(ANode visualObject : visualObjects)
						{
							subEntityVizTree.addChild(visualObject);
						}
					}
					else if(composite instanceof CompositeNode)
					{
						for(ANode visualObject : visualObjects)
						{
							getCompositeNode(subEntityVizTree, composite.getId()).addChild(visualObject);
						}
					}
				}
			}
		}
		else
		{
			for(ANode visualObject : visualObjects)
			{
				composite.addChild(visualObject);
			}
		}

	}

	/**
	 * @param subEntityVizTree
	 * @param compositeId
	 * @return
	 */
	private CompositeNode getCompositeNode(AspectSubTreeNode subEntityVizTree, String compositeId)
	{
		for(ANode child : subEntityVizTree.getChildren())
		{
			if(child.getId().equals(compositeId) && child instanceof CompositeNode)
			{
				return (CompositeNode) child;
			}
		}
		CompositeNode composite = new CompositeNode(compositeId, compositeId);
		subEntityVizTree.addChild(composite);
		return composite;
	}

	

	/**
	 * @param location
	 * @param visualizationTree
	 * @param list
	 * @return
	 */
	private List<AVisualObjectNode> createNodesFromMorphologyBySegmentGroup(Map<String, List<String>> segmentsMap, Cell cell)
	{
		List<AVisualObjectNode> visualCellNodes = new ArrayList<AVisualObjectNode>();

		Morphology cellmorphology = cell.getMorphology();
		CompositeNode allSegments = getVisualObjectsFromListOfSegments(cellmorphology.getSegment(), segmentsMap, cellmorphology.getId());

		Map<String, List<AVisualObjectNode>> segmentGeometries = new HashMap<String, List<AVisualObjectNode>>();

		if(!cellmorphology.getSegmentGroup().isEmpty())
		{
			Map<String, List<String>> subgroupsMap = new HashMap<String, List<String>>();
			for(SegmentGroup sg : cellmorphology.getSegmentGroup())
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
				visualCellNodes.addAll(segmentGeometries.get(sgId));
			
			}

		}

		return visualCellNodes;
	}

	/**
	 * Create Channel densities visual grups for a cell
	 * 
	 * @param cell
	 *            - Densities visual groups for this cell
	 * @return
	 */
	private CompositeNode createChannelDensities(Cell cell)
	{

		Map<String, VisualGroupNode> groupsMap = new HashMap<String, VisualGroupNode>();

		CompositeNode densities = null;

		if(cell.getBiophysicalProperties() != null && cell.getBiophysicalProperties().getMembraneProperties() != null
				&& cell.getBiophysicalProperties().getMembraneProperties().getChannelDensity() != null)
		{
			densities = new CompositeNode("ChannelDensities");
			densities.setName("Channel Densities");

			for(ChannelDensity density : cell.getBiophysicalProperties().getMembraneProperties().getChannelDensity())
			{
				if(!groupsMap.containsKey(density.getIonChannel()))
				{
					VisualGroupNode vis = new VisualGroupNode(density.getIonChannel());
					vis.setName(density.getIonChannel());
					vis.setType(type);
					vis.setHighSpectrumColor(highSpectrum);
					vis.setLowSpectrumColor(lowSpectrum);
					vis.setParent(densities);
					if(!density.getId().equals("Leak_all"))
					{
						VisualGroupElementNode element = new VisualGroupElementNode(density.getSegmentGroup());
						element.setName(density.getId());

						String regExp = "\\s*([0-9-]*\\.?[0-9]*[eE]?[-+]?[0-9]+)?\\s*(\\w*)";
						Pattern pattern = Pattern.compile(regExp);
						Matcher matcher = pattern.matcher(density.getCondDensity());
						if(matcher.find())
						{
							PhysicalQuantity physicalQuantity = new PhysicalQuantity();
							physicalQuantity.setValue(new FloatValue(Float.parseFloat(matcher.group(1))));
							physicalQuantity.setUnit(new Unit(matcher.group(2)));
							element.setParameter(physicalQuantity);
						}

						element.setParent(vis);
						element.setDefaultColor(defaultColor);
						vis.getVisualGroupElements().add(element);
					}

					densities.addChild(vis);
					groupsMap.put(density.getIonChannel(), vis);
				}
				else
				{
					VisualGroupNode vis = groupsMap.get(density.getIonChannel());

					if(!density.getId().equals("Leak_all"))
					{
						VisualGroupElementNode element = new VisualGroupElementNode(density.getSegmentGroup());
						element.setName(density.getId());

						String regExp = "\\s*([0-9-]*\\.?[0-9]*[eE]?[-+]?[0-9]+)?\\s*(\\w*)";
						Pattern pattern = Pattern.compile(regExp);
						Matcher matcher = pattern.matcher(density.getCondDensity());
						if(matcher.find())
						{
							PhysicalQuantity physicalQuantity = new PhysicalQuantity();
							physicalQuantity.setValue(new FloatValue(Float.parseFloat(matcher.group(1))));
							physicalQuantity.setUnit(new Unit(matcher.group(2)));
							element.setParameter(physicalQuantity);
						}

						element.setParent(vis);
						element.setDefaultColor(defaultColor);
						vis.getVisualGroupElements().add(element);
					}

					densities.addChild(vis);
					groupsMap.put(density.getIonChannel(), vis);
				}
			}
		}

		return densities;
	}

	/**
	 * Gets all segments group from cell. Creates a map with segments as key of map, and list of groups it belongs as value. Creates visual groups for cell regions while looping through segment
	 * groups.
	 * 
	 * @param segmentsGroup
	 * @param visualizationTree
	 * @return
	 */
	private Map<String, List<String>> createCellPartsVisualGroups(List<SegmentGroup> segmentsGroup, AspectSubTreeNode visualizationTree)
	{

		VisualGroupNode cellParts = new VisualGroupNode("CellRegions");
		cellParts.setName("Cell Regions");

		// Create map with segment ids, keeping track of groups they correspond to
		Map<String, List<String>> segmentsMap = new HashMap<String, List<String>>();
		Map<String, List<String>> segmentsGroupsMap = new HashMap<String, List<String>>();

		// Get all the segment groups from morphology
		for(SegmentGroup g : segmentsGroup)
		{

			// segment found
			String segmentGroupID = g.getId();

			VisualGroupElementNode vis = null;

			// create visual groups for cell regions
			if(segmentGroupID.equals(SOMA))
			{
				vis = new VisualGroupElementNode(segmentGroupID);
				vis.setName("Soma");
				vis.setDefaultColor(somaColor);
			}
			else if(segmentGroupID.equals(DENDRITES))
			{
				vis = new VisualGroupElementNode(segmentGroupID);
				vis.setName("Dendrites");
				vis.setDefaultColor(dendritesColor);
			}
			else if(segmentGroupID.equals(AXONS))
			{
				vis = new VisualGroupElementNode(segmentGroupID);
				vis.setName("Axons");
				vis.setDefaultColor(axonsColor);
			}

			if(vis != null)
			{
				vis.setParent(cellParts);
				cellParts.getVisualGroupElements().add(vis);
			}

			// segment not in map, add with new list for groups
			if(!segmentsGroupsMap.containsKey(segmentGroupID))
			{
				List<String> includeGroups = new ArrayList<String>();
				segmentsGroupsMap.put(segmentGroupID, includeGroups);
			}

			// traverse through group segments finding segments inside
			for(Member i : g.getMember())
			{
				// segment found
				String segmentID = i.getSegment().toString();
				// segment not in map, add with new list for groups
				if(!segmentsMap.containsKey(segmentID))
				{
					List<String> groups = new ArrayList<String>();
					groups.add(g.getId());
					segmentsMap.put(segmentID, groups);
				}
				// segment in mpa, get list and put with updated one for groups
				else
				{
					List<String> groups = segmentsMap.get(segmentID);
					groups.add(g.getId());
					segmentsMap.put(segmentID, groups);
				}

				List<String> groups = segmentsGroupsMap.get(segmentGroupID);
				groups.add(segmentID);
				segmentsGroupsMap.put(segmentGroupID, groups);
			}
			// traverse through group segments finding segments inside
			for(Include i : g.getInclude())
			{
				// segment found
				String sg = i.getSegmentGroup();
				// segment not in map, add with new list for groups
				if(segmentsGroupsMap.containsKey(sg))
				{
					List<String> segmentsMembers = segmentsGroupsMap.get(sg);
					for(String key : segmentsMembers)
					{
						List<String> groups = segmentsMap.get(key);
						groups.add(segmentGroupID);
						segmentsMap.put(key, groups);
					}
				}
			}
		}

		visualizationTree.addChild(cellParts);
		return segmentsMap;
	}


	/**
	 * @param sg
	 * @param allSegments
	 * @return
	 */
	private List<AVisualObjectNode> getVisualObjectsForGroup(SegmentGroup sg, CompositeNode allSegments)
	{
		List<AVisualObjectNode> geometries = new ArrayList<AVisualObjectNode>();
		for(Member m : sg.getMember())
		{
			List<ANode> segments = allSegments.getChildren();

			for(ANode g : segments)
			{
				if(((AVisualObjectNode) g).getId().equals("vo"+m.getSegment().toString()))
				{
					geometries.add((AVisualObjectNode) g);
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
	private AVisualObjectNode getCylinderFromSegment(Segment s, Point3DWithDiam parentDistal)
	{
		Point3DWithDiam proximal = (s.getProximal() == null) ? parentDistal : s.getProximal();
		Point3DWithDiam distal = s.getDistal();

		if(samePoint(proximal, distal)) // ideally an equals but the objects
										// are generated. hassle postponed.
		{
			SphereNode sphere = new SphereNode(s.getName());
			sphere.setRadius(proximal.getDiameter() / 2);
			sphere.setPosition(getPoint(proximal));
			sphere.setId("vo"+s.getId().toString());
			return sphere;
		}
		else
		{
			CylinderNode cyl = new CylinderNode(s.getName());
			cyl.setId("vo"+s.getId().toString());
			if(proximal != null)
			{
				cyl.setPosition(getPoint(proximal));
				cyl.setRadiusBottom(proximal.getDiameter() / 2);
			}
			if(distal != null)
			{
				cyl.setRadiusTop(s.getDistal().getDiameter() / 2);
				cyl.setDistal(getPoint(distal));
				cyl.setHeight(0d);
			}
			return cyl;
		}
	}

	/**
	 * @param distal
	 * @return
	 */
	private Point getPoint(Point3DWithDiam distal)
	{
		Point point = new Point();
		point.setX(distal.getX());
		point.setY(distal.getY());
		point.setZ(distal.getZ());
		return point;
	}

	/**
	 * @param location
	 * @return
	 */
	private Point getPoint(Location location)
	{
		Point point = new Point();
		point.setX(location.getX().doubleValue());
		point.setY(location.getY().doubleValue());
		point.setZ(location.getZ().doubleValue());
		return point;
	}
}