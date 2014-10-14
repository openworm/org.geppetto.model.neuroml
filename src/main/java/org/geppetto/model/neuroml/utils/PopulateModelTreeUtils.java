package org.geppetto.model.neuroml.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.model.quantities.PhysicalQuantity;
import org.geppetto.core.model.quantities.Quantity;
import org.geppetto.core.model.runtime.ANode;
import org.geppetto.core.model.runtime.CompositeNode;
import org.geppetto.core.model.runtime.FunctionNode;
import org.geppetto.core.model.runtime.ParameterNode;
import org.geppetto.core.model.runtime.ParameterSpecificationNode;
import org.geppetto.core.model.runtime.TextMetadataNode;
import org.geppetto.core.model.values.FloatValue;
import org.geppetto.core.model.values.IntValue;
import org.geppetto.core.model.values.StringValue;
import org.lemsml.jlems.core.sim.ContentError;
import org.lemsml.jlems.core.type.ComponentType;
import org.lemsml.jlems.core.type.DerivedParameter;
import org.lemsml.jlems.core.type.Parameter;
import org.lemsml.jlems.core.type.dynamics.DerivedVariable;
import org.lemsml.jlems.core.type.dynamics.Dynamics;
import org.neuroml.model.AdExIaFCell;
import org.neuroml.model.Annotation;
import org.neuroml.model.Base;
import org.neuroml.model.BaseCell;
import org.neuroml.model.BiophysicalProperties;
import org.neuroml.model.Cell;
import org.neuroml.model.ChannelDensity;
import org.neuroml.model.DecayingPoolConcentrationModel;
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
import org.neuroml.model.IntracellularProperties;
import org.neuroml.model.IonChannel;
import org.neuroml.model.IzhikevichCell;
import org.neuroml.model.MembraneProperties;
import org.neuroml.model.Q10Settings;
import org.neuroml.model.Resistivity;
import org.neuroml.model.Species;
import org.neuroml.model.SpecificCapacitance;
import org.neuroml.model.SpikeThresh;
import org.neuroml.model.Standalone;
import org.w3c.dom.Element;

public class PopulateModelTreeUtils {
	
	public ParameterSpecificationNode createParameterSpecificationNode(String name, String id, String value){
		if (value != null){
//			ParameterSpecificationNode parameterSpecificationNode = new ParameterSpecificationNode(name.get(), id);
			
			String regExp = "\\s*([0-9]*\\.?[0-9]*[eE]?[-+]?[0-9]+)?\\s*(\\w*)";
			
			Pattern pattern = Pattern.compile(regExp);
			Matcher matcher = pattern.matcher(value);
			
//			PhysicalQuantity physicalQuantity = new PhysicalQuantity();
			if (matcher.find()) {
//				physicalQuantity.setValue(new FloatValue(Float.parseFloat(matcher.group(1))));
//				if (matcher.group(1)!=null){
//					physicalQuantity.setUnit(matcher.group(2));
//				}
				return createParameterSpecificationNode(name, id, matcher.group(1), matcher.group(2));
			}
//			parameterSpecificationNode.setValue(physicalQuantity);
			
//			return parameterSpecificationNode;
		}
		return null;
	}
	public ParameterSpecificationNode createParameterSpecificationNode(String name, String id, String value, String unit){
		if (value != null){
			ParameterSpecificationNode parameterSpecificationNode = new ParameterSpecificationNode(name, id);
			
			
			PhysicalQuantity physicalQuantity = new PhysicalQuantity();
			physicalQuantity.setValue(new FloatValue(Float.parseFloat(value)));
			physicalQuantity.setUnit(unit);
			parameterSpecificationNode.setValue(physicalQuantity);
			
			return parameterSpecificationNode;
		}
		return null;
	}
	

