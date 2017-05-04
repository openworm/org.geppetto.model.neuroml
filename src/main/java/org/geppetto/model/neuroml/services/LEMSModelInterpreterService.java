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
package org.geppetto.model.neuroml.services;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.beans.ModelInterpreterConfig;
import org.geppetto.core.data.model.IAspectConfiguration;
import org.geppetto.core.model.AModelInterpreter;
import org.geppetto.core.model.GeppettoModelAccess;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.services.registry.ServicesRegistry;
import org.geppetto.model.GeppettoLibrary;
import org.geppetto.model.ModelFormat;
import org.geppetto.model.neuroml.features.DefaultViewCustomiserFeature;
import org.geppetto.model.neuroml.features.LEMSParametersFeature;
import org.geppetto.model.neuroml.utils.OptimizedLEMSReader;
import org.geppetto.model.types.Type;
import org.geppetto.model.util.GeppettoVisitingException;
import org.geppetto.model.values.Pointer;
import org.lemsml.jlems.core.sim.LEMSException;
import org.neuroml.model.util.NeuroMLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author matteocantarelli
 * @author Adrian Quintana (adrian.perez@ucl.ac.uk)
 * 
 */
@Service
public class LEMSModelInterpreterService extends AModelInterpreter
{

	private static Log _logger = LogFactory.getLog(LEMSModelInterpreterService.class);
	private NeuroMLModelInterpreterService _neuroMLModelInterpreter = new NeuroMLModelInterpreterService();

	@Autowired
	private ModelInterpreterConfig jlemsModelInterpreterConfig;

	public LEMSModelInterpreterService()
	{
		super();
		
		// Add LEMS Parameter Feature
		this.addFeature(new LEMSParametersFeature());
		this.addFeature(new DefaultViewCustomiserFeature());
	}

	@Override
	public Type importType(URL url, String typeId, GeppettoLibrary library, GeppettoModelAccess access) throws ModelInterpreterException
	{
		long startTime = System.currentTimeMillis();

		Type type;
		dependentModels.clear();

		try
		{
			// Read main and includes as a String
			OptimizedLEMSReader reader = new OptimizedLEMSReader(this.dependentModels);
			reader.readAllFormats(url);

			// Extract Types from the lems/neuroml files
			type = _neuroMLModelInterpreter.extractTypes(url, typeId, library, access, reader.getLEMSDocument(), reader.getNeuroMLDocument());
		}
		catch(IOException | NumberFormatException | NeuroMLException | LEMSException | GeppettoVisitingException e)
		{
			e.printStackTrace();
			throw new ModelInterpreterException(e);
		}

		long endTime = System.currentTimeMillis();
		_logger.info("Import Type took " + (endTime - startTime) + " milliseconds for url " + url + " and typename " + typeId);
		return type;
	}

	@Override
	public String getName()
	{
		return this.jlemsModelInterpreterConfig.getModelInterpreterName();
	}

	@Override
	public void registerGeppettoService()
	{
		List<ModelFormat> modelFormats = new ArrayList<ModelFormat>(Arrays.asList(ServicesRegistry.registerModelFormat("LEMS")));
		ServicesRegistry.registerModelInterpreterService(this, modelFormats);
	}

	@Override
	public File downloadModel(Pointer pointer, ModelFormat format, IAspectConfiguration aspectConfiguration) throws ModelInterpreterException
	{
		// AQP
		return _neuroMLModelInterpreter.downloadModel(pointer, format, aspectConfiguration);

		// We are taking the domain model for the last element of the pointer
		// IModel model = (IModel) pointer.getElements().get(pointer.getElements().size() - 1).getType().getDomainModel();
		//
		// if(format.equals(ServicesRegistry.getModelFormat("LEMS")) || format.equals(ServicesRegistry.getModelFormat("NEUROML")))
		// {
		// try
		// {
		// // Create file and folder
		// File outputFolder = PathConfiguration.createFolderInProjectTmpFolder(getScope(), projectId,
		// PathConfiguration.getName(format.getModelFormat() + PathConfiguration.downloadModelFolderName, true));
		// String outputFile = ((URL) ((ModelWrapper) model).getModel(NeuroMLAccessUtility.URL_ID)).getPath();
		//
		// // Serialise objects
		// String serialisedModel = "";
		// if(format.equals(ServicesRegistry.getModelFormat("LEMS")))
		// {
		// // Serialise LEMS object
		// Lems lems = (Lems) ((ModelWrapper) model).getModel(ServicesRegistry.getModelFormat("LEMS"));
		// serialisedModel = XMLSerializer.serialize(lems);
		// }
		// else
		// {
		// // Serialise NEUROML object
		// NeuroMLDocument neuroMLDoc = (NeuroMLDocument) ((ModelWrapper) model).getModel(ServicesRegistry.getModelFormat("NEUROML"));
		// NeuroMLConverter neuroMLConverter = new NeuroMLConverter();
		// serialisedModel = neuroMLConverter.neuroml2ToXml(neuroMLDoc);
		// // Change extension to nml
		// outputFile = outputFile.substring(0, outputFile.lastIndexOf(".") + 1) + "nml";
		// }
		//
		// // Write to disc
		// PrintWriter writer = new PrintWriter(outputFolder + outputFile.substring(outputFile.lastIndexOf("/")));
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
		//
		// // Call conversion service
		// LEMSConversionService lemsConversionService = new LEMSConversionService();
		// lemsConversionService.setProjectId(projectId);
		// lemsConversionService.setScope(Scope.CONNECTION);
		// ModelWrapper outputModel = null;
		// try
		// {
		// outputModel = (ModelWrapper) lemsConversionService.convert(model, ServicesRegistry.getModelFormat("LEMS"), format, aspectConfiguration);
		// }
		// catch(ConversionException e)
		// {
		// throw new ModelInterpreterException(e);
		// }
		// String outputFile = (String) outputModel.getModel(format);
		// return new File(outputFile.substring(0, outputFile.lastIndexOf(File.separator)));
		// }
	}

	@Override
	public List<ModelFormat> getSupportedOutputs(Pointer pointer) throws ModelInterpreterException
	{
		// AQP
		return _neuroMLModelInterpreter.getSupportedOutputs(pointer);

		// List<ModelFormat> supportedOutputs = super.getSupportedOutputs(pointer);
		// supportedOutputs.add(ServicesRegistry.getModelFormat("NEUROML"));
		// try
		// {
		// // We are taking the domain model for the last element of the pointer
		// IModel model = (IModel) pointer.getElements().get(pointer.getElements().size() - 1).getType().getDomainModel();
		//
		// LEMSConversionService lemsConversionService = new LEMSConversionService();
		// supportedOutputs.addAll(lemsConversionService.getSupportedOutputs(model, ServicesRegistry.getModelFormat("LEMS")));
		// }
		// catch(ConversionException e)
		// {
		// throw new ModelInterpreterException(e);
		// }
		// return supportedOutputs;
	}

}