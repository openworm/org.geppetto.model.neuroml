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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.model.runtime.ACompositeNode;
import org.geppetto.core.model.runtime.ANode;
import org.geppetto.core.model.runtime.AspectSubTreeNode;
import org.geppetto.core.model.runtime.CompositeNode;
import org.geppetto.core.model.runtime.EntityNode;
import org.geppetto.core.model.runtime.TextMetadataNode;
import org.geppetto.core.model.values.IntValue;
import org.geppetto.core.model.values.StringValue;
import org.lemsml.jlems.core.sim.ContentError;
import org.neuroml.model.AdExIaFCell;
import org.neuroml.model.AlphaCondSynapse;
import org.neuroml.model.AlphaCurrSynapse;
import org.neuroml.model.BaseCell;
import org.neuroml.model.BiophysicalProperties;
import org.neuroml.model.BlockingPlasticSynapse;
import org.neuroml.model.Cell;
import org.neuroml.model.DecayingPoolConcentrationModel;
import org.neuroml.model.ExpCondSynapse;
import org.neuroml.model.ExpCurrSynapse;
import org.neuroml.model.ExpOneSynapse;
import org.neuroml.model.ExpTwoSynapse;
import org.neuroml.model.ExplicitInput;
import org.neuroml.model.ExtracellularProperties;
import org.neuroml.model.FitzHughNagumoCell;
import org.neuroml.model.FixedFactorConcentrationModel;
import org.neuroml.model.IafCell;
import org.neuroml.model.IafRefCell;
import org.neuroml.model.IafTauCell;
import org.neuroml.model.IafTauRefCell;
import org.neuroml.model.InputList;
import org.neuroml.model.Instance;
import org.neuroml.model.IntracellularProperties;
import org.neuroml.model.IonChannel;
import org.neuroml.model.IonChannelHH;
import org.neuroml.model.IzhikevichCell;
import org.neuroml.model.Network;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.Population;
import org.neuroml.model.Projection;
import org.neuroml.model.Region;
import org.neuroml.model.SynapticConnection;

/**
 * Populates the Model Tree of Aspect
 * 
 * @author  Jesus R. Martinez (jesus@metacell.us)
 * @author Adrian Quintana (adrian.perez@ucl.ac.uk)
 * 
 */
public class PopulateModelTree {

	private boolean _populated = false;
	
	private NeuroMLAccessUtility neuroMLAccessUtility = new NeuroMLAccessUtility();
	
	private PopulateNeuroMLModelTreeUtils populateNeuroMLModelTreeUtils = new PopulateNeuroMLModelTreeUtils();
	
	public PopulateModelTree() {		
	}
	
