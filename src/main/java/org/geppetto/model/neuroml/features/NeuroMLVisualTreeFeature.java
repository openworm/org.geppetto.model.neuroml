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
package org.geppetto.model.neuroml.features;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.features.IVisualTreeFeature;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.runtime.ANode;
import org.geppetto.core.model.runtime.AspectNode;
import org.geppetto.core.model.runtime.AspectSubTreeNode;
import org.geppetto.core.model.runtime.EntityNode;
import org.geppetto.core.model.runtime.AspectSubTreeNode.AspectTreeType;
import org.geppetto.core.services.GeppettoFeature;
import org.geppetto.model.neuroml.visitors.PopulateVisualTreeVisitor;
import org.lemsml.jlems.api.interfaces.ILEMSDocument;
import org.neuroml.model.NeuroMLDocument;

/**
 * Abstract feature class for populating neuroml visual tree
 * 
 * @author Jesus R Martinez (jesus@metacell.us)
 *
 */
public class NeuroMLVisualTreeFeature implements IVisualTreeFeature{

	private static Log _logger = LogFactory.getLog(NeuroMLVisualTreeFeature.class);

	private Map<String, List<ANode>> _visualizationNodes = null;
	
	private PopulateVisualTreeVisitor _populateVisualTree = new PopulateVisualTreeVisitor();
	private GeppettoFeature type = GeppettoFeature.VISUAL_TREE_FEATURE;
	private NeuroMLDocument neuroMLDocument;

	public NeuroMLVisualTreeFeature(NeuroMLDocument document, ILEMSDocument lemsDocument) {
		_visualizationNodes = new HashMap<String, List<ANode>>();
		neuroMLDocument = document;
	}

	@Override
	public GeppettoFeature getType() {
		return type ;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.geppetto.core.simulator.ISimulator#populateVisualTree(org.geppetto
	 * .core.model.runtime.AspectNode)
	 */
	@Override
	public boolean populateVisualTree(AspectNode aspectNode) throws ModelInterpreterException {

		long start=System.currentTimeMillis();
		AspectSubTreeNode visualizationTree = (AspectSubTreeNode) aspectNode.getSubTree(AspectTreeType.VISUALIZATION_TREE);

		try {
			if (neuroMLDocument != null) {
				_populateVisualTree.createNodesFromNeuroMLDocument(visualizationTree, neuroMLDocument, null, _visualizationNodes);
				//If a cell is not part of a network or there is not a target component, add it to to the visualizationtree
				for (List<ANode> visualizationNodesItem : _visualizationNodes.values()){
					visualizationTree.addChildren(visualizationNodesItem);
				}
				visualizationTree.setModified(true);
				aspectNode.setModified(true);
				((EntityNode) aspectNode.getParentEntity()).updateParentEntitiesFlags(true);
			}
		} catch (Exception e) {
			throw new ModelInterpreterException(e);
		}
		_logger.info("Populate visual tree completed, took "+(System.currentTimeMillis()-start)+"ms");
		return true;
	}
}
