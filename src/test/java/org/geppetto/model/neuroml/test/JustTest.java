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

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.emfjson.jackson.resource.JsonResourceFactory;
import org.geppetto.core.manager.SharedLibraryManager;
import org.geppetto.core.model.GeppettoCommonLibraryAccess;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.model.GeppettoFactory;
import org.geppetto.model.GeppettoLibrary;
import org.geppetto.model.GeppettoModel;
import org.geppetto.model.GeppettoPackage;
import org.geppetto.model.impl.GeppettoFactoryImpl;
import org.geppetto.model.neuroml.services.NeuroMLModelInterpreterService;
import org.geppetto.model.types.Type;
import org.junit.Test;
import org.lemsml.jlems.core.sim.ContentError;
import org.neuroml.model.util.NeuroMLException;

/**
 * @author matteocantarelli
 * 
 */
public class JustTest
{

	public void serialise(String modelPath, String outputPath, String typeName, boolean allTypes) throws Exception
	{
		GeppettoFactory geppettoFactory = GeppettoFactoryImpl.eINSTANCE;
		GeppettoLibrary gl = geppettoFactory.createGeppettoLibrary();
		GeppettoModel gm = geppettoFactory.createGeppettoModel();
		gm.getLibraries().add(gl);
		
		NeuroMLModelInterpreterService modelInterpreter = new NeuroMLModelInterpreterService();
		URL url = this.getClass().getResource(modelPath);
		
		gm.getLibraries().add(EcoreUtil.copy(SharedLibraryManager.getSharedCommonLibrary()));
		GeppettoCommonLibraryAccess commonLibraryAccess = new GeppettoCommonLibraryAccess(gm);
		
		Type type = modelInterpreter.importType(url, typeName, gl, commonLibraryAccess);

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
	}

	@Test
	public void test2() throws Exception
	{
		serialise("/acnet2/MediumNet.net.nml", "./src/test/resources/test2AllTypes.xmi", "network_ACnet2", true);
		serialise("/acnet2/MediumNet.net.nml", "./src/test/resources/test2SingleType.json", "network_ACnet2", false);
		serialise("/acnet2/MediumNet.net.nml", "./src/test/resources/test2SingleTypeWithoutTypeName.json", null, false);
	}

	/**
	 * Test method for {@link org.geppetto.model.neuroml.services.LemsMLModelInterpreterService#readModel(java.net.URL)}.
	 * 
	 * @throws ModelInterpreterException
	 * @throws IOException
	 * @throws ContentError
	 * @throws NeuroMLException
	 */
	@Test
	public void test1() throws Exception
	{
		serialise("/acnet2/bask.cell.nml", "./src/test/resources/testAllTypes.xmi", "bask", true);
		serialise("/acnet2/bask.cell.nml", "./src/test/resources/testAllTypes.json", "bask", true);
		serialise("/acnet2/bask.cell.nml", "./src/test/resources/testSingleType.json", "bask", false);
		// AQP Commented until we decide what to return when if it is not a network 
		//serialiseAsJSON("/acnet2/bask.cell.nml", "./src/test/resources/test.json", null, false);
	}

}

