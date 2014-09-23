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
	COND_DENSITY("Passive conductance density"),
	SPIKE_THRESHOLD("Spike Threshold"),
	EREV("Reverse Potential"),
	SPECIFIC_CAPACITANCE("Specific Capacitance"),
	INIT_MEMBRANE_POTENTIAL("Initial Membrane Potential"),
	RESISTIVITY("Resistivity"),
	MEMBRANE_P("Membrane Properties"),
	MEMBRANE_P_SOMA("Membrane Properties (Soma)"),
	INTRACELLULAR_P("Intracellular Properties"),
	INTRACELLULAR_P_SOMA("Intracellular Properties (Soma)"), 
	SYNAPSE("Synapse Type"), 
	CONNECTION_TYPE("Connection Type"),
	PRE_SYNAPTIC("Input"),
	POST_SYNAPTIC("Output"),
	ION("Ion"),
	ION_CHANNEL("Ion Channel"),
	CHANNEL_DENSITY("Channel Density"),
	SPECIES("Species"),
	INIT_CONCENTRATION("Initial Concentration"),
	INIT_EXT_CONCENTRATION("Initial External Concentration"),
	CONCENTRATION_MODEL("Concentration Model"),
	BIOPHYSICAL_PROPERTIES("Biophysical Properties"),
	ANOTATION("Anotation"),
	FW_RATE_FN("Forward Rate Dynamics"),
	MIDPOINT("Midpoint"),
	RATE("Rate"),
	SCALE("Scale"),
	GATE("Gate");
	
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
