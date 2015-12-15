/*******************************************************************************
 * The MIT License (MIT)
 * 
 * Copyright (c) 2011 - 2015 OpenWorm.
 * http://openworm.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT License
 * which accompanies this distribution, and is available at
 * http://opensource.org/licenses/MIT
 *
 * Contributors:
 *     	OpenWorm - http://openworm.org/people.html
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights 
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
 * copies of the Software, and to permit persons to whom the Software is 
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE 
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/

package org.geppetto.model.neuroml.utils.modeltree;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.geppetto.core.model.GeppettoModelAccess;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.model.neuroml.services.ModelInterpreterUtils;
import org.geppetto.model.neuroml.utils.Resources;
import org.geppetto.model.types.CompositeType;
import org.geppetto.model.types.HTMLType;
import org.geppetto.model.types.TypesFactory;
import org.geppetto.model.types.TypesPackage;
import org.geppetto.model.util.GeppettoVisitingException;
import org.geppetto.model.values.Argument;
import org.geppetto.model.values.Dynamics;
import org.geppetto.model.values.Expression;
import org.geppetto.model.values.Function;
import org.geppetto.model.values.ValuesFactory;
import org.geppetto.model.variables.Variable;
import org.geppetto.model.variables.VariablesFactory;
import org.lemsml.jlems.core.type.Component;
import org.neuroml.export.info.InfoTreeCreator;
import org.neuroml.export.info.model.ExpressionNode;
import org.neuroml.export.info.model.InfoNode;
import org.neuroml.export.info.model.PlotNode;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.util.NeuroMLException;

/**
 * Populates the Model Tree of Aspect
 * 
 * @author Adrian Quintana (adrian.perez@ucl.ac.uk)
 * 
 */

public class PopulateSummaryNodesModelTreeUtils
{

	TypesFactory typeFactory = TypesFactory.eINSTANCE;
	VariablesFactory variablesFactory = VariablesFactory.eINSTANCE;
	ValuesFactory valuesFactory = ValuesFactory.eINSTANCE;

	GeppettoModelAccess access;
	NeuroMLDocument neuroml;

	public PopulateSummaryNodesModelTreeUtils(NeuroMLDocument neuroml, GeppettoModelAccess access)
	{
		// initialiseModelDescription();
		this.neuroml = neuroml;
		this.access = access;
	}

	public List<Variable> getSummaryVariables() throws ModelInterpreterException, GeppettoVisitingException, NeuroMLException
	{
		List<Variable> summaryVariables = new ArrayList<Variable>();
		// Summary2
		// Variable descriptionVariable = variablesFactory.createVariable();
		// descriptionVariable.setId(Resources.MODEL_DESCRIPTION.getId());
		// descriptionVariable.setName(Resources.MODEL_DESCRIPTION.get());
		//
		// descriptionVariable.getAnonymousTypes().add(populateSummaryNodesModelTreeUtils.createDescriptionNode((CompositeType) type));
		//
		// ((CompositeType) type).getVariables().add(descriptionVariable);

		// Summary
		Variable summaryVariable = variablesFactory.createVariable();
		summaryVariable.setId(Resources.SUMMARY.getId());
		summaryVariable.setName(Resources.SUMMARY.get());
		summaryVariable.getAnonymousTypes().add(createInfoNode(InfoTreeCreator.createInfoTree(neuroml)));
		summaryVariables.add(summaryVariable);
		
		return summaryVariables;
	}

	// Create Map of NeuroMlComponent and Nodes for description node
	// private Map<ResourcesDomainType, Map<Standalone, ANode>> modelDescriptionComponents;

	// public void initialiseModelDescription()
	// {
	// modelDescriptionComponents = new HashMap<ResourcesDomainType, Map<Standalone, ANode>>();
	// modelDescriptionComponents.put(ResourcesDomainType.NETWORK, new HashMap<Standalone, ANode>());
	// modelDescriptionComponents.put(ResourcesDomainType.POPULATION, new HashMap<Standalone, ANode>());
	// modelDescriptionComponents.put(ResourcesDomainType.SYNAPSE, new HashMap<Standalone, ANode>());
	// modelDescriptionComponents.put(ResourcesDomainType.CELL, new HashMap<Standalone, ANode>());
	// modelDescriptionComponents.put(ResourcesDomainType.PULSEGENERATOR, new HashMap<Standalone, ANode>());
	// modelDescriptionComponents.put(ResourcesDomainType.IONCHANNEL, new HashMap<Standalone, ANode>());
	// }
	//
	// public void addNodeToModelDescription(ResourcesDomainType domainType, Standalone standalone, ANode node)
	// {
	// Map<Standalone, ANode> modelDescriptionComponentsItem = modelDescriptionComponents.get(domainType);
	// if(!modelDescriptionComponentsItem.containsKey(standalone))
	// {
	// modelDescriptionComponentsItem.put(standalone, node);
	// }
	// }

