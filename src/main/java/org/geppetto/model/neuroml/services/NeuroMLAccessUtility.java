/**
 * 
 */
package org.geppetto.model.neuroml.services;

import java.util.Map;

import org.geppetto.core.model.ModelWrapper;
import org.neuroml.model.AdExIaFCell;
import org.neuroml.model.BaseCell;
import org.neuroml.model.Cell;
import org.neuroml.model.IafCell;
import org.neuroml.model.NeuroMLDocument;

/**
 * @author matteocantarelli
 * 
 */
public class NeuroMLAccessUtility
{

	public static BaseCell discoverAllCells(NeuroMLDocument doc, ModelWrapper model)
	{
		Map<String, BaseCell> discoveredComponents = (Map<String, BaseCell>) model.getModel(NeuroMLModelInterpreterService.DISCOVERED_COMPONENTS);

		for(AdExIaFCell c : doc.getAdExIaFCell())
		{
			discoveredComponents.put(c.getId(), c);
		}
		for(IafCell c : doc.getIafCell())
		{
			discoveredComponents.put(c.getId(), c);
		}
		for(Cell c : doc.getCell())
		{
			discoveredComponents.put(c.getId(), c);
		}
		return null;
	}

	public static BaseCell getCellById(String componentId, NeuroMLDocument doc, ModelWrapper model)
	{
		Map<String, BaseCell> discoveredComponents = (Map<String, BaseCell>) model.getModel(NeuroMLModelInterpreterService.DISCOVERED_COMPONENTS);
		if(discoveredComponents.containsKey(componentId))
		{
			return discoveredComponents.get(componentId);
		}

		for(AdExIaFCell c : doc.getAdExIaFCell())
		{
			if(c.getId().equals(componentId))
			{
				discoveredComponents.put(c.getId(), c);
				return c;
			}
		}
		for(IafCell c : doc.getIafCell())
		{
			if(c.getId().equals(componentId))
			{
				discoveredComponents.put(c.getId(), c);
				return c;
			}
		}
		for(Cell c : doc.getCell())
		{
			if(c.getId().equals(componentId))
			{
				discoveredComponents.put(c.getId(), c);
				return c;
			}
		}
		return null;
	}
}
