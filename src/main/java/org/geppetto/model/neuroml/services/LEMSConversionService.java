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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.RandomStringUtils;
import org.geppetto.core.conversion.AConversion;
import org.geppetto.core.conversion.ConversionException;
import org.geppetto.core.model.IModel;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.services.IModelFormat;
import org.geppetto.core.services.registry.ServicesRegistry;
import org.lemsml.export.base.IBaseWriter;
import org.lemsml.jlems.core.expression.ParseError;
import org.lemsml.jlems.core.sim.ContentError;
import org.lemsml.jlems.core.sim.LEMSException;
import org.lemsml.jlems.core.type.Lems;
import org.neuroml.export.exceptions.GenerationException;
import org.neuroml.export.exceptions.ModelFeatureSupportException;
import org.neuroml.export.utils.ExportFactory;
import org.neuroml.export.utils.Format;
import org.neuroml.export.utils.SupportedFormats;
import org.neuroml.model.util.NeuroMLException;
import org.springframework.stereotype.Service;

/**
 * @author Adrian Quintana (adrian.perez@ucl.ac.uk)
 * 
 */
@Service
public class LEMSConversionService extends AConversion
{

	@Override
	public List<IModelFormat> getSupportedInputs() throws ConversionException
	{
		return new ArrayList<IModelFormat>(Arrays.asList(ModelFormat.LEMS));
	}

	@Override
	public List<IModelFormat> getSupportedOutputs() throws ConversionException
	{
		//return new ArrayList<IModelFormat>(Arrays.asList(ModelFormat.NEURON));
		List<IModelFormat> modelFormats = new ArrayList<IModelFormat>(); 
		for (Format format : SupportedFormats.getSupportedOutputs()){
			IModelFormat modelFormat = ModelFormat.fromExportValue(format.toString());
			if (modelFormat != null) modelFormats.add(modelFormat);
		}
		return modelFormats;
	}

	@Override
	public List<IModelFormat> getSupportedOutputs(IModel model, IModelFormat input) throws ConversionException
	{
		//return getSupportedOutputs();
		Lems lems = (Lems) ((ModelWrapper) model).getModel(input);
		processLems(lems);
		List<IModelFormat> modelFormats = new ArrayList<IModelFormat>(); 
		try
		{
			for (Format format : SupportedFormats.getSupportedOutputs(lems)){
				ModelFormat modelFormat = ModelFormat.fromExportValue(format.toString());
				if (modelFormat != null) modelFormats.add(modelFormat);
			}
		}
		catch(NeuroMLException | LEMSException e)
		{
			throw new ConversionException(e);
		}
		return modelFormats;
	}

	@Override
	public IModel convert(IModel model, IModelFormat input, IModelFormat output) throws ConversionException
	{
		//checkSupportedFormat(input);

		Lems lems = (Lems) ((ModelWrapper) model).getModel(ModelFormat.LEMS);
		processLems(lems);

		ModelWrapper outputModel = new ModelWrapper(UUID.randomUUID().toString());
		try
		{
			String tmpFolder = output.toString() + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
			File outputFolder = new File(this.getConvertedResultsPath(), tmpFolder);
			if(!outputFolder.exists()) outputFolder.mkdirs();
			//This only works in linux
//			Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxrwx--x");
//		    FileAttribute<Set<PosixFilePermission>> fileAttributes = PosixFilePermissions.asFileAttribute(perms);
//			Path tmpFolder = Files.createTempDirectory(outputFolder.toPath(), output.toString(), new FileAttribute<?>[0]);
			
			//FIXME: the py extension can be added inside.
			String outputFileName = "main_script.py"; 
			
			IBaseWriter exportWriter = ExportFactory.getExportWriter(lems, outputFolder, outputFileName, ModelFormat.valueOf(output.toString()).getExportValue());
			List<File> outputFiles = exportWriter.convert();
			outputModel.wrapModel(output, outputFolder + System.getProperty("file.separator") + outputFileName);
		}
		catch(GenerationException | IOException | ModelFeatureSupportException | NeuroMLException | LEMSException e)
		{
			throw new ConversionException(e);
		}

		return outputModel;
	}

	private void processLems(Lems lems) throws ConversionException
	{
		try
		{
			lems.setResolveModeLoose();
			lems.deduplicate();
			lems.resolve();
			lems.evaluateStatic();
		}
		catch(ContentError | ParseError e)
		{
			throw new ConversionException(e);
		}
	}

}
