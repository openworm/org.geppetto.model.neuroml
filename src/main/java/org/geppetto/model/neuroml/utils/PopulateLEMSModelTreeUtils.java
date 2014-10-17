package org.geppetto.model.neuroml.utils;

import org.geppetto.core.model.runtime.CompositeNode;
import org.geppetto.core.model.runtime.FunctionNode;
import org.lemsml.jlems.core.sim.ContentError;
import org.lemsml.jlems.core.type.ComponentType;
import org.lemsml.jlems.core.type.DerivedParameter;
import org.lemsml.jlems.core.type.dynamics.ConditionalDerivedVariable;
import org.lemsml.jlems.core.type.dynamics.DerivedVariable;
import org.lemsml.jlems.core.type.dynamics.Dynamics;

public class PopulateLEMSModelTreeUtils {
	

	//private PopulateModelTreeUtils populateModelTreeUtils = new PopulateModelTreeUtils();
	
	//TODO: Improve to parse all the attribute in a component type 	
	public static CompositeNode createCompositeNodeFromComponentType(String name, String id, ComponentType componentType) throws ContentError{
		CompositeNode compositeNode = new CompositeNode(name, id);
		
		
		compositeNode.addChild(PopulateModelTreeUtils.createParameterSpecificationNode(Resources.NAME.get(), Resources.NAME.getId(), componentType.getName()));
		//TODO: This is needed?
		if (componentType.getExtends() != null){
			compositeNode.addChild(createCompositeNodeFromComponentType(Resources.EXTENDS.get(), Resources.EXTENDS.getId(), componentType.getExtends()));
		}
		compositeNode.addChild(PopulateModelTreeUtils.createParameterSpecificationNode(Resources.DESCRIPTION.get(), Resources.DESCRIPTION.getId(), componentType.getDescription()));
		
		
		Dynamics dynamics = componentType.getDynamics();
		if (dynamics != null){
			CompositeNode dynamicsNode = new CompositeNode(Resources.DYNAMICS.get(), Resources.DYNAMICS.getId());
			
			
			for (DerivedVariable derivedVariable : dynamics.getDerivedVariables()){
				
				if (derivedVariable.getValueExpression() != null){
					FunctionNode  functionNode = new FunctionNode(derivedVariable.getName(), derivedVariable.getName());
					functionNode.setExpression(derivedVariable.getValueExpression());
					dynamicsNode.addChild(functionNode);
				}
				//TODO: Do we want to store the dimension? 										
//				derivedVariable.getDimensionString();
				
			}
			
			//TODO: We need to implement events
			for (ConditionalDerivedVariable conditionalDerivedVariable : dynamics.getConditionalDerivedVariables()){
				
			}
			
			compositeNode.addChild(dynamicsNode);
		}
		
		//TODO: In a Parameter the value comes from the instance (component)
//		for (Parameter parameter : componentType.getParameters()){
//			compositeNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.PARAMETER, Resources.PARAMETER + "_" +  parameter.getName(), ));
//			parameter.getName();
//			parameter.getDimension();
//		}
		
		for (DerivedParameter derivedParameter : componentType.getDerivedParameters()){
			if (derivedParameter.getValue() != null){
				compositeNode.addChild(PopulateModelTreeUtils.createParameterSpecificationNode(derivedParameter.getName(), derivedParameter.getName(), derivedParameter.getValue(), derivedParameter.getDimension().getName()));
			}
		}
		
		return compositeNode;
	}
	
	
	
	
}
