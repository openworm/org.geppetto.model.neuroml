/**
 * 
 */
package org.geppetto.model.neuroml.utils;

import java.util.HashMap;
import java.util.List;

import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.services.registry.ServicesRegistry;
import org.geppetto.model.DomainModel;
import org.lemsml.jlems.core.sim.ContentError;
import org.lemsml.jlems.core.type.Component;
import org.lemsml.jlems.core.type.Lems;

/**
 * @author Adrian Quintana (adrian.perez@ucl.ac.uk)
 * 
 */
public class LEMSAccessUtility
{
	public static final String DISCOVERED_LEMS_COMPONENTS = "discoveredLEMSComponents";

	public LEMSAccessUtility()
	{
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
	public Object getComponent(String componentId, DomainModel model) throws ModelInterpreterException
	{
		// Check if we have already discovered this component
		Object component = this.getComponentFromCache(componentId, model);

		// let's first check if the cell is of a predefined neuroml type
		if(component == null)
		{
			try
			{
				component = getComponentById(componentId, model);
			}
			catch(ContentError e1)
			{
				// throw new ModelInterpreterException("Can't find the componet " + componentId + ". Exception throw: " + e1.toString());
				component = null;
			}
		}

		if(component == null)
		{
			// sorry no luck!
			// throw new ModelInterpreterException("Can't find the componet " + componentId);
			component = null;
		}
		return component;
	}

	public Object getComponentFromCache(String componentId, DomainModel model)
	{
		HashMap<String, Object> _discoveredLEMSComponents = ((HashMap<String, Object>) ((ModelWrapper) model).getModel(DISCOVERED_LEMS_COMPONENTS));

		// TODO Can we have the same id for two different components
		if(_discoveredLEMSComponents.containsKey(componentId))
		{
			return _discoveredLEMSComponents.get(componentId);
		}

		return null;
	}

	public Object getComponentById(String componentId, DomainModel model) throws ContentError
	{
		// Look for the model in the document
		Lems lems = (Lems) ((ModelWrapper) model).getModel(ServicesRegistry.getModelFormat("LEMS"));
		// System.out.println(lems.getComponents());
		Object component = lems.getComponentTypeByName(componentId);
		// Store the component in the cache
		((HashMap<String, Object>) ((ModelWrapper) model).getModel(DISCOVERED_LEMS_COMPONENTS)).put(componentId, component);

		return component;
	}

	/**
	 * This method looks for a component given the id at any point in the LEMS hierarchy
	 * 
	 * @param components
	 *            a list of components where to start the search from
	 * @param id
	 *            the id of the component we are looking for
	 * @return the component if it was found, null otherwise
	 */
	public static Component findLEMSComponent(List<Component> components, String id)
	{
		for(Component c : components)
		{
			if(c.getID() != null)
			{
				if(c.getID().equals(id))
				{
					return c;
				}
			}
		}
		for(Component c : components)
		{
			Component found = findLEMSComponent(c.getComponents().getContents(), id);
			if(found != null)
			{
				return found;
			}
		}
		return null;
	}
	
	public static String getSimulationTreePathType(Component targetComponent) throws ModelInterpreterException
	{
		if(targetComponent.getDeclaredType().equals("network"))
		{
			// It is a network
			for(Component componentChild : targetComponent.getAllChildren())
			{
				if(componentChild.getDeclaredType().equals("population"))
				{
					//population = componentChild;
					if(componentChild.getComponentType().getName().equals("populationList"))
					{
						return "populationList";
					}
				}
			}
			return "population";

		}
		else
		{
			// It is a cell
			return "cell";
		}

	}

}
