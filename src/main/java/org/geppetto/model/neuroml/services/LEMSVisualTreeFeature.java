package org.geppetto.model.neuroml.services;

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
import org.lemsml.jlems.api.LEMSDocumentReader;
import org.lemsml.jlems.api.interfaces.ILEMSDocument;
import org.lemsml.jlems.core.expression.ParseError;
import org.lemsml.jlems.core.sim.ContentError;
import org.lemsml.jlems.core.type.Component;
import org.lemsml.jlems.core.type.Lems;
import org.neuroml.model.NeuroMLDocument;

public class LEMSVisualTreeFeature implements IVisualTreeFeature{

	private List<String> _targetCells = null;
	private Map<String, List<ANode>> _visualizationNodes = null;
	
	private PopulateVisualTreeVisitor _populateVisualTree = new PopulateVisualTreeVisitor();
	private GeppettoFeature type = GeppettoFeature.VISUAL_TREE_FEATURE;
	private NeuroMLDocument _neuroMLDocument;

	public LEMSVisualTreeFeature(NeuroMLDocument neuroMLDocument, ILEMSDocument document) {
		_visualizationNodes = new HashMap<String, List<ANode>>();
		_neuroMLDocument = neuroMLDocument;
		Lems lems = (Lems) document;
		String targetComponent;
		try {
			targetComponent = LEMSDocumentReader.getTarget(document);
			if(targetComponent != null)
			{
				_targetCells = new ArrayList<String>();
				for(Component population : lems.getComponent(targetComponent).getChildrenAL("populations"))
				{
					_targetCells.add(population.getAttributes().getByName("component").getValue());
				}
			}
			else
			{
				_targetCells = null;
			}
		} catch (ContentError | ParseError e) {
			e.printStackTrace();
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
