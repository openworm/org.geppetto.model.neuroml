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
 *
 */
public enum Resources
{
	COND_DENSITY("Passive Conductance Density", "PassiveConductanceDensity"),
	SPIKE_THRESHOLD("Spike Threshold", "SpikeThreshold"),
	EREV("Reverse Potential", "ReversePotential"),
	SPECIFIC_CAPACITANCE("Specific Capacitance", "SpecificCapacitance"),
	INIT_MEMBRANE_POTENTIAL("Initial Membrane Potential", "InitialMembranePotential"),
	RESISTIVITY("Resistivity", "Resistivity"),
	MEMBRANE_P("Membrane Properties", "MembraneProperties"),
	MEMBRANE_P_SOMA("Soma Membrane Properties", "SomaMembraneProperties"),
	INTRACELLULAR_P("Intracellular Properties", "IntracellularProperties"),
	INTRACELLULAR_P_SOMA("Soma Intracellular Properties", "SomaIntracellularProperties"), 
	SYNAPSE("Synapse", "Synapse"), 
	CONNECTION("Connection", "Connection"),
	PRE_SYNAPTIC("Input", "Input"),
	POST_SYNAPTIC("Output", "Output"),
	ION("Ion", "Ion"),
	ION_CHANNEL("Ion Channel", "IonChannel"),
	CHANNEL_DENSITY("Channel Density", "ChannelDensity"),
	SPECIES("Species", "Species"),
	INIT_CONCENTRATION("Initial Concentration", "InitialConcentration"),
	INIT_EXT_CONCENTRATION("Initial External Concentration", "InitialExternalConcentration"),
	CONCENTRATION_MODEL("Concentration Model", "ConcentrationModel"),
	BIOPHYSICAL_PROPERTIES("Biophysical Properties", "BiophysicalProperties"),
	PULSE_GENERATOR("Pulse Generator", "PulseGenerator"),
	AMPLITUDE("Amplitude", "Amplitude"),
	DELAY("Delay", "Delay"),
	DURATION("Duration", "Duration"),
	ANOTATION("Anotation", "Anotation"),
	FW_RATE("Forward Rate", "ForwardRate"),
	FW_RATE_FN("Forward Rate Dynamics", "ForwardRateDynamics"),
	BW_RATE("Backward Rate", "BackwardRate"),
	BW_RATE_FN("Backward Rate Dynamics", "BackwardRateDynamics"),
	MIDPOINT("Midpoint", "Midpoint"),
	RATE("Rate", "Rate"),
	SCALE("Scale", "Scale"),
	GATE("Gate", "Gate"),
	TIMECOURSE("Time Course", "TimeCourse"),
	TAU("Tau", "Tau"),
	STEADY_STATE("Steady State", "SteadyState"),
	CELL("Cell", "Cell"),
	COMPONENT_TYPE("Component", "Component"),
	GATE_DYNAMICS("Gate Dynamics", "GateDynamics"),
	DYNAMICS("Dynamics", "Dynamics"),
	CONDUCTANCE("Conductance", "Conductance"),
	IONCHANNEL_DYNAMICS("Ion Channel Dynamics", "IonChannelDynamics"),
	NOTES("Notes", "Notes"),
	DECAY_CONSTANT("Decay Constant", "DecayConstant"),
	RESTING_CONC("Resting Concentration", "RestingConcentration"),
	RHO("Rho", "Rho"),
	SHELL_THICKNESS("Shell Thickness", "ShellThickness"),
	THRESH("Thresh", "Thresh"),
	RESET("Reset", "Reset"),
	LEAK_REVERSAL("Leak Reversal","LeakReversal"),
	LEAK_CONDUCTANCE("Leak Conductance", "LeakConductance"),
	REFRACT("Refract", "Refract"),
	CAPACITANCE("Capacitance", "Capacitance"),
	EL("EL", "EL"),
	VT("VT", "VT"),
	A("a", "a"),
	B("b", "b"),
	C("c", "c"),
	D("d", "d"),
	DELT("delT", "delT"),
	GL("gL", "gL"),
	TAUW("tauw", "tauw"),
	I("I", "I"),
	v0("v0", "v0"),
	ELEMENT("Element", "Element"),
	ID("Id", "Id"),
	NEUROLEX_ID("NeuroLexId", "NeuroLexId"),
	SIZE("Size", "Size"),
	METAID("MetaId", "MetaId"),
	POPULATION_TYPE("Population Type", "PopulationType"),
	INSTANCES("Instances", "Instances"),
	Q10SETTINGS("Q10 Settings", "Q10Settings"),
	EXPERIMENTAL_TEMP("Experimental Temperature", "ExperimentalTemperature"),
	FIXEDQ10("Fixed Q10","FixedQ10"),
	Q10FACTOR("Q10 Factor", "Q10Factor"),
	TYPE("Type", "Type"),
	NAME("Name", "Name"),
	EXTENDS("Extends", "Extends"),
	DESCRIPTION("Description", "Description"),
	ION_CHANNEL_HH("Ion Channel HH", "IonChannelHH"),
	EXTRACELLULAR_P("Extracellular Properties", "ExtracellularProperties"),
	NETWORK("Network", "Network"),
	POPULATION("Population", "Population"),
	PARAMETER("Parameter", "Parameter"),
	GBASE("GBase", "GBase"), 
	TAUDECAY("Tau Decay", "TauDecay"),
	TAURISE("Tau Rise", "TauRise"),
	EXPTWOSYNAPSE("Exp Two Synapse", "ExpTwoSynapse"),
	EXPONESYNAPSE("Exp One Synapse", "ExpOneSynapse"),
	BLOCKINGPLASTICSYNAPSE("Blocking Plastic Synapse", "BlockingPlasticSynapse"),
	PLASTICITYMECHANISM("Plasticity Mechanism", "PlasticityMechanism"),
	INITRELEASEPROB("Init Release Probability", "InitReleaseProb"),
	TAUFAC("Tau Fac", "TauFac"),
	TAUREC("Tau Rec", "TauRec"),
	BLOCKMECHANISM("Block Mechanism", "BlockMechanism"),
	SCALINGCONC("Scaling Concentration", "ScalingConc"),
	BLOCKCONCENTRATION("Block Concentration", "BlockConcentration"),
	SCALINGVOLT("Scaling Voltage", "ScalingVolt"),
	TAUSYN("Tau Syn", "TauSyn"),
	CONNECTIONORIGIN("Connection Origin", "ConnectionOrigin"),
	CONNECTIONFROM("Connection From", "ConnectionFrom"),
	CONNECTIONDESTINATION("Connection Destination", "ConnectionDestination"),
	CONNECTIONTO("Connection To", "ConnectionTo"),
	PRESEGMENT("Presegment", "Presegment"),
	POSTSEGMENT("Postsegment", "Postsegment"),
	PREFRACTIONALONG("Prefraction Along", "Prefractionalong"),
	POSTFRACTIONALONG("Postfraction Along", "Postfractionalong"),
	SUMMARY("Summary", "Summary"),
	RESOURCE("Resource", "Resource"),
	PYNN_SYNAPSE("Pynn Synapse", "PynnSynapse"),
	TAU_M("Tau M", "TauM"),
	TAU_REFRAC("Tau Refrac", "Taurefrac"),
	V_RESET("V Reset", "VReset"),
	V_REST("V Rest", "VRest"),
	V_THRESH("V Thresh", "VThresh"),
	CM("CM", "Cm"),
	I_OFFSET("I Offset", "IOffset"),
	TAU_SYN_E("Tau Syn E","TauSynE"),
	TAU_SYN_I("Tau Syn I","TauSynI"),
	V_INIT("V Init", "VInit"),
	DELTAT("Delta T", "DeltaT"),
	V_SPIKE("V Spike", "VSpike"),
	V_OFFSET("V Offset", "VOffset"),
	E_REV_E("E Rev E", "ERevE"),
	E_REV_I("E Rev I", "ERevI"),
	E_REV_K("E Rev K", "ERevK"),
	E_REV_NA("E Rev I", "ERevNa"),
	E_REV_LEAK("E Rev I", "ERevLEAK"),
	G_LEAK("G Leak","GLeak"),
	G_BARK("G Bark", "GBark"),
	G_BAR_NA("G Bar Na", "GBarNa"),
	NUMBER_CHANNEL("Number of Channels", "NumberChannels"),
	VARIABLE_PARAMETER("Variable Parameter", "VariableParameter"),
	INHOMOGENEOUS_VALUE("Inhomogeneous Value", "InhomogeneousValue"),
	VALUE("Value", "Value"),
	CHANNEL_DENSITY_NERNST("Channel Density Nernst", "ChannelDensityNernst"),
	CHANNEL_DENSITY_GHK("Channel Density GHK", "ChannelDensityGHK"),
	PERMEABILITY("Permeability", "Permeability"),
	CHANNEL_DENSITY_NON_UNIFORM("Channel Density Non Uniform", "ChannelDensityNonUniform"),
	CHANNEL_DENSITY_NON_UNIFORM_NERNST("Channel Density Non Uniform Nernst", "ChannelDensityNonUniformNernst"),
	CONNECTION_ID("Connection Id", "ConnectionId"),
	PROJECTION_ID("Projection Id", "ProjectionId");
	
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
}
