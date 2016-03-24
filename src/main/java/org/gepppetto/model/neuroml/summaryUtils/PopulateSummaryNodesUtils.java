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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.model.GeppettoModelAccess;
import org.geppetto.core.model.ModelInterpreterException;
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
import org.lemsml.model.Case;
import org.lemsml.model.ConditionalDerivedVariable;
import org.lemsml.model.exceptions.LEMSCompilerException;
import org.lemsml.model.extended.LemsNode;
import org.lemsml.model.extended.Scope;
import org.lemsml.model.extended.Symbol;
import org.neuroml.export.info.model.ChannelInfoExtractor;
import org.neuroml.export.info.model.ExpressionNode;
import org.neuroml.export.info.model.InfoNode;
import org.neuroml.export.info.model.PlotMetadataNode;
import org.neuroml.export.utils.Utils;
import org.neuroml.model.IonChannel;
import org.neuroml.model.Standalone;
import org.neuroml.model.util.NeuroMLException;
import org.neuroml2.export.info.FunctionNodeHelper;
import org.neuroml2.model.BaseGate;
import org.neuroml2.model.BaseVoltageDepRate;
import org.neuroml2.model.BaseVoltageDepTime;
import org.neuroml2.model.BaseVoltageDepVariable;
import org.neuroml2.model.Cell;
import org.neuroml2.model.IonChannelHH;
import org.neuroml2.model.NeuroML2ModelReader;
import org.neuroml2.model.Neuroml2;

import com.google.common.base.Joiner;

import expr_parser.utils.UndefinedSymbolException;
import expr_parser.visitors.ARenderAs;
import expr_parser.visitors.AntlrExpressionParser;
import expr_parser.visitors.RenderMathJS;

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

	Neuroml2 neuroml;
	
	Map<String, String> gatesExpression = new HashMap<String, String>();

	public PopulateSummaryNodesUtils(Map<String, List<Type>> typesMap, Type type, URL url, GeppettoModelAccess access, String neuromlContent) throws Throwable
	{
		this.access = access;
		this.typesMap = typesMap;
		this.url = url;
		this.type = type;
		    // Create temp file.
		    File temp = File.createTempFile("tmp", ".nml");

		    // Delete temp file when program exits.
		    temp.deleteOnExit();

		    // Write to temp file
		    BufferedWriter out = new BufferedWriter(new FileWriter(temp));
		    out.write(neuromlContent);
		    out.close();
		this.neuroml = NeuroML2ModelReader.read(temp);
		;
	}

	public Variable getSummaryVariable() throws ModelInterpreterException, GeppettoVisitingException, NeuroMLException, LEMSException, LEMSCompilerException, UndefinedSymbolException
	{
		getAllExpressionNodes();
		return createDescriptionNode();
	}

	private void getAllExpressionNodes() throws LEMSCompilerException, UndefinedSymbolException
	{
		for(Cell cell : neuroml.getCells())
		{
			for(IonChannelHH chan : cell.getAllOfType(IonChannelHH.class))
			{
				
//				System.out.println("#############################");
//				System.out.println("channel:" + chan.getName());
				for(BaseGate gate : chan.getAllOfType(BaseGate.class))
				{
					gatesExpression.put(chan.getId() + "_" + gate.getId(), gateInfo(gate));
//					System.out.println("gate:" + gate.getName());
//					System.out.println(gateInfo(gate));
				}
//				System.out.println("#############################\n\n");
			}
		}
	}

	private String gateInfo(BaseGate gate) throws LEMSCompilerException, UndefinedSymbolException
	{
		String ret = "";
		for(BaseVoltageDepRate r : gate.getAllOfType(BaseVoltageDepRate.class))
		{
			ret += r.getName() + ": \n" + processExpression(r.getScope().resolve("r"));
		}
		for(BaseVoltageDepTime t : gate.getAllOfType(BaseVoltageDepTime.class))
		{
			ret += t.getName() + ": \n" + processExpression(t.getScope().resolve("t"));
		}
		for(BaseVoltageDepVariable x : gate.getAllOfType(BaseVoltageDepVariable.class))
		{
			ret += x.getName() + ": \n" + processExpression(x.getScope().resolve("x"));
		}

		return ret;
	}

	private String processExpression(Symbol resolved) throws LEMSCompilerException, UndefinedSymbolException
	{

		LemsNode type = resolved.getType();
		FunctionNodeHelper f = new FunctionNodeHelper();
		f.setName(resolved.getName());
		f.register(depsToMathJS(resolved));
		f.setIndependentVariable("v");

		if(type instanceof ConditionalDerivedVariable)
		{
			ConditionalDerivedVariable cdv = (ConditionalDerivedVariable) resolved.getType();
			f.register(f.getName(), conditionalDVToMathJS(cdv));
		}

		return f.getBigFatExpression(f.getName()) + "\n";
	}

	private Map<String, String> depsToMathJS(Symbol resolved) throws LEMSCompilerException, UndefinedSymbolException
	{
		Map<String, String> ret = new LinkedHashMap<String, String>();
		Scope scope = resolved.getScope();
		Map<String, String> sortedContext = scope.buildTopoSortedContext(resolved);
		for(Entry<String, String> kv : sortedContext.entrySet())
		{
			String var = kv.getKey();
			String def = kv.getValue();
			ret.put(var, adaptToMathJS(def));
		}
		return ret;
	}

	private String adaptToMathJS(String expression)
	{
		ARenderAs adaptor = new RenderMathJS(neuroml.getSymbolToUnit());
		AntlrExpressionParser p = new AntlrExpressionParser(expression);
		return p.parseAndVisitWith(adaptor);
	}

	private String conditionalDVToMathJS(ConditionalDerivedVariable cdv)
	{
		List<String> condsVals = new ArrayList<String>();
		String defaultCase = null;

		for(Case c : cdv.getCase())
		{
			if(null == c.getCondition()) // undocumented LEMS feature: no
											// condition, "catch-all" case
			defaultCase = adaptToMathJS(c.getValueDefinition());
			else condsVals.add(adaptToMathJS(c.getCondition()) + " ? " + adaptToMathJS(c.getValueDefinition()));
		}
		if(null != defaultCase) condsVals.add(defaultCase);
		else condsVals.add("null"); // no case satisfied, no default

		return Joiner.on(" : ").join(condsVals);
	}

	private Variable createDescriptionNode() throws ModelInterpreterException, GeppettoVisitingException, NeuroMLException, LEMSException
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
	
