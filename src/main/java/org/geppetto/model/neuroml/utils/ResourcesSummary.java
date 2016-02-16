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
public enum ResourcesSummary
{
	
	FORWARD_RATE ("forwardRate", "forward rate"),
	REVERSE_RATE ("reverseRate", "reverse rate"),
	STEADY_STATE("steadyState", "steady state"),
	TIME_CONSTANT("timeConstant", "time constant");
	
	
	private String _neuromlId;
	private String _summaryId;
	
	private ResourcesSummary(String neuromlId, String summaryId)
	{
		_neuromlId = neuromlId;
		_summaryId = summaryId;
	}
	
	public String getNeuromlId()
	{
		return _neuromlId;
	}
	
	public String getSummaryId()
	{
		return _summaryId;
	}
	
	public static ResourcesSummary getValueByValue(String value){
		for(ResourcesSummary e : ResourcesSummary.values()){
            if(value.trim().equals(e._summaryId)) return e;
        }
		//If we can't find a value, return the id
		return null;
	}
	
}