	//TODO: Improve to parse all the attribute in a component type 	
	public CompositeNode createCompositeNodeFromComponentType(String name, String id, ComponentType componentType) throws ContentError{
		CompositeNode compositeNode = new CompositeNode(name, id);
		
		
		compositeNode.addChild(createParameterSpecificationNode(Resources.NAME.get(), Resources.NAME.getId(), componentType.getName()));
		//TODO: This is needed?
		if (componentType.getExtends() != null){
			compositeNode.addChild(createCompositeNodeFromComponentType(Resources.EXTENDS.get(), Resources.EXTENDS.getId(), componentType.getExtends()));
		}
		compositeNode.addChild(createParameterSpecificationNode(Resources.DESCRIPTION.get(), Resources.DESCRIPTION.getId(), componentType.getDescription()));
		
		
		Dynamics dynamics = componentType.getDynamics();
		if (dynamics != null){
			CompositeNode dynamicsNode = new CompositeNode(Resources.DYNAMICS.get(), "Dynamics");
			 
			for (DerivedVariable derivedVariables : dynamics.getDerivedVariables()){
				FunctionNode  functionNode = new FunctionNode(derivedVariables.getName(), derivedVariables.getName());
				functionNode.setExpression(derivedVariables.getValueExpression());
				//derivedVariables.getName();
				//TODO: Do we want to store the dimension and the exposure? 										
				//derivedVariables.getDimension();
				//derivedVariables.getExposure();
				dynamicsNode.addChild(functionNode);
			}
			compositeNode.addChild(dynamicsNode);
		}
		
//		for (Parameter parameter : componentType.getParameters()){
//			compositeNode.addChild(createParameterNode(Resources.PARAMETER, Resources.PARAMETER + "_" +  parameter.getName(), ));
//			parameter.getName();
//			parameter.getDimension();
//		}
		
		for (DerivedParameter derivedParameter : componentType.getDerivedParameters()){
			compositeNode.addChild(createParameterSpecificationNode(derivedParameter.getName(), derivedParameter.getName(), derivedParameter.getValue(), derivedParameter.getDimension().getName()));
		}
		
//		componentType.get
		
		
		return compositeNode;
	}
		
	public CompositeNode createRateGateNode(Resources name, HHRate rate, NeuroMLAccessUtility neuroMLAccessUtility,
			ModelWrapper model) throws ModelInterpreterException, ContentError{
		
		CompositeNode rateGateNode = new CompositeNode(name.get(), name.getId());
			
		if (rate != null){
			if (rate.getType() != null){
				ComponentType typeRate = (ComponentType) neuroMLAccessUtility.getComponent(rate.getType(), model, Resources.COMPONENT_TYPE);
				rateGateNode.addChild(createCompositeNodeFromComponentType(rate.getType(), rate.getType(), typeRate));
			}

			rateGateNode.addChild(createParameterSpecificationNode(Resources.MIDPOINT.get(), Resources.MIDPOINT.getId(), rate.getMidpoint()));
			rateGateNode.addChild(createParameterSpecificationNode(Resources.RATE.get(), Resources.RATE.getId(), rate.getRate()));
			rateGateNode.addChild(createParameterSpecificationNode(Resources.SCALE.get(), Resources.SCALE.getId(), rate.getScale()));
			
			return rateGateNode;
		}
		return null;
	}

	public CompositeNode createSteadyStateNode(Resources name, HHVariable variable, NeuroMLAccessUtility neuroMLAccessUtility,
			ModelWrapper model) throws ModelInterpreterException, ContentError{
		
		if (variable != null){
			CompositeNode steadyStateNode = new CompositeNode(name.get(), name.getId());
				
			if (variable.getType() != null){
				ComponentType typeSteadyState = (ComponentType) neuroMLAccessUtility.getComponent(variable.getType(), model, Resources.COMPONENT_TYPE);
				steadyStateNode.addChild(createCompositeNodeFromComponentType(variable.getType(), variable.getType(), typeSteadyState));
			}
	
			steadyStateNode.addChild(createParameterSpecificationNode(Resources.MIDPOINT.get(), Resources.MIDPOINT.getId(), variable.getMidpoint()));
			if (variable.getRate() != null){
				steadyStateNode.addChild(createParameterSpecificationNode(Resources.RATE.get(), Resources.RATE.getId(), Float.toString(variable.getRate())));
			}
			steadyStateNode.addChild(createParameterSpecificationNode(Resources.SCALE.get(), Resources.SCALE.getId(), variable.getScale()));
			
			return steadyStateNode;
		}
		return null;
	}
	
	public CompositeNode createTimeCourseNode(Resources name, HHTime timeCourse, NeuroMLAccessUtility neuroMLAccessUtility, ModelWrapper model) throws ModelInterpreterException, ContentError{
		
		if (timeCourse != null){
			CompositeNode timeCourseNode = new CompositeNode(name.get(), name.getId());
			
			if (timeCourse.getType() != null){
				ComponentType typeTimeCourse = (ComponentType) neuroMLAccessUtility.getComponent(timeCourse.getType(), model, Resources.COMPONENT_TYPE);
				timeCourseNode.addChild(createCompositeNodeFromComponentType(timeCourse.getType(), timeCourse.getType(), typeTimeCourse));
			}
			
			timeCourseNode.addChild(createParameterSpecificationNode(Resources.MIDPOINT.get(), Resources.MIDPOINT.getId(), timeCourse.getMidpoint()));
			timeCourseNode.addChild(createParameterSpecificationNode(Resources.RATE.get(), Resources.RATE.getId(), timeCourse.getRate()));
			timeCourseNode.addChild(createParameterSpecificationNode(Resources.SCALE.get(), Resources.SCALE.getId(), timeCourse.getScale()));
			timeCourseNode.addChild(createParameterSpecificationNode(Resources.TAU.get(), Resources.TAU.getId(), timeCourse.getTau()));
			
			return timeCourseNode;
		}
		return null;
	}
	
