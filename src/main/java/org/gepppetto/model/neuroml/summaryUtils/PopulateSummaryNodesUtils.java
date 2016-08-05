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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.emf.common.util.EList;
import org.geppetto.core.model.GeppettoModelAccess;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.model.neuroml.utils.ModelInterpreterUtils;
import org.geppetto.model.neuroml.utils.Resources;
import org.geppetto.model.neuroml.utils.ResourcesDomainType;
import org.geppetto.model.types.ArrayType;
import org.geppetto.model.types.CompositeType;
import org.geppetto.model.types.CompositeVisualType;
import org.geppetto.model.types.Type;
import org.geppetto.model.types.TypesFactory;
import org.geppetto.model.types.TypesPackage;
import org.geppetto.model.types.VisualType;
import org.geppetto.model.util.GeppettoVisitingException;
import org.geppetto.model.values.Argument;
import org.geppetto.model.values.Dynamics;
import org.geppetto.model.values.Expression;
import org.geppetto.model.values.Function;
import org.geppetto.model.values.FunctionPlot;
import org.geppetto.model.values.HTML;
import org.geppetto.model.values.Text;
import org.geppetto.model.values.ValuesFactory;
import org.geppetto.model.values.VisualGroup;
import org.geppetto.model.variables.Variable;
import org.geppetto.model.variables.VariablesFactory;
import org.lemsml.jlems.core.sim.LEMSException;
import org.lemsml.jlems.core.type.Component;
import org.neuroml.export.info.model.ChannelInfoExtractor;
import org.neuroml.export.info.model.ExpressionNode;
import org.neuroml.export.info.model.InfoNode;
import org.neuroml.export.info.model.PlotMetadataNode;
import org.neuroml.model.IonChannel;
import org.neuroml.model.IonChannelHH;
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
	private static String NOTES = "Notes";
	TypesFactory typeFactory = TypesFactory.eINSTANCE;
	VariablesFactory variablesFactory = VariablesFactory.eINSTANCE;
	ValuesFactory valuesFactory = ValuesFactory.eINSTANCE;

	GeppettoModelAccess access;
	Map<String, List<Type>> typesMap;
	Map<String, List<Variable>> plottableVariables = new HashMap<String, List<Variable>>();

	Type type;

	URL url;
	private NeuroMLDocument neuroMLDocument;

	public PopulateSummaryNodesUtils(Map<String, List<Type>> typesMap, Type type, URL url, GeppettoModelAccess access, NeuroMLDocument neuroMLDocument)
	{
		this.access = access;
		this.typesMap = typesMap;
		this.url = url;
		this.type = type;
		this.neuroMLDocument = neuroMLDocument;
	}

	/**
	 * Creates all HTML variables for objects in maps.
	 */
	public void createHTMLVariables() throws ModelInterpreterException, GeppettoVisitingException, NeuroMLException, LEMSException
	{
		this.createCellsHTMLVariable();
		this.createSynapsesHTMLVariable();
		this.createChannelsHTMLVariable();
		this.createInputsHTMLVariable();
	}

	/**
	 * Creates general Model description
	 * 
	 * @return
	 * @throws ModelInterpreterException
	 * @throws GeppettoVisitingException
	 * @throws NeuroMLException
	 * @throws LEMSException
	 */
	public Variable getDescriptionNode() throws ModelInterpreterException, GeppettoVisitingException, NeuroMLException, LEMSException
	{

		List<Type> networkComponents = typesMap.containsKey(ResourcesDomainType.NETWORK.get()) ? typesMap.get(ResourcesDomainType.NETWORK.get()) : null;
		List<Type> populationComponents = typesMap.containsKey(ResourcesDomainType.POPULATION.get()) ? typesMap.get(ResourcesDomainType.POPULATION.get()) : null;
		List<Type> cellComponents = typesMap.containsKey(ResourcesDomainType.CELL.get()) ? typesMap.get(ResourcesDomainType.CELL.get()) : null;
		List<Type> ionChannelComponents = typesMap.containsKey(ResourcesDomainType.IONCHANNEL.get()) ? typesMap.get(ResourcesDomainType.IONCHANNEL.get()) : null;
		List<Type> synapseComponents = typesMap.containsKey(ResourcesDomainType.SYNAPSE.get()) ? typesMap.get(ResourcesDomainType.SYNAPSE.get()) : null;
		List<Type> pulseGeneratorComponents = typesMap.containsKey(ResourcesDomainType.PULSEGENERATOR.get()) ? typesMap.get(ResourcesDomainType.PULSEGENERATOR.get()) : null;

		StringBuilder modelDescription = new StringBuilder();

		if(networkComponents != null && networkComponents.size() > 0)
		{
			modelDescription.append("Description: ");
			for(Type network : networkComponents)
			{
				modelDescription.append("<a href=\"#\" instancePath=\"Model.neuroml." + network.getId() + "\">" + network.getName() + "</a><br/><br/>");
			}
		}
		modelDescription.append("<a target=\"_blank\" href=\"" + url.toString() + "\">NeuroML Source File</a><br/><br/>");

		if(populationComponents != null && populationComponents.size() > 0)
		{
			modelDescription.append("<b>Populations</b><br/>");
			for(Type population : populationComponents)
			{
				modelDescription.append("Population " + population.getName() + ": ");
				// get proper name of population cell with brackets and index # of population
				String name = ((ArrayType) population).getArrayType().getId().trim() + "." + population.getId().trim() + "[" + populationComponents.indexOf(population) + "]";
				modelDescription.append("<a href=\"#\" instancePath=\"Model.neuroml." + name + "\">" + ((ArrayType) population).getSize() + " " + ((ArrayType) population).getArrayType().getName()
						+ "</a><br/>");
			}
			modelDescription.append("<br/>");
		}

		if(cellComponents != null && cellComponents.size() > 0)
		{
			modelDescription.append("<b>Cells</b><br/>");
			for(Type cell : cellComponents)
			{
				modelDescription.append("<a href=\"#\" instancePath=\"Model.neuroml." + cell.getId() + "\">" + cell.getName() + "</a> | ");
			}
			modelDescription.append("<br/><br/>");
		}

		if(ionChannelComponents != null && ionChannelComponents.size() > 0)
		{
			modelDescription.append("<b>Channels</b><br/>");
			for(Type ionChannel : ionChannelComponents)
			{

				modelDescription.append("<a href=\"#\" instancePath=\"Model.neuroml." + ionChannel.getId() + "\">" + ionChannel.getName() + "</a> | ");

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
				modelDescription.append("<a href=\"#\" instancePath=\"Model.neuroml." + synapse.getId() + "\">" + synapse.getName() + "</a> | ");
			}
			modelDescription.append("<br/><br/>");
		}

		if(pulseGeneratorComponents != null && pulseGeneratorComponents.size() > 0)
		{
			// FIXME: Pulse generator? InputList? ExplicitList?
			modelDescription.append("<b>Inputs</b><br/>");
			for(Type pulseGenerator : pulseGeneratorComponents)
			{
				modelDescription.append("<a href=\"#\" instancePath=\"Model.neuroml." + pulseGenerator.getId() + "\">" + pulseGenerator.getName() + "</a> | ");
			}
			modelDescription.append("<br/>");
		}

		// If there is nothing at least show a link to open the whole model in a tree visualiser
		if((networkComponents == null || networkComponents.size() == 0) && (populationComponents == null || populationComponents.size() == 0) && (cellComponents == null || cellComponents.size() == 0)
				&& (synapseComponents == null || synapseComponents.size() == 0) && (pulseGeneratorComponents == null || pulseGeneratorComponents.size() == 0))
		{
			modelDescription.insert(0, "Description: <a href=\"#\" instancePath=\"Model.neuroml." + type.getId() + "\">" + type.getName() + "</a><br/><br/>");
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

	/**
	 * Create Variable with HTML value for a Cell
	 * 
	 * @param cell
	 *            - Cell used to create this html element
	 * @return
	 * @throws ModelInterpreterException
	 * @throws GeppettoVisitingException
	 * @throws NeuroMLException
	 * @throws LEMSException
	 */
	private void createCellsHTMLVariable() throws ModelInterpreterException, GeppettoVisitingException, NeuroMLException, LEMSException
	{
		List<Type> cellComponents = typesMap.containsKey(ResourcesDomainType.CELL.get()) ? typesMap.get(ResourcesDomainType.CELL.get()) : null;

		if(cellComponents != null && cellComponents.size() > 0)
		{
			for(Type cell : cellComponents)
			{
				List<Variable> notesComponents = new ArrayList<Variable>();
				List<Type> ionChannelComponents = typesMap.containsKey(ResourcesDomainType.IONCHANNEL.get()) ? typesMap.get(ResourcesDomainType.IONCHANNEL.get()) : null;
				List<Type> synapseComponents = typesMap.containsKey(ResourcesDomainType.SYNAPSE.get()) ? typesMap.get(ResourcesDomainType.SYNAPSE.get()) : null;
				List<Type> pulseGeneratorComponents = typesMap.containsKey(ResourcesDomainType.PULSEGENERATOR.get()) ? typesMap.get(ResourcesDomainType.PULSEGENERATOR.get()) : null;

				EList<Variable> cellVariables = ((CompositeType) cell).getVariables();
				for(Variable v : cellVariables)
				{
					if(v.getId().equals(NOTES))
					{
						notesComponents.add(v);
					}
				}

				if(notesComponents != null && notesComponents.size() > 0)
				{
					StringBuilder htmlText = new StringBuilder();

					htmlText.append("<b>Description</b><br/>");
					for(Variable note : notesComponents)
					{
						Text about = (Text) note.getInitialValues().get(access.getType(TypesPackage.Literals.TEXT_TYPE));
						htmlText.append("<p instancePath=\"Model.neuroml." + note.getId() + "\">" + about.getText() + "</p> ");
					}
					htmlText.append("<br/>");

					Variable htmlVariable = variablesFactory.createVariable();
					htmlVariable.setId(Resources.NOTES.getId());
					htmlVariable.setName(Resources.NOTES.get());

					// Create HTML Value object and set HTML text
					HTML html = valuesFactory.createHTML();
					html.setHtml(htmlText.toString());
					htmlVariable.getTypes().add(access.getType(TypesPackage.Literals.HTML_TYPE));
					htmlVariable.getInitialValues().put(access.getType(TypesPackage.Literals.HTML_TYPE), html);

					((CompositeType) cell).getVariables().add(htmlVariable);
				}

				if(ionChannelComponents != null && ionChannelComponents.size() > 0)
				{
					StringBuilder htmlText = new StringBuilder();

					htmlText.append("<b>Channels</b><br/>");
					for(Type ionChannel : ionChannelComponents)
					{
						htmlText.append("<a href=\"#\" instancePath=\"Model.neuroml." + ionChannel.getId() + "\">" + ionChannel.getName() + "</a> | ");
					}
					htmlText.append("<br/><br/>");

					Variable htmlVariable = variablesFactory.createVariable();
					htmlVariable.setId(Resources.ION_CHANNEL.getId());
					htmlVariable.setName(Resources.ION_CHANNEL.get());

					// Create HTML Value object and set HTML text
					HTML html = valuesFactory.createHTML();
					html.setHtml(htmlText.toString());
					htmlVariable.getTypes().add(access.getType(TypesPackage.Literals.HTML_TYPE));
					htmlVariable.getInitialValues().put(access.getType(TypesPackage.Literals.HTML_TYPE), html);

					((CompositeType) cell).getVariables().add(htmlVariable);
				}

				if(synapseComponents != null && synapseComponents.size() > 0)
				{
					StringBuilder htmlText = new StringBuilder();

					htmlText.append("<b>Synapses</b><br/>");
					for(Type synapse : synapseComponents)
					{
						htmlText.append("<a href=\"#\" instancePath=\"Model.neuroml." + synapse.getId() + "\">" + synapse.getName() + "</a> | ");
					}
					htmlText.append("<br/><br/>");

					Variable htmlVariable = variablesFactory.createVariable();
					htmlVariable.setId(Resources.SYNAPSE.getId());
					htmlVariable.setName(Resources.SYNAPSE.get());

					// Create HTML Value object and set HTML text
					HTML html = valuesFactory.createHTML();
					html.setHtml(htmlText.toString());
					htmlVariable.getTypes().add(access.getType(TypesPackage.Literals.HTML_TYPE));
					htmlVariable.getInitialValues().put(access.getType(TypesPackage.Literals.HTML_TYPE), html);

					((CompositeType) cell).getVariables().add(htmlVariable);
				}

				// Add Visual Group to model cell description
				VisualType visualType = cell.getVisualType();
				List<VisualGroup> visualGroups = ((CompositeVisualType) visualType).getVisualGroups();
				if(visualGroups != null && visualGroups.size()>0)
				{
					StringBuilder htmlText = new StringBuilder();

					htmlText.append("<b>Show Visual Group:</b><br/>");
					for(VisualGroup visualGroup : visualGroups)
					{
						htmlText.append("<a href=\"#\" type=\"visual\" instancePath=\"Model.neuroml." + visualType.getId() + "."+ visualGroup.getId()+"\">" + visualGroup.getName() + " Cell Regions</a> | ");
					}
					htmlText.append("<br/><br/>");

					Variable htmlVariable = variablesFactory.createVariable();
					htmlVariable.setId(visualType.getId());
					htmlVariable.setName(visualType.getName());

					// Create HTML Value object and set HTML text
					HTML html = valuesFactory.createHTML();
					html.setHtml(htmlText.toString());
					htmlVariable.getTypes().add(access.getType(TypesPackage.Literals.HTML_TYPE));
					htmlVariable.getInitialValues().put(access.getType(TypesPackage.Literals.HTML_TYPE), html);

					((CompositeType) cell).getVariables().add(htmlVariable);
				}

				if(pulseGeneratorComponents != null && pulseGeneratorComponents.size() > 0)
				{
					StringBuilder htmlText = new StringBuilder();
					// FIXME: Pulse generator? InputList? ExplicitList?
					htmlText.append("<b>Inputs</b><br/>");
					for(Type pulseGenerator : pulseGeneratorComponents)
					{
						htmlText.append("<a href=\"#\" instancePath=\"Model.neuroml." + pulseGenerator.getId() + "\">" + pulseGenerator.getName() + "</a> ");
					}
					htmlText.append("<br/>");

					Variable htmlVariable = variablesFactory.createVariable();
					htmlVariable.setId(Resources.PULSE_GENERATOR.getId());
					htmlVariable.setName(Resources.PULSE_GENERATOR.get());

					// Create HTML Value object and set HTML text
					HTML html = valuesFactory.createHTML();
					html.setHtml(htmlText.toString());
					htmlVariable.getTypes().add(access.getType(TypesPackage.Literals.HTML_TYPE));
					htmlVariable.getInitialValues().put(access.getType(TypesPackage.Literals.HTML_TYPE), html);

					((CompositeType) cell).getVariables().add(htmlVariable);
				}
			}
		}
	}

	private void createChannelsHTMLVariable() throws ModelInterpreterException, GeppettoVisitingException, NeuroMLException, LEMSException
	{
		List<Type> ionChannelComponents = typesMap.containsKey(ResourcesDomainType.IONCHANNEL.get()) ? typesMap.get(ResourcesDomainType.IONCHANNEL.get()) : null;

		if(ionChannelComponents != null && ionChannelComponents.size() > 0)
		{
			for(Type ionChannel : ionChannelComponents)
			{
				List<Variable> notesComponents = new ArrayList<Variable>();

				EList<Variable> cellVariables = ((CompositeType) ionChannel).getVariables();
				for(Variable v : cellVariables)
				{
					if(v.getId().equals(NOTES))
					{
						notesComponents.add(v);
					}
				}

				StringBuilder htmlText = new StringBuilder();

				if(notesComponents != null && notesComponents.size() > 0)
				{
					htmlText.append("<b>Description</b><br/>");
					for(Variable note : notesComponents)
					{
						Text about = (Text) note.getInitialValues().get(access.getType(TypesPackage.Literals.TEXT_TYPE));
						htmlText.append("<p instancePath=\"Model.neuroml." + note.getId() + "\">" + about.getText() + "</p> ");
					}
					htmlText.append("<br/>");
				}
				
				// Create HTML Value object and set HTML text
				htmlText.append("<a href=\"#\" instancePath=\"Model.neuroml." + ionChannel.getId() + "\">" + ionChannel.getName() + "</a>");
				htmlText.append("<br/><br/>");

				// Adds plot activation variables
				List<Variable> variables = this.plottableVariables.get(ionChannel.getName());
				if(variables != null)
				{
					htmlText.append("<b>Plot activation variables</b><br/>");
					for(Variable v : variables)
					{
						String[] split = v.getPath().split("\\.");
						String shortLabel = v.getPath();
						if(split.length > 5)
						{
							shortLabel = split[1] + "." + split[2] + "..." + split[split.length - 1];
						}
						htmlText.append("<a href=\"#\" type=\"variable\" instancePath=\"Model." + v.getPath() + "\">" + shortLabel + "</a><br/>");
					}
				}
				Variable htmlVariable = variablesFactory.createVariable();
				htmlVariable.setId(ionChannel.getId());
				htmlVariable.setName(ionChannel.getName());
				
				HTML html = valuesFactory.createHTML();
				html.setHtml(htmlText.toString());
				htmlVariable.getTypes().add(access.getType(TypesPackage.Literals.HTML_TYPE));
				htmlVariable.getInitialValues().put(access.getType(TypesPackage.Literals.HTML_TYPE), html);
				((CompositeType) ionChannel).getVariables().add(htmlVariable);
			}
		}
	}

	private void createSynapsesHTMLVariable() throws ModelInterpreterException, GeppettoVisitingException, NeuroMLException, LEMSException
	{
		List<Type> synapseComponents = typesMap.containsKey(ResourcesDomainType.SYNAPSE.get()) ? typesMap.get(ResourcesDomainType.SYNAPSE.get()) : null;

		if(synapseComponents != null && synapseComponents.size() > 0)
		{
			for(Type synapse : synapseComponents)
			{
				StringBuilder htmlText = new StringBuilder();

				Variable htmlVariable = variablesFactory.createVariable();
				htmlVariable.setId(synapse.getId());
				htmlVariable.setName(synapse.getName());

				// Create HTML Value object and set HTML text
				HTML html = valuesFactory.createHTML();
				htmlText.append("<a href=\"#\" instancePath=\"Model.neuroml." + synapse.getId() + "\">" + synapse.getName() + "</a> ");
				htmlText.append("<br/><br/>");
				html.setHtml(htmlText.toString());
				htmlVariable.getTypes().add(access.getType(TypesPackage.Literals.HTML_TYPE));
				htmlVariable.getInitialValues().put(access.getType(TypesPackage.Literals.HTML_TYPE), html);
				((CompositeType) synapse).getVariables().add(htmlVariable);
			}
		}
	}

	private void createInputsHTMLVariable() throws ModelInterpreterException, GeppettoVisitingException, NeuroMLException, LEMSException
	{
		List<Type> pulseGeneratorComponents = typesMap.containsKey(ResourcesDomainType.PULSEGENERATOR.get()) ? typesMap.get(ResourcesDomainType.PULSEGENERATOR.get()) : null;

		if(pulseGeneratorComponents != null && pulseGeneratorComponents.size() > 0)
		{
			for(Type pulseGenerator : pulseGeneratorComponents)
			{
				StringBuilder htmlText = new StringBuilder();

				Variable htmlVariable = variablesFactory.createVariable();
				htmlVariable.setId(pulseGenerator.getId());
				htmlVariable.setName(pulseGenerator.getName());

				// Create HTML Value object and set HTML text
				HTML html = valuesFactory.createHTML();
				htmlText.append("<a href=\"#\" instancePath=\"Model.neuroml." + pulseGenerator.getId() + "\">" + pulseGenerator.getName() + "</a> ");
				htmlText.append("<br/><br/>");
				html.setHtml(htmlText.toString());
				htmlVariable.getTypes().add(access.getType(TypesPackage.Literals.HTML_TYPE));
				htmlVariable.getInitialValues().put(access.getType(TypesPackage.Literals.HTML_TYPE), html);
				((CompositeType) pulseGenerator).getVariables().add(htmlVariable);
			}
		}
	}

	/**
	 * @param component
	 * @return the NeuroML cell corresponding to a given LEMS component
	 */
	private Standalone getNeuroMLIonChannel(Component component)
	{
		String lemsId = component.getID();
		for(IonChannel c : neuroMLDocument.getIonChannel())
		{
			if(c.getId().equals(lemsId))
			{
				return c;
			}
		}
		for(IonChannelHH c : neuroMLDocument.getIonChannelHH())
		{
			if(c.getId().equals(lemsId))
			{
				return c;
			}
		}
		// for(IonChannelKS c : neuroMLDocument.getIonChannelKS())
		// {
		// if(c.getId().equals(lemsId))
		// {
		// return c;
		// }
		// }
		return null;
	}

	private void addExpresionNodes(CompositeType ionChannel) throws NeuroMLException, LEMSException, GeppettoVisitingException, ModelInterpreterException
	{
		// Get lems component and convert to neuroml
		Component component = ((Component) ionChannel.getDomainModel().getDomainModel());
		Standalone neuromlIonChannel = getNeuroMLIonChannel(component);

		// Create channel info extractor from export library
		if(neuromlIonChannel != null)
		{
			ChannelInfoExtractor channelInfoExtractor = new ChannelInfoExtractor((IonChannel) neuromlIonChannel);
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
								// Match property id in export lib with neuroml id
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
											Variable variable = getExpressionVariable(gateProperties.getKey(), (ExpressionNode) gateProperties.getValue());
											rateType.getVariables().add(variable);

											if(!((ExpressionNode) gateProperties.getValue()).getExpression().startsWith("org.neuroml.export"))
											{
												List<Variable> variables = this.plottableVariables.get(ionChannel.getName());
												if(variables == null) variables = new ArrayList<Variable>();
												variables.add(variable);
												this.plottableVariables.put(ionChannel.getName(), variables);
											}
										}
									}

								}
								else
								{
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

}
