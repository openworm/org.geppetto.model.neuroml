package org.geppetto.model.neuroml.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geppetto.core.model.quantities.PhysicalQuantity;
import org.geppetto.core.model.runtime.ParameterSpecificationNode;
import org.geppetto.core.model.values.FloatValue;

public class PopulateModelTreeUtils {
	
	public static ParameterSpecificationNode createParameterSpecificationNode(Resources name, String id, String value){
		ParameterSpecificationNode parameterSpecificationNode = new ParameterSpecificationNode(name.get(), id);
		
		
		String regExp = "\\s*([0-9]*\\.?[0-9]*[eE]?[-+]?[0-9]+)?\\s*(\\w*)";
		
		Pattern pattern = Pattern.compile(regExp);
		Matcher matcher = pattern.matcher(value);
		
		PhysicalQuantity physicalQuantity = new PhysicalQuantity();
		if (matcher.find()) {
			physicalQuantity.setValue(new FloatValue(Float.parseFloat(matcher.group(1))));
			if (matcher.group(1)!=null){
				physicalQuantity.setUnit(matcher.group(2));
			}
		}
		parameterSpecificationNode.setValue(physicalQuantity);
		
		return parameterSpecificationNode;
	}
	
}
