/**
 * 
 */
package org.geppetto.model.neuroml.services;

import java.net.URL;
import java.util.List;

import org.openworm.simulationengine.core.model.IModel;
import org.openworm.simulationengine.core.model.IModelInterpreter;
import org.openworm.simulationengine.core.visualisation.model.Scene;
import org.springframework.stereotype.Service;

/**
 * @author matteocantarelli
 *
 */
@Service
public class NeuroMLModelInterpreterService implements IModelInterpreter {

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openworm.simulationengine.core.model.IModelProvider#readModel(java
	 * .lang.String)
	 */
	public List<IModel> readModel(URL url)
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.openworm.simulationengine.core.model.IModelInterpreter#getSceneFromModel(java.util.List)
	 */
	public Scene getSceneFromModel(List<IModel> model)
	{
		return null;
	}
}

