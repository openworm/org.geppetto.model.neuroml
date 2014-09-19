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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.geppetto.core.beans.ModelInterpreterConfig;
import org.geppetto.core.model.IModel;
import org.geppetto.core.model.IModelInterpreter;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.model.runtime.AspectNode;
import org.geppetto.core.model.runtime.EntityNode;
import org.geppetto.model.neuroml.utils.OptimizedLEMSReader;
import org.lemsml.jlems.core.api.LEMSDocumentReader;
import org.lemsml.jlems.core.api.interfaces.ILEMSDocument;
import org.lemsml.jlems.core.api.interfaces.ILEMSDocumentReader;
import org.lemsml.jlems.core.sim.ContentError;
import org.neuroml.model.BaseCell;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.util.NeuroMLConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author matteocantarelli
 * 
 */
@Service
public class LEMSModelInterpreterService implements IModelInterpreter
{

	private NeuroMLModelInterpreterService _neuroMLModelInterpreter = new NeuroMLModelInterpreterService();

	@Autowired
	private ModelInterpreterConfig jlemsModelInterpreterConfig;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openworm.simulationengine.core.model.IModelProvider#readModel(java .lang.String)
	 */
	public IModel readModel(URL url, List<URL> recordings, String instancePath) throws ModelInterpreterException
	{
		ModelWrapper model = new ModelWrapper(instancePath);
		try
		{
			OptimizedLEMSReader reader = new OptimizedLEMSReader();
			String lemsString = reader.read(url);

			ILEMSDocumentReader lemsReader = new LEMSDocumentReader();
			long start = System.currentTimeMillis();
			ILEMSDocument document = lemsReader.readModel(lemsString);
			System.out.println(System.currentTimeMillis() - start);
			model = new ModelWrapper(UUID.randomUUID().toString());
			model.setInstancePath(instancePath);

			// two different representation of the same file, one used to
			// simulate the other used to visualize
			if(reader.getNeuroMLs().size() == 1)
			{
				model.wrapModel(NeuroMLModelInterpreterService.NEUROML_ID, reader.getNeuroMLs().values().toArray()[0]);
			}
			else
			{
				model.wrapModel(NeuroMLModelInterpreterService.NEUROML_ID, reader.getNeuroMLs());
			}
			model.wrapModel(NeuroMLModelInterpreterService.SUBENTITIES_MAPPING_ID, new HashMap<BaseCell, EntityNode>());
			model.wrapModel(NeuroMLModelInterpreterService.DISCOVERED_COMPONENTS, new HashMap<String, BaseCell>());
			model.wrapModel(NeuroMLModelInterpreterService.LEMS_ID, document);
			model.wrapModel(NeuroMLModelInterpreterService.URL_ID, url);
		}
		catch(IOException e)
		{
			throw new ModelInterpreterException(e);
		}
		catch(ContentError e)
		{
			throw new ModelInterpreterException(e);
		}
		return model;
	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geppetto.core.model.IModelInterpreter#populateModelTree(org.geppetto.core.model.runtime.AspectNode)
	 */
	@Override
	public boolean populateModelTree(AspectNode aspectNode) throws ModelInterpreterException
	{
		return _neuroMLModelInterpreter.populateModelTree(aspectNode);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geppetto.core.model.IModelInterpreter#populateRuntimeTree(org.geppetto.core.model.runtime.AspectNode)
	 */
	@Override
	public boolean populateRuntimeTree(AspectNode aspectNode) throws ModelInterpreterException
	{
		return _neuroMLModelInterpreter.populateRuntimeTree(aspectNode);
	}

	@Override
	public String getName()
	{
		return this.jlemsModelInterpreterConfig.getModelInterpreterName();
	}

}