	public HTMLType createDescriptionNode(CompositeType type) throws ModelInterpreterException
	{

		Component component = (Component) type.getDomainModel().getDomainModel();

		// Map<Standalone, ANode> networkComponents = modelDescriptionComponents.get(ResourcesDomainType.NETWORK);
		// Map<Standalone, ANode> populationComponents = modelDescriptionComponents.get(ResourcesDomainType.POPULATION);
		// Map<Standalone, ANode> cellComponents = modelDescriptionComponents.get(ResourcesDomainType.CELL);
		// Map<Standalone, ANode> ionChannelComponents = modelDescriptionComponents.get(ResourcesDomainType.IONCHANNEL);
		// Map<Standalone, ANode> synapseComponents = modelDescriptionComponents.get(ResourcesDomainType.SYNAPSE);
		// Map<Standalone, ANode> pulseGeneratorComponents = modelDescriptionComponents.get(ResourcesDomainType.PULSEGENERATOR);
		//
		// StringBuilder modelDescription = new StringBuilder();
		// modelDescription.append("<b>Model Summary</b><br/>");
		//
		// // FIXME: We need to extract the main component (target component) and extract the description from it in order to do this feature generic. This will wait until the instance/type refactor
		// // FIXME: We need to add something about how beautiful the network is and so on
		// if(networkComponents.size() > 0)
		// {
		// modelDescription.append("Description: ");
		// for(ANode node : networkComponents.values())
		// {
		// modelDescription.append("<a href=\"#\" instancePath=\"$" + node.getInstancePath() + "$\">" + node.getName() + "</a> ");
		// }
		// }
		// modelDescription.append("<br/><a target=\"_blank\" href=\"" + modelUrl.toString() + "\">NeuroML Source File</a><br/>");
		//
		// // FIXME: We should improve this once the instance/type refactor is done as we need the cell type
		// if(populationComponents.size() > 0)
		// {
		// modelDescription.append("<b>Populations</b><br/>");
		// for(Map.Entry<Standalone, ANode> node : populationComponents.entrySet())
		// {
		// modelDescription.append("Population " + node.getValue().getName() + ": ");
		// Population population = ((Population) node.getKey());
		//
		// for(Map.Entry<Standalone, ANode> cellNode : cellComponents.entrySet())
		// {
		// if(cellNode.getKey().getId().equals(population.getComponent()))
		// {
		// modelDescription.append("<a href=\"#\" instancePath=\"$" + cellNode.getValue().getInstancePath() + "$\">" + population.getInstance().size() + " "
		// + cellNode.getValue().getName() + "</a><br/>");
		// }
		// }
		// }
		// modelDescription.append("<br/>");
		// }
		//
		// if(cellComponents.size() > 0)
		// {
		// modelDescription.append("<b>Cells</b><br/>");
		// for(ANode node : cellComponents.values())
		// {
		// modelDescription.append("<a href=\"#\" instancePath=\"$" + node.getInstancePath() + "$\">" + node.getName() + "</a> ");
		// }
		// modelDescription.append("<br/>");
		// }
		//
		// if(ionChannelComponents.size() > 0)
		// {
		// modelDescription.append("<b>Channels</b><br/>");
		// for(ANode node : ionChannelComponents.values())
		// {
		// modelDescription.append("<a href=\"#\" instancePath=\"$" + node.getInstancePath() + "$\">" + node.getName() + "</a> ");
		// }
		// modelDescription.append("<br/>");
		// }
		//
		// if(synapseComponents.size() > 0)
		// {
		// modelDescription.append("<b>Synapses</b><br/>");
		// for(ANode node : synapseComponents.values())
		// {
		// modelDescription.append("<a href=\"#\" instancePath=\"$" + node.getInstancePath() + "$\">" + node.getName() + "</a> ");
		// }
		// modelDescription.append("<br/>");
		// }
		//
		// if(pulseGeneratorComponents.size() > 0)
		// {
		// // FIXME: Pulse generator? InputList? ExplicitList?
		// modelDescription.append("<b>Inputs</b><br/>");
		// for(ANode node : pulseGeneratorComponents.values())
		// {
		// modelDescription.append("<a href=\"#\" instancePath=\"$" + node.getInstancePath() + "$\">" + node.getName() + "</a> ");
		// }
		// modelDescription.append("<br/>");
		// }

		// return new HTMLMetadataNode(Resources.MODEL_DESCRIPTION.getId(), Resources.MODEL_DESCRIPTION.get(), new StringValue(modelDescription.toString()));
		return null;
	}

