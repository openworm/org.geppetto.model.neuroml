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
	COND_DENSITY("Passiveconductancedensity"),
	SPIKE_THRESHOLD("SpikeThreshold"),
	EREV("ReversePotential"),
	SPECIFIC_CAPACITANCE("SpecificCapacitance"),
	INIT_MEMBRANE_POTENTIAL("InitialMembranePotential"),
	RESISTIVITY("Resistivity"),
	MEMBRANE_P("MembraneProperties"),
	MEMBRANE_P_SOMA("MembraneProperties(Soma)"),
	INTRACELLULAR_P("IntracellularProperties"),
	INTRACELLULAR_P_SOMA("IntracellularProperties(Soma)"), 
	SYNAPSE("SynapseType"), 
	CONNECTION_TYPE("ConnectionType"),
	PRE_SYNAPTIC("Input"),
	POST_SYNAPTIC("Output"),
	ION("Ion"),
	ION_CHANNEL("IonChannel"),
	CHANNEL_DENSITY("ChannelDensity"),
	SPECIES("Species"),
	INIT_CONCENTRATION("InitialConcentration"),
	INIT_EXT_CONCENTRATION("InitialExternalConcentration"),
	CONCENTRATION_MODEL("ConcentrationModel"),
	BIOPHYSICAL_PROPERTIES("BiophysicalProperties"),
	ANOTATION("Anotation"),
	FW_RATE("ForwardRate"),
	FW_RATE_FN("ForwardRateDynamics"),
	BW_RATE("BackwardRate"),
	BW_RATE_FN("BackwardRateDynamics"),
	MIDPOINT("Midpoint"),
	RATE("Rate"),
	SCALE("Scale"),
	GATE("Gate"),
	TIMECOURSE("TimeCourse"),
	TAU("Tau"),
	STEADY_STATE("SteadyState");
	
	private String _value;
	
	private Resources(String value)
	{
		_value=value;
	}
	
	public String get()
	{
		return _value;
	}
}
