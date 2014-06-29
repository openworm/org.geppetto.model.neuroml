/*******************************************************************************
 * The MIT License (MIT)
 *
 * Copyright (c) 2011, 2013 OpenWorm.
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

package org.geppetto.model.neuroml.services;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.model.IModel;
import org.geppetto.core.model.IModelInterpreter;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.model.simulation.Aspect;
import org.geppetto.core.model.simulation.Entity;
import org.geppetto.core.model.state.ACompositeStateNode;
import org.geppetto.core.model.state.AVisualNode;
import org.geppetto.core.model.state.AspectNode;
import org.geppetto.core.model.state.AspectTreeNode;
import org.geppetto.core.model.state.EntityNode;
import org.geppetto.core.model.state.EntityNode.Connection;
import org.geppetto.core.model.state.TextMetadataNode;
import org.geppetto.core.visualisation.model.AVisualObject;
import org.geppetto.core.visualisation.model.Cylinder;
import org.geppetto.core.visualisation.model.Point;
import org.geppetto.core.visualisation.model.Sphere;
import org.geppetto.core.visualisation.model.VisualModel;
import org.lemsml.jlems.core.api.LEMSDocumentReader;
import org.lemsml.jlems.core.api.interfaces.ILEMSDocument;
import org.lemsml.jlems.core.api.interfaces.ILEMSDocumentReader;
import org.lemsml.jlems.core.sim.ContentError;
import org.neuroml.model.BaseCell;
import org.neuroml.model.Cell;
import org.neuroml.model.ChannelDensity;
import org.neuroml.model.IafCell;
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
import org.neuroml.model.SpecificCapacitance;
import org.neuroml.model.SynapticConnection;
import org.neuroml.model.util.NeuroMLConverter;
import org.springframework.stereotype.Service;

/**
 * @author matteocantarelli
 * 
 */
@Service
public class NeuroMLModelInterpreterService implements IModelInterpreter
{

	private static final String GROUP_PROPERTY = "group";

	private static final String LEMS_ID = "lems";
	private static final String NEUROML_ID = "neuroml";
	private static final String URL_ID = "url";

	private static Log _logger = LogFactory.getLog(NeuroMLModelInterpreterService.class);

	private EntityNode _visualEntity = null;
	private int _modelHash = 0;

	private String _aspectId;

	private Map<String, BaseCell> _discoveredCells = new HashMap<String, BaseCell>();

	private String _parentInstancePath;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openworm.simulationengine.core.model.IModelProvider#readModel(java .lang.String)
	 */
	public IModel readModel(URL url, List<URL> recordings, String instancePath) throws ModelInterpreterException
	{
		ModelWrapper lemsWrapper = null;
		try
		{
			Scanner scanner = new Scanner(url.openStream(), "UTF-8");
			String neuroMLString = scanner.useDelimiter("\\A").next();
			scanner.close();
			String lemsString = NeuroMLConverter.convertNeuroML2ToLems(neuroMLString);

			ILEMSDocumentReader lemsReader = new LEMSDocumentReader();
			ILEMSDocument document = lemsReader.readModel(lemsString);

			NeuroMLConverter neuromlConverter = new NeuroMLConverter();
			NeuroMLDocument neuroml = neuromlConverter.urlToNeuroML(url);

			lemsWrapper = new ModelWrapper(UUID.randomUUID().toString());
			lemsWrapper.setInstancePath(instancePath);
			// two different interpretations of the same file, one used to simulate the other used to visualize
			lemsWrapper.wrapModel(LEMS_ID, document);
			lemsWrapper.wrapModel(NEUROML_ID, neuroml);
			lemsWrapper.wrapModel(URL_ID, url);

		}
		catch(IOException e)
		{
			throw new ModelInterpreterException(e);
		}
		catch(ContentError e)
		{
			throw new ModelInterpreterException(e);
		}
		catch(JAXBException e)
		{
			throw new ModelInterpreterException(e);
		}
		catch(Exception e)
		{
			throw new ModelInterpreterException(e);
		}
		return lemsWrapper;
	}