	public CompositeNode createCellNode(BaseCell c, NeuroMLAccessUtility neuroMLAccessUtility, ModelWrapper model) throws ModelInterpreterException, ContentError{
		CompositeNode cellNode = new CompositeNode(Resources.CELL.get(), Resources.CELL.getId());
		
		if (c instanceof Cell){
			Cell cell = (Cell) c;
			if (cell.getBiophysicalProperties() != null){
				cellNode.addChild(getBiophysicalPropertiesNode(cell.getBiophysicalProperties(), neuroMLAccessUtility, model));
			}
		}
		else if (c instanceof AdExIaFCell) {
			AdExIaFCell cell = (AdExIaFCell) c;
			cellNode.addChild(createParameterSpecificationNode(Resources.CAPACITANCE.get(), Resources.CAPACITANCE.getId(), cell.getC()));
 			cellNode.addChild(createParameterSpecificationNode(Resources.EL.get(), Resources.EL.getId(), cell.getEL()));
 			cellNode.addChild(createParameterSpecificationNode(Resources.VT.get(), Resources.VT.getId(), cell.getVT()));
 			cellNode.addChild(createParameterSpecificationNode(Resources.A.get(), Resources.A.getId(), cell.getA()));
 			cellNode.addChild(createParameterSpecificationNode(Resources.B.get(), Resources.B.getId(), cell.getB()));
 			cellNode.addChild(createParameterSpecificationNode(Resources.DELT.get(), Resources.DELT.getId(), cell.getDelT()));
 			cellNode.addChild(createParameterSpecificationNode(Resources.GL.get(), Resources.GL.getId(), cell.getGL()));
 			cellNode.addChild(createParameterSpecificationNode(Resources.REFRACT.get(), Resources.REFRACT.getId(), cell.getRefract()));
 			cellNode.addChild(createParameterSpecificationNode(Resources.RESET.get(), Resources.RESET.getId(), cell.getReset()));
 			cellNode.addChild(createParameterSpecificationNode(Resources.TAUW.get(), Resources.TAUW.getId(), cell.getTauw()));
 			cellNode.addChild(createParameterSpecificationNode(Resources.THRESH.get(), Resources.THRESH.getId(), cell.getThresh()));
		}
		else if (c instanceof FitzHughNagumoCell) {
			FitzHughNagumoCell cell = (FitzHughNagumoCell) c;
			cellNode.addChild(createParameterSpecificationNode(Resources.I.get(), Resources.I.getId(), cell.getI()));
		}
		else if (c instanceof IzhikevichCell) {
			IzhikevichCell cell = (IzhikevichCell) c;
			cellNode.addChild(createParameterSpecificationNode(Resources.A.get(), Resources.A.getId(), cell.getA()));
 			cellNode.addChild(createParameterSpecificationNode(Resources.B.get(), Resources.B.getId(), cell.getB()));
 			cellNode.addChild(createParameterSpecificationNode(Resources.C.get(), Resources.C.getId(), cell.getB()));
 			cellNode.addChild(createParameterSpecificationNode(Resources.D.get(), Resources.D.getId(), cell.getB()));
 			cellNode.addChild(createParameterSpecificationNode(Resources.v0.get(), Resources.v0.getId(), cell.getB()));
 			cellNode.addChild(createParameterSpecificationNode(Resources.THRESH.get(), Resources.THRESH.getId(), cell.getThresh()));
		}
		else if (c instanceof IafRefCell) {
			IafRefCell cell = (IafRefCell) c;
			cellNode.addChildren(createIafCellChildren(cell));
 			cellNode.addChild(createParameterSpecificationNode(Resources.REFRACT.get(), Resources.REFRACT.getId(), cell.getRefract()));
		}
		else if (c instanceof IafCell) {
			IafCell cell = (IafCell) c;
			cellNode.addChildren(createIafCellChildren(cell));
		}
		else if (c instanceof IafTauRefCell) {
			IafTauRefCell cell = (IafTauRefCell) c;
			cellNode.addChildren(createIafTauCellChildren(cell));
 			cellNode.addChild(createParameterSpecificationNode(Resources.REFRACT.get(), Resources.REFRACT.getId(), cell.getRefract()));
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
	public CompositeNode getBiophysicalPropertiesNode(BiophysicalProperties properties, NeuroMLAccessUtility neuroMLAccessUtility, ModelWrapper model) throws ModelInterpreterException, ContentError{
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
				IonChannel ionChannel = (IonChannel) neuroMLAccessUtility.getComponent(channelDensity.getIonChannel(), model, Resources.ION_CHANNEL);
				channelDensityNode.addChild(createChannelNode(ionChannel, neuroMLAccessUtility, model));
				
				// Passive conductance density				
				channelDensityNode.addChild(createParameterSpecificationNode(Resources.COND_DENSITY.get(), Resources.COND_DENSITY.getId() + "_" +channelDensity.getId(), channelDensity.getCondDensity()));
				
				// ION	
				channelDensityNode.addChild(new TextMetadataNode(Resources.ION.get(), Resources.ION.getId() + "_" + channelDensity.getId(),  new StringValue(channelDensity.getIon())));
				
				// Reverse Potential					
				channelDensityNode.addChild(createParameterSpecificationNode(Resources.EREV.get(), Resources.EREV.getId() + "_" + channelDensity.getId(), channelDensity.getErev()));
				
				// Segment Group
				//TODO: Point to a visualization group?
				
				membranePropertiesNode.addChild(channelDensityNode);
			}

			// Spike threshold
			for(int i = 0; i < spikeThreshs.size(); i++)
			{
				membranePropertiesNode.addChild(createParameterSpecificationNode(Resources.SPIKE_THRESHOLD.get(), Resources.SPIKE_THRESHOLD.getId() + "_" + i, spikeThreshs.get(i).getValue()));
			}

			// Specific Capacitance
			for(int i = 0; i < specificCapacitances.size(); i++)
			{
				membranePropertiesNode.addChild(createParameterSpecificationNode(Resources.SPECIFIC_CAPACITANCE.get(), Resources.SPECIFIC_CAPACITANCE.getId() + "_" + i, specificCapacitances.get(i).getValue()));
			}
			
			// Initial Membrance Potentials
			for(int i = 0; i < initMembPotentials.size(); i++)
			{
				membranePropertiesNode.addChild(createParameterSpecificationNode(Resources.INIT_MEMBRANE_POTENTIAL.get(), Resources.INIT_MEMBRANE_POTENTIAL.getId() + "_" + i, initMembPotentials.get(i).getValue()));
			}
			
			biophysicalPropertiesNode.addChild(membranePropertiesNode);
		}
		
		IntracellularProperties intracellularProperties = properties.getIntracellularProperties();
		biophysicalPropertiesNode.addChild(createIntracellularPropertiesNode(intracellularProperties, neuroMLAccessUtility, model));
		
		ExtracellularProperties extracellularProperties = properties.getExtracellularProperties();
		biophysicalPropertiesNode.addChild(createExtracellularPropertiesNode(extracellularProperties, neuroMLAccessUtility, model));
		
		return biophysicalPropertiesNode;
	}
	
