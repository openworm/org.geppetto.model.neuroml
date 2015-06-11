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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.model.runtime.AspectNode;
import org.geppetto.core.model.runtime.EntityNode;
import org.geppetto.core.model.runtime.ParameterSpecificationNode;
import org.geppetto.core.model.runtime.RuntimeTreeRoot;
import org.geppetto.core.services.registry.ServicesRegistry;
import org.geppetto.model.neuroml.services.LEMSModelInterpreterService;
import org.geppetto.model.neuroml.services.NeuroMLModelInterpreterService;
import org.geppetto.model.neuroml.utils.LEMSAccessUtility;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lemsml.jlems.core.sim.ContentError;
import org.lemsml.jlems.core.type.Component;
import org.lemsml.jlems.core.type.Lems;
import org.lemsml.jlems.core.type.ParamValue;
import org.neuroml.model.ChannelDensity;

/**
 * @author matteocantarelli
 * 
 */
public class NeuroMLModelInterpreterServiceTest
{

	//FIXME: We have to use OSGI text or spring app context initialization
	@BeforeClass
	public static void initializeServiceRegistry() throws Exception
	{
		LEMSModelInterpreterService lemsModelInterpreter = new LEMSModelInterpreterService();
		lemsModelInterpreter.registerGeppettoService();
		
		NeuroMLModelInterpreterService neuromlModelInterpreter = new NeuroMLModelInterpreterService();
		neuromlModelInterpreter.registerGeppettoService();
	}

	/**
	 * Test method for {@link org.geppetto.model.neuroml.services.LemsMLModelInterpreterService#readModel(java.net.URL)}.
	 * 
	 * @throws ModelInterpreterException
	 */
	@Test
	public void testReadModel() throws ModelInterpreterException
	{
		NeuroMLModelInterpreterService modelInterpreter = new NeuroMLModelInterpreterService();
		URL url = this.getClass().getResource("/NML2_FullCell.nml");
		ModelWrapper model = (ModelWrapper) modelInterpreter.readModel(url, null, "");
		assertNotNull(model);
		assertNotNull(model.getModel("url"));
		assertNotNull(model.getModel(ServicesRegistry.getModelFormat("LEMS")));
		assertNotNull(model.getModel(ServicesRegistry.getModelFormat("NEUROML")));
	}

	/**
	 * Test method for {@link org.geppetto.model.neuroml.services.LemsMLModelInterpreterService#readModel(java.net.URL)}.
	 * 
	 * @throws ModelInterpreterException
	 */
	@Test
	public void testSetParameters() throws ModelInterpreterException
	{
		NeuroMLModelInterpreterService modelInterpreter = new NeuroMLModelInterpreterService();
		URL url = this.getClass().getResource("/acnet2/bask.cell.nml");
		ModelWrapper model = (ModelWrapper) modelInterpreter.readModel(url, null, "");
		assertNotNull(model);
		assertNotNull(model.getModel("url"));
		assertNotNull(model.getModel(ServicesRegistry.getModelFormat("LEMS")));
		assertNotNull(model.getModel(ServicesRegistry.getModelFormat("NEUROML")));
		RuntimeTreeRoot root = new RuntimeTreeRoot("scene");
		EntityNode entity = new EntityNode("bask");
		AspectNode aspectNode = new AspectNode("electrical");
		entity.addChild(aspectNode);
		root.addChild(entity);
		aspectNode.setModel(model);
		modelInterpreter.populateRuntimeTree(aspectNode);
		modelInterpreter.populateModelTree(aspectNode);
		String parameterNodeInstancePath = "bask.electrical.ModelTree.Cell.biophys.MembraneProperties.Kdr_bask_soma_group.PassiveConductanceDensity";
		
		TestParametersVisitor visitor = new TestParametersVisitor();
		aspectNode.apply(visitor);
		ParameterSpecificationNode node = visitor.getParametersMap().get(parameterNodeInstancePath);
		ChannelDensity density = (ChannelDensity) modelInterpreter.getObjectsMap().get(node);
		assertEquals("50.0", node.getValue().getValue().toString());
		assertEquals("50.0 mS_per_cm2", density.getCondDensity());
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(parameterNodeInstancePath, "10");
		modelInterpreter.setParameter(parameters);
		
		aspectNode.apply(visitor);
		assertEquals("10.0", node.getValue().getValue().getStringValue());
		assertEquals("10.0", density.getCondDensity());
		
		Lems lems = (Lems) model.getModel(ServicesRegistry.getModelFormat("LEMS"));
		Component comp = LEMSAccessUtility.findLEMSComponent(lems.getComponents().getContents(), density.getId());
		//unspeakable things happening, going from a method name to parameter name
		Method method=(Method)modelInterpreter.getMethodsMap().get(node);
		String paramName = Character.toLowerCase(method.getName().charAt(3))+method.getName().substring(4);
		ParamValue lemsParam;
		try
		{
			lemsParam = comp.getParamValue(paramName);
			assertEquals(100.0d, lemsParam.getDoubleValue(),0d);
		}
		catch(ContentError e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

	}

}
