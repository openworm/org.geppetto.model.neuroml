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
				throw new ModelInterpreterException("Can't find the componet " + componentId + ". Exception throw: " + e1.toString());
			}
		}

		if(component == null)
		{
			// sorry no luck!
			throw new ModelInterpreterException("Can't find the componet " + componentId);
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
		Lems lems = (Lems) ((ModelWrapper) model).getModel(NeuroMLAccessUtility.LEMS_ID);
//		System.out.println(lems.getComponents());
		Object component = lems.getComponentTypeByName(componentId);
		//Store the component in the cache
		((HashMap<String, Object>)((ModelWrapper) model).getModel(DISCOVERED_LEMS_COMPONENTS)).put(componentId, component);
		
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
	
}
