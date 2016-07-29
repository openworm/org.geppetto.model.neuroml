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
import java.util.List;
import java.util.Map;

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
import org.geppetto.model.neuroml.modelInterpreterUtils.PopulateTypes;
import org.geppetto.model.neuroml.utils.OptimizedLEMSReader;
import org.geppetto.model.neuroml.utils.Resources;
import org.geppetto.model.types.CompositeType;
import org.geppetto.model.types.Type;
import org.geppetto.model.util.GeppettoVisitingException;
import org.geppetto.model.util.PointerUtility;
import org.geppetto.model.values.Pointer;
import org.gepppetto.model.neuroml.summaryUtils.PopulateSummaryNodesUtils;
import org.lemsml.jlems.api.interfaces.ILEMSDocument;
import org.lemsml.jlems.core.sim.ContentError;
import org.lemsml.jlems.core.sim.LEMSException;
import org.lemsml.jlems.core.type.Component;
import org.lemsml.jlems.core.type.ComponentType;
import org.lemsml.jlems.core.type.Lems;
import org.lemsml.jlems.io.xmlio.XMLSerializer;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.util.NeuroML2Validator;
import org.neuroml.model.util.NeuroMLElements;
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
	private PopulateTypes populateTypes = null;

	private GeppettoModelAccess access;
	private OptimizedLEMSReader reader = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geppetto.core.model.IModelInterpreter#importType(java.net.URL, java.lang.String, org.geppetto.core.library.LibraryManager)
	 */
	@Override
	public Type importType(URL url, String typeId, GeppettoLibrary library, GeppettoModelAccess access) throws ModelInterpreterException
	{
		long startTime = System.currentTimeMillis();

		if(this.reader == null)
		{
			Type type = null;
			// Read the neuroml/lems model and includes
			// if there is a neuroml/lems exception -> call NeuroML Validator in order to get a good explanation for the user
			try
			{
				// Read main and includes as a String
				dependentModels.clear();
				reader = new OptimizedLEMSReader(this.dependentModels);
				reader.readAllFormats(url);

				// Extract Types from the lems/neuroml files
				type = extractTypes(url, typeId, library, access, reader.getLEMSDocument(), reader.getNeuroMLDocument());
			}
			catch(IOException | NumberFormatException | GeppettoVisitingException e)
			{
				throw new ModelInterpreterException(e);
			}
			catch(NeuroMLException | LEMSException e)
			{

				// Call NeuroMl2Validator to check if it is a valid lems/neuroml model
				try
				{
					NeuroML2Validator neuroML2Validator = new NeuroML2Validator();
					neuroML2Validator.validateWithTests(reader.getNeuroMLDocument());
					if(neuroML2Validator.hasWarnings() || !neuroML2Validator.isValid())
					{
						throw new ModelInterpreterException("Validity: " + neuroML2Validator.getValidity() + " Warnings: " + neuroML2Validator.getWarnings());
					}
					throw new ModelInterpreterException(e);
				}
				catch(NeuroMLException e1)
				{
					throw new ModelInterpreterException(e1);
				}
			}

			// Add LEMS Parameter Feature
			this.addFeature(new LEMSParametersFeature());
			this.access = access;

			long endTime = System.currentTimeMillis();
			_logger.info("Import Type took " + (endTime - startTime) + " milliseconds for url " + url + " and typename " + typeId);

			return type;
		}

		else
		{
			long start = System.currentTimeMillis();
			Type resolvedType = populateTypes.resolveType(typeId, library);
			_logger.info("Import Type took " + (System.currentTimeMillis() - start) + " milliseconds for type " + typeId);
			addTypesToLibrary(library);
			return resolvedType;
		}

	}

	public Type extractTypes(URL url, String typeId, GeppettoLibrary library, GeppettoModelAccess access, ILEMSDocument lemsDocument, NeuroMLDocument neuroMLDocument) throws NeuroMLException,
			LEMSException, GeppettoVisitingException, ContentError, ModelInterpreterException
	{
		try
		{
			long start = System.currentTimeMillis();

			// Init variables
			types = new HashMap<String, Type>();
			Type type = null;

			// Resolve LEMS model
			Lems lems = ((Lems) lemsDocument);
			lems.resolve();

			_logger.info("Resolved LEMS model, took " + (System.currentTimeMillis() - start) + "ms");

			start = System.currentTimeMillis();

			populateTypes = new PopulateTypes(types, access, neuroMLDocument);
			// If we have a typeId let's get the type for this component
			// Otherwise let's iterate through all the components
			if(typeId != null && !typeId.isEmpty())
			{
				Component mainComponent = lems.getComponent(typeId);
				type = populateTypes.extractInfoFromComponent(mainComponent, null);
			}
			else
			{
				// If no type id then just iterate over the components
				// While iterating get the main type
				for(Component component : lems.getComponents())
				{
					if(!types.containsKey(component.getDeclaredType() + component.getID()) && component.getID() != null)
					{
						types.put(component.getDeclaredType() + component.getID(), populateTypes.extractInfoFromComponent(component, null));
					}
				}

				// Get a single type and remove it from types
				type = getUniqueType();
				String main = null;
				for(String s : types.keySet())
				{
					if(types.get(s).equals(type))
					{
						main = s;
					}
				}
				types.remove(main);
				
			}

			_logger.info("Extracted info from component, took " + (System.currentTimeMillis() - start) + "ms");

			start = System.currentTimeMillis();

			addTypesToLibrary(library);

			// Extract Summary and Description nodes from type
			PopulateSummaryNodesUtils populateSummaryNodesModelTreeUtils = new PopulateSummaryNodesUtils(populateTypes.getTypesMap(), type, url, access, neuroMLDocument);

			((CompositeType) type).getVariables().add(populateSummaryNodesModelTreeUtils.getDescriptionNode());
			populateSummaryNodesModelTreeUtils.createHTMLVariables();
			_logger.info("Extracted summaries, took " + (System.currentTimeMillis() - start) + "ms");

			return type;
		}
		catch(NumberFormatException | LEMSException e)
		{
			_logger.warn("Error resolving lems file");
			throw new ModelInterpreterException(e);
		}
	}

	private void addTypesToLibrary(GeppettoLibrary library)
	{
		// Add all the types to the library bar the main one (which will be swapped by Geppetto
		library.getTypes().addAll(types.values());
	}

	// Business rule: 1) If there is a network in the NeuroML file we don't visualise spurious cells which
	// "most likely" are just included types in NeuroML and are instantiated as part of the network
	// populations
	// If there is not a network we visualise the cell (as far as there is just a single cell)
	// If there is just a single component we return the single cell
	// Otherwise we throw an exception
	private Type getUniqueType() throws ModelInterpreterException
	{
		boolean multipleTypes = false;
		Type type = null;
		for(Type currentType : types.values())
		{
			if(currentType.getDomainModel() != null)
			{
				if(type == null)
				{
					type = currentType;
				}
				else
				{
					ComponentType declaredType = ((Component) type.getDomainModel().getDomainModel()).getComponentType();
					ComponentType currentDeclaredType = ((Component) currentType.getDomainModel().getDomainModel()).getComponentType();
					if(!declaredType.isOrExtends(Resources.NETWORK.getId())
							&& (currentDeclaredType.isOrExtends(Resources.NETWORK.getId()) || (!declaredType.isOrExtends(Resources.BASE_CELL.getId()) && currentDeclaredType
									.isOrExtends(Resources.BASE_CELL.getId()))))
					{
						multipleTypes = false;
						type = currentType;
					}
					else if((!declaredType.isOrExtends(Resources.BASE_CELL.getId()) && !declaredType.isOrExtends(Resources.NETWORK.getId()))
							|| (declaredType.isOrExtends(Resources.BASE_CELL.getId()) && currentDeclaredType.isOrExtends(Resources.BASE_CELL.getId()))
							|| (declaredType.isOrExtends(Resources.NETWORK.getId()) && currentDeclaredType.isOrExtends(Resources.NETWORK.getId())))
					{
						multipleTypes = true;
					}
				}
			}
		}
		if(multipleTypes) throw new ModelInterpreterException("Multiple types found and no type id specified");
		if(type == null) throw new ModelInterpreterException("No type found when parsing NeuroML Model Interpreter");
		return type;
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
					XMLSerializer xmlSer = new XMLSerializer(true);
					String compString = xmlSer.writeObject((Component) domainModel.getDomainModel());
					serialisedModel = "<lems>" + compString + "</lems>";
					outputFile += ".xml";
				}
				else
				{
					// Convert to NeuroML
					XMLSerializer xmlSer = new XMLSerializer(true);
					String compString = xmlSer.writeObject((Component) domainModel.getDomainModel());

					serialisedModel = "<neuroml xmlns=\"http://www.neuroml.org/schema/neuroml2\"\n" + "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
							+ "      xsi:schemaLocation=\"http://www.neuroml.org/schema/neuroml2 " + NeuroMLElements.LATEST_SCHEMA_LOCATION + "\">" + compString + "</neuroml>";

					// Change extension to nml
					outputFile += ".nml";
				}

				// Write to disc
				PrintWriter writer = new PrintWriter(outputFolder + "/" + outputFile);
				writer.print(serialisedModel);
				writer.close();
				return outputFolder;

			}
			catch(IOException | LEMSException e)
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
			return new File((String) outputDomainModel.getDomainModel()).getParentFile();
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
