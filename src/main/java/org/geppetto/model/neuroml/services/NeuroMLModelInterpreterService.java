/*******************************************************************************
. * The MIT License (MIT)
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
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.beans.ModelInterpreterConfig;
import org.geppetto.core.model.IModel;
import org.geppetto.core.model.IModelInterpreter;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.model.runtime.AspectNode;
import org.geppetto.core.model.runtime.AspectSubTreeNode;
import org.geppetto.core.model.runtime.AspectSubTreeNode.AspectTreeType;
import org.geppetto.core.model.runtime.EntityNode;
import org.geppetto.core.utilities.VariablePathSerializer;
import org.geppetto.core.visualisation.model.Point;
import org.geppetto.model.neuroml.utils.LEMSAccessUtility;
import org.geppetto.model.neuroml.utils.NeuroMLAccessUtility;
import org.geppetto.model.neuroml.utils.OptimizedLEMSReader;
import org.geppetto.model.neuroml.utils.PopulateModelTree;
import org.geppetto.model.neuroml.utils.ResourcesSuffix;
import org.lemsml.jlems.core.api.LEMSDocumentReader;
import org.lemsml.jlems.core.api.interfaces.ILEMSDocument;
import org.lemsml.jlems.core.api.interfaces.ILEMSDocumentReader;
import org.lemsml.jlems.core.sim.ContentError;
import org.lemsml.jlems.core.type.Component;
import org.neuroml.model.Base;
import org.neuroml.model.BaseCell;
import org.neuroml.model.Instance;
import org.neuroml.model.Location;
import org.neuroml.model.Network;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.Population;
import org.neuroml.model.PopulationTypes;
import org.neuroml.model.util.NeuroMLConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author matteocantarelli
 * 
 */
@Service
public class NeuroMLModelInterpreterService implements IModelInterpreter
{


	private NeuroMLAccessUtility neuroMLAccessUtility = new NeuroMLAccessUtility();

	private static Log _logger = LogFactory.getLog(NeuroMLModelInterpreterService.class);

	// helper class to populate model tree
	private PopulateModelTree populateModelTree = new PopulateModelTree();

