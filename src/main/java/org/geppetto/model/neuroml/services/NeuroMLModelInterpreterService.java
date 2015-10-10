/*******************************************************************************
. * The MIT License (MIT)
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

package org.geppetto.model.neuroml.services;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.beans.ModelInterpreterConfig;
import org.geppetto.core.beans.PathConfiguration;
import org.geppetto.core.conversion.ConversionException;
import org.geppetto.core.data.model.IAspectConfiguration;
import org.geppetto.core.manager.Scope;
import org.geppetto.core.model.AModelInterpreter;
import org.geppetto.core.model.IModel;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.model.geppettomodel.ConnectionType;
import org.geppetto.core.model.runtime.AspectNode;
import org.geppetto.core.model.runtime.AspectSubTreeNode;
import org.geppetto.core.model.runtime.AspectSubTreeNode.AspectTreeType;
import org.geppetto.core.model.runtime.CompositeNode;
import org.geppetto.core.model.runtime.ConnectionNode;
import org.geppetto.core.model.runtime.EntityNode;
import org.geppetto.core.model.runtime.ParameterSpecificationNode;
import org.geppetto.core.model.runtime.TextMetadataNode;
import org.geppetto.core.model.runtime.VisualObjectReferenceNode;
import org.geppetto.core.model.values.StringValue;
import org.geppetto.core.services.ModelFormat;
import org.geppetto.core.services.registry.ServicesRegistry;
import org.geppetto.core.utilities.VariablePathSerializer;
import org.geppetto.core.visualisation.model.Point;
import org.geppetto.model.neuroml.features.LEMSParametersFeature;
import org.geppetto.model.neuroml.features.LEMSSimulationTreeFeature;
import org.geppetto.model.neuroml.features.NeuroMLVisualTreeFeature;
import org.geppetto.model.neuroml.utils.LEMSAccessUtility;
import org.geppetto.model.neuroml.utils.NeuroMLAccessUtility;
import org.geppetto.model.neuroml.utils.OptimizedLEMSReader;
import org.geppetto.model.neuroml.utils.Resources;
import org.geppetto.model.neuroml.utils.ResourcesDomainType;
import org.geppetto.model.neuroml.utils.modeltree.PopulateGeneralModelTreeUtils;
import org.geppetto.model.neuroml.utils.modeltree.PopulateModelTree;
import org.geppetto.model.neuroml.utils.modeltree.PopulateNeuroMLModelTreeUtils;
import org.geppetto.model.neuroml.utils.modeltree.PopulateNodesModelTreeUtils;
import org.lemsml.jlems.api.LEMSDocumentReader;
import org.lemsml.jlems.api.interfaces.ILEMSDocument;
import org.lemsml.jlems.api.interfaces.ILEMSDocumentReader;
import org.lemsml.jlems.core.sim.ContentError;
import org.lemsml.jlems.core.type.Lems;
import org.lemsml.jlems.io.xmlio.XMLSerializer;
import org.neuroml.model.Base;
import org.neuroml.model.BaseCell;
import org.neuroml.model.BaseConductanceBasedSynapse;
import org.neuroml.model.Cell;
import org.neuroml.model.Instance;
import org.neuroml.model.Location;
import org.neuroml.model.Network;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.Population;
import org.neuroml.model.PopulationTypes;
import org.neuroml.model.Projection;
import org.neuroml.model.util.NeuroMLConverter;
import org.neuroml.model.util.NeuroMLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author matteocantarelli
 * @author Adrian Quintana (adrian.perez@ucl.ac.uk)
 * 
 */
@Service
public class NeuroMLModelInterpreterService extends AModelInterpreter
{
	private NeuroMLAccessUtility neuroMLAccessUtility = new NeuroMLAccessUtility();

	private static Log _logger = LogFactory.getLog(NeuroMLModelInterpreterService.class);

	// helper class to populate model tree
	private PopulateModelTree populateModelTree = new PopulateModelTree();

	@Autowired
	private ModelInterpreterConfig neuroMLModelInterpreterConfig;

	private PopulateNeuroMLModelTreeUtils populateNeuroMLModelTreeUtils = new PopulateNeuroMLModelTreeUtils();

