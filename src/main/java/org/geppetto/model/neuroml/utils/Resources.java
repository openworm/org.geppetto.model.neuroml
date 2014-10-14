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
	EXTRACELLULAR_P("Extracellular Properties", "ExtracellularProperties");
	
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
