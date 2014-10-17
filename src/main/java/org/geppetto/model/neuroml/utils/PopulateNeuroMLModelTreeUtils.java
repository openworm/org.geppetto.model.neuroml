package org.geppetto.model.neuroml.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.model.runtime.ANode;
import org.geppetto.core.model.runtime.AspectNode;
import org.geppetto.core.model.runtime.AspectSubTreeNode;
import org.geppetto.core.model.runtime.CompositeNode;
import org.geppetto.core.model.runtime.EntityNode;
import org.geppetto.core.model.runtime.ParameterSpecificationNode;
import org.geppetto.core.model.runtime.TextMetadataNode;
import org.geppetto.core.model.runtime.AspectSubTreeNode.AspectTreeType;
import org.geppetto.core.model.values.IntValue;
import org.geppetto.core.model.values.StringValue;
import org.geppetto.core.utilities.VariablePathSerializer;
import org.lemsml.jlems.core.sim.ContentError;
import org.lemsml.jlems.core.type.ComponentType;
import org.neuroml.model.AdExIaFCell;
import org.neuroml.model.AlphaCondSynapse;
import org.neuroml.model.Annotation;
import org.neuroml.model.Base;
import org.neuroml.model.BaseCell;
import org.neuroml.model.BaseConductanceBasedSynapse;
import org.neuroml.model.BasePynnSynapse;
import org.neuroml.model.BiophysicalProperties;
import org.neuroml.model.BlockMechanism;
import org.neuroml.model.BlockingPlasticSynapse;
import org.neuroml.model.Cell;
import org.neuroml.model.ChannelDensity;
import org.neuroml.model.DecayingPoolConcentrationModel;
import org.neuroml.model.ExpCondSynapse;
import org.neuroml.model.ExpOneSynapse;
import org.neuroml.model.ExpTwoSynapse;
import org.neuroml.model.ExplicitInput;
import org.neuroml.model.ExtracellularProperties;
import org.neuroml.model.FitzHughNagumoCell;
import org.neuroml.model.FixedFactorConcentrationModel;
import org.neuroml.model.GateHHRates;
import org.neuroml.model.GateHHRatesInf;
import org.neuroml.model.GateHHRatesTau;
import org.neuroml.model.GateHHTauInf;
import org.neuroml.model.GateHHUndetermined;
import org.neuroml.model.HHRate;
import org.neuroml.model.HHTime;
import org.neuroml.model.HHVariable;
import org.neuroml.model.IafCell;
import org.neuroml.model.IafRefCell;
import org.neuroml.model.IafTauCell;
import org.neuroml.model.IafTauRefCell;
import org.neuroml.model.InitMembPotential;
import org.neuroml.model.InputList;
import org.neuroml.model.Instance;
import org.neuroml.model.IntracellularProperties;
import org.neuroml.model.IonChannel;
import org.neuroml.model.IzhikevichCell;
import org.neuroml.model.MembraneProperties;
import org.neuroml.model.Network;
import org.neuroml.model.PlasticityMechanism;
import org.neuroml.model.Population;
import org.neuroml.model.PopulationTypes;
import org.neuroml.model.Projection;
import org.neuroml.model.Q10Settings;
import org.neuroml.model.Region;
import org.neuroml.model.Resistivity;
import org.neuroml.model.Species;
import org.neuroml.model.SpecificCapacitance;
import org.neuroml.model.SpikeThresh;
import org.neuroml.model.Standalone;
import org.neuroml.model.SynapticConnection;
import org.w3c.dom.Element;

public class PopulateNeuroMLModelTreeUtils {
	
	private NeuroMLAccessUtility neuroMLAccessUtility = new NeuroMLAccessUtility();
	
	private PopulateModelTreeUtils populateModelTreeUtils = new PopulateModelTreeUtils();
	
	private PopulateLEMSModelTreeUtils populateLEMSModelTreeUtils = new PopulateLEMSModelTreeUtils();
	
	private ModelWrapper model;
	
	public ModelWrapper getModel() {
		return model;
	}

	public void setModel(ModelWrapper model) {
		this.model = model;
	}
	
