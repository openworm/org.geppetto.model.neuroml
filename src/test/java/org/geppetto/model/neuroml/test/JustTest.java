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
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.emfjson.jackson.resource.JsonResourceFactory;
import org.geppetto.core.manager.SharedLibraryManager;
import org.geppetto.core.model.GeppettoModelAccess;
import org.geppetto.core.model.IModelInterpreter;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.model.GeppettoFactory;
import org.geppetto.model.GeppettoLibrary;
import org.geppetto.model.GeppettoModel;
import org.geppetto.model.GeppettoPackage;
import org.geppetto.model.neuroml.services.LEMSConversionService;
import org.geppetto.model.neuroml.services.LEMSModelInterpreterService;
import org.geppetto.model.neuroml.services.NeuroMLModelInterpreterService;
import org.geppetto.model.types.Type;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lemsml.jlems.core.sim.ContentError;
import org.neuroml.model.util.NeuroMLException;

/**
 * @author matteocantarelli & friends
 * 
 */
public class JustTest
{
	
	private static Log _logger = LogFactory.getLog(JustTest.class);

	@BeforeClass
	public static void initializeServiceRegistry() throws Exception
	{
		LEMSConversionService lemsConversionService = new LEMSConversionService();
		lemsConversionService.registerGeppettoService();
		
		LEMSModelInterpreterService lemsModelInterpreter = new LEMSModelInterpreterService();
		lemsModelInterpreter.registerGeppettoService();
		
		NeuroMLModelInterpreterService neuromlModelInterpreter = new NeuroMLModelInterpreterService();
		neuromlModelInterpreter.registerGeppettoService();
	}
	
	public void serialise(String modelPath, String outputPath, String typeName, boolean allTypes, IModelInterpreter modelInterpreter) throws Exception
	{
		GeppettoFactory geppettoFactory = GeppettoFactory.eINSTANCE;
		GeppettoLibrary gl = geppettoFactory.createGeppettoLibrary();
		GeppettoModel gm = geppettoFactory.createGeppettoModel();
		gm.getLibraries().add(gl);
		
		URL url = this.getClass().getResource(modelPath);
		
		gm.getLibraries().add(EcoreUtil.copy(SharedLibraryManager.getSharedCommonLibrary()));
		GeppettoModelAccess commonLibraryAccess = new GeppettoModelAccess(gm);
		
		Type type = modelInterpreter.importType(url, typeName, gl, commonLibraryAccess);

		long startTime = System.currentTimeMillis();
		
		// Initialize the factory and the resource set
		GeppettoPackage.eINSTANCE.eClass();
		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		Map<String, Object> m = reg.getExtensionToFactoryMap();
		m.put("json", new JsonResourceFactory()); // sets the factory for the JSON type
		m.put("xmi", new XMIResourceFactoryImpl()); // sets the factory for the XMI typ
		ResourceSet resSet = new ResourceSetImpl();

		// How to save to JSON
		Resource resource = resSet.createResource(URI.createURI(outputPath));
		if (allTypes)
			resource.getContents().add(gm);
		else
			resource.getContents().add(type);
		
		resource.save(null);
		
		long endTime = System.currentTimeMillis();
		_logger.info("Serialising " + (endTime - startTime) + " milliseconds for url " + url + " and  typename " + typeName);
	}
	
	@Test
	public void testAcnet() throws Exception
	{
		serialise("/acnet2/MediumNet.net.nml", "./src/test/resources/acnet2.xmi", "network_ACnet2", true, new NeuroMLModelInterpreterService());
		serialise("/acnet2/MediumNet.net.nml", "./src/test/resources/acnet2NoTarget.xmi", null, true, new NeuroMLModelInterpreterService());
	}
	
	@Test
	public void testPurkinje() throws Exception
	{
		serialise("/purkinje/purk.nml", "./src/test/resources/purkinje.xmi", "purk2", true, new NeuroMLModelInterpreterService());
		serialise("/purkinje/purk.nml", "./src/test/resources/purkinjeNotarget.xmi", null, true, new NeuroMLModelInterpreterService());
	}

	@Test
	public void testBask() throws Exception
	{
		serialise("/acnet2/bask.cell.nml", "./src/test/resources/bask.xmi", "bask", true, new NeuroMLModelInterpreterService());
		serialise("/acnet2/bask.cell.nml", "./src/test/resources/baskNoTarget.xmi", null, true, new NeuroMLModelInterpreterService());
		serialise("/acnet2/bask.cell.nml", "./src/test/resources/bask.json", "bask", true, new NeuroMLModelInterpreterService());
	}

	@Test
	public void testHHCell() throws Exception
	{
		serialise("/hhcell/LEMS_NML2_Ex5_DetCell.xml", "./src/test/resources/hhcell.xmi", "hhcell", true, new LEMSModelInterpreterService());
		serialise("/hhcell/LEMS_NML2_Ex5_DetCell.xml", "./src/test/resources/hhcellNoTarget.xmi", null, true, new LEMSModelInterpreterService());
		serialise("/hhcell/LEMS_NML2_Ex5_DetCell.xml", "./src/test/resources/hhnet1.xmi", "net1", true, new LEMSModelInterpreterService());
	}

	@Test
	public void testPVDR() throws Exception
	{
		serialise("/pvdr/PVDR.nml", "./src/test/resources/pvdr.xmi", "PVDR", true, new NeuroMLModelInterpreterService());
		serialise("/pvdr/PVDR.nml", "./src/test/resources/pvdrNoTarget.xmi", null, true, new NeuroMLModelInterpreterService());
	}
	
	@AfterClass
	public static void doYourOneTimeTeardown()
	{
		File acnet2 = new File("./src/test/resources/acnet2.xmi");
		acnet2.delete();
		File acnet2NoTarget = new File("./src/test/resources/acnet2NoTarget.xmi");
		acnet2NoTarget.delete();
		
		File purkinje = new File("./src/test/resources/purkinje.xmi");
		purkinje.delete();
		File purkinjeNotarget = new File("./src/test/resources/purkinjeNotarget.xmi");
		purkinjeNotarget.delete();
		
		File bask = new File("./src/test/resources/bask.xmi");
		bask.delete();
		File baskNoTarget = new File("./src/test/resources/baskNoTarget.xmi");
		baskNoTarget.delete();
		File baskjson = new File("./src/test/resources/bask.json");
		baskjson.delete();
		
		File hhcell = new File("./src/test/resources/hhcell.xmi");
		hhcell.delete();
		File hhcellNoTarget = new File("./src/test/resources/hhcellNoTarget.xmi");
		hhcellNoTarget.delete();
		File hhnet1 = new File("./src/test/resources/hhnet1.xmi");
		hhnet1.delete();
		
		File pvdr = new File("./src/test/resources/pvdr.xmi");
		pvdr.delete();
		File pvdrNoTarget = new File("./src/test/resources/pvdrNoTarget.xmi");
		pvdrNoTarget.delete();
	}
}

