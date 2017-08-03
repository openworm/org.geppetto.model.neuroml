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
package org.geppetto.model.neuroml.visualUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.model.GeppettoModelAccess;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.model.GeppettoFactory;
import org.geppetto.model.Tag;
import org.geppetto.model.neuroml.modelInterpreterUtils.NeuroMLModelInterpreterUtils;
import org.geppetto.model.neuroml.utils.CellUtils;
import org.geppetto.model.neuroml.utils.Resources;
import org.geppetto.model.types.TypesFactory;
import org.geppetto.model.types.TypesPackage;
import org.geppetto.model.util.GeppettoVisitingException;
import org.geppetto.model.values.PhysicalQuantity;
import org.geppetto.model.values.Unit;
import org.geppetto.model.values.ValuesFactory;
import org.geppetto.model.values.VisualGroup;
import org.geppetto.model.values.VisualGroupElement;
import org.geppetto.model.values.VisualValue;
import org.geppetto.model.variables.Variable;
import org.geppetto.model.variables.VariablesFactory;
import org.lemsml.jlems.core.eval.DoubleEvaluator;
import org.lemsml.jlems.core.expression.ParseError;
import org.lemsml.jlems.core.expression.ParseTree;
import org.lemsml.jlems.core.expression.Parser;
import org.lemsml.jlems.core.sim.ContentError;
import org.neuroml.model.Base;
import org.neuroml.model.BiophysicalProperties;
import org.neuroml.model.Cell;
import org.neuroml.model.ChannelDensity;
import org.neuroml.model.ChannelDensityNernst;
import org.neuroml.model.ChannelDensityNonUniform;
import org.neuroml.model.ChannelDensityNonUniformNernst;
import org.neuroml.model.InhomogeneousParameter;
import org.neuroml.model.Segment;
import org.neuroml.model.SegmentGroup;
import org.neuroml.model.VariableParameter;
import org.neuroml.model.util.NeuroMLException;

/**
 * Helper class to populate visualization tree for neuroml models
 * 
 */
public class PopulateChannelDensityVisualGroups
{
	private static Log _logger = LogFactory.getLog(PopulateChannelDensityVisualGroups.class);

	private CellUtils cellUtils;
	private Cell cell;
	private LinkedHashMap<String, List<Segment>> segmentGroupSegMap;
	private Map<Integer, Variable> segmentIdsvisualObjectsSegments;
	private GeppettoModelAccess access;
	
	TypesFactory typeFactory;
	ValuesFactory valuesFactory;
	VariablesFactory variablesFactory;
	GeppettoFactory geppettoFactory;
	
	private Tag channelDensityTag;
	
	public PopulateChannelDensityVisualGroups(Cell cell, LinkedHashMap<String, List<Segment>> segmentGroupSegMap, Map<Integer, Variable> segmentIdsvisualObjectsSegments, GeppettoModelAccess access)
	{
		super();
		this.cell = cell;
		this.segmentGroupSegMap = segmentGroupSegMap;
		this.segmentIdsvisualObjectsSegments = segmentIdsvisualObjectsSegments;
		this.access = access;
		typeFactory = TypesFactory.eINSTANCE;
		valuesFactory = ValuesFactory.eINSTANCE;
		variablesFactory = VariablesFactory.eINSTANCE;
		geppettoFactory = GeppettoFactory.eINSTANCE;
	}


	public Tag getChannelDensityTag()
	{
		return channelDensityTag;
	}