	@Override
	public EntityNode getVisualEntity(IModel model, Aspect aspect, AspectTreeNode stateTree) throws ModelInterpreterException
	{
		Entity currentEntity = aspect.getParentEntity();
		_aspectId = aspect.getId();
		_parentInstancePath=aspect.getParentEntity().getInstancePath();
		try
		{
			if(_visualEntity == null || _modelHash != model.hashCode())
			{
				_visualEntity = new EntityNode();
				AspectNode visualAspect = new AspectNode();
				visualAspect.setId(aspect.getId());
				_visualEntity.getAspects().add(visualAspect);
				_modelHash = model.hashCode();
				NeuroMLDocument neuroml = (NeuroMLDocument) ((ModelWrapper) model).getModel(NEUROML_ID);
				if(neuroml != null)
				{
					URL url = (URL) ((ModelWrapper) model).getModel(URL_ID);

					populateEntity(neuroml, _visualEntity, currentEntity, url);

					_visualEntity.getChildren().addAll(getEntitiesFromNetwork(neuroml, url));
				}
				return _visualEntity;
			}
			else
			{
				// if we already sent once the update every other time it's going to be empty unless it changes
				// as the geometry won't change
				EntityNode empty = new EntityNode();
				AspectNode visualAspect = new AspectNode();
				visualAspect.setId(aspect.getId());
				empty.getAspects().add(visualAspect);
				return empty;
			}

		}
		catch(Exception e)
		{
			throw new ModelInterpreterException(e);
		}
	}

	/**
	 * @param neuroml
	 * @param visualEntity
	 * @param currentEntity
	 */
	private void populateEntity(NeuroMLDocument neuroml, EntityNode visualEntity, Entity currentEntity, URL url)
	{

		// We try to figure out here what kind of neuroml file we are looking at
		// Is it a single cell? Is it a network? Does it contain cells? Does it contain morphologies?

		// C. If it contains a network if the other cells are separately available as geppetto entities it will just add information about their connection

		List<EntityNode> discoveredEntities = getCEntitiesFromNeuroMLDocument(neuroml, url);

		if(discoveredEntities.size() == 1)
		{
			// A. If it is a single cell we populate the visual entity with information and visual model about this cell
			List<VisualModel> discoveredVisualModels = discoveredEntities.get(0).getAspects().get(0).getVisualModel();
			visualEntity.getAspects().get(0).getVisualModel().addAll(discoveredVisualModels);
			visualEntity.setMetadata(discoveredEntities.get(0).getMetadata());
		}
		else if(discoveredEntities.size() > 1)
		{
			// B. If it contains more than one cell or more than one morphology the cells will all be children of the current entity
			visualEntity.getChildren().addAll(discoveredEntities);
		}

	}

	/**
	 * @param id
	 * @return
	 */
	private EntityNode getNewNeuronalEntity(String id)
	{
		EntityNode EntityNode = new EntityNode();
		EntityNode.setId(id);
		EntityNode.setInstancePath(_parentInstancePath+"."+_aspectId+"."+id);
		AspectNode cAspect = new AspectNode();
		cAspect.setId(_aspectId);
		cAspect.setInstancePath(_parentInstancePath+"."+_aspectId+"."+id);
		EntityNode.getAspects().add(cAspect);
		return EntityNode;
	}

	/**
	 * @param visualEntity
	 * @param morphology
	 */
	private EntityNode populateEntityNodeFromMorphology(EntityNode visualEntity, Morphology morphology)
	{
		return populateEntityNodeFromListOfSegments(visualEntity, morphology.getSegment());

	}

	/**
	 * @param list
	 * @return
	 */
	private EntityNode populateEntityNodeFromListOfSegments(EntityNode entity, List<Segment> list)
	{
		VisualModel visualModel = getVisualModelFromListOfSegments(list);
		entity.getAspects().get(0).getVisualModel().add(visualModel);
		return entity;
	}

