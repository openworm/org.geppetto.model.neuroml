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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.geppetto.core.beans.PathConfig;
import org.geppetto.core.conversion.AConversion;
import org.geppetto.core.conversion.ConversionException;
import org.geppetto.core.model.IModel;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.services.IModelFormat;
import org.geppetto.core.services.registry.ServicesRegistry;
import org.geppetto.model.neuroml.utils.NeuroMLAccessUtility;
import org.lemsml.export.base.IBaseWriter;
import org.lemsml.export.c.CWriter;
import org.lemsml.export.dlems.DLemsWriter;
import org.lemsml.export.matlab.MatlabWriter;
import org.lemsml.export.modelica.ModelicaWriter;
import org.lemsml.export.sedml.SEDMLWriter;
import org.lemsml.jlems.core.expression.ParseError;
import org.lemsml.jlems.core.sim.ContentError;
import org.lemsml.jlems.core.sim.LEMSException;
import org.lemsml.jlems.core.type.Lems;
import org.neuroml.export.brian.BrianWriter;
import org.neuroml.export.cellml.CellMLWriter;
import org.neuroml.export.dnsim.DNSimWriter;
import org.neuroml.export.exceptions.GenerationException;
import org.neuroml.export.exceptions.ModelFeatureSupportException;
import org.neuroml.export.graph.GraphWriter;
import org.neuroml.export.nest.NestWriter;
import org.neuroml.export.neuron.NeuronWriter;
import org.neuroml.export.pynn.PyNNWriter;
import org.neuroml.export.sbml.SBMLWriter;
import org.neuroml.export.utils.ExportFactory;
import org.neuroml.export.xpp.XppWriter;
import org.neuroml.model.util.NeuroMLException;
import org.springframework.stereotype.Service;

/**
 * @author Adrian Quintana (adrian.perez@ucl.ac.uk)
 * 
 */
@Service
public class LEMSConversionService extends AConversion
{
	private PathConfig pathConfig = new PathConfig();
	private ExportFactory exportFactory = new ExportFactory();

	public LEMSConversionService()
	{
		super();
		this.addSupportedInput(ModelFormat.LEMS);
	}

	@Override
	public IModel convert(IModel model, IModelFormat input, IModelFormat output) throws ConversionException
	{
		checkSupportedFormat(input);

		Lems lems = (Lems) ((ModelWrapper) model).getModel(ModelFormat.LEMS);
		try
		{
			lems.setResolveModeLoose();
			lems.deduplicate();
			lems.resolve();
			lems.evaluateStatic();
		}
		catch(ContentError | ParseError e)
		{
			e.printStackTrace();
			throw new ConversionException(e);
		}

		File outputFolder = new File(this.pathConfig.getConvertedResultsPath());
		String outputFileName = "main_script.py";
		if (!outputFolder.exists()){
			outputFolder.mkdirs();
		}
		
		IBaseWriter exportWriter;
		try
		{
			exportWriter = exportFactory.getExportWriter(lems, outputFolder, outputFileName, ((ModelFormat)output).getExportValue());
		}
		catch(ModelFeatureSupportException | NeuroMLException | LEMSException e1)
		{
			e1.printStackTrace();
			throw new ConversionException(e1);			
		}
		
		List<File> outputFiles;
		try
		{
			outputFiles = exportWriter.convert();
		}
		catch(GenerationException | IOException e)
		{
			throw new ConversionException(e);
		}

		ModelWrapper outputModel = new ModelWrapper(UUID.randomUUID().toString());
		// Remove until deciding if it is needed
		//outputModel.setInstancePath(model.getInstancePath());
		outputModel.wrapModel(output, outputFolder + System.getProperty("file.separator") + outputFileName);

		return outputModel;
	}

	@Override
	public List<IModelFormat> getSupportedOutputs(IModel model, IModelFormat input) throws ConversionException
	{
		//FIXME: We need to call a method in the export librarry
		List<IModelFormat> modelFormatList = new ArrayList<IModelFormat>();
		modelFormatList.add(ModelFormat.NEURON);
		return modelFormatList;
	}
	
	@Override
	public List<IModelFormat> getSupportedOutputs()
	{
		List<IModelFormat> modelFormatList = new ArrayList<IModelFormat>();
		modelFormatList.add(ModelFormat.NEURON);
		return modelFormatList;
	}

	@Override
	public void registerGeppettoService()
	{
		List<IModelFormat> modelFormatList = new ArrayList<IModelFormat>();
		modelFormatList.add(ModelFormat.NEUROML);
		ServicesRegistry.registerConversionService(this, getSupportedInputs(), getSupportedOutputs());
	}

}
