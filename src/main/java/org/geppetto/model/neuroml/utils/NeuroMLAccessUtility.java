/**
 * 
 */
package org.geppetto.model.neuroml.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.model.runtime.ANode;
import org.lemsml.jlems.core.sim.ContentError;
import org.neuroml.model.AdExIaFCell;
import org.neuroml.model.Base;
import org.neuroml.model.Cell;
import org.neuroml.model.DecayingPoolConcentrationModel;
import org.neuroml.model.FitzHughNagumoCell;
import org.neuroml.model.FixedFactorConcentrationModel;
import org.neuroml.model.IafCell;
import org.neuroml.model.IafRefCell;
import org.neuroml.model.IafTauCell;
import org.neuroml.model.IafTauRefCell;
import org.neuroml.model.IonChannel;
import org.neuroml.model.IonChannelHH;
import org.neuroml.model.IzhikevichCell;
import org.neuroml.model.Network;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.Population;

/**
 * @author Adrian Quintana (adrian.perez@ucl.ac.uk)
 * 
 */
public class NeuroMLAccessUtility
{

	public static final String DISCOVERED_COMPONENTS = "discoveredComponents";
	public static final String LEMS_ID = "lems";
	public static final String NEUROML_ID = "neuroml";
	public static final String NEUROML_ID_INCLUSIONS = "neuromlInclusions";
	public static final String URL_ID = "url";
	public static final String SUBENTITIES_MAPPING_ID = "entitiesMapping";
//	public static final String LEMS_UTILS_ID = "lemsUtils";
	public static final String LEMS_ID_INCLUSIONS = "lemsInclusions";
//	public static final String DISCOVERED_NODES = "discovered_nodes";
	public static final String DISCOVERED_NESTED_COMPONENTS_ID = "discoveredNestedComponents";
	
	private LEMSAccessUtility lemsAccessUtility = new LEMSAccessUtility();
	
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
	public Object getComponent(String componentId, ModelWrapper model, Resources componentType) throws ModelInterpreterException
	{
		Object component;
		//Check if we have already discovered this component
		if (componentType == Resources.COMPONENT_TYPE){
			component = this.lemsAccessUtility.getComponent(componentId, model);
		}
		else{
			component = this.getComponentFromCache(componentId, model);
			
//			if (component == null){
//				component = this.lemsAccessUtility.getComponentFromCache(componentId, model);
//			}
			
			// let's first check if the cell is of a predefined neuroml type
			if(component == null){
				try {
					component = getComponentById(componentId, model, componentType);
				} catch (ContentError e1) {
					throw new ModelInterpreterException("Can't find the componet " + componentId);
				}
			}
		}	

		if(component == null)
		{
			// sorry no luck!
			throw new ModelInterpreterException("Can't find the componet " + componentId);
		}
		return component;
	}
	
