/*******************************************************************************
. * The MIT License (MIT)
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
import java.net.URL;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.beans.ModelInterpreterConfig;
import org.geppetto.core.model.IModel;
import org.geppetto.core.model.IModelInterpreter;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.model.runtime.AspectNode;
import org.geppetto.core.model.runtime.AspectSubTreeNode;
import org.geppetto.core.model.runtime.AspectSubTreeNode.AspectTreeType;
import org.lemsml.jlems.core.api.LEMSDocumentReader;
import org.lemsml.jlems.core.api.interfaces.ILEMSDocument;
import org.lemsml.jlems.core.api.interfaces.ILEMSDocumentReader;
import org.lemsml.jlems.core.sim.ContentError;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.util.NeuroMLConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author matteocantarelli
 * 
 */
@Service
public class
NeuroMLModelInterpreterService implements IModelInterpreter
{

	private static final String LEMS_ID = "lems";
	private static final String NEUROML_ID = "neuroml";
	private static final String URL_ID = "url";

	private static Log _logger = LogFactory.getLog(NeuroMLModelInterpreterService.class);

	//helper class to populate model tree
	private PopulateModelTree populateModelTree = new PopulateModelTree();
	
	@Autowired
	private ModelInterpreterConfig neuroMLModelInterpreterConfig;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openworm.simulationengine.core.model.IModelProvider#readModel(java .lang.String)
	 */
	public IModel readModel(URL url, List<URL> recordings, String instancePath) throws ModelInterpreterException
	{
		ModelWrapper lemsWrapper = null;
		try
		{
			Scanner scanner = new Scanner(url.openStream(), "UTF-8");
			String neuroMLString = scanner.useDelimiter("\\A").next();
			scanner.close();
			String lemsString = NeuroMLConverter.convertNeuroML2ToLems(neuroMLString);

			ILEMSDocumentReader lemsReader = new LEMSDocumentReader();
			ILEMSDocument document = lemsReader.readModel(lemsString);

			NeuroMLConverter neuromlConverter = new NeuroMLConverter();
			NeuroMLDocument neuroml = neuromlConverter.urlToNeuroML(url);

			lemsWrapper = new ModelWrapper(UUID.randomUUID().toString());
			lemsWrapper.setInstancePath(instancePath);
			// two different interpretations of the same file, one used to simulate the other used to visualize
			lemsWrapper.wrapModel(LEMS_ID, document);
			lemsWrapper.wrapModel(NEUROML_ID, neuroml);
			lemsWrapper.wrapModel(URL_ID, url);

		}
		catch(IOException e)
		{
			throw new ModelInterpreterException(e);
		}
		catch(ContentError e)
		{
			throw new ModelInterpreterException(e);
		}
		catch(JAXBException e)
		{
			throw new ModelInterpreterException(e);
		}
		catch(Exception e)
		{
			throw new ModelInterpreterException(e);
		}
		return lemsWrapper;
	}

	/*
	 * (non-Javadoc)
	 * @see org.geppetto.core.model.IModelInterpreter#populateModelTree(org.geppetto.core.model.runtime.AspectNode)
	 */
	@Override
	public boolean populateModelTree(AspectNode aspectNode) throws ModelInterpreterException {
		
		boolean modified = false;
		
		AspectSubTreeNode modelTree = (AspectSubTreeNode) aspectNode.getSubTree(AspectTreeType.MODEL_TREE);
		modelTree.setId(AspectTreeType.MODEL_TREE.toString());

		IModel model = aspectNode.getModel();		
		try
		{
			NeuroMLDocument neuroml = (NeuroMLDocument) ((ModelWrapper) model).getModel(NEUROML_ID);
			if(neuroml != null)
			{
				//Use local class to populate model tree
				modified = populateModelTree.populateModelTree(modelTree,neuroml);
				modelTree.setModified(modified);
			}

		}
		catch(Exception e)
		{
			throw new ModelInterpreterException(e);
		}
		return modified;
	}

	/*
	 * (non-Javadoc)
	 * @see org.geppetto.core.model.IModelInterpreter#populateRuntimeTree(org.geppetto.core.model.runtime.AspectNode)
	 */
	@Override
	public boolean populateRuntimeTree(AspectNode aspectNode) {		
		AspectSubTreeNode modelTree = (AspectSubTreeNode) aspectNode.getSubTree(AspectTreeType.MODEL_TREE);
		AspectSubTreeNode visualizationTree = (AspectSubTreeNode) aspectNode.getSubTree(AspectTreeType.VISUALIZATION_TREE);
		
		return true;
	}


	@Override
	public String getName()
	{
		return this.neuroMLModelInterpreterConfig.getModelInterpreterName();
	}

}
