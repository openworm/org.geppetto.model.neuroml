package org.geppetto.model.neuroml.features;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.features.ISetParameterFeature;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.model.runtime.ParameterSpecificationNode;
import org.geppetto.core.model.values.AValue;
import org.geppetto.core.model.values.DoubleValue;
import org.geppetto.core.services.GeppettoFeature;
import org.geppetto.core.services.registry.ServicesRegistry;
import org.geppetto.model.neuroml.utils.LEMSAccessUtility;
import org.geppetto.model.neuroml.utils.modeltree.PopulateModelTree;
import org.lemsml.jlems.core.expression.ParseError;
import org.lemsml.jlems.core.sim.ContentError;
import org.lemsml.jlems.core.type.Component;
import org.lemsml.jlems.core.type.DimensionalQuantity;
import org.lemsml.jlems.core.type.Lems;
import org.lemsml.jlems.core.type.ParamValue;
import org.lemsml.jlems.core.type.QuantityReader;
import org.neuroml.model.Base;

public class LEMSParametersFeature implements ISetParameterFeature{

	private static Log logger = LogFactory.getLog(LEMSParametersFeature.class);
	
	private PopulateModelTree populateModelTree = new PopulateModelTree();
	
	private ModelWrapper model;

	private GeppettoFeature type = GeppettoFeature.SET_PARAMETERS_FEATURE;

	public LEMSParametersFeature(PopulateModelTree populateModelTree, ModelWrapper model){
		this.populateModelTree = populateModelTree;
		this.model = model;
	}
	
	@Override
	public GeppettoFeature getType()
	{
		return type;
	}

	@Override
	public void setParameter(Map<String, String> parameters) throws ModelInterpreterException
	{
		Map<String, ParameterSpecificationNode> modelParameters = this.populateModelTree.getParametersNode();

		Set<String> paramValues = parameters.keySet();
		Iterator<String> it = paramValues.iterator();
		while(it.hasNext())
		{
			String newValue = it.next();
			AValue value = new DoubleValue(Double.valueOf(parameters.get(newValue)));
			ParameterSpecificationNode node = modelParameters.get(newValue);
			node.getValue().setValue(value);
			node.setModified(true);

			// retrieve NeuroML object instance associated with param node
			Object instance = this.populateModelTree.getParametersNodeToObjectsMap().get(node);
			// retrieve setter method from instance associated with param node
			Method method = (Method)this.populateModelTree.getParametersNodeToMethodsMap().get(node);
			try
			{
				// invoke setter method passing instance and new value
				method.invoke(instance, value.toString());
			}
			catch(Exception e)
			{
				throw new ModelInterpreterException(e);
			}

			try
			{
				// Change the parameter value also in the LEMS model
				Lems lems = (Lems) model.getModel(ServicesRegistry.getModelFormat("LEMS"));
				lems.setResolveModeLoose();
				lems.deduplicate();
				lems.resolve();
				lems.evaluateStatic();
				Component comp = LEMSAccessUtility.findLEMSComponent(lems.getComponents().getContents(), ((Base) instance).getId());
				//unspeakable things happening, going from a method name to parameter name
				String paramName = Character.toLowerCase(method.getName().charAt(3))+method.getName().substring(4);
				ParamValue lemsParam = comp.getParamValue(paramName); 
				String valueWithUnit=node.getValue().getValue().toString() +node.getValue().getUnit();
				DimensionalQuantity dq = QuantityReader.parseValue(valueWithUnit, lems.getUnits());
				lemsParam.setDoubleValue(dq.getDoubleValue());
				logger.info("The value of the LEMS parameter "+paramName+" has changed, new value is "+valueWithUnit);
			}
			catch(ContentError e)
			{
				throw new ModelInterpreterException(e);
			}
			catch(ParseError e)
			{
				throw new ModelInterpreterException(e);
			}

		}
	}
}