	/**
	 * @param list
	 * @return
	 */
	private VisualModel getVisualModelFromListOfSegments(List<Segment> list)
	{
		VisualModel visualModel = new VisualModel();
		Map<String, Point3DWithDiam> distalPoints = new HashMap<String, Point3DWithDiam>();
		for(Segment s : list)
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
			visualModel.getObjects().add(getCylinderFromSegment(s, parentDistal));
			distalPoints.put(s.getId().toString(), s.getDistal());
		}
		return visualModel;
	}

	/**
	 * @param neuroml
	 * @return
	 */
	public List<EntityNode> getCEntitiesFromNeuroMLDocument(NeuroMLDocument neuroml, URL url)
	{
		List<EntityNode> entities = new ArrayList<EntityNode>();
		List<Morphology> morphologies = neuroml.getMorphology();
		if(morphologies != null)
		{
			for(Morphology m : morphologies)
			{
				EntityNode entity = getNewNeuronalEntity(m.getId());
				populateEntityNodeFromMorphology(entity, m);
				entities.add(entity);
			}
		}
		List<Cell> cells = neuroml.getCell();
		if(cells != null)
		{
			for(Cell c : cells)
			{
				_discoveredCells.put(c.getId(), c);
				EntityNode entity = getEntityNodefromCell(c);
				entities.add(entity);
			}
		}
		List<IafCell> iafCells = neuroml.getIafCell();
		if(iafCells != null)
		{
			for(IafCell iafCell : iafCells)
			{
				_discoveredCells.put(iafCell.getId(), iafCell);
			}
		}
		return entities;
	}

	/**
	 * @param c
	 * @return
	 */
	private EntityNode getEntityNodefromCell(Cell c)
	{
		EntityNode entity = getNewNeuronalEntity(c.getId());
		Morphology cellmorphology = c.getMorphology();
		entity.getAspects().get(0).getVisualModel().addAll(getVisualModelsFromMorphologyBySegmentGroup(cellmorphology, c.getId()));
		augmentWithMetaData(entity, c);
		return entity;
	}

	/**
	 * @param c
	 * @param id 
	 * @return
	 */
	private EntityNode getEntityNodefromCell(BaseCell c, String id)
	{
		EntityNode entity = getNewNeuronalEntity(id);
		VisualModel visualModel = new VisualModel();
		Sphere sphere = new Sphere();
		sphere.setRadius(1d);
		Point origin=new Point();
		origin.setX(0d);
		origin.setY(0d);
		origin.setZ(0d);
		sphere.setPosition(origin);
		sphere.setId("abstract");
		visualModel.getObjects().add(sphere);
		entity.getAspects().get(0).getVisualModel().add(visualModel);
		return entity;
	}

	/**
	 * @param componentId
	 * @param url
	 * @return
	 */
	private BaseCell retrieveNeuroMLCell(String componentId, URL url) throws Exception
	{
		if(_discoveredCells.containsKey(componentId))
		{
			return _discoveredCells.get(componentId);
		}
		NeuroMLConverter neuromlConverter = new NeuroMLConverter();
		boolean attemptConnection = true;
		String baseURL = url.getFile();
		if(url.getFile().endsWith("nml"))
		{
			baseURL = baseURL.substring(0, baseURL.lastIndexOf("/") + 1);
		}
		int attempts = 0;
		NeuroMLDocument neuromlDocument = null;
		while(attemptConnection)
		{
			try
			{
				attemptConnection = false;
				attempts++;
				URL componentURL = new URL(url.getProtocol() + "://" + url.getAuthority() + baseURL + componentId + ".nml");

				neuromlDocument = neuromlConverter.urlToNeuroML(componentURL);

				List<Cell> cells = neuromlDocument.getCell();
				if(cells != null)
				{
					for(Cell c : cells)
					{
						_discoveredCells.put(componentId, c);
						if(c.getId().equals(componentId))
						{
							return c;
						}
					}
				}
			}
			catch(MalformedURLException e)
			{
				throw e;
			}
			catch(UnmarshalException e)
			{
				if(e.getLinkedException() instanceof IOException)
				{
					if(attempts < MAX_ATTEMPTS)
					{
						attemptConnection = true;
					}
				}
			}
			catch(Exception e)
			{
				throw e;
			}
		}
		return null;
	}

	private static final int MAX_ATTEMPTS = 3;

	/**
	 * @param neuroml
	 * @param scene
	 * @param url
	 * @throws Exception
	 */
	private Collection<EntityNode> getEntitiesFromNetwork(NeuroMLDocument neuroml, URL url) throws Exception
	{
		Map<String, EntityNode> entities = new HashMap<String, EntityNode>();
		List<Network> networks = neuroml.getNetwork();

		for(Network n : networks)
		{
			for(Population p : n.getPopulation())
			{
				BaseCell cell = retrieveNeuroMLCell(p.getComponent(), url);

				if(p.getType() != null && p.getType().equals(PopulationTypes.POPULATION_LIST))
				{
					int i = 0;
					for(Instance instance : p.getInstance())
					{
						EntityNode e = getEntityNodefromCell(cell, p.getId());

						if(instance.getLocation() != null)
						{
							e.setPosition(getPoint(instance.getLocation()));
						}
						if(p.getInstance().size()>1)
						{
							e.setId(p.getId()+"["+i+"]");
						}
						else
						{
							e.setId(p.getId());
						}						
						entities.put(e.getId(), e);
					}
					i++;

				}
				else
				{
					int size = p.getSize().intValue();

					for(int i = 0; i < size; i++)
					{
						// FIXME the position of the population within the network needs to be specified in neuroml
						EntityNode e = getEntityNodefromCell(cell, cell.getId());

						e.setId(e.getId() + "[" + i + "]");
						entities.put(e.getId(), e);
					}
				}

				// FIXME what's the purpose of the id here?
				String id = p.getId();

			}
			for(SynapticConnection c : n.getSynapticConnection())
			{
				String from = c.getFrom();
				String to = c.getTo();

				TextMetadataNode mPost = new TextMetadataNode();
				mPost.setAdditionalProperties(Resources.SYNAPSE.get(), c.getSynapse());
				mPost.setAdditionalProperties(Resources.CONNECTION_TYPE.get(), Resources.POST_SYNAPTIC.get());
				Connection rPost = new Connection();
				rPost.setEntityId(to);
				rPost.setMetadata(mPost);
				rPost.setType(Resources.POST_SYNAPTIC.get());

				TextMetadataNode mPre = new TextMetadataNode();
				mPre.setAdditionalProperties(Resources.SYNAPSE.get(), c.getSynapse());
				mPre.setAdditionalProperties(Resources.CONNECTION_TYPE.get(), Resources.PRE_SYNAPTIC.get());
				Connection rPre = new Connection();
				rPre.setEntityId(from);
				rPre.setMetadata(mPre);
				rPost.setType(Resources.PRE_SYNAPTIC.get());

				if(entities.containsKey(from))
				{
					entities.get(from).getConnections().add(rPost);
				}
				else
				{
					throw new Exception("Connection not found." + from + " was not found in the path of the network file");
				}

				if(entities.containsKey(to))
				{
					entities.get(to).getConnections().add(rPre);
				}
				else
				{
					throw new Exception("Connection not found." + to + " was not found in the path of the network file");
				}
			}

		}
		return entities.values();
	}

	/**
	 * @param entity
	 * @param c
	 */
	private void augmentWithMetaData(EntityNode entity, Cell c)
	{
		try
		{
			if(c.getBiophysicalProperties() != null)
			{
				TextMetadataNode membraneProperties = new TextMetadataNode();
				if(c.getBiophysicalProperties().getMembraneProperties() != null)
				{
					for(ChannelDensity channelDensity : c.getBiophysicalProperties().getMembraneProperties().getChannelDensity())
					{
						membraneProperties.setAdditionalProperties(Resources.COND_DENSITY.get(), channelDensity.getCondDensity());
					}

					for(SpecificCapacitance specificCapacitance : c.getBiophysicalProperties().getMembraneProperties().getSpecificCapacitance())
					{
						membraneProperties.setAdditionalProperties(Resources.SPECIFIC_CAPACITANCE.get(), specificCapacitance.getValue());
					}
				}

				TextMetadataNode intracellularProperties = new TextMetadataNode();
				if(c.getBiophysicalProperties().getIntracellularProperties() != null)
				{
					if(c.getBiophysicalProperties().getIntracellularProperties().getResistivity() != null && c.getBiophysicalProperties().getIntracellularProperties().getResistivity().size() > 0)
					{
						intracellularProperties.setAdditionalProperties(Resources.RESISTIVITY.get(), c.getBiophysicalProperties().getIntracellularProperties().getResistivity().get(0).getValue());
					}
				}

				// Sample code to add URL metadata
				// Metadata externalResources = new Metadata();
				// externalResources.setAdditionalProperties("Worm Atlas", "URL:http://www.wormatlas.org/neurons/Individual%20Neurons/PVDmainframe.htm");
				// externalResources.setAdditionalProperties("WormBase", "URL:https://www.wormbase.org/tools/tree/run?name=PVDR;class=Cell");

				entity.setMetadata(new TextMetadataNode());
				entity.getMetadata().setAdditionalProperties(Resources.MEMBRANE_P.get(), membraneProperties);
				entity.getMetadata().setAdditionalProperties(Resources.INTRACELLULAR_P.get(), intracellularProperties);
				// entity.getMetadata().setAdditionalProperties("External Resources", externalResources);
			}
		}
		catch(NullPointerException ex)
		{

		}
	}

	/**
	 * @param morphology
	 * @param cellId
	 * @return
	 */
	private List<VisualModel> getVisualModelsFromMorphologyBySegmentGroup(Morphology morphology, String cellId)
	{
		VisualModel allSegments = getVisualModelFromListOfSegments(morphology.getSegment());

		List<VisualModel> visualModels = new ArrayList<VisualModel>();
		Map<String, List<AVisualObject>> segmentGeometries = new HashMap<String, List<AVisualObject>>();

		if(morphology.getSegmentGroup().isEmpty())
		{
			// there are no segment groups
			visualModels.add(allSegments);
		}
		else
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
			for(String sg : segmentGeometries.keySet())
			{
				for(AVisualObject vo : segmentGeometries.get(sg))
				{
					vo.setAdditionalProperty("segment_groups", getAllGroupsString(sg, subgroupsMap, ""));
				}
			}

			// this adds all segment groups not contained in the macro groups if any
			for(String sgId : segmentGeometries.keySet())
			{
				VisualModel visualModel = new VisualModel();
				visualModel.getObjects().addAll(segmentGeometries.get(sgId));
				visualModel.setAdditionalProperty(GROUP_PROPERTY, sgId);
				visualModel.setId(getGroupId(cellId, sgId));
				visualModels.add(visualModel);
			}

		}
		return visualModels;

	}

	/**
	 * @param targetSg
	 * @param subgroupsMap
	 * @param allGroupsStringp
	 * @return a semicolon separated string containing all the subgroups that contain a given subgroup
	 */
	private String getAllGroupsString(String targetSg, Map<String, List<String>> subgroupsMap, String allGroupsStringp)
	{
		if(subgroupsMap.containsKey(targetSg))
		{
			StringBuilder allGroupsString = new StringBuilder(allGroupsStringp);
			for(String containerGroup : subgroupsMap.get(targetSg))
			{
				allGroupsString.append(containerGroup + "; ");
				allGroupsString.append(getAllGroupsString(containerGroup, subgroupsMap, ""));
			}
			return allGroupsString.toString();
		}
		return allGroupsStringp.trim();
	}

	/**
	 * @param cellId
	 * @param segmentGroupId
	 * @return
	 */
	private String getGroupId(String cellId, String segmentGroupId)
	{
		return cellId + "." + segmentGroupId;
	}

	/**
	 * @param somaGroup
	 * @param segmentGeometries
	 */
	private VisualModel createVisualModelForMacroGroup(SegmentGroup macroGroup, Map<String, List<AVisualObject>> segmentGeometries, List<AVisualObject> allSegments)
	{
		VisualModel visualModel = new VisualModel();
		visualModel.setAdditionalProperty(GROUP_PROPERTY, macroGroup.getId());
		for(Include i : macroGroup.getInclude())
		{
			if(segmentGeometries.containsKey(i.getSegmentGroup()))
			{
				visualModel.getObjects().addAll(segmentGeometries.get(i.getSegmentGroup()));
			}
		}
		for(Member m : macroGroup.getMember())
		{
			for(AVisualObject g : allSegments)
			{
				if(g.getId().equals(m.getSegment().toString()))
				{
					visualModel.getObjects().add(g);
					allSegments.remove(g);
					break;
				}
			}
		}
		segmentGeometries.remove(macroGroup.getId());
		return visualModel;
	}

	/**
	 * @param sg
	 * @param allSegments
	 * @return
	 */
	private List<AVisualObject> getVisualObjectsForGroup(SegmentGroup sg, VisualModel allSegments)
	{
		List<AVisualObject> geometries = new ArrayList<AVisualObject>();
		for(Member m : sg.getMember())
		{
			List<AVisualNode> segments = allSegments.getObjects();
			
			for(AVisualNode g : segments )
			{
				if(((AVisualObject) g).getId().equals(m.getSegment().toString()))
				{
					geometries.add((AVisualObject) g);
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
	 * @return
	 */
	private AVisualObject getCylinderFromSegment(Segment s, Point3DWithDiam parentDistal)
	{

		Point3DWithDiam proximal = s.getProximal() == null ? parentDistal : s.getProximal();
		Point3DWithDiam distal = s.getDistal();

		if(samePoint(proximal, distal)) // ideally an equals but the objects
										// are generated. hassle postponed.
		{
			Sphere sphere = new Sphere();
			sphere.setRadius(proximal.getDiameter() / 2);
			sphere.setPosition(getPoint(proximal));
			sphere.setId(s.getId().toString());
			return sphere;
		}
		else
		{
			Cylinder cyl = new Cylinder();
			cyl.setId(s.getId().toString());
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

	@Override
	public boolean populateVisualTree(AspectNode aspectNode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean populateModelTree(AspectNode aspectNode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean populateRuntimeTree(AspectNode aspectNode) {
		// TODO Auto-generated method stub
		return false;
	}

}