	/**
	 * Method that is contacted to start populating the model tree
	 * 
	 * @param modelTree - Model tree that is to be populated
	 * @param neuroml - NeuroMLDocument used to populate the tree, values are in here
	 * @return 
	 * @throws ModelInterpreterException 
	 */
	public boolean populateModelTree(AspectSubTreeNode modelTree, ModelWrapper model) throws ModelInterpreterException
	{		
		NeuroMLDocument neuroml = (NeuroMLDocument) ((ModelWrapper) model).getModel(NeuroMLAccessUtility.NEUROML_ID);
		
		Map<String, EntityNode> mapping = (Map<String, EntityNode>) ((ModelWrapper) model).getModel(NeuroMLAccessUtility.SUBENTITIES_MAPPING_ID);
		Map<String, ANode> discoveredNodesInNeuroML = new HashMap<String, ANode>();
		
		populateNeuroMLModelTreeUtils.setModel((ModelWrapper) model);
		
		try {
			EntityNode entityNode = mapping.get(modelTree.getParent().getParent().getId());
			//If it is not a subentity we will go first for the networks
			if (entityNode == null){

				//TODO: Shall we go through all the stand alone element or check the lem component?
				
				/**
		 		 * NETWORKS
		 		 */
				for(Network n : neuroml.getNetwork()){
					CompositeNode compositeNode = populateNeuroMLModelTreeUtils.createNetworkNode(n);
					discoveredNodesInNeuroML.put(compositeNode.getId(), compositeNode);
				}
				
				/**
		 		 * CELLS
		 		 */
				for (CompositeNode compositeNode : getCells(model)){
	 				discoveredNodesInNeuroML.put(compositeNode.getId(), compositeNode);
				}
				
				/**
		 		 * CHANNELS
		 		 */
		 		for (IonChannel ionChannel : neuroml.getIonChannel()){
		 			CompositeNode compositeNode = populateNeuroMLModelTreeUtils.createChannelNode(ionChannel);
		 			discoveredNodesInNeuroML.put(compositeNode.getId(), compositeNode);
		 		}
		 		for (IonChannelHH ionChannelHH : neuroml.getIonChannelHH()){
		 			CompositeNode compositeNode = populateNeuroMLModelTreeUtils.createChannelNode(ionChannelHH);
		 			discoveredNodesInNeuroML.put(compositeNode.getId(), compositeNode);
		 		}
		 		
		 		/**
		 		 * Extracellular Properties
		 		 */
		 		for (ExtracellularProperties extracellularProperties : neuroml.getExtracellularProperties()){
		 			CompositeNode compositeNode = populateNeuroMLModelTreeUtils.createExtracellularPropertiesNode(extracellularProperties);
		 			discoveredNodesInNeuroML.put(compositeNode.getId(), compositeNode);
		 		}
		 		
		 		/**
		 		 * Intracellular Properties
		 		 */
		 		for (IntracellularProperties intracellularProperties : neuroml.getIntracellularProperties()){
		 			CompositeNode compositeNode = populateNeuroMLModelTreeUtils.createIntracellularPropertiesNode(intracellularProperties);
		 			discoveredNodesInNeuroML.put(compositeNode.getId(), compositeNode);
		 		}
		 		
		 		/**
		 		 * Concentration Model
		 		 */
		 		for (DecayingPoolConcentrationModel decayingPoolConcentrationModel : neuroml.getDecayingPoolConcentrationModel()){
		 			CompositeNode compositeNode = populateNeuroMLModelTreeUtils.createConcentrationModelNode(decayingPoolConcentrationModel);
		 			discoveredNodesInNeuroML.put(compositeNode.getId(), compositeNode);
		 		}
		 		for (FixedFactorConcentrationModel fixedFactorConcentrationModel : neuroml.getFixedFactorConcentrationModel()){
		 			CompositeNode compositeNode = populateNeuroMLModelTreeUtils.createConcentrationModelNode(fixedFactorConcentrationModel);
		 			discoveredNodesInNeuroML.put(compositeNode.getId(), compositeNode);
		 		}
		 		
		 		/**
		 		 * Synapses Types
		 		 */
		 		for (ExpTwoSynapse expTwoSynapse : neuroml.getExpTwoSynapse()){
		 			CompositeNode compositeNode = populateNeuroMLModelTreeUtils.createSynapseNode(expTwoSynapse);
		 			discoveredNodesInNeuroML.put(compositeNode.getId(), compositeNode);
		 			
		 		}
		 		for (ExpOneSynapse expOneSynapse : neuroml.getExpOneSynapse()){
		 			CompositeNode compositeNode = populateNeuroMLModelTreeUtils.createSynapseNode(expOneSynapse);
		 			discoveredNodesInNeuroML.put(compositeNode.getId(), compositeNode);
		 			
		 		}
		 		for (BlockingPlasticSynapse blockingPlasticSynapse : neuroml.getBlockingPlasticSynapse()){
		 			CompositeNode compositeNode = populateNeuroMLModelTreeUtils.createSynapseNode(blockingPlasticSynapse);
		 			discoveredNodesInNeuroML.put(compositeNode.getId(), compositeNode);
		 			
		 		}
		 		
		 		/**
		 		 * PyNN Synapses Types
		 		 */
		 		for (AlphaCondSynapse alphaCondSynapse : neuroml.getAlphaCondSynapse()){
		 			CompositeNode compositeNode = populateNeuroMLModelTreeUtils.createPynnSynapseNode(alphaCondSynapse);
		 			discoveredNodesInNeuroML.put(compositeNode.getId(), compositeNode);
		 			
		 		}
		 		for (ExpCondSynapse expCondSynapse : neuroml.getExpCondSynapse()){
		 			CompositeNode compositeNode = populateNeuroMLModelTreeUtils.createPynnSynapseNode(expCondSynapse);
		 			discoveredNodesInNeuroML.put(compositeNode.getId(), compositeNode);
		 			
		 		}
		 		for (ExpCurrSynapse expCurrSynapse : neuroml.getExpCurrSynapse()){
		 			CompositeNode compositeNode = populateNeuroMLModelTreeUtils.createPynnSynapseNode(expCurrSynapse);
		 			discoveredNodesInNeuroML.put(compositeNode.getId(), compositeNode);
		 			
		 		}
		 		for (AlphaCurrSynapse alphaCurrSynapse : neuroml.getAlphaCurrSynapse()){
		 			CompositeNode compositeNode = populateNeuroMLModelTreeUtils.createPynnSynapseNode(alphaCurrSynapse);
		 			discoveredNodesInNeuroML.put(compositeNode.getId(), compositeNode);
		 			
		 		}
		 		
		 		/**
		 		 * Biophysical Properties
		 		 */
		 		for (BiophysicalProperties biophysicalProperties : neuroml.getBiophysicalProperties()){
		 			CompositeNode compositeNode = populateNeuroMLModelTreeUtils.createBiophysicalPropertiesNode(biophysicalProperties);
		 			discoveredNodesInNeuroML.put(compositeNode.getId(), compositeNode);
		 		}
		 		
		 		
		 		
			}
			else{
//				int endIndex = entityNode.getId().lastIndexOf("_");
//			    if (endIndex != -1)  
//			    {
//			        String newstr = entityNode.getId().substring(0, endIndex);
//			    }
				
				//neuroMLAccessUtility.getComponent(componentId, model, Resources.CELL);
				for (CompositeNode compositeNode : getCells(model)){
	 				discoveredNodesInNeuroML.put(compositeNode.getId(), compositeNode);
				}
			}
			
			
		 		for (Map.Entry<String, ANode> entry : discoveredNodesInNeuroML.entrySet()) {
		 		    String key = entry.getKey();
		 		   if (discoveredNodesInNeuroML.size() == 1){
		 			  Object node = discoveredNodesInNeuroML.values().toArray()[0];
		 			   if (node instanceof ACompositeNode){
		 				  modelTree.addChildren(((ACompositeNode)node).getChildren());
		 				  break;
		 			   }
					}
		 		    modelTree.addChild(entry.getValue());
		 		}
	 		
	 		
	 		_populated = true;
		} catch (Exception e) {
			_populated = false;
			throw new ModelInterpreterException(e);
		}
 		
 		return _populated;
	}
	
