/**
 * 
 */
package org.geppetto.model.neuroml.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;

import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.ModelWrapper;
import org.lemsml.jlems.core.sim.ContentError;
import org.lemsml.jlems.core.type.Lems;
import org.lemsml.jlems.core.type.Component;
import org.neuroml.model.AdExIaFCell;
import org.neuroml.model.Base;
import org.neuroml.model.Cell;
import org.neuroml.model.IafCell;
import org.neuroml.model.IonChannel;
import org.neuroml.model.IonChannelHH;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.util.NeuroMLConverter;

/**
 * @author Adrian Quintana (adrian.perez@ucl.ac.uk)
 * 
 */
public class NeuroMLAccessUtility
{

	public static final String LEMS_ID = "lems";
	public static final String NEUROML_ID = "neuroml";
	public static final String URL_ID = "url";
	public static final String SUBENTITIES_MAPPING_ID = "entitiesMapping";
	public static final String DISCOVERED_COMPONENTS = "discoveredComponents";
	public static final String LEMS_UTILS_ID = "lemsUtils";
	public static final String DISCOVERED_LEMS_COMPONENTS = "discoveredLEMSComponents";
	
	
	
	private int maxAttempts = 3;
	
	
	
	public NeuroMLAccessUtility(int maxAttempts) {
		super();
		this.maxAttempts = maxAttempts;
	}



	public NeuroMLAccessUtility() {
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
	public Base getComponent(String componentId, ModelWrapper model, ResourcesSuffix componentType) throws ModelInterpreterException
	{
		// let's first check if the cell is of a predefined neuroml type
		Base component;
		try {
			component = getComponentById(componentId, model, componentType);
		} catch (ContentError e1) {
			throw new ModelInterpreterException("Can't find the componet " + componentId);
		}

		if(component == null)
		{
			try
			{
				// otherwise let's check if it's defined in the same folder as the current component
				component = retrieveNeuroMLComponent(componentId, componentType, model);
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
	public Base retrieveNeuroMLComponent(String componentId, ResourcesSuffix componentType, ModelWrapper model) throws JAXBException, MalformedURLException
	{
		URL url = (URL) ((ModelWrapper) model).getModel(NeuroMLAccessUtility.URL_ID);
		NeuroMLConverter neuromlConverter = new NeuroMLConverter();
		boolean attemptConnection = true;
		String baseURL = url.getFile();
		HashMap<String, Base> _discoveredComponents = ((HashMap<String, Base>)((ModelWrapper) model).getModel(NeuroMLAccessUtility.DISCOVERED_COMPONENTS));
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
						_discoveredComponents.put(componentId, c);
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
					if(attempts < maxAttempts)
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

	private Base getComponentById(String componentId, ModelWrapper model, ResourcesSuffix componentType) throws ContentError
	{
		NeuroMLDocument doc = (NeuroMLDocument) ((ModelWrapper) model).getModel(NeuroMLAccessUtility.NEUROML_ID);
//		Lems lems = (Lems) ((ModelWrapper) model).getModel(NeuroMLAccessUtility.LEMS_ID);
		
		HashMap<String, Base> _discoveredComponents = ((HashMap<String, Base>)((ModelWrapper) model).getModel(NeuroMLAccessUtility.DISCOVERED_COMPONENTS));
//		HashMap<String, Component> _discoveredLEMSComponents = ((HashMap<String, Component>)((ModelWrapper) model).getModel(NeuroMLAccessUtility.DISCOVERED_LEMS_COMPONENTS));
				
		//TODO Can we have the same id for two different components 
		if(_discoveredComponents.containsKey(componentId))
		{
			return _discoveredComponents.get(componentId);
		}
		
		switch (componentType) {
		case ION_CHANNEL:
			for (IonChannel ionChannel : doc.getIonChannel()){
				if(ionChannel.getId().equals(componentId))
				{
					_discoveredComponents.put(ionChannel.getId(), ionChannel);
					return ionChannel;
				}
			}
			for (IonChannelHH ionChannelHH : doc.getIonChannelHH()){
				if(ionChannelHH.getId().equals(componentId))
				{
					_discoveredComponents.put(ionChannelHH.getId(), ionChannelHH);
					return ionChannelHH;
				}
			}
		case CELL:	
			
			for(AdExIaFCell c : doc.getAdExIaFCell())
			{
				if(c.getId().equals(componentId))
				{
					_discoveredComponents.put(c.getId(), c);
					return c;
				}
			}
			for(IafCell c : doc.getIafCell())
			{
				if(c.getId().equals(componentId))
				{
					_discoveredComponents.put(c.getId(), c);
					return c;
				}
			}
			for(Cell c : doc.getCell())
			{
				if(c.getId().equals(componentId))
				{
					_discoveredComponents.put(c.getId(), c);
					return c;
				}
			}
			return null;

		case HHRATE:
//			_discoveredLEMSComponents.put(componentId, lems.getComponent(componentId));
			
			
		default:
			break;
		}
		
		return null;

	}
	
}
