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

package org.gepppetto.model.neuroml.summaryUtils;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.model.GeppettoModelAccess;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.model.neuroml.modelInterpreterUtils.NeuroMLModelInterpreterUtils;
import org.geppetto.model.neuroml.utils.ModelInterpreterUtils;
import org.geppetto.model.neuroml.utils.Resources;
import org.geppetto.model.neuroml.utils.ResourcesDomainType;
import org.geppetto.model.types.ArrayType;
import org.geppetto.model.types.CompositeType;
import org.geppetto.model.types.Type;
import org.geppetto.model.types.TypesFactory;
import org.geppetto.model.types.TypesPackage;
import org.geppetto.model.util.GeppettoVisitingException;
import org.geppetto.model.values.Argument;
import org.geppetto.model.values.Composite;
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

public class PopulateSummaryNodesUtils
{
	private static Log logger = LogFactory.getLog(PopulateSummaryNodesUtils.class);

	TypesFactory typeFactory = TypesFactory.eINSTANCE;
	VariablesFactory variablesFactory = VariablesFactory.eINSTANCE;
	ValuesFactory valuesFactory = ValuesFactory.eINSTANCE;

	GeppettoModelAccess access;
	Map<String, List<Type>> typesMap;
	Type type;
	
	URL url;

	public PopulateSummaryNodesUtils(Map<String, List<Type>> typesMap, Type type, URL url, GeppettoModelAccess access)
	{
		this.access = access;
		this.typesMap = typesMap;
		this.url = url;
		this.type = type;
	}

	public CompositeType getSummaryVariable() throws ModelInterpreterException, GeppettoVisitingException, NeuroMLException, LEMSException
	{
		return createDescriptionNode();
	}

