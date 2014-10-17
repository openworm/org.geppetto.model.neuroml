package org.geppetto.model.neuroml.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.model.quantities.PhysicalQuantity;
import org.geppetto.core.model.quantities.Quantity;
import org.geppetto.core.model.runtime.ANode;
import org.geppetto.core.model.runtime.CompositeNode;
import org.geppetto.core.model.runtime.FunctionNode;
import org.geppetto.core.model.runtime.ParameterNode;
import org.geppetto.core.model.runtime.ParameterSpecificationNode;
import org.geppetto.core.model.runtime.TextMetadataNode;
import org.geppetto.core.model.values.FloatValue;
import org.geppetto.core.model.values.IntValue;
import org.geppetto.core.model.values.StringValue;
import org.lemsml.jlems.core.sim.ContentError;
import org.lemsml.jlems.core.type.ComponentType;
import org.lemsml.jlems.core.type.DerivedParameter;
import org.lemsml.jlems.core.type.Parameter;
import org.lemsml.jlems.core.type.dynamics.ConditionalDerivedVariable;
import org.lemsml.jlems.core.type.dynamics.DerivedVariable;
import org.lemsml.jlems.core.type.dynamics.Dynamics;
import org.neuroml.model.AdExIaFCell;
import org.neuroml.model.Annotation;
import org.neuroml.model.Base;
import org.neuroml.model.BaseCell;
import org.neuroml.model.BiophysicalProperties;
import org.neuroml.model.Cell;
import org.neuroml.model.ChannelDensity;
import org.neuroml.model.DecayingPoolConcentrationModel;
import org.neuroml.model.ExplicitInput;
import org.neuroml.model.ExtracellularProperties;
import org.neuroml.model.FitzHughNagumoCell;
import org.neuroml.model.FixedFactorConcentrationModel;
import org.neuroml.model.GateHHRates;
import org.neuroml.model.GateHHRatesInf;
import org.neuroml.model.GateHHRatesTau;
import org.neuroml.model.GateHHTauInf;
import org.neuroml.model.GateHHUndetermined;
import org.neuroml.model.HHRate;
import org.neuroml.model.HHTime;
import org.neuroml.model.HHVariable;
import org.neuroml.model.IafCell;
import org.neuroml.model.IafRefCell;
import org.neuroml.model.IafTauCell;
import org.neuroml.model.IafTauRefCell;
import org.neuroml.model.InitMembPotential;
import org.neuroml.model.InputList;
import org.neuroml.model.Instance;
import org.neuroml.model.IntracellularProperties;
import org.neuroml.model.IonChannel;
import org.neuroml.model.IzhikevichCell;
import org.neuroml.model.MembraneProperties;
import org.neuroml.model.Network;
import org.neuroml.model.Population;
import org.neuroml.model.Projection;
import org.neuroml.model.Q10Settings;
import org.neuroml.model.Region;
import org.neuroml.model.Resistivity;
import org.neuroml.model.Species;
import org.neuroml.model.SpecificCapacitance;
import org.neuroml.model.SpikeThresh;
import org.neuroml.model.Standalone;
import org.neuroml.model.SynapticConnection;
import org.w3c.dom.Element;

public class PopulateLEMSModelTreeUtils {
	

	private PopulateModelTreeUtils populateModelTreeUtils = new PopulateModelTreeUtils();
	
	//TODO: Improve to parse all the attribute in a component type 	
	public CompositeNode createCompositeNodeFromComponentType(String name, String id, ComponentType componentType) throws ContentError{
		CompositeNode compositeNode = new CompositeNode(name, id);
		
		
		compositeNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.NAME.get(), Resources.NAME.getId(), componentType.getName()));
		//TODO: This is needed?
		if (componentType.getExtends() != null){
			compositeNode.addChild(createCompositeNodeFromComponentType(Resources.EXTENDS.get(), Resources.EXTENDS.getId(), componentType.getExtends()));
		}
		compositeNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.DESCRIPTION.get(), Resources.DESCRIPTION.getId(), componentType.getDescription()));
		
		
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
				compositeNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(derivedParameter.getName(), derivedParameter.getName(), derivedParameter.getValue(), derivedParameter.getDimension().getName()));
			}
		}
		
		return compositeNode;
	}
	
	
	
	
}
