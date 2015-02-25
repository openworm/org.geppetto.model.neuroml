/**
 * 
 */
package org.geppetto.model.neuroml.utils;

import java.util.HashMap;

import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.model.neuroml.services.Format;
import org.lemsml.jlems.core.sim.ContentError;
import org.lemsml.jlems.core.type.Lems;

/**
 * @author Adrian Quintana (adrian.perez@ucl.ac.uk)
 * 
 */
public class LEMSAccessUtility
{
	public static final String DISCOVERED_LEMS_COMPONENTS = "discoveredLEMSComponents";

	public LEMSAccessUtility() {
		super();
	}

	/**
	 * @param p
	 * @param neuroml
	 * @param url
	 * @return
	 * @throws ModelInterpreterException 
	 * @throws ContentError 
	 */
	public Object getComponent(String componentId, ModelWrapper model) throws ModelInterpreterException
	{
		//Check if we have already discovered this component
		Object component = this.getComponentFromCache(componentId, model);
		
		// let's first check if the cell is of a predefined neuroml type
		if(component == null){
			try {
				component = getComponentById(componentId, model);
			} catch (ContentError e1) {
				//throw new ModelInterpreterException("Can't find the componet " + componentId + ". Exception throw: " + e1.toString());
				component = null;
			}
		}

		if(component == null)
		{
			// sorry no luck!
			//throw new ModelInterpreterException("Can't find the componet " + componentId);
			component = null;
		}
		return component;
	}

	public Object getComponentFromCache(String componentId, ModelWrapper model){
		HashMap<String, Object> _discoveredLEMSComponents = ((HashMap<String, Object>)((ModelWrapper) model).getModel(DISCOVERED_LEMS_COMPONENTS));
		
		//TODO Can we have the same id for two different components 
		if(_discoveredLEMSComponents.containsKey(componentId))
		{
			return _discoveredLEMSComponents.get(componentId);
		}
		
		return null;
	}
	
	public Object getComponentById(String componentId, ModelWrapper model) throws ContentError
	{
		//Look for the model in the document
		Lems lems = (Lems) ((ModelWrapper) model).getModel(Format.LEMS_MODELFORMAT);
//		System.out.println(lems.getComponents());
		Object component = lems.getComponentTypeByName(componentId);
		//Store the component in the cache
		((HashMap<String, Object>)((ModelWrapper) model).getModel(DISCOVERED_LEMS_COMPONENTS)).put(componentId, component);
		
		return component;
	}
	
	
}
