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

package org.geppetto.model.neuroml.modelinterpreter.utils;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.model.GeppettoModelAccess;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.model.neuroml.utils.Resources;
import org.geppetto.model.neuroml.utils.ResourcesDomainType;
import org.geppetto.model.neuroml.utils.ResourcesSummary;
import org.geppetto.model.types.ArrayType;
import org.geppetto.model.types.CompositeType;
import org.geppetto.model.types.Type;
import org.geppetto.model.types.TypesFactory;
import org.geppetto.model.types.TypesPackage;
import org.geppetto.model.util.GeppettoVisitingException;
import org.geppetto.model.values.Argument;
import org.geppetto.model.values.Dynamics;
import org.geppetto.model.values.Expression;
import org.geppetto.model.values.Function;
import org.geppetto.model.values.FunctionPlot;
import org.geppetto.model.values.HTML;
import org.geppetto.model.values.ValuesFactory;
import org.geppetto.model.variables.Variable;
import org.geppetto.model.variables.VariablesFactory;
import org.lemsml.jlems.core.sim.LEMSException;
import org.lemsml.jlems.core.type.Component;
import org.neuroml.export.info.model.ChannelInfoExtractor;
import org.neuroml.export.info.model.ExpressionNode;
import org.neuroml.export.info.model.InfoNode;
import org.neuroml.export.info.model.PlotMetadataNode;
import org.neuroml.export.utils.Utils;
import org.neuroml.model.IonChannel;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.Standalone;
import org.neuroml.model.util.NeuroMLException;

/**
 * Populates the Model Tree of Aspect
 * 
 * @author Adrian Quintana (adrian.perez@ucl.ac.uk)
 * 
 */

public class PopulateSummaryNodesModelTreeUtils
{
	private static Log logger = LogFactory.getLog(PopulateSummaryNodesModelTreeUtils.class);

	TypesFactory typeFactory = TypesFactory.eINSTANCE;
	VariablesFactory variablesFactory = VariablesFactory.eINSTANCE;
	ValuesFactory valuesFactory = ValuesFactory.eINSTANCE;

	GeppettoModelAccess access;
	NeuroMLDocument neuroml;
	Map<ResourcesDomainType, List<Type>> typesMap;
	URL url;

	public PopulateSummaryNodesModelTreeUtils(NeuroMLDocument neuroml, Map<ResourcesDomainType, List<Type>> typesMap, URL url, GeppettoModelAccess access)
	{
		this.neuroml = neuroml;
		this.access = access;
		this.typesMap = typesMap;
		this.url = url;
	}

	public List<Variable> getSummaryVariables() throws ModelInterpreterException, GeppettoVisitingException, NeuroMLException, LEMSException
	{
		List<Variable> summaryVariables = new ArrayList<Variable>();
		// Summary2
		summaryVariables.add(createDescriptionNode());

		// // Summary
		// Variable summaryVariable = variablesFactory.createVariable();
		// ModelInterpreterUtils.initialiseNodeFromString(summaryVariable, "Summary");
		// long start = System.currentTimeMillis();
		// InfoNode info = InfoTreeCreator.createInfoTree(neuroml);
		// logger.info("Creating the NeuroML summary using the export library took: " + (System.currentTimeMillis() - start) + "ms");
		// start = System.currentTimeMillis();
		// CompositeType compositeInfo = createInfoNode(info, "Summary");
		// summaryVariable.getAnonymousTypes().add(compositeInfo);
		// summaryVariables.add(summaryVariable);
		// logger.info("Converting the NeuroML summary to the Geppetto model took: " + (System.currentTimeMillis() - start) + "ms");

		return summaryVariables;
	}

