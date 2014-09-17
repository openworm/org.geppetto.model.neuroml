/*******************************************************************************
 * The MIT License (MIT)
 * 
 * Copyright (c) 2011, 2013 OpenWorm.
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
package org.geppetto.model.neuroml.services;

import java.util.List;

import org.geppetto.core.model.quantities.PhysicalQuantity;
import org.geppetto.core.model.runtime.AspectSubTreeNode;
import org.geppetto.core.model.runtime.CompositeNode;
import org.geppetto.core.model.runtime.DynamicsSpecificationNode;
import org.geppetto.core.model.runtime.FunctionNode;
import org.geppetto.core.model.runtime.ParameterSpecificationNode;
import org.geppetto.core.model.runtime.TextMetadataNode;
import org.geppetto.core.model.values.StringValue;
import org.neuroml.model.BiophysicalProperties;
import org.neuroml.model.Cell;
import org.neuroml.model.ChannelDensity;
import org.neuroml.model.ChannelPopulation;
import org.neuroml.model.InitMembPotential;
import org.neuroml.model.IntracellularProperties;
import org.neuroml.model.MembraneProperties;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.SpecificCapacitance;
import org.neuroml.model.SpikeThresh;

/**
 * Populates the Model Tree of Aspect
 * 
 * @author  Jesus R. Martinez (jesus@metacell.us)
 *
 */
public class PopulateModelTree {

	private boolean _populated = false;
	
	public PopulateModelTree() {		
	}
	
	/**
	 * Method that is contacted to start populating the model tree
	 * 
	 * @param modelTree - Model tree that is to be populated
	 * @param neuroml - NeuroMLDocument used to populate the tree, values are in here
	 * @return 
	 */
	public boolean populateModelTree(AspectSubTreeNode modelTree, NeuroMLDocument neuroml)
	{		
		//FIXME : Remove and apply data accurately
 		List<Cell> cells = neuroml.getCell();
 		for(Cell c : cells){
 			addProperties(modelTree, c.getBiophysicalProperties());
 		}
 		
 		return _populated;
	}

	/**
	 * Using properties found in NeuroMLDocument to populate the tree, this is where 
	 * the creationg of the nodes happen
	 * 
	 * @param modelTree - Object containing model tree
	 * @param properties - Model properties extracted from neuroml document object
	 */
	public void addProperties(AspectSubTreeNode modelTree,BiophysicalProperties properties){
		//FIXME : Remove and apply data accurately
		if(properties != null)
		{
			CompositeNode biophysicalPropertiesNode = new CompositeNode(properties.getId());
			biophysicalPropertiesNode.setId(properties.getId());
			
			MembraneProperties membraneProperties = properties.getMembraneProperties();
			if(membraneProperties != null)
			{
				CompositeNode membranePropertiesNode = new CompositeNode("membraneProperties");
				membranePropertiesNode.setId("membraneProperties");
				
				List<ChannelDensity> channelDensities = membraneProperties.getChannelDensity();
				List<ChannelPopulation> channelPopulations = membraneProperties.getChannelPopulation();
				List<SpecificCapacitance> specificCapacitance = membraneProperties.getSpecificCapacitance();
				List<SpikeThresh> spikeThresh = membraneProperties.getSpikeThresh();
				List<InitMembPotential> initMembPotential = membraneProperties.getInitMembPotential();

				for(ChannelDensity channelDensity : channelDensities)
				{
					CompositeNode channelDensityNode = new CompositeNode(channelDensity.getId());
					channelDensityNode.setId(channelDensity.getId());
					
					ParameterSpecificationNode condDensityNode = new ParameterSpecificationNode("condDensity");
					condDensityNode.setId("condDensity");
					String[] condDensityValueArray = channelDensity.getCondDensity().split(" ");
					PhysicalQuantity condDensityValue = new PhysicalQuantity();
					condDensityValue.setUnit(condDensityValueArray[1]);
					condDensityValue.setValue(new StringValue(condDensityValueArray[0]));
					condDensityNode.setValue(condDensityValue);
					channelDensityNode.addChild(condDensityNode);
					
					TextMetadataNode textMetadataNode = new TextMetadataNode("ion");
					textMetadataNode.setId("ion");
					textMetadataNode.setValue(new StringValue(channelDensity.getIon()));
					channelDensityNode.addChild(textMetadataNode);
					
					//TODO: We need to work on this nested element
					CompositeNode ionChannelNode = new CompositeNode(channelDensity.getIonChannel());
					ionChannelNode.setId(channelDensity.getIonChannel());
					channelDensityNode.addChild(ionChannelNode);
										
					ParameterSpecificationNode erevNode = new ParameterSpecificationNode("erev");
					erevNode.setId("erev");
					String[] erevValueArray = channelDensity.getErev().split(" ");
					PhysicalQuantity erevValue = new PhysicalQuantity();
					erevValue.setUnit(erevValueArray[1]);
					erevValue.setValue(new StringValue(erevValueArray[0]));
					erevNode.setValue(erevValue);
					channelDensityNode.addChild(erevNode);
					
					membranePropertiesNode.addChild(channelDensityNode);
				}

//				for(ChannelPopulation pop : channelPopulations)
//				{
//					ParameterSpecificationNode population = new ParameterSpecificationNode(pop.getId());
//
//					PhysicalQuantity value = new PhysicalQuantity();
//					value.setScalingFactor(pop.getSegmentGroup());
//					value.setUnit(pop.getIon());
//					value.setValue(new StringValue(pop.getErev()));
//					population.setValue(value);
//
//					props.addChild(population);
//				}
//
//				for(SpecificCapacitance s : specs)
//				{
//					ParameterSpecificationNode population = new ParameterSpecificationNode(s.getSegmentGroup());
//
//					PhysicalQuantity value = new PhysicalQuantity();
//					value.setScalingFactor(s.getSegmentGroup());
//					value.setUnit(s.getSegmentGroup());
//					value.setValue(new StringValue(s.getValue()));
//					population.setValue(value);
//
//					props.addChild(population);
//				}
				
				
				biophysicalPropertiesNode.addChild(membranePropertiesNode);
			}
			
			IntracellularProperties intracellularProperties = properties.getIntracellularProperties();
			if(intracellularProperties != null)
			{
				
			}

			_populated = true;
			modelTree.addChild(biophysicalPropertiesNode);

		}
	}
}
