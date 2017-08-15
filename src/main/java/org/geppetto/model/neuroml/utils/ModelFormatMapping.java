
package org.geppetto.model.neuroml.utils;


/**
 * @author Adrian Quintana (adrian.perez@ucl.ac.uk)
 *
 */
public enum ModelFormatMapping 
{

	NEUROML("NeuroML"),
	LEMS("LEMS"),

	//LEMS
	C("C"),
	DLEMS("DLEMS"),
	MATLAB("MATLAB"),
	MODELICA("MODELICA"),
	SEDML("SED-ML"),
	//NEUROML
	BRIAN("BRIAN"),
	CELLML("CELLML"),
	DN_SIM("DN_SIM"),
	GRAPH_VIZ("GraphViz"),
	NEST("NEST"),
	NEURON("NEURON"),
	NETPYNE("NETPYNE"),
	PYNN("PyNN"),
	SBML("SBML"),
	SVG("SVG"),
	XINEML("XINEML"),
	XPP("XPP"),
	JNEUROML("jNeuroML");
	
	
	private String _value;
	
	private ModelFormatMapping(String value)
	{
		_value = value;
	}
	
	public String getExportValue()
	{
		return _value;
	}
	
	public static ModelFormatMapping fromExportValue(String format) {
	    if (format != null) {
	      for (ModelFormatMapping mf : ModelFormatMapping.values()) {
	        if (format.equalsIgnoreCase(mf.getExportValue())) {
	          return mf;
	        }
	      }
	    }
	    return null;
	  }
	
}
