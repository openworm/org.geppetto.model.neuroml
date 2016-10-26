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
	NETPYNE("NetPyNE"),
	PYNN("PyNN"),
	SBML("SBML"),
	SVG("SVG"),
	XINEML("XINEML"),
	XPP("XPP");
	
	
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
