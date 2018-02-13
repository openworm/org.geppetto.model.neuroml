package org.geppetto.model.neuroml.modelInterpreterUtils;

import org.geppetto.core.model.GeppettoModelAccess;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.model.GeppettoLibrary;
import org.geppetto.model.neuroml.utils.ModelInterpreterUtils;
import org.geppetto.model.neuroml.utils.Resources;
import org.geppetto.model.neuroml.utils.ResourcesDomainType;
import org.geppetto.model.types.ArrayType;
import org.geppetto.model.types.CompositeType;
import org.geppetto.model.types.ConnectionType;
import org.geppetto.model.types.ImportType;
import org.geppetto.model.types.Type;
import org.geppetto.model.types.TypesPackage;
import org.geppetto.model.util.GeppettoVisitingException;
import org.geppetto.model.util.PointerUtility;
import org.geppetto.model.values.Connection;
import org.geppetto.model.values.Connectivity;
import org.geppetto.model.values.Text;
import org.geppetto.model.variables.Variable;
import org.lemsml.jlems.core.sim.ContentError;
import org.lemsml.jlems.core.sim.LEMSException;
import org.lemsml.jlems.core.type.Component;
import org.neuroml.model.Cell;
import org.neuroml.model.util.NeuroMLException;

public class PopulateContinuousProjectionTypes extends APopulateProjectionTypes
{

	public PopulateContinuousProjectionTypes(PopulateTypes populateTypes, GeppettoModelAccess access, GeppettoLibrary library)
	{
		super(populateTypes, access, library);
	}

	/* (non-Javadoc)
	 * @see org.geppetto.model.neuroml.modelInterpreterUtils.APopulateProjectionTypes#resolveProjectionImportType(org.lemsml.jlems.core.type.Component, org.geppetto.model.types.ImportType)
	 */
	@Override
	public Type resolveProjectionImportType(Component projection, ImportType importType) throws ModelInterpreterException
	{
		try
		{
			super.resolveProjectionImportType(projection, importType);

			// Iterate over all the children. Most of them are connections
			for(Component projectionChild : projection.getStrictChildren())
			{
				if(projectionChild.getComponentType().isOrExtends(Resources.CONTINUOUS_CONNECTION.getId()))
				{
					projectionType.getVariables().add(extractConnection(projectionChild, prePopulationType, prePopulationVariable, postPopulationType, postPopulationVariable));
				}
				else
				{
					CompositeType anonymousCompositeType = populateTypes.extractInfoFromComponent(projectionChild);
					if(anonymousCompositeType != null)
					{
						Variable variable = variablesFactory.createVariable();
						NeuroMLModelInterpreterUtils.initialiseNodeFromComponent(variable, projectionChild);
						variable.getAnonymousTypes().add(anonymousCompositeType);
						projectionType.getVariables().add(variable);
					}
				}
			}

			return projectionType;
		}
		catch(NumberFormatException | NeuroMLException | LEMSException | GeppettoVisitingException e)
		{
			throw new ModelInterpreterException(e);
		}
	}

	/**
	 * @param projectionChild
	 * @param prePopulationType
	 * @param prePopulationVariable
	 * @param postPopulationType
	 * @param postPopulationVariable
	 * @return
	 * @throws ModelInterpreterException
	 */
	private Variable extractConnection(Component projectionChild, ArrayType prePopulationType, Variable prePopulationVariable, ArrayType postPopulationType, Variable postPopulationVariable)
            throws ModelInterpreterException, GeppettoVisitingException
	{
		ConnectionType connectionType = (ConnectionType) populateTypes.getTypeFactory().getSuperType(ResourcesDomainType.CONNECTION);
                connectionType.getSuperType().add(this.geppettoModelAccess.getType(TypesPackage.Literals.CONNECTION_TYPE));
		Connection connection = valuesFactory.createConnection();
		connection.setConnectivity(Connectivity.DIRECTIONAL);
        String weight = null;

		try
		{            
			String preSegmentId = projectionChild.hasAttribute("preSegment") ? projectionChild.getAttributeValue("preSegment") : "0";
			String postSegmentId = projectionChild.hasAttribute("postSegment") ? projectionChild.getAttributeValue("postSegment") : "0";
            
			String preFractionAlong = projectionChild.hasAttribute("preFractionAlong") ? projectionChild.getAttributeValue("preFractionAlong") : "0.5";
			String postFractionAlong = projectionChild.hasAttribute("preFractionAlong") ? projectionChild.getAttributeValue("postFractionAlong") : "0.5";
            
			String preCell = projectionChild.getComponentType().isOrExtends(Resources.CONTINUOUS_CONNECTION_INSTANCE.getId()) 
                                ? ModelInterpreterUtils.parseCellRefStringForCellNum(projectionChild.getAttributeValue("preCell")) 
                                : projectionChild.getAttributeValue("preCell");
            
			String postCell = projectionChild.getComponentType().isOrExtends(Resources.CONTINUOUS_CONNECTION_INSTANCE.getId()) 
                                ? ModelInterpreterUtils.parseCellRefStringForCellNum(projectionChild.getAttributeValue("postCell")) 
                                : projectionChild.getAttributeValue("postCell");
            
			if(preCell != null)
			{
				connection.setA(PointerUtility.getPointer(prePopulationVariable, prePopulationType, Integer.parseInt(preCell)));
				if(preSegmentId != null)
				{
					Cell neuroMLCell = this.populateTypes.getGeppettoCellTypesMap().get(prePopulationType.getArrayType());
					connection.getA().setPoint(NeuroMLModelInterpreterUtils.getPointAtFractionAlong(neuroMLCell, preSegmentId,preFractionAlong));
				}
			}
			if(postCell != null)
			{
				connection.setB(PointerUtility.getPointer(postPopulationVariable, postPopulationType, Integer.parseInt(postCell)));
				if(postSegmentId != null)
				{
					Cell neuroMLCell = this.populateTypes.getGeppettoCellTypesMap().get(postPopulationType.getArrayType());
					connection.getB().setPoint(NeuroMLModelInterpreterUtils.getPointAtFractionAlong(neuroMLCell,postSegmentId,postFractionAlong));
				}
			}
		}
		catch(ContentError | NumberFormatException | NeuroMLException e)
		{
			throw new ModelInterpreterException(e);
		}

		Variable variable = variablesFactory.createVariable();
		NeuroMLModelInterpreterUtils.initialiseNodeFromComponent(variable, projectionChild);
		variable.getTypes().add(connectionType);
		variable.getInitialValues().put(connectionType, connection);
        
        if(projectionChild.getComponentType().isOrExtends(Resources.CONTINUOUS_CONNECTION_INSTANCE_W.getId())) {
            
            Text weightValue = valuesFactory.createText();
            weightValue.setText(weight);
            Variable weightVar = variablesFactory.createVariable();
            NeuroMLModelInterpreterUtils.initialiseNodeFromString(weightVar, "weight");
            variable.getInitialValues().put(geppettoModelAccess.getType(TypesPackage.Literals.TEXT_TYPE), weightValue);
		}
        
		return variable;

	}

}
