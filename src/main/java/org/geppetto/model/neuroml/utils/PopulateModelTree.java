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
package org.geppetto.model.neuroml.utils;

import java.util.List;

import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.model.runtime.AspectSubTreeNode;
import org.geppetto.core.model.runtime.CompositeNode;
import org.geppetto.core.model.runtime.FunctionNode;
import org.geppetto.core.model.runtime.TextMetadataNode;
import org.geppetto.core.model.values.StringValue;
import org.lemsml.jlems.core.sim.ContentError;
import org.lemsml.jlems.core.type.ComponentType;
import org.lemsml.jlems.core.type.dynamics.DerivedVariable;
import org.neuroml.model.AdExIaFCell;
import org.neuroml.model.Annotation;
import org.neuroml.model.BiophysicalProperties;
import org.neuroml.model.Cell;
import org.neuroml.model.ChannelDensity;
import org.neuroml.model.DecayingPoolConcentrationModel;
import org.neuroml.model.FixedFactorConcentrationModel;
import org.neuroml.model.GateHHUndetermined;
import org.neuroml.model.GateTypes;
import org.neuroml.model.HHRate;
import org.neuroml.model.IafCell;
import org.neuroml.model.IafRefCell;
import org.neuroml.model.IafTauCell;
import org.neuroml.model.IafTauRefCell;
import org.neuroml.model.InitMembPotential;
import org.neuroml.model.IntracellularProperties;
import org.neuroml.model.IonChannel;
import org.neuroml.model.MembraneProperties;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.Resistivity;
import org.neuroml.model.Species;
import org.neuroml.model.SpecificCapacitance;
import org.neuroml.model.SpikeThresh;

/**
 * Populates the Model Tree of Aspect
 * 
 * @author  Jesus R. Martinez (jesus@metacell.us)
 * @author Adrian Quintana (adrian.perez@ucl.ac.uk)
 * 
 */
public class PopulateModelTree {

	private boolean _populated = false;
	
//	private URL url;
	
//	private NeuroMLDocument neuroml;
	
	private NeuroMLAccessUtility neuroMLAccessUtility = new NeuroMLAccessUtility();
	
	private PopulateModelTreeUtils populateModelTreeUtils = new PopulateModelTreeUtils();
	
	
	
	public PopulateModelTree() {		
	}
	
