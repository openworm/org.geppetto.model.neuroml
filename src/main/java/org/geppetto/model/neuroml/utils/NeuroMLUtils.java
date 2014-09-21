/**
 * 
 */
package org.geppetto.model.neuroml.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;

import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.ModelWrapper;
import org.neuroml.model.AdExIaFCell;
import org.neuroml.model.Base;
import org.neuroml.model.BaseCell;
import org.neuroml.model.Cell;
import org.neuroml.model.IafCell;
import org.neuroml.model.IonChannel;
import org.neuroml.model.IonChannelHH;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.Population;
import org.neuroml.model.Standalone;
import org.neuroml.model.util.NeuroMLConverter;

/**
 * @author Adrian Quintana (adrian.perez@ucl.ac.uk)
 * 
 */
public class NeuroMLUtils
{

	private Map<String, Base> discoveredComponents = new HashMap<String, Base>();
	
	private static final int MAX_ATTEMPTS = 3;
	
	/**
	 * @param p
	 * @param neuroml
	 * @param url
	 * @return
	 * @throws ModelInterpreterException 
	 */
	public Base getComponent(String componentId, NeuroMLDocument neuroMLDocument, URL url, ResourcesSuffix componentType) throws ModelInterpreterException
	{
		// let's first check if the cell is of a predefined neuroml type
		Base component = getComponentById(componentId, neuroMLDocument, componentType);

		if(component == null)
		{
			try
			{
				// otherwise let's check if it's defined in the same folder as the current component
				component = retrieveNeuroMLComponent(componentId, url, componentType);
			}
			catch(MalformedURLException e)
			{
				throw new ModelInterpreterException(e);
			}
			catch(JAXBException e)
			{
				throw new ModelInterpreterException(e);
			}
		}
		if(component == null)
		{
			// sorry no luck!
			throw new ModelInterpreterException("Can't find the componet " + componentId);
		}
		return component;
	}
	
	/**
	 * @param componentId
	 * @param url
	 * @return
	 * @throws JAXBException
	 * @throws MalformedURLException
	 */
	public Base retrieveNeuroMLComponent(String componentId, URL url, ResourcesSuffix componentType) throws JAXBException, MalformedURLException
	{
		NeuroMLConverter neuromlConverter = new NeuroMLConverter();
		boolean attemptConnection = true;
		String baseURL = url.getFile();
		if(url.getFile().endsWith("nml"))
		{
			baseURL = baseURL.substring(0, baseURL.lastIndexOf("/") + 1);
		}
		int attempts = 0;
		NeuroMLDocument neuromlDocument = null;
		while(attemptConnection)
		{
			try
			{
				attemptConnection = false;
				attempts++;
				URL componentURL = new URL(url.getProtocol() + "://" + url.getAuthority() + baseURL + componentId + componentType.get() + ".nml");

				neuromlDocument = neuromlConverter.urlToNeuroML(componentURL);

				List<? extends Base> components = null;
				
				switch (componentType) {
				case ION_CHANNEL:
					components = neuromlDocument.getIonChannel();
				default:
					break;
				}
				
				if(components != null)
				{
					for(Base c : neuromlDocument.getIonChannel())
					{
						this.discoveredComponents.put(componentId, c);
						if(((Base)c).getId().equals(componentId))
						{
							return c;
						}
					}
				}
			}
			catch(MalformedURLException e)
			{
				throw e;
			}
			catch(UnmarshalException e)
			{
				if(e.getLinkedException() instanceof IOException)
				{
					if(attempts < MAX_ATTEMPTS)
					{
						attemptConnection = true;
					}
				}
			}
			catch(Exception e)
			{
				throw e;
			}
		}
		return null;
	}

	private Base getComponentById(String componentId, NeuroMLDocument doc, ResourcesSuffix componentType)
	{
		//TODO Can we have the same id for two different components 
		if(this.discoveredComponents.containsKey(componentId))
		{
			return this.discoveredComponents.get(componentId);
		}
		
		switch (componentType) {
		case ION_CHANNEL:
			for (IonChannel ionChannel : doc.getIonChannel()){
				if(ionChannel.getId().equals(componentId))
				{
					this.discoveredComponents.put(ionChannel.getId(), ionChannel);
					return ionChannel;
				}
			}
			for (IonChannelHH ionChannelHH : doc.getIonChannelHH()){
				if(ionChannelHH.getId().equals(componentId))
				{
					this.discoveredComponents.put(ionChannelHH.getId(), ionChannelHH);
					return ionChannelHH;
				}
			}
		case CELL:	
			
			for(AdExIaFCell c : doc.getAdExIaFCell())
			{
				if(c.getId().equals(componentId))
				{
					this.discoveredComponents.put(c.getId(), c);
					return c;
				}
			}
			for(IafCell c : doc.getIafCell())
			{
				if(c.getId().equals(componentId))
				{
					this.discoveredComponents.put(c.getId(), c);
					return c;
				}
			}
			for(Cell c : doc.getCell())
			{
				if(c.getId().equals(componentId))
				{
					this.discoveredComponents.put(c.getId(), c);
					return c;
				}
			}
			return null;

		case HHRATE:
			doc.getHHCondExp();
			
		default:
			break;
		}
		
		return null;

	}
}