	public CompositeType createInfoNode(InfoNode node) throws ModelInterpreterException, GeppettoVisitingException
	{
		CompositeType summaryCompositeType = typeFactory.createCompositeType();
		summaryCompositeType.setId(Resources.SUMMARY.getId());
		summaryCompositeType.setName(Resources.SUMMARY.get());

		for(Map.Entry<String, Object> properties : node.getProperties().entrySet())
		{
			String keyProperties = properties.getKey();
			Object valueProperties = properties.getValue();
			if(!keyProperties.equals("ID"))
			{

				if(valueProperties == null)
				{
					summaryCompositeType.getVariables().add(ModelInterpreterUtils.createTextTypeVariable(keyProperties, "", access));
				}
				else if(valueProperties instanceof String)
				{
					summaryCompositeType.getVariables().add(ModelInterpreterUtils.createTextTypeVariable(keyProperties, (String) valueProperties, access));
				}
				else if(valueProperties instanceof BigInteger)
				{
					summaryCompositeType.getVariables().add(ModelInterpreterUtils.createTextTypeVariable(keyProperties, ((BigInteger) valueProperties).toString(), access));
				}
				else if(valueProperties instanceof Integer)
				{
					summaryCompositeType.getVariables().add(ModelInterpreterUtils.createTextTypeVariable(keyProperties, Integer.toString((Integer) valueProperties), access));
				}
				else if(valueProperties instanceof ExpressionNode)
				{
					ExpressionNode expressionNode = ((ExpressionNode) valueProperties);

					Argument argument = valuesFactory.createArgument();
					argument.setArgument("v");

					Expression expression = valuesFactory.createExpression();
					expression.setExpression(expressionNode.getExpression());

					Function function = valuesFactory.createFunction();
					function.setExpression(expression);
					function.getArguments().add(argument);

					Dynamics dynamics = valuesFactory.createDynamics();
					dynamics.setDynamics(function);

					Variable variable = variablesFactory.createVariable();
					variable.setId(PopulateGeneralModelTreeUtils.parseId(keyProperties));
					variable.setName(keyProperties);
					// variable.getInitialValues().put(access.getType(TypesPackage.Literals.DYNAMICS_TYPE), dynamics);
					variable.getTypes().add(access.getType(TypesPackage.Literals.DYNAMICS_TYPE));

					// AQP: How to model this?
					// PlotMetadataNode plotMetadataNode = expressionNode.getPlotMetadataNode();
					// if(plotMetadataNode != null)
					// {
					// functionNode.getPlotMetadata().put("PlotTitle", plotMetadataNode.getPlotTitle());
					// functionNode.getPlotMetadata().put("XAxisLabel", plotMetadataNode.getXAxisLabel());
					// functionNode.getPlotMetadata().put("YAxisLabel", plotMetadataNode.getYAxisLabel());
					// functionNode.getPlotMetadata().put("InitialValue", Double.toString(plotMetadataNode.getInitialValue()));
					// functionNode.getPlotMetadata().put("FinalValue", Double.toString(plotMetadataNode.getFinalValue()));
					// functionNode.getPlotMetadata().put("StepValue", Double.toString(plotMetadataNode.getStepValue()));
					// }

					summaryCompositeType.getVariables().add(variable);
				}
				else if(valueProperties instanceof InfoNode)
				{
					Variable variable = variablesFactory.createVariable();
					variable.setId(PopulateGeneralModelTreeUtils.parseId(keyProperties));
					variable.setName(keyProperties);
					variable.getAnonymousTypes().add(createInfoNode((InfoNode) valueProperties));
					summaryCompositeType.getVariables().add(variable);
				}
				else if(!(valueProperties instanceof PlotNode))
				{
					throw new ModelInterpreterException("Info Writer Node type not supported. Object: " + keyProperties + ". Java class" + valueProperties.getClass());
				}
			}
		}
		return summaryCompositeType;
	}

}
