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
import java.util.List;
import java.util.Map;

import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.model.runtime.AspectSubTreeNode;
import org.geppetto.core.model.runtime.CompositeNode;
import org.geppetto.core.model.runtime.EntityNode;
import org.geppetto.core.model.runtime.TextMetadataNode;
import org.geppetto.core.model.values.IntValue;
import org.geppetto.core.model.values.StringValue;
import org.lemsml.jlems.core.sim.ContentError;
import org.neuroml.model.AdExIaFCell;
import org.neuroml.model.BaseCell;
import org.neuroml.model.Cell;
import org.neuroml.model.DecayingPoolConcentrationModel;
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
	
//	private URL url;
	
//	private NeuroMLDocument neuroml;
	
	private NeuroMLAccessUtility neuroMLAccessUtility = new NeuroMLAccessUtility();
	
	private PopulateModelTreeUtils populateModelTreeUtils = new PopulateModelTreeUtils();
	
	
	
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
		
		try {
			EntityNode entityNode = mapping.get(modelTree.getParent().getParent());
			//If it is not a subentity we will go first for the networks
			if (entityNode == null){
				/**
		 		 * NETWORK
		 		 */
				if (neuroml.getNetwork() == null){
		 			getNetworks(modelTree, model);
				}
				else{
					List<CompositeNode> cellNodes = getCells(model);
					for (CompositeNode cellNode : cellNodes){
						modelTree.addChildren(cellNode.getChildren());
					}
				}
			}
			else{
				List<CompositeNode> cellNodes = getCells(model);
				for (CompositeNode cellNode : cellNodes){
					modelTree.addChildren(cellNode.getChildren());
				}
			}
	 		
	 		
	 		/**
	 		 * CHANNELS
	 		 */
	 		for (IonChannel ionChannel : neuroml.getIonChannel()){
	 			modelTree.addChild(populateModelTreeUtils.createChannelNode(ionChannel, neuroMLAccessUtility, model));
	 		}
	 		for (IonChannelHH ionChannelHH : neuroml.getIonChannelHH()){
	 			modelTree.addChild(populateModelTreeUtils.createChannelNode(ionChannelHH, neuroMLAccessUtility, model));
	 		}
	 		
	 		/**
	 		 * Extracellular Properties
	 		 */
	 		for (ExtracellularProperties extracellularProperties : neuroml.getExtracellularProperties()){
 				modelTree.addChild(populateModelTreeUtils.createExtracellularPropertiesNode(extracellularProperties, neuroMLAccessUtility, model));
	 		}
	 		
	 		/**
	 		 * Intracellular Properties
	 		 */
	 		for (IntracellularProperties intracellularProperties : neuroml.getIntracellularProperties()){
 				modelTree.addChild(populateModelTreeUtils.createIntracellularPropertiesNode(intracellularProperties, neuroMLAccessUtility, model));
	 		}
	 		
	 		/**
	 		 * Concentration Model
	 		 */
	 		for (DecayingPoolConcentrationModel decayingPoolConcentrationModel : neuroml.getDecayingPoolConcentrationModel()){
	 			modelTree.addChild(populateModelTreeUtils.createConcentrationModel(decayingPoolConcentrationModel));
	 		}
	 		for (FixedFactorConcentrationModel fixedFactorConcentrationModel : neuroml.getFixedFactorConcentrationModel()){
	 			modelTree.addChild(populateModelTreeUtils.createConcentrationModel(fixedFactorConcentrationModel));
	 		}
	 		
	 		_populated = true;
		} catch (Exception e) {
			_populated = false;
			throw new ModelInterpreterException(e);
		}
 		
 		return _populated;
	}
	
	public void getNetworks(AspectSubTreeNode modelTree, ModelWrapper model) throws ModelInterpreterException, ContentError{
		NeuroMLDocument neuroml = (NeuroMLDocument) ((ModelWrapper) model).getModel(NeuroMLAccessUtility.NEUROML_ID);
		
 		for(Network n : neuroml.getNetwork()){
 			modelTree.addChildren(populateModelTreeUtils.createStandaloneChildren(n));
 			
 			for(InputList i : n.getInputList()){
 				
 			}
 			
 			for(ExplicitInput e : n.getExplicitInput()){
 				
 			}
 			
 			for(Region r : n.getRegion()){
 				
 			}
 			
 			for(Projection p : n.getProjection()){
 				
 			}
 			
 			List<Population> populations = n.getPopulation();
			for(Population p : populations){
				modelTree.addChildren(populateModelTreeUtils.createStandaloneChildren(p));

				BaseCell baseCell = (BaseCell) neuroMLAccessUtility.getComponent(p.getComponent(), model, Resources.CELL);
				modelTree.addChild(populateModelTreeUtils.createCellNode(baseCell, neuroMLAccessUtility, model));
				
				modelTree.addChild(new TextMetadataNode(Resources.SIZE.get(), Resources.SIZE.getId(),  new IntValue(p.getSize().intValue())));
				
				if(p.getType() != null){
					modelTree.addChild(new TextMetadataNode(Resources.POPULATION_TYPE.get(), Resources.POPULATION_TYPE.getId(),  new StringValue(p.getType().value())));
				}
				
				//TODO: Just reading the number of instances and displaying as a text metadata node 				
				List<Instance> instanceList = p.getInstance();
				if (instanceList != null && instanceList.size() != 0){
					modelTree.addChild(new TextMetadataNode(Resources.INSTANCES.get(), Resources.INSTANCES.getId(),  new IntValue(instanceList.size())));
				}
			}
			
			List<SynapticConnection> synapticConnections = n.getSynapticConnection();
			for(SynapticConnection s : synapticConnections){
				
			}
 		}
		
		
		
		
			
			
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
 			cellNodes.add(populateModelTreeUtils.createCellNode(c, neuroMLAccessUtility, model));
 		}
 		for(AdExIaFCell c : adExIaFCells){
 			cellNodes.add(populateModelTreeUtils.createCellNode(c, neuroMLAccessUtility, model));
 		}
 		for(FitzHughNagumoCell c : fitzHughNagumoCells){
 			cellNodes.add(populateModelTreeUtils.createCellNode(c, neuroMLAccessUtility, model));
 		}
 		for(IzhikevichCell c : izhikevichCells){
 			cellNodes.add(populateModelTreeUtils.createCellNode(c, neuroMLAccessUtility, model));
 		}
 		for(IafRefCell c : iafRefCells){
 			cellNodes.add(populateModelTreeUtils.createCellNode(c, neuroMLAccessUtility, model));
 		}
 		for(IafCell c : iaFCells){
 			cellNodes.add(populateModelTreeUtils.createCellNode(c, neuroMLAccessUtility, model));
 		}
 		for(IafTauRefCell c : iafTauRefCells){
 			cellNodes.add(populateModelTreeUtils.createCellNode(c, neuroMLAccessUtility, model));
 		}
 		for(IafTauCell c : iafTauCells){
 			cellNodes.add(populateModelTreeUtils.createCellNode(c, neuroMLAccessUtility, model));
 		}
 		
 		return cellNodes;
	}
	
	
}
