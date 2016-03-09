package org.geppetto.model.neuroml.features;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.features.ISetParameterFeature;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.services.GeppettoFeature;
import org.geppetto.model.VariableValue;
import org.geppetto.model.neuroml.utils.ModelInterpreterUtils;
import org.geppetto.model.values.PhysicalQuantity;
import org.geppetto.model.values.Quantity;
import org.geppetto.model.values.Value;
import org.lemsml.jlems.core.expression.ParseError;
import org.lemsml.jlems.core.sim.ContentError;
import org.lemsml.jlems.core.sim.LEMSException;
import org.lemsml.jlems.core.type.Attribute;
import org.lemsml.jlems.core.type.Component;
import org.lemsml.jlems.core.type.Dimension;
import org.lemsml.jlems.core.type.Lems;
import org.lemsml.jlems.core.type.LemsCollection;
import org.lemsml.jlems.core.type.Unit;
import org.neuroml.export.utils.Utils;
import org.neuroml.model.util.NeuroMLConverter;
import org.neuroml.model.util.NeuroMLException;

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
		Quantity parameterValue = (Quantity) variableValue.getValue();
		String value = String.valueOf(parameterValue.getValue());
		//String units = parameterValue.getUnit().getUnit();

		// Set the new value to the param
		try
		{
			// Get a lems doc to get the units
			Lems lems = Utils.readLemsNeuroMLFile(NeuroMLConverter.convertNeuroML2ToLems("<neuroml></neuroml>")).getLems();
			
			// Extract the unit from the current value
			String regExp = "\\s*([0-9-]*\\.?[0-9]*[eE]?[-+]?[0-9]+)?\\s*(\\w*)";
			Pattern pattern = Pattern.compile(regExp);
			Matcher matcher = pattern.matcher(component.getStringValue(paramName));

			if(matcher.find())
			{
				component.getParamValue(paramName).setValue(value + matcher.group(2), lems.getUnits());
			}
			else{
				throw new ModelInterpreterException("Problem setting value");
			}
			
		}
		catch(NumberFormatException | LEMSException e)
		{
			throw new ModelInterpreterException(e);
		}

		logger.info("The value of the LEMS parameter " + paramName + " has changed, new value is " + value);
	}
}
