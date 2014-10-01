package org.geppetto.model.neuroml.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.model.quantities.PhysicalQuantity;
import org.geppetto.core.model.quantities.Quantity;
import org.geppetto.core.model.runtime.CompositeNode;
import org.geppetto.core.model.runtime.FunctionNode;
import org.geppetto.core.model.runtime.ParameterSpecificationNode;
import org.geppetto.core.model.values.FloatValue;
import org.lemsml.jlems.core.sim.ContentError;
import org.lemsml.jlems.core.type.ComponentType;
import org.lemsml.jlems.core.type.dynamics.DerivedVariable;
import org.neuroml.model.HHRate;
import org.neuroml.model.HHTime;
import org.neuroml.model.HHVariable;

public class PopulateModelTreeUtils {
	
	
	
	public ParameterSpecificationNode createParameterSpecificationNode(Resources name, String id, String value){
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
	
	public CompositeNode createRateGateNode(Resources name, String id, HHRate rate, NeuroMLAccessUtility neuroMLAccessUtility,
			ModelWrapper model) throws ModelInterpreterException, ContentError{
		
		CompositeNode rateGateNode = new CompositeNode(name.get(), id);
			
		if (rate.getType() != null){
			ComponentType typeRate = (ComponentType) neuroMLAccessUtility.getComponent(rate.getType(), model, Resources.COMPONENT_TYPE);
		
			FunctionNode  forwardRateFunctionNode = new FunctionNode(rate.getType(), rate.getType()); 
			for (DerivedVariable derivedVariables:typeRate.getDynamics().getDerivedVariables()){
				forwardRateFunctionNode.setExpression(derivedVariables.getValueExpression());
				//derivedVariables.getName();
				//TODO: Do we want to store the dimension and the exposure? 										
				//derivedVariables.getDimension();
				//derivedVariables.getExposure();
			}
			rateGateNode.addChild(forwardRateFunctionNode);
		}
		if (rate.getMidpoint() != null){
			rateGateNode.addChild(this.createParameterSpecificationNode(Resources.MIDPOINT, "midPoint", rate.getMidpoint()));
		}
		if (rate.getRate() != null){
			rateGateNode.addChild(this.createParameterSpecificationNode(Resources.RATE, "rate", rate.getRate()));
		}
		if (rate.getScale() != null){
			rateGateNode.addChild(this.createParameterSpecificationNode(Resources.SCALE, "scale", rate.getScale()));
		}
		
		return rateGateNode;
	}

	public CompositeNode createSteadyStateNode(Resources name, String id, HHVariable variable, NeuroMLAccessUtility neuroMLAccessUtility,
			ModelWrapper model) throws ModelInterpreterException, ContentError{
		
		CompositeNode steadyStateNode = new CompositeNode(name.get(), id);
			
		if (variable.getType() != null){
			ComponentType typeSteadyState = (ComponentType) neuroMLAccessUtility.getComponent(variable.getType(), model, Resources.COMPONENT_TYPE);
		
			FunctionNode  steadyStateFunctionNode = new FunctionNode(variable.getType(), variable.getType()); 
			for (DerivedVariable derivedVariables:typeSteadyState.getDynamics().getDerivedVariables()){
				steadyStateFunctionNode.setExpression(derivedVariables.getValueExpression());
				//derivedVariables.getName();
				//TODO: Do we want to store the dimension and the exposure? 										
				//derivedVariables.getDimension();
				//derivedVariables.getExposure();
			}
			steadyStateNode.addChild(steadyStateFunctionNode);
		}
		if (variable.getMidpoint() != null){
			steadyStateNode.addChild(this.createParameterSpecificationNode(Resources.MIDPOINT, "midPoint", variable.getMidpoint()));
		}
		if (variable.getRate() != null){
			steadyStateNode.addChild(this.createParameterSpecificationNode(Resources.RATE, "rate", Float.toString(variable.getRate())));
		}
		if (variable.getScale() != null){
			steadyStateNode.addChild(this.createParameterSpecificationNode(Resources.SCALE, "scale", variable.getScale()));
		}
		
		return steadyStateNode;
	}
	
	public CompositeNode createTimeCourseNode(Resources name, String id, HHTime timeCourse, NeuroMLAccessUtility neuroMLAccessUtility,
			ModelWrapper model) throws ModelInterpreterException, ContentError{
		
		CompositeNode timeCourseNode = new CompositeNode(name.get(), id);
		
		if (timeCourse.getType() != null){
			ComponentType typeTimeCourse = (ComponentType) neuroMLAccessUtility.getComponent(timeCourse.getType(), model, Resources.COMPONENT_TYPE);
			
			FunctionNode  timeCourseFunctionNode = new FunctionNode(timeCourse.getType(), timeCourse.getType()); 
			for (DerivedVariable derivedVariables:typeTimeCourse.getDynamics().getDerivedVariables()){
				timeCourseFunctionNode.setExpression(derivedVariables.getValueExpression());
				//derivedVariables.getName();
				//TODO: Do we want to store the dimension and the exposure? 										
				//derivedVariables.getDimension();
				//derivedVariables.getExposure();
			}
			timeCourseNode.addChild(timeCourseFunctionNode);
		}
		
		if (timeCourse.getMidpoint() != null){
			timeCourseNode.addChild(this.createParameterSpecificationNode(Resources.MIDPOINT, "midPoint", timeCourse.getMidpoint()));
		}
		if (timeCourse.getRate() != null){
			timeCourseNode.addChild(this.createParameterSpecificationNode(Resources.RATE, "rate", timeCourse.getRate()));
		}
		if (timeCourse.getScale() != null){
			timeCourseNode.addChild(this.createParameterSpecificationNode(Resources.SCALE, "scale", timeCourse.getScale()));
		}
		if (timeCourse.getTau() != null){
			timeCourseNode.addChild(this.createParameterSpecificationNode(Resources.TAU, "tau", timeCourse.getTau()));
		}
		
		
		return timeCourseNode;
	}
	
	
}
