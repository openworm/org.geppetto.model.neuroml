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
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.emfjson.jackson.resource.JsonResourceFactory;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.model.GeppettoFactory;
import org.geppetto.model.GeppettoLibrary;
import org.geppetto.model.GeppettoModel;
import org.geppetto.model.GeppettoPackage;
import org.geppetto.model.impl.GeppettoFactoryImpl;
import org.geppetto.model.neuroml.services.NeuroMLModelInterpreterService;
import org.geppetto.model.neuroml.utils.OptimizedLEMSReader;
import org.geppetto.model.types.Type;
import org.junit.Test;
import org.lemsml.jlems.api.LEMSDocumentReader;
import org.lemsml.jlems.api.interfaces.ILEMSDocument;
import org.lemsml.jlems.api.interfaces.ILEMSDocumentReader;
import org.lemsml.jlems.core.sim.ContentError;
import org.lemsml.jlems.core.type.Component;
import org.lemsml.jlems.core.type.Exposure;
import org.lemsml.jlems.core.type.Lems;
import org.lemsml.jlems.core.type.ParamValue;
import org.neuroml.export.utils.Utils;
import org.neuroml.model.util.NeuroMLException;

/**
 * @author matteocantarelli
 * 
 */
public class JustTest
{
	@Test
	public void test2() throws Exception
	{
		GeppettoFactory geppettoFactory = GeppettoFactoryImpl.eINSTANCE;
		GeppettoLibrary gl = geppettoFactory.createGeppettoLibrary();
		
		NeuroMLModelInterpreterService modelInterpreter = new NeuroMLModelInterpreterService();
		URL url = this.getClass().getResource("/acnet2/bask.cell.nml");
		Type type = modelInterpreter.importType(url, "bask", gl);
		System.out.println("Final Node");
		System.out.println(type);
		
		
		// Initialize the factory and the resource set
		GeppettoPackage.eINSTANCE.eClass();
		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		Map<String, Object> m = reg.getExtensionToFactoryMap();
		m.put("json", new JsonResourceFactory()); // sets the factory for the JSON type
		ResourceSet resSet = new ResourceSetImpl();
		
//		GeppettoFactory gp = GeppettoFactoryImpl.eINSTANCE;
//		GeppettoModel geppettoModel = gp.createGeppettoModel();
//		geppettoModel.getLibraries().add(gl);
		
		// How to save to JSON
		Resource jsonResource = resSet.createResource(URI.createURI("./src/test/resources/test.json"));
		jsonResource.getContents().add(type);
		jsonResource.save(null);
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
		System.out.println("taka");
		URL url = this.getClass().getResource("/acnet2/bask.cell.nml");

		OptimizedLEMSReader reader = new OptimizedLEMSReader(new ArrayList<URL>());
		int index = url.toString().lastIndexOf('/');
		String urlBase = url.toString().substring(0, index + 1);
		reader.read(url, urlBase, OptimizedLEMSReader.NMLDOCTYPE.NEUROML); // expand it to have all the inclusions

		/*
		 * LEMS
		 */
		ILEMSDocumentReader lemsReader = new LEMSDocumentReader();
		ILEMSDocument lemsDocument = lemsReader.readModel(reader.getLEMSString());

		Lems lems = ((Lems) lemsDocument);
		lems.setResolveModeLoose();
		lems.deduplicate();
		lems.resolve();
		lems.evaluateStatic();

		for(Component component : lems.getComponents())
		{
			extractInfoFromComponent(component);
		}
	}
	
	private void extractInfoFromComponent(Component component) throws Exception{
		
		if (!component.getDeclaredType().equals("morphology")){
			System.out.println("New Component Type");
			System.out.println("Name " + component.getDeclaredType());
			
			
			if (component.getID() != null)
				System.out.println("Id " + component.getID());
			for(ParamValue pv : component.getParamValues())
			{
				if(component.hasAttribute(pv.getName()))
				{
					String orig = component.getStringValue(pv.getName());
					System.out.println("Parameter Specification Node");
					System.out.println(pv.getName() + ":" + orig);
				}
			}
			
			for (Entry<String, String> entry : component.getTextParamMap().entrySet()){
				System.out.println("TextMetadata Node");
				System.out.println(entry.getKey() + ":" + entry.getValue());
			}
			
			for (Entry<String, Component> entry : component.getRefComponents().entrySet()){
				System.out.println("Component Node");
				System.out.println(entry.getKey() + ":" + entry.getValue());
			}
			
			//Simulation Tree (Variable Node)
			for (Exposure exposure : component.getComponentType().getExposures()){
				System.out.println("Exposure");
				System.out.println(exposure.getName());
				String unit = Utils.getSIUnitInNeuroML(exposure.getDimension()).getSymbol();
				System.out.println("Units " + unit);
				if(unit.equals("none"))
				{
					unit = "";
				}
			}
			
			for (Component componentChild : component.getAllChildren()){
				extractInfoFromComponent(componentChild);
			}
		}
	}

}
