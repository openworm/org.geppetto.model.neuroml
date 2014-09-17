/**
 * 
 */
package org.geppetto.model.neuroml.services;

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

	public static BaseCell getCellById(String cellId, NeuroMLDocument doc)
	{
		for (AdExIaFCell c:doc.getAdExIaFCell())
		{
			if(c.getId().equals(cellId))
			{
				return c;
			}
		}
		for (IafCell c:doc.getIafCell())
		{
			if(c.getId().equals(cellId))
			{
				return c;
			}
		}
		for (Cell c:doc.getCell())
		{
			if(c.getId().equals(cellId))
			{
				return c;
			}
		}
		return null;
	}
}
