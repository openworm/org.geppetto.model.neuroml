package org.geppetto.model.neuroml.utils;

import org.geppetto.core.model.quantities.PhysicalQuantity;
import org.geppetto.core.model.runtime.ParameterSpecificationNode;
import org.geppetto.core.model.values.FloatValue;

public class PopulateModelTreeUtils {
	
	public static ParameterSpecificationNode createParameterSpecificationNode(Resources name, String id, String value){
		ParameterSpecificationNode parameterSpecificationNode = new ParameterSpecificationNode(name.get(), id);
		
		String[] valueArray = value.split(" ");
		if(valueArray.length>1){
			PhysicalQuantity physicalQuantity = new PhysicalQuantity();
			physicalQuantity.setUnit(valueArray[1]);
			physicalQuantity.setValue(new FloatValue(Float.parseFloat(valueArray[0])));
			parameterSpecificationNode.setValue(physicalQuantity);
		}
		
		return parameterSpecificationNode;
	}
	
}