	public Variable createDescriptionNode() throws ModelInterpreterException, GeppettoVisitingException, NeuroMLException, LEMSException
	{

		List<Type> networkComponents = typesMap.containsKey(ResourcesDomainType.NETWORK) ? typesMap.get(ResourcesDomainType.NETWORK) : null;
		List<Type> populationComponents = typesMap.containsKey(ResourcesDomainType.POPULATION) ? typesMap.get(ResourcesDomainType.POPULATION) : null;
		List<Type> cellComponents = typesMap.containsKey(ResourcesDomainType.CELL) ? typesMap.get(ResourcesDomainType.CELL) : null;
		List<Type> ionChannelComponents = typesMap.containsKey(ResourcesDomainType.IONCHANNEL) ? typesMap.get(ResourcesDomainType.IONCHANNEL) : null;
		List<Type> synapseComponents = typesMap.containsKey(ResourcesDomainType.SYNAPSE) ? typesMap.get(ResourcesDomainType.SYNAPSE) : null;
		List<Type> pulseGeneratorComponents = typesMap.containsKey(ResourcesDomainType.PULSEGENERATOR) ? typesMap.get(ResourcesDomainType.PULSEGENERATOR) : null;

		StringBuilder modelDescription = new StringBuilder();
		// modelDescription.append("<b>Model Summary</b><br/>");

		// // FIXME: We need to add something about how beautiful the network is and so on
		if(networkComponents != null && networkComponents.size() > 0)
		{
			modelDescription.append("Description: ");
			for(Type network : networkComponents)
			{
				modelDescription.append("<a href=\"#\" instancePath=\"Model.neuroml." + network.getId() + "\">" + network.getName() + "</a> ");
			}
		}
		modelDescription.append("<br/><br/><a target=\"_blank\" href=\"" + url.toString() + "\">NeuroML Source File</a><br/><br/>");

		// FIXME: We should improve this once the instance/type refactor is done as we need the cell type
		if(populationComponents != null && populationComponents.size() > 0)
		{
			modelDescription.append("<b>Populations</b><br/>");
			for(Type population : populationComponents)
			{
				modelDescription.append("Population " + population.getName() + ": ");
				modelDescription.append("<a href=\"#\" instancePath=\"Model.neuroml." + ((ArrayType) population).getArrayType().getId() + "\">" + ((ArrayType) population).getSize() + " "
						+ ((ArrayType) population).getArrayType().getName() + "</a><br/>");
			}
			modelDescription.append("<br/>");
		}

		if(cellComponents != null && cellComponents.size() > 0)
		{
			modelDescription.append("<b>Cells</b><br/>");
			for(Type cell : cellComponents)
			{
				modelDescription.append("<a href=\"#\" instancePath=\"Model.neuroml." + cell.getId() + "\">" + cell.getName() + "</a> ");
			}
			modelDescription.append("<br/><br/>");
		}

		if(ionChannelComponents != null && ionChannelComponents.size() > 0)
		{
			modelDescription.append("<b>Channels</b><br/>");
			for(Type ionChannel : ionChannelComponents)
			{
				modelDescription.append("<a href=\"#\" instancePath=\"Model.neuroml." + ionChannel.getId() + "\">" + ionChannel.getName() + "</a> ");

				// Add expresion nodes from the export library for the gate rates
				addExpresionNodes((CompositeType) ionChannel);
			}
			modelDescription.append("<br/><br/>");
		}

		if(synapseComponents != null && synapseComponents.size() > 0)
		{
			modelDescription.append("<b>Synapses</b><br/>");
			for(Type synapse : synapseComponents)
			{
				modelDescription.append("<a href=\"#\" instancePath=\"Model.neuroml." + synapse.getId() + "\">" + synapse.getName() + "</a> ");
			}
			modelDescription.append("<br/><br/>");
		}

		if(pulseGeneratorComponents != null && pulseGeneratorComponents.size() > 0)
		{
			// FIXME: Pulse generator? InputList? ExplicitList?
			modelDescription.append("<b>Inputs</b><br/>");
			for(Type pulseGenerator : pulseGeneratorComponents)
			{
				modelDescription.append("<a href=\"#\" instancePath=\"Model.neuroml." + pulseGenerator.getId() + "\">" + pulseGenerator.getName() + "</a> ");
			}
			modelDescription.append("<br/>");
		}

		HTML html = valuesFactory.createHTML();
		html.setHtml(modelDescription.toString());

		Variable descriptionVariable = variablesFactory.createVariable();
		descriptionVariable.setId(Resources.MODEL_DESCRIPTION.getId());
		descriptionVariable.setName(Resources.MODEL_DESCRIPTION.get());
		descriptionVariable.getTypes().add(access.getType(TypesPackage.Literals.HTML_TYPE));
		descriptionVariable.getInitialValues().put(access.getType(TypesPackage.Literals.HTML_TYPE), html);

		return descriptionVariable;
	}

