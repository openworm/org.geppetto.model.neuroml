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
package org.geppetto.model.neuroml.visitors;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.model.quantities.PhysicalQuantity;
import org.geppetto.core.model.quantities.Unit;
import org.geppetto.core.model.runtime.CompositeNode;
import org.geppetto.core.model.runtime.VisualGroupElementNode;
import org.geppetto.core.model.runtime.VisualGroupNode;
import org.geppetto.core.model.values.FloatValue;
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
import org.neuroml.model.Include;
import org.neuroml.model.InhomogeneousParameter;
import org.neuroml.model.Point3DWithDiam;
import org.neuroml.model.Segment;
import org.neuroml.model.SegmentGroup;
import org.neuroml.model.VariableParameter;
import org.neuroml.model.util.CellUtils;
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

	private double calculateDistanceToGroup(double distance, Segment segment, LinkedHashMap<Integer, Segment> idsVsSegments, List<Integer> segmentsPerGroup)
	{
		if(!segmentsPerGroup.contains(segment))
		{
			Point3DWithDiam proximal = (segment.getProximal() == null) ? idsVsSegments.get(segment.getParent().getSegment()).getDistal() : segment.getProximal();
			distance += CellUtils.distance(proximal, segment.getDistal());
		}

		if(segment.getParent() != null)
		{
			return calculateDistanceToGroup(distance, idsVsSegments.get(segment.getParent().getSegment()), idsVsSegments, segmentsPerGroup);
		}
		return distance;
	}

	private double calculareDistanceInGroup(double distance, Segment segment, LinkedHashMap<Integer, Segment> idsVsSegments, List<Integer> segmentsPerGroup)
	{
		if(segmentsPerGroup.contains(segment))
		{
			Point3DWithDiam proximal = (segment.getProximal() == null) ? idsVsSegments.get(segment.getParent().getSegment()).getDistal() : segment.getProximal();
			distance += CellUtils.distance(proximal, segment.getDistal());
		}

		if(segment.getParent() != null && segmentsPerGroup.contains(segment.getParent().getSegment()))
		{
			return calculareDistanceInGroup(distance, idsVsSegments.get(segment.getParent().getSegment()), idsVsSegments, segmentsPerGroup);
		}
		return distance;
	}

	/**
	 * Create Channel densities visual grups for a cell
	 * 
	 * @param cell
	 *            - Densities visual groups for this cell
	 * @return
	 */
	public CompositeNode createChannelDensities(Cell cell)
	{

		Map<String, VisualGroupNode> groupsMap = new HashMap<String, VisualGroupNode>();

		CompositeNode densities = null;

		BiophysicalProperties biophysicalProperties = cell.getBiophysicalProperties();
		if(biophysicalProperties != null
				&& biophysicalProperties.getMembraneProperties() != null
				&& (biophysicalProperties.getMembraneProperties().getChannelDensity() != null || biophysicalProperties.getMembraneProperties().getChannelDensityNernst() != null || biophysicalProperties
						.getMembraneProperties().getChannelDensityNonUniform() != null))
		{
			densities = new CompositeNode("ChannelDensities");
			densities.setName("Channel Densities");

			for(ChannelDensity density : cell.getBiophysicalProperties().getMembraneProperties().getChannelDensity())
			{
				createVisualGroupFromCondDensity(groupsMap, densities, density, "Density_Nernst_" + density.getIonChannel(), density.getSegmentGroup(), density.getCondDensity());
			}

			for(ChannelDensityNernst density : cell.getBiophysicalProperties().getMembraneProperties().getChannelDensityNernst())
			{
				createVisualGroupFromCondDensity(groupsMap, densities, density, "Density_Nernst_" + density.getIonChannel(), density.getSegmentGroup(), density.getCondDensity());
			}

			for(ChannelDensityNonUniform density : biophysicalProperties.getMembraneProperties().getChannelDensityNonUniform())
			{
				createVisualGroupElement(groupsMap, densities, density, cell);
			}

		}

		return densities;
	}

	private VisualGroupNode createVisualGroup(Map<String, VisualGroupNode> groupsMap, CompositeNode densities, String ionChannel)
	{
		VisualGroupNode vis = new VisualGroupNode(ionChannel);
		vis.setName(ionChannel);
		vis.setType(type);
		vis.setHighSpectrumColor(highSpectrum);
		vis.setLowSpectrumColor(lowSpectrum);
		vis.setParent(densities);
		densities.addChild(vis);
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

	private void createVisualGroupElement(Map<String, VisualGroupNode> groupsMap, CompositeNode densities, ChannelDensityNonUniform density, Cell cell)
	{

		if(!density.getId().equals("Leak_all"))
		{
			//Get all segments in the cell
			LinkedHashMap<Integer, Segment> idsVsSegments = CellUtils.getIdsVsSegments(cell);

			// Iterate through the segment groups looking for the right segment group with a variable parameter equals to condDensity
			for(SegmentGroup segmentGroup : cell.getMorphology().getSegmentGroup())
			{
				for(VariableParameter variableParameter : density.getVariableParameter())
				{
					if(variableParameter.getParameter().equals("condDensity") && segmentGroup.getId().equals(variableParameter.getSegmentGroup()))
					{
						String ionChannel = "Density_Non_Uniform_" + density.getIonChannel() + "_" + segmentGroup.getId();
						VisualGroupNode vis = createVisualGroup(groupsMap, densities, ionChannel);
						
						// Get expression evaluator for inhomogeneous expresion
						DoubleEvaluator doubleEvaluator = getExpressionEvaluator(variableParameter.getInhomogeneousValue().getValue());

						// Look for all segment id in this segment group
						List<Integer> segmentsPerGroup = CellUtils.getSegmentIdsInGroup(cell, segmentGroup.getId());

						// Get the inhomogeneous parameter for the segment group
						for(InhomogeneousParameter inhomogeneousParameter : segmentGroup.getInhomogeneousParameter())
						{
							if(inhomogeneousParameter.getId().equals(variableParameter.getInhomogeneousValue().getInhomogeneousParameter()))
							{
								// FIXME: add translation
								inhomogeneousParameter.getProximal().getTranslationStart();

								// Iterate segment group and create a visual group element node for each subsegment
								for(Include subSegment : segmentGroup.getInclude())
								{
									// Calculate average distance for all segments in the sub segment group
									double averageDistance = calculateDistanceForSegmentGroup(cell, segmentGroup, segmentsPerGroup, idsVsSegments, subSegment);

									// Calculate conductance density
									HashMap<String, Double> valHM = new HashMap<String, Double>();
									valHM.put(inhomogeneousParameter.getVariable(), averageDistance);

									// Create visual group element
									VisualGroupElementNode element = new VisualGroupElementNode(subSegment.getSegmentGroup());
									element.setName(density.getId());

									// Add calculated value as a physical quantity
									// FIXME We are hardcoding the units
									PhysicalQuantity physicalQuantity = new PhysicalQuantity(new FloatValue((float) doubleEvaluator.evalD(valHM)), new Unit("uF_per_cm2"));
									element.setParameter(physicalQuantity);
									element.setParent(vis);
									element.setDefaultColor(defaultColor);
									vis.getVisualGroupElements().add(element);

								}

							}
						}
					}
				}

			}
		}

	}

	private double calculateDistanceForSegmentGroup(Cell cell, SegmentGroup segmentGroup, List<Integer> segmentsPerGroup, LinkedHashMap<Integer, Segment> idsVsSegments, Include sgInclude)
	{
		double distanceToGroup = 0.0;
		double distanceInGroup = 0.0;
		List<Segment> segmentsPerSubgroup = null;
		try
		{
			segmentsPerSubgroup = CellUtils.getSegmentsInGroup(cell, sgInclude.getSegmentGroup());
		}
		catch(NeuroMLException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for(Segment sg : segmentsPerSubgroup)
		{
			if(distanceToGroup == 0.0) distanceToGroup = calculateDistanceToGroup(0.0, sg, idsVsSegments, segmentsPerGroup);
			distanceInGroup += calculareDistanceInGroup(distanceInGroup, sg, idsVsSegments, segmentsPerGroup);
		}

		double averageDistance = (distanceInGroup + distanceToGroup) / segmentGroup.getInclude().size();
		return averageDistance;
	}

	private void createVisualGroupFromCondDensity(Map<String, VisualGroupNode> groupsMap, CompositeNode densities, Base density, String ionChannel, String segmentGroup, String condDensity)
	{
		if(!groupsMap.containsKey(ionChannel))
		{
			if(!density.getId().endsWith("_all"))
			{
				VisualGroupNode vis = createVisualGroup(groupsMap, densities, ionChannel);
				createVisualGroupElementFromSegmentGroup(density, segmentGroup, condDensity, vis);
			}

		}
		else
		{
			VisualGroupNode vis = groupsMap.get(ionChannel);

			if(!density.getId().endsWith("_all")) createVisualGroupElementFromSegmentGroup(density, segmentGroup, condDensity, vis);

			densities.addChild(vis);
			groupsMap.put(ionChannel, vis);
		}
	}

	private void createVisualGroupElementFromSegmentGroup(Base density, String segmentGroup, String condDensity, VisualGroupNode vis)
	{
		VisualGroupElementNode element = new VisualGroupElementNode(segmentGroup);
		element.setName(density.getId());
		element.setParameter(getParameterFromCondDensity(condDensity));
		element.setParent(vis);
		element.setDefaultColor(defaultColor);
		vis.getVisualGroupElements().add(element);
	}

	private PhysicalQuantity getParameterFromCondDensity(String condDensity)
	{
		String regExp = "\\s*([0-9-]*\\.?[0-9]*[eE]?[-+]?[0-9]+)?\\s*(\\w*)";
		Pattern pattern = Pattern.compile(regExp);
		Matcher matcher = pattern.matcher(condDensity);

		PhysicalQuantity physicalQuantity = null;
		if(matcher.find())
		{
			physicalQuantity = new PhysicalQuantity(new FloatValue(Float.parseFloat(matcher.group(1))), new Unit(matcher.group(2)));
		}
		return physicalQuantity;
	}

}