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
import org.lemsml.jlems.core.type.ComponentType;
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
public class LEMSAccessUtility
{
	public static final String DISCOVERED_LEMS_COMPONENTS = "discoveredLEMSComponents";
	
	
	
	private int maxAttempts = 3;
	
	
	
	public LEMSAccessUtility(int maxAttempts) {
		super();
		this.maxAttempts = maxAttempts;
	}



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
	public ComponentType getComponent(String componentId, ModelWrapper model) throws ModelInterpreterException
	{
		// let's first check if the cell is of a predefined neuroml type
		ComponentType component;
		try {
			component = getComponentById(componentId, model);
		} catch (ContentError e1) {
			throw new ModelInterpreterException("Can't find the componet " + componentId);
		}

//		if(component == null)
//		{
//			try
//			{
//				// otherwise let's check if it's defined in the same folder as the current component
//				component = retrieveNeuroMLComponent(componentId, componentType, model);
//			}
//			catch(MalformedURLException e)
//			{
//				throw new ModelInterpreterException(e);
//			}
//			catch(JAXBException e)
//			{
//				throw new ModelInterpreterException(e);
//			}
//		}
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
//	public Base retrieveNeuroMLComponent(String componentId, ResourcesSuffix componentType, ModelWrapper model) throws JAXBException, MalformedURLException
//	{
//		URL url = (URL) ((ModelWrapper) model).getModel(LEMSAccessUtility.URL_ID);
//		NeuroMLConverter neuromlConverter = new NeuroMLConverter();
//		boolean attemptConnection = true;
//		String baseURL = url.getFile();
//		HashMap<String, Base> _discoveredComponents = ((HashMap<String, Base>)((ModelWrapper) model).getModel(LEMSAccessUtility.DISCOVERED_COMPONENTS));
//		if(url.getFile().endsWith("nml"))
//		{
//			baseURL = baseURL.substring(0, baseURL.lastIndexOf("/") + 1);
//		}
//		int attempts = 0;
//		NeuroMLDocument neuromlDocument = null;
//		while(attemptConnection)
//		{
//			try
//			{
//				attemptConnection = false;
//				attempts++;
//				URL componentURL = new URL(url.getProtocol() + "://" + url.getAuthority() + baseURL + componentId + componentType.get() + ".nml");
//
//				neuromlDocument = neuromlConverter.urlToNeuroML(componentURL);
//
//				List<? extends Base> components = null;
//				
//				switch (componentType) {
//				case ION_CHANNEL:
//					components = neuromlDocument.getIonChannel();
//				default:
//					break;
//				}
//				
//				if(components != null)
//				{
//					
//					
//					for(Base c : neuromlDocument.getIonChannel())
//					{
//						_discoveredComponents.put(componentId, c);
//						if(((Base)c).getId().equals(componentId))
//						{
//							return c;
//						}
//					}
//				}
//			}
//			catch(MalformedURLException e)
//			{
//				throw e;
//			}
//			catch(UnmarshalException e)
//			{
//				if(e.getLinkedException() instanceof IOException)
//				{
//					if(attempts < maxAttempts)
//					{
//						attemptConnection = true;
//					}
//				}
//			}
//			catch(Exception e)
//			{
//				throw e;
//			}
//		}
//		return null;
//	}

	private ComponentType getComponentById(String componentId, ModelWrapper model) throws ContentError
	{
		Lems lems = (Lems) ((ModelWrapper) model).getModel(NeuroMLAccessUtility.LEMS_ID);
		
		HashMap<String, ComponentType> _discoveredLEMSComponents = ((HashMap<String, ComponentType>)((ModelWrapper) model).getModel(DISCOVERED_LEMS_COMPONENTS));
				
		//TODO Can we have the same id for two different components 
		if(_discoveredLEMSComponents.containsKey(componentId))
		{
			return _discoveredLEMSComponents.get(componentId);
		}
		
		ComponentType component = lems.getComponentTypeByName(componentId);
		
		_discoveredLEMSComponents.put(componentId, component);
			
		return component;
			
	}
	
}
