package org.geppetto.model.neuroml.features;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.features.ISetParameterFeature;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.services.GeppettoFeature;
import org.geppetto.model.VariableValue;
import org.geppetto.model.values.Quantity;
import org.lemsml.jlems.core.sim.LEMSException;
import org.lemsml.jlems.core.type.Component;
import org.lemsml.jlems.core.type.Lems;
import org.lemsml.jlems.core.type.ParamValue;
import org.lemsml.jlems.core.xml.XMLAttribute;
import org.neuroml.export.utils.Utils;
import org.neuroml.model.util.NeuroMLConverter;

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

    public boolean saveSpikes() {
        return true;
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
				String unit = matcher.group(2);
				ParamValue paramValue = component.getParamValue(paramName);
				paramValue.setValue(value + unit, lems.getUnits());
				
				//create attribute object with new value and unit
				XMLAttribute xml = new XMLAttribute(paramName,value.toString()+unit);
				
				//replace old xmlattribute with new one that has updated value/unit
				component.getAttributes().remove(paramName);
				component.getAttributes().add(xml);
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