	@Autowired
	private ModelInterpreterConfig neuroMLModelInterpreterConfig;
	
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openworm.simulationengine.core.model.IModelProvider#readModel(java .lang.String)
	 */
	public IModel readModel(URL url, List<URL> recordings, String instancePath) throws ModelInterpreterException
	{
		ModelWrapper model = new ModelWrapper(instancePath);
		try
		{
			Scanner scanner = new Scanner(url.openStream(), "UTF-8");
			String neuroMLString = scanner.useDelimiter("\\A").next();
			scanner.close();
			String lemsString = NeuroMLConverter.convertNeuroML2ToLems(neuroMLString);

			ILEMSDocumentReader lemsReader = new LEMSDocumentReader();
//			ILEMSDocument document = lemsReader.readModel(lemsString);

			OptimizedLEMSReader reader = new OptimizedLEMSReader();
			String lemsStringOptimized = reader.processLEMSInclusions(lemsString);
			ILEMSDocument document = lemsReader.readModel(lemsStringOptimized);
			
//			String lemsStringUtils = URLReader.readStringFromURL(this.getClass().getResource("/NEUROML2BETA"));
//			ILEMSDocument documentLemsUtils = lemsReader.readModel(lemsStringUtils);

			NeuroMLConverter neuromlConverter = new NeuroMLConverter();
			NeuroMLDocument neuroml = neuromlConverter.urlToNeuroML(url);

			model = new ModelWrapper(UUID.randomUUID().toString());
			model.setInstancePath(instancePath);
			// two different interpretations of the same file, one used to simulate the other used to visualize
			model.wrapModel(NeuroMLAccessUtility.LEMS_ID, document);
			model.wrapModel(NeuroMLAccessUtility.NEUROML_ID, neuroml);
			model.wrapModel(NeuroMLAccessUtility.URL_ID, url);
			model.wrapModel(NeuroMLAccessUtility.SUBENTITIES_MAPPING_ID, new HashMap<String, EntityNode>());
			model.wrapModel(NeuroMLAccessUtility.DISCOVERED_COMPONENTS, new HashMap<String, Base>());
			model.wrapModel(LEMSAccessUtility.DISCOVERED_LEMS_COMPONENTS, new HashMap<String, Component>());
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
		return model;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geppetto.core.model.IModelInterpreter#populateModelTree(org.geppetto.core.model.runtime.AspectNode)
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
			NeuroMLDocument neuroml = (NeuroMLDocument) ((ModelWrapper) model).getModel(NeuroMLAccessUtility.NEUROML_ID);
			if(neuroml != null)
			{
//				URL url = (URL) ((ModelWrapper) model).getModel(URL_ID);
				// Use local class to populate model tree
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
	 * @see org.geppetto.core.model.IModelInterpreter#populateRuntimeTree(org.geppetto.core.model.runtime.AspectNode)
	 */
	@Override
	public boolean populateRuntimeTree(AspectNode aspectNode) throws ModelInterpreterException
	{
		AspectSubTreeNode modelTree = (AspectSubTreeNode) aspectNode.getSubTree(AspectTreeType.MODEL_TREE);
		AspectSubTreeNode visualizationTree = (AspectSubTreeNode) aspectNode.getSubTree(AspectTreeType.VISUALIZATION_TREE);

		modelTree.setId(AspectTreeType.MODEL_TREE.toString());
		visualizationTree.setId(AspectTreeType.VISUALIZATION_TREE.toString());
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
		NeuroMLDocument neuroml = (NeuroMLDocument) ((ModelWrapper) aspectNode.getModel()).getModel(NeuroMLAccessUtility.NEUROML_ID);
		URL url = (URL) ((ModelWrapper) aspectNode.getModel()).getModel(NeuroMLAccessUtility.URL_ID);

		List<Network> networks = neuroml.getNetwork();
		if(networks == null || networks.size() == 0)
		{
			// What do we do?
		}
		else if(networks.size() == 1)
		{
			// there's only one network, we consider the entity for it our network entity
			addNetworkSubEntities(networks.get(0), (EntityNode) aspectNode.getParentEntity(), url, aspectNode, (ModelWrapper) aspectNode.getModel());
		}
		else if(networks.size() > 1)
		{
			// there's more than one network, each network will become an entity
			for(Network n : networks)
			{
				EntityNode networkEntity = new EntityNode(n.getId());
				addNetworkSubEntities(n, networkEntity, url, aspectNode, (ModelWrapper) aspectNode.getModel());
				aspectNode.getChildren().add(networkEntity);
			}
		}
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
		if(n.getPopulation().size() == 1 && parentEntity.getName().equals(n.getPopulation().get(0).getComponent()) && n.getPopulation().get(0).getSize().equals(BigInteger.ONE))
		{
			// there's only one cell whose name is the same as the geppetto entity, don't create any subentities
			neuroMLAccessUtility.getComponent(n.getPopulation().get(0).getComponent(), model, ResourcesSuffix.CELL);
			mapCellIdToEntity(parentEntity.getId(), parentEntity, aspect);
			return;
		}
		for(Population p : n.getPopulation())
		{
			//BaseCell cell = getCell(p, url, model);
			BaseCell cell = (BaseCell) neuroMLAccessUtility.getComponent(p.getComponent(), model, ResourcesSuffix.CELL);
			if(p.getType() != null && p.getType().equals(PopulationTypes.POPULATION_LIST))
			{
				int i = 0;
				for(Instance instance : p.getInstance())
				{
					String id=VariablePathSerializer.getArrayName(p.getId(), i);
					EntityNode e = getEntityNodefromCell(cell, id, aspect);

					if(instance.getLocation() != null)
					{
						e.setPosition(getPoint(instance.getLocation()));
					}
					e.setId(id);
					parentEntity.addChild(e);
				}
				i++;

			}
			else
			{
				int size = p.getSize().intValue();

				for(int i = 0; i < size; i++)
				{
					// FIXME the position of the population within the network needs to be specified in neuroml
					String id=VariablePathSerializer.getArrayName(cell.getId(), i);
					EntityNode e = getEntityNodefromCell(cell, id, aspect);
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
		entity.getAspects().add(aspectNode);
		AspectSubTreeNode modelTree = (AspectSubTreeNode) aspectNode.getSubTree(AspectTreeType.MODEL_TREE);
		AspectSubTreeNode visualizationTree = (AspectSubTreeNode) aspectNode.getSubTree(AspectTreeType.VISUALIZATION_TREE);
		modelTree.setId(AspectTreeType.MODEL_TREE.toString());
		visualizationTree.setId(AspectTreeType.VISUALIZATION_TREE.toString());
		mapCellIdToEntity(id, entity, parentAspectNode);
		return entity;
	}

	/**
	 * @param cell
	 * @param entity
	 */
	private void mapCellIdToEntity(String id, EntityNode entity, AspectNode parentEntityAspect)
	{
		Map<String, EntityNode> mapping = (Map<String, EntityNode>) ((ModelWrapper) parentEntityAspect.getModel()).getModel(NeuroMLAccessUtility.SUBENTITIES_MAPPING_ID);
		mapping.put(id, entity);
	}

	/**
	 * @param cell
	 * @return
	 */
	public static AspectSubTreeNode getSubEntityAspectSubTreeNode(BaseCell cell, AspectSubTreeNode.AspectTreeType type, AspectNode aspect, ModelWrapper model)
	{
		EntityNode entity = ((Map<BaseCell, EntityNode>) model.getModel(NeuroMLAccessUtility.SUBENTITIES_MAPPING_ID)).get(cell);
		for(AspectNode a : entity.getAspects())
		{
			if(a.getId().equals(aspect.getId()))
			{
				return a.getSubTree(type);
			}
		}
		return null;
	}

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

}
