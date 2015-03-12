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
package org.geppetto.model.neuroml.test;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.geppetto.core.conversion.ConversionException;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.services.IModelFormat;
import org.geppetto.model.neuroml.services.LEMSConversionService;
import org.geppetto.model.neuroml.services.LEMSModelInterpreterService;
import org.geppetto.model.neuroml.services.ModelFormat;
import org.junit.Test;
import org.lemsml.jlems.api.LEMSBuildException;
import org.lemsml.jlems.core.sim.LEMSException;

/**
 * @author Adrian Quintana (adrian.perez@ucl.ac.uk)
 * 
 */
public class LEMSConversionServiceTest
{

	/**
	 * "" Test method for {@link org.geppetto.model.neuroml.services.LEMSConversionService#readModel(java.net.URL)}.
	 * 
	 * @throws ModelInterpreterException
	 * @throws IOException
	 * @throws LEMSException
	 * @throws LEMSBuildException
	 */
	@Test
	public void testTargetedLemsCellModel() throws ConversionException, ModelInterpreterException, LEMSException, IOException
	{
		LEMSConversionService lemsConversionService = new LEMSConversionService();

		// HH
		LEMSModelInterpreterService modelInterpreter = new LEMSModelInterpreterService();
		URL url = new URL("https://raw.githubusercontent.com/openworm/org.geppetto.samples/development/LEMS/SingleComponentHH/LEMS_NML2_Ex5_DetCell.xml");

		ModelWrapper modelWrapper = (ModelWrapper) modelInterpreter.readModel(url, null, "");

		// lemsConversionService.convert(modelWrapper, ModelFormat.LEMS, ModelFormat.NEURON);

	}

	/**
	 * "" Test method for {@link org.geppetto.model.neuroml.services.LEMSConversionService#readModel(java.net.URL)}.
	 * 
	 * @throws ModelInterpreterException
	 * @throws IOException
	 * @throws LEMSException
	 * @throws LEMSBuildException
	 */
	@Test
	public void testOutputsFormats() throws ConversionException, ModelInterpreterException, LEMSException, IOException
	{
		LEMSConversionService lemsConversionService = new LEMSConversionService();

		// HH
		LEMSModelInterpreterService modelInterpreter = new LEMSModelInterpreterService();
		URL url = new URL("https://raw.githubusercontent.com/openworm/org.geppetto.samples/development/LEMS/SingleComponentHH/LEMS_NML2_Ex5_DetCell.xml");

		ModelWrapper modelWrapper = (ModelWrapper) modelInterpreter.readModel(url, null, "");

		List<IModelFormat> modelFormats = lemsConversionService.getSupportedOutputs();

		modelFormats = lemsConversionService.getSupportedOutputs(modelWrapper, ModelFormat.LEMS);
	}

	/**
	 * "" Test method for {@link org.geppetto.model.neuroml.services.LEMSConversionService#readModel(java.net.URL)}.
	 * 
	 * @throws ModelInterpreterException
	 * @throws IOException
	 * @throws LEMSException
	 * @throws LEMSBuildException
	 */
	@Test
	public void testNeuroMLCellModel() throws ConversionException, ModelInterpreterException, LEMSException, IOException
	{
		// NeuroMLConversionService neuroMLConversionService = new NeuroMLConversionService();
		//
		// //Purkinje
		// NeuroMLModelInterpreterService modelInterpreter = new NeuroMLModelInterpreterService();
		// URL url = new URL("https://raw.github.com/openworm/org.geppetto.samples/master/NeuroML/Purkinje/purk.nml");
		//
		// ModelWrapper model = (ModelWrapper) modelInterpreter.readModel(url, null, "");
		// neuroMLConversionService.convert(model, new ModelFormat(ConversionUtils.NEUROML_MODELFORMAT), new ModelFormat(ConversionUtils.NEURON_MODELFORMAT));
	}

	/**
	 * "" Test method for {@link org.geppetto.model.neuroml.services.LEMSConversionService#readModel(java.net.URL)}.
	 * 
	 * @throws ModelInterpreterException
	 * @throws IOException
	 * @throws LEMSException
	 * @throws LEMSBuildException
	 */
	@Test
	public void testNeuroMLChannelModel() throws ConversionException, ModelInterpreterException, LEMSException, IOException
	{
		// NeuroMLConversionService neuroMLConversionService = new NeuroMLConversionService();
		//
		// //Channel
		// NeuroMLModelInterpreterService modelInterpreter = new NeuroMLModelInterpreterService();
		// URL url = new URL("https://raw.githubusercontent.com/OpenSourceBrain/GranCellLayer/master/neuroConstruct/generatedNeuroML2/Gran_CaHVA_98.channel.nml");
		//
		// ModelWrapper model = (ModelWrapper) modelInterpreter.readModel(url, null, "");
		// neuroMLConversionService.convert(model, new ModelFormat(ConversionUtils.NEUROML_MODELFORMAT), new ModelFormat(ConversionUtils.NEURON_MODELFORMAT));

	}

}
