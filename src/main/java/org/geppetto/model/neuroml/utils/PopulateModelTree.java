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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.model.runtime.AspectSubTreeNode;
import org.geppetto.core.model.runtime.CompositeNode;
import org.geppetto.core.model.runtime.ParameterSpecificationNode;
import org.geppetto.core.model.runtime.TextMetadataNode;
import org.geppetto.core.model.values.StringValue;
import org.lemsml.jlems.core.sim.ContentError;
import org.lemsml.jlems.core.type.ComponentType;
import org.neuroml.model.AdExIaFCell;
import org.neuroml.model.BiophysicalProperties;
import org.neuroml.model.Cell;
import org.neuroml.model.ChannelDensity;
import org.neuroml.model.DecayingPoolConcentrationModel;
import org.neuroml.model.FitzHughNagumoCell;
import org.neuroml.model.FixedFactorConcentrationModel;
import org.neuroml.model.GateHHUndetermined;
import org.neuroml.model.IafCell;
import org.neuroml.model.IafRefCell;
import org.neuroml.model.IafTauCell;
import org.neuroml.model.IafTauRefCell;
import org.neuroml.model.InitMembPotential;
import org.neuroml.model.IntracellularProperties;
import org.neuroml.model.IonChannel;
import org.neuroml.model.IzhikevichCell;
import org.neuroml.model.MembraneProperties;
import org.neuroml.model.Network;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.Population;
import org.neuroml.model.Resistivity;
import org.neuroml.model.Species;
import org.neuroml.model.SpecificCapacitance;
import org.neuroml.model.SpikeThresh;
import org.neuroml.model.SynapticConnection;

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
		NeuroMLDocument neuroml = (NeuroMLDocument) ((ModelWrapper) model).getModel(NeuroMLAccessUtility.NEUROML_ID);
		
		try {
			/**
			 * CELLS
			 */
			
	 		List<Cell> cells = neuroml.getCell();
	 		List<AdExIaFCell> adExIaFCells = neuroml.getAdExIaFCell();
	 		List<IafCell> iaFCells = neuroml.getIafCell();
	 		List<IafRefCell> iafRefCells = neuroml.getIafRefCell();
	 		List<IafTauRefCell> iafTauRefCells = neuroml.getIafTauRefCell();
	 		List<IafTauCell> iafTauCells = neuroml.getIafTauCell();
	 		List<FitzHughNagumoCell> fitzHughNagumoCells = neuroml.getFitzHughNagumoCell();
	 		List<IzhikevichCell> izhikevichCells = neuroml.getIzhikevichCell();
	 		
	 		for(Cell c : cells){
	 			if (c.getBiophysicalProperties() != null){
	 				modelTree.addChild(getBiophysicalPropertiesNode(c.getBiophysicalProperties(), model));
	 			}
	 			modelTree.addChild(new TextMetadataNode(Resources.ANOTATION.get(), Resources.ANOTATION.getId(),  new StringValue(c.getNotes())));
	 			modelTree.addChild(populateModelTreeUtils.createTextMetadataNodeFromAnnotation(c.getAnnotation()));
	 		}
	 		for(AdExIaFCell c : adExIaFCells){
	 			modelTree.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.CAPACITANCE, Resources.CAPACITANCE.getId(), c.getC()));
	 			modelTree.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.EL, Resources.EL.getId(), c.getEL()));
	 			modelTree.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.VT, Resources.VT.getId(), c.getVT()));
	 			modelTree.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.A, Resources.A.getId(), c.getA()));
	 			modelTree.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.B, Resources.B.getId(), c.getB()));
	 			modelTree.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.DELT, Resources.DELT.getId(), c.getDelT()));
	 			modelTree.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.GL, Resources.GL.getId(), c.getGL()));
	 			modelTree.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.REFRACT, Resources.REFRACT.getId(), c.getRefract()));
	 			modelTree.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.RESET, Resources.RESET.getId(), c.getReset()));
	 			modelTree.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.TAUW, Resources.TAUW.getId(), c.getTauw()));
	 			modelTree.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.THRESH, Resources.THRESH.getId(), c.getThresh()));
	 		}
	 		for(FitzHughNagumoCell c : fitzHughNagumoCells){
	 			modelTree.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.I, Resources.I.getId(), c.getI()));
	 		}
	 		for(IzhikevichCell c : izhikevichCells){
	 			modelTree.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.A, Resources.A.getId(), c.getA()));
	 			modelTree.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.B, Resources.B.getId(), c.getB()));
	 			modelTree.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.C, Resources.C.getId(), c.getB()));
	 			modelTree.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.D, Resources.D.getId(), c.getB()));
	 			modelTree.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.v0, Resources.v0.getId(), c.getB()));
	 			modelTree.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.THRESH, Resources.THRESH.getId(), c.getThresh()));
	 		}
	 		
	 		for(IafRefCell c : iafRefCells){
	 			modelTree.addChildren(createIafCellNode(c));
	 			modelTree.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.REFRACT, Resources.REFRACT.getId(), c.getRefract()));
	 		}
	 		for(IafCell c : iaFCells){
	 			modelTree.addChildren(createIafCellNode(c));
	 		}
	 		for(IafTauRefCell c : iafTauRefCells){
	 			modelTree.addChildren(createIafTauCellNode(c));
	 			modelTree.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.REFRACT, Resources.REFRACT.getId(), c.getRefract()));
	 		}
	 		for(IafTauCell c : iafTauCells){
	 			modelTree.addChildren(createIafTauCellNode(c));
	 		}
	 		
	 		/**
	 		 * NETWORK
	 		 */
	 		List<Network> networks = neuroml.getNetwork();
	 		for(Network n : networks){
	 			n.getInputList();
	 			List<Population> populations = n.getPopulation();
	 			for(Population p : populations){
	 				p.getComponent();
	 				p.getId();
	 				p.getInstance();
	 				
	 			}
	 			List<SynapticConnection> synapticConnection = n.getSynapticConnection();
	 			
	 		}
	 		
	 		_populated = true;
		} catch (Exception e) {
			_populated = false;
			throw new ModelInterpreterException(e);
		}
 		
 		return _populated;
	}
	
	public Collection<ParameterSpecificationNode> createIafTauCellNode(IafTauCell c){
		Collection<ParameterSpecificationNode> iafTauCellChildren = new ArrayList<ParameterSpecificationNode>();
		iafTauCellChildren.add(populateModelTreeUtils.createParameterSpecificationNode(Resources.LEAK_REVERSAL, Resources.LEAK_REVERSAL.getId(), c.getLeakReversal()));
		iafTauCellChildren.add(populateModelTreeUtils.createParameterSpecificationNode(Resources.TAU, Resources.TAU.getId(), c.getTau()));
		iafTauCellChildren.add(populateModelTreeUtils.createParameterSpecificationNode(Resources.RESET, Resources.RESET.getId(), c.getReset()));
		iafTauCellChildren.add(populateModelTreeUtils.createParameterSpecificationNode(Resources.THRESH, Resources.THRESH.getId(), c.getThresh()));
		return iafTauCellChildren;
	}
	
	public Collection<ParameterSpecificationNode> createIafCellNode(IafCell c){
		Collection<ParameterSpecificationNode> iafCellChildren = new ArrayList<ParameterSpecificationNode>();
		iafCellChildren.add(populateModelTreeUtils.createParameterSpecificationNode(Resources.LEAK_REVERSAL, Resources.LEAK_REVERSAL.getId(), c.getLeakReversal()));
		iafCellChildren.add(populateModelTreeUtils.createParameterSpecificationNode(Resources.LEAK_CONDUCTANCE, Resources.LEAK_CONDUCTANCE.getId(), c.getLeakReversal()));
		iafCellChildren.add(populateModelTreeUtils.createParameterSpecificationNode(Resources.CAPACITANCE, Resources.CAPACITANCE.getId(), c.getC()));
		iafCellChildren.add(populateModelTreeUtils.createParameterSpecificationNode(Resources.RESET, Resources.RESET.getId(), c.getReset()));
		iafCellChildren.add(populateModelTreeUtils.createParameterSpecificationNode(Resources.THRESH, Resources.THRESH.getId(), c.getThresh()));
		return iafCellChildren;
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
			CompositeNode membranePropertiesNode = new CompositeNode(Resources.MEMBRANE_P.get(), Resources.MEMBRANE_P.getId());
			
			
			List<ChannelDensity> channelDensities = membraneProperties.getChannelDensity();
//				List<ChannelPopulation> channelPopulations = membraneProperties.getChannelPopulation();
			List<SpecificCapacitance> specificCapacitances = membraneProperties.getSpecificCapacitance();
			List<SpikeThresh> spikeThreshs = membraneProperties.getSpikeThresh();
			List<InitMembPotential> initMembPotentials = membraneProperties.getInitMembPotential();

			// Channel Density
			for(ChannelDensity channelDensity : channelDensities)
			{
				CompositeNode channelDensityNode = new CompositeNode(Resources.CHANNEL_DENSITY.get(), channelDensity.getId());
				
				// Ion Channel
				CompositeNode ionChannelNode = new CompositeNode(Resources.ION_CHANNEL.get(), channelDensity.getIonChannel());
				
				IonChannel ionChannel = (IonChannel) this.neuroMLAccessUtility.getComponent(channelDensity.getIonChannel(), model, Resources.ION_CHANNEL);
				
				//TODO: Read an annotation node properly
				ionChannelNode.addChild(populateModelTreeUtils.createTextMetadataNodeFromAnnotation(ionChannel.getAnnotation()));
				
				//Read Gates
				for (GateHHUndetermined gateHHUndetermined : ionChannel.getGate()){
					CompositeNode gateHHUndeterminedNode = new CompositeNode(Resources.GATE.get(), gateHHUndetermined.getId());

					//Forward Rate
					if (gateHHUndetermined.getForwardRate() != null){
						gateHHUndeterminedNode.addChild(populateModelTreeUtils.createRateGateNode(Resources.FW_RATE, gateHHUndetermined.getForwardRate(), this.neuroMLAccessUtility, model));
					}
					
					//Reverse Rate
					if (gateHHUndetermined.getReverseRate() != null){
						gateHHUndeterminedNode.addChild(populateModelTreeUtils.createRateGateNode(Resources.BW_RATE, gateHHUndetermined.getReverseRate(), this.neuroMLAccessUtility, model));
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
