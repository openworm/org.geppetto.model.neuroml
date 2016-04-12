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

	PopulateDynamicsGate populateDynamicsGate;
	

	public PopulateSummaryNodesUtils(Map<String, List<Type>> typesMap, Type type, URL url, GeppettoModelAccess access, String neuromlContent) throws Throwable
	{
		this.access = access;
		this.typesMap = typesMap;
		this.url = url;
		this.type = type;
		    
		populateDynamicsGate =new PopulateDynamicsGate(neuromlContent, access);
	}

	public Variable getSummaryVariable() throws Throwable
	{
		return createDescriptionNode();
	}

	private Variable createDescriptionNode() throws Throwable
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
			populateDynamicsGate.getAllExpressionNodes();
			modelDescription.append("<b>Channels</b><br/>");
			for(Type ionChannel : ionChannelComponents)
			{
				modelDescription.append("<a href=\"#\" instancePath=\"Model.neuroml." + ionChannel.getId() + "\">" + ionChannel.getName() + "</a> ");

				// Add expresion nodes from the export library for the gate rates
				//addExpresionNodes((CompositeType) ionChannel);
				populateDynamicsGate.addExpresionNodes((CompositeType) ionChannel);
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

	


}
