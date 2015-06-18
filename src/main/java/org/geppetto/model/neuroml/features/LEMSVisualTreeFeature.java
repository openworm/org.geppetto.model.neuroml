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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geppetto.core.features.IVisualTreeFeature;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.runtime.ANode;
import org.geppetto.core.model.runtime.AspectNode;
import org.geppetto.core.model.runtime.AspectSubTreeNode;
import org.geppetto.core.model.runtime.EntityNode;
import org.geppetto.core.model.runtime.AspectSubTreeNode.AspectTreeType;
import org.geppetto.core.services.GeppettoFeature;
import org.geppetto.model.neuroml.visitors.PopulateVisualTreeVisitor;
import org.lemsml.jlems.api.LEMSBuildConfiguration;
import org.lemsml.jlems.api.LEMSBuildException;
import org.lemsml.jlems.api.LEMSBuildOptions;
import org.lemsml.jlems.api.LEMSBuildOptionsEnum;
import org.lemsml.jlems.api.LEMSBuilder;
import org.lemsml.jlems.api.LEMSDocumentReader;
import org.lemsml.jlems.api.interfaces.ILEMSBuildConfiguration;
import org.lemsml.jlems.api.interfaces.ILEMSBuildOptions;
import org.lemsml.jlems.api.interfaces.ILEMSBuilder;
import org.lemsml.jlems.api.interfaces.ILEMSDocument;
import org.lemsml.jlems.core.expression.ParseError;
import org.lemsml.jlems.core.sim.ContentError;
import org.lemsml.jlems.core.type.Component;
import org.lemsml.jlems.core.type.Lems;
import org.neuroml.model.NeuroMLDocument;

/**
 * Populates visual tree for an aspect, given a NeuroMLDocument object
 * to extract visualization objects from.
 * 
 * @author Jesus R Martinez (jesus@metacell.us)
 *
 */
public class LEMSVisualTreeFeature implements IVisualTreeFeature{

	private List<String> _targetCells = null;
	private Map<String, List<ANode>> _visualizationNodes = null;
	
	private PopulateVisualTreeVisitor _populateVisualTree = new PopulateVisualTreeVisitor();
	private GeppettoFeature type = GeppettoFeature.VISUAL_TREE_FEATURE;
	private NeuroMLDocument _neuroMLDocument;

	public LEMSVisualTreeFeature(NeuroMLDocument neuroMLDocument, ILEMSDocument document) throws ModelInterpreterException {
		try {
			_visualizationNodes = new HashMap<String, List<ANode>>();
			_neuroMLDocument = neuroMLDocument;
			ILEMSBuilder builder = new LEMSBuilder();

			builder.addDocument(document);

			ILEMSBuildOptions options = new LEMSBuildOptions();
			options.addBuildOption(LEMSBuildOptionsEnum.FLATTEN);

			ILEMSBuildConfiguration config = new LEMSBuildConfiguration();
			builder.build(config, options); // pre-build to read the run
			Lems lems = (Lems) document;
			
			_targetCells = null;
			if (lems.getTargets().size() > 0){
				String targetComponent = LEMSDocumentReader.getTarget(document);
				if(targetComponent != null)
				{
					_targetCells = new ArrayList<String>();
					for(Component population : lems.getComponent(targetComponent).getChildrenAL("populations"))
					{
						_targetCells.add(population.getAttributes().getByName("component").getValue());
					}
				}
			}
		} catch (ContentError | ParseError e) {
			throw new ModelInterpreterException(e);
		} catch (LEMSBuildException e) {
			throw new ModelInterpreterException(e);
		}

	}

	@Override
	public GeppettoFeature getType() {
		return type ;
	}

	/**
	 * @param neuroml
	 * @param visualizationTree
	 * @param aspectNode
	 * @param targetComponents
	 */
	private void process(NeuroMLDocument neuroml, AspectSubTreeNode visualizationTree, AspectNode aspectNode)
	{
		_populateVisualTree.createNodesFromNeuroMLDocument(visualizationTree, neuroml, _targetCells, _visualizationNodes);
		visualizationTree.setModified(true);
		aspectNode.setModified(true);
		((EntityNode) aspectNode.getParentEntity()).updateParentEntitiesFlags(true);

	}
	
	@Override
	public boolean populateVisualTree(AspectNode aspectNode) throws ModelInterpreterException {
		AspectSubTreeNode visualizationTree = (AspectSubTreeNode) aspectNode.getSubTree(AspectTreeType.VISUALIZATION_TREE);
		try
		{
			process(_neuroMLDocument, visualizationTree, aspectNode);

			//If a cell is not part of a network or there is not a target component, add it to to the visualizationtree
			if (_targetCells == null){
				for (List<ANode> visualizationNodesItem : _visualizationNodes.values()){
					visualizationTree.addChildren(visualizationNodesItem);
				}
			}
			else if(_targetCells != null && _targetCells.size() > 0)
			{
				for(Map.Entry<String, List<ANode>> entry : _visualizationNodes.entrySet())
				{
					if(_targetCells.contains(entry.getKey()))
					{
						visualizationTree.addChildren(entry.getValue());
					}
				}
			}

		}
		catch(Exception e)
		{
			throw new ModelInterpreterException(e);
		}

		return true;
	}

}
