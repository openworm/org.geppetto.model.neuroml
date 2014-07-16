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

import org.geppetto.core.model.runtime.AspectSubTreeNode;
import org.geppetto.core.model.runtime.CompositeVariableNode;
import org.geppetto.core.model.runtime.ParameterNode;
import org.neuroml.model.BiophysicalProperties;
import org.neuroml.model.Cell;
import org.neuroml.model.ChannelDensity;
import org.neuroml.model.ChannelPopulation;
import org.neuroml.model.MembraneProperties;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.SpecificCapacitance;

/**
 * Populates the Model Tree of Aspect
 * 
 * @author  Jesus R. Martinez (jesus@metacell.us)
 *
 */
public class PopulateModelTree {
	
	/*
	 * Properties for NeuroML model. We will be looking for this and populating
	 * model tree using the values extracted from the NeuroML.
	 */
	private static final String condDensity = "condDensity";
	private static final String id = "id";
	private static final String ionChannel = "ionChannel";
	private static final String ion = "ion";
	private static final String segment = "segment";
	private static final String segmentGroup = "segmentGroup";
	private static final String neurolexid = "neurolexid";
	private static final String erev = "erev";
	private static final String capacitanceValue = "value";

	public PopulateModelTree() {		
	}
	
	/**
	 * Method that is contacted to start populating the model tree
	 * 
	 * @param modelTree - Model tree that is to be populated
	 * @param neuroml - NeuroMLDocument used to populate the tree, values are in here
	 */
	public void populateModelTree(AspectSubTreeNode modelTree, NeuroMLDocument neuroml)
	{
 		List<Cell> cells = neuroml.getCell();
 		for(Cell c : cells){
 			addProperties(modelTree, c.getBiophysicalProperties());
 		}
	}

	/**
	 * Using properties found in NeuroMLDocument to populate the tree, this is where 
	 * the creationg of the nodes happen
	 * 
	 * @param modelTree - Object containing model tree
	 * @param properties - Model properties extracted from neuroml document object
	 */
	public void addProperties(AspectSubTreeNode modelTree,BiophysicalProperties properties){
		if(properties != null)
		{
			CompositeVariableNode props = new CompositeVariableNode(properties.getId());
			MembraneProperties memProperties = properties.getMembraneProperties();
			if(memProperties != null)
			{
				List<ChannelDensity> channelDensities = memProperties.getChannelDensity();
				List<ChannelPopulation> channelPopulations = memProperties.getChannelPopulation();
				List<SpecificCapacitance> specs = memProperties.getSpecificCapacitance();

				for(ChannelDensity m : channelDensities)
				{
					ParameterNode density = new ParameterNode(m.getId());
					density.addProperty(this.condDensity,m.getCondDensity());
					density.addProperty(this.id, m.getId());
					density.addProperty(this.ionChannel, m.getIonChannel());
					density.addProperty(this.segmentGroup, m.getSegmentGroup());
					density.addProperty(this.erev, m.getErev());
					density.addProperty(this.ion, m.getIon());
					density.addProperty(this.neurolexid, m.getNeuroLexId());
					density.addProperty(this.segment, m.getSegment());

					props.addChild(density);
				}

				for(ChannelPopulation pop : channelPopulations)
				{
					ParameterNode population = new ParameterNode(pop.getId());
					population.addProperty(this.id, pop.getId());
					population.addProperty(this.ionChannel, pop.getIonChannel());
					population.addProperty(this.segmentGroup, pop.getSegmentGroup());
					population.addProperty(this.erev, pop.getErev());
					population.addProperty(this.ion, pop.getIon());
					population.addProperty(this.neurolexid, pop.getNeuroLexId());
					population.addProperty(this.segment, pop.getSegment());

					props.addChild(population);
				}

				for(SpecificCapacitance s : specs)
				{
					ParameterNode sCap = new ParameterNode("Specific Capacitance");
					sCap.addProperty(this.segmentGroup, s.getSegmentGroup());
					sCap.addProperty(this.segment, s.getSegment());
					sCap.addProperty(this.capacitanceValue, s.getValue());

					props.addChild(sCap);
				}
			}

			modelTree.addChild(props);

		}
	}

	
}
