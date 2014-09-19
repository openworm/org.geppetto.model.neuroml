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
package org.geppetto.model.neuroml.visitors;

import java.util.List;

import org.geppetto.core.model.runtime.EntityNode;
import org.geppetto.core.model.runtime.VariableNode;
import org.geppetto.core.model.state.visitors.DefaultStateVisitor;

/**
 * @author matteocantarelli
 *
 */
public class AddStatesToSceneVisitor extends DefaultStateVisitor
{
	private List<EntityNode> _entities;

	public AddStatesToSceneVisitor(List<EntityNode> entities)
	{
		_entities=entities;
	}

	@Override
	public boolean visitVariableNode(VariableNode node)
	{
		//TODO This is just a hacked implementation just to try, it's mapping all the states to the first entity
		//because we know there's only one. The real implementation will have to associate the different states
		//to the pertinent entities we are streaming to the frontend.
		String value=node.consumeFirstValue().getValue().getStringValue();
		_entities.get(0).getMetadata().setAdditionalProperties(node.getInstancePath(), value);
		if(node.getName().equals("v"))
		{
			System.out.println("V:"+value);
		}
		return super.visitVariableNode(node);
	}

}
