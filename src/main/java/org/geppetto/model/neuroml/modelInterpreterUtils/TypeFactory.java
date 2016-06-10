package org.geppetto.model.neuroml.modelInterpreterUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geppetto.model.neuroml.utils.ResourcesDomainType;
import org.geppetto.model.types.Type;
import org.geppetto.model.types.TypesFactory;

public class TypeFactory
{

	private TypesFactory typeFactory = TypesFactory.eINSTANCE;

	private Map<String, List<Type>> typesMap = new HashMap<String, List<Type>>();

	private Map<String, Type> types;

	public TypeFactory(Map<String, Type> types)
	{
		super();
		this.types = types;
	}

	/*
	 * Return a regular composite type if domainType is null. Otherwise return a composite type with supertype equal to the domaintype or an array type
	 */
	public Type getType(String domainName)
	{
		Type newType;
		
		// Return a regular compositeType if no domain name or domain is not a special domain name 
		// Otherwise return a type with super type
		if(domainName == null || ResourcesDomainType.getValueById(domainName) == null)
		{
			newType = typeFactory.createCompositeType();
		}
		else{
			ResourcesDomainType resourcesDomainType = ResourcesDomainType.getValueById(domainName);

			// Create array, connection or composite type and set super type
			if(resourcesDomainType.get().equals(ResourcesDomainType.POPULATION.get()))
			{
				newType = typeFactory.createArrayType();
			}
			else if(resourcesDomainType.get().equals(ResourcesDomainType.CONNECTION.get()))
			{
				newType = typeFactory.createConnectionType();
			}
			else
			{
				newType = typeFactory.createCompositeType();
			}
			newType.getSuperType().add(getSuperType(resourcesDomainType));

			// Add new type to typesMap. It will be used later on to generate description node
			List<Type> typeList = typesMap.get(resourcesDomainType.get());
			typeList.add(newType);
		}
		return newType;
	}

	private Type getSuperType(ResourcesDomainType resourcesDomainType)
	{
		// Create super type
		if(!types.containsKey(resourcesDomainType.get()))
		{
			typesMap.put(resourcesDomainType.get(), new ArrayList<Type>());

			Type domainType = typeFactory.createCompositeType();
			domainType.setId(resourcesDomainType.get());
			domainType.setName(resourcesDomainType.get());
			types.put(resourcesDomainType.get(), domainType);
		}
		
		return types.get(resourcesDomainType.get());
	}

	public Map<String, List<Type>> getTypesMap()
	{
		return typesMap;
	}

}
