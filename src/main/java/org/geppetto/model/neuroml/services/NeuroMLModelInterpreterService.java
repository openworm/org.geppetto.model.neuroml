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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;

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
import org.geppetto.core.visualisation.model.Point;
import org.lemsml.jlems.core.api.LEMSDocumentReader;
import org.lemsml.jlems.core.api.interfaces.ILEMSDocument;
import org.lemsml.jlems.core.api.interfaces.ILEMSDocumentReader;
import org.lemsml.jlems.core.sim.ContentError;
import org.neuroml.model.BaseCell;
import org.neuroml.model.Cell;
import org.neuroml.model.Instance;
import org.neuroml.model.Location;
import org.neuroml.model.Network;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.Point3DWithDiam;
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

	private static final String LEMS_ID = "lems";
	private static final String NEUROML_ID = "neuroml";
	private static final String URL_ID = "url";

	private static Log _logger = LogFactory.getLog(NeuroMLModelInterpreterService.class);

	// helper class to populate model tree
	private PopulateModelTree populateModelTree = new PopulateModelTree();

	@Autowired
	private ModelInterpreterConfig neuroMLModelInterpreterConfig;

	// Cached model, the service is recreated for each simulation
	private ModelWrapper _model;
	private Map<String, BaseCell> _discoveredCells = new HashMap<String, BaseCell>();
	private static final int MAX_ATTEMPTS = 3;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openworm.simulationengine.core.model.IModelProvider#readModel(java .lang.String)
	 */
	public IModel readModel(URL url, List<URL> recordings, String instancePath) throws ModelInterpreterException
	{
		if(_model == null)
		{
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

				_model = new ModelWrapper(UUID.randomUUID().toString());
				_model.setInstancePath(instancePath);
				// two different interpretations of the same file, one used to simulate the other used to visualize
				_model.wrapModel(LEMS_ID, document);
				_model.wrapModel(NEUROML_ID, neuroml);
				_model.wrapModel(URL_ID, url);

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
		}
		return _model;
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
			NeuroMLDocument neuroml = (NeuroMLDocument) ((ModelWrapper) model).getModel(NEUROML_ID);
			if(neuroml != null)
			{
				// Use local class to populate model tree
				modified = populateModelTree.populateModelTree(modelTree, neuroml);
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
		populateSubEntities(aspectNode, _model);
		return true;
	}

	public void setModel(ModelWrapper model)
	{
		this._model = model;
	}

	/**
	 * @param aspectNode
	 * @param _model
	 * @throws ModelInterpreterException 
	 * @throws MalformedURLException
	 * @throws JAXBException
	 */
	private void populateSubEntities(AspectNode aspectNode, ModelWrapper _model) throws ModelInterpreterException  
	{
		NeuroMLDocument neuroml = (NeuroMLDocument) _model.getModel(NEUROML_ID);
		URL url = (URL) _model.getModel(URL_ID);

		List<Network> networks = neuroml.getNetwork();
		if(networks.size() > 1)
		{
			// there's more than one network, each network will become an entity
			for(Network n : networks)
			{
				EntityNode networkEntity = new EntityNode(n.getId());
				addNetworkSubEntities(n, networkEntity, url, aspectNode, neuroml);
				aspectNode.getChildren().add(networkEntity);
			}
		}
		else if(networks.size() == 1)
		{
			// there's only one network, we consider the entity for it our network entity
			addNetworkSubEntities(networks.get(0), (EntityNode) aspectNode.getParentEntity(), url, aspectNode, neuroml);
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
	 */
	private void addNetworkSubEntities(Network n, EntityNode parentEntity, URL url, AspectNode aspect, NeuroMLDocument neuroml) throws ModelInterpreterException
	{

		for(Population p : n.getPopulation())
		{
			//let's first check if the cell is of a predefined neuroml type
			BaseCell cell = NeuroMLAccessUtility.getCellById(p.getComponent(), neuroml);

			if(cell == null)
			{
				try
				{
					//otherwise let's check if it's defined in the same folder as the current component 
					cell = retrieveNeuroMLCell(p.getComponent(), url);
				}
				catch(MalformedURLException e)
				{
					throw new ModelInterpreterException(e);
				}
				catch(JAXBException e)
				{
					throw new ModelInterpreterException(e);
				}
			}
			if(cell == null)
			{
				//sorry no luck!
				throw new ModelInterpreterException("Can't find the cell " + p.getComponent());
			}
			if(p.getType() != null && p.getType().equals(PopulationTypes.POPULATION_LIST))
			{
				int i = 0;
				for(Instance instance : p.getInstance())
				{
					EntityNode e = getEntityNodefromCell(cell, p.getId(), aspect);

					if(instance.getLocation() != null)
					{
						e.setPosition(getPoint(instance.getLocation()));
					}
					if(p.getInstance().size() > 1)
					{
						e.setId(p.getId() + "[" + i + "]");
					}
					else
					{
						e.setId(p.getId());
					}
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
					EntityNode e = getEntityNodefromCell(cell, cell.getId(), aspect);
					e.setId(e.getId() + "[" + i + "]");
					parentEntity.addChild(e);
				}
			}
		}

	}

	/**
	 * @param c
	 * @param id
	 * @param aspect
	 * @return
	 */
	private EntityNode getEntityNodefromCell(BaseCell c, String id, AspectNode aspect)
	{
		EntityNode entity = new EntityNode(id);
		AspectNode aspectNode = new AspectNode(aspect.getId());
		aspectNode.setParent(entity);
		entity.getAspects().add(aspectNode);
		AspectSubTreeNode modelTree = (AspectSubTreeNode) aspectNode.getSubTree(AspectTreeType.MODEL_TREE);
		AspectSubTreeNode visualizationTree = (AspectSubTreeNode) aspectNode.getSubTree(AspectTreeType.VISUALIZATION_TREE);
		modelTree.setId(AspectTreeType.MODEL_TREE.toString());
		visualizationTree.setId(AspectTreeType.VISUALIZATION_TREE.toString());
		return entity;
	}

	@Override
	public String getName()
	{
		return this.neuroMLModelInterpreterConfig.getModelInterpreterName();
	}

	/**
	 * @param componentId
	 * @param url
	 * @return
	 * @throws JAXBException
	 * @throws MalformedURLException
	 */
	private BaseCell retrieveNeuroMLCell(String componentId, URL url) throws JAXBException, MalformedURLException
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
