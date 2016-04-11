package org.geppetto.model.neuroml.modelInterpreterUtils;

import org.geppetto.core.model.GeppettoModelAccess;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.model.neuroml.utils.ModelInterpreterUtils;
import org.geppetto.model.neuroml.utils.Resources;
import org.geppetto.model.neuroml.utils.ResourcesDomainType;
import org.geppetto.model.types.ArrayType;
import org.geppetto.model.types.CompositeType;
import org.geppetto.model.types.ConnectionType;
import org.geppetto.model.util.GeppettoVisitingException;
import org.geppetto.model.util.PointerUtility;
import org.geppetto.model.values.Connection;
import org.geppetto.model.values.Connectivity;
import org.geppetto.model.variables.Variable;
import org.lemsml.jlems.core.sim.LEMSException;
import org.lemsml.jlems.core.type.Attribute;
import org.lemsml.jlems.core.type.Component;
import org.neuroml.model.util.NeuroMLException;

public class PopulateProjectionTypes extends APopulateProjectionTypes
{

	public PopulateProjectionTypes(PopulateTypes populateTypes, GeppettoModelAccess access)
	{
		super(populateTypes, access);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createConnectionTypeVariablesFromProjection(Component projection, CompositeType compositeType) throws GeppettoVisitingException, LEMSException, NeuroMLException,
			NumberFormatException, ModelInterpreterException
	{
		super.createConnectionTypeVariablesFromProjection(projection, compositeType);
		
		// Create synapse type
		createSynapseType(projection, projectionType);

		// Iterate over all the children. Most of them are connections
		for(Component projectionChild : projection.getStrictChildren())
		{
			if(projectionChild.getComponentType().isOrExtends(Resources.CONNECTION.getId()) || projectionChild.getComponentType().isOrExtends(Resources.CONNECTION_WD.getId()))
			{
				projectionType.getVariables().add(extractConnection(projectionChild, prePopulationType, prePopulationVariable, postPopulationType, postPopulationVariable));
			}
			else
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

	}
	
	protected Variable extractConnection(Component projectionChild, ArrayType prePopulationType, Variable prePopulationVariable, ArrayType postPopulationType, Variable postPopulationVariable)
			throws GeppettoVisitingException
	{
		ConnectionType connectionType = (ConnectionType) populateTypes.getTypeFactory().getType(ResourcesDomainType.CONNECTION.getId());
		NeuroMLModelInterpreterUtils.initialiseNodeFromComponent(connectionType, projectionChild);

		Connection connection = valuesFactory.createConnection();
		connection.setConnectivity(Connectivity.DIRECTIONAL);

		for(Attribute attribute : projectionChild.getAttributes())
		{
			if(attribute.getName().equals("preCellId"))
			{
				String preCellId = ModelInterpreterUtils.parseCellRefStringForCellNum(attribute.getValue());
				connection.getA().add(PointerUtility.getPointer(prePopulationVariable, prePopulationType, Integer.parseInt(preCellId)));
			}
			else if(attribute.getName().equals("postCellId"))
			{
				String postCellId = ModelInterpreterUtils.parseCellRefStringForCellNum(attribute.getValue());
				connection.getB().add(PointerUtility.getPointer(postPopulationVariable, postPopulationType, Integer.parseInt(postCellId)));
			}
			else
			{
				// preSegmentId, preFractionAlong, postSegmentId, postFractionAlong
				connectionType.getVariables().add(ModelInterpreterUtils.createTextTypeVariable(attribute.getName(), attribute.getValue(), access));
			}
		}

		Variable variable = variablesFactory.createVariable();
		NeuroMLModelInterpreterUtils.initialiseNodeFromComponent(variable, projectionChild);
		variable.getAnonymousTypes().add(connectionType);
		variable.getInitialValues().put(connectionType, connection);
		return variable;

	}
}