	public CompositeNode createIntracellularPropertiesNode(IntracellularProperties intracellularProperties, NeuroMLAccessUtility neuroMLAccessUtility, ModelWrapper model) throws ModelInterpreterException {
		if(intracellularProperties != null)
		{
			CompositeNode intracellularPropertiesNode = new CompositeNode(Resources.INTRACELLULAR_P.getId(), Resources.INTRACELLULAR_P.get());
			
			
			List<Resistivity> resistivities = intracellularProperties.getResistivity();
			List<Species> species = intracellularProperties.getSpecies();
			
			// Resistivity
			CompositeNode resistivitiesNode = new CompositeNode(Resources.RESISTIVITY.getId(), Resources.RESISTIVITY.get());
			for(int i = 0; i < resistivities.size(); i++)
			{
				resistivitiesNode.addChild(createParameterSpecificationNode(Resources.RESISTIVITY.get(), Resources.RESISTIVITY.getId() + "_" + i, resistivities.get(i).getValue()));
			}
			intracellularPropertiesNode.addChild(resistivitiesNode);
			
			if (species != null){
				CompositeNode speciesNode = new CompositeNode(Resources.SPECIES.getId(), Resources.SPECIES.get());
				speciesNode.addChildren(createSpeciesNode(species, neuroMLAccessUtility, model));
				intracellularPropertiesNode.addChild(speciesNode);
			}
			return intracellularPropertiesNode;
		}
		return null;
	}
	
	public CompositeNode createExtracellularPropertiesNode(ExtracellularProperties extracellularProperties, NeuroMLAccessUtility neuroMLAccessUtility, ModelWrapper model) throws ModelInterpreterException {
		if(extracellularProperties != null){
			CompositeNode extracellularPropertiesNode = new CompositeNode(Resources.EXTRACELLULAR_P.getId(), Resources.EXTRACELLULAR_P.get());
			extracellularPropertiesNode.addChildren(createBaseChildren(extracellularProperties));
			
			List<Species> species = extracellularProperties.getSpecies();
			if (species != null){
				CompositeNode speciesNode = new CompositeNode(Resources.SPECIES.getId(), Resources.SPECIES.get());
				speciesNode.addChildren(createSpeciesNode(species, neuroMLAccessUtility, model));
				extracellularPropertiesNode.addChild(speciesNode);
			}
			return extracellularPropertiesNode;
		}
		return null;
	}
	
