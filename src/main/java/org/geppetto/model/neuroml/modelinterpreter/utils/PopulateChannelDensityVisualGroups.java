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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.model.GeppettoFactory;
import org.geppetto.model.Tag;
import org.geppetto.model.types.TypesFactory;
import org.geppetto.model.util.GeppettoVisitingException;
import org.geppetto.model.values.PhysicalQuantity;
import org.geppetto.model.values.Unit;
import org.geppetto.model.values.ValuesFactory;
import org.geppetto.model.values.VisualGroup;
import org.geppetto.model.values.VisualGroupElement;
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
	private String type = "static";
	private String highSpectrum = "0XFF0000";
	private String lowSpectrum = "0XFFFF00";
	private String defaultColor = "0XFF3300";

	private static Log _logger = LogFactory.getLog(PopulateChannelDensityVisualGroups.class);

	private CellUtils cellUtils;
	private Cell cell;
	TypesFactory typeFactory;
	ValuesFactory valuesFactory;
	VariablesFactory variablesFactory;
	GeppettoFactory geppettoFactory;
	
	private Tag channelDensityTag;

	public PopulateChannelDensityVisualGroups(Cell cell)
	{
		super();
		this.cell = cell;
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
	 */
	public List<VisualGroup> createChannelDensities() throws GeppettoVisitingException
	{
		Map<String, VisualGroup> groupsMap = new HashMap<String, VisualGroup>();

		// CompositeNode densities = null;

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
		vis.setId(ionChannel);
		vis.setName(ionChannel);
		vis.setType(type);
		vis.setHighSpectrumColor(highSpectrum);
		vis.setLowSpectrumColor(lowSpectrum);
		groupsMap.put(ionChannel, vis);
		return vis;
	}

	private DoubleEvaluator getExpressionEvaluator(String expression)
	{
		DoubleEvaluator doubleEvaluator = null;
		try
		{
			Parser parser = new Parser();
			ParseTree parseTree = parser.parseExpression(expression);
			doubleEvaluator = parseTree.makeFloatEvaluator();
		}
		catch(ParseError | ContentError e2)
		{
			_logger.error("Error creating expression evaluator");
			return null;
		}
		return doubleEvaluator;
	}

	private void createVisualGroupElement(Map<String, VisualGroup> groupsMap, Tag tag, String densityId, String ionChannel, List<VariableParameter> variableParameters)
	{
		// Iterate through the segment groups looking for the right segment group with a variable parameter equals to condDensity
		for(SegmentGroup segmentGroup : cell.getMorphology().getSegmentGroup())
		{
			for(VariableParameter variableParameter : variableParameters)
			{
				if(variableParameter.getParameter().equals("condDensity") && segmentGroup.getId().equals(variableParameter.getSegmentGroup()))
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
							try
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
									element.setId("vo" + sg.getId().toString());
									element.setName(sg.getName());

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
									element.setDefaultColor(defaultColor);
									visualGroup.getVisualGroupElements().add(element);
								}
							}
							catch(NeuroMLException e1)
							{
								_logger.error("Error extracting channel densities");
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
			// densities.addChild(vis);
			groupsMap.put(ionChannel, vis);
		}
	}

	private void createVisualGroupElementFromSegmentGroup(String segmentGroup, String condDensity, VisualGroup vis) throws GeppettoVisitingException
	{
		// VisualGroupElementNode element = new VisualGroupElementNode(segmentGroup);
		VisualGroupElement element = valuesFactory.createVisualGroupElement();
		// element.setName(density.getId());
		element.setParameter(getParameterFromCondDensity(condDensity));
		element.setDefaultColor(defaultColor);
		vis.getVisualGroupElements().add(element);
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

			// Variable variable = variablesFactory.createVariable();
			// initialiseNodeFromString(variable, condDensity);
			// variable.getTypes().add(this.access.getType(TypesPackage.Literals.PARAMETER_TYPE));
			// variable.getInitialValues().put(this.access.getType(TypesPackage.Literals.PARAMETER_TYPE), physicalQuantity);
			return physicalQuantity;
		}
		return null;
	}

}