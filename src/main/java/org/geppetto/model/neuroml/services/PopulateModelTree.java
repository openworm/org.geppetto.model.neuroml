package org.geppetto.model.neuroml.services;

import java.net.URL;
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

public class PopulateModelTree {
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
	
	public void populateModelTree(AspectSubTreeNode modelTree, NeuroMLDocument neuroml, URL url)
	{
 		List<Cell> cells = neuroml.getCell();
 		for(Cell c : cells){
 			addProperties(modelTree, c.getBiophysicalProperties());
 		}
	}

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