	private ModelWrapper model;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openworm.simulationengine.core.model.IModelProvider#readModel(java .lang.String)
	 */
	public IModel readModel(URL url, List<URL> recordings, String instancePath) throws ModelInterpreterException
	{
		dependentModels.clear();
		model = new ModelWrapper(instancePath);
		try
		{
			OptimizedLEMSReader reader = new OptimizedLEMSReader(this.dependentModels);
			int index = url.toString().lastIndexOf('/');
			String urlBase = url.toString().substring(0, index + 1);
			reader.read(url, urlBase, OptimizedLEMSReader.NMLDOCTYPE.NEUROML); // expand it to have all the inclusions

			/*
			 * LEMS
			 */
			long start = System.currentTimeMillis();
			ILEMSDocumentReader lemsReader = new LEMSDocumentReader();
			ILEMSDocument lemsDocument = lemsReader.readModel(reader.getLEMSString());
			_logger.info("Parsed LEMS document, took " + (System.currentTimeMillis() - start) + "ms");
			/*
			 * PrintWriter out = new PrintWriter("LEMS.txt"); out.println(reader.getLEMSString()); out.close();
			 */
			/*
			 * NEUROML
			 */
			start = System.currentTimeMillis();
			NeuroMLConverter neuromlConverter = new NeuroMLConverter();
			NeuroMLDocument neuroml = neuromlConverter.loadNeuroML(reader.getNeuroMLString());
			_logger.info("Parsed NeuroML document of size " + reader.getNeuroMLString().length() / 1024 + "KB, took " + (System.currentTimeMillis() - start) + "ms");
			/*
			 * out = new PrintWriter("NEUROML.txt"); out.println(reader.getNeuroMLString()); out.close();
			 */
			/*
			 * CREATE MODEL WRAPPER
			 */
			model = new ModelWrapper(UUID.randomUUID().toString());
			model.setInstancePath(instancePath);

			model.wrapModel(ServicesRegistry.getModelFormat("LEMS"), lemsDocument);
			model.wrapModel(ServicesRegistry.getModelFormat("NEUROML"), neuroml);
			model.wrapModel(NeuroMLAccessUtility.URL_ID, url);

			// TODO: This need to be changed (BaseCell, String)
			model.wrapModel(NeuroMLAccessUtility.SUBENTITIES_MAPPING_ID, new HashMap<String, EntityNode>());
			model.wrapModel(NeuroMLAccessUtility.CELL_SUBENTITIES_MAPPING_ID, new HashMap<String, BaseCell>());

			model.wrapModel(NeuroMLAccessUtility.DISCOVERED_COMPONENTS, new HashMap<String, Base>());
			model.wrapModel(LEMSAccessUtility.DISCOVERED_LEMS_COMPONENTS, new HashMap<String, Object>());
			model.wrapModel(NeuroMLAccessUtility.DISCOVERED_NESTED_COMPONENTS_ID, new ArrayList<String>());

			addRecordings(recordings, instancePath, model);

			// add visual tree feature to the model service
			NeuroMLVisualTreeFeature visualTreeFeature = new NeuroMLVisualTreeFeature();
			this.addFeature(visualTreeFeature);
			this.addFeature(new LEMSParametersFeature(this.populateModelTree, model));
			this.addFeature(new LEMSSimulationTreeFeature());
		}
		catch(IOException e)
		{
			throw new ModelInterpreterException(e);
		}
		catch(ContentError e)
		{
			throw new ModelInterpreterException(e);
		}
		catch(NeuroMLException e)
		{
			throw new ModelInterpreterException(e);
		}
		return model;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geppetto.core.model.IModelInterpreter#populateModelTree(org.geppetto .core.model.runtime.AspectNode)
	 */
	@Override
	public boolean populateModelTree(AspectNode aspectNode) throws ModelInterpreterException
	{

		boolean modified = false;

		AspectSubTreeNode modelTree = (AspectSubTreeNode) aspectNode.getSubTree(AspectTreeType.MODEL_TREE);
		modelTree.setId(AspectTreeType.MODEL_TREE.toString());

		IModel model = aspectNode.getModel();
		try
		{
			NeuroMLDocument neuroml = (NeuroMLDocument) ((ModelWrapper) model).getModel(ServicesRegistry.getModelFormat("NEUROML"));
			if(neuroml != null)
			{
				modified = populateModelTree.populateModelTree(modelTree, ((ModelWrapper) model));
				modelTree.setModified(modified);
			}

		}
		catch(Exception e)
		{
			throw new ModelInterpreterException(e);
		}
		return modified;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geppetto.core.model.IModelInterpreter#populateRuntimeTree(org.geppetto .core.model.runtime.AspectNode)
	 */
	@Override
	public boolean populateRuntimeTree(AspectNode aspectNode) throws ModelInterpreterException
	{
		AspectSubTreeNode modelTree = (AspectSubTreeNode) aspectNode.getSubTree(AspectTreeType.MODEL_TREE);
		AspectSubTreeNode visualizationTree = (AspectSubTreeNode) aspectNode.getSubTree(AspectTreeType.VISUALIZATION_TREE);
		AspectSubTreeNode simulationTree = (AspectSubTreeNode) aspectNode.getSubTree(AspectTreeType.SIMULATION_TREE);

		modelTree.setId(AspectTreeType.MODEL_TREE.toString());
		visualizationTree.setId(AspectTreeType.VISUALIZATION_TREE.toString());
		simulationTree.setId(AspectTreeType.SIMULATION_TREE.toString());
		populateSubEntities(aspectNode);
		return true;
	}

	/**
	 * @param aspectNode
	 * @param _model
	 * @throws ModelInterpreterException
	 * @throws MalformedURLException
	 * @throws JAXBException
	 */
	/**
	 * @param aspectNode
	 * @throws ModelInterpreterException
	 */
	private void populateSubEntities(AspectNode aspectNode) throws ModelInterpreterException
	{
		long start = System.currentTimeMillis();
		NeuroMLDocument nmlDoc = (NeuroMLDocument) ((ModelWrapper) aspectNode.getModel()).getModel(ServicesRegistry.getModelFormat("NEUROML"));
		if(nmlDoc != null)
		{
			// Pure LEMS document don't have a neuroml document
			extractSubEntities(aspectNode, nmlDoc);
			_logger.info("Extracted subEntities, took " + (System.currentTimeMillis() - start) + "ms");
		}
	}

	private void extractSubEntities(AspectNode aspectNode, NeuroMLDocument neuroml) throws ModelInterpreterException
	{
		URL url = (URL) ((ModelWrapper) aspectNode.getModel()).getModel(NeuroMLAccessUtility.URL_ID);

		List<Network> networks = neuroml.getNetwork();
		if(networks == null || networks.size() == 0)
		{
			// no network, if there is just a single cell we will add it to cell mapping
			List<Cell> cells = neuroml.getCell();
			if(cells != null && cells.size() == 1)
			{
				Map<String, BaseCell> cellMapping = (Map<String, BaseCell>) ((ModelWrapper) aspectNode.getModel()).getModel(NeuroMLAccessUtility.CELL_SUBENTITIES_MAPPING_ID);
				cellMapping.put(aspectNode.getParentEntity().getId(), cells.get(0));
			}
		}
		else if(networks.size() == 1)
		{
			// there's only one network, we consider the entity for it our
			// network entity
			EntityNode networkEntity = aspectNode.getParentEntity();
			networkEntity.setDomainType(ResourcesDomainType.NETWORK.get());
			addNetworkSubEntities(networks.get(0), networkEntity, url, aspectNode, (ModelWrapper) aspectNode.getModel());
			createConnections(networks.get(0), aspectNode);
		}
		else if(networks.size() > 1)
		{
			// there's more than one network, each network will become an entity
			for(Network n : networks)
			{
				EntityNode networkEntity = new EntityNode(n.getId());
				networkEntity.setDomainType(ResourcesDomainType.NETWORK.get());
				addNetworkSubEntities(n, networkEntity, url, aspectNode, (ModelWrapper) aspectNode.getModel());
				createConnections(n, aspectNode);
				aspectNode.getChildren().add(networkEntity);
			}
		}
	}

	private void createConnections(Network network, AspectNode aspectNode) throws ModelInterpreterException
	{
		long start = System.currentTimeMillis();
		ModelWrapper model = ((ModelWrapper) aspectNode.getModel());
		String aspectNodeName = aspectNode.getName();
		Map<String, EntityNode> mapping = (Map<String, EntityNode>) model.getModel(NeuroMLAccessUtility.SUBENTITIES_MAPPING_ID);

		for(Projection projection : network.getProjection())
		{

			for(org.neuroml.model.Connection connection : projection.getConnection())
			{
				// Theoretically cellid and postif cannot be null but...
				if(connection.getPreCellId() != null && connection.getPostCellId() != null)
				{
					ConnectionNode connectionNodeFrom = new ConnectionNode(projection.getId() + connection.getId(), aspectNode);
					ConnectionNode connectionNodeTo = new ConnectionNode(projection.getId() + connection.getId(), aspectNode);

					// Get connections entities
					String preCellId = PopulateGeneralModelTreeUtils.parseCellRefStringForCellNum(connection.getPreCellId());
					String postCellId = PopulateGeneralModelTreeUtils.parseCellRefStringForCellNum(connection.getPostCellId());
					EntityNode entityNodeFrom = mapping.get(VariablePathSerializer.getArrayName(projection.getPresynapticPopulation(), preCellId));
					EntityNode entityNodeTo = mapping.get(VariablePathSerializer.getArrayName(projection.getPostsynapticPopulation(), postCellId));

					connectionNodeFrom.setName(Resources.CONNECTIONTO.get() + " " + entityNodeTo.getId() + " (" + projection.getId() + "_" + connection.getId() + ")");
					connectionNodeTo.setName(Resources.CONNECTIONFROM.get() + " " + entityNodeFrom.getId() + " (" + projection.getId() + "_" + connection.getId() + ")");

					// Extract the aspect from the origin and destinity
					AspectNode aspectNodeFrom = null;
					AspectNode aspectNodeTo = null;
					for(AspectNode aspectNodeItem : entityNodeFrom.getAspects())
					{
						if(aspectNodeItem.getId().equals(aspectNodeName))
						{
							aspectNodeFrom = aspectNodeItem;
							break;
						}
					}
					for(AspectNode aspectNodeItem : entityNodeTo.getAspects())
					{
						if(aspectNodeItem.getId().equals(aspectNodeName))
						{
							aspectNodeTo = aspectNodeItem;
							break;
						}
					}

					// Store Projection Id
					TextMetadataNode c1 = PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.PROJECTION_ID.getId(), Resources.PROJECTION_ID.get(), new StringValue(projection.getId()
							.toString()));
					connectionNodeFrom.getCustomNodes().add(c1);
					TextMetadataNode c2 = PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.PROJECTION_ID.getId(), Resources.PROJECTION_ID.get(), new StringValue(projection.getId()
							.toString()));
					connectionNodeTo.getCustomNodes().add(c2);

					c1.setParent(aspectNodeFrom);
					c2.setParent(connectionNodeTo);

					// Store Connection Id
					TextMetadataNode p1 = PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.CONNECTION_ID.getId(), Resources.CONNECTION_ID.get(), new StringValue(connection.getId()
							.toString()));
					connectionNodeFrom.getCustomNodes().add(p1);
					TextMetadataNode p2 = PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.CONNECTION_ID.getId(), Resources.CONNECTION_ID.get(), new StringValue(connection.getId()
							.toString()));
					connectionNodeTo.getCustomNodes().add(p2);

					p1.setParent(connectionNodeFrom);
					p2.setParent(connectionNodeTo);

					// Store PreSegment and PostSegment as VisualReferenceNode
					if(connection.getPreSegmentId() != null)
					{
						VisualObjectReferenceNode visualObjectReferenceNode = new VisualObjectReferenceNode(projection.getId() + connection.getId() + connection.getPreSegmentId());
						visualObjectReferenceNode.setName(Resources.PRESEGMENT.get());
						String[] path = connection.getPreCellId().split("/");
						String cellName = path[path.length - 1];
						visualObjectReferenceNode.setVisualObjectId(cellName + "." + connection.getPreSegmentId().toString());
						visualObjectReferenceNode.setAspectInstancePath(aspectNodeFrom.getInstancePath());
						connectionNodeFrom.getVisualReferences().add(visualObjectReferenceNode);
						connectionNodeTo.getVisualReferences().add(visualObjectReferenceNode);
					}
					if(connection.getPostSegmentId() != null)
					{
						VisualObjectReferenceNode visualObjectReferenceNode = new VisualObjectReferenceNode(projection.getId() + connection.getId() + connection.getPostSegmentId());
						visualObjectReferenceNode.setName(Resources.POSTSEGMENT.get());
						String[] path = connection.getPostCellId().split("/");
						String cellName = path[path.length - 1];
						visualObjectReferenceNode.setVisualObjectId(cellName + "." + connection.getPostSegmentId().toString());
						visualObjectReferenceNode.setAspectInstancePath(aspectNodeTo.getInstancePath());
						connectionNodeFrom.getVisualReferences().add(visualObjectReferenceNode);
						connectionNodeTo.getVisualReferences().add(visualObjectReferenceNode);
					}

					// Store PreFraction and PostFraction as CustomNodes
					if(connection.getPreFractionAlong() != null)
					{
						TextMetadataNode prefractionalongNode = PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.PREFRACTIONALONG.get(), Resources.PREFRACTIONALONG.getId(),
								new StringValue(String.valueOf(connection.getPreFractionAlong())));
						prefractionalongNode.setDomainType(ResourcesDomainType.PREFRACTIONALONG.get());
						connectionNodeFrom.getCustomNodes().add(prefractionalongNode);
						connectionNodeTo.getCustomNodes().add(prefractionalongNode);
					}
					if(connection.getPostFractionAlong() != null)
					{
						TextMetadataNode postFractionAlongNode = PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.POSTFRACTIONALONG.get(), Resources.POSTFRACTIONALONG.getId(),
								new StringValue(String.valueOf(connection.getPostFractionAlong())));
						postFractionAlongNode.setDomainType(ResourcesDomainType.POSTFRACTIONALONG.get());
						connectionNodeFrom.getCustomNodes().add(postFractionAlongNode);
						connectionNodeTo.getCustomNodes().add(postFractionAlongNode);
					}

