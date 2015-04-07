package org.geppetto.model.neuroml.services;

import java.util.ArrayList;
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
import org.lemsml.jlems.api.LEMSDocumentReader;
import org.lemsml.jlems.api.interfaces.ILEMSDocument;
import org.lemsml.jlems.core.type.Component;
import org.lemsml.jlems.core.type.Lems;
import org.neuroml.model.NeuroMLDocument;

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