	/**
	 * Method that is contacted to start populating the model tree
	 * 
	 * @param modelTree - Model tree that is to be populated
	 * @param neuroml - NeuroMLDocument used to populate the tree, values are in here
	 * @return 
	 * @throws ModelInterpreterException 
	 */
	public boolean populateModelTree(AspectSubTreeNode modelTree, ModelWrapper model) throws ModelInterpreterException
	{		
		
//		this.url = (URL) ((ModelWrapper) model).getModel(NeuroMLAccessUtility.URL_ID);
		NeuroMLDocument neuroml = (NeuroMLDocument) ((ModelWrapper) model).getModel(NeuroMLAccessUtility.NEUROML_ID);
		
		modelTree.getParent().getParent();
		
		//FIXME : Remove and apply data accurately
		try {
	 		List<Cell> cells = neuroml.getCell();
	 		List<AdExIaFCell> adExIaFCells = neuroml.getAdExIaFCell();
	 		List<IafCell> iaFCells = neuroml.getIafCell();
	 		List<IafRefCell> iafRefCells = neuroml.getIafRefCell();
	 		List<IafTauRefCell> iafTauRefCells = neuroml.getIafTauRefCell();
	 		List<IafTauCell> iafTauCells = neuroml.getIafTauCell();
//	 		neuroml.getNetwork()
	 		
	 		for(Cell c : cells){
	 			if (c.getBiophysicalProperties() != null){
	 				modelTree.addChild(getBiophysicalPropertiesNode(c.getBiophysicalProperties(), model));
	 			}
	 			modelTree.addChild(new TextMetadataNode(Resources.ANOTATION.get(), "anotation",  new StringValue(c.getNotes())));
	 			modelTree.addChild(populateModelTreeUtils.createTextMetadataNodeFromAnnotation(c.getAnnotation()));
	 		}
	 		for(AdExIaFCell c : adExIaFCells){
	 		}
	 		for(IafRefCell c : iafRefCells){
	 			
	 		}
	 		for(IafCell c : iaFCells){
	 			modelTree.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.RESET, "Reset", c.getReset()));
	 			modelTree.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.THRESH, "Thresh", c.getThresh()));
	 			modelTree.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.LEAK_REVERSAL, "Leak Reversal", c.getLeakReversal()));
	 			modelTree.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.LEAK_CONDUCTANCE, "Leak Conductance", c.getLeakReversal()));
	 			modelTree.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.C, "Capacitance", c.getC()));
	 			
	 		}
	 		for(IafTauRefCell c : iafTauRefCells){
	 			modelTree.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.LEAK_REVERSAL, "Leak Reversal", c.getLeakReversal()));
	 			modelTree.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.TAU, "Tau", c.getTau()));
	 			modelTree.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.RESET, "Reset", c.getReset()));
	 			modelTree.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.THRESH, "Thresh", c.getThresh()));
	 			modelTree.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.REFRACT, "Refract", c.getRefract()));
	 		}
	 		for(IafTauCell c : iafTauCells){
	 			modelTree.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.LEAK_REVERSAL, "Leak Reversal", c.getLeakReversal()));
	 			modelTree.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.TAU, "Tau", c.getTau()));
	 			modelTree.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.RESET, "Reset", c.getReset()));
	 			modelTree.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.THRESH, "Thresh", c.getThresh()));
	 		}
	 		
	 		_populated = true;
		} catch (Exception e) {
			_populated = false;
			throw new ModelInterpreterException(e);
		}
 		
 		return _populated;
	}
	
	

	/**
	 * Using properties found in NeuroMLDocument to populate the tree, this is where 
	 * the creationg of the nodes happen
	 * 
	 * @param modelTree - Object containing model tree
	 * @param properties - Model properties extracted from neuroml document object
	 * @throws ModelInterpreterException 
	 * @throws ContentError 
	 */
	public CompositeNode getBiophysicalPropertiesNode(BiophysicalProperties properties, ModelWrapper model) throws ModelInterpreterException, ContentError{
		CompositeNode biophysicalPropertiesNode = new CompositeNode(Resources.BIOPHYSICAL_PROPERTIES.get(), properties.getId());
		
		MembraneProperties membraneProperties = properties.getMembraneProperties();
		if(membraneProperties != null)
		{
			CompositeNode membranePropertiesNode = new CompositeNode(Resources.MEMBRANE_P.get(), "MembraneProperties");
			
			
			List<ChannelDensity> channelDensities = membraneProperties.getChannelDensity();
//				List<ChannelPopulation> channelPopulations = membraneProperties.getChannelPopulation();
			List<SpecificCapacitance> specificCapacitances = membraneProperties.getSpecificCapacitance();
			List<SpikeThresh> spikeThreshs = membraneProperties.getSpikeThresh();
			List<InitMembPotential> initMembPotentials = membraneProperties.getInitMembPotential();

			// Channel Density
			for(ChannelDensity channelDensity : channelDensities)
			{
				CompositeNode channelDensityNode = new CompositeNode(Resources.CHANNEL_DENSITY.get() + "_" + channelDensity.getId(), channelDensity.getId());
				
				// Ion Channel
				CompositeNode ionChannelNode = new CompositeNode(Resources.ION_CHANNEL.get(), channelDensity.getIonChannel());
				
				IonChannel ionChannel = (IonChannel) this.neuroMLAccessUtility.getComponent(channelDensity.getIonChannel(), model, Resources.ION_CHANNEL);
				
				//TODO: Read an annotation node properly
				ionChannelNode.addChild(populateModelTreeUtils.createTextMetadataNodeFromAnnotation(ionChannel.getAnnotation()));
				
				//Read Gates
				for (GateHHUndetermined gateHHUndetermined : ionChannel.getGate()){
					CompositeNode gateHHUndeterminedNode = new CompositeNode(Resources.GATE.get() + "_" + gateHHUndetermined.getId(), gateHHUndetermined.getId());

					//Forward Rate
					if (gateHHUndetermined.getForwardRate() != null){
						gateHHUndeterminedNode.addChild(populateModelTreeUtils.createRateGateNode(Resources.FW_RATE , "forwardRate_" + gateHHUndetermined.getId(), gateHHUndetermined.getForwardRate(), this.neuroMLAccessUtility, model));
					}
					
					//Reverse Rate
					if (gateHHUndetermined.getReverseRate() != null){
						gateHHUndeterminedNode.addChild(populateModelTreeUtils.createRateGateNode(Resources.BW_RATE , "backwardRate_" + gateHHUndetermined.getId(), gateHHUndetermined.getReverseRate(), this.neuroMLAccessUtility, model));
					}
					
					ComponentType typeRate = (ComponentType) neuroMLAccessUtility.getComponent(gateHHUndetermined.getType().value(), model, Resources.COMPONENT_TYPE);
					gateHHUndeterminedNode.addChild(populateModelTreeUtils.createCompositeNodeFromComponentType(Resources.GATE_DYNAMICS.get(), "GateDynamics", typeRate));
					
					
					gateHHUndetermined.getInstances();
					gateHHUndetermined.getQ10Settings();
					
					if (gateHHUndetermined.getTimeCourse() != null){
						gateHHUndeterminedNode.addChild(populateModelTreeUtils.createTimeCourseNode(Resources.TIMECOURSE, "timeCourse_" + gateHHUndetermined.getId(), gateHHUndetermined.getTimeCourse(), neuroMLAccessUtility, model));
					}
					
					if (gateHHUndetermined.getSteadyState() != null){
						gateHHUndeterminedNode.addChild(populateModelTreeUtils.createSteadyStateNode(Resources.STEADY_STATE, "steadyState" + gateHHUndetermined.getId(), gateHHUndetermined.getSteadyState(), neuroMLAccessUtility, model));
					}
					
					ionChannelNode.addChild(gateHHUndeterminedNode);
				}
				
				ionChannel.getGateHHrates();
				ionChannel.getGateHHratesInf();
				ionChannel.getGateHHratesTau();
				ionChannel.getGateHHtauInf();
				
				ionChannelNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.CONDUCTANCE, "Conductance", ionChannel.getConductance()));
				
				ionChannel.getSpecies();
				
				ionChannelNode.addChild(new TextMetadataNode(Resources.NOTES.get(), "Notes",  new StringValue(ionChannel.getNotes())));
				
				if (ionChannel.getType() != null){
					ComponentType typeIonChannel = (ComponentType) neuroMLAccessUtility.getComponent(ionChannel.getType().value(), model, Resources.COMPONENT_TYPE);
					ionChannelNode.addChild(populateModelTreeUtils.createCompositeNodeFromComponentType(Resources.IONCHANNEL_DYNAMICS.get(), "IonChannelDynamics", typeIonChannel));
				}
		
				channelDensityNode.addChild(ionChannelNode);
				
				// Passive conductance density				
				channelDensityNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.COND_DENSITY, "condDensity_"+channelDensity.getId(), channelDensity.getCondDensity()));
				
				// ION	
				channelDensityNode.addChild(new TextMetadataNode(Resources.ION.get(), "ion_"+channelDensity.getId(),  new StringValue(channelDensity.getIon())));
				
				// Reverse Potential					
				channelDensityNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.EREV, "erev_"+channelDensity.getId(), channelDensity.getErev()));
				
				// Segment Group
				//TODO: Point to a visualization group?
				
				membranePropertiesNode.addChild(channelDensityNode);
			}

			// Spike threshold
			for(int i = 0; i < spikeThreshs.size(); i++)
			{
				membranePropertiesNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.SPIKE_THRESHOLD, "spikeThresh_"+i, spikeThreshs.get(i).getValue()));
			}

			// Specific Capacitance
			for(int i = 0; i < specificCapacitances.size(); i++)
			{
				membranePropertiesNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.SPECIFIC_CAPACITANCE, "specificCapacitance_"+i, specificCapacitances.get(i).getValue()));
			}
			
			// Initial Membrance Potentials
			for(int i = 0; i < initMembPotentials.size(); i++)
			{
				membranePropertiesNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.INIT_MEMBRANE_POTENTIAL, "initMembPotential_"+i, initMembPotentials.get(i).getValue()));
			}
			
			biophysicalPropertiesNode.addChild(membranePropertiesNode);
		}
		
		IntracellularProperties intracellularProperties = properties.getIntracellularProperties();
		if(intracellularProperties != null)
		{
			CompositeNode intracellularPropertiesNode = new CompositeNode(Resources.INTRACELLULAR_P.get(), "IntracellularProperties");
			CompositeNode speciesNode = new CompositeNode(Resources.SPECIES.get(), "Species");
			
			List<Resistivity> resistivities = intracellularProperties.getResistivity();
			List<Species> species = intracellularProperties.getSpecies();
			
			// Resistivity
			for(int i = 0; i < resistivities.size(); i++)
			{
				intracellularPropertiesNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.RESISTIVITY, "resistivity_"+i, resistivities.get(i).getValue()));
			}
			biophysicalPropertiesNode.addChild(intracellularPropertiesNode);
			
			// Specie
			for(Species specie : species)
			{
				CompositeNode speciesNodeItem = new CompositeNode(Resources.SPECIES.get(), "Species");
				
				// Initial Concentration
				speciesNodeItem.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.INIT_CONCENTRATION, "initialConcentration_"+specie.getId(), specie.getInitialConcentration()));
				
				// Initial External Concentration
				speciesNodeItem.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.INIT_EXT_CONCENTRATION, "initialExtConcentration_"+specie.getId(), specie.getInitialExtConcentration()));
				
				// Ion
				speciesNodeItem.addChild(new TextMetadataNode(Resources.ION.get(), "ion_"+specie.getId(),  new StringValue(specie.getIon())));
				
				// Concentration Model
				Object concentrationModel = neuroMLAccessUtility.getComponent(specie.getConcentrationModel(), model, Resources.CONCENTRATION_MODEL);
				CompositeNode concentrationModelNode = new CompositeNode(Resources.CONCENTRATION_MODEL.get(),"ConcentrationModel");
				if (concentrationModel instanceof DecayingPoolConcentrationModel){
					DecayingPoolConcentrationModel decayingPoolConcentrationModel = (DecayingPoolConcentrationModel) concentrationModel;
					concentrationModelNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.DECAY_CONSTANT, "DecayConstant", decayingPoolConcentrationModel.getDecayConstant()));
					concentrationModelNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.RESTING_CONC, "RestingConcentration", decayingPoolConcentrationModel.getRestingConc()));
					concentrationModelNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.SHELL_THICKNESS, "ShellThickness", decayingPoolConcentrationModel.getShellThickness()));
					concentrationModelNode.addChild(new TextMetadataNode(Resources.ION.get(), "ion",  new StringValue(decayingPoolConcentrationModel.getIon())));
					concentrationModelNode.addChild(new TextMetadataNode(Resources.NOTES.get(), "notes",  new StringValue(decayingPoolConcentrationModel.getNotes())));
				}
				else{
					FixedFactorConcentrationModel fixedFactorConcentrationModel = (FixedFactorConcentrationModel) concentrationModel;
					concentrationModelNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.DECAY_CONSTANT, "DecayConstant", fixedFactorConcentrationModel.getDecayConstant()));
					concentrationModelNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.RESTING_CONC, "RestingConcentration", fixedFactorConcentrationModel.getRestingConc()));
					concentrationModelNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.RHO, "Rho", fixedFactorConcentrationModel.getRho()));
					concentrationModelNode.addChild(new TextMetadataNode(Resources.ION.get(), "ion",  new StringValue(fixedFactorConcentrationModel.getIon())));
					concentrationModelNode.addChild(new TextMetadataNode(Resources.NOTES.get(), "notes",  new StringValue(fixedFactorConcentrationModel.getNotes())));
				}
				
				speciesNodeItem.addChild(concentrationModelNode);
				speciesNode.addChild(speciesNodeItem);
			}
			
			biophysicalPropertiesNode.addChild(speciesNode);
		}
		return biophysicalPropertiesNode;
		//modelTree.addChild(biophysicalPropertiesNode);
	}
	
	
}