	/**
	 * Create Channel densities visual groups for a cell
	 * 
	 * @param cell
	 *            - Densities visual groups for this cell
	 * @return
	 * @throws GeppettoVisitingException
	 * @throws ModelInterpreterException
	 * @throws NeuroMLException
	 * @throws ParseError
	 * @throws ContentError
	 */
	public List <VisualGroup> createChannelDensities() throws GeppettoVisitingException, ModelInterpreterException, NeuroMLException, ContentError, ParseError
	{
		Map<String, VisualGroup> groupsMap = new HashMap<String, VisualGroup>();

		BiophysicalProperties biophysicalProperties = cell.getBiophysicalProperties();
		if(biophysicalProperties != null
				&& biophysicalProperties.getMembraneProperties() != null
				&& (biophysicalProperties.getMembraneProperties().getChannelDensity() != null || biophysicalProperties.getMembraneProperties().getChannelDensityNernst() != null || biophysicalProperties
						.getMembraneProperties().getChannelDensityNonUniform() != null))
		{
			channelDensityTag = geppettoFactory.createTag();
			channelDensityTag.setName("Channel Densities");

			if(biophysicalProperties.getMembraneProperties().getChannelDensity() != null && biophysicalProperties.getMembraneProperties().getChannelDensity().size() > 0)
			{
				Tag channelDensityTypeTag = geppettoFactory.createTag();
				channelDensityTypeTag.setName("Density");
				channelDensityTag.getTags().add(channelDensityTypeTag);

				for(ChannelDensity density : cell.getBiophysicalProperties().getMembraneProperties().getChannelDensity())
				{
					createVisualGroupFromCondDensity(groupsMap, channelDensityTypeTag, density, density.getIonChannel(), density.getSegmentGroup(), density.getCondDensity());
				}
			}

			if(biophysicalProperties.getMembraneProperties().getChannelDensityNernst() != null && biophysicalProperties.getMembraneProperties().getChannelDensityNernst().size() > 0)
			{
				Tag channelDensityTypeTag = geppettoFactory.createTag();
				channelDensityTypeTag.setName("Density Nernst");
				channelDensityTag.getTags().add(channelDensityTypeTag);

				for(ChannelDensityNernst density : cell.getBiophysicalProperties().getMembraneProperties().getChannelDensityNernst())
				{
					createVisualGroupFromCondDensity(groupsMap, channelDensityTypeTag, density, density.getIonChannel(), density.getSegmentGroup(), density.getCondDensity());
				}
			}

			if(biophysicalProperties.getMembraneProperties().getChannelDensityNonUniform() != null && biophysicalProperties.getMembraneProperties().getChannelDensityNonUniform().size() > 0)
			{
				Tag channelDensityTypeTag = geppettoFactory.createTag();
				channelDensityTypeTag.setName("Density Non Uniform");
				channelDensityTag.getTags().add(channelDensityTypeTag);

				cellUtils = new CellUtils(cell);
				for(ChannelDensityNonUniform density : biophysicalProperties.getMembraneProperties().getChannelDensityNonUniform())
				{
					createVisualGroupElement(groupsMap, channelDensityTypeTag, density.getId(), density.getIonChannel(), density.getVariableParameter());
				}
			}

			if(biophysicalProperties.getMembraneProperties().getChannelDensityNonUniformNernst() != null
					&& biophysicalProperties.getMembraneProperties().getChannelDensityNonUniformNernst().size() > 0)
			{
				Tag channelDensityTypeTag = geppettoFactory.createTag();
				channelDensityTypeTag.setName("Density Non Uniform Nernst");
				channelDensityTag.getTags().add(channelDensityTypeTag);

				cellUtils = new CellUtils(cell);
				for(ChannelDensityNonUniformNernst density : biophysicalProperties.getMembraneProperties().getChannelDensityNonUniformNernst())
				{
					createVisualGroupElement(groupsMap, channelDensityTypeTag, density.getId(), density.getIonChannel(), density.getVariableParameter());
				}
			}

		}

		return new ArrayList<VisualGroup>(groupsMap.values());
	}

	private VisualGroup createVisualGroup(Map<String, VisualGroup> groupsMap, String ionChannel)
	{
		VisualGroup vis = valuesFactory.createVisualGroup();
		NeuroMLModelInterpreterUtils.initialiseNodeFromString(vis, ionChannel);
		vis.setHighSpectrumColor(ModelInterpreterVisualConstants.HIGH_SPECTRUM);
		vis.setLowSpectrumColor(ModelInterpreterVisualConstants.LOW_SPECTRUM);
		groupsMap.put(ionChannel, vis);
		return vis;
	}

	public static DoubleEvaluator getExpressionEvaluator(String expression) throws ContentError, ParseError
	{
		Parser parser = new Parser();
		ParseTree parseTree = parser.parseExpression(expression);
		return parseTree.makeFloatEvaluator();
	}

