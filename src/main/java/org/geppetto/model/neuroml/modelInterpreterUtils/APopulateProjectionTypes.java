package org.geppetto.model.neuroml.modelInterpreterUtils;

import org.geppetto.core.model.GeppettoModelAccess;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.model.neuroml.utils.Resources;
import org.geppetto.model.neuroml.utils.ResourcesDomainType;
import org.geppetto.model.types.ArrayType;
import org.geppetto.model.types.CompositeType;
import org.geppetto.model.types.TypesPackage;
import org.geppetto.model.util.GeppettoVisitingException;
import org.geppetto.model.util.PointerUtility;
import org.geppetto.model.values.ValuesFactory;
import org.geppetto.model.variables.Variable;
import org.geppetto.model.variables.VariablesFactory;
import org.lemsml.jlems.core.sim.LEMSException;
import org.lemsml.jlems.core.type.Component;
import org.neuroml.model.util.NeuroMLException;

public abstract class APopulateProjectionTypes
{
	PopulateTypes populateTypes;

	ValuesFactory valuesFactory = ValuesFactory.eINSTANCE;
	VariablesFactory variablesFactory = VariablesFactory.eINSTANCE;

	GeppettoModelAccess access;

	CompositeType projectionType;
	ArrayType prePopulationType;
	Variable prePopulationVariable;
	ArrayType postPopulationType;
	Variable postPopulationVariable;

	public APopulateProjectionTypes(PopulateTypes populateTypes, GeppettoModelAccess access)
	{
		super();
		this.populateTypes = populateTypes;
		this.access = access;
	}

	public void createConnectionTypeVariablesFromProjection(Component projection, CompositeType compositeType) throws GeppettoVisitingException, LEMSException, NeuroMLException,
			NumberFormatException, ModelInterpreterException
	{
		// Create projection type and add it to the composite type
		projectionType = createProjectionType(projection, compositeType);

		// Create pre synaptic population
		prePopulationType = (ArrayType) populateTypes.getTypes().get(Resources.POPULATION.getId() + projection.getAttributeValue(Resources.PRE_SYNAPTIC_POPULATION.getId()));
		prePopulationVariable = getPopulation(compositeType, projectionType, prePopulationType, Resources.PRE_SYNAPTIC_POPULATION);

		// Create post synaptic population
		postPopulationType = (ArrayType) populateTypes.getTypes().get(Resources.POPULATION.getId() + projection.getAttributeValue(Resources.POST_SYNAPTIC_POPULATION.getId()));
		postPopulationVariable = getPopulation(compositeType, projectionType, postPopulationType, Resources.POST_SYNAPTIC_POPULATION);

		// Extract child component
		for(Component projectionChild : projection.getChildHM().values())
		{
			CompositeType anonymousCompositeType = populateTypes.extractInfoFromComponent(projectionChild, null);
			if(anonymousCompositeType != null)
			{
				Variable variable = variablesFactory.createVariable();
				NeuroMLModelInterpreterUtils.initialiseNodeFromComponent(variable, projectionChild);
				variable.getAnonymousTypes().add(anonymousCompositeType);
				projectionType.getVariables().add(variable);
			}
		}

	}

	private Variable getPopulation(CompositeType compositeType, CompositeType projectionType, ArrayType populationType, Resources populationName) throws GeppettoVisitingException
	{
		Variable populationVariable = null;
		for(Variable variable : compositeType.getVariables())
		{
			if(variable.getId().equals(populationType.getId()))
			{
				populationVariable = variable;
				break;
			}
		}
		Variable populationPointerVariable = variablesFactory.createVariable();
		NeuroMLModelInterpreterUtils.initialiseNodeFromString(populationPointerVariable, populationName.getId());
		populationPointerVariable.getTypes().add(access.getType(TypesPackage.Literals.POINTER_TYPE));
		populationPointerVariable.getInitialValues().put(access.getType(TypesPackage.Literals.POINTER_TYPE), PointerUtility.getPointer(populationVariable, populationType, null));
		projectionType.getVariables().add(populationPointerVariable);
		return populationVariable;
	}

	protected void createSynapseType(Component projection, CompositeType projectionType) throws NeuroMLException, LEMSException, GeppettoVisitingException, ModelInterpreterException
	{
		Component synapse = projection.getRefComponents().get(Resources.SYNAPSE.getId());
		if(!populateTypes.getTypes().containsKey(synapse.getDeclaredType() + synapse.getID()))
		{
			populateTypes.getTypes().put(synapse.getDeclaredType() + synapse.getID(),
					populateTypes.extractInfoFromComponent(projection.getRefComponents().get(Resources.SYNAPSE.getId()), ResourcesDomainType.SYNAPSE.getId()));
		}
		Variable synapsesVariable = variablesFactory.createVariable();
		NeuroMLModelInterpreterUtils.initialiseNodeFromComponent(synapsesVariable, synapse);
		synapsesVariable.getTypes().add(populateTypes.getTypes().get(synapse.getDeclaredType() + synapse.getID()));
		projectionType.getVariables().add(synapsesVariable);
	}

	private CompositeType createProjectionType(Component projection, CompositeType compositeType)
	{
		// get/create the projection type and variable
		CompositeType projectionType = null;
		if(!populateTypes.getTypesMap().containsKey(projection.getDeclaredType() + projection.getID()))
		{
			projectionType = (CompositeType) populateTypes.getTypeFactory().getType(ResourcesDomainType.PROJECTION.getId());
			NeuroMLModelInterpreterUtils.initialiseNodeFromComponent(projectionType, projection);
			populateTypes.getTypes().put(projection.getDeclaredType() + projection.getID(), projectionType);
		}
		else
		{
			projectionType = (CompositeType) populateTypes.getTypes().get(projection.getDeclaredType() + projection.getID());
		}

		Variable projectionVariable = variablesFactory.createVariable();
		NeuroMLModelInterpreterUtils.initialiseNodeFromComponent(projectionVariable, projection);
		projectionVariable.getTypes().add(populateTypes.getTypes().get(projection.getDeclaredType() + projection.getID()));
		compositeType.getVariables().add(projectionVariable);
		return projectionType;
	}
}
