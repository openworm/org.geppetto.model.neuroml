package org.geppetto.model.neuroml.features;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.features.ISetParameterFeature;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.services.GeppettoFeature;
import org.geppetto.model.VariableValue;
import org.geppetto.model.values.Quantity;
import org.lemsml.jlems.core.sim.ContentError;
import org.lemsml.jlems.core.type.Component;

/**
 * Set a Lems Parameter (State Variable)
 * 
 * @author Adrian Quintana (adrian.perez@ucl.ac.uk)
 * 
 */
public class LEMSParametersFeature implements ISetParameterFeature
{

	private static Log logger = LogFactory.getLog(LEMSParametersFeature.class);

	private GeppettoFeature type = GeppettoFeature.SET_PARAMETERS_FEATURE;

	@Override
	public GeppettoFeature getType()
	{
		return type;
	}

	@Override
	public void setParameter(VariableValue variableValue) throws ModelInterpreterException
	{

		// Get the parameter id
		String paramName = variableValue.getPointer().getElements().get(variableValue.getPointer().getElements().size() - 1).getVariable().getId();

		// Get the lems component stored in the parent type
		Component component = (Component) variableValue.getPointer().getElements().get(variableValue.getPointer().getElements().size() - 2).getType().getDomainModel().getDomainModel();

		// Get the parameter value
		String value = String.valueOf(((Quantity) variableValue.getValue()).getValue());

		// Set the new value to the param
		try
		{
			component.getParamValue(paramName).setDoubleValue(Double.parseDouble(value));
		}
		catch(NumberFormatException | ContentError e)
		{
			throw new ModelInterpreterException(e);
		}

		logger.info("The value of the LEMS parameter " + paramName + " has changed, new value is " + value);
	}
}
