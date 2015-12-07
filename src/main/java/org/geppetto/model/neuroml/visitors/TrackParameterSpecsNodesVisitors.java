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
package org.geppetto.model.neuroml.visitors;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.model.VariableValue;
import org.geppetto.model.types.Type;
import org.geppetto.model.values.PointerElement;
import org.geppetto.model.values.Value;
import org.geppetto.model.variables.Variable;
import org.geppetto.model.variables.util.VariablesSwitch;
import org.lemsml.jlems.core.type.Component;

/**
 * Visitor used tracking parameter specification nodes inside model tree.
 * 
 * @author Jesus R. Martinez (jesus@metacell.us)
 * @author Adrian Quintana (adrian.perez@ucl.ac.uk)
 *
 */
public class TrackParameterSpecsNodesVisitors extends VariablesSwitch<Object>
{

	private static Log _logger = LogFactory.getLog(TrackParameterSpecsNodesVisitors.class);

	// private Map<String, ParameterSpecificationNode> _parameters = new HashMap<String,ParameterSpecificationNode>();

	private Component component;

	private VariableValue variableValue;

	public TrackParameterSpecsNodesVisitors(VariableValue variableValue)
	{
		this.variableValue = variableValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geppetto.core.model.state.visitors.DefaultStateVisitor#inCompositeStateNode(org.geppetto.core.model.state.CompositeStateNode)
	 */
	// @Override
	// public boolean visitParameterSpecificationNode(ParameterSpecificationNode node)
	// {
	// this._parameters.put(node.getInstancePath(),node);
	// return true;
	// }
	//
	// public Map<String, ParameterSpecificationNode> getParametersMap() {
	// return this._parameters;
	// }

	@Override
	public Object caseVariable(Variable object)
	{
		for(Map.Entry<Type, Value> entry : object.getInitialValues().entrySet())
		{
			if(entry.getValue().equals(variableValue))
			{
				PointerElement pointerElementParent = variableValue.getPointer().getElements().get(variableValue.getPointer().getElements().size() - 2);

				for(Type pointerElementType : pointerElementParent.getVariable().getTypes())
				{
					if(pointerElementType.getId().equals(entry.getKey().getId()))
					{
						component = (Component) pointerElementType.getDomainModel();
					}
				}
			}
		}

		// TODO Auto-generated method stub
		return super.caseVariable(object);
	}

	public Component getComponent()
	{
		return component;
	}

}