	private List<CompositeNode> createSpeciesNode(List<Species> species, NeuroMLAccessUtility neuroMLAccessUtility, ModelWrapper model) throws ModelInterpreterException {
		List<CompositeNode> speciesNodeList = new ArrayList<CompositeNode>();
		// Specie
		for(Species specie : species)
		{
			CompositeNode speciesNodeItem = new CompositeNode(Resources.SPECIES.get(), Resources.SPECIES.getId());
			
			// Initial Concentration
			speciesNodeItem.addChild(createParameterSpecificationNode(Resources.INIT_CONCENTRATION.get(), Resources.INIT_CONCENTRATION.getId() + "_" + specie.getId(), specie.getInitialConcentration()));
			
			// Initial External Concentration
			speciesNodeItem.addChild(createParameterSpecificationNode(Resources.INIT_EXT_CONCENTRATION.get(), Resources.INIT_EXT_CONCENTRATION.getId() + "_" + specie.getId(), specie.getInitialExtConcentration()));
			
			// Ion
			speciesNodeItem.addChild(new TextMetadataNode(Resources.ION.get(), Resources.ION.getId() + "_" + specie.getId(),  new StringValue(specie.getIon())));
			
			// Concentration Model
			Object concentrationModel = neuroMLAccessUtility.getComponent(specie.getConcentrationModel(), model, Resources.CONCENTRATION_MODEL);
			speciesNodeItem.addChild(createConcentrationModel(concentrationModel));
			
			speciesNodeList.add(speciesNodeItem);
		}
		return speciesNodeList;
	}
	
	public CompositeNode createConcentrationModel(Object concentrationModel) {
		if (concentrationModel != null){
			CompositeNode concentrationModelNode = new CompositeNode(Resources.CONCENTRATION_MODEL.get(), Resources.CONCENTRATION_MODEL.getId());
			if (concentrationModel instanceof DecayingPoolConcentrationModel){
				DecayingPoolConcentrationModel decayingPoolConcentrationModel = (DecayingPoolConcentrationModel) concentrationModel;
				concentrationModelNode.addChild(createParameterSpecificationNode(Resources.DECAY_CONSTANT.get(), Resources.DECAY_CONSTANT.getId(), decayingPoolConcentrationModel.getDecayConstant()));
				concentrationModelNode.addChild(createParameterSpecificationNode(Resources.RESTING_CONC.get(), Resources.RESTING_CONC.getId(), decayingPoolConcentrationModel.getRestingConc()));
				concentrationModelNode.addChild(createParameterSpecificationNode(Resources.SHELL_THICKNESS.get(), Resources.SHELL_THICKNESS.getId(), decayingPoolConcentrationModel.getShellThickness()));
				concentrationModelNode.addChild(new TextMetadataNode(Resources.ION.get(), Resources.ION.getId(),  new StringValue(decayingPoolConcentrationModel.getIon())));
				concentrationModelNode.addChild(new TextMetadataNode(Resources.NOTES.get(), Resources.NOTES.getId(),  new StringValue(decayingPoolConcentrationModel.getNotes())));
			}
			else{
				FixedFactorConcentrationModel fixedFactorConcentrationModel = (FixedFactorConcentrationModel) concentrationModel;
				concentrationModelNode.addChild(createParameterSpecificationNode(Resources.DECAY_CONSTANT.get(), Resources.DECAY_CONSTANT.getId(), fixedFactorConcentrationModel.getDecayConstant()));
				concentrationModelNode.addChild(createParameterSpecificationNode(Resources.RESTING_CONC.get(), Resources.RESTING_CONC.getId(), fixedFactorConcentrationModel.getRestingConc()));
				concentrationModelNode.addChild(createParameterSpecificationNode(Resources.RHO.get(), Resources.RHO.getId(), fixedFactorConcentrationModel.getRho()));
				concentrationModelNode.addChild(new TextMetadataNode(Resources.ION.get(), Resources.ION.getId(),  new StringValue(fixedFactorConcentrationModel.getIon())));
				concentrationModelNode.addChild(new TextMetadataNode(Resources.NOTES.get(), Resources.NOTES.getId(),  new StringValue(fixedFactorConcentrationModel.getNotes())));
			}
			concentrationModelNode.addChildren(createStandaloneChildren((Standalone)concentrationModel));
			
			return concentrationModelNode;
		}
		return null;
	}