					// Store Synapses as CustomNodes
					CompositeNode synapsesNode;
					try
					{
						synapsesNode = populateNeuroMLModelTreeUtils.createSynapseNode((BaseConductanceBasedSynapse) neuroMLAccessUtility.getComponent(projection.getSynapse(), model,
								Resources.SYNAPSE));
						synapsesNode.setDomainType(ResourcesDomainType.SYNAPSE.get());
					}
					catch(ContentError | ModelInterpreterException e)
					{
						throw new ModelInterpreterException(e);
					}
					connectionNodeFrom.getCustomNodes().add(synapsesNode);
					connectionNodeTo.getCustomNodes().add(synapsesNode);
					synapsesNode.setParent(connectionNodeFrom);

					connectionNodeFrom.setType(ConnectionType.FROM);
					connectionNodeTo.setType(ConnectionType.TO);

					// Store Path to entity connection points to and set the
					// parent
					connectionNodeFrom.setEntityInstancePath(entityNodeTo.getInstancePath());
					connectionNodeFrom.setParent(entityNodeFrom);
					connectionNodeTo.setEntityInstancePath(entityNodeFrom.getInstancePath());
					connectionNodeTo.setParent(entityNodeTo);

					entityNodeFrom.getConnections().add(connectionNodeFrom);
					entityNodeTo.getConnections().add(connectionNodeTo);
				}
			}
		}
		_logger.info("Extracted connections, took " + (System.currentTimeMillis() - start) + "ms");
	}

	/**
	 * @param n
	 * @param parentEntity
	 * @param url
	 * @param aspect
	 * @param neuroml
	 * @throws MalformedURLException
	 * @throws JAXBException
	 * @throws ModelInterpreterException
	 * @throws ContentError
	 */
	private void addNetworkSubEntities(Network n, EntityNode parentEntity, URL url, AspectNode aspect, ModelWrapper model) throws ModelInterpreterException
	{
		if(n.getPopulation().size() == 1 && n.getPopulation().get(0).getSize().equals(BigInteger.ONE))
		{
			// there's only one cell whose name is the same as the geppetto
			// entity, don't create any subentities
			BaseCell cell = (BaseCell) neuroMLAccessUtility.getComponent(n.getPopulation().get(0).getComponent(), model, Resources.CELL);
			parentEntity.setDomainType(ResourcesDomainType.CELL.get());
			mapCellIdToEntity(parentEntity.getId(), parentEntity, aspect, cell);
			return;
		}
		for(Population p : n.getPopulation())
		{
			// BaseCell cell = getCell(p, url, model);
			BaseCell cell = (BaseCell) neuroMLAccessUtility.getComponent(p.getComponent(), model, Resources.CELL);
			if(p.getType() != null && p.getType().equals(PopulationTypes.POPULATION_LIST))
			{
				int i = 0;
				for(Instance instance : p.getInstance())
				{
					String id = VariablePathSerializer.getArrayName(p.getId(), i);
					EntityNode e = getEntityNodefromCell(cell, id, aspect);
					e.setDomainType(ResourcesDomainType.CELL.get());

					if(instance.getLocation() != null)
					{
						e.setPosition(getPoint(instance.getLocation()));
					}
					e.setId(id);
					parentEntity.addChild(e);
					i++;
				}

			}
			else
			{
				int size = p.getSize().intValue();

				for(int i = 0; i < size; i++)
				{
					// FIXME the position of the population within the network
					// needs to be specified in neuroml
					String id = VariablePathSerializer.getArrayName(p.getId(), i);
					// TODO why do we need the cell?
					EntityNode e = getEntityNodefromCell(cell, id, aspect);
					e.setDomainType(ResourcesDomainType.CELL.get());
					e.setId(id);
					parentEntity.addChild(e);
				}
			}
		}

	}

	/**
	 * @param c
	 * @param id
	 * @param parentAspectNode
	 * @return
	 */
	private EntityNode getEntityNodefromCell(BaseCell c, String id, AspectNode parentAspectNode)
	{
		EntityNode entity = new EntityNode(id);
		AspectNode aspectNode = new AspectNode(parentAspectNode.getId());
		aspectNode.setParent(entity);
		aspectNode.setId(parentAspectNode.getId());
		aspectNode.setModelInterpreter(parentAspectNode.getModelInterpreter());
		aspectNode.setModel(parentAspectNode.getModel());
		entity.getAspects().add(aspectNode);
		AspectSubTreeNode modelTree = (AspectSubTreeNode) aspectNode.getSubTree(AspectTreeType.MODEL_TREE);
		AspectSubTreeNode visualizationTree = (AspectSubTreeNode) aspectNode.getSubTree(AspectTreeType.VISUALIZATION_TREE);
		AspectSubTreeNode simulationTree = (AspectSubTreeNode) aspectNode.getSubTree(AspectTreeType.SIMULATION_TREE);
		modelTree.setId(AspectTreeType.MODEL_TREE.toString());
		visualizationTree.setId(AspectTreeType.VISUALIZATION_TREE.toString());
		simulationTree.setId(AspectTreeType.SIMULATION_TREE.toString());
		mapCellIdToEntity(id, entity, parentAspectNode, c);
		return entity;
	}

	/**
	 * @param cell
	 * @param entity
	 */
	private void mapCellIdToEntity(String id, EntityNode entity, AspectNode parentEntityAspect, BaseCell c)
	{
		Map<String, EntityNode> mapping = (Map<String, EntityNode>) ((ModelWrapper) parentEntityAspect.getModel()).getModel(NeuroMLAccessUtility.SUBENTITIES_MAPPING_ID);
		mapping.put(id, entity);
		// TODO: This can be useful when the model is requested for a subentity
		Map<String, BaseCell> cellMapping = (Map<String, BaseCell>) ((ModelWrapper) parentEntityAspect.getModel()).getModel(NeuroMLAccessUtility.CELL_SUBENTITIES_MAPPING_ID);
		cellMapping.put(id, c);
	}

	/**
	 * @param cell
	 * @return
	 */
	// public static AspectSubTreeNode getSubEntityAspectSubTreeNode(BaseCell
	// cell, AspectSubTreeNode.AspectTreeType type, AspectNode aspect,
	// ModelWrapper model)
	// {
	// EntityNode entity = ((Map<BaseCell, EntityNode>)
	// model.getModel(NeuroMLAccessUtility.SUBENTITIES_MAPPING_ID2)).get(cell);
	// for(AspectNode a : entity.getAspects())
	// {
	// if(a.getId().equals(aspect.getId()))
	// {
	// return a.getSubTree(type);
	// }
	// }
	// return null;
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geppetto.core.model.IModelInterpreter#getName()
	 */
	@Override
	public String getName()
	{
		return this.neuroMLModelInterpreterConfig.getModelInterpreterName();
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
	public void registerGeppettoService()
	{
		List<ModelFormat> modelFormats = new ArrayList<ModelFormat>(Arrays.asList(ServicesRegistry.registerModelFormat("NEUROML"), ServicesRegistry.registerModelFormat("LEMS")));
		ServicesRegistry.registerModelInterpreterService(this, modelFormats);
	}

	@Override
	public File downloadModel(AspectNode aspectNode, ModelFormat format, IAspectConfiguration aspectConfiguration) throws ModelInterpreterException
	{
		if(format.equals(ServicesRegistry.getModelFormat("LEMS")) || format.equals(ServicesRegistry.getModelFormat("NEUROML")))
		{
			try
			{
				// Create file and folder
				File outputFolder = PathConfiguration.createFolderInProjectTmpFolder(getScope(), projectId, PathConfiguration.getName(format.getModelFormat()+ PathConfiguration.downloadModelFolderName,true));
				String outputFile = ((URL) ((ModelWrapper) aspectNode.getModel()).getModel(NeuroMLAccessUtility.URL_ID)).getPath();

				// Serialise objects
				String serialisedModel = "";
				if(format.equals(ServicesRegistry.getModelFormat("LEMS")))
				{
					// Serialise LEMS object
					Lems lems = (Lems) ((ModelWrapper) aspectNode.getModel()).getModel(ServicesRegistry.getModelFormat("LEMS"));
					serialisedModel = XMLSerializer.serialize(lems);
				}
				else
				{
					// Serialise NEUROML object
					NeuroMLDocument neuroMLDoc = (NeuroMLDocument) ((ModelWrapper) aspectNode.getModel()).getModel(ServicesRegistry.getModelFormat("NEUROML"));
					NeuroMLConverter neuroMLConverter = new NeuroMLConverter();
					serialisedModel = neuroMLConverter.neuroml2ToXml(neuroMLDoc);
					// Change extension to nml
					outputFile = outputFile.substring(0, outputFile.lastIndexOf(".") + 1) + "nml";
				}

				// Write to disc
				PrintWriter writer = new PrintWriter(outputFolder + outputFile.substring(outputFile.lastIndexOf(File.separator)));
				writer.print(serialisedModel);
				writer.close();
				return outputFolder;

			}
			catch(ContentError | IOException | NeuroMLException e)
			{
				throw new ModelInterpreterException(e);
			}

		}
		else
		{
			// Call conversion service
			LEMSConversionService lemsConversionService = new LEMSConversionService();
			lemsConversionService.setProjectId(projectId);
			lemsConversionService.setScope(Scope.CONNECTION);
			ModelWrapper outputModel = null;
			try
			{
				outputModel = (ModelWrapper) lemsConversionService.convert(aspectNode.getModel(), ServicesRegistry.getModelFormat("LEMS"), format, aspectConfiguration);
			}
			catch(ConversionException e)
			{
				throw new ModelInterpreterException(e);
			}
			String outputFile = (String) outputModel.getModel(format);
			return new File(outputFile.substring(0, outputFile.lastIndexOf(File.separator)));
		}
	}

	@Override
	public List<ModelFormat> getSupportedOutputs(AspectNode aspectNode) throws ModelInterpreterException
	{
		List<ModelFormat> supportedOutputs = super.getSupportedOutputs(aspectNode);
		supportedOutputs.add(ServicesRegistry.getModelFormat("LEMS"));
		try
		{
			LEMSConversionService lemsConversionService = new LEMSConversionService();
			supportedOutputs.addAll(lemsConversionService.getSupportedOutputs(aspectNode.getModel(), ServicesRegistry.getModelFormat("LEMS")));
		}
		catch(ConversionException e)
		{
			throw new ModelInterpreterException(e);
		}
		return supportedOutputs;
	}

	public PopulateModelTree getPopulateModelTree()
	{
		return this.populateModelTree;
	}

	public ModelWrapper getModel()
	{
		return this.model;
	}

	public Map<ParameterSpecificationNode, Object> getMethodsMap()
	{
		return this.populateModelTree.getParametersNodeToMethodsMap();
	}

	public Map<ParameterSpecificationNode, Object> getObjectsMap()
	{
		return this.populateModelTree.getParametersNodeToObjectsMap();
	}

}
