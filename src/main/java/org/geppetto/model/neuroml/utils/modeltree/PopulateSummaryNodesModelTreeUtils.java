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
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.model.neuroml.utils.NeuroMLAccessUtility;
import org.geppetto.model.neuroml.utils.Resources;
import org.geppetto.model.neuroml.utils.ResourcesDomainType;
import org.lemsml.jlems.core.expression.FunctionNode;
import org.neuroml.export.info.InfoTreeCreator;
import org.neuroml.export.info.model.ExpressionNode;
import org.neuroml.export.info.model.InfoNode;
import org.neuroml.export.info.model.PlotMetadataNode;
import org.neuroml.export.info.model.PlotNode;
import org.neuroml.model.Population;
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

//	public PopulateSummaryNodesModelTreeUtils(ModelWrapper model) {	
//		this.model = model;
//		initialiseModelDescription();
//	}
//	
//	private ModelWrapper model;
//	
//	// Create Map of NeuroMlComponent and Nodes for description node
//	private Map<ResourcesDomainType, Map<Standalone, ANode>> modelDescriptionComponents;
//	
//	private InfoNode infoNode = new InfoNode();
//	
//	
//	public void addInfoNode(Standalone element) throws NeuroMLException{
//		infoNode.putAll(InfoTreeCreator.createPropertiesFromStandaloneComponent(element));
//	}
//	
//	public void initialiseModelDescription(){
//		modelDescriptionComponents = new HashMap<ResourcesDomainType, Map<Standalone, ANode>>();
//		modelDescriptionComponents.put(ResourcesDomainType.NETWORK, new HashMap<Standalone, ANode>());
//		modelDescriptionComponents.put(ResourcesDomainType.POPULATION, new HashMap<Standalone, ANode>());
//		modelDescriptionComponents.put(ResourcesDomainType.SYNAPSE, new HashMap<Standalone, ANode>());
//		modelDescriptionComponents.put(ResourcesDomainType.CELL, new HashMap<Standalone, ANode>());
//		modelDescriptionComponents.put(ResourcesDomainType.PULSEGENERATOR, new HashMap<Standalone, ANode>());
//		modelDescriptionComponents.put(ResourcesDomainType.IONCHANNEL, new HashMap<Standalone, ANode>());
//	}
//	
//	public void addNodeToModelDescription(ResourcesDomainType domainType, Standalone standalone, ANode node){
//		Map<Standalone, ANode> modelDescriptionComponentsItem = modelDescriptionComponents.get(domainType);
//		if (!modelDescriptionComponentsItem.containsKey(standalone)){
//			modelDescriptionComponentsItem.put(standalone, node);
//		}
//	}
//
//	public HTMLMetadataNode createDescriptionNode() throws ModelInterpreterException
//	{
//		//Design doodle: https://docs.google.com/drawings/d/1NDwzCU6LfG9Wq162_S9huNNuAekxPKn8VT8Q0PoC-aw/edit
//		URL modelUrl = (URL) model.getModel(NeuroMLAccessUtility.URL_ID);
//		
//		Map<Standalone, ANode> networkComponents = modelDescriptionComponents.get(ResourcesDomainType.NETWORK);
//		Map<Standalone, ANode> populationComponents = modelDescriptionComponents.get(ResourcesDomainType.POPULATION);
//		Map<Standalone, ANode> cellComponents = modelDescriptionComponents.get(ResourcesDomainType.CELL);
//		Map<Standalone, ANode> ionChannelComponents = modelDescriptionComponents.get(ResourcesDomainType.IONCHANNEL);
//		Map<Standalone, ANode> synapseComponents = modelDescriptionComponents.get(ResourcesDomainType.SYNAPSE);
//		Map<Standalone, ANode> pulseGeneratorComponents = modelDescriptionComponents.get(ResourcesDomainType.PULSEGENERATOR);
//		
//		StringBuilder modelDescription = new StringBuilder(); 
//		modelDescription.append("<b>Model Summary</b><br/>");
//
//		//FIXME: We need to extract the main component (target component) and extract the description from it in order to do this feature generic. This will wait until the instance/type refactor
//		//FIXME: We need to add something about how beautiful the network is and so on
//		if (networkComponents.size() > 0){
//			modelDescription.append("Description: ");
//			for (ANode node : networkComponents.values()){
//				modelDescription.append("<a href=\"#\" instancePath=\"$" + node.getInstancePath() + "$\">" + node.getName() + "</a> ");
//			}
//		}
//		modelDescription.append("<br/><a target=\"_blank\" href=\"" + modelUrl.toString() + "\">NeuroML Source File</a><br/>");
//		
//		//FIXME: We should improve this once the instance/type refactor is done as we need the cell type
//		if (populationComponents.size() > 0){
//			modelDescription.append("<b>Populations</b><br/>");
//			for (Map.Entry<Standalone, ANode> node : populationComponents.entrySet()){
//				modelDescription.append("Population " + node.getValue().getName() + ": ");
//				Population population = ((Population)node.getKey());
//				
//				for (Map.Entry<Standalone, ANode> cellNode : cellComponents.entrySet()){
//					if (cellNode.getKey().getId().equals(population.getComponent())){
//						modelDescription.append("<a href=\"#\" instancePath=\"$" + cellNode.getValue().getInstancePath() + "$\">" + population.getInstance().size() + " " + cellNode.getValue().getName()  + "</a><br/>");
//					}
//				}
//			}
//			modelDescription.append("<br/>");
//		}
//			
//		if (cellComponents.size() > 0){
//			modelDescription.append("<b>Cells</b><br/>");
//			for (ANode node : cellComponents.values()){
//				modelDescription.append("<a href=\"#\" instancePath=\"$" + node.getInstancePath() + "$\">" + node.getName() + "</a> ");
//			}
//			modelDescription.append("<br/>");
//		}
//		
//		if (ionChannelComponents.size() > 0){
//			modelDescription.append("<b>Channels</b><br/>");
//			for (ANode node : ionChannelComponents.values()){
//				modelDescription.append("<a href=\"#\" instancePath=\"$" + node.getInstancePath() + "$\">" + node.getName() + "</a> ");
//			}
//			modelDescription.append("<br/>");
//		}
//		
//		if (synapseComponents.size() > 0){
//			modelDescription.append("<b>Synapses</b><br/>");
//			for (ANode node : synapseComponents.values()){
//				modelDescription.append("<a href=\"#\" instancePath=\"$" + node.getInstancePath() + "$\">" + node.getName() + "</a> ");
//			}
//			modelDescription.append("<br/>");
//		}
//		
//		if (pulseGeneratorComponents.size() > 0){
//			//FIXME: Pulse generator? InputList? ExplicitList?
//			modelDescription.append("<b>Inputs</b><br/>");
//			for (ANode node : pulseGeneratorComponents.values()){
//				modelDescription.append("<a href=\"#\" instancePath=\"$" + node.getInstancePath() + "$\">" + node.getName() + "</a> ");
//			}
//			modelDescription.append("<br/>");
//		}
//		
//		return new HTMLMetadataNode(Resources.MODEL_DESCRIPTION.getId(), Resources.MODEL_DESCRIPTION.get(), new StringValue(modelDescription.toString()));
//	}
//	
//	public CompositeNode createSummaryNode() throws ModelInterpreterException
//	{
//		CompositeNode summaryNode = new CompositeNode(Resources.SUMMARY.getId(), Resources.SUMMARY.get());
//		summaryNode.addChildren(createInfoNode(infoNode));
//		return summaryNode;
//	}
//	
//	public List<ANode> createInfoNode(InfoNode node) throws ModelInterpreterException
//	{
//		List<ANode> summaryElementList = new ArrayList<ANode>();
//		for(Map.Entry<String, Object> properties : node.getProperties().entrySet())
//		{
//			String keyProperties = properties.getKey();
//			Object valueProperties = properties.getValue();
//			if(!keyProperties.equals("ID"))
//			{
//
//				if(valueProperties == null)
//				{
//					summaryElementList.add(PopulateNodesModelTreeUtils.createTextMetadataNode(PopulateGeneralModelTreeUtils.parseId(keyProperties), keyProperties, new StringValue("")));
//				}
//				else if(valueProperties instanceof String)
//				{
//					summaryElementList.add(PopulateNodesModelTreeUtils.createTextMetadataNode(PopulateGeneralModelTreeUtils.parseId(keyProperties), keyProperties, new StringValue(
//							(String) valueProperties)));
//				}
//				else if(valueProperties instanceof BigInteger)
//				{
//					summaryElementList.add(PopulateNodesModelTreeUtils.createTextMetadataNode(PopulateGeneralModelTreeUtils.parseId(keyProperties), keyProperties, new StringValue(
//							((BigInteger) valueProperties).toString())));
//				}
//				else if(valueProperties instanceof Integer)
//				{
//					summaryElementList.add(PopulateNodesModelTreeUtils.createTextMetadataNode(PopulateGeneralModelTreeUtils.parseId(keyProperties), keyProperties,
//							new StringValue(Integer.toString((Integer) valueProperties))));
//				}
//				else if(valueProperties instanceof ExpressionNode)
//				{
//					ExpressionNode expressionNode = ((ExpressionNode) valueProperties);
//
//					FunctionNode functionNode = new FunctionNode(PopulateGeneralModelTreeUtils.parseId(keyProperties), keyProperties);
//					functionNode.setExpression(expressionNode.getExpression());
//					functionNode.getArgument().add("v");
//
//					PlotMetadataNode plotMetadataNode = expressionNode.getPlotMetadataNode();
//					if(plotMetadataNode != null)
//					{
//						functionNode.getPlotMetadata().put("PlotTitle", plotMetadataNode.getPlotTitle());
//						functionNode.getPlotMetadata().put("XAxisLabel", plotMetadataNode.getXAxisLabel());
//						functionNode.getPlotMetadata().put("YAxisLabel", plotMetadataNode.getYAxisLabel());
//						functionNode.getPlotMetadata().put("InitialValue", Double.toString(plotMetadataNode.getInitialValue()));
//						functionNode.getPlotMetadata().put("FinalValue", Double.toString(plotMetadataNode.getFinalValue()));
//						functionNode.getPlotMetadata().put("StepValue", Double.toString(plotMetadataNode.getStepValue()));
//					}
//					summaryElementList.add(functionNode);
//				}
//				else if(valueProperties instanceof InfoNode)
//				{
//					CompositeNode subSummaryElementNode = new CompositeNode(PopulateGeneralModelTreeUtils.parseId(keyProperties), keyProperties);
//					subSummaryElementNode.addChildren(createInfoNode((InfoNode) valueProperties));
//					summaryElementList.add(subSummaryElementNode);
//				}
//				else if(!(valueProperties instanceof PlotNode))
//				{
//					throw new ModelInterpreterException("Info Writer Node type not supported. Object: " + keyProperties + ". Java class" + valueProperties.getClass());
//				}
//			}
//		}
//		return summaryElementList;
//	}


}