	public CompositeNode createGateNode(Base gate, NeuroMLAccessUtility neuroMLAccessUtility, ModelWrapper model) throws ModelInterpreterException, ContentError{
		CompositeNode gateNode = new CompositeNode(Resources.GATE.get(), gate.getId());
		if (gate instanceof  GateHHUndetermined){
			GateHHUndetermined gateHHUndetermined = (GateHHUndetermined) gate;
	
			//Forward Rate
			gateNode.addChild(createRateGateNode(Resources.FW_RATE, gateHHUndetermined.getForwardRate(), neuroMLAccessUtility, model));
			
			//Reverse Rate
			gateNode.addChild(createRateGateNode(Resources.BW_RATE, gateHHUndetermined.getReverseRate(), neuroMLAccessUtility, model));
			
			//Type
			ComponentType typeRate = (ComponentType) neuroMLAccessUtility.getComponent(gateHHUndetermined.getType().value(), model, Resources.COMPONENT_TYPE);
			gateNode.addChild(createCompositeNodeFromComponentType(Resources.GATE_DYNAMICS.get(), Resources.GATE_DYNAMICS.getId(), typeRate));
			
			//Instances
			gateNode.addChild(new TextMetadataNode(Resources.INSTANCES.get(), Resources.INSTANCES.getId(),  new IntValue(gateHHUndetermined.getInstances().intValue())));
			
			//Q10Settings
			gateNode.addChild(createQ10SettingsNode(neuroMLAccessUtility, model, gateHHUndetermined.getQ10Settings()));
			
			//Time Course
			gateNode.addChild(createTimeCourseNode(Resources.TIMECOURSE, gateHHUndetermined.getTimeCourse(), neuroMLAccessUtility, model));
			//Steady State
			gateNode.addChild(createSteadyStateNode(Resources.STEADY_STATE, gateHHUndetermined.getSteadyState(), neuroMLAccessUtility, model));
			
			gateNode.addChild(new TextMetadataNode(Resources.NOTES.get(), Resources.NOTES.getId(),  new StringValue(gateHHUndetermined.getNotes())));
		}
		else if (gate instanceof  GateHHRates) {
			GateHHRates gateHHRates = (GateHHRates) gate;

			gateNode.addChild(createRateGateNode(Resources.FW_RATE, gateHHRates.getForwardRate(), neuroMLAccessUtility, model));
			gateNode.addChild(createRateGateNode(Resources.BW_RATE, gateHHRates.getReverseRate(), neuroMLAccessUtility, model));

			ComponentType typeRate = (ComponentType) neuroMLAccessUtility.getComponent(gateHHRates.getType().value(), model, Resources.COMPONENT_TYPE);
			gateNode.addChild(createCompositeNodeFromComponentType(Resources.GATE_DYNAMICS.get(), Resources.GATE_DYNAMICS.getId(), typeRate));
			
			gateNode.addChild(new TextMetadataNode(Resources.INSTANCES.get(), Resources.INSTANCES.getId(),  new IntValue(gateHHRates.getInstances().intValue())));
			
			gateNode.addChild(createQ10SettingsNode(neuroMLAccessUtility, model, gateHHRates.getQ10Settings()));
			
			gateNode.addChild(new TextMetadataNode(Resources.NOTES.get(), Resources.NOTES.getId(),  new StringValue(gateHHRates.getNotes())));
			
		}
		else if (gate instanceof  GateHHRatesInf) {
			GateHHRatesInf gateHHRatesInf = (GateHHRatesInf) gate;
			
			gateNode.addChild(createRateGateNode(Resources.FW_RATE, gateHHRatesInf.getForwardRate(), neuroMLAccessUtility, model));
			gateNode.addChild(createRateGateNode(Resources.BW_RATE, gateHHRatesInf.getReverseRate(), neuroMLAccessUtility, model));

			ComponentType typeRate = (ComponentType) neuroMLAccessUtility.getComponent(gateHHRatesInf.getType().value(), model, Resources.COMPONENT_TYPE);
			gateNode.addChild(createCompositeNodeFromComponentType(Resources.GATE_DYNAMICS.get(), Resources.GATE_DYNAMICS.getId(), typeRate));
			
			gateNode.addChild(new TextMetadataNode(Resources.INSTANCES.get(), Resources.INSTANCES.getId(),  new IntValue(gateHHRatesInf.getInstances().intValue())));
			
			gateNode.addChild(createQ10SettingsNode(neuroMLAccessUtility, model, gateHHRatesInf.getQ10Settings()));
			
			gateNode.addChild(new TextMetadataNode(Resources.NOTES.get(), Resources.NOTES.getId(),  new StringValue(gateHHRatesInf.getNotes())));
			
			gateNode.addChild(createSteadyStateNode(Resources.STEADY_STATE, gateHHRatesInf.getSteadyState(), neuroMLAccessUtility, model));
		}
		else if (gate instanceof  GateHHRatesTau) {
			GateHHRatesTau gateHHRatesTau = (GateHHRatesTau) gate;
			
			gateNode.addChild(createRateGateNode(Resources.FW_RATE, gateHHRatesTau.getForwardRate(), neuroMLAccessUtility, model));
			gateNode.addChild(createRateGateNode(Resources.BW_RATE, gateHHRatesTau.getReverseRate(), neuroMLAccessUtility, model));

			ComponentType typeRate = (ComponentType) neuroMLAccessUtility.getComponent(gateHHRatesTau.getType().value(), model, Resources.COMPONENT_TYPE);
			gateNode.addChild(createCompositeNodeFromComponentType(Resources.GATE_DYNAMICS.get(), Resources.GATE_DYNAMICS.getId(), typeRate));
			
			gateNode.addChild(new TextMetadataNode(Resources.INSTANCES.get(), Resources.INSTANCES.getId(),  new IntValue(gateHHRatesTau.getInstances().intValue())));
			
			gateNode.addChild(createQ10SettingsNode(neuroMLAccessUtility, model, gateHHRatesTau.getQ10Settings()));
			
			gateNode.addChild(new TextMetadataNode(Resources.NOTES.get(), Resources.NOTES.getId(),  new StringValue(gateHHRatesTau.getNotes())));
			
			gateNode.addChild(createTimeCourseNode(Resources.TIMECOURSE, gateHHRatesTau.getTimeCourse(), neuroMLAccessUtility, model));
			
		}
		else if (gate instanceof  GateHHTauInf) {
			GateHHTauInf gateHHTauInf = (GateHHTauInf) gate;
			
			ComponentType typeRate = (ComponentType) neuroMLAccessUtility.getComponent(gateHHTauInf.getType().value(), model, Resources.COMPONENT_TYPE);
			gateNode.addChild(createCompositeNodeFromComponentType(Resources.GATE_DYNAMICS.get(), Resources.GATE_DYNAMICS.getId(), typeRate));
			
			gateNode.addChild(new TextMetadataNode(Resources.INSTANCES.get(), Resources.INSTANCES.getId(),  new IntValue(gateHHTauInf.getInstances().intValue())));
			
			gateNode.addChild(createQ10SettingsNode(neuroMLAccessUtility, model, gateHHTauInf.getQ10Settings()));
			
			gateNode.addChild(new TextMetadataNode(Resources.NOTES.get(), Resources.NOTES.getId(),  new StringValue(gateHHTauInf.getNotes())));
			
			gateNode.addChild(createTimeCourseNode(Resources.TIMECOURSE, gateHHTauInf.getTimeCourse(), neuroMLAccessUtility, model));
			
			gateNode.addChild(createSteadyStateNode(Resources.STEADY_STATE, gateHHTauInf.getSteadyState(), neuroMLAccessUtility, model));
		}

		
		gateNode.addChildren(createBaseChildren(gate));
		return gateNode;
	}

