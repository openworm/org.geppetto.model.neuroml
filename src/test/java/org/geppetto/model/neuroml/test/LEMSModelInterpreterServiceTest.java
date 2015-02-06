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

package org.geppetto.model.neuroml.test;

import static org.junit.Assert.assertNotNull;

import java.net.MalformedURLException;
import java.net.URL;

import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.model.neuroml.services.LEMSModelInterpreterService;
import org.junit.Test;
import org.lemsml.jlems.api.LEMSBuildConfiguration;
import org.lemsml.jlems.api.LEMSBuildException;
import org.lemsml.jlems.api.LEMSBuildOptions;
import org.lemsml.jlems.api.LEMSBuildOptionsEnum;
import org.lemsml.jlems.api.LEMSBuilder;
import org.lemsml.jlems.api.interfaces.ILEMSBuildConfiguration;
import org.lemsml.jlems.api.interfaces.ILEMSBuildOptions;
import org.lemsml.jlems.api.interfaces.ILEMSBuilder;
import org.lemsml.jlems.api.interfaces.ILEMSDocument;

/**
 * @author matteocantarelli
 * 
 */
public class LEMSModelInterpreterServiceTest
{

	/**
	 * "" Test method for {@link org.geppetto.model.neuroml.services.LemsMLModelInterpreterService#readModel(java.net.URL)}.
	 * 
	 * @throws MalformedURLException
	 * @throws ModelInterpreterException
	 * @throws LEMSBuildException
	 */
	@Test
	public void testReadModel() throws MalformedURLException, ModelInterpreterException, LEMSBuildException
	{
		LEMSModelInterpreterService modelInterpreter = new LEMSModelInterpreterService();
		URL url = new URL("https://raw.githubusercontent.com/openworm/org.geppetto.samples/development/LEMS/SingleComponentHH/LEMS_NML2_Ex5_DetCell.xml");
		//URL url=this.getClass().getResource("/LEMS_NML2_Ex5_DetCell.xml");
		ModelWrapper model;

		model = (ModelWrapper) modelInterpreter.readModel(url, null, "");
		assertNotNull(model);
		assertNotNull(model.getModel("url"));
		assertNotNull(model.getModel("lems"));
		assertNotNull(model.getModel("neuroml"));
		
		ILEMSBuilder builder = new LEMSBuilder();
		// TODO Refactor simulators to deal with more than one model!
		ILEMSDocument lemsDocument = (ILEMSDocument) (model).getModel("lems");

		builder.addDocument(lemsDocument);

		ILEMSBuildOptions options = new LEMSBuildOptions();
		options.addBuildOption(LEMSBuildOptionsEnum.FLATTEN);

		ILEMSBuildConfiguration config = new LEMSBuildConfiguration();
		builder.build(config, options); // pre-build to read the run configuration and target from the file

	}

	/**
	 * "" Test method for {@link org.geppetto.model.neuroml.services.LemsMLModelInterpreterService#readModel(java.net.URL)}.
	 * 
	 * @throws MalformedURLException
	 * @throws ModelInterpreterException
	 * @throws LEMSBuildException
	 */
	@Test
	public void testReadC302Model() throws MalformedURLException, ModelInterpreterException, LEMSBuildException
	{
		LEMSModelInterpreterService modelInterpreter = new LEMSModelInterpreterService();
		URL url = new URL("https://raw.githubusercontent.com/openworm/org.geppetto.samples/development/LEMS/C302/LEMS_c302_A.xml");
		ModelWrapper model;

		model = (ModelWrapper) modelInterpreter.readModel(url, null, "");
		assertNotNull(model);
		assertNotNull(model.getModel("url"));
		assertNotNull(model.getModel("lems"));
		assertNotNull(model.getModel("neuroml"));

		ILEMSBuilder builder = new LEMSBuilder();
		// TODO Refactor simulators to deal with more than one model!
		ILEMSDocument lemsDocument = (ILEMSDocument) (model).getModel("lems");

		builder.addDocument(lemsDocument);

		ILEMSBuildOptions options = new LEMSBuildOptions();
		options.addBuildOption(LEMSBuildOptionsEnum.FLATTEN);

		ILEMSBuildConfiguration config = new LEMSBuildConfiguration();
		builder.build(config, options); // pre-build to read the run configuration and target from the file

	}

}
