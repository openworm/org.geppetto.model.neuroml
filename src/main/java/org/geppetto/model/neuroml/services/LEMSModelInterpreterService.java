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
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import org.geppetto.core.model.IModel;
import org.geppetto.core.model.IModelInterpreter;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.model.simulation.Aspect;
import org.geppetto.core.model.state.StateTreeRoot;
import org.geppetto.core.visualisation.model.CEntity;
import org.lemsml.jlems.core.api.LEMSDocumentReader;
import org.lemsml.jlems.core.api.interfaces.ILEMSDocument;
import org.lemsml.jlems.core.api.interfaces.ILEMSDocumentReader;
import org.lemsml.jlems.core.sim.ContentError;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.util.NeuroMLConverter;
import org.springframework.stereotype.Service;

/**
 * @author matteocantarelli
 * 
 */
@Service
public class LEMSModelInterpreterService implements IModelInterpreter
{

	private static final String LEMS_ID = "lems";
	private static final String NEUROML_ID = "neuroml";
	private static final String URL_ID = "url";
	private NeuroMLModelInterpreterService _neuroMLModelInterpreter = new NeuroMLModelInterpreterService();

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
			Scanner scanner=new Scanner(url.openStream(), "UTF-8");
			String lemsString = scanner.useDelimiter("\\A").next();
			scanner.close();
			ILEMSDocumentReader lemsReader = new LEMSDocumentReader();
			ILEMSDocument document = lemsReader.readModel(url);

			lemsWrapper = new ModelWrapper(UUID.randomUUID().toString());
			lemsWrapper.setInstancePath(instancePath);
			
			NeuroMLConverter neuromlConverter = new NeuroMLConverter();
			URL neuroMLURL = getNeuroMLURL(lemsString);
			if(neuroMLURL != null)
			{
				NeuroMLDocument neuroml = neuromlConverter.urlToNeuroML(neuroMLURL);
				// two different representation of the same file, one used to
				// simulate the other used to visualize
				lemsWrapper.wrapModel(NEUROML_ID, neuroml);
			}
			lemsWrapper.wrapModel(LEMS_ID, document);
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
		catch(Exception e)
		{
			throw new ModelInterpreterException(e);
		}
		return lemsWrapper;
	}

	/**
	 * @param lemsString
	 * @return
	 * @throws MalformedURLException
	 */
	private URL getNeuroMLURL(String lemsString) throws MalformedURLException
	{
		// FIXME This is a HACK. Importers from a LemsDocument will have to be
		// written, see issue on GitHub
		// https://github.com/NeuroML/org.neuroml.import/issues/1
		if(lemsString.contains(".nml"))
		{
			int end = lemsString.indexOf(".nml");
			end = lemsString.indexOf("\"", end);
			String header = lemsString.substring(0, end);
			int start = header.lastIndexOf("url=") + 5;
			String url = header.substring(start, end);
			return new URL(url);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.geppetto.core.model.IModelInterpreter#getVisualEntity(org.geppetto.core.model.IModel, org.geppetto.core.model.simulation.Aspect, org.geppetto.core.model.state.StateTreeRoot)
	 */
	@Override
	public CEntity getVisualEntity(IModel model, Aspect aspect, StateTreeRoot stateTree) throws ModelInterpreterException
	{
		return _neuroMLModelInterpreter.getVisualEntity(model, aspect, stateTree);
	}

}
