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
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.beans.ModelInterpreterConfig;
import org.geppetto.core.beans.PathConfiguration;
import org.geppetto.core.conversion.ConversionException;
import org.geppetto.core.data.model.IAspectConfiguration;
import org.geppetto.core.manager.Scope;
import org.geppetto.core.model.AModelInterpreter;
import org.geppetto.core.model.GeppettoModelAccess;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.services.registry.ServicesRegistry;
import org.geppetto.model.DomainModel;
import org.geppetto.model.ExternalDomainModel;
import org.geppetto.model.GeppettoLibrary;
import org.geppetto.model.ModelFormat;
import org.geppetto.model.neuroml.features.LEMSParametersFeature;
import org.geppetto.model.neuroml.modelinterpreter.utils.ExtractVisualType;
import org.geppetto.model.neuroml.modelinterpreter.utils.ModelInterpreterUtils;
import org.geppetto.model.neuroml.modelinterpreter.utils.PopulateSummaryNodesModelTreeUtils;
import org.geppetto.model.neuroml.utils.OptimizedLEMSReader;
import org.geppetto.model.neuroml.utils.Resources;
import org.geppetto.model.neuroml.utils.ResourcesDomainType;
import org.geppetto.model.types.ArrayType;
import org.geppetto.model.types.CompositeType;
import org.geppetto.model.types.CompositeVisualType;
import org.geppetto.model.types.ConnectionType;
import org.geppetto.model.types.Type;
import org.geppetto.model.types.TypesFactory;
import org.geppetto.model.types.TypesPackage;
import org.geppetto.model.types.VisualType;
import org.geppetto.model.util.GeppettoVisitingException;
import org.geppetto.model.util.PointerUtility;
import org.geppetto.model.values.ArrayElement;
import org.geppetto.model.values.ArrayValue;
import org.geppetto.model.values.Connection;
import org.geppetto.model.values.Connectivity;
import org.geppetto.model.values.Point;
import org.geppetto.model.values.Pointer;
import org.geppetto.model.values.Sphere;
import org.geppetto.model.values.Text;
import org.geppetto.model.values.ValuesFactory;
import org.geppetto.model.variables.Variable;
import org.geppetto.model.variables.VariablesFactory;
import org.lemsml.jlems.api.interfaces.ILEMSDocument;
import org.lemsml.jlems.core.sim.ContentError;
import org.lemsml.jlems.core.sim.LEMSException;
import org.lemsml.jlems.core.type.Attribute;
import org.lemsml.jlems.core.type.Component;
import org.lemsml.jlems.core.type.Exposure;
import org.lemsml.jlems.core.type.Lems;
import org.lemsml.jlems.core.type.ParamValue;
import org.lemsml.jlems.io.xmlio.XMLSerializer;
import org.neuroml.export.utils.Utils;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.Standalone;
import org.neuroml.model.util.NeuroML2Validator;
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

	private Map<String, Type> types = new HashMap<String, Type>();
	private Map<ResourcesDomainType, List<Type>> typesMap = new HashMap<ResourcesDomainType, List<Type>>();
	private Type type = null;

	private GeppettoModelAccess access;

	private TypesFactory typeFactory = TypesFactory.eINSTANCE;
	private ValuesFactory valuesFactory = ValuesFactory.eINSTANCE;
	private VariablesFactory variablesFactory = VariablesFactory.eINSTANCE;

	private Map<Component, List<Variable>> cellSegmentMap = new HashMap<Component, List<Variable>>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geppetto.core.model.IModelInterpreter#importType(java.net.URL, java.lang.String, org.geppetto.core.library.LibraryManager)
	 */
	@Override
	public Type importType(URL url, String typeId, GeppettoLibrary library, GeppettoModelAccess access) throws ModelInterpreterException
	{
		long startTime = System.currentTimeMillis();
		dependentModels.clear();
		OptimizedLEMSReader reader = null;
		try
		{
			// Read main and includes as a String
			reader = new OptimizedLEMSReader(this.dependentModels);
			reader.readAllFormats(url, OptimizedLEMSReader.NMLDOCTYPE.NEUROML);

			// Extract Types from the lems/neuroml files
			extractTypes(url, typeId, library, access, reader.getLEMSDocument(), reader.getNeuroMLDocument());
		}
		catch(IOException | NumberFormatException | GeppettoVisitingException e)
		{
			throw new ModelInterpreterException(e);
		}
		catch(NeuroMLException | LEMSException e)
		{
			NeuroML2Validator neuroML2Validator = new NeuroML2Validator();
			neuroML2Validator.validateWithTests(reader.getNeuroMLDocument());
			// AQP: Change to isValid once we update model.neuroml
			if(neuroML2Validator.hasWarnings()
					|| !(neuroML2Validator.getValidity().equals(NeuroML2Validator.VALID_AGAINST_SCHEMA) || 
							neuroML2Validator.getValidity().equals(NeuroML2Validator.VALID_AGAINST_SCHEMA_AND_TESTS) ||
							neuroML2Validator.getValidity().equals(NeuroML2Validator.VALID_AGAINST_TESTS))){
				throw new ModelInterpreterException("Validity: " + neuroML2Validator.getValidity() + " Warnings: " + neuroML2Validator.getWarnings());
			}
			else{
				throw new ModelInterpreterException(e);
			}
		}

		long endTime = System.currentTimeMillis();
		_logger.info("Import Type took " + (endTime - startTime) + " milliseconds for url " + url + " and typename " + typeId);
		return type;
	}

	public Type extractTypes(URL url, String typeId, GeppettoLibrary library, GeppettoModelAccess access, ILEMSDocument lemsDocument, NeuroMLDocument neuroml) throws NeuroMLException, LEMSException,
			GeppettoVisitingException, ContentError, ModelInterpreterException
	{
		this.access = access;

		// Resolve LEMS model
		// If there is any problem resolving the lems model, we will try to go ahead anyway
		// as there are some models, as purkinje, which are not valid LEMS format
		Lems lems = ((Lems) lemsDocument);
		try
		{
			ModelInterpreterUtils.processLems(lems);
		}
		catch(NumberFormatException | LEMSException e)
		{
			_logger.warn("Error resolving lems file");
			throw new ModelInterpreterException(e);
		}

		// If we have a typeId let's get the type for this component
		// Otherwise let's iterate through all the components
		if(typeId != null && !typeId.isEmpty())
		{
			types.put(typeId, extractInfoFromComponent(lems.getComponent(typeId), null));
			type = types.get(typeId);
		}
		else
		{
			// If no type id then just iterate over the components
			// While iterating get the main type
			boolean multipleTypes = false;
			for(Component component : lems.getComponents())
			{
				if(!types.containsKey(component.getID()))
				{

					types.put(component.getID(), extractInfoFromComponent(component, null));

					// Business rule: 1) If there is a network in the NeuroML file we don't visualise spurious cells which
					// "most likely" are just included types in NeuroML and are instantiated as part of the network
					// populations
					// If there is not a network we visualise the cell (as far as there is just a single cell)
					// If there is just a single component we return the single cell
					// Otherwise we throw an exception
					Type currentType = types.get(component.getID());
					if(type == null)
					{
						type = currentType;
					}
					else
					{
						String declaredType = ((Component) type.getDomainModel().getDomainModel()).getDeclaredType();
						String currentDeclaredType = ((Component) currentType.getDomainModel().getDomainModel()).getDeclaredType();
						if(!declaredType.equals(Resources.NETWORK.getId())
								&& (currentDeclaredType.equals(Resources.NETWORK.getId()) || (!declaredType.equals(Resources.CELL.getId()) && currentDeclaredType.equals(Resources.CELL.getId()))))
						{
							multipleTypes = false;
							type = currentType;
						}
						else if((!declaredType.equals(Resources.CELL.getId()) && !declaredType.equals(Resources.NETWORK.getId()))
								|| (declaredType.equals(Resources.CELL.getId()) && currentDeclaredType.equals(Resources.CELL.getId()))
								|| (declaredType.equals(Resources.NETWORK.getId()) && currentDeclaredType.equals(Resources.NETWORK.getId())))
						{
							multipleTypes = true;
						}
					}
				}
			}

			if(multipleTypes) throw new ModelInterpreterException("Multiple types found and no type id specified");
		}

		// AQP: Shall we verify if types != null?
		// Add all the types to the library
		library.getTypes().addAll(types.values());

		// Extract Summary and Description nodes from type
		PopulateSummaryNodesModelTreeUtils populateSummaryNodesModelTreeUtils = new PopulateSummaryNodesModelTreeUtils(neuroml, typesMap, url, access);
		((CompositeType) type).getVariables().addAll(populateSummaryNodesModelTreeUtils.getSummaryVariables());

		// Add LEMS Parameter Feature
		this.addFeature(new LEMSParametersFeature());

		return type;
	}

	/*
	 * Return a regular composite type if domainType is null. Otherwise return a composite type with supertype equal to the domaintype or an array type
	 */
	private Type getCompositeType(String domainName)
	{
		// return a regular compositeType if no domain
		if(domainName == null)
		{
			return typeFactory.createCompositeType();
		}

		// Create super type
		if(!types.containsKey(domainName))
		{
			Type domainType;
			typesMap.put(ResourcesDomainType.getValueByValue(domainName), new ArrayList<Type>());

			domainType = typeFactory.createCompositeType();
			domainType.setId(domainName);
			domainType.setName(domainName);
			types.put(domainName, domainType);
		}

		// Create array/composite type and set super type
		Type newType;
		if(domainName.equals(ResourcesDomainType.POPULATION.get()))
		{
			newType = typeFactory.createArrayType();
		}
		else if(domainName.equals(ResourcesDomainType.CONNECTION.get()))
		{
			newType = typeFactory.createConnectionType();
		}
		else
		{
			newType = typeFactory.createCompositeType();
		}
		newType.setSuperType(types.get(domainName));

		// Add new type to typesMap. It will be used later on to generate description node
		List<Type> typeList = typesMap.get(ResourcesDomainType.getValueByValue(domainName));
		typeList.add(newType);
		return newType;
	}

	/*
	 * Generic method to extract info from any component
	 */
	private CompositeType extractInfoFromComponent(Component component, String domainType) throws NumberFormatException, NeuroMLException, LEMSException, GeppettoVisitingException,
			ModelInterpreterException
	{
		if(component.getID() != null && component.getID().equals("BackgroundRandomIClamps"))
		{
			System.out.println("tak");
		}
		// Create composite type depending on type of component and initialise it
		CompositeType compositeType = (CompositeType) getCompositeType((domainType != null) ? domainType : null);
		ModelInterpreterUtils.initialiseNodeFromComponent(compositeType, component);

		// Parameter types
		for(ParamValue pv : component.getParamValues())
		{
			if(component.hasAttribute(pv.getName()))
			{
				compositeType.getVariables().add(ModelInterpreterUtils.createParameterTypeVariable(pv.getName(), component.getStringValue(pv.getName()), this.access));
			}
		}

		// String preCellId = ModelInterpreterUtils.parseCellRefStringForCellNum(attribute.getValue());
		// connection.getA().add(PointerUtility.getPointer(prePopulationVariable, prePopulationType, Integer.parseInt(preCellId)));
		//
		// for(Attribute entry : component.getAttributes()){
		// entry.getClass()
		// }

		// Text types
		for(Entry<String, String> entry : component.getTextParamMap().entrySet())
		{
			compositeType.getVariables().add(ModelInterpreterUtils.createTextTypeVariable(entry.getKey(), entry.getValue(), this.access));
		}

		// Composite Type
		for(Entry<String, Component> entry : component.getRefComponents().entrySet())
		{
			if(!types.containsKey(entry.getValue().getID()))
			{
				types.put(entry.getValue().getID(), extractInfoFromComponent(entry.getValue(), null));
			}
			Variable variable = variablesFactory.createVariable();
			ModelInterpreterUtils.initialiseNodeFromComponent(variable, entry.getValue());
			variable.getTypes().add(types.get(entry.getValue().getID()));
			compositeType.getVariables().add(variable);
		}

		// Extracting populations (this needs to be executed before extracting the projection otherwise we can't get the population)
		for(Component population : component.getChildrenAL("populations"))
		{
			if(!types.containsKey(population.getID()))
			{
				createPopulationTypeVariable(population);
			}
			Variable variable = variablesFactory.createVariable();
			ModelInterpreterUtils.initialiseNodeFromComponent(variable, population);
			variable.getTypes().add(types.get(population.getID()));
			compositeType.getVariables().add(variable);
		}

		// Extracting projection and connections
		for(Component projection : component.getChildrenAL("projections"))
		{
			createConnectionTypeVariablesFromProjection(projection, compositeType);
		}

		// Extracting the rest of the child
		for(Component componentChild : component.getChildHM().values())
		{
			if(componentChild.getDeclaredType().equals(Resources.MORPHOLOGY.getId()))
			{
				createVisualTypeFromMorphology(component, compositeType, componentChild);
			}
			else if(componentChild.getDeclaredType().equals(Resources.ANNOTATION.getId()))
			{
				createCompositeTypeFromAnnotation(compositeType, componentChild);
			}
			else if(componentChild.getDeclaredType().equals(Resources.NOTES.getId()))
			{
				compositeType.getVariables().add(ModelInterpreterUtils.createTextTypeVariable(Resources.NOTES.get(), componentChild.getAbout(), this.access));
			}
			else
			{
				// For the moment all the child are extracted as anonymous types
				CompositeType anonymousCompositeType = extractInfoFromComponent(componentChild, null);
				if(anonymousCompositeType != null)
				{
					Variable variable = variablesFactory.createVariable();
					ModelInterpreterUtils.initialiseNodeFromComponent(variable, componentChild);
					variable.getAnonymousTypes().add(anonymousCompositeType);
					compositeType.getVariables().add(variable);
				}
			}
		}

		// Extracting the rest of the children
		for(Component componentChild : component.getStrictChildren())
		{
			if(!componentChild.getDeclaredType().equals(Resources.POPULATION.getId()) && !componentChild.getDeclaredType().equals(Resources.PROJECTION.getId()))
			{
				// If it is not a population, a projection/connection or a morphology, let's deal with it in a generic way
				CompositeType anonymousCompositeType = extractInfoFromComponent(componentChild, null);
				if(anonymousCompositeType != null)
				{
					// For the moment all the children are extracted as anonymous types
					Variable variable = variablesFactory.createVariable();
					ModelInterpreterUtils.initialiseNodeFromComponent(variable, componentChild);
					variable.getAnonymousTypes().add(anonymousCompositeType);
					compositeType.getVariables().add(variable);
				}
			}
		}

		// Exposures are the variables that can potentially be watched
		for(Exposure exposure : component.getComponentType().getExposures())
		{
			if(cellSegmentMap.containsKey(component) && cellSegmentMap.get(component).size() > 1 && (exposure.getName().equals("v") || exposure.getName().equals("spiking")))
			{
				if(!types.containsKey("compartment") || ((CompositeType) types.get("compartment")).getVariables().size() == 1)
				{

					if(!types.containsKey("compartment"))
					{
						CompositeType compartmentCompositeType = (CompositeType) getCompositeType(null);
						ModelInterpreterUtils.initialiseNodeFromString(compartmentCompositeType, "compartment");
						types.put("compartment", compartmentCompositeType);
					}

					if(exposure.getName().equals("v") || exposure.getName().equals("spiking"))
					{
						CompositeType compartmentCompositeType = (CompositeType) types.get("compartment");
						compartmentCompositeType.getVariables().add(
								ModelInterpreterUtils.createExposureTypeVariable(exposure.getName(), Utils.getSIUnitInNeuroML(exposure.getDimension()).getSymbol(), this.access));

					}
				}
			}
			else
			{
				compositeType.getVariables().add(ModelInterpreterUtils.createExposureTypeVariable(exposure.getName(), Utils.getSIUnitInNeuroML(exposure.getDimension()).getSymbol(), this.access));
			}
		}

		if(cellSegmentMap.containsKey(component) && cellSegmentMap.get(component).size() > 1)
		{
			for(Variable compartment : cellSegmentMap.get(component))
			{
				Variable variable = variablesFactory.createVariable();
				ModelInterpreterUtils.initialiseNodeFromString(variable, compartment.getName());
				variable.getTypes().add(types.get("compartment"));
				compositeType.getVariables().add(variable);
			}
		}

		return compositeType;

	}

	private void createCompositeTypeFromAnnotation(CompositeType compositeType, Component annotation) throws LEMSException, NeuroMLException, GeppettoVisitingException
	{
		CompositeType annotationType = typeFactory.createCompositeType();
		ModelInterpreterUtils.initialiseNodeFromComponent(annotationType, annotation);
		for(Map.Entry<String, Component> entry : annotation.getChildHM().entrySet())
		{
			if(entry.getKey().equals("property"))
			{
				Component property = entry.getValue();
				Text text = valuesFactory.createText();
				text.setText(property.getTextParam("value"));

				Variable variable = variablesFactory.createVariable();
				ModelInterpreterUtils.initialiseNodeFromString(variable, property.getTextParam("tag"));
				variable.getTypes().add(access.getType(TypesPackage.Literals.TEXT_TYPE));
				variable.getInitialValues().put(access.getType(TypesPackage.Literals.TEXT_TYPE), text);
				annotationType.getVariables().add(variable);
			}
			else
			{
				Component rdf = entry.getValue();
				if(rdf.hasTextParam("xmlns:rdf"))
				{
					Text text = valuesFactory.createText();
					text.setText(rdf.getTextParam("xmlns:rdf"));

					Variable variable = variablesFactory.createVariable();
					ModelInterpreterUtils.initialiseNodeFromString(variable, "rdf");
					variable.getTypes().add(access.getType(TypesPackage.Literals.TEXT_TYPE));
					variable.getInitialValues().put(access.getType(TypesPackage.Literals.TEXT_TYPE), text);
					annotationType.getVariables().add(variable);
				}

				Component rdfDescription = rdf.getChild("rdf:Description");
				for(Map.Entry<String, Component> rdfDescriptionChild : rdfDescription.getChildHM().entrySet())
				{
					// AQP: we can't access the information in here. Schema needs to change so that rdf:li are children instead of child
					rdfDescriptionChild.getValue().getChild("rdf:Bag").getChild("rdf:li").getAbout();
					rdfDescriptionChild.getValue().getChild("rdf:Bag").getChild("rdf:li").getTextParam("rdf:resource");

					Text text = valuesFactory.createText();
					// AQP: we need to conver from this to a readable label
					text.setText(rdfDescriptionChild.getKey());

					Variable variable = variablesFactory.createVariable();
					ModelInterpreterUtils.initialiseNodeFromString(variable, rdfDescriptionChild.getKey());
					variable.getTypes().add(access.getType(TypesPackage.Literals.TEXT_TYPE));
					variable.getInitialValues().put(access.getType(TypesPackage.Literals.TEXT_TYPE), text);
					annotationType.getVariables().add(variable);
				}
			}
		}

		Variable variable = variablesFactory.createVariable();
		ModelInterpreterUtils.initialiseNodeFromComponent(variable, annotation);
		variable.getAnonymousTypes().add(annotationType);
		compositeType.getVariables().add(variable);
	}

	private void createVisualTypeFromMorphology(Component component, CompositeType compositeType, Component morphology) throws GeppettoVisitingException, LEMSException, NeuroMLException,
			ModelInterpreterException
	{
		if(!types.containsKey(morphology.getID()))
		{
			ExtractVisualType extractVisualType = new ExtractVisualType(component, access);
			types.put(morphology.getID(), extractVisualType.createTypeFromCellMorphology());

			cellSegmentMap.put(component, extractVisualType.getVisualObjectsSegments());
		}

		compositeType.setVisualType((VisualType) types.get(morphology.getID()));
	}

	public void createConnectionTypeVariablesFromProjection(Component projection, CompositeType compositeType) throws GeppettoVisitingException, LEMSException, NeuroMLException,
			NumberFormatException, ModelInterpreterException
	{
		// get/create the projection type and variable
		CompositeType projectionType = null;
		if(!types.containsKey(projection.getID()))
		{
			projectionType = (CompositeType) getCompositeType(ResourcesDomainType.PROJECTION.get());
			ModelInterpreterUtils.initialiseNodeFromComponent(projectionType, projection);
			types.put(projection.getID(), projectionType);
		}
		else
		{
			projectionType = (CompositeType) types.get(projection.getID());
		}
		Variable projectionVariable = variablesFactory.createVariable();
		ModelInterpreterUtils.initialiseNodeFromComponent(projectionVariable, projection);
		projectionVariable.getTypes().add(types.get(projection.getID()));
		compositeType.getVariables().add(projectionVariable);

		// Create synapse type
		Component synapse = projection.getRefComponents().get(Resources.SYNAPSE.getId());
		if(!types.containsKey(synapse.getID()))
		{
			types.put(synapse.getID(), extractInfoFromComponent(projection.getRefComponents().get(Resources.SYNAPSE.getId()), ResourcesDomainType.SYNAPSE.get()));
		}
		Variable synapsesVariable = variablesFactory.createVariable();
		ModelInterpreterUtils.initialiseNodeFromComponent(synapsesVariable, synapse);
		synapsesVariable.getTypes().add(types.get(synapse.getID()));
		projectionType.getVariables().add(synapsesVariable);

		// Create pre synaptic population
		ArrayType prePopulationType = (ArrayType) types.get(projection.getAttributeValue(Resources.PRE_SYNAPTIC_POPULATION.getId()));
		Variable prePopulationVariable = null;
		for(Variable variable : compositeType.getVariables())
		{
			if(variable.getId().equals(prePopulationType.getId()))
			{
				prePopulationVariable = variable;
				break;
			}
		}
		Variable preSynapticPopulationVariable = variablesFactory.createVariable();
		ModelInterpreterUtils.initialiseNodeFromString(preSynapticPopulationVariable, Resources.PRE_SYNAPTIC_POPULATION.getId());
		preSynapticPopulationVariable.getTypes().add(access.getType(TypesPackage.Literals.POINTER_TYPE));
		preSynapticPopulationVariable.getInitialValues().put(access.getType(TypesPackage.Literals.POINTER_TYPE), PointerUtility.getPointer(prePopulationVariable, prePopulationType, null));
		projectionType.getVariables().add(preSynapticPopulationVariable);

		// Create post synaptic population
		ArrayType postPopulationType = (ArrayType) types.get(projection.getAttributeValue(Resources.POST_SYNAPTIC_POPULATION.getId()));
		Variable postPopulationVariable = null;
		for(Variable variable : compositeType.getVariables())
		{
			if(variable.getId().equals(postPopulationType.getId()))
			{
				postPopulationVariable = variable;
				break;
			}
		}
		Variable postSynapticPopulationVariable = variablesFactory.createVariable();
		ModelInterpreterUtils.initialiseNodeFromString(postSynapticPopulationVariable, Resources.POST_SYNAPTIC_POPULATION.getId());
		postSynapticPopulationVariable.getTypes().add(access.getType(TypesPackage.Literals.POINTER_TYPE));
		postSynapticPopulationVariable.getInitialValues().put(access.getType(TypesPackage.Literals.POINTER_TYPE), PointerUtility.getPointer(postPopulationVariable, postPopulationType, null));
		projectionType.getVariables().add(postSynapticPopulationVariable);

		for(Component projectionChild : projection.getChildHM().values())
		{
			CompositeType anonymousCompositeType = extractInfoFromComponent(projectionChild, null);
			if(anonymousCompositeType != null)
			{
				Variable variable = variablesFactory.createVariable();
				ModelInterpreterUtils.initialiseNodeFromComponent(variable, projectionChild);
				variable.getAnonymousTypes().add(anonymousCompositeType);
				projectionType.getVariables().add(variable);
			}
		}

		// Iterate over all the children. Most of them are connections
		for(Component projectionChild : projection.getStrictChildren())
		{
			if(projectionChild.getDeclaredType().equals(Resources.CONNECTION.getId()))
			{
				ConnectionType connectionType = (ConnectionType) getCompositeType(ResourcesDomainType.CONNECTION.get());
				ModelInterpreterUtils.initialiseNodeFromComponent(connectionType, projectionChild);

				Connection connection = valuesFactory.createConnection();
				connection.setConnectivity(Connectivity.DIRECTIONAL);

				for(Attribute attribute : projectionChild.getAttributes())
				{
					if(attribute.getName().equals("preCellId"))
					{
						String preCellId = ModelInterpreterUtils.parseCellRefStringForCellNum(attribute.getValue());
						connection.getA().add(PointerUtility.getPointer(prePopulationVariable, prePopulationType, Integer.parseInt(preCellId)));
					}
					else if(attribute.getName().equals("postCellId"))
					{
						String postCellId = ModelInterpreterUtils.parseCellRefStringForCellNum(attribute.getValue());
						connection.getB().add(PointerUtility.getPointer(postPopulationVariable, postPopulationType, Integer.parseInt(postCellId)));
					}
					else
					{
						// preSegmentId, preFractionAlong, postSegmentId, postFractionAlong
						connectionType.getVariables().add(ModelInterpreterUtils.createTextTypeVariable(attribute.getName(), attribute.getValue(), access));
					}
				}

				Variable variable = variablesFactory.createVariable();
				ModelInterpreterUtils.initialiseNodeFromComponent(variable, projectionChild);
				variable.getAnonymousTypes().add(connectionType);
				variable.getInitialValues().put(connectionType, connection);
				projectionType.getVariables().add(variable);

			}
			else
			{
				CompositeType anonymousCompositeType = extractInfoFromComponent(projectionChild, null);
				if(anonymousCompositeType != null)
				{
					Variable variable = variablesFactory.createVariable();
					ModelInterpreterUtils.initialiseNodeFromComponent(variable, projectionChild);
					variable.getAnonymousTypes().add(anonymousCompositeType);
					projectionType.getVariables().add(variable);
				}
			}
		}

	}

	public void createPopulationTypeVariable(Component populationComponent) throws GeppettoVisitingException, LEMSException, NeuroMLException, NumberFormatException, ModelInterpreterException
	{
		if(!types.containsKey(populationComponent.getRefComponents().get("component").getID()))
		{
			types.put(populationComponent.getRefComponents().get("component").getID(),
					extractInfoFromComponent(populationComponent.getRefComponents().get("component"), ResourcesDomainType.CELL.get()));
		}
		CompositeType refCompositeType = (CompositeType) types.get(populationComponent.getRefComponents().get("component").getID());

		ArrayType arrayType = (ArrayType) getCompositeType(ResourcesDomainType.POPULATION.get());
		ModelInterpreterUtils.initialiseNodeFromComponent(arrayType, populationComponent);

		// If it is not of type cell, it won't have morphology and we can assume an sphere in the
		if(!populationComponent.getRefComponents().get("component").getDeclaredType().equals(Resources.CELL.getId()))
		{
			if(!types.containsKey("morphology_sphere"))
			{
				CompositeVisualType visualCompositeType = typeFactory.createCompositeVisualType();
				ModelInterpreterUtils.initialiseNodeFromString(visualCompositeType, "morphology_sphere");

				Sphere sphere = valuesFactory.createSphere();
				sphere.setRadius(1.2d);
				Point point = valuesFactory.createPoint();
				point.setX(0);
				point.setY(0);
				point.setZ(0);
				sphere.setPosition(point);

				Variable variable = variablesFactory.createVariable();
				ModelInterpreterUtils.initialiseNodeFromString(variable, populationComponent.getRefComponents().get("component").getID());
				variable.getTypes().add(access.getType(TypesPackage.Literals.VISUAL_TYPE));
				variable.getInitialValues().put(access.getType(TypesPackage.Literals.VISUAL_TYPE), sphere);

				visualCompositeType.getVariables().add(variable);

				types.put("morphology_sphere", visualCompositeType);
			}

			refCompositeType.setVisualType((VisualType) types.get("morphology_sphere"));

		}
		arrayType.setArrayType(refCompositeType);

		ArrayValue arrayValue = valuesFactory.createArrayValue();

		String populationType = populationComponent.getTypeName();
		// If it is not of type populationList we don't have to do anything in particular
		if(populationType != null && populationType.equals("populationList"))
		{

			int size = 0;
			for(Component populationChild : populationComponent.getAllChildren())
			{
				if(populationChild.getDeclaredType().equals("instance"))
				{
					Point point = null;
					for(Component instanceChild : populationChild.getAllChildren())
					{
						if(instanceChild.getDeclaredType().equals("location"))
						{
							point = valuesFactory.createPoint();
							point.setX(Double.parseDouble(instanceChild.getStringValue("x")));
							point.setY(Double.parseDouble(instanceChild.getStringValue("y")));
							point.setZ(Double.parseDouble(instanceChild.getStringValue("z")));
						}
					}

					ArrayElement arrayElement = valuesFactory.createArrayElement();
					arrayElement.setIndex(Integer.parseInt(populationChild.getID()));
					arrayElement.setPosition(point);
					arrayValue.getElements().add(arrayElement);

					size++;
				}
			}
			arrayType.setSize(size);
		}
		else
		{
			// If it has size attribute we read it otherwise we count the number of instances
			if(populationComponent.hasStringValue(Resources.SIZE.getId())) arrayType.setSize(Integer.parseInt(populationComponent.getStringValue(Resources.SIZE.getId())));
		}
		arrayType.setDefaultValue(arrayValue);
		types.put(populationComponent.getID(), arrayType);
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

	@Override
	public void registerGeppettoService()
	{
		List<ModelFormat> modelFormats = new ArrayList<ModelFormat>(Arrays.asList(ServicesRegistry.registerModelFormat("NEUROML"), ServicesRegistry.registerModelFormat("LEMS")));
		ServicesRegistry.registerModelInterpreterService(this, modelFormats);
	}

	@Override
	public File downloadModel(Pointer pointer, ModelFormat format, IAspectConfiguration aspectConfiguration) throws ModelInterpreterException
	{
		// Get domain model
		DomainModel domainModel = PointerUtility.getType(pointer).getDomainModel();
		if(format.equals(ServicesRegistry.getModelFormat("LEMS")) || format.equals(ServicesRegistry.getModelFormat("NEUROML")))
		{
			try
			{
				// Create file and folder
				File outputFolder = PathConfiguration.createFolderInProjectTmpFolder(getScope(), projectId,
						PathConfiguration.getName(format.getModelFormat() + PathConfiguration.downloadModelFolderName, true));

				String outputFile = PointerUtility.getType(pointer).getId();

				// Serialise objects
				String serialisedModel = "";
				if(format.equals(ServicesRegistry.getModelFormat("LEMS")))
				{
					// Serialise LEMS object
					serialisedModel = XMLSerializer.serialize((Component) domainModel.getDomainModel());
					outputFile += "xml";
				}
				else
				{
					// Convert to NeuroML
					LinkedHashMap<String, Standalone> neuroMLComponent = Utils.convertLemsComponentToNeuroML((Component) domainModel.getDomainModel());
					NeuroMLDocument neuroMLDoc = new NeuroMLDocument();
					for(Standalone standalone : neuroMLComponent.values())
					{
						NeuroMLConverter.addElementToDocument(neuroMLDoc, standalone);
					}

					// Serialise NEUROML object
					NeuroMLConverter neuroMLConverter = new NeuroMLConverter();
					serialisedModel = neuroMLConverter.neuroml2ToXml(neuroMLDoc);
					// Change extension to nml
					outputFile += "nml";
				}

				// Write to disc
				PrintWriter writer = new PrintWriter(outputFolder + outputFile);
				writer.print(serialisedModel);
				writer.close();
				return outputFolder;

			}
			catch(IOException | LEMSException | NeuroMLException e)
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
			ExternalDomainModel outputDomainModel = null;
			try
			{
				outputDomainModel = (ExternalDomainModel) lemsConversionService.convert(domainModel, format, aspectConfiguration, this.access);
			}
			catch(ConversionException e)
			{
				throw new ModelInterpreterException(e);
			}
			return (File) outputDomainModel.getDomainModel();
		}
	}

	@Override
	public List<ModelFormat> getSupportedOutputs(Pointer pointer) throws ModelInterpreterException
	{
		List<ModelFormat> supportedOutputs = super.getSupportedOutputs(pointer);
		supportedOutputs.add(ServicesRegistry.getModelFormat("LEMS"));
		try
		{
			LEMSConversionService lemsConversionService = new LEMSConversionService();
			supportedOutputs.addAll(lemsConversionService.getSupportedOutputs(PointerUtility.getType(pointer).getDomainModel()));
		}
		catch(ConversionException e)
		{
			throw new ModelInterpreterException(e);
		}
		return supportedOutputs;
	}

}
