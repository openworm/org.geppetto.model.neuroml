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

import org.lemsml.jlems.expression.ParseError;
import org.lemsml.jlems.sim.ContentError;
import org.lemsml.jlems.sim.LemsProcess;
import org.lemsml.jlems.sim.ParseException;
import org.lemsml.jlems.type.BuildException;
import org.lemsml.jlems.xml.XMLException;
import org.neuroml.model.util.NeuroMLConverter;
import org.openworm.simulationengine.core.model.IModel;
import org.openworm.simulationengine.core.model.IModelInterpreter;
import org.openworm.simulationengine.core.model.ModelInterpreterException;
import org.openworm.simulationengine.core.model.ModelWrapper;
import org.openworm.simulationengine.core.visualisation.model.Scene;
import org.springframework.stereotype.Service;

/**
 * @author matteocantarelli
 * 
 */
@Service
public class LemsMLModelInterpreterService implements IModelInterpreter
{

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openworm.simulationengine.core.model.IModelProvider#readModel(java .lang.String)
	 */
	public List<IModel> readModel(URL url) throws ModelInterpreterException 
	{

		List<IModel> model = new ArrayList<IModel>();
		try
		{
			String neuroMLString = new Scanner(url.openStream(), "UTF-8").useDelimiter("\\A").next();
			String lemsString = NeuroMLConverter.convertNeuroML2ToLems(neuroMLString);
			LemsProcess lemsProcess = new LemsProcess(lemsString);
			URL resourceURL = this.getClass().getResource("/");
			lemsProcess.readModel();
			ModelWrapper lemsWrapper = new ModelWrapper(UUID.randomUUID().toString(), lemsProcess.getLems());
			model.add(lemsWrapper);
		}
		catch (IOException e)
		{
			throw new ModelInterpreterException(e);
		}
		catch (ContentError e)
		{
			throw new ModelInterpreterException(e);
		}
		catch (ParseError e)
		{
			throw new ModelInterpreterException(e);
		}
		catch (ParseException e)
		{
			throw new ModelInterpreterException(e);
		}
		catch (BuildException e)
		{
			throw new ModelInterpreterException(e);
		}
		catch (XMLException e)
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
