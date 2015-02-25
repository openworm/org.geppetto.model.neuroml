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

import org.geppetto.core.conversion.AConversion;
import org.geppetto.core.conversion.ConversionException;
import org.geppetto.core.model.IModel;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.services.ModelFormat;
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

	public LEMSConversionService()
	{
		super();
		this.addSupportedInput(new ModelFormat(Format.LEMS_MODELFORMAT));
	}

	@Override
	public IModel convert(IModel model, ModelFormat input, ModelFormat output) throws ConversionException
	{
		checkSupportedFormat(input);

		Lems lems = (Lems) ((ModelWrapper) model).getModel(Format.LEMS_MODELFORMAT);
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

		// TODO: where should we create the tmp file?
		IBaseWriter exportWriter = null;
		File outputFolder = new File("/home/adrian/tmp/");
		String outputFileName = "taka";
		try
		{
			if(output.equals(Format.C_MODELFORMAT))
			{
//				String outputFileName = "";
				exportWriter = new CWriter(lems, outputFolder, outputFileName);
			}
			else if(output.equals(Format.DLEMS_MODELFORMAT))
			{
//				String outputFileName = "";
				exportWriter = new DLemsWriter(lems, outputFolder, outputFileName);
			}
			else if(output.equals(Format.MATLAB_MODELFORMAT))
			{
				// String outputFileName = "";
				exportWriter = new MatlabWriter(lems, outputFolder, outputFileName);
			}
			else if(output.equals(Format.MODELICA_MODELFORMAT))
			{
				// String outputFileName = "";
				exportWriter = new ModelicaWriter(lems, outputFolder, outputFileName);
			}
			else if(output.equals(Format.SEDML_MODELFORMAT))
			{
				// String outputFileName = "";
				String inputFileName = ((URL)((ModelWrapper) model).getModel(NeuroMLAccessUtility.URL_ID)).getPath();
				exportWriter = new SEDMLWriter(lems, outputFolder, outputFileName, inputFileName);
			}
			else if(output.equals(Format.BRIAN_MODELFORMAT))
			{
				// String outputFileName = "";
				exportWriter = new BrianWriter(lems, outputFolder, outputFileName);
			}
			else if(output.equals(Format.CELLML_MODELFORMAT))
			{
				// String outputFileName = "";
				exportWriter = new CellMLWriter(lems, outputFolder, outputFileName);
			}
			else if(output.equals(Format.DN_SIM_MODELFORMAT))
			{
				// String outputFileName = "";
				exportWriter = new DNSimWriter(lems, outputFolder, outputFileName);
			}
			else if(output.equals(Format.GRAPH_MODELFORMAT))
			{
				// String outputFileName = "";
				exportWriter = new GraphWriter(lems, outputFolder, outputFileName);
			}
			else if(output.equals(Format.NEST_MODELFORMAT))
			{
				// String outputFileName = "";
				exportWriter = new NestWriter(lems, outputFolder, outputFileName);
			}
			else if(output.equals(Format.NEURON_MODELFORMAT))
			{
				//String outputFileName = "temp-file-name_nrn.py";
				exportWriter = new NeuronWriter(lems, outputFolder, outputFileName);
			}
			else if(output.equals(Format.PYNN_MODELFORMAT))
			{
				// String outputFileName = "";
				exportWriter = new PyNNWriter(lems, outputFolder, outputFileName);
			}
			else if(output.equals(Format.SBML_MODELFORMAT))
			{
				// String outputFileName = "";
				exportWriter = new SBMLWriter(lems, outputFolder, outputFileName);
			}
			else if(output.equals(Format.SVG_MODELFORMAT))
			{
				//FIXME: We need to look for a method which converts from lems to neuroml
				// String outputFileName = "";
				//exportWriter = new SVGWriter(lems, outputFolder, outputFileName);
			}
			else if(output.equals(Format.XINEML_MODELFORMAT))
			{
				//FIXME: This conversion allows to two input formats : SPINEML and NINEML
				// String outputFileName = "";
//				exportWriter = new XineMLWriter(lems, outputFolder, outputFileName);
			}
			else if(output.equals(Format.XPP_MODELFORMAT))
			{
				// String outputFileName = "";
				exportWriter = new XppWriter(lems, outputFolder, outputFileName);
			}
		}
		catch(ModelFeatureSupportException | NeuroMLException | LEMSException e)
		{
			e.printStackTrace();
			throw new ConversionException(e);
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
		outputModel.wrapModel(output.getFormat(), outputFiles);

		return outputModel;
	}

	@Override
	public List<ModelFormat> getSupportedOutputs(IModel model, ModelFormat input) throws ConversionException
	{
		//FIXME: We need to call a method in the export librarry
		List<ModelFormat> modelFormatList = new ArrayList<ModelFormat>();
		modelFormatList.add(new ModelFormat(Format.NEURON_MODELFORMAT));
		return modelFormatList;
	}
	
	@Override
	public List<ModelFormat> getSupportedOutputs() throws ConversionException
	{
		List<ModelFormat> modelFormatList = new ArrayList<ModelFormat>();
		modelFormatList.add(new ModelFormat(Format.NEURON_MODELFORMAT));
		return modelFormatList;
	}

	@Override
	public void registerGeppettoService()
	{
		List<ModelFormat> modelFormatList = new ArrayList<ModelFormat>();
		modelFormatList.add(new ModelFormat(Format.NEUROML_MODELFORMAT));
		try
		{
			ServicesRegistry.registerConversionService(this, getSupportedInputs(), getSupportedOutputs());
		}
		catch(ConversionException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