//	private void addExpresionNodes(CompositeType ionChannel) throws NeuroMLException, LEMSException, GeppettoVisitingException, ModelInterpreterException
//	{
//		for(Variable gateVariable : ionChannel.getVariables())
//		{
//			if(gateVariable.getId().equals(id))
//			{
//				CompositeType gateType = (CompositeType) gateVariable.getAnonymousTypes().get(0);
//				for(Variable rateVariable : gateType.getVariables())
//				{
//					//ResourcesSummary gatePropertyResources = ResourcesSummary.getValueByValue(gateProperties.getKey());
//					if(rateVariable.getId().equals(gatePropertyResources.getNeuromlId()))
//					{
//						CompositeType rateType = (CompositeType) rateVariable.getAnonymousTypes().get(0);
//						// Create expression node
//						rateType.getVariables().add(getExpressionVariable(gateProperties.getKey(), (ExpressionNode) gateProperties.getValue()));
//					}
//				}
//			}
//		}
//	}
	
	private void addExpresionNodes(CompositeType ionChannel) throws NeuroMLException, LEMSException, GeppettoVisitingException, ModelInterpreterException
	{
		// Get lems component and convert to neuroml
		Component component = ((Component) ionChannel.getDomainModel().getDomainModel());
		LinkedHashMap<String, Standalone> ionChannelMap = Utils.convertLemsComponentToNeuroML(component);
		if(ionChannelMap.get(component.getID()) instanceof IonChannel)
		{
			IonChannel neuromlIonChannel = (IonChannel) ionChannelMap.get(component.getID());
			if(neuromlIonChannel != null)
			{
				// Create channel info extractor from export library
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
												rateType.getVariables().add(getExpressionVariable(gateProperties.getKey(), (ExpressionNode) gateProperties.getValue()));
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
	}
	
//	private void addExpresionNodes(CompositeType ionChannel) throws NeuroMLException, LEMSException, GeppettoVisitingException, ModelInterpreterException
//	{
//		// Get lems component and convert to neuroml
//		Component component = ((Component) ionChannel.getDomainModel().getDomainModel());
//		LinkedHashMap<String, Standalone> ionChannelMap = Utils.convertLemsComponentToNeuroML(component);
//		if(ionChannelMap.get(component.getID()) instanceof IonChannel)
//		{
//			IonChannel neuromlIonChannel = (IonChannel) ionChannelMap.get(component.getID());
//			if(neuromlIonChannel != null)
//			{
//				// Create channel info extractor from export library
//				ChannelInfoExtractor channelInfoExtractor = new ChannelInfoExtractor(neuromlIonChannel);
//				InfoNode gatesNode = channelInfoExtractor.getGates();
//				for(Map.Entry<String, Object> entry : gatesNode.getProperties().entrySet())
//				{
//					String id = entry.getKey().substring(entry.getKey().lastIndexOf(" ") + 1);
//					for(Variable gateVariable : ionChannel.getVariables())
//					{
//						if(gateVariable.getId().equals(id))
//						{
//							InfoNode gateNode = (InfoNode) entry.getValue();
//							for(Map.Entry<String, Object> gateProperties : gateNode.getProperties().entrySet())
//							{
//								if(gateProperties.getValue() instanceof ExpressionNode)
//								{
//									// Match property id in export lib with neuroml id
//									ResourcesSummary gatePropertyResources = ResourcesSummary.getValueByValue(gateProperties.getKey());
//									if(gatePropertyResources != null)
//									{
//										CompositeType gateType = (CompositeType) gateVariable.getAnonymousTypes().get(0);
//										for(Variable rateVariable : gateType.getVariables())
//										{
//											if(rateVariable.getId().equals(gatePropertyResources.getNeuromlId()))
//											{
//												CompositeType rateType = (CompositeType) rateVariable.getAnonymousTypes().get(0);
//												// Create expression node
//												rateType.getVariables().add(getExpressionVariable(gateProperties.getKey(), (ExpressionNode) gateProperties.getValue()));
//											}
//										}
//
//									}
//									else
//									{
//										throw new ModelInterpreterException("No node matches summary gate rate");
//									}
//								}
//							}
//						}
//					}
//
//				}
//			}
//		}
//	}

}
