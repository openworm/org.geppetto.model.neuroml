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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.model.IModel;
import org.geppetto.core.model.IModelInterpreter;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.model.state.StateTreeRoot;
import org.geppetto.core.visualisation.model.AGeometry;
import org.geppetto.core.visualisation.model.Cylinder;
import org.geppetto.core.visualisation.model.Entity;
import org.geppetto.core.visualisation.model.Metadata;
import org.geppetto.core.visualisation.model.Point;
import org.geppetto.core.visualisation.model.Reference;
import org.geppetto.core.visualisation.model.Scene;
import org.geppetto.core.visualisation.model.Sphere;
import org.lemsml.jlems.core.api.LEMSDocumentReader;
import org.lemsml.jlems.core.api.interfaces.ILEMSDocument;
import org.lemsml.jlems.core.api.interfaces.ILEMSDocumentReader;
import org.lemsml.jlems.core.sim.ContentError;
import org.neuroml.model.Cell;
import org.neuroml.model.ChannelDensity;
import org.neuroml.model.Include;
import org.neuroml.model.Member;
import org.neuroml.model.Morphology;
import org.neuroml.model.Network;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.Point3DWithDiam;
import org.neuroml.model.Population;
import org.neuroml.model.Segment;
import org.neuroml.model.SegmentGroup;
import org.neuroml.model.SynapticConnection;
import org.neuroml.model.ValueAcrossSegOrSegGroup;
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

	// neuroml hardcoded concepts
	private static final String DENDRITE_GROUP = "dendrite_group";
	private static final String AXON_GROUP = "axon_group";
	private static final String SOMA_GROUP = "soma_group";

	private static final String LEMS_ID = "lems";
	private static final String NEUROML_ID = "neuroml";
	private static final String URL_ID = "url";

	private static Log _logger = LogFactory.getLog(NeuroMLModelInterpreterService.class);
	private Scene _scene = null;
	private int _modelHash = 0;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openworm.simulationengine.core.model.IModelProvider#readModel(java .lang.String)
	 */
	public IModel readModel(URL url) throws ModelInterpreterException
	{
		ModelWrapper lemsWrapper = null;
		try
		{
			String neuroMLString = new Scanner(url.openStream(), "UTF-8").useDelimiter("\\A").next();
			String lemsString = NeuroMLConverter.convertNeuroML2ToLems(neuroMLString);

			ILEMSDocumentReader lemsReader = new LEMSDocumentReader();
			ILEMSDocument document = lemsReader.readModel(lemsString);

			NeuroMLConverter neuromlConverter = new NeuroMLConverter();
			NeuroMLDocument neuroml = neuromlConverter.urlToNeuroML(url);

			lemsWrapper = new ModelWrapper(UUID.randomUUID().toString());
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

	private Scene getScene(IModel model) throws Exception
	{
		if(_scene == null || _modelHash != model.hashCode())
		{
			_scene = new Scene();
			_modelHash = model.hashCode();
			NeuroMLDocument neuroml = (NeuroMLDocument) ((ModelWrapper) model).getModel(NEUROML_ID);
			if(neuroml != null)
			{
				URL url = (URL) ((ModelWrapper) model).getModel(URL_ID);
				_scene.getEntities().addAll(getEntitiesFromMorphologies(neuroml)); // if there's any morphology
				_scene.getEntities().addAll(getEntitiesFromNetwork(neuroml, url));
			}
		}
		return _scene;
	}

	// END_HACK

	@Override
	public Scene getSceneFromModel(IModel model, StateTreeRoot stateTree) throws ModelInterpreterException
	{
		try
		{
			return getScene(model);
		}
		catch(Exception e)
		{
			throw new ModelInterpreterException(e);
		}
	}

	/**
	 * @param neuroml
	 * @return
	 */
	public List<Entity> getEntitiesFromMorphologies(NeuroMLDocument neuroml)
	{
		List<Entity> entities = new ArrayList<Entity>();
		List<Morphology> morphologies = neuroml.getMorphology();
		if(morphologies != null)
		{
			for(Morphology m : morphologies)
			{
				Entity entity = getEntityFromMorphology(m);
				entities.add(entity);
			}
		}
		List<Cell> cells = neuroml.getCell();
		if(cells != null)
		{
			for(Cell c : cells)
			{
				Morphology cellmorphology = c.getMorphology();
				if(cellmorphology != null)
				{
					Entity cell = new Entity();
					cell.setSubentities(getEntitiesFromMorphologyBySegmentGroup(cellmorphology, c.getId()));
					cell.setId(c.getId());
					augmentWithMetaData(cell, c);
					entities.add(cell);
				}
			}
		}
		return entities;
	}

	private static final int MAX_ATTEMPTS = 3;

	/**
	 * @param neuroml
	 * @param scene
	 * @param url
	 * @throws Exception
	 */
	private Collection<Entity> getEntitiesFromNetwork(NeuroMLDocument neuroml, URL url) throws Exception
	{
		Map<String, Entity> entities = new HashMap<String, Entity>();
		String baseURL = url.getFile();
		if(url.getFile().endsWith("nml"))
		{
			baseURL = baseURL.substring(0, baseURL.lastIndexOf("/") + 1);
		}
		List<Network> networks = neuroml.getNetwork();
		NeuroMLConverter neuromlConverter = new NeuroMLConverter();
		for(Network n : networks)
		{
			for(Population p : n.getPopulation())
			{
				boolean localCell = false;
				for(Cell c : neuroml.getCell())

				{
					if(c.getId().equals(p.getComponent()))
					{
						localCell = true;
						break;
					}
				}
				if(localCell)
				{
					// TODO What do we do?
				}
				else
				{
					boolean attemptConnection = true;
					int attempts = 0;
					NeuroMLDocument neuromlComponent = null;
					String component = p.getComponent();
					while(attemptConnection)
					{
						try
						{
							attemptConnection = false;
							attempts++;
							URL componentURL = new URL(url.getProtocol() + "://" + url.getAuthority() + baseURL + component + ".nml");

							neuromlComponent = neuromlConverter.urlToNeuroML(componentURL);
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
					int size = p.getSize().intValue();

					for(int i = 0; i < size; i++)
					{
						// FIXME the position of the population within the network needs to be specified in neuroml
						List<Entity> localEntities = getEntitiesFromMorphologies(neuromlComponent);
						for(Entity e : localEntities)
						{
							e.setId(e.getId() + "[" + i + "]");
							entities.put(e.getId(), e);
						}
					}
					// FIXME what's the purpose of the id here?
					String id = p.getId();

				}
			}
			for(SynapticConnection c : n.getSynapticConnection())
			{
				String from = c.getFrom();
				String to = c.getTo();

				Metadata mPost = new Metadata();
				mPost.setAdditionalProperties(Resources.SYNAPSE.get(), c.getSynapse());
				mPost.setAdditionalProperties(Resources.CONNECTION_TYPE.get(), Resources.POST_SYNAPTIC.get());
				Reference rPost = new Reference();
				rPost.setEntityId(to);
				rPost.setMetadata(mPost);

				Metadata mPre = new Metadata();
				mPre.setAdditionalProperties(Resources.SYNAPSE.get(), c.getSynapse());
				mPre.setAdditionalProperties(Resources.CONNECTION_TYPE.get(), Resources.PRE_SYNAPTIC.get());
				Reference rPre = new Reference();
				rPre.setEntityId(from);
				rPre.setMetadata(mPre);

				if(entities.containsKey(from))
				{
					entities.get(from).getReferences().add(rPost);
				}
				else
				{
					throw new Exception("Reference not found." + from + " was not found in the path of the network file");
				}

				if(entities.containsKey(to))
				{
					entities.get(to).getReferences().add(rPre);
				}
				else
				{
					throw new Exception("Reference not found." + to + " was not found in the path of the network file");
				}
			}
		}
		return entities.values();
	}

	/**
	 * @param entity
	 * @param c
	 */
	private void augmentWithMetaData(Entity entity, Cell c)
	{
		try
		{
			if(c.getBiophysicalProperties() != null)
			{
				Metadata membraneProperties = new Metadata();
				if(c.getBiophysicalProperties().getMembraneProperties() != null)
				{
					List<JAXBElement<?>> membranePropertiesPart = c.getBiophysicalProperties().getMembraneProperties().getChannelPopulationOrChannelDensityOrChannelDensityNernst();
					if(membranePropertiesPart != null)
					{
						for(JAXBElement<?> e : membranePropertiesPart)
						{
							if(e.getName().equals("channelDensity"))
							{
								membraneProperties.setAdditionalProperties(Resources.COND_DENSITY.get(), ((ChannelDensity) e.getValue()).getCondDensity());
							}
							else if(e.getName().equals("specificCapacitance"))
							{
								membraneProperties.setAdditionalProperties(Resources.SPECIFIC_CAPACITANCE.get(), ((ValueAcrossSegOrSegGroup) e.getValue()).getValue());
							}
						}
					}
				}

				Metadata intracellularProperties = new Metadata();
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

				entity.setMetadata(new Metadata());
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
	 * @return
	 */
	private Entity getEntityFromMorphology(Morphology morphology)
	{
		return getEntityFromListOfSegments(morphology.getSegment());
	}

	/**
	 * @param morphology
	 * @param cellId
	 * @return
	 */
	private List<Entity> getEntitiesFromMorphologyBySegmentGroup(Morphology morphology, String cellId)
	{
		Entity allSegments = getEntityFromListOfSegments(morphology.getSegment());
		List<Entity> entities = new ArrayList<Entity>();
		Map<String, List<AGeometry>> segmentGeometries = new HashMap<String, List<AGeometry>>();
		SegmentGroup somaGroup = null;
		SegmentGroup axonGroup = null;
		SegmentGroup dendriteGroup = null;

		if(morphology.getSegmentGroup().isEmpty())
		{
			// there are no segment groups
			entities.add(allSegments);
		}
		else
		{
			for(SegmentGroup sg : morphology.getSegmentGroup())
			{
				// three hardcoded groups :(
				if(sg.getId().equals(SOMA_GROUP))
				{
					somaGroup = sg;
				}
				else if(sg.getId().equals(AXON_GROUP))
				{
					axonGroup = sg;
				}
				else if(sg.getId().equals(DENDRITE_GROUP))
				{
					dendriteGroup = sg;
				}

				if(!sg.getMember().isEmpty())
				{
					segmentGeometries.put(sg.getId(), getGeometriesForGroup(sg, allSegments));
				}

			}

			if(somaGroup != null)
			{
				Entity entity = createEntityForMacroGroup(somaGroup, segmentGeometries, allSegments.getGeometries());
				entity.setId(getGroupId(cellId, somaGroup.getId()));
				entities.add(entity);
			}
			if(axonGroup != null)
			{
				Entity entity = createEntityForMacroGroup(axonGroup, segmentGeometries, allSegments.getGeometries());
				entity.setId(getGroupId(cellId, axonGroup.getId()));
				entities.add(entity);
			}
			if(dendriteGroup != null)
			{
				Entity entity = createEntityForMacroGroup(dendriteGroup, segmentGeometries, allSegments.getGeometries());
				entity.setId(getGroupId(cellId, dendriteGroup.getId()));
				entities.add(entity);
			}

			// this adds all segment groups not contained in the macro groups if any
			for(String sgId : segmentGeometries.keySet())
			{
				Entity entity = new Entity();
				entity.getGeometries().addAll(segmentGeometries.get(sgId));
				entity.setAdditionalProperties(GROUP_PROPERTY, sgId);
				entity.setId(getGroupId(cellId, sgId));
				entities.add(entity);
			}
		}
		return entities;
	}

	/**
	 * @param cellId
	 * @param segmentGroupId
	 * @return
	 */
	private String getGroupId(String cellId, String segmentGroupId)
	{
		return cellId + " " + segmentGroupId;
	}

	/**
	 * @param somaGroup
	 * @param segmentGeometries
	 */
	private Entity createEntityForMacroGroup(SegmentGroup macroGroup, Map<String, List<AGeometry>> segmentGeometries, List<AGeometry> allSegments)
	{
		Entity entity = new Entity();
		entity.setAdditionalProperties(GROUP_PROPERTY, macroGroup.getId());
		for(Include i : macroGroup.getInclude())
		{
			if(segmentGeometries.containsKey(i.getSegmentGroup()))
			{
				entity.getGeometries().addAll(segmentGeometries.get(i.getSegmentGroup()));
			}
		}
		for(Member m : macroGroup.getMember())
		{
			for(AGeometry g : allSegments)
			{
				if(g.getId().equals(m.getSegment().toString()))
				{
					entity.getGeometries().add(g);
					allSegments.remove(g);
					break;
				}
			}
		}
		segmentGeometries.remove(macroGroup.getId());
		return entity;
	}

	/**
	 * @param sg
	 * @param allSegments
	 * @return
	 */
	private List<AGeometry> getGeometriesForGroup(SegmentGroup sg, Entity allSegments)
	{
		List<AGeometry> geometries = new ArrayList<AGeometry>();
		for(Member m : sg.getMember())
		{
			for(AGeometry g : allSegments.getGeometries())
			{
				if(g.getId().equals(m.getSegment().toString()))
				{
					geometries.add(g);
				}
			}
		}
		return geometries;
	}

	/**
	 * @param list
	 * @return
	 */
	private Entity getEntityFromListOfSegments(List<Segment> list)
	{
		Entity entity = new Entity();
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
			entity.getGeometries().add(getCylinderFromSegment(s, parentDistal));
			distalPoints.put(s.getId().toString(), s.getDistal());
		}
		return entity;
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
	private AGeometry getCylinderFromSegment(Segment s, Point3DWithDiam parentDistal)
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

}