	private void createVisualGroupElement(Map<String, VisualGroup> groupsMap, Tag tag, String densityId, String ionChannel, List<VariableParameter> variableParameters) throws NeuroMLException,
			ContentError, ParseError
	{
		// Iterate through the segment groups looking for the right segment group with a variable parameter equals to condDensity
		for(SegmentGroup segmentGroup : cell.getMorphology().getSegmentGroup())
		{
			for(VariableParameter variableParameter : variableParameters)
			{
				if(variableParameter.getParameter().equals(Resources.COND_DENSITY.getId()) && segmentGroup.getId().equals(variableParameter.getSegmentGroup()))
				{
					String ionChannelLabel = ionChannel + "_" + segmentGroup.getId();
					VisualGroup visualGroup = createVisualGroup(groupsMap, ionChannelLabel);
					visualGroup.getTags().add(tag);

					// Get expression evaluator for inhomogeneous expresion
					DoubleEvaluator doubleEvaluator = getExpressionEvaluator(variableParameter.getInhomogeneousValue().getValue());

					// Get the inhomogeneous parameter for the segment group
					for(InhomogeneousParameter inhomogeneousParameter : segmentGroup.getInhomogeneousParameter())
					{
						if(inhomogeneousParameter.getId().equals(variableParameter.getInhomogeneousValue().getInhomogeneousParameter()))
						{
							// Get all segments for the subgroup
							List<Segment> segmentsPerSubgroup = cellUtils.getSegmentsInGroup(segmentGroup.getId());
							for(Segment sg : segmentsPerSubgroup)
							{
								double distanceAllSegments = cellUtils.calculateDistanceInGroup(0.0, sg);
								if(inhomogeneousParameter.getProximal() != null) distanceAllSegments = distanceAllSegments - inhomogeneousParameter.getProximal().getTranslationStart();

								// double distanceAllSegments = distanceInGroup - inhomogeneousParameter.getProximal().getTranslationStart();

								// Calculate conductance density
								HashMap<String, Double> valHM = new HashMap<String, Double>();
								valHM.put(inhomogeneousParameter.getVariable(), distanceAllSegments);

								// Create visual group element
								VisualGroupElement element = valuesFactory.createVisualGroupElement();
								NeuroMLModelInterpreterUtils.initialiseNodeFromString(element, NeuroMLModelInterpreterUtils.getVisualObjectIdentifier(sg));

								// Add calculated value as a physical quantity
								// FIXME We are hardcoding the units as NeuroML2 does not have it for inhomogeneous channels
								PhysicalQuantity physicalQuantity = valuesFactory.createPhysicalQuantity();
								// physicalQuantity.setScalingFactor(value);
								physicalQuantity.setValue((float) doubleEvaluator.evalD(valHM));

								Unit unit = valuesFactory.createUnit();
								unit.setUnit("S_per_cm2");
								physicalQuantity.setUnit(unit);
								physicalQuantity.setScalingFactor(1);

								element.setParameter(physicalQuantity);
								visualGroup.getVisualGroupElements().add(element);
							}
						}
					}
				}
			}
		}
	}

	private void createVisualGroupFromCondDensity(Map<String, VisualGroup> groupsMap, Tag tag, Base density, String ionChannel, String segmentGroup, String condDensity)
			throws GeppettoVisitingException
	{
		if(!groupsMap.containsKey(ionChannel))
		{
			if(!density.getId().endsWith("_all"))
			{
				VisualGroup vis = createVisualGroup(groupsMap, ionChannel);
				vis.getTags().add(tag);
				createVisualGroupElementFromSegmentGroup(segmentGroup, condDensity, vis);
			}
		}
		else
		{
			VisualGroup vis = groupsMap.get(ionChannel);
			if(!density.getId().endsWith("_all")) createVisualGroupElementFromSegmentGroup(segmentGroup, condDensity, vis);
			groupsMap.put(ionChannel, vis);
		}
	}

	private void createVisualGroupElementFromSegmentGroup(String segmentGroup, String condDensity, VisualGroup vis) throws GeppettoVisitingException
	{
		VisualGroupElement element = valuesFactory.createVisualGroupElement();
		element.setId(vis.getId() + "_" + segmentGroup);
		element.setName(vis.getName() + "_" + segmentGroup);
		element.setParameter(getParameterFromCondDensity(condDensity));
		//element.setDefaultColor(defaultColor);
		vis.getVisualGroupElements().add(element);

		if (segmentGroupSegMap.containsKey(segmentGroup)){
			for (Segment segment : segmentGroupSegMap.get(segmentGroup)){
				((VisualValue)segmentIdsvisualObjectsSegments.get(segment.getId()).getInitialValues().get(this.access.getType(TypesPackage.Literals.VISUAL_TYPE))).getGroupElements().add(element);
			}
		}
		else if (segmentGroup.equals("all")) {
			for (Variable visualObject : segmentIdsvisualObjectsSegments.values()){
				((VisualValue)visualObject.getInitialValues().get(this.access.getType(TypesPackage.Literals.VISUAL_TYPE))).getGroupElements().add(element);
			}
		}
		

	}

	private PhysicalQuantity getParameterFromCondDensity(String condDensity) throws GeppettoVisitingException
	{
		String regExp = "\\s*([0-9-]*\\.?[0-9]*[eE]?[-+]?[0-9]+)?\\s*(\\w*)";
		Pattern pattern = Pattern.compile(regExp);
		Matcher matcher = pattern.matcher(condDensity);

		if(matcher.find())
		{
			PhysicalQuantity physicalQuantity = valuesFactory.createPhysicalQuantity();
			// physicalQuantity.setScalingFactor(value);
			physicalQuantity.setValue(Float.parseFloat(matcher.group(1)));

			Unit unit = valuesFactory.createUnit();
			unit.setUnit(matcher.group(2));
			physicalQuantity.setUnit(unit);
			physicalQuantity.setScalingFactor(1);

			return physicalQuantity;
		}
		return null;
	}
	
}