	public CompositeNode createRateGateNode(Resources name, HHRate rate) throws ModelInterpreterException, ContentError{
		
		CompositeNode rateGateNode = new CompositeNode(name.getId(), name.get());
			
		if (rate != null){
			if (rate.getType() != null){
//				ComponentType typeRate = (ComponentType) neuroMLAccessUtility.getComponent(rate.getType(), model, Resources.COMPONENT_TYPE);
				rateGateNode.addChild(populateLEMSModelTreeUtils.createCompositeNodeFromComponentType(rate.getType(), rate.getType(), (ComponentType) neuroMLAccessUtility.getComponent(rate.getType(), model, Resources.COMPONENT_TYPE)));
			}

			rateGateNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.MIDPOINT.get(), Resources.MIDPOINT.getId(), rate.getMidpoint()));
			rateGateNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.RATE.get(), Resources.RATE.getId(), rate.getRate()));
			rateGateNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.SCALE.get(), Resources.SCALE.getId(), rate.getScale()));
			
			return rateGateNode;
		}
		return null;
	}

	public CompositeNode createSteadyStateNode(HHVariable variable) throws ModelInterpreterException, ContentError{
		
		if (variable != null){
			CompositeNode steadyStateNode = new CompositeNode(Resources.STEADY_STATE.getId(), Resources.STEADY_STATE.get());
				
			if (variable.getType() != null){
				ComponentType typeSteadyState = (ComponentType) neuroMLAccessUtility.getComponent(variable.getType(), model, Resources.COMPONENT_TYPE);
				steadyStateNode.addChild(populateLEMSModelTreeUtils.createCompositeNodeFromComponentType(variable.getType(), variable.getType(), typeSteadyState));
			}
	
			steadyStateNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.MIDPOINT.get(), Resources.MIDPOINT.getId(), variable.getMidpoint()));
			if (variable.getRate() != null){
				steadyStateNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.RATE.get(), Resources.RATE.getId(), Float.toString(variable.getRate())));
			}
			steadyStateNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.SCALE.get(), Resources.SCALE.getId(), variable.getScale()));
			
			return steadyStateNode;
		}
		return null;
	}
	
	public CompositeNode createTimeCourseNode(HHTime timeCourse) throws ModelInterpreterException, ContentError{
		
		if (timeCourse != null){
			CompositeNode timeCourseNode = new CompositeNode(Resources.TIMECOURSE.getId(), Resources.TIMECOURSE.get());
			
			if (timeCourse.getType() != null){
				ComponentType typeTimeCourse = (ComponentType) neuroMLAccessUtility.getComponent(timeCourse.getType(), model, Resources.COMPONENT_TYPE);
				timeCourseNode.addChild(populateLEMSModelTreeUtils.createCompositeNodeFromComponentType(timeCourse.getType(), timeCourse.getType(), typeTimeCourse));
			}
			
			timeCourseNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.MIDPOINT.get(), Resources.MIDPOINT.getId(), timeCourse.getMidpoint()));
			timeCourseNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.RATE.get(), Resources.RATE.getId(), timeCourse.getRate()));
			timeCourseNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.SCALE.get(), Resources.SCALE.getId(), timeCourse.getScale()));
			timeCourseNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.TAU.get(), Resources.TAU.getId(), timeCourse.getTau()));
			
			return timeCourseNode;
		}
		return null;
	}
	
	public CompositeNode createCellNode(BaseCell c) throws ModelInterpreterException, ContentError{
		CompositeNode cellNode = new CompositeNode(Resources.CELL.getId(), Resources.CELL.get());
		
		if (c instanceof Cell){
			Cell cell = (Cell) c;
			if (cell.getBiophysicalProperties() != null){
				cellNode.addChild(createBiophysicalPropertiesNode(cell.getBiophysicalProperties()));
			}
		}
		else if (c instanceof AdExIaFCell) {
			AdExIaFCell cell = (AdExIaFCell) c;
			cellNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.CAPACITANCE.get(), Resources.CAPACITANCE.getId(), cell.getC()));
 			cellNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.EL.get(), Resources.EL.getId(), cell.getEL()));
 			cellNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.VT.get(), Resources.VT.getId(), cell.getVT()));
 			cellNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.A.get(), Resources.A.getId(), cell.getA()));
 			cellNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.B.get(), Resources.B.getId(), cell.getB()));
 			cellNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.DELT.get(), Resources.DELT.getId(), cell.getDelT()));
 			cellNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.GL.get(), Resources.GL.getId(), cell.getGL()));
 			cellNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.REFRACT.get(), Resources.REFRACT.getId(), cell.getRefract()));
 			cellNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.RESET.get(), Resources.RESET.getId(), cell.getReset()));
 			cellNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.TAUW.get(), Resources.TAUW.getId(), cell.getTauw()));
 			cellNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.THRESH.get(), Resources.THRESH.getId(), cell.getThresh()));
		}
		else if (c instanceof FitzHughNagumoCell) {
			FitzHughNagumoCell cell = (FitzHughNagumoCell) c;
			cellNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.I.get(), Resources.I.getId(), cell.getI()));
		}
		else if (c instanceof IzhikevichCell) {
			IzhikevichCell cell = (IzhikevichCell) c;
			cellNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.A.get(), Resources.A.getId(), cell.getA()));
 			cellNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.B.get(), Resources.B.getId(), cell.getB()));
 			cellNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.C.get(), Resources.C.getId(), cell.getB()));
 			cellNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.D.get(), Resources.D.getId(), cell.getB()));
 			cellNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.v0.get(), Resources.v0.getId(), cell.getB()));
 			cellNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.THRESH.get(), Resources.THRESH.getId(), cell.getThresh()));
		}
		else if (c instanceof IafRefCell) {
			IafRefCell cell = (IafRefCell) c;
			cellNode.addChildren(createIafCellChildren(cell));
 			cellNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.REFRACT.get(), Resources.REFRACT.getId(), cell.getRefract()));
		}
		else if (c instanceof IafCell) {
			IafCell cell = (IafCell) c;
			cellNode.addChildren(createIafCellChildren(cell));
		}
		else if (c instanceof IafTauRefCell) {
			IafTauRefCell cell = (IafTauRefCell) c;
			cellNode.addChildren(createIafTauCellChildren(cell));
 			cellNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.REFRACT.get(), Resources.REFRACT.getId(), cell.getRefract()));
		}
		else if (c instanceof IafTauCell) {
			IafTauCell cell = (IafTauCell) c;
			cellNode.addChildren(createIafTauCellChildren(cell));
		}
		
		cellNode.addChildren(createStandaloneChildren(c));
		
		return cellNode;
		
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
	public CompositeNode createBiophysicalPropertiesNode(BiophysicalProperties properties) throws ModelInterpreterException, ContentError{
		CompositeNode biophysicalPropertiesNode = new CompositeNode(properties.getId(), Resources.BIOPHYSICAL_PROPERTIES.get());
		
		MembraneProperties membraneProperties = properties.getMembraneProperties();
		if(membraneProperties != null)
		{
			CompositeNode membranePropertiesNode = new CompositeNode(Resources.MEMBRANE_P.getId(), Resources.MEMBRANE_P.get());
			
			
			List<ChannelDensity> channelDensities = membraneProperties.getChannelDensity();
//				List<ChannelPopulation> channelPopulations = membraneProperties.getChannelPopulation();
			List<SpecificCapacitance> specificCapacitances = membraneProperties.getSpecificCapacitance();
			List<SpikeThresh> spikeThreshs = membraneProperties.getSpikeThresh();
			List<InitMembPotential> initMembPotentials = membraneProperties.getInitMembPotential();

			// Channel Density
			for(ChannelDensity channelDensity : channelDensities)
			{
				CompositeNode channelDensityNode = new CompositeNode(channelDensity.getId(), getUniqueName(Resources.CHANNEL_DENSITY.get(), channelDensity));
				
				// Ion Channel
				IonChannel ionChannel = (IonChannel) neuroMLAccessUtility.getComponent(channelDensity.getIonChannel(), model, Resources.ION_CHANNEL);
				channelDensityNode.addChild(createChannelNode(ionChannel));
				
				// Passive conductance density				
				channelDensityNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.COND_DENSITY.get(), Resources.COND_DENSITY.getId(), channelDensity.getCondDensity()));
				
				// ION	
				channelDensityNode.addChild(new TextMetadataNode(Resources.ION.get(), Resources.ION.getId(),  new StringValue(channelDensity.getIon())));
				
				// Reverse Potential					
				channelDensityNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.EREV.get(), Resources.EREV.getId(), channelDensity.getErev()));
				
				// Segment Group
				//TODO: Point to a visualization group?
				
				membranePropertiesNode.addChild(channelDensityNode);
			}

			// Spike threshold
			for(int i = 0; i < spikeThreshs.size(); i++)
			{
				membranePropertiesNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.SPIKE_THRESHOLD.get(), Resources.SPIKE_THRESHOLD.getId() + "_" + i, spikeThreshs.get(i).getValue()));
			}

			// Specific Capacitance
			for(int i = 0; i < specificCapacitances.size(); i++)
			{
				membranePropertiesNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.SPECIFIC_CAPACITANCE.get(), Resources.SPECIFIC_CAPACITANCE.getId() + "_" + i, specificCapacitances.get(i).getValue()));
			}
			
			// Initial Membrance Potentials
			for(int i = 0; i < initMembPotentials.size(); i++)
			{
				membranePropertiesNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.INIT_MEMBRANE_POTENTIAL.get(), Resources.INIT_MEMBRANE_POTENTIAL.getId() + "_" + i, initMembPotentials.get(i).getValue()));
			}
			
			biophysicalPropertiesNode.addChild(membranePropertiesNode);
		}
		
		IntracellularProperties intracellularProperties = properties.getIntracellularProperties();
		biophysicalPropertiesNode.addChild(createIntracellularPropertiesNode(intracellularProperties));
		
		ExtracellularProperties extracellularProperties = properties.getExtracellularProperties();
		biophysicalPropertiesNode.addChild(createExtracellularPropertiesNode(extracellularProperties));
		
		return biophysicalPropertiesNode;
	}
	
	public CompositeNode createIntracellularPropertiesNode(IntracellularProperties intracellularProperties) throws ModelInterpreterException {
		if(intracellularProperties != null)
		{
			CompositeNode intracellularPropertiesNode = new CompositeNode(Resources.INTRACELLULAR_P.getId(), Resources.INTRACELLULAR_P.get());
			
			
			List<Resistivity> resistivities = intracellularProperties.getResistivity();
			List<Species> species = intracellularProperties.getSpecies();
			
			// Resistivity
			CompositeNode resistivitiesNode = new CompositeNode(Resources.RESISTIVITY.getId(), Resources.RESISTIVITY.get());
			for(int i = 0; i < resistivities.size(); i++)
			{
				resistivitiesNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.RESISTIVITY.get(), Resources.RESISTIVITY.getId() + "_" + i, resistivities.get(i).getValue()));
			}
			intracellularPropertiesNode.addChild(resistivitiesNode);
			
			if (species != null){
				CompositeNode speciesNode = new CompositeNode(Resources.SPECIES.getId(), Resources.SPECIES.get());
				speciesNode.addChildren(createSpeciesNode(species));
				intracellularPropertiesNode.addChild(speciesNode);
			}
			return intracellularPropertiesNode;
		}
		return null;
	}
	
	public CompositeNode createExtracellularPropertiesNode(ExtracellularProperties extracellularProperties) throws ModelInterpreterException {
		if(extracellularProperties != null){
			CompositeNode extracellularPropertiesNode = new CompositeNode(Resources.EXTRACELLULAR_P.getId(), Resources.EXTRACELLULAR_P.get());
			extracellularPropertiesNode.addChildren(createBaseChildren(extracellularProperties));
			
			List<Species> species = extracellularProperties.getSpecies();
			if (species != null){
				CompositeNode speciesNode = new CompositeNode(Resources.SPECIES.getId(), Resources.SPECIES.get());
				speciesNode.addChildren(createSpeciesNode(species));
				extracellularPropertiesNode.addChild(speciesNode);
			}
			return extracellularPropertiesNode;
		}
		return null;
	}
	
	private List<CompositeNode> createSpeciesNode(List<Species> species) throws ModelInterpreterException {
		List<CompositeNode> speciesNodeList = new ArrayList<CompositeNode>();
		// Specie
		for(Species specie : species)
		{
			CompositeNode speciesNodeItem = new CompositeNode(Resources.SPECIES.getId(), Resources.SPECIES.get());
			
			// Initial Concentration
			speciesNodeItem.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.INIT_CONCENTRATION.get(), Resources.INIT_CONCENTRATION.getId(), specie.getInitialConcentration()));
			
			// Initial External Concentration
			speciesNodeItem.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.INIT_EXT_CONCENTRATION.get(), Resources.INIT_EXT_CONCENTRATION.getId(), specie.getInitialExtConcentration()));
			
			// Ion
			speciesNodeItem.addChild(new TextMetadataNode(Resources.ION.get(), Resources.ION.getId(),  new StringValue(specie.getIon())));
			
			// Concentration Model
			Object concentrationModel = neuroMLAccessUtility.getComponent(specie.getConcentrationModel(), model, Resources.CONCENTRATION_MODEL);
			speciesNodeItem.addChild(createConcentrationModelNode(concentrationModel));
			
			speciesNodeList.add(speciesNodeItem);
		}
		return speciesNodeList;
	}
	
	public CompositeNode createConcentrationModelNode(Object concentrationModel) {
		if (concentrationModel != null){
			CompositeNode concentrationModelNode = new CompositeNode(Resources.CONCENTRATION_MODEL.getId(), Resources.CONCENTRATION_MODEL.get());
			if (concentrationModel instanceof DecayingPoolConcentrationModel){
				DecayingPoolConcentrationModel decayingPoolConcentrationModel = (DecayingPoolConcentrationModel) concentrationModel;
				concentrationModelNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.DECAY_CONSTANT.get(), Resources.DECAY_CONSTANT.getId(), decayingPoolConcentrationModel.getDecayConstant()));
				concentrationModelNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.RESTING_CONC.get(), Resources.RESTING_CONC.getId(), decayingPoolConcentrationModel.getRestingConc()));
				concentrationModelNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.SHELL_THICKNESS.get(), Resources.SHELL_THICKNESS.getId(), decayingPoolConcentrationModel.getShellThickness()));
				concentrationModelNode.addChild(new TextMetadataNode(Resources.ION.get(), Resources.ION.getId(),  new StringValue(decayingPoolConcentrationModel.getIon())));
			}
			else{
				FixedFactorConcentrationModel fixedFactorConcentrationModel = (FixedFactorConcentrationModel) concentrationModel;
				concentrationModelNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.DECAY_CONSTANT.get(), Resources.DECAY_CONSTANT.getId(), fixedFactorConcentrationModel.getDecayConstant()));
				concentrationModelNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.RESTING_CONC.get(), Resources.RESTING_CONC.getId(), fixedFactorConcentrationModel.getRestingConc()));
				concentrationModelNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.RHO.get(), Resources.RHO.getId(), fixedFactorConcentrationModel.getRho()));
				concentrationModelNode.addChild(new TextMetadataNode(Resources.ION.get(), Resources.ION.getId(),  new StringValue(fixedFactorConcentrationModel.getIon())));
			}
			concentrationModelNode.addChildren(createStandaloneChildren((Standalone)concentrationModel));
			
			return concentrationModelNode;
		}
		return null;
	}

	public CompositeNode createGateNode(Base gate) throws ModelInterpreterException, ContentError{
		CompositeNode gateNode = new CompositeNode(gate.getId(), getUniqueName(Resources.GATE.get(), gate));
		gateNode.addChildren(createBaseChildren(gate));
		
		if (gate instanceof  GateHHUndetermined){
			GateHHUndetermined gateHHUndetermined = (GateHHUndetermined) gate;
	
			gateNode.addChild(createRateGateNode(Resources.FW_RATE, gateHHUndetermined.getForwardRate()));
			gateNode.addChild(createRateGateNode(Resources.BW_RATE, gateHHUndetermined.getReverseRate()));
			
			if (gateHHUndetermined.getType() != null){
				ComponentType typeRate = (ComponentType) neuroMLAccessUtility.getComponent(gateHHUndetermined.getType().value(), model, Resources.COMPONENT_TYPE);
				gateNode.addChild(populateLEMSModelTreeUtils.createCompositeNodeFromComponentType(Resources.GATE_DYNAMICS.get(), Resources.GATE_DYNAMICS.getId(), typeRate));
			}

			gateNode.addChild(new TextMetadataNode(Resources.INSTANCES.get(), Resources.INSTANCES.getId(),  new IntValue(gateHHUndetermined.getInstances().intValue())));
			gateNode.addChild(createQ10SettingsNode(gateHHUndetermined.getQ10Settings()));
			gateNode.addChild(createTimeCourseNode(gateHHUndetermined.getTimeCourse()));
			gateNode.addChild(createSteadyStateNode(gateHHUndetermined.getSteadyState()));
			
			if (gateHHUndetermined.getNotes() != null){
				gateNode.addChild(new TextMetadataNode(Resources.NOTES.get(), Resources.NOTES.getId(),  new StringValue(gateHHUndetermined.getNotes())));
			}	
		}
		else if (gate instanceof  GateHHRates) {
			GateHHRates gateHHRates = (GateHHRates) gate;

			gateNode.addChild(createRateGateNode(Resources.FW_RATE, gateHHRates.getForwardRate()));
			gateNode.addChild(createRateGateNode(Resources.BW_RATE, gateHHRates.getReverseRate()));

			if (gateHHRates.getType() != null){
				ComponentType typeRate = (ComponentType) neuroMLAccessUtility.getComponent(gateHHRates.getType().value(), model, Resources.COMPONENT_TYPE);
				gateNode.addChild(populateLEMSModelTreeUtils.createCompositeNodeFromComponentType(Resources.GATE_DYNAMICS.get(), Resources.GATE_DYNAMICS.getId(), typeRate));
			}
			gateNode.addChild(new TextMetadataNode(Resources.INSTANCES.get(), Resources.INSTANCES.getId(),  new IntValue(gateHHRates.getInstances().intValue())));
			gateNode.addChild(createQ10SettingsNode(gateHHRates.getQ10Settings()));
			
			if (gateHHRates.getNotes() != null){
				gateNode.addChild(new TextMetadataNode(Resources.NOTES.get(), Resources.NOTES.getId(),  new StringValue(gateHHRates.getNotes())));
			}	
			
		}
		else if (gate instanceof  GateHHRatesInf) {
			GateHHRatesInf gateHHRatesInf = (GateHHRatesInf) gate;
			
			gateNode.addChild(createRateGateNode(Resources.FW_RATE, gateHHRatesInf.getForwardRate()));
			gateNode.addChild(createRateGateNode(Resources.BW_RATE, gateHHRatesInf.getReverseRate()));

			if (gateHHRatesInf.getType() != null){
				ComponentType typeRate = (ComponentType) neuroMLAccessUtility.getComponent(gateHHRatesInf.getType().value(), model, Resources.COMPONENT_TYPE);
				gateNode.addChild(populateLEMSModelTreeUtils.createCompositeNodeFromComponentType(Resources.GATE_DYNAMICS.get(), Resources.GATE_DYNAMICS.getId(), typeRate));
			}
			
			gateNode.addChild(new TextMetadataNode(Resources.INSTANCES.get(), Resources.INSTANCES.getId(),  new IntValue(gateHHRatesInf.getInstances().intValue())));
			gateNode.addChild(createQ10SettingsNode(gateHHRatesInf.getQ10Settings()));
			gateNode.addChild(createSteadyStateNode(gateHHRatesInf.getSteadyState()));
			
			if (gateHHRatesInf.getNotes() != null){
				gateNode.addChild(new TextMetadataNode(Resources.NOTES.get(), Resources.NOTES.getId(),  new StringValue(gateHHRatesInf.getNotes())));
			}	
		}
		else if (gate instanceof  GateHHRatesTau) {
			GateHHRatesTau gateHHRatesTau = (GateHHRatesTau) gate;
			
			gateNode.addChild(createRateGateNode(Resources.FW_RATE, gateHHRatesTau.getForwardRate()));
			gateNode.addChild(createRateGateNode(Resources.BW_RATE, gateHHRatesTau.getReverseRate()));

			if (gateHHRatesTau.getType() != null){
				ComponentType typeRate = (ComponentType) neuroMLAccessUtility.getComponent(gateHHRatesTau.getType().value(), model, Resources.COMPONENT_TYPE);
				gateNode.addChild(populateLEMSModelTreeUtils.createCompositeNodeFromComponentType(Resources.GATE_DYNAMICS.get(), Resources.GATE_DYNAMICS.getId(), typeRate));
			}
			
			gateNode.addChild(new TextMetadataNode(Resources.INSTANCES.get(), Resources.INSTANCES.getId(),  new IntValue(gateHHRatesTau.getInstances().intValue())));
			gateNode.addChild(createQ10SettingsNode(gateHHRatesTau.getQ10Settings()));
			gateNode.addChild(createTimeCourseNode(gateHHRatesTau.getTimeCourse()));
			
			if (gateHHRatesTau.getNotes() != null){
				gateNode.addChild(new TextMetadataNode(Resources.NOTES.get(), Resources.NOTES.getId(),  new StringValue(gateHHRatesTau.getNotes())));
			}
		}
		else if (gate instanceof  GateHHTauInf) {
			GateHHTauInf gateHHTauInf = (GateHHTauInf) gate;
			
			if (gateHHTauInf.getType() != null){
				ComponentType typeRate = (ComponentType) neuroMLAccessUtility.getComponent(gateHHTauInf.getType().value(), model, Resources.COMPONENT_TYPE);
				gateNode.addChild(populateLEMSModelTreeUtils.createCompositeNodeFromComponentType(Resources.GATE_DYNAMICS.get(), Resources.GATE_DYNAMICS.getId(), typeRate));
			}
			
			gateNode.addChild(new TextMetadataNode(Resources.INSTANCES.get(), Resources.INSTANCES.getId(),  new IntValue(gateHHTauInf.getInstances().intValue())));
			
			gateNode.addChild(createQ10SettingsNode(gateHHTauInf.getQ10Settings()));
			gateNode.addChild(createTimeCourseNode(gateHHTauInf.getTimeCourse()));
			gateNode.addChild(createSteadyStateNode(gateHHTauInf.getSteadyState()));
			
			if (gateHHTauInf.getNotes() != null){
				gateNode.addChild(new TextMetadataNode(Resources.NOTES.get(), Resources.NOTES.getId(),  new StringValue(gateHHTauInf.getNotes())));
			}	
		}
		
		return gateNode;
	}

	private CompositeNode createQ10SettingsNode(Q10Settings q10Settings)	throws ModelInterpreterException, ContentError {
		if (q10Settings != null) {
			CompositeNode q10SettingsNode = new CompositeNode(Resources.Q10SETTINGS.getId(), Resources.Q10SETTINGS.get());
			if (q10Settings.getExperimentalTemp() != null){
				q10SettingsNode.addChild(new TextMetadataNode(Resources.EXPERIMENTAL_TEMP.get(), Resources.EXPERIMENTAL_TEMP.getId(),  new StringValue(q10Settings.getExperimentalTemp())));
			}
			if (q10Settings.getFixedQ10() != null){
				q10SettingsNode.addChild(new TextMetadataNode(Resources.FIXEDQ10.get(), Resources.FIXEDQ10.getId(),  new StringValue(q10Settings.getFixedQ10())));
			}
			if (q10Settings.getQ10Factor() != null){
				q10SettingsNode.addChild(new TextMetadataNode(Resources.Q10FACTOR.get(), Resources.Q10FACTOR.getId(),  new StringValue(q10Settings.getQ10Factor())));
			}
			ComponentType typeQ10Settings = (ComponentType) neuroMLAccessUtility.getComponent(q10Settings.getType(), model, Resources.COMPONENT_TYPE);
			q10SettingsNode.addChild(populateLEMSModelTreeUtils.createCompositeNodeFromComponentType(Resources.TYPE.get(), Resources.TYPE.getId(), typeQ10Settings));
			return q10SettingsNode;
		}
		return null;
	}
	
	public CompositeNode createChannelNode(Standalone ionChannelBase) throws ModelInterpreterException, ContentError {
		// Ion Channel
		CompositeNode ionChannelNode = new CompositeNode(ionChannelBase.getId());
		if (ionChannelBase instanceof IonChannel){
			ionChannelNode.setName(Resources.ION_CHANNEL.get());
		}
		else{
			ionChannelNode.setName(Resources.ION_CHANNEL_HH.get());
		}
		
		IonChannel ionChannel = (IonChannel)ionChannelBase;
		
		ionChannelNode.addChildren(createStandaloneChildren(ionChannel));
		//Read Gates
		for (GateHHUndetermined gateHHUndetermined : ionChannel.getGate()){
			ionChannelNode.addChild(createGateNode(gateHHUndetermined));
		}
		for (GateHHRates gateHHRates : ionChannel.getGateHHrates()){
			ionChannelNode.addChild(createGateNode(gateHHRates));
		}
		for (GateHHRatesInf gateHHRatesInf : ionChannel.getGateHHratesInf()){
			ionChannelNode.addChild(createGateNode(gateHHRatesInf));
		}
		for (GateHHRatesTau gateHHRatesTau : ionChannel.getGateHHratesTau()){
			ionChannelNode.addChild(createGateNode(gateHHRatesTau));
		}
		for (GateHHTauInf gateHHRatesTauInf : ionChannel.getGateHHtauInf()){
			ionChannelNode.addChild(createGateNode(gateHHRatesTauInf));
		}
		
		ionChannelNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.CONDUCTANCE.get(), Resources.CONDUCTANCE.getId(), ionChannel.getConductance()));
		
		ionChannelNode.addChild(new TextMetadataNode(Resources.SPECIES.get(), Resources.SPECIES.getId(), new StringValue(ionChannel.getSpecies())));
		
		if (ionChannel.getType() != null){
			ComponentType typeIonChannel = (ComponentType) neuroMLAccessUtility.getComponent(ionChannel.getType().value(), model, Resources.COMPONENT_TYPE);
			ionChannelNode.addChild(populateLEMSModelTreeUtils.createCompositeNodeFromComponentType(Resources.IONCHANNEL_DYNAMICS.get(), Resources.IONCHANNEL_DYNAMICS.getId(), typeIonChannel));
		}
		return ionChannelNode;
	}
	
	
	public Collection<ParameterSpecificationNode> createIafTauCellChildren(IafTauCell c){
		Collection<ParameterSpecificationNode> iafTauCellChildren = new ArrayList<ParameterSpecificationNode>();
		iafTauCellChildren.add(populateModelTreeUtils.createParameterSpecificationNode(Resources.LEAK_REVERSAL.get(), Resources.LEAK_REVERSAL.getId(), c.getLeakReversal()));
		iafTauCellChildren.add(populateModelTreeUtils.createParameterSpecificationNode(Resources.TAU.get(), Resources.TAU.getId(), c.getTau()));
		iafTauCellChildren.add(populateModelTreeUtils.createParameterSpecificationNode(Resources.RESET.get(), Resources.RESET.getId(), c.getReset()));
		iafTauCellChildren.add(populateModelTreeUtils.createParameterSpecificationNode(Resources.THRESH.get(), Resources.THRESH.getId(), c.getThresh()));
		return iafTauCellChildren;
	}
	
	public Collection<ParameterSpecificationNode> createIafCellChildren(IafCell c){
		Collection<ParameterSpecificationNode> iafCellChildren = new ArrayList<ParameterSpecificationNode>();
		iafCellChildren.add(populateModelTreeUtils.createParameterSpecificationNode(Resources.LEAK_REVERSAL.get(), Resources.LEAK_REVERSAL.getId(), c.getLeakReversal()));
		iafCellChildren.add(populateModelTreeUtils.createParameterSpecificationNode(Resources.LEAK_CONDUCTANCE.get(), Resources.LEAK_CONDUCTANCE.getId(), c.getLeakConductance()));
		iafCellChildren.add(populateModelTreeUtils.createParameterSpecificationNode(Resources.CAPACITANCE.get(), Resources.CAPACITANCE.getId(), c.getC()));
		iafCellChildren.add(populateModelTreeUtils.createParameterSpecificationNode(Resources.RESET.get(), Resources.RESET.getId(), c.getReset()));
		iafCellChildren.add(populateModelTreeUtils.createParameterSpecificationNode(Resources.THRESH.get(), Resources.THRESH.getId(), c.getThresh()));
		return iafCellChildren;
	}
	
	
	public CompositeNode createNetworkNode(Network n) throws ModelInterpreterException, ContentError {

		CompositeNode networkNode = new CompositeNode(Resources.NETWORK.getId(), Resources.NETWORK.get());
		
		networkNode.addChildren(createStandaloneChildren(n));
		
		for(InputList i : n.getInputList()){
			
		}
		
		for(ExplicitInput e : n.getExplicitInput()){
			
		}
		
		for(Region r : n.getRegion()){
			
		}
		
		for(Projection p : n.getProjection()){
			
		}
		
		//Iterate through the entities in order to fill the model document
		Map<String, EntityNode> mapping = (Map<String, EntityNode>) ((ModelWrapper) model).getModel(NeuroMLAccessUtility.SUBENTITIES_MAPPING_ID);
		
		List<Population> populations = n.getPopulation();
		for(Population p : populations){
			CompositeNode populationNode = new CompositeNode(p.getId(), getUniqueName(Resources.POPULATION.get(), p) );
			
			populationNode.addChildren(createStandaloneChildren(p));

			BaseCell baseCell = (BaseCell) neuroMLAccessUtility.getComponent(p.getComponent(), model, Resources.CELL);
			populationNode.addChild(createCellNode(baseCell));
			
			if (p.getSize() != null){
				populationNode.addChild(new TextMetadataNode(Resources.SIZE.get(), Resources.SIZE.getId(),  new IntValue(p.getSize().intValue())));
			}
			
			
			
			PopulationTypes populationType = p.getType();
			if(populationType != null){
				populationNode.addChild(new TextMetadataNode(Resources.POPULATION_TYPE.get(), Resources.POPULATION_TYPE.getId(),  new StringValue(populationType.value())));

				if(populationType.equals(PopulationTypes.POPULATION_LIST))
				{
					
					BaseCell cell = (BaseCell) neuroMLAccessUtility.getComponent(p.getComponent(), model, Resources.CELL);
					for(int i=0; i < p.getInstance().size(); i++)
					{
						String id = VariablePathSerializer.getArrayName(p.getId(), i);
						EntityNode entityNode = mapping.get(id);
						
						
						//TODO: Do we add the model tree to every aspect?
						for (AspectNode aspectNode : entityNode.getAspects()){
							AspectSubTreeNode modelTree = (AspectSubTreeNode)aspectNode.getSubTree(AspectTreeType.MODEL_TREE);
							modelTree.addChildren(createCellNode(cell).getChildren());
							modelTree.setModified(true);
						}
						
					}
				
				}
				
			}
			else{
				
			}
			
			//TODO: Just reading the number of instances and displaying as a text metadata node 				
			List<Instance> instanceList = p.getInstance();
			if (instanceList != null && instanceList.size() != 0){
				populationNode.addChild(new TextMetadataNode(Resources.INSTANCES.get(), Resources.INSTANCES.getId(),  new IntValue(instanceList.size())));
			}
			networkNode.addChild(populationNode);
		}
		
		List<SynapticConnection> synapticConnections = n.getSynapticConnection();
		for(SynapticConnection s : synapticConnections){
			
		}
		
		return networkNode;
	}

	public CompositeNode createSynapseNode(BaseConductanceBasedSynapse synapse) throws ContentError, ModelInterpreterException {
		CompositeNode synapseNode = new CompositeNode(synapse.getId());
		
		synapseNode.addChildren(createStandaloneChildren(synapse));
		
		synapseNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.EREV.get(), Resources.EREV.getId(), synapse.getErev()));
		synapseNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.GBASE.get(), Resources.GBASE.getId(), synapse.getGbase()));
		
		if (synapse instanceof ExpTwoSynapse){
			synapseNode.setName(getUniqueName(Resources.EXPTWOSYNAPSE.get(), synapse));
			
			ExpTwoSynapse expTwoSynapse = (ExpTwoSynapse) synapse;
			synapseNode.addChildren(createExpTwoSynapseChildren(expTwoSynapse));
		}
		else if (synapse instanceof ExpOneSynapse) {
			synapseNode.setName(getUniqueName(Resources.EXPONESYNAPSE.get(), synapse));
			
			ExpOneSynapse expOneSynapse = (ExpOneSynapse) synapse;
			synapseNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.TAUDECAY.get(), Resources.TAUDECAY.getId(), expOneSynapse.getTauDecay()));
		}
		else if (synapse instanceof BlockingPlasticSynapse) {
			synapseNode.setName(getUniqueName(Resources.BLOCKINGPLASTICSYNAPSE.get(), synapse));
			
			BlockingPlasticSynapse blockingPlasticSynapse = (BlockingPlasticSynapse) synapse;
			
			synapseNode.addChildren(createExpTwoSynapseChildren(blockingPlasticSynapse));
			
			PlasticityMechanism plasticityMechanism = blockingPlasticSynapse.getPlasticityMechanism();
			if (plasticityMechanism != null){
				CompositeNode plasticityMechanismNode = new CompositeNode(Resources.PLASTICITYMECHANISM.getId(), Resources.PLASTICITYMECHANISM.get());
				
				plasticityMechanismNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.INITRELEASEPROB.get(), Resources.INITRELEASEPROB.getId(), String.valueOf(plasticityMechanism.getInitReleaseProb())));
				plasticityMechanismNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.TAUFAC.get(), Resources.TAUFAC.getId(), plasticityMechanism.getTauFac()));
				plasticityMechanismNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.TAUREC.get(), Resources.TAUREC.getId(), plasticityMechanism.getTauRec()));
				
				plasticityMechanismNode.addChild(populateLEMSModelTreeUtils.createCompositeNodeFromComponentType(Resources.TYPE.get(), Resources.TYPE.getId(), (ComponentType) neuroMLAccessUtility.getComponent(plasticityMechanism.getType().value(), model, Resources.COMPONENT_TYPE)));
				
				synapseNode.addChild(plasticityMechanismNode);
				
			}
			BlockMechanism blockMechanism = blockingPlasticSynapse.getBlockMechanism();
			if (blockMechanism != null){
				CompositeNode blockMechanismNode = new CompositeNode(Resources.BLOCKMECHANISM.getId(), Resources.BLOCKMECHANISM.get());
				
				blockMechanismNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.SCALINGCONC.get(), Resources.SCALINGCONC.getId(), blockMechanism.getScalingConc()));
				blockMechanismNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.BLOCKCONCENTRATION.get(), Resources.BLOCKCONCENTRATION.getId(), blockMechanism.getBlockConcentration()));
				blockMechanismNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.SCALINGVOLT.get(), Resources.SCALINGVOLT.getId(), blockMechanism.getScalingVolt()));
				blockMechanismNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.SPECIES.get(), Resources.SPECIES.getId(), blockMechanism.getSpecies()));
				
				blockMechanismNode.addChild(populateLEMSModelTreeUtils.createCompositeNodeFromComponentType(Resources.TYPE.get(), Resources.TYPE.getId(), (ComponentType) neuroMLAccessUtility.getComponent(blockMechanism.getType().value(), model, Resources.COMPONENT_TYPE)));
				
				synapseNode.addChild(blockMechanismNode);
			}
		}
		
		return synapseNode;
	}	
	
	public CompositeNode createPynnSynapseNode(BasePynnSynapse pynnSynapse) throws ContentError, ModelInterpreterException {
		CompositeNode pynnSynapsesNode = new CompositeNode(pynnSynapse.getId(), getUniqueName(Resources.SYNAPSE.get(), pynnSynapse));
		
		pynnSynapsesNode.addChildren(createStandaloneChildren(pynnSynapse));
		
		pynnSynapsesNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.TAUSYN.get(), Resources.TAUSYN.getId(), String.valueOf(pynnSynapse.getTauSyn())));
		
		if (pynnSynapse instanceof AlphaCondSynapse){
			pynnSynapsesNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.EREV.get(), Resources.EREV.getId(), String.valueOf(((AlphaCondSynapse) pynnSynapse).getERev())));
		}
		else if (pynnSynapse instanceof ExpCondSynapse){
			pynnSynapsesNode.addChild(populateModelTreeUtils.createParameterSpecificationNode(Resources.EREV.get(), Resources.EREV.getId(), String.valueOf(((AlphaCondSynapse) pynnSynapse).getERev())));
		}
		return pynnSynapsesNode;
	}
	
	public Collection<ANode> createExpTwoSynapseChildren(ExpTwoSynapse expTwoSynapse){
		Collection<ANode> expTwoSynapseChildren = new ArrayList<ANode>();
		expTwoSynapseChildren.add(populateModelTreeUtils.createParameterSpecificationNode(Resources.TAUDECAY.get(), Resources.TAUDECAY.getId(), expTwoSynapse.getTauDecay()));
		expTwoSynapseChildren.add(populateModelTreeUtils.createParameterSpecificationNode(Resources.TAURISE.get(), Resources.TAURISE.getId(), expTwoSynapse.getTauRise()));
		return expTwoSynapseChildren;
	}
	
	public Collection<ANode> createStandaloneChildren(Standalone standaloneComponent){
		Collection<ANode> standaloneChildren = new ArrayList<ANode>();

		standaloneChildren.addAll(createBaseChildren(standaloneComponent));
		
		//TODO: Improve to parse all the attribute in an annotation	
		Annotation annotation = standaloneComponent.getAnnotation();
		if (annotation != null){
			CompositeNode annotationNode = new CompositeNode(Resources.ANOTATION.getId(), Resources.ANOTATION.get());
			for (Element element : annotation.getAny()){
				annotationNode.addChild(new TextMetadataNode(Resources.ELEMENT.get(), Resources.ELEMENT.getId(),  new StringValue(element.getTextContent())));
			}
			standaloneChildren.add(annotationNode);
		}
		if (standaloneComponent.getNotes() != null){
			standaloneChildren.add(new TextMetadataNode(Resources.NOTES.get(), Resources.NOTES.getId(),  new StringValue(standaloneComponent.getNotes())));
		}
		if (standaloneComponent.getMetaid() != null){
			standaloneChildren.add(new TextMetadataNode(Resources.METAID.get(), Resources.METAID.getId(),  new StringValue(standaloneComponent.getMetaid())));
		}
		
		return standaloneChildren;
	}
	
	public Collection<ANode> createBaseChildren(Base baseComponent){
		Collection<ANode> baseChildren = new ArrayList<ANode>();
		if (baseComponent.getId() != null){
			baseChildren.add(new TextMetadataNode(Resources.ID.get(), Resources.ID.getId(),  new StringValue(baseComponent.getId())));
		}
		if (baseComponent.getNeuroLexId() != null){
			baseChildren.add(new TextMetadataNode(Resources.NEUROLEX_ID.get(), Resources.NEUROLEX_ID.getId(),  new StringValue(baseComponent.getNeuroLexId())));
		}
		return baseChildren;
	}
	
	public String getUniqueName(String label, Base base){
		return label + " - " + base.getId();
	}
}
