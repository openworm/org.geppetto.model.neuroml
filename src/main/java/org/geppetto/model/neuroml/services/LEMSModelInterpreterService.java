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
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.beans.ModelInterpreterConfig;
import org.geppetto.core.conversion.ConversionException;
import org.geppetto.core.data.model.IAspectConfiguration;
import org.geppetto.core.model.AModelInterpreter;
import org.geppetto.core.model.IModel;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.model.runtime.AspectNode;
import org.geppetto.core.model.runtime.EntityNode;
import org.geppetto.core.services.ModelFormat;
import org.geppetto.core.services.registry.ServicesRegistry;
import org.geppetto.core.utilities.URLReader;
import org.geppetto.model.neuroml.features.LEMSParametersFeature;
import org.geppetto.model.neuroml.features.LEMSSimulationTreeFeature;
import org.geppetto.model.neuroml.features.LEMSVisualTreeFeature;
import org.geppetto.model.neuroml.utils.LEMSAccessUtility;
import org.geppetto.model.neuroml.utils.NeuroMLAccessUtility;
import org.geppetto.model.neuroml.utils.OptimizedLEMSReader;
import org.lemsml.jlems.api.LEMSDocumentReader;
import org.lemsml.jlems.api.interfaces.ILEMSDocument;
import org.lemsml.jlems.api.interfaces.ILEMSDocumentReader;
import org.lemsml.jlems.core.sim.ContentError;
import org.lemsml.jlems.core.type.Lems;
import org.lemsml.jlems.io.util.FileUtil;
import org.lemsml.jlems.io.xmlio.XMLSerializer;
import org.neuroml.model.Base;
import org.neuroml.model.BaseCell;
import org.neuroml.model.NeuroMLDocument;
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
public class LEMSModelInterpreterService extends AModelInterpreter
{

	private static Log _logger = LogFactory.getLog(LEMSModelInterpreterService.class);
	private NeuroMLModelInterpreterService _neuroMLModelInterpreter = new NeuroMLModelInterpreterService();

	@Autowired
	private ModelInterpreterConfig jlemsModelInterpreterConfig;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openworm.simulationengine.core.model.IModelProvider#readModel(java .lang.String)
	 */
	public IModel readModel(URL url, List<URL> recordings, String instancePath) throws ModelInterpreterException
	{
		dependentModels.clear();
		ModelWrapper model = new ModelWrapper(instancePath);
		try
		{
			OptimizedLEMSReader reader = new OptimizedLEMSReader(dependentModels);
			int index = url.toString().lastIndexOf('/');
			String urlBase = url.toString().substring(0, index + 1);
			reader.read(url, urlBase, OptimizedLEMSReader.NMLDOCTYPE.LEMS);

			model = new ModelWrapper(UUID.randomUUID().toString());
			model.setInstancePath(instancePath);

			/*
			 * LEMS
			 */
			long start = System.currentTimeMillis();
			ILEMSDocumentReader lemsReader = new LEMSDocumentReader();
			ILEMSDocument document = lemsReader.readModel(reader.getLEMSString());
			_logger.info("Parsed LEMS document, took " + (System.currentTimeMillis() - start) + "ms");
			/*
			 * PrintWriter out = new PrintWriter("LEMS.txt"); out.println(reader.getLEMSString()); out.close();
			 */

			model = new ModelWrapper(UUID.randomUUID().toString());
			model.setInstancePath(instancePath);
			/*
			 * NEUROML
			 */
			if(!reader.getNeuroMLString().isEmpty())
			{
				start = System.currentTimeMillis();
				NeuroMLConverter neuromlConverter = new NeuroMLConverter();
				NeuroMLDocument neuroml_inclusions = neuromlConverter.loadNeuroML(reader.getNeuroMLString());
				_logger.info("Parsed NeuroML document of size " + reader.getNeuroMLString().length() / 1024 + "KB, took " + (System.currentTimeMillis() - start) + "ms");
				model.wrapModel(ServicesRegistry.getModelFormat("NEUROML"), neuroml_inclusions);

				// add visual tree feature to the model service
				this.addFeature(new LEMSVisualTreeFeature(neuroml_inclusions, document));
			}

			model.wrapModel(ServicesRegistry.getModelFormat("LEMS"), document);
			model.wrapModel(NeuroMLAccessUtility.URL_ID, url);

			/*
			 * out = new PrintWriter("NEUROML.txt"); out.println(reader.getNeuroMLString()); out.close();
			 */

			// TODO: This need to be changed (BaseCell, String)
			model.wrapModel(NeuroMLAccessUtility.SUBENTITIES_MAPPING_ID, new HashMap<BaseCell, EntityNode>());
			model.wrapModel(NeuroMLAccessUtility.CELL_SUBENTITIES_MAPPING_ID, new HashMap<String, BaseCell>());

			model.wrapModel(NeuroMLAccessUtility.DISCOVERED_COMPONENTS, new HashMap<String, Base>());
			model.wrapModel(LEMSAccessUtility.DISCOVERED_LEMS_COMPONENTS, new HashMap<String, Object>());
			model.wrapModel(NeuroMLAccessUtility.DISCOVERED_NESTED_COMPONENTS_ID, new ArrayList<String>());

			addRecordings(recordings, instancePath, model);

			this.addFeature(new LEMSParametersFeature(this._neuroMLModelInterpreter.getPopulateModelTree(), model));
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
		return _neuroMLModelInterpreter.populateModelTree(aspectNode);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geppetto.core.model.IModelInterpreter#populateRuntimeTree(org.geppetto .core.model.runtime.AspectNode)
	 */
	@Override
	public boolean populateRuntimeTree(AspectNode aspectNode) throws ModelInterpreterException
	{
		return _neuroMLModelInterpreter.populateRuntimeTree(aspectNode);
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
	public File downloadModel(AspectNode aspectNode, ModelFormat format, List<? extends IAspectConfiguration> aspectConfigurations) throws ModelInterpreterException
	{
		if(format.equals(ServicesRegistry.getModelFormat("LEMS")) || format.equals(ServicesRegistry.getModelFormat("NEUROML")))
		{
			try
			{
				// Create file and folder
				File outputFolder = URLReader.createProjectFolder(format, getPathConfiguration().getConvertedResultsPath());
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
			ModelWrapper outputModel = null;
			try
			{
				for(IAspectConfiguration aspectConfig : aspectConfigurations)
				{
					outputModel = (ModelWrapper) lemsConversionService.convert(aspectNode.getModel(), ServicesRegistry.getModelFormat("LEMS"), format, aspectConfig);
				}
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
		supportedOutputs.add(ServicesRegistry.getModelFormat("NEUROML"));
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

}
