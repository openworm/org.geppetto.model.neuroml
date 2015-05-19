package org.geppetto.model.neuroml.utils.modeltree;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geppetto.core.model.runtime.ParameterSpecificationNode;
import org.geppetto.core.model.runtime.TextMetadataNode;
import org.geppetto.core.model.values.AValue;
import org.geppetto.core.model.values.FloatValue;


public class PopulateNodesModelTreeUtils {
	
	//Parameter Specification Node Utils	
	public static ParameterSpecificationNode createParameterSpecificationNode(String id, String name, String value){
		if (value != null){
			String regExp = "\\s*([0-9-]*\\.?[0-9]*[eE]?[-+]?[0-9]+)?\\s*(\\w*)";
			
			Pattern pattern = Pattern.compile(regExp);
			Matcher matcher = pattern.matcher(value);
			
			if (matcher.find()) {
				return createParameterSpecificationNode(id, name, matcher.group(1), matcher.group(2));
			}
		}
		return null;
	}
	
	public static ParameterSpecificationNode createParameterSpecificationNode(String id, String name, String value, String unit){
		if (value != null){
			return new ParameterSpecificationNode(id, name, new FloatValue(Float.parseFloat(value)), unit);
		}
		return null;
	}
	
	//TextMetadata Node Utils
	public static TextMetadataNode createTextMetadataNode(String id, String name, AValue aValue){
		if (aValue.getStringValue() != null && aValue.getStringValue() != ""){
			return new TextMetadataNode(id, name, aValue);
		}
		return null;
	}

}