	private void addExpresionNodes(CompositeType ionChannel) throws NeuroMLException, LEMSException, GeppettoVisitingException, ModelInterpreterException
	{
		// Get lems component and convert to neuroml
		Component component = ((Component) ionChannel.getDomainModel().getDomainModel());
		LinkedHashMap<String, Standalone> ionChannelMap = Utils.convertLemsComponentToNeuroML(component);
		IonChannel neuromlIonChannel = (IonChannel) ionChannelMap.get(component.getID());
		if(neuromlIonChannel != null)
		{
			//Create channel info extractor from export library
			ChannelInfoExtractor channelInfoExtractor = new ChannelInfoExtractor(neuromlIonChannel);
			InfoNode gatesNode = channelInfoExtractor.getGates();
			for(Map.Entry<String, Object> entry : gatesNode.getProperties().entrySet())
			{
				String id = entry.getKey().substring(entry.getKey().lastIndexOf(" ") + 1);
				for(Variable gateVariable : ionChannel.getVariables())
				{
					if(gateVariable.getId().equals(id))
					{
						InfoNode gateNode = (InfoNode) entry.getValue();
						for(Map.Entry<String, Object> gateProperties : gateNode.getProperties().entrySet())
						{
							if(gateProperties.getValue() instanceof ExpressionNode)
							{
								//Match property id in export lib with neuroml id
								ResourcesSummary gatePropertyResources = ResourcesSummary.getValueByValue(gateProperties.getKey());
								if(gatePropertyResources != null)
								{
									CompositeType gateType = (CompositeType) gateVariable.getAnonymousTypes().get(0);
									for(Variable rateVariable : gateType.getVariables())
									{
										if(rateVariable.getId().equals(gatePropertyResources.getNeuromlId()))
										{
											CompositeType rateType = (CompositeType) rateVariable.getAnonymousTypes().get(0);
											// Create expression node
											rateType.getVariables().add(getExpressionVariable(gateProperties.getKey(), (ExpressionNode) gateProperties.getValue()));
										}
									}

								}
								else{
									throw new ModelInterpreterException("No node matches summary gate rate");
								}
							}
						}
					}
				}

			}
		}
	}

	private Variable getExpressionVariable(String expressionNodeId, ExpressionNode expressionNode) throws GeppettoVisitingException
	{

		Argument argument = valuesFactory.createArgument();
		argument.setArgument("v");

		Expression expression = valuesFactory.createExpression();
		expression.setExpression(expressionNode.getExpression());

		Function function = valuesFactory.createFunction();
		function.setExpression(expression);
		function.getArguments().add(argument);
		PlotMetadataNode plotMetadataNode = expressionNode.getPlotMetadataNode();
		if(plotMetadataNode != null)
		{
			FunctionPlot functionPlot = valuesFactory.createFunctionPlot();
			functionPlot.setTitle(plotMetadataNode.getPlotTitle());
			functionPlot.setXAxisLabel(plotMetadataNode.getXAxisLabel());
			functionPlot.setYAxisLabel(plotMetadataNode.getYAxisLabel());
			functionPlot.setInitialValue(plotMetadataNode.getInitialValue());
			functionPlot.setFinalValue(plotMetadataNode.getFinalValue());
			functionPlot.setStepValue(plotMetadataNode.getStepValue());
			function.setFunctionPlot(functionPlot);
		}

		Dynamics dynamics = valuesFactory.createDynamics();
		dynamics.setDynamics(function);

		Variable variable = variablesFactory.createVariable();
		variable.setId(ModelInterpreterUtils.parseId(expressionNodeId));
		variable.setName(expressionNodeId);
		variable.getInitialValues().put(access.getType(TypesPackage.Literals.DYNAMICS_TYPE), dynamics);
		variable.getTypes().add(access.getType(TypesPackage.Literals.DYNAMICS_TYPE));

		return variable;
	}

