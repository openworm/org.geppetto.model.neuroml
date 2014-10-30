package org.geppetto.model.neuroml.utils.modeltree;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geppetto.core.model.quantities.PhysicalQuantity;
import org.geppetto.core.model.runtime.ParameterSpecificationNode;
import org.geppetto.core.model.runtime.TextMetadataNode;
import org.geppetto.core.model.values.AValue;
import org.geppetto.core.model.values.FloatValue;


public class PopulateNodesModelTreeUtils {
	
	public static ParameterSpecificationNode createParameterSpecificationNode(String name, String id, String value){
		if (value != null){
//			ParameterSpecificationNode parameterSpecificationNode = new ParameterSpecificationNode(name.get(), id);
			
			String regExp = "\\s*([0-9-]*\\.?[0-9]*[eE]?[-+]?[0-9]+)?\\s*(\\w*)";
			
			Pattern pattern = Pattern.compile(regExp);
			Matcher matcher = pattern.matcher(value);
			
//			PhysicalQuantity physicalQuantity = new PhysicalQuantity();
			if (matcher.find()) {
//				physicalQuantity.setValue(new FloatValue(Float.parseFloat(matcher.group(1))));
//				if (matcher.group(1)!=null){
//					physicalQuantity.setUnit(matcher.group(2));
//				}
				return createParameterSpecificationNode(name, id, matcher.group(1), matcher.group(2));
			}
//			parameterSpecificationNode.setValue(physicalQuantity);
			
//			return parameterSpecificationNode;
		}
		return null;
	}
	public static ParameterSpecificationNode createParameterSpecificationNode(String name, String id, String value, String unit){
		if (value != null){
			ParameterSpecificationNode parameterSpecificationNode = new ParameterSpecificationNode(name, id);
			
			
			PhysicalQuantity physicalQuantity = new PhysicalQuantity();
			physicalQuantity.setValue(new FloatValue(Float.parseFloat(value)));
			physicalQuantity.setUnit(unit);
			parameterSpecificationNode.setValue(physicalQuantity);
			
			return parameterSpecificationNode;
		}
		return null;
	}
	
	public static TextMetadataNode createTextMetadataNode(String name, String id, AValue aValue){
		if (aValue.getStringValue() != null && aValue.getStringValue() != ""){
			TextMetadataNode textMetadataNode = new TextMetadataNode(name, id);
			textMetadataNode.setValue(aValue);
			return textMetadataNode;
		}
		return null;
	}
	

}
