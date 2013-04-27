/**
 * 
 */
package org.geppetto.model.neuroml.services;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import org.geppetto.core.model.IModel;
import org.geppetto.core.model.IModelInterpreter;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.visualisation.model.Scene;
import org.lemsml.jlems.core.api.LEMSDocumentReader;
import org.lemsml.jlems.core.api.interfaces.ILEMSDocument;
import org.lemsml.jlems.core.api.interfaces.ILEMSDocumentReader;
import org.lemsml.jlems.core.sim.ContentError;
import org.neuroml.model.util.NeuroMLConverter;
import org.springframework.stereotype.Service;

/**
 * @author matteocantarelli
 * 
 */
@Service
public class NeuroMLModelInterpreterService implements IModelInterpreter
{

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openworm.simulationengine.core.model.IModelProvider#readModel(java .lang.String)
	 */
	public List<IModel> readModel(URL url)  throws ModelInterpreterException 
	{
		List<IModel> model = new ArrayList<IModel>();

		try
		{
			String neuroMLString = new Scanner(url.openStream(), "UTF-8").useDelimiter("\\A").next();

			String lemsString = NeuroMLConverter.convertNeuroML2ToLems(neuroMLString);

			ILEMSDocumentReader lemsReader = new LEMSDocumentReader();
			ILEMSDocument document = lemsReader.readModel(lemsString);

			ModelWrapper lemsWrapper = new ModelWrapper(UUID.randomUUID().toString(), document);
			model.add(lemsWrapper);
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
	 * @see org.openworm.simulationengine.core.model.IModelInterpreter#getSceneFromModel(java.util.List)
	 */
	public Scene getSceneFromModel(List<IModel> model)
	{
		return null;
	}
}
