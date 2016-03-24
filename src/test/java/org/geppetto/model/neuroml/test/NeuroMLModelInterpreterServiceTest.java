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

import org.geppetto.model.neuroml.services.NeuroMLModelInterpreterService;
import org.junit.AfterClass;
import org.junit.Test;

/**
 * @author Adrian Quintana (adrian.perez@ucl.ac.uk)
 * 
 */
public class NeuroMLModelInterpreterServiceTest
{

	/**
	 * Test method for {@link org.geppetto.model.neuroml.services.LemsMLModelInterpreterService#readModel(java.net.URL)}.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testReadModelACnet() throws Exception
	{
//		ModelInterpreterTestUtils.serialise("/acnet2/MediumNet.net.nml", "network_ACnet2", new NeuroMLModelInterpreterService());
		ModelInterpreterTestUtils.serialise("/acnet2/MediumNet.net.nml", null, new NeuroMLModelInterpreterService());
//		ModelInterpreterTestUtils.serialise("/acnet2/bask.cell.nml", "bask", new NeuroMLModelInterpreterService());
//		ModelInterpreterTestUtils.serialise("/acnet2/bask.cell.nml", null, new NeuroMLModelInterpreterService());
	}

	/**
	 * Test method for {@link org.geppetto.model.neuroml.services.LemsMLModelInterpreterService#readModel(java.net.URL)}.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testReadModelPVDR() throws Exception
	{
//		ModelInterpreterTestUtils.serialise("/pvdr/PVDR.nml", "PVDR", new NeuroMLModelInterpreterService());
//		ModelInterpreterTestUtils.serialise("/pvdr/PVDR.nml", null, new NeuroMLModelInterpreterService());
	}

	@Test
	public void testCA1() throws Exception
	{
		//ModelInterpreterTestUtils.serialise("/ca1/BigCA1.net.nml", "CA1", new NeuroMLModelInterpreterService());
//		ModelInterpreterTestUtils.serialise("/ca1/BigCA1.net.nml", null, new NeuroMLModelInterpreterService());
	}


	@AfterClass
	public static void doYourOneTimeTeardown()
	{
		File acnet2 = new File("./src/test/resources/MediumNet.net.nml.xmi");
		acnet2.delete();
//		File bask = new File("./src/test/resources/bask.cell.nml.xmi");
//		bask.delete();

//		File pvdr = new File("./src/test/resources/PVDR.nml.xmi");
//		pvdr.delete();

//		File ca1 = new File("./src/test/resources/BigCA1.net.nml.xmi");
//		ca1.delete();
	}

}
