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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.log.NullLogChute;
import org.geppetto.core.conversion.AConversion;
import org.geppetto.core.conversion.ConversionException;
import org.geppetto.core.model.IModel;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.services.ModelFormat;
import org.geppetto.core.services.registry.ServicesRegistry;
import org.geppetto.model.neuroml.utils.NeuroMLAccessUtility;
import org.lemsml.jlems.core.type.Lems;
import org.neuroml.export.neuron.NeuronWriter;
import org.springframework.stereotype.Service;

/**
 * @author Adrian Quintana (adrian.perez@ucl.ac.uk)
 * 
 */
@Service
public class LEMSConversionService extends AConversion{
	
	public LEMSConversionService() {
		super();
		this.addSupportedInput(new ModelFormat(ConversionUtils.LEMS_MODELFORMAT));
	}

	@Override
	public List<ModelFormat> getSupportedOutputs(IModel model, ModelFormat input) throws ConversionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IModel convert(IModel model, ModelFormat input, ModelFormat output) throws ConversionException {
		checkSupportedFormat(input);
		
		if (output.equals(ConversionUtils.NEURON_MODELFORMAT)){
			Lems lems = (Lems) ((ModelWrapper) model).getModel(NeuroMLAccessUtility.LEMS_ID);
			try {
				lems.setResolveModeLoose();
				lems.deduplicate();
				lems.resolve();
				lems.evaluateStatic();
				
				Properties props = new Properties();
				props.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM_CLASS, NullLogChute.class.getName());                        
				Velocity.init(props);
	
				//TODO: where should we create the tmp file?
				File mainFile = File.createTempFile("temp-file-name", "_nrn.py", new File("/home/adrian/tmp/"));
				NeuronWriter nw = new NeuronWriter(lems);
				nw.setNoGui(true);
	
		         List<File> ff = nw.generateMainScriptAndMods(mainFile);
		         for (File f : ff) {
		             System.out.println("Generated: " + f.getAbsolutePath());
		         }
		         
		         ModelWrapper outputModel = new ModelWrapper(UUID.randomUUID().toString());
		         outputModel.setInstancePath(model.getInstancePath());
		         outputModel.wrapModel(ConversionUtils.NEURON_MODELFORMAT, ff);
	         
			} catch (Exception e) {
				e.printStackTrace();
				throw new ConversionException(e);
			}
			
		}
		else if (output.equals(ConversionUtils.NEURON_MODELFORMAT)){
			
		}
		return null;
	}

	@Override
	public List<ModelFormat> getSupportedOutputs() throws ConversionException
	{
		List<ModelFormat> modelFormatList = new ArrayList<ModelFormat>();
		modelFormatList.add(new ModelFormat(ConversionUtils.NEURON_MODELFORMAT));
		return modelFormatList;
	}

	@Override
	public void registerGeppettoService()
	{
		List<ModelFormat> modelFormatList = new ArrayList<ModelFormat>();
		modelFormatList.add(new ModelFormat(ConversionUtils.NEUROML_MODELFORMAT));
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