	private CompositeNode createQ10SettingsNode(NeuroMLAccessUtility neuroMLAccessUtility, ModelWrapper model, Q10Settings q10Settings)	throws ModelInterpreterException, ContentError {
		if (q10Settings != null) {
			CompositeNode q10SettingsNode = new CompositeNode(Resources.Q10SETTINGS.get(), Resources.Q10SETTINGS.getId());
			q10SettingsNode.addChild(new TextMetadataNode(Resources.EXPERIMENTAL_TEMP.get(), Resources.EXPERIMENTAL_TEMP.getId(),  new StringValue(q10Settings.getExperimentalTemp())));
			q10SettingsNode.addChild(new TextMetadataNode(Resources.FIXEDQ10.get(), Resources.FIXEDQ10.getId(),  new StringValue(q10Settings.getFixedQ10())));
			q10SettingsNode.addChild(new TextMetadataNode(Resources.Q10FACTOR.get(), Resources.Q10FACTOR.getId(),  new StringValue(q10Settings.getQ10Factor())));
			ComponentType typeQ10Settings = (ComponentType) neuroMLAccessUtility.getComponent(q10Settings.getType(), model, Resources.COMPONENT_TYPE);
			q10SettingsNode.addChild(createCompositeNodeFromComponentType(Resources.TYPE.get(), Resources.TYPE.getId(), typeQ10Settings));
			return q10SettingsNode;
		}
		return null;
	}
	
	public CompositeNode createChannelNode(Standalone ionChannelBase, NeuroMLAccessUtility neuroMLAccessUtility, ModelWrapper model) throws ModelInterpreterException, ContentError {
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
			ionChannelNode.addChild(createGateNode(gateHHUndetermined, neuroMLAccessUtility, model));
		}
		for (GateHHRates gateHHRates : ionChannel.getGateHHrates()){
			ionChannelNode.addChild(createGateNode(gateHHRates, neuroMLAccessUtility, model));
		}
		for (GateHHRatesInf gateHHRatesInf : ionChannel.getGateHHratesInf()){
			ionChannelNode.addChild(createGateNode(gateHHRatesInf, neuroMLAccessUtility, model));
		}
		for (GateHHRatesTau gateHHRatesTau : ionChannel.getGateHHratesTau()){
			ionChannelNode.addChild(createGateNode(gateHHRatesTau, neuroMLAccessUtility, model));
		}
		for (GateHHTauInf gateHHRatesTauInf : ionChannel.getGateHHtauInf()){
			ionChannelNode.addChild(createGateNode(gateHHRatesTauInf, neuroMLAccessUtility, model));
		}
		
		ionChannelNode.addChild(createParameterSpecificationNode(Resources.CONDUCTANCE.get(), Resources.CONDUCTANCE.getId(), ionChannel.getConductance()));
		
		ionChannelNode.addChild(new TextMetadataNode(Resources.SPECIES.get(), Resources.SPECIES.getId(), new StringValue(ionChannel.getSpecies())));
		
		if (ionChannel.getType() != null){
			ComponentType typeIonChannel = (ComponentType) neuroMLAccessUtility.getComponent(ionChannel.getType().value(), model, Resources.COMPONENT_TYPE);
			ionChannelNode.addChild(createCompositeNodeFromComponentType(Resources.IONCHANNEL_DYNAMICS.get(), Resources.IONCHANNEL_DYNAMICS.getId(), typeIonChannel));
		}
		return ionChannelNode;
	}
	
	
	public Collection<ParameterSpecificationNode> createIafTauCellChildren(IafTauCell c){
		Collection<ParameterSpecificationNode> iafTauCellChildren = new ArrayList<ParameterSpecificationNode>();
		iafTauCellChildren.add(createParameterSpecificationNode(Resources.LEAK_REVERSAL.get(), Resources.LEAK_REVERSAL.getId(), c.getLeakReversal()));
		iafTauCellChildren.add(createParameterSpecificationNode(Resources.TAU.get(), Resources.TAU.getId(), c.getTau()));
		iafTauCellChildren.add(createParameterSpecificationNode(Resources.RESET.get(), Resources.RESET.getId(), c.getReset()));
		iafTauCellChildren.add(createParameterSpecificationNode(Resources.THRESH.get(), Resources.THRESH.getId(), c.getThresh()));
		return iafTauCellChildren;
	}
	
	public Collection<ParameterSpecificationNode> createIafCellChildren(IafCell c){
		Collection<ParameterSpecificationNode> iafCellChildren = new ArrayList<ParameterSpecificationNode>();
		iafCellChildren.add(createParameterSpecificationNode(Resources.LEAK_REVERSAL.get(), Resources.LEAK_REVERSAL.getId(), c.getLeakReversal()));
		iafCellChildren.add(createParameterSpecificationNode(Resources.LEAK_CONDUCTANCE.get(), Resources.LEAK_CONDUCTANCE.getId(), c.getLeakReversal()));
		iafCellChildren.add(createParameterSpecificationNode(Resources.CAPACITANCE.get(), Resources.CAPACITANCE.getId(), c.getC()));
		iafCellChildren.add(createParameterSpecificationNode(Resources.RESET.get(), Resources.RESET.getId(), c.getReset()));
		iafCellChildren.add(createParameterSpecificationNode(Resources.THRESH.get(), Resources.THRESH.getId(), c.getThresh()));
		return iafCellChildren;
	}
	
	public Collection<ANode> createStandaloneChildren(Standalone standaloneComponent){
		Collection<ANode> standaloneChildren = new ArrayList<ANode>();

		standaloneChildren.addAll(createBaseChildren(standaloneComponent));
		
		//TODO: Improve to parse all the attribute in an annotation	
		Annotation annotation = standaloneComponent.getAnnotation();
		if (annotation != null){
			CompositeNode annotationNode = new CompositeNode(Resources.ANOTATION.get(), Resources.ANOTATION.getId());
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
	

	
//	public Collection<ANode> createConcentrationModelChildren(Standalone concentrationModel){
//		Collection<ANode> concentrationModelChildren = new ArrayList();
//		if (concentrationModel instanceof DecayingPoolConcentrationModel){
//			DecayingPoolConcentrationModel decayingPoolConcentrationModel = (DecayingPoolConcentrationModel) concentrationModel;
//			concentrationModelChildren.add(new TextMetadataNode(Resources.ION.get(), Resources.ION.getId(),  new StringValue(decayingPoolConcentrationModel.getIon())));
//			concentrationModelChildren.add(createParameterSpecificationNode(Resources.DECAY_CONSTANT, "DecayConstant", decayingPoolConcentrationModel.getDecayConstant()));
//			
//		}
//		else{
//			
//		}
//		return concentrationModelChildren;
//	}
	
}
