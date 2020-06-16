

package org.geppetto.model.neuroml.utils;

import org.lemsml.jlems.core.type.ComponentType;

/**
 * Class to hold resources used in the visualiser. This elements will be displayed to the user.
 * @author matteocantarelli
 * @author Adrian Quintana (adrian.perez@ucl.ac.uk)
 *
 */
public enum ResourcesDomainType
{
	
	SYNAPSE("synapse", "synapse"),
	BASE_SYNAPSE("synapse", "baseSynapse"),
	PREFRACTIONALONG("preFractionAlong", "preFractionAlong"),
	POSTFRACTIONALONG("postFractionAlong", "postFractionAlong"),
	IONCHANNEL("ionChannel", "baseIonChannel"),
	PULSEGENERATOR("pulseGenerator", "pulseGenerator"),
	POISSONFIRINGSYNAPSE("poissonFiringSynapse", "poissonFiringSynapse"),
	POPULATION("population", "population"),
	POPULATION_LIST("population", "populationList"),
	PROJECTION("projection", "projection"),
        CONTINUOUS_PROJECTION("continuousProjection", "continuousProjection"),
        ELECTRICAL_PROJECTION("electricalProjection", "electricalProjection"),
	CONNECTION("connection", "connection"),
	CONNECTIONWD("connection", "connectionWD"),
	NETWORK("network", "network"),
	CELL("cell", "baseCell");
	
	
	private String _value;
	private String _id;
	
	private ResourcesDomainType(String value, String id)
	{
		_value = value;
		_id = id;
	}
	
	public String get()
	{
		return _value;
	}
	
	public String getId()
	{
		return _id;
	}
	
	public static ResourcesDomainType getValueById(String id){
            for(ResourcesDomainType e : ResourcesDomainType.values()){
                if(id.equals(e._id)) return e;
            }
            //If we can't find a value, return the id
            return null;
	}
	
	public static String getValueByComponentType(ComponentType componentType){
            for(ResourcesDomainType e : ResourcesDomainType.values()){
                if(componentType.isOrExtends(e._id)) return e._id;
            }
            //If we can't find a value, return the id
            return null;
	}
	
}
