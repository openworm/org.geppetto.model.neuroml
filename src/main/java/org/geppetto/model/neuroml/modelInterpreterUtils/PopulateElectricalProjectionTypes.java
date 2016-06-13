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
import org.geppetto.model.values.ValuesFactory;
import org.geppetto.model.values.VisualReference;
import org.geppetto.model.variables.Variable;
import org.lemsml.jlems.core.sim.ContentError;
import org.lemsml.jlems.core.sim.LEMSException;
import org.lemsml.jlems.core.type.Component;
import org.neuroml.model.util.NeuroMLException;

public class PopulateElectricalProjectionTypes extends APopulateProjectionTypes
{

	public PopulateElectricalProjectionTypes(PopulateTypes populateTypes, GeppettoModelAccess access)
	{
		super(populateTypes, access);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createConnectionTypeVariablesFromProjection(Component projection, CompositeType compositeType) throws GeppettoVisitingException, LEMSException, NeuroMLException,
			NumberFormatException, ModelInterpreterException
	{
		super.createConnectionTypeVariablesFromProjection(projection, compositeType);

		// // Create synapse type
		// createSynapseType(projection, projectionType);

		// Iterate over all the children. Most of them are connections
		for(Component projectionChild : projection.getStrictChildren())
		{
			if(projectionChild.getComponentType().isOrExtends(Resources.ELECTRICAL_CONNECTION.getId())
					|| projectionChild.getComponentType().isOrExtends(Resources.ELECTRICAL_CONNECTION_INSTANCE.getId()))
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
			throws GeppettoVisitingException, ModelInterpreterException
	{
		ConnectionType connectionType = (ConnectionType) populateTypes.getTypeFactory().getType(ResourcesDomainType.CONNECTION.getId());
		NeuroMLModelInterpreterUtils.initialiseNodeFromComponent(connectionType, projectionChild);

		Connection connection = valuesFactory.createConnection();
		connection.setConnectivity(Connectivity.DIRECTIONAL);

		try
		{

			String preCell = projectionChild.getDeclaredType().equals(Resources.ELECTRICAL_CONNECTION_INSTANCE.getId()) ? ModelInterpreterUtils.parseCellRefStringForCellNum(projectionChild
					.getAttributeValue("preCellId")) : projectionChild.getAttributeValue("preCellId");
			String postCell = projectionChild.getDeclaredType().equals(Resources.ELECTRICAL_CONNECTION_INSTANCE.getId()) ? ModelInterpreterUtils.parseCellRefStringForCellNum(projectionChild
					.getAttributeValue("postCellId")) : projectionChild.getAttributeValue("postCellId");
			String preSegmentId = projectionChild.getAttributeValue("preSegmentId");
			String preFractionAlong = projectionChild.getAttributeValue("preFractionAlong");
			String postSegmentId = projectionChild.getAttributeValue("postSegmentId");
			String postFractionAlong = projectionChild.getAttributeValue("postFractionAlong");
			if(preCell != null)
			{
				connection.setA(PointerUtility.getPointer(prePopulationVariable, prePopulationType, Integer.parseInt(preCell)));
				if(preSegmentId != null)
				{
					VisualReference visualReference = ValuesFactory.eINSTANCE.createVisualReference();
					connection.getA().setVisualReference(visualReference);
					Variable targetVisualVariable = NeuroMLModelInterpreterUtils.getVisualVariable(preSegmentId);
					visualReference.setVisualVariable(targetVisualVariable);
					if(preFractionAlong != null)
					{
						visualReference.setFraction(Float.parseFloat(preFractionAlong));
					}
				}
			}
			if(postCell != null)
			{
				connection.setA(PointerUtility.getPointer(prePopulationVariable, prePopulationType, Integer.parseInt(postCell)));
				if(postSegmentId != null)
				{
					VisualReference visualReference = ValuesFactory.eINSTANCE.createVisualReference();
					connection.getA().setVisualReference(visualReference);
					Variable targetVisualVariable = NeuroMLModelInterpreterUtils.getVisualVariable(postSegmentId);
					visualReference.setVisualVariable(targetVisualVariable);
					if(postFractionAlong != null)
					{
						visualReference.setFraction(Float.parseFloat(postFractionAlong));
					}
				}
			}
		}
		catch(ContentError e)
		{
			throw new ModelInterpreterException(e);
		}

		Variable variable = variablesFactory.createVariable();
		NeuroMLModelInterpreterUtils.initialiseNodeFromComponent(variable, projectionChild);
		variable.getAnonymousTypes().add(connectionType);
		variable.getInitialValues().put(connectionType, connection);
		return variable;

	}
}
