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

import java.io.File;
import java.net.MalformedURLException;

import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.model.neuroml.services.LEMSModelInterpreterService;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.lemsml.jlems.api.LEMSBuildException;

/**
 * @author Adrian Quintana (adrian.perez@ucl.ac.uk)
 * 
 */
public class LEMSModelInterpreterServiceTest
{
    private ModelInterpreterTestUtils modelInterpreterTestUtils;

	@Before
	public void oneTimeSetUp()
	{
		modelInterpreterTestUtils = new ModelInterpreterTestUtils();
	}
	/**
	 * "" Test method for {@link org.geppetto.model.neuroml.services.LemsMLModelInterpreterService#readModel(java.net.URL)}.
	 * 
	 * @throws MalformedURLException
	 * @throws ModelInterpreterException
	 * @throws LEMSBuildException
	 */
	@Test
	public void testReadModelHHCell() throws Exception
	{
		modelInterpreterTestUtils.serialise("/hhcell/LEMS_NML2_Ex5_DetCell.xml", "hhcell", new LEMSModelInterpreterService());
//		ModelInterpreterTestUtils.serialise("/hhcell/LEMS_NML2_Ex5_DetCell.xml", null, new LEMSModelInterpreterService());
//		ModelInterpreterTestUtils.serialise("/hhcell/LEMS_NML2_Ex5_DetCell.xml", "net1", new LEMSModelInterpreterService());
	}

	
	@Test
	public void testReadModelc302() throws Exception
	{
//		ModelInterpreterTestUtils.serialise("/c302/LEMS_c302_A.xml", "c302_A", new LEMSModelInterpreterService());
		modelInterpreterTestUtils.serialise("/c302/LEMS_c302_A.xml", null, new LEMSModelInterpreterService());
	}
	
	@Test
	public void testReadModelMuscle() throws Exception
	{
//		ModelInterpreterTestUtils.serialise("/muscle/LEMS_NeuronMuscle.xml", "net1", new LEMSModelInterpreterService());
//		ModelInterpreterTestUtils.serialise("/muscle/LEMS_NeuronMuscle.xml", null, new LEMSModelInterpreterService());
	}
	
	@AfterClass
	public static void doYourOneTimeTeardown()
	{
		 File c302 = new File("./src/test/resources/LEMS_c302_A.xml.xmi");
		 c302.delete();
		 
//		 File muscle = new File("./src/test/resources/LEMS_NeuronMuscle.xml.xmi");
//		 muscle.delete();
		
		 File hhcell = new File("./src/test/resources/LEMS_NML2_Ex5_DetCell.xml.xmi");
		 hhcell.delete();

	}
}
