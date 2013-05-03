package org.geppetto.model.neuroml.services;

/**
 * Class to hold resources used in the visualiser. This elements will be displayed to the user.
 * @author matteocantarelli
 *
 */
public enum Resources
{
	COND_DENSITY("Passive conductance density"),
	SPIKE_THRESHOLD("Spike Threshold"),
	SPECIFIC_CAPACITANCE("Specific Capacitance"),
	INIT_MEMBRANE_POTENTIAL("Initial Membrane Potential"),
	RESISTIVITY("Resistivity"),
	MEMBRANE_P("Membrane Properties (Soma)"),
	INTRACELLULAR_P("Intracellular Properties (Soma)"), 
	SYNAPSE("Synapse Type"), 
	CONNECTION_TYPE("Connection Type"),
	PRE_SYNAPTIC("Input"),
	POST_SYNAPTIC("Output");
	
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