	private CompositeType createDescriptionNode() throws ModelInterpreterException, GeppettoVisitingException, NeuroMLException, LEMSException
	{

		List<Type> networkComponents = typesMap.containsKey(ResourcesDomainType.NETWORK.get()) ? typesMap.get(ResourcesDomainType.NETWORK.get()) : null;
		List<Type> populationComponents = typesMap.containsKey(ResourcesDomainType.POPULATION.get()) ? typesMap.get(ResourcesDomainType.POPULATION.get()) : null;
		List<Type> cellComponents = typesMap.containsKey(ResourcesDomainType.CELL.get()) ? typesMap.get(ResourcesDomainType.CELL.get()) : null;
		List<Type> ionChannelComponents = typesMap.containsKey(ResourcesDomainType.IONCHANNEL.get()) ? typesMap.get(ResourcesDomainType.IONCHANNEL.get()) : null;
		List<Type> synapseComponents = typesMap.containsKey(ResourcesDomainType.SYNAPSE.get()) ? typesMap.get(ResourcesDomainType.SYNAPSE.get()) : null;
		List<Type> pulseGeneratorComponents = typesMap.containsKey(ResourcesDomainType.PULSEGENERATOR.get()) ? typesMap.get(ResourcesDomainType.PULSEGENERATOR.get()) : null;

		//create composite type to hold all variables for html description
		CompositeType root = typeFactory.createCompositeType();
		root.setId(Resources.MODEL_DESCRIPTION.getId());
		root.setName(Resources.MODEL_DESCRIPTION.get());
		
		//stores combined HTML valued for storing in variable
		StringBuilder html = new StringBuilder();
		//Create Variable holding HTML for network description
		if(networkComponents != null && networkComponents.size() > 0)
		{
			html.append("Description: ");
			for(Type network : networkComponents)
			{
				html.append("<a href=\"#\" instancePath=\"Model.neuroml." + network.getId() + "\">" + network.getName() + "</a><br/><br/>");
			}	
		}
		html.append("<a target=\"_blank\" href=\"" + url.toString() + "\">NeuroML Source File</a><br/><br/>");
		this.createHTML(html.toString(),"networks", root);

		html = new StringBuilder();
		//Create Variable holding HTML for all populations
		if(populationComponents != null && populationComponents.size() > 0)
		{
			html.append("<b>Populations</b><br/>");
			for(Type population : populationComponents)
			{
				html.append("Population " + population.getName() + ": ");
				html.append("<a href=\"#\" instancePath=\"Model.neuroml." + ((ArrayType) population).getArrayType().getId() + "\">" + ((ArrayType) population).getSize() + " "
						+ ((ArrayType) population).getArrayType().getName() + "</a><br/>");
			}
			html.append("<br/>");
			this.createHTML(html.toString(),"populations", root);
		}

		html = new StringBuilder();
		//Create Variable for HTML for all cells
		if(cellComponents != null && cellComponents.size() > 0)
		{
			html.append("<b>Cells</b><br/>");
			for(Type cell : cellComponents)
			{
				html.append("<a href=\"#\" instancePath=\"Model.neuroml." + cell.getId() + "\">" + cell.getName() + "</a> ");
			}
			html.append("<br/><br/>");
			this.createHTML(html.toString(), "cells",root);
		}

		html = new StringBuilder();
		if(ionChannelComponents != null && ionChannelComponents.size() > 0)
		{
			html.append("<b>Channels</b><br/>");
			for(Type ionChannel : ionChannelComponents)
			{
				html.append("<a href=\"#\" instancePath=\"Model.neuroml." + ionChannel.getId() + "\">" + ionChannel.getName() + "</a> ");

				// Add expresion nodes from the export library for the gate rates
				addExpresionNodes((CompositeType) ionChannel);
			}
			html.append("<br/><br/>");
			this.createHTML(html.toString(), "chanels",root);
		}

		html = new StringBuilder();
		if(synapseComponents != null && synapseComponents.size() > 0)
		{
			html.append("<b>Synapses</b><br/>");
			for(Type synapse : synapseComponents)
			{
				html.append("<a href=\"#\" instancePath=\"Model.neuroml." + synapse.getId() + "\">" + synapse.getName() + "</a> ");
			}
			html.append("<br/><br/>");
			this.createHTML(html.toString(), "synapses",root);
		}

		html = new StringBuilder();
		if(pulseGeneratorComponents != null && pulseGeneratorComponents.size() > 0)
		{
			// FIXME: Pulse generator? InputList? ExplicitList?
			html.append("<b>Inputs</b><br/>");
			for(Type pulseGenerator : pulseGeneratorComponents)
			{
				html.append("<a href=\"#\" instancePath=\"Model.neuroml." + pulseGenerator.getId() + "\">" + pulseGenerator.getName() + "</a> ");
			}
			html.append("<br/>");
			this.createHTML(html.toString(), "inputs",root);
		}

		html = new StringBuilder();
		//If there is nothing at least show a link to open the whole model in a tree visualiser
		if((networkComponents == null || networkComponents.size() == 0) && 
				(populationComponents == null || populationComponents.size() == 0) && 
				(cellComponents == null || cellComponents.size() == 0) &&
				(synapseComponents == null || synapseComponents.size() == 0) && 
				(pulseGeneratorComponents == null || pulseGeneratorComponents.size() == 0)){
			html.append("Description: <a href=\"#\" instancePath=\"Model.neuroml." + type.getId() + "\">" + type.getName() + "</a><br/><br/>");
			this.createHTML( html.toString(),"description", root);
		}

		return root;
	}
	
	/**
	 * Creates a Variable Type, which holds an HTML value. 
	 * 
	 * @param text - HTML text
	 * @param id - ID of variable to be created
	 * @param container - Composite Type object used to hold all Variables
	 * 
	 * @throws GeppettoVisitingException
	 */
	private void createHTML(String text, String id, CompositeType container) throws GeppettoVisitingException{
		//Create Variable, and assign ID to it
		Variable descriptionVariable = variablesFactory.createVariable();
		NeuroMLModelInterpreterUtils.initialiseNodeFromString(descriptionVariable, id);
		
		//Create HTML Value object and set HTML text
		HTML html = valuesFactory.createHTML();
		html.setHtml(text);
		
		//Add HTML Value to Variable, and Variable to parent 
		descriptionVariable.getInitialValues().put(access.getType(TypesPackage.Literals.HTML_TYPE), html);
		descriptionVariable.getTypes().add(access.getType(TypesPackage.Literals.HTML_TYPE));
		container.getVariables().add(descriptionVariable);
	}

	private void addExpresionNodes(CompositeType ionChannel) throws NeuroMLException, LEMSException, GeppettoVisitingException, ModelInterpreterException
	{
		// Get lems component and convert to neuroml
		Component component = ((Component) ionChannel.getDomainModel().getDomainModel());
		LinkedHashMap<String, Standalone> ionChannelMap = Utils.convertLemsComponentToNeuroML(component);
		if (ionChannelMap.get(component.getID()) instanceof IonChannel){
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

}
