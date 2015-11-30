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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.beans.ModelInterpreterConfig;
import org.geppetto.core.data.model.IAspectConfiguration;
import org.geppetto.core.model.AModelInterpreter;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.services.ModelFormat;
import org.geppetto.core.services.registry.ServicesRegistry;
import org.geppetto.model.GeppettoLibrary;
import org.geppetto.model.Node;
import org.geppetto.model.neuroml.utils.LEMSAccessUtility;
import org.geppetto.model.neuroml.utils.NeuroMLAccessUtility;
import org.geppetto.model.neuroml.utils.OptimizedLEMSReader;
import org.geppetto.model.neuroml.utils.Resources;
import org.geppetto.model.types.CompositeType;
import org.geppetto.model.types.ParameterType;
import org.geppetto.model.types.StateVariableType;
import org.geppetto.model.types.TextType;
import org.geppetto.model.types.Type;
import org.geppetto.model.types.TypesFactory;
import org.geppetto.model.types.VisualType;
import org.geppetto.model.types.impl.TypesFactoryImpl;
import org.geppetto.model.values.PhysicalQuantity;
import org.geppetto.model.values.Pointer;
import org.geppetto.model.values.Text;
import org.geppetto.model.values.Unit;
import org.geppetto.model.values.ValuesFactory;
import org.geppetto.model.values.impl.ValuesFactoryImpl;
import org.geppetto.model.variables.Variable;
import org.geppetto.model.variables.VariablesFactory;
import org.geppetto.model.variables.impl.VariablesFactoryImpl;
import org.lemsml.jlems.api.LEMSDocumentReader;
import org.lemsml.jlems.api.interfaces.ILEMSDocument;
import org.lemsml.jlems.api.interfaces.ILEMSDocumentReader;
import org.lemsml.jlems.core.sim.ContentError;
import org.lemsml.jlems.core.sim.LEMSException;
import org.lemsml.jlems.core.type.Component;
import org.lemsml.jlems.core.type.Exposure;
import org.lemsml.jlems.core.type.Lems;
import org.lemsml.jlems.core.type.ParamValue;
import org.neuroml.export.utils.Utils;
import org.neuroml.model.Base;
import org.neuroml.model.Morphology;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.Standalone;
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
	private static Log _logger = LogFactory.getLog(NeuroMLModelInterpreterService.class);

	@Autowired
	private ModelInterpreterConfig neuroMLModelInterpreterConfig;

	private Map<String, Type> types;

	// AQP: Do we need the model wrapper??
	private ModelWrapper model;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geppetto.core.model.IModelInterpreter#importType(java.net.URL, java.lang.String, org.geppetto.core.library.LibraryManager)
	 */
	@Override
	public Type importType(URL url, String typeName, GeppettoLibrary library) throws ModelInterpreterException
	{

		// AQP: Shall we verify if types != null?

		types = new HashMap<String, Type>();

		dependentModels.clear();
		// model = new ModelWrapper(instancePath);
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
			 * NEUROML
			 */
			start = System.currentTimeMillis();
			NeuroMLConverter neuromlConverter = new NeuroMLConverter();
			NeuroMLDocument neuroml = neuromlConverter.loadNeuroML(reader.getNeuroMLString());
			_logger.info("Parsed NeuroML document of size " + reader.getNeuroMLString().length() / 1024 + "KB, took " + (System.currentTimeMillis() - start) + "ms");

			/*
			 * CREATE MODEL WRAPPER
			 */
			model = new ModelWrapper(UUID.randomUUID().toString());
			//model.setInstancePath(instancePath);

			Lems lems = ((Lems) lemsDocument);
			lems.setResolveModeLoose();
			lems.deduplicate();
			lems.resolve();
			lems.evaluateStatic();
			
//			model.wrapModel(ServicesRegistry.getModelFormat("LEMS"), lemsDocument);
//			model.wrapModel(ServicesRegistry.getModelFormat("NEUROML"), neuroml);
//			model.wrapModel(NeuroMLAccessUtility.URL_ID, url);
//
//			model.wrapModel(NeuroMLAccessUtility.DISCOVERED_COMPONENTS, new HashMap<String, Base>());
//			model.wrapModel(LEMSAccessUtility.DISCOVERED_LEMS_COMPONENTS, new HashMap<String, Object>());
//			model.wrapModel(NeuroMLAccessUtility.DISCOVERED_NESTED_COMPONENTS_ID, new ArrayList<String>());

			// AQP: and this?
			// addRecordings(recordings, instancePath, model);

			for(Component component : lems.getComponents())
			{
				if(!types.containsKey(component.getID())) types.put(component.getID(), extractInfoFromComponent(component));
			}

			// AQP: This may remain.
			// this.addFeature(new LEMSParametersFeature(this.populateModelTree, model));

		}
		catch(IOException | NumberFormatException | NeuroMLException | LEMSException e)
		{
			throw new ModelInterpreterException(e);
		}

		library.getTypes().addAll(types.values());

		if(typeName != null)
		{
			return types.get(typeName);
		}
		// Business rule: If there is a network in the NeuroML file we don't visualize spurious cells which
		// "most likely" are just included types in NeuroML and are instantiated as part of the network
		// populations
		else {
			for (Entry<String, Type> entryType : types.entrySet()){
				if (((Component)entryType.getValue().getDomainModel()).getDeclaredType().equals("network")){
					return entryType.getValue();
				}
			}
			
			//FIXME
			//return types.values();
		}
		
		return null;
	}

	private void initialiseNodeFromComponent(Node node, Component component)
	{
		if (node instanceof Type){
			((Type) node).setDomainModel(component);
		}
		node.setName(Resources.getValueById(component.getDeclaredType()) + ((component.getID() != null) ? " - " + component.getID() : ""));
		node.setId((component.getID() != null) ? component.getID() : component.getDeclaredType());
	}

	private void initialiseNodeFromString(Node node, String attributesName)
	{
		node.setName(Resources.getValueById(attributesName));
		node.setId(attributesName);
	}

	private CompositeType extractInfoFromComponent(Component component) throws NumberFormatException, NeuroMLException, LEMSException
	{
		TypesFactory typeFactory = TypesFactoryImpl.eINSTANCE;
		ValuesFactory valuesFactory = ValuesFactoryImpl.eINSTANCE;
		VariablesFactory variablesFactory = VariablesFactoryImpl.eINSTANCE;

		if(component.getDeclaredType().equals("morphology"))
		{
			VisualType visualType = typeFactory.createVisualType();
			initialiseNodeFromComponent(visualType, component);

			// Convert lems component to NeuroML
			LinkedHashMap<String, Standalone> morphologyMap = Utils.convertLemsComponentToNeuroML(component);
			Morphology morphology = (Morphology) morphologyMap.get(component.getID());

			// AQP: I would like to join processMorphologyFromGroup and processMorphology into just one single method/approach
			// create nodes for visual objects, segments of cell
			// ExtractVisualType extractVisualType = new ExtractVisualType();
			//
			//
			// visualType.getReferencedVariables().addAll(extractVisualType.createCellPartsVisualGroups(morphology.getSegmentGroup()));
			//
			//
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

		}
		else
		{

			CompositeType compositeType = typeFactory.createCompositeType();
			initialiseNodeFromComponent(compositeType, component);

			for(ParamValue pv : component.getParamValues())
			{
				if(component.hasAttribute(pv.getName()))
				{
					String orig = component.getStringValue(pv.getName());

					// AQP: Extracted from PopulateNodesModelTreeUtils
					String regExp = "\\s*([0-9-]*\\.?[0-9]*[eE]?[-+]?[0-9]+)?\\s*(\\w*)";
					Pattern pattern = Pattern.compile(regExp);
					Matcher matcher = pattern.matcher(orig);

					if(matcher.find())
					{
						PhysicalQuantity physicalQuantity = valuesFactory.createPhysicalQuantity();
						// physicalQuantity.setScalingFactor(value);
						physicalQuantity.setValue(Float.parseFloat(matcher.group(1)));

						Unit unit = valuesFactory.createUnit();
						unit.setUnit(matcher.group(2));
						physicalQuantity.setUnit(unit);
						physicalQuantity.setScalingFactor(1);

						ParameterType parameterType = typeFactory.createParameterType();
						initialiseNodeFromString(parameterType, pv.getName());
						parameterType.setDefaultValue(physicalQuantity);

						Variable variable = variablesFactory.createVariable();
						initialiseNodeFromString(variable, pv.getName());
						variable.getAnonymousTypes().add(parameterType);
						compositeType.getVariables().add(variable);
					}
				}
			}

			for(Entry<String, String> entry : component.getTextParamMap().entrySet())
			{
				Text text = valuesFactory.createText();
				text.setText(entry.getValue());

				TextType textType = typeFactory.createTextType();
				initialiseNodeFromString(textType, entry.getKey());
				textType.setDefaultValue(text);

				Variable variable = variablesFactory.createVariable();
				initialiseNodeFromString(textType, entry.getKey());
				variable.getAnonymousTypes().add(textType);
				compositeType.getVariables().add(variable);
			}

			for(Entry<String, Component> entry : component.getRefComponents().entrySet())
			{
				if(!types.containsKey(entry.getKey()))
				{
					CompositeType refCompositeType = extractInfoFromComponent(entry.getValue());

					Variable variable = variablesFactory.createVariable();
					initialiseNodeFromComponent(variable, entry.getValue());
					variable.getTypes().add(refCompositeType);
					compositeType.getVariables().add(variable);
					types.put(entry.getValue().getID(), refCompositeType);
				}
			}

			// Simulation Tree (Variable Node)
			for(Exposure exposure : component.getComponentType().getExposures())
			{
				String unitSymbol = Utils.getSIUnitInNeuroML(exposure.getDimension()).getSymbol();
				if(unitSymbol.equals("none")) unitSymbol = "";

				PhysicalQuantity physicalQuantity = valuesFactory.createPhysicalQuantity();
				// physicalQuantity.setScalingFactor(value);
				Unit unit = valuesFactory.createUnit();
				unit.setUnit(unitSymbol);
				physicalQuantity.setUnit(unit);

				StateVariableType stateVariableType = typeFactory.createStateVariableType();
				initialiseNodeFromString(stateVariableType, exposure.getName());
				stateVariableType.setDefaultValue(physicalQuantity);

				Variable variable = variablesFactory.createVariable();
				initialiseNodeFromString(variable, exposure.getName());
				variable.getAnonymousTypes().add(stateVariableType);
				compositeType.getVariables().add(variable);
			}

			for(Component componentChild : component.getAllChildren())
			{
				CompositeType anonymousCompositeType = extractInfoFromComponent(componentChild);
				if(anonymousCompositeType != null)
				{
					Variable variable = variablesFactory.createVariable();
					initialiseNodeFromComponent(variable, componentChild);
					variable.getAnonymousTypes().add(anonymousCompositeType);
					compositeType.getVariables().add(variable);
				}
			}

			return compositeType;
		}

		return null;
	}

	//
	// private void createConnections(Network network, AspectNode aspectNode) throws ModelInterpreterException
	// {
	// long start = System.currentTimeMillis();
	// ModelWrapper model = ((ModelWrapper) aspectNode.getModel());
	// String aspectNodeName = aspectNode.getName();
	// Map<String, EntityNode> mapping = (Map<String, EntityNode>) model.getModel(NeuroMLAccessUtility.SUBENTITIES_MAPPING_ID);
	//
	// for(Projection projection : network.getProjection())
	// {
	//
	// for(org.neuroml.model.Connection connection : projection.getConnection())
	// {
	// // Theoretically cellid and postif cannot be null but...
	// if(connection.getPreCellId() != null && connection.getPostCellId() != null)
	// {
	// ConnectionNode connectionNodeFrom = new ConnectionNode(projection.getId() + connection.getId(), aspectNode);
	// ConnectionNode connectionNodeTo = new ConnectionNode(projection.getId() + connection.getId(), aspectNode);
	//
	// // Get connections entities
	// String preCellId = PopulateGeneralModelTreeUtils.parseCellRefStringForCellNum(connection.getPreCellId());
	// String postCellId = PopulateGeneralModelTreeUtils.parseCellRefStringForCellNum(connection.getPostCellId());
	// EntityNode entityNodeFrom = mapping.get(VariablePathSerializer.getArrayName(projection.getPresynapticPopulation(), preCellId));
	// EntityNode entityNodeTo = mapping.get(VariablePathSerializer.getArrayName(projection.getPostsynapticPopulation(), postCellId));
	//
	// connectionNodeFrom.setName(Resources.CONNECTIONTO.get() + " " + entityNodeTo.getId() + " (" + projection.getId() + "_" + connection.getId() + ")");
	// connectionNodeTo.setName(Resources.CONNECTIONFROM.get() + " " + entityNodeFrom.getId() + " (" + projection.getId() + "_" + connection.getId() + ")");
	//
	// // Extract the aspect from the origin and destinity
	// AspectNode aspectNodeFrom = null;
	// AspectNode aspectNodeTo = null;
	// for(AspectNode aspectNodeItem : entityNodeFrom.getAspects())
	// {
	// if(aspectNodeItem.getId().equals(aspectNodeName))
	// {
	// aspectNodeFrom = aspectNodeItem;
	// break;
	// }
	// }
	// for(AspectNode aspectNodeItem : entityNodeTo.getAspects())
	// {
	// if(aspectNodeItem.getId().equals(aspectNodeName))
	// {
	// aspectNodeTo = aspectNodeItem;
	// break;
	// }
	// }
	//
	// // Store Projection Id
	// TextMetadataNode c1 = PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.PROJECTION_ID.getId(), Resources.PROJECTION_ID.get(), new StringValue(projection.getId()
	// .toString()));
	// connectionNodeFrom.getCustomNodes().add(c1);
	// TextMetadataNode c2 = PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.PROJECTION_ID.getId(), Resources.PROJECTION_ID.get(), new StringValue(projection.getId()
	// .toString()));
	// connectionNodeTo.getCustomNodes().add(c2);
	//
	// c1.setParent(aspectNodeFrom);
	// c2.setParent(connectionNodeTo);
	//
	// // Store Connection Id
	// TextMetadataNode p1 = PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.CONNECTION_ID.getId(), Resources.CONNECTION_ID.get(), new StringValue(connection.getId()
	// .toString()));
	// connectionNodeFrom.getCustomNodes().add(p1);
	// TextMetadataNode p2 = PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.CONNECTION_ID.getId(), Resources.CONNECTION_ID.get(), new StringValue(connection.getId()
	// .toString()));
	// connectionNodeTo.getCustomNodes().add(p2);
	//
	// p1.setParent(connectionNodeFrom);
	// p2.setParent(connectionNodeTo);
	//
	// // Store PreSegment and PostSegment as VisualReferenceNode
	// if(connection.getPreSegmentId() != null)
	// {
	// VisualObjectReferenceNode visualObjectReferenceNode = new VisualObjectReferenceNode(projection.getId() + connection.getId() + connection.getPreSegmentId());
	// visualObjectReferenceNode.setName(Resources.PRESEGMENT.get());
	// String[] path = connection.getPreCellId().split("/");
	// String cellName = path[path.length - 1];
	// visualObjectReferenceNode.setVisualObjectId(cellName + "." + connection.getPreSegmentId().toString());
	// visualObjectReferenceNode.setAspectInstancePath(aspectNodeFrom.getInstancePath());
	// connectionNodeFrom.getVisualReferences().add(visualObjectReferenceNode);
	// connectionNodeTo.getVisualReferences().add(visualObjectReferenceNode);
	// }
	// if(connection.getPostSegmentId() != null)
	// {
	// VisualObjectReferenceNode visualObjectReferenceNode = new VisualObjectReferenceNode(projection.getId() + connection.getId() + connection.getPostSegmentId());
	// visualObjectReferenceNode.setName(Resources.POSTSEGMENT.get());
	// String[] path = connection.getPostCellId().split("/");
	// String cellName = path[path.length - 1];
	// visualObjectReferenceNode.setVisualObjectId(cellName + "." + connection.getPostSegmentId().toString());
	// visualObjectReferenceNode.setAspectInstancePath(aspectNodeTo.getInstancePath());
	// connectionNodeFrom.getVisualReferences().add(visualObjectReferenceNode);
	// connectionNodeTo.getVisualReferences().add(visualObjectReferenceNode);
	// }
	//
	// // Store PreFraction and PostFraction as CustomNodes
	// if(connection.getPreFractionAlong() != null)
	// {
	// TextMetadataNode prefractionalongNode = PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.PREFRACTIONALONG.get(), Resources.PREFRACTIONALONG.getId(),
	// new StringValue(String.valueOf(connection.getPreFractionAlong())));
	// prefractionalongNode.setDomainType(ResourcesDomainType.PREFRACTIONALONG.get());
	// connectionNodeFrom.getCustomNodes().add(prefractionalongNode);
	// connectionNodeTo.getCustomNodes().add(prefractionalongNode);
	// }
	// if(connection.getPostFractionAlong() != null)
	// {
	// TextMetadataNode postFractionAlongNode = PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.POSTFRACTIONALONG.get(), Resources.POSTFRACTIONALONG.getId(),
	// new StringValue(String.valueOf(connection.getPostFractionAlong())));
	// postFractionAlongNode.setDomainType(ResourcesDomainType.POSTFRACTIONALONG.get());
	// connectionNodeFrom.getCustomNodes().add(postFractionAlongNode);
	// connectionNodeTo.getCustomNodes().add(postFractionAlongNode);
	// }
	//
	// // Store Synapses as CustomNodes
	// CompositeNode synapsesNode;
	// try
	// {
	// synapsesNode = populateNeuroMLModelTreeUtils.createSynapseNode((BaseConductanceBasedSynapse) neuroMLAccessUtility.getComponent(projection.getSynapse(), model,
	// Resources.SYNAPSE));
	// }
	// catch(ContentError | ModelInterpreterException e)
	// {
	// throw new ModelInterpreterException(e);
	// }
	// connectionNodeFrom.getCustomNodes().add(synapsesNode);
	// connectionNodeTo.getCustomNodes().add(synapsesNode);
	// synapsesNode.setParent(connectionNodeFrom);
	//
	// connectionNodeFrom.setType(ConnectionType.FROM);
	// connectionNodeTo.setType(ConnectionType.TO);
	//
	// // Store Path to entity connection points to and set the
	// // parent
	// connectionNodeFrom.setEntityInstancePath(entityNodeTo.getInstancePath());
	// connectionNodeFrom.setParent(entityNodeFrom);
	// connectionNodeTo.setEntityInstancePath(entityNodeFrom.getInstancePath());
	// connectionNodeTo.setParent(entityNodeTo);
	//
	// entityNodeFrom.getConnections().add(connectionNodeFrom);
	// entityNodeTo.getConnections().add(connectionNodeTo);
	// }
	// }
	// }
	// _logger.info("Extracted connections, took " + (System.currentTimeMillis() - start) + "ms");
	// }

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
	// private void addNetworkSubEntities(Network n, EntityNode parentEntity, AspectNode aspect, ModelWrapper model) throws ModelInterpreterException
	// {
	// if(n.getPopulation().size() == 1 && n.getPopulation().get(0).getSize().equals(BigInteger.ONE))
	// {
	// // there's only one cell whose name is the same as the geppetto
	// // entity, don't create any subentities
	// BaseCell cell = (BaseCell) neuroMLAccessUtility.getComponent(n.getPopulation().get(0).getComponent(), model, Resources.CELL);
	// parentEntity.setDomainType(ResourcesDomainType.CELL.get());
	// mapCellIdToEntity(parentEntity.getId(), parentEntity, aspect, cell);
	// return;
	// }
	// for(Population p : n.getPopulation())
	// {
	// // BaseCell cell = getCell(p, url, model);
	// BaseCell cell = (BaseCell) neuroMLAccessUtility.getComponent(p.getComponent(), model, Resources.CELL);
	// if(p.getType() != null && p.getType().equals(PopulationTypes.POPULATION_LIST))
	// {
	// int i = 0;
	// for(Instance instance : p.getInstance())
	// {
	//
	// String id = VariablePathSerializer.getArrayName(p.getId(), i);
	// EntityNode e = getEntityNodefromCell(cell, id, aspect);
	// e.setDomainType(ResourcesDomainType.CELL.get());
	//
	// if(instance.getLocation() != null)
	// {
	// e.setPosition(getPoint(instance.getLocation()));
	// }
	// e.setId(id);
	// parentEntity.addChild(e);
	// i++;
	// }
	//
	// }
	// else
	// {
	// int size = p.getSize().intValue();
	//
	// for(int i = 0; i < size; i++)
	// {
	// // FIXME the position of the population within the network
	// // needs to be specified in neuroml
	// String id = VariablePathSerializer.getArrayName(p.getId(), i);
	// // TODO why do we need the cell?
	// EntityNode e = getEntityNodefromCell(cell, id, aspect);
	// e.setDomainType(ResourcesDomainType.CELL.get());
	// e.setId(id);
	// parentEntity.addChild(e);
	// }
	// }
	// }
	//
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
	// private Point getPoint(Location location)
	// {
	// Point point = new Point();
	// point.setX(location.getX().doubleValue());
	// point.setY(location.getY().doubleValue());
	// point.setZ(location.getZ().doubleValue());
	// return point;
	// }

	@Override
	public void registerGeppettoService()
	{
		List<ModelFormat> modelFormats = new ArrayList<ModelFormat>(Arrays.asList(ServicesRegistry.registerModelFormat("NEUROML"), ServicesRegistry.registerModelFormat("LEMS")));
		ServicesRegistry.registerModelInterpreterService(this, modelFormats);
	}

	@Override
	public File downloadModel(Pointer pointer, ModelFormat format, IAspectConfiguration aspectConfiguration) throws ModelInterpreterException
	{
		// if(format.equals(ServicesRegistry.getModelFormat("LEMS")) || format.equals(ServicesRegistry.getModelFormat("NEUROML")))
		// {
		// try
		// {
		// // Create file and folder
		// File outputFolder = PathConfiguration.createFolderInProjectTmpFolder(getScope(), projectId,
		// PathConfiguration.getName(format.getModelFormat() + PathConfiguration.downloadModelFolderName, true));
		// String outputFile = ((URL) ((ModelWrapper) aspectNode.getModel()).getModel(NeuroMLAccessUtility.URL_ID)).getPath();
		//
		// // Serialise objects
		// String serialisedModel = "";
		// if(format.equals(ServicesRegistry.getModelFormat("LEMS")))
		// {
		// // Serialise LEMS object
		// Lems lems = (Lems) ((ModelWrapper) aspectNode.getModel()).getModel(ServicesRegistry.getModelFormat("LEMS"));
		// serialisedModel = XMLSerializer.serialize(lems);
		// }
		// else
		// {
		// // Serialise NEUROML object
		// NeuroMLDocument neuroMLDoc = (NeuroMLDocument) ((ModelWrapper) aspectNode.getModel()).getModel(ServicesRegistry.getModelFormat("NEUROML"));
		// NeuroMLConverter neuroMLConverter = new NeuroMLConverter();
		// serialisedModel = neuroMLConverter.neuroml2ToXml(neuroMLDoc);
		// // Change extension to nml
		// outputFile = outputFile.substring(0, outputFile.lastIndexOf(".") + 1) + "nml";
		// }
		//
		// // Write to disc
		// PrintWriter writer = new PrintWriter(outputFolder + outputFile.substring(outputFile.lastIndexOf(File.separator)));
		// writer.print(serialisedModel);
		// writer.close();
		// return outputFolder;
		//
		// }
		// catch(ContentError | IOException | NeuroMLException e)
		// {
		// throw new ModelInterpreterException(e);
		// }
		//
		// }
		// else
		// {
		// // Call conversion service
		// LEMSConversionService lemsConversionService = new LEMSConversionService();
		// lemsConversionService.setProjectId(projectId);
		// lemsConversionService.setScope(Scope.CONNECTION);
		// ModelWrapper outputModel = null;
		// try
		// {
		// outputModel = (ModelWrapper) lemsConversionService.convert(aspectNode.getModel(), ServicesRegistry.getModelFormat("LEMS"), format, aspectConfiguration);
		// }
		// catch(ConversionException e)
		// {
		// throw new ModelInterpreterException(e);
		// }
		// String outputFile = (String) outputModel.getModel(format);
		// return new File(outputFile.substring(0, outputFile.lastIndexOf(File.separator)));
		// }
		return null;

	}

//	@Override
//	public List<ModelFormat> getSupportedOutputs(Pointer pointer) throws ModelInterpreterException
//	{
//		List<ModelFormat> supportedOutputs = super.getSupportedOutputs(pointer);
//		supportedOutputs.add(ServicesRegistry.getModelFormat("LEMS"));
//		try
//		{
//			LEMSConversionService lemsConversionService = new LEMSConversionService();
//			supportedOutputs.addAll(lemsConversionService.getSupportedOutputs(aspectNode.getModel(), ServicesRegistry.getModelFormat("LEMS")));
//		}
//		catch(ConversionException e)
//		{
//			throw new ModelInterpreterException(e);
//		}
//		return supportedOutputs;
//	}

	public ModelWrapper getModel()
	{
		return this.model;
	}

//	public Map<ParameterSpecificationNode, Object> getMethodsMap()
//	{
//		return this.populateModelTree.getParametersNodeToMethodsMap();
//	}
//
//	public Map<ParameterSpecificationNode, Object> getObjectsMap()
//	{
//		return this.populateModelTree.getParametersNodeToObjectsMap();
//	}

}