	public List<CompositeNode> getCells(ModelWrapper model) throws ModelInterpreterException, ContentError{
		NeuroMLDocument neuroml = (NeuroMLDocument) ((ModelWrapper) model).getModel(NeuroMLAccessUtility.NEUROML_ID);
		
		List<CompositeNode> cellNodes = new ArrayList<CompositeNode>();
		
		/**
		 * CELLS
		 */
 		List<Cell> cells = neuroml.getCell();
 		List<AdExIaFCell> adExIaFCells = neuroml.getAdExIaFCell();
 		List<IafCell> iaFCells = neuroml.getIafCell();
 		List<IafRefCell> iafRefCells = neuroml.getIafRefCell();
 		List<IafTauRefCell> iafTauRefCells = neuroml.getIafTauRefCell();
 		List<IafTauCell> iafTauCells = neuroml.getIafTauCell();
 		List<FitzHughNagumoCell> fitzHughNagumoCells = neuroml.getFitzHughNagumoCell();
 		List<IzhikevichCell> izhikevichCells = neuroml.getIzhikevichCell();
 		
 		for(Cell c : cells){
 			cellNodes.add(populateNeuroMLModelTreeUtils.createCellNode(c));
 		}
 		for(AdExIaFCell c : adExIaFCells){
 			cellNodes.add(populateNeuroMLModelTreeUtils.createCellNode(c));
 		}
 		for(FitzHughNagumoCell c : fitzHughNagumoCells){
 			cellNodes.add(populateNeuroMLModelTreeUtils.createCellNode(c));
 		}
 		for(IzhikevichCell c : izhikevichCells){
 			cellNodes.add(populateNeuroMLModelTreeUtils.createCellNode(c));
 		}
 		for(IafRefCell c : iafRefCells){
 			cellNodes.add(populateNeuroMLModelTreeUtils.createCellNode(c));
 		}
 		for(IafCell c : iaFCells){
 			cellNodes.add(populateNeuroMLModelTreeUtils.createCellNode(c));
 		}
 		for(IafTauRefCell c : iafTauRefCells){
 			cellNodes.add(populateNeuroMLModelTreeUtils.createCellNode(c));
 		}
 		for(IafTauCell c : iafTauCells){
 			cellNodes.add(populateNeuroMLModelTreeUtils.createCellNode(c));
 		}
 		
 		return cellNodes;
	}
	
	
}