	public Base getComponentFromCache(String componentId, ModelWrapper model){
		HashMap<String, Base> _discoveredComponents = ((HashMap<String, Base>)((ModelWrapper) model).getModel(NeuroMLAccessUtility.DISCOVERED_COMPONENTS));
		List<String> _discoveredNestedComponentsId = ((ArrayList<String>)((ModelWrapper) model).getModel(NeuroMLAccessUtility.DISCOVERED_NESTED_COMPONENTS_ID));
		
		//TODO We may think about storing nodes instead of components
		if(_discoveredComponents.containsKey(componentId))
		{
			if (!_discoveredNestedComponentsId.contains(componentId)){
				_discoveredNestedComponentsId.add(componentId);
			}
			return _discoveredComponents.get(componentId);
		}
		
		return null;
	}
	
	
	private Object getComponentById(String componentId, ModelWrapper model, Resources componentType) throws ContentError
	{
		
//		Lems lems = (Lems) ((ModelWrapper) model).getModel(NeuroMLAccessUtility.LEMS_ID);
		HashMap<String, Base> _discoveredComponents = ((HashMap<String, Base>)((ModelWrapper) model).getModel(NeuroMLAccessUtility.DISCOVERED_COMPONENTS));
		
		NeuroMLDocument doc = (NeuroMLDocument) ((ModelWrapper) model).getModel(NeuroMLAccessUtility.NEUROML_ID_INCLUSIONS);
		if (doc == null){
			doc = (NeuroMLDocument) ((ModelWrapper) model).getModel(NeuroMLAccessUtility.NEUROML_ID);
		}
		
		switch (componentType) {
		case ION_CHANNEL:
			for (IonChannel ionChannel : doc.getIonChannel()){
				_discoveredComponents.put(ionChannel.getId(), ionChannel);
				if(ionChannel.getId().equals(componentId))
				{
					return ionChannel;
				}
			}
			for (IonChannelHH ionChannelHH : doc.getIonChannelHH()){
				_discoveredComponents.put(ionChannelHH.getId(), ionChannelHH);
				if(ionChannelHH.getId().equals(componentId))
				{
					return ionChannelHH;
				}
			}
		case CELL:	
			
			for(AdExIaFCell c : doc.getAdExIaFCell())
			{
				_discoveredComponents.put(c.getId(), c);
				if(c.getId().equals(componentId))
				{
					return c;
				}
			}
			for(IafCell c : doc.getIafCell())
			{
				_discoveredComponents.put(c.getId(), c);
				if(c.getId().equals(componentId))
				{
					return c;
				}
			}
			for(Cell c : doc.getCell())
			{
				_discoveredComponents.put(c.getId(), c);
				if(c.getId().equals(componentId))
				{
					return c;
				}
			}
			for(IafRefCell c : doc.getIafRefCell())
			{
				_discoveredComponents.put(c.getId(), c);
				if(c.getId().equals(componentId))
				{
					return c;
				}
			}
			for(IafTauRefCell c : doc.getIafTauRefCell())
			{
				_discoveredComponents.put(c.getId(), c);
				if(c.getId().equals(componentId))
				{
					return c;
				}
			}
			for(IafTauCell c : doc.getIafTauCell())
			{
				_discoveredComponents.put(c.getId(), c);
				if(c.getId().equals(componentId))
				{
					return c;
				}
			}
			for(FitzHughNagumoCell c : doc.getFitzHughNagumoCell())
			{
				_discoveredComponents.put(c.getId(), c);
				if(c.getId().equals(componentId))
				{
					return c;
				}
			}
			for(IzhikevichCell c : doc.getIzhikevichCell())
			{
				_discoveredComponents.put(c.getId(), c);
				if(c.getId().equals(componentId))
				{
					return c;
				}
			}
			
		case CONCENTRATION_MODEL:
			for(FixedFactorConcentrationModel c : doc.getFixedFactorConcentrationModel())
			{
				_discoveredComponents.put(c.getId(), c);
				if(c.getId().equals(componentId))
				{
					return c;
				}
			}
			
			for(DecayingPoolConcentrationModel c : doc.getDecayingPoolConcentrationModel())
			{
				_discoveredComponents.put(c.getId(), c);
				if(c.getId().equals(componentId))
				{
					return c;
				}
			}
			
		case POPULATION:	
			for(Network n : doc.getNetwork()){
				for (Population p : n.getPopulation()){
					if (p.getId().equals(componentId)){
						return p;
					}
				}
			}
			
		default:
			return this.lemsAccessUtility.getComponentById(componentId, model);
		}
		
		
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
//		URL url = (URL) ((ModelWrapper) model).getModel(NeuroMLAccessUtility.URL_ID);
//		NeuroMLConverter neuromlConverter = new NeuroMLConverter();
//		boolean attemptConnection = true;
//		String baseURL = url.getFile();
//		HashMap<String, Base> _discoveredComponents = ((HashMap<String, Base>)((ModelWrapper) model).getModel(NeuroMLAccessUtility.DISCOVERED_COMPONENTS));
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
