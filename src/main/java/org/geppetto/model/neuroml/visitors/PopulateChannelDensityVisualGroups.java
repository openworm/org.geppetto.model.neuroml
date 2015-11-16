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
import org.neuroml.model.ChannelDensityNonUniformNernst;
import org.neuroml.model.Include;
import org.neuroml.model.InhomogeneousParameter;
import org.neuroml.model.IonChannel;
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

	
	private Cell cell;
	//Get all segments in the cell
	LinkedHashMap<Integer, Segment> idsVsSegments;
	
	public PopulateChannelDensityVisualGroups(Cell cell)
	{
		super();
		this.cell = cell;
	}

	/**
	 * Create Channel densities visual groups for a cell
	 * 
	 * @param cell
	 *            - Densities visual groups for this cell
	 * @return
	 */
	public CompositeNode createChannelDensities()
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

			if (biophysicalProperties.getMembraneProperties().getChannelDensity() != null && biophysicalProperties.getMembraneProperties().getChannelDensity().size() > 0){
				CompositeNode densityNode = new CompositeNode("Density");
				densityNode.setName("Density");
				densityNode.setParent(densities);
				densities.addChild(densityNode);
				
				for(ChannelDensity density : cell.getBiophysicalProperties().getMembraneProperties().getChannelDensity())
				{
					createVisualGroupFromCondDensity(groupsMap, densityNode, density, density.getIonChannel(), density.getSegmentGroup(), density.getCondDensity());
				}
			}
			
			if (biophysicalProperties.getMembraneProperties().getChannelDensityNernst() != null && biophysicalProperties.getMembraneProperties().getChannelDensityNernst().size() > 0){
				CompositeNode densityNode = new CompositeNode("Density_Nernst");
				densityNode.setName("Density Nernst");
				densityNode.setParent(densities);
				densities.addChild(densityNode);
				
				for(ChannelDensityNernst density : cell.getBiophysicalProperties().getMembraneProperties().getChannelDensityNernst())
				{
					createVisualGroupFromCondDensity(groupsMap, densityNode, density, density.getIonChannel(), density.getSegmentGroup(), density.getCondDensity());
				}
			}

			if (biophysicalProperties.getMembraneProperties().getChannelDensityNonUniform() != null && biophysicalProperties.getMembraneProperties().getChannelDensityNonUniform().size() > 0){
				CompositeNode densityNode = new CompositeNode("Density_Non_Uniform");
				densityNode.setName("Density Non Uniform");
				densityNode.setParent(densities);
				densities.addChild(densityNode);
				
				idsVsSegments = CellUtils.getIdsVsSegments(cell);
				for(ChannelDensityNonUniform density : biophysicalProperties.getMembraneProperties().getChannelDensityNonUniform())
				{
					createVisualGroupElement(groupsMap, densityNode, density.getId(), density.getIonChannel(), density.getVariableParameter());
				}
			}
			
			if (biophysicalProperties.getMembraneProperties().getChannelDensityNonUniformNernst() != null && biophysicalProperties.getMembraneProperties().getChannelDensityNonUniformNernst().size() > 0){
				CompositeNode densityNode = new CompositeNode("Density_Non_Uniform_Nernst");
				densityNode.setName("Density Non Uniform Nernst");
				densityNode.setParent(densities);
				densities.addChild(densityNode);
				
				idsVsSegments = CellUtils.getIdsVsSegments(cell);
				for(ChannelDensityNonUniformNernst density : biophysicalProperties.getMembraneProperties().getChannelDensityNonUniformNernst())
				{
					createVisualGroupElement(groupsMap, densityNode, density.getId(), density.getIonChannel(), density.getVariableParameter());
				}
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

	private void createVisualGroupElement(Map<String, VisualGroupNode> groupsMap, CompositeNode densities, String densityId, String ionChannel, List<VariableParameter> variableParameters)
	{

			// Iterate through the segment groups looking for the right segment group with a variable parameter equals to condDensity
			for(SegmentGroup segmentGroup : cell.getMorphology().getSegmentGroup())
			{
				for(VariableParameter variableParameter : variableParameters)
				{
					if(variableParameter.getParameter().equals("condDensity") && segmentGroup.getId().equals(variableParameter.getSegmentGroup()))
					{
						String ionChannelLabel = ionChannel + "_" + segmentGroup.getId();
						VisualGroupNode vis = createVisualGroup(groupsMap, densities, ionChannelLabel);
						
						// Get expression evaluator for inhomogeneous expresion
						DoubleEvaluator doubleEvaluator = getExpressionEvaluator(variableParameter.getInhomogeneousValue().getValue());

						// Get the inhomogeneous parameter for the segment group
						for(InhomogeneousParameter inhomogeneousParameter : segmentGroup.getInhomogeneousParameter())
						{
							if(inhomogeneousParameter.getId().equals(variableParameter.getInhomogeneousValue().getInhomogeneousParameter()))
							{
								// Look for all segment id in this segment group
								//List<Integer> segmentsPerGroup = CellUtils.getSegmentIdsInGroup(cell, segmentGroup.getId());

								// Calculate average distance for all segments in the sub segment group
								//double averageDistance = calculateDistanceForSegmentGroup(cell, segmentGroup, inhomogeneousParameter.getProximal().getTranslationStart());

								try
								{
									//Get all segments for the subgroup
									List<Segment> segmentsPerSubgroup = CellUtils.getSegmentsInGroup(cell, segmentGroup.getId());
									for(Segment sg : segmentsPerSubgroup)
									{
										double distanceInGroup = calculateDistanceInGroup(0.0, sg);
										double distanceAllSegments = distanceInGroup - inhomogeneousParameter.getProximal().getTranslationStart();
										
										// Calculate conductance density
										HashMap<String, Double> valHM = new HashMap<String, Double>();
										valHM.put(inhomogeneousParameter.getVariable(), distanceAllSegments);

										// Create visual group element
										VisualGroupElementNode element = new VisualGroupElementNode(sg.getId().toString());
										element.setName(sg.getName());

										// Add calculated value as a physical quantity
										// FIXME We are hardcoding the units
										PhysicalQuantity physicalQuantity = new PhysicalQuantity(new FloatValue((float) doubleEvaluator.evalD(valHM)), new Unit("S_per_cm2"));
										element.setParameter(physicalQuantity);
										element.setParent(vis);
										element.setDefaultColor(defaultColor);
										vis.getVisualGroupElements().add(element);
									}

								}
								catch(NeuroMLException e1)
								{
									_logger.error("Error extracting channel densities");
								}
								
								
								// Iterate segment group and create a visual group element node for each subsegment
//								for(Include include : segmentGroup.getInclude())
//								{
//									// Calculate average distance for all segments in the sub segment group
//									double averageDistance = calculateDistanceForSegmentGroup(cell, segmentsPerGroup, include, inhomogeneousParameter.getProximal().getTranslationStart());
//
//									// Calculate conductance density
//									HashMap<String, Double> valHM = new HashMap<String, Double>();
//									valHM.put(inhomogeneousParameter.getVariable(), averageDistance);
//
//									// Create visual group element
//									VisualGroupElementNode element = new VisualGroupElementNode(include.getSegmentGroup());
//									element.setName(densityId + "_" + include.getSegmentGroup());
//
//									// Add calculated value as a physical quantity
//									// FIXME We are hardcoding the units
//									PhysicalQuantity physicalQuantity = new PhysicalQuantity(new FloatValue((float) doubleEvaluator.evalD(valHM)), new Unit("S_per_cm2"));
//									element.setParameter(physicalQuantity);
//									element.setParent(vis);
//									element.setDefaultColor(defaultColor);
//									vis.getVisualGroupElements().add(element);
//
//								}

							}
						}
					}
				}

			}

	}
	
//	private double calculateDistanceToGroup(double distance, Segment segment, List<Integer> segmentsPerGroup)
//	{
//		if(!segmentsPerGroup.contains(segment.getId()))
//		{
//			Point3DWithDiam proximal = (segment.getProximal() == null) ? idsVsSegments.get(segment.getParent().getSegment()).getDistal() : segment.getProximal();
//			distance += CellUtils.distance(proximal, segment.getDistal());
//		}
//
//		if(segment.getParent() != null)
//		{
//			return calculateDistanceToGroup(distance, idsVsSegments.get(segment.getParent().getSegment()), segmentsPerGroup);
//		}
//		return distance;
//	}

	private double calculateDistanceInGroup(double distance, Segment segment)
	{
		Point3DWithDiam proximal = (segment.getProximal() == null) ? idsVsSegments.get(segment.getParent().getSegment()).getDistal() : segment.getProximal();
		distance += CellUtils.distance(proximal, segment.getDistal());

		if(segment.getParent() != null)
		{
			return calculateDistanceInGroup(distance, idsVsSegments.get(segment.getParent().getSegment()));
		}
		return distance;
	}

	private double calculateDistanceForSegmentGroup(Cell cell,SegmentGroup segmentGroup, double translationStart)
	{
		double distanceAllSegments = 0.0;
		//double distanceToGroup = 0.0;
		try
		{
			//Get all segments for the subgroup
			List<Segment> segmentsPerSubgroup = CellUtils.getSegmentsInGroup(cell, segmentGroup.getId());
			//Calculate distance to group
			//if(distanceToGroup == 0.0) distanceToGroup = calculateDistanceToGroup(0.0, segmentsPerSubgroup.get(0), segmentsPerGroup);
			// Calculate inner distance for each segment
			for(Segment sg : segmentsPerSubgroup)
			{
				double distanceInGroup = calculateDistanceInGroup(0.0, sg);
				distanceAllSegments += distanceInGroup - translationStart;
			}

			return distanceAllSegments / segmentsPerSubgroup.size();
		}
		catch(NeuroMLException e1)
		{
			_logger.error("Error extracting channel densities");
		}
		return 0.0;
		
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