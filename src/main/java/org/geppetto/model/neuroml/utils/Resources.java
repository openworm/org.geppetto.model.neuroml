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

package org.geppetto.model.neuroml.utils;

/**
 * Class to hold resources used in the visualiser. This elements will be displayed to the user.
 * @author matteocantarelli
 * @author Adrian Quintana (adrian.perez@ucl.ac.uk)
 *
 */
public enum Resources
{
	PASSIVE_COND_DENSITY("Passive Conductance Density", "passiveConductanceDensity"),
	COND_DENSITY("Conductance Density", "condDensity"),
	SPIKE_THRESHOLD("Spike Threshold", "spikeThreshold"),
	EREV("Reverse Potential", "reversePotential"),
	SPECIFIC_CAPACITANCE("Specific Capacitance", "specificCapacitance"),
	INIT_MEMBRANE_POTENTIAL("Initial Membrane Potential", "initialMembranePotential"),
	RESISTIVITY("Resistivity", "resistivity"),
	MEMBRANE_P("Membrane Properties", "membraneProperties"),
	MEMBRANE_P_SOMA("Soma Membrane Properties", "somaMembraneProperties"),
	INTRACELLULAR_P("Intracellular Properties", "intracellularProperties"),
	INTRACELLULAR_P_SOMA("Soma Intracellular Properties", "somaIntracellularProperties"), 
	SYNAPSE("Synapse", "synapse"), 
	CONNECTION("Connection", "connection"),
	PROJECTION("Projection", "projection"),
	PRE_SYNAPTIC("Input", "input"),
	POST_SYNAPTIC("Output", "output"),
	ION("Ion", "ion"),
	ION_CHANNEL("Ion Channel", "ionChannel"),
	CHANNEL_DENSITY("Channel Density", "channelDensity"),
	SPECIES("Species", "species"),
	INIT_CONCENTRATION("Initial Concentration", "initialConcentration"),
	INIT_EXT_CONCENTRATION("Initial External Concentration", "initialExternalConcentration"),
	CONCENTRATION_MODEL("Concentration Model", "concentrationModel"),
	BIOPHYSICAL_PROPERTIES("Biophysical Properties", "biophysicalProperties"),
	PULSE_GENERATOR("Pulse Generator", "pulseGenerator"),
	AMPLITUDE("Amplitude", "amplitude"),
	DELAY("Delay", "delay"),
	DURATION("Duration", "duration"),
	ANOTATION("Anotation", "anotation"),
	FW_RATE("Forward Rate", "forwardRate"),
	FW_RATE_FN("Forward Rate Dynamics", "forwardRateDynamics"),
	BW_RATE("Backward Rate", "backwardRate"),
	BW_RATE_FN("Backward Rate Dynamics", "backwardRateDynamics"),
	MIDPOINT("Midpoint", "midpoint"),
	RATE("Rate", "rate"),
	SCALE("Scale", "scale"),
	GATE("Gate", "gate"),
	TIMECOURSE("Time Course", "timeCourse"),
	TAU("Tau", "tau"),
	STEADY_STATE("Steady State", "steadyState"),
	CELL("Cell", "cell"),
	COMPONENT_TYPE("Component", "component"),
	GATE_DYNAMICS("Gate Dynamics", "gateDynamics"),
	DYNAMICS("Dynamics", "dynamics"),
	CONDUCTANCE("Conductance", "conductance"),
	IONCHANNEL_DYNAMICS("Ion Channel Dynamics", "ionChannelDynamics"),
	NOTES("Notes", "notes"),
	DECAY_CONSTANT("Decay Constant", "decayConstant"),
	RESTING_CONC("Resting Concentration", "restingConcentration"),
	RHO("Rho", "rho"),
	SHELL_THICKNESS("Shell Thickness", "shellThickness"),
	THRESH("Thresh", "thresh"),
	RESET("Reset", "reset"),
	LEAK_REVERSAL("Leak Reversal","leakReversal"),
	LEAK_CONDUCTANCE("Leak Conductance", "leakConductance"),
	REFRACT("Refract", "refract"),
	CAPACITANCE("Capacitance", "capacitance"),
	EL("EL", "EL"),
	VT("VT", "VT"),
	A("a", "a"),
	B("b", "b"),
	C("c", "c"),
	D("d", "d"),
	DELT("delT", "delT"),
	GL("gL", "gL"),
	TAUW("tauw", "tauw"),
	I("I", "i"),
	v0("v0", "v0"),
	ELEMENT("Element", "element"),
	ID("Id", "id"),
	NEUROLEX_ID("NeuroLexId", "neuroLexId"),
	SIZE("Size", "size"),
	METAID("MetaId", "metaId"),
	POPULATION_TYPE("Population Type", "populationType"),
	INSTANCES("Instances", "Instances"),
	Q10SETTINGS("Q10 Settings", "q10Settings"),
	EXPERIMENTAL_TEMP("Experimental Temperature", "experimentalTemperature"),
	TEMP("Temperature", "temperature"),
	FIXEDQ10("Fixed Q10","fixedQ10"),
	Q10FACTOR("Q10 Factor", "q10Factor"),
	TYPE("Type", "type"),
	NAME("Name", "name"),
	EXTENDS("Extends", "Extends"),
	DESCRIPTION("Description", "description"),
	ION_CHANNEL_HH("Ion Channel HH", "ionChannelHH"),
	EXTRACELLULAR_P("Extracellular Properties", "extracellularProperties"),
	NETWORK("Network", "network"),
	POPULATION("Population", "population"),
	PARAMETER("Parameter", "parameter"),
	GBASE("GBase", "gBase"), 
	TAUDECAY("Tau Decay", "tauDecay"),
	TAURISE("Tau Rise", "tauRise"),
	EXPTWOSYNAPSE("Exp Two Synapse", "expTwoSynapse"),
	EXPONESYNAPSE("Exp One Synapse", "expOneSynapse"),
	BLOCKINGPLASTICSYNAPSE("Blocking Plastic Synapse", "blockingPlasticSynapse"),
	PLASTICITYMECHANISM("Plasticity Mechanism", "plasticityMechanism"),
	INITRELEASEPROB("Init Release Probability", "initReleaseProb"),
	TAUFAC("Tau Fac", "tauFac"),
	TAUREC("Tau Rec", "tauRec"),
	BLOCKMECHANISM("Block Mechanism", "blockMechanism"),
	SCALINGCONC("Scaling Concentration", "scalingConc"),
	BLOCKCONCENTRATION("Block Concentration", "blockConcentration"),
	SCALINGVOLT("Scaling Voltage", "scalingVolt"),
	TAUSYN("Tau Syn", "tauSyn"),
	CONNECTIONORIGIN("Connection Origin", "ConnectionOrigin"),
	CONNECTIONFROM("Connection From", "ConnectionFrom"),
	CONNECTIONDESTINATION("Connection Destination", "ConnectionDestination"),
	CONNECTIONTO("Connection To", "ConnectionTo"),
	PRESEGMENT("Presegment", "presegment"),
	POSTSEGMENT("Postsegment", "postsegment"),
	PREFRACTIONALONG("Prefraction Along", "prefractionalong"),
	POSTFRACTIONALONG("Postfraction Along", "postfractionalong"),
	MODEL_DESCRIPTION("Model Description", "modelDescription"),
	RESOURCE("Resource", "resource"),
	PYNN_SYNAPSE("Pynn Synapse", "pynnSynapse"),
	TAU_M("Tau M", "tauM"),
	TAU_REFRAC("Tau Refrac", "taurefrac"),
	V_RESET("V Reset", "vReset"),
	V_REST("V Rest", "vRest"),
	V_THRESH("V Thresh", "vThresh"),
	CM("CM", "cm"),
	I_OFFSET("I Offset", "iOffset"),
	TAU_SYN_E("Tau Syn E","tauSynE"),
	TAU_SYN_I("Tau Syn I","tauSynI"),
	V_INIT("V Init", "vInit"),
	DELTAT("Delta T", "deltaT"),
	V_SPIKE("V Spike", "vSpike"),
	V_OFFSET("V Offset", "vOffset"),
	E_REV_E("E Rev E", "eRevE"),
	E_REV_I("E Rev I", "eRevI"),
	E_REV_K("E Rev K", "eRevK"),
	E_REV_NA("E Rev I", "eRevNa"),
	E_REV_LEAK("E Rev I", "eRevLEAK"),
	G_LEAK("G Leak","gLeak"),
	G_BARK("G Bark", "gBark"),
	G_BAR_NA("G Bar Na", "gBarNa"),
	NUMBER_CHANNEL("Number of Channels", "NumberChannels"),
	VARIABLE_PARAMETER("Variable Parameter", "VariableParameter"),
	INHOMOGENEOUS_VALUE("Inhomogeneous Value", "inhomogeneousValue"),
	VALUE("Value", "value"),
	CHANNEL_DENSITY_NERNST("Channel Density Nernst", "channelDensityNernst"),
	CHANNEL_DENSITY_GHK("Channel Density GHK", "channelDensityGHK"),
	PERMEABILITY("Permeability", "permeability"),
	CHANNEL_DENSITY_NON_UNIFORM("Channel Density Non Uniform", "channelDensityNonUniform"),
	CHANNEL_DENSITY_NON_UNIFORM_NERNST("Channel Density Non Uniform Nernst", "channelDensityNonUniformNernst"),
	CONNECTION_ID("Connection Id", "ConnectionId"),
	PROJECTION_ID("Projection Id", "ProjectionId"),
	INPUT_LIST("Input List", "inputList"),
	POST_SYNAPTIC_POPULATION("Post Synaptic Population", "postsynapticPopulation"),
	PRE_SYNAPTIC_POPULATION("Pre Synaptic Population", "presynapticPopulation"),
	MORPHOLOGY("Morphology", "morphology"),
	ANNOTATION("Annotation", "annotation"),
	CELL_REGIONS("Cell Regions", "cellRegions"),
	SOMA("Soma", "soma_group"),
	AXONS("Axons", "axon_group"),
	DENDRITES("Dendrites", "dendrite_group"),
	
	
	BIO_RESOURCE("Resource", "rdf:resource"),
	BIO_DESCRIBED("Description", "bqmodel:isDescribedBy"),
	BIO_Version("Version", "bqbiol:isVersionOf"),
	;
	
	
	
	private String _value;
	private String _id;
	
	private Resources(String value, String id)
	{
		_value = value;
		_id = id;
	}
	
	public String get()
	{
		return _value;
	}
	
	public String getId()
	{
		return _id;
	}
	
	public static String getValueById(String id){
		for(Resources e : Resources.values()){
            if(id.equals(e._id)) return e._value;
        }
		//If we can't find a value, return the id
        return id;
	}
}