	// public CompositeType createInfoNode(InfoNode node, String typeName) throws ModelInterpreterException, GeppettoVisitingException
	// {
	// CompositeType summaryCompositeType = typeFactory.createCompositeType();
	// ModelInterpreterUtils.initialiseNodeFromString(summaryCompositeType, typeName);
	//
	// for(Map.Entry<String, Object> properties : node.getProperties().entrySet())
	// {
	// String keyProperties = properties.getKey();
	// Object valueProperties = properties.getValue();
	// if(!keyProperties.equals("ID"))
	// {
	// if(valueProperties == null)
	// {
	// summaryCompositeType.getVariables().add(ModelInterpreterUtils.createTextTypeVariable(keyProperties, "", access));
	// }
	// else if(valueProperties instanceof String)
	// {
	// summaryCompositeType.getVariables().add(ModelInterpreterUtils.createTextTypeVariable(keyProperties, (String) valueProperties, access));
	// }
	// else if(valueProperties instanceof BigInteger)
	// {
	// summaryCompositeType.getVariables().add(ModelInterpreterUtils.createTextTypeVariable(keyProperties, ((BigInteger) valueProperties).toString(), access));
	// }
	// else if(valueProperties instanceof Integer)
	// {
	// summaryCompositeType.getVariables().add(ModelInterpreterUtils.createTextTypeVariable(keyProperties, Integer.toString((Integer) valueProperties), access));
	// }
	// else if(valueProperties instanceof ExpressionNode)
	// {
	// ExpressionNode expressionNode = ((ExpressionNode) valueProperties);
	//
	// Argument argument = valuesFactory.createArgument();
	// argument.setArgument("v");
	//
	// Expression expression = valuesFactory.createExpression();
	// expression.setExpression(expressionNode.getExpression());
	//
	// Function function = valuesFactory.createFunction();
	// function.setExpression(expression);
	// function.getArguments().add(argument);
	// PlotMetadataNode plotMetadataNode = expressionNode.getPlotMetadataNode();
	// if(plotMetadataNode != null)
	// {
	// FunctionPlot functionPlot = valuesFactory.createFunctionPlot();
	// functionPlot.setTitle(plotMetadataNode.getPlotTitle());
	// functionPlot.setXAxisLabel(plotMetadataNode.getXAxisLabel());
	// functionPlot.setYAxisLabel(plotMetadataNode.getYAxisLabel());
	// functionPlot.setInitialValue(plotMetadataNode.getInitialValue());
	// functionPlot.setFinalValue(plotMetadataNode.getFinalValue());
	// functionPlot.setStepValue(plotMetadataNode.getStepValue());
	// function.setFunctionPlot(functionPlot);
	// }
	//
	// Dynamics dynamics = valuesFactory.createDynamics();
	// dynamics.setDynamics(function);
	//
	// Variable variable = variablesFactory.createVariable();
	// variable.setId(ModelInterpreterUtils.parseId(keyProperties));
	// variable.setName(keyProperties);
	// variable.getInitialValues().put(access.getType(TypesPackage.Literals.DYNAMICS_TYPE), dynamics);
	// variable.getTypes().add(access.getType(TypesPackage.Literals.DYNAMICS_TYPE));
	//
	// summaryCompositeType.getVariables().add(variable);
	// }
	// else if(valueProperties instanceof InfoNode)
	// {
	// // This shouldn't happen but sometimes export library returns a node with no children inside
	// if(((InfoNode) valueProperties).getProperties().size() > 0)
	// {
	// Variable variable = variablesFactory.createVariable();
	// variable.setId(ModelInterpreterUtils.parseId(keyProperties));
	// variable.setName(keyProperties);
	// variable.getAnonymousTypes().add(createInfoNode((InfoNode) valueProperties, keyProperties));
	// summaryCompositeType.getVariables().add(variable);
	// }
	// }
	// // This should be removed once it is fixed in the export library
	// else if(!(valueProperties instanceof PlotNode))
	// {
	// throw new ModelInterpreterException("Info Writer Node type not supported. Object: " + keyProperties + ". Java class" + valueProperties.getClass());
	// }
	// }
	// }
	// return summaryCompositeType;
	// }

}
