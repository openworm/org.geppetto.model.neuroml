package org.geppetto.model.neuroml.utils;

import org.geppetto.core.model.quantities.PhysicalQuantity;
import org.geppetto.core.model.runtime.ParameterSpecificationNode;
import org.geppetto.core.model.values.FloatValue;

public class PopulateModelTreeUtils {
	
	public static ParameterSpecificationNode createParameterSpecificationNode(Resources name, String id, String value){
		ParameterSpecificationNode parameterSpecificationNode = new ParameterSpecificationNode(name.get(), id);
		
		String[] valueArray = new String[2];
		if (value.contains(" ")){
			valueArray = value.split(" ");
		}
		else{
			
			for (int i = 0; i < value.length(); i++){
			    if (!Character.isDigit(value.charAt(i)) && value.charAt(i)!='.' &&  value.charAt(i)!='-' && value.charAt(i)!='e' || (value.charAt(i)=='e' &&!Character.isDigit(value.charAt(i+1)) &&  value.charAt(i+1)!='-')){
			    	valueArray[0] = value.substring(0,i);
			    	valueArray[1] = value.substring(i);
			    	break;
			    }
			}
		}
		PhysicalQuantity physicalQuantity = new PhysicalQuantity();
		physicalQuantity.setUnit(valueArray[1]);
		physicalQuantity.setValue(new FloatValue(Float.parseFloat(valueArray[0])));
		parameterSpecificationNode.setValue(physicalQuantity);
		
		return parameterSpecificationNode;
	}
	
}
