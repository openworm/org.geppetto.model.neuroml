package org.geppetto.model.neuroml.modelInterpreterUtils;

import org.geppetto.core.model.GeppettoModelAccess;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.model.GeppettoLibrary;
import org.geppetto.model.neuroml.utils.Resources;
import org.geppetto.model.neuroml.utils.ResourcesDomainType;
import org.geppetto.model.types.ArrayType;
import org.geppetto.model.types.CompositeType;
import org.geppetto.model.types.ImportType;
import org.geppetto.model.types.Type;
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
	protected PopulateTypes populateTypes;

	protected ValuesFactory valuesFactory = ValuesFactory.eINSTANCE;
	protected VariablesFactory variablesFactory = VariablesFactory.eINSTANCE;

	protected GeppettoModelAccess geppettoModelAccess;

	protected CompositeType projectionType;
	protected ArrayType prePopulationType;
	protected Variable prePopulationVariable;
	protected ArrayType postPopulationType;
	protected Variable postPopulationVariable;

	private GeppettoLibrary library;

	public APopulateProjectionTypes(PopulateTypes populateTypes, GeppettoModelAccess access, GeppettoLibrary library)
	{
		super();
		this.populateTypes = populateTypes;
		this.geppettoModelAccess = access;
		this.library = library;
	}

	/**
	 * @param projection
	 * @param importType
	 * @param parent
	 * @throws ModelInterpreterException
	 */
	public Type resolveProjectionImportType(Component projection, ImportType importType) throws ModelInterpreterException
	{
		try
		{

			CompositeType parent = null;
			if(importType.getReferencedVariables().size() == 1)
			{
				parent = (CompositeType) importType.getReferencedVariables().get(0).eContainer();
			}
			else
			{
				throw new ModelInterpreterException("Ops, the projection is used more than once, we need to change this algorithm");
			}

			// Create projection type and add it to the composite type
			projectionType = createProjectionType(projection, parent);

			projectionType = createProjectionType(projection, parent);
			geppettoModelAccess.swapType(importType, projectionType, (GeppettoLibrary) importType.eContainer());
			geppettoModelAccess.removeType(importType);

			// Create pre synaptic population
			prePopulationType = (ArrayType) populateTypes.getTypes().get(Resources.POPULATION.getId() + projection.getAttributeValue(Resources.PRE_SYNAPTIC_POPULATION.getId()));

			prePopulationVariable = getPopulation(parent, projectionType, prePopulationType, Resources.PRE_SYNAPTIC_POPULATION);

			// Create post synaptic population
			postPopulationType = (ArrayType) populateTypes.getTypes().get(Resources.POPULATION.getId() + projection.getAttributeValue(Resources.POST_SYNAPTIC_POPULATION.getId()));
			postPopulationVariable = getPopulation(parent, projectionType, postPopulationType, Resources.POST_SYNAPTIC_POPULATION);

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

			return projectionType;
		}
		catch(GeppettoVisitingException | NumberFormatException | NeuroMLException | LEMSException e)
		{
			throw new ModelInterpreterException(e);
		}
	}

	/**
	 * @param compositeType
	 * @param projectionType
	 * @param populationType
	 * @param populationName
	 * @return
	 * @throws GeppettoVisitingException
	 */
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
		populationPointerVariable.getTypes().add(geppettoModelAccess.getType(TypesPackage.Literals.POINTER_TYPE));
		populationPointerVariable.getInitialValues().put(geppettoModelAccess.getType(TypesPackage.Literals.POINTER_TYPE), PointerUtility.getPointer(populationVariable, populationType, null));
		projectionType.getVariables().add(populationPointerVariable);
		return populationVariable;
	}

	/**
	 * @param projection
	 * @param projectionType
	 * @throws NeuroMLException
	 * @throws LEMSException
	 * @throws GeppettoVisitingException
	 * @throws ModelInterpreterException
	 */
	protected void createSynapseType(Component projection, CompositeType projectionType) throws NeuroMLException, LEMSException, GeppettoVisitingException, ModelInterpreterException
	{
		Component synapse = projection.getRefComponents().get(Resources.SYNAPSE.getId());
		if(!populateTypes.getTypes().containsKey(synapse.getDeclaredType() + synapse.getID()))
		{
			Type synapseType = populateTypes.extractInfoFromComponent(projection.getRefComponents().get(Resources.SYNAPSE.getId()), ResourcesDomainType.SYNAPSE.getId());
			populateTypes.getTypes().put(synapse.getDeclaredType() + synapse.getID(), synapseType);
			geppettoModelAccess.addTypeToLibrary(synapseType, library);
		}
		Variable synapsesVariable = variablesFactory.createVariable();
		NeuroMLModelInterpreterUtils.initialiseNodeFromComponent(synapsesVariable, synapse);
		synapsesVariable.getTypes().add(populateTypes.getTypes().get(synapse.getDeclaredType() + synapse.getID()));
		projectionType.getVariables().add(synapsesVariable);
	}

	/**
	 * @param projection
	 * @param parentCompositeType
	 *            the parent to which we want to add the projection
	 * @return
	 */
	private CompositeType createProjectionType(Component projection, CompositeType parentCompositeType)
	{
		// get/create the projection type and variable
		CompositeType projectionType = null;
		if(!populateTypes.getTypesMap().containsKey(projection.getDeclaredType() + projection.getID()))
		{
			projectionType = (CompositeType) populateTypes.getTypeFactory().createType(ResourcesDomainType.PROJECTION.getId());
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
		parentCompositeType.getVariables().add(projectionVariable);
		return projectionType;
	}

}
