/**
 * 
 */
package org.geppetto.model.neuroml.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.model.neuroml.services.ModelFormat;
import org.lemsml.jlems.core.sim.ContentError;
import org.neuroml.model.AdExIaFCell;
import org.neuroml.model.AlphaCondSynapse;
import org.neuroml.model.AlphaCurrSynapse;
import org.neuroml.model.Base;
import org.neuroml.model.BiophysicalProperties;
import org.neuroml.model.BlockingPlasticSynapse;
import org.neuroml.model.Cell;
import org.neuroml.model.DecayingPoolConcentrationModel;
import org.neuroml.model.ExpCondSynapse;
import org.neuroml.model.ExpCurrSynapse;
import org.neuroml.model.ExpOneSynapse;
import org.neuroml.model.ExpTwoSynapse;
import org.neuroml.model.ExtracellularProperties;
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
	public static final String URL_ID = "url";
	public static final String SUBENTITIES_MAPPING_ID = "entitiesMapping";
	public static final String DISCOVERED_NESTED_COMPONENTS_ID = "discoveredNestedComponents";
	public static final String CELL_SUBENTITIES_MAPPING_ID = "cellEntitiesMapping";

	private LEMSAccessUtility lemsAccessUtility = new LEMSAccessUtility();

	public NeuroMLAccessUtility()
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
	public Object getComponent(String componentId, ModelWrapper model, Resources componentType) throws ModelInterpreterException
	{
		Object component;
		// Check if we have already discovered this component
		if(componentType == Resources.COMPONENT_TYPE)
		{
			component = this.lemsAccessUtility.getComponent(componentId, model);
		}
		else
		{
			component = this.getComponentFromCache(componentId, model);

			// let's first check if the cell is of a predefined neuroml type
			if(component == null)
			{
				try
				{
					component = getComponentById(componentId, model, componentType);
					if(component != null)
					{
						registerAsDiscoverNestedComponents(componentId, model);
					}
				}
				catch(ContentError e1)
				{
					return null;
				}
			}
		}

		if(component == null)
		{
			// sorry no luck!
			return null;
		}
		return component;
	}

	public Base getComponentFromCache(String componentId, ModelWrapper model)
	{
		HashMap<String, Base> _discoveredComponents = ((HashMap<String, Base>) ((ModelWrapper) model).getModel(NeuroMLAccessUtility.DISCOVERED_COMPONENTS));

		// TODO We may think about storing nodes instead of components
		if(_discoveredComponents.containsKey(componentId))
		{
			registerAsDiscoverNestedComponents(componentId, model);

			return _discoveredComponents.get(componentId);
		}

		return null;
	}

	private Object getComponentById(String componentId, ModelWrapper model, Resources componentType) throws ContentError
	{
		HashMap<String, Base> _discoveredComponents = ((HashMap<String, Base>) ((ModelWrapper) model).getModel(NeuroMLAccessUtility.DISCOVERED_COMPONENTS));
		NeuroMLDocument doc = (NeuroMLDocument) ((ModelWrapper) model).getModel(ModelFormat.NEUROML);

		switch(componentType)
		{
			case ION_CHANNEL:
				for(IonChannel ionChannel : doc.getIonChannel())
				{
					_discoveredComponents.put(ionChannel.getId(), ionChannel);
					if(ionChannel.getId().equals(componentId))
					{
						return ionChannel;
					}
				}
				for(IonChannelHH ionChannelHH : doc.getIonChannelHH())
				{
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
				for(Network n : doc.getNetwork())
				{
					for(Population p : n.getPopulation())
					{
						if(p.getId().equals(componentId))
						{
							return p;
						}
					}
				}

			case SYNAPSE:
				for(ExpTwoSynapse s : doc.getExpTwoSynapse())
				{
					_discoveredComponents.put(s.getId(), s);
					if(s.getId().equals(componentId))
					{
						return s;
					}
				}
				for(ExpOneSynapse s : doc.getExpOneSynapse())
				{
					_discoveredComponents.put(s.getId(), s);
					if(s.getId().equals(componentId))
					{
						return s;
					}
				}
				for(BlockingPlasticSynapse s : doc.getBlockingPlasticSynapse())
				{
					_discoveredComponents.put(s.getId(), s);
					if(s.getId().equals(componentId))
					{
						return s;
					}
				}

			case EXTRACELLULAR_P:
				for(ExtracellularProperties e : doc.getExtracellularProperties())
				{
					_discoveredComponents.put(e.getId(), e);
					if(e.getId().equals(componentId))
					{
						return e;
					}
				}
			case PYNN_SYNAPSE:
				for(AlphaCondSynapse s : doc.getAlphaCondSynapse())
				{
					_discoveredComponents.put(s.getId(), s);
					if(s.getId().equals(componentId))
					{
						return s;
					}

				}
				for(ExpCondSynapse s : doc.getExpCondSynapse())
				{
					_discoveredComponents.put(s.getId(), s);
					if(s.getId().equals(componentId))
					{
						return s;
					}

				}
				for(ExpCurrSynapse s : doc.getExpCurrSynapse())
				{
					_discoveredComponents.put(s.getId(), s);
					if(s.getId().equals(componentId))
					{
						return s;
					}

				}
				for(AlphaCurrSynapse s : doc.getAlphaCurrSynapse())
				{
					_discoveredComponents.put(s.getId(), s);
					if(s.getId().equals(componentId))
					{
						return s;
					}
				}

			case BIOPHYSICAL_PROPERTIES:
				for(BiophysicalProperties b : doc.getBiophysicalProperties())
				{
					_discoveredComponents.put(b.getId(), b);
					if(b.getId().equals(componentId))
					{
						return b;
					}
				}

			default:
				return this.lemsAccessUtility.getComponentById(componentId, model);
		}

	}
	
	private void registerAsDiscoverNestedComponents(String componentId, ModelWrapper model)
	{
		List<String> _discoveredNestedComponentsId = ((ArrayList<String>) ((ModelWrapper) model).getModel(NeuroMLAccessUtility.DISCOVERED_NESTED_COMPONENTS_ID));
		if(!_discoveredNestedComponentsId.contains(componentId))
		{
			_discoveredNestedComponentsId.add(componentId);
		}
	}

}
