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
package org.geppetto.model.neuroml.utils.modeltree;

import java.math.BigInteger;
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
import org.geppetto.model.neuroml.utils.NeuroMLAccessUtility;
import org.geppetto.model.neuroml.utils.Resources;
import org.lemsml.jlems.core.sim.ContentError;
import org.lemsml.jlems.core.type.Component;
import org.lemsml.jlems.core.type.ComponentType;
import org.lemsml.jlems.core.type.Lems;
import org.lemsml.jlems.core.type.ParamValue;
import org.neuroml.export.info.InfoTreeCreator;
import org.neuroml.export.info.model.InfoNode;
import org.neuroml.export.info.model.PlotNode;
import org.neuroml.model.AdExIaFCell;
import org.neuroml.model.AlphaCondSynapse;
import org.neuroml.model.AlphaCurrSynapse;
import org.neuroml.model.Base;
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
	
//	private NeuroMLAccessUtility neuroMLAccessUtility = new NeuroMLAccessUtility();
	
	private PopulateNeuroMLModelTreeUtils populateNeuroMLModelTreeUtils = new PopulateNeuroMLModelTreeUtils();
	
	public PopulateModelTree() {		
	}
	
	private List<ANode> createInfoNode(InfoNode infoNode) throws ModelInterpreterException {
		List<ANode> summaryElementList = new ArrayList<ANode>();
//		for (Map.Entry<String, Object> entry : infoNode.getProperties().entrySet()) {
//			CompositeNode summaryElementNode = new CompositeNode(entry.getKey().replace(" ", ""), entry.getKey());
//		    Object value = entry.getValue();
		    for (Map.Entry<String, Object> properties : ((InfoNode)infoNode).getProperties().entrySet()) {
		    	
			    String keyProperties = properties.getKey();
			    Object valueProperties = properties.getValue();
			    
			    if (valueProperties instanceof String){
			    	summaryElementList.add(PopulateNodesModelTreeUtils.createTextMetadataNode(keyProperties, keyProperties.replace(" ", ""), new StringValue((String)valueProperties)));
			    }
			    else if (valueProperties instanceof BigInteger) {
			    	summaryElementList.add(PopulateNodesModelTreeUtils.createTextMetadataNode(keyProperties, keyProperties.replace(" ", ""), new StringValue(((BigInteger)valueProperties).toString())));
			    }
			    else if (valueProperties instanceof Integer) {
			    	summaryElementList.add(PopulateNodesModelTreeUtils.createTextMetadataNode(keyProperties, keyProperties.replace(" ", ""), new StringValue(Integer.toString((Integer)valueProperties))));
			    }
			    else if (valueProperties instanceof PlotNode) {
					
				}
			    else if (valueProperties instanceof InfoNode) {
			    	CompositeNode subSummaryElementNode = new CompositeNode(keyProperties.replace(" ", ""), keyProperties);
			    	subSummaryElementNode.addChildren(createInfoNode((InfoNode)valueProperties));
			    	summaryElementList.add(subSummaryElementNode);
				}
			    else{
			    	throw new ModelInterpreterException("Info Writer Node type not supported. Object: " + keyProperties + ". Java class" + valueProperties.getClass());
			    }
		    }
//		    summaryElementList.add(summaryElementNode);
//		}  
		return summaryElementList;
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
//		Lems lems = (Lems) ((ModelWrapper) model).getModel(NeuroMLAccessUtility.LEMS_ID);
		
		Map<String, EntityNode> mapping = (Map<String, EntityNode>) ((ModelWrapper) model).getModel(NeuroMLAccessUtility.SUBENTITIES_MAPPING_ID);
		
		List<String> _discoveredNestedComponentsId = ((ArrayList<String>)((ModelWrapper) model).getModel(NeuroMLAccessUtility.DISCOVERED_NESTED_COMPONENTS_ID));
		
		HashMap<String, Base> _discoveredComponents = ((HashMap<String, Base>)((ModelWrapper) model).getModel(NeuroMLAccessUtility.DISCOVERED_COMPONENTS));
		
		HashMap<String, ANode> _discoveredNodesInNeuroML =  new HashMap<String, ANode>();
		
		populateNeuroMLModelTreeUtils.setModel((ModelWrapper) model);
		
		try {
//			EntityNode entityNode = mapping.get(modelTree.getParent().getParent().getId());
//			if (entityNode == null){

				//TODO: Shall we go through all the stand alone element or check the lem component?
			
				InfoNode infoNode = InfoTreeCreator.createInfoTree(neuroml);
				if (infoNode != null){
					CompositeNode summaryNode = new CompositeNode(Resources.SUMMARY.getId(), Resources.SUMMARY.get());
					summaryNode.addChildren(createInfoNode(infoNode));
					_discoveredNodesInNeuroML.put(Resources.SUMMARY.getId(), summaryNode);
				}
			
				/**
		 		 * NETWORKS
		 		 */
				for(Network n : neuroml.getNetwork()){
					CompositeNode compositeNode = populateNeuroMLModelTreeUtils.createNetworkNode(n, modelTree);
					_discoveredComponents.put(n.getId(), n);
					_discoveredNodesInNeuroML.put(n.getId(), compositeNode);
				}
				
				/**
		 		 * CELLS
		 		 */
//				getCells(model);
		 		List<Cell> cells = neuroml.getCell();
		 		List<AdExIaFCell> adExIaFCells = neuroml.getAdExIaFCell();
		 		List<IafCell> iaFCells = neuroml.getIafCell();
		 		List<IafRefCell> iafRefCells = neuroml.getIafRefCell();
		 		List<IafTauRefCell> iafTauRefCells = neuroml.getIafTauRefCell();
		 		List<IafTauCell> iafTauCells = neuroml.getIafTauCell();
		 		List<FitzHughNagumoCell> fitzHughNagumoCells = neuroml.getFitzHughNagumoCell();
		 		List<IzhikevichCell> izhikevichCells = neuroml.getIzhikevichCell();
		 		
		 		for(Cell c : cells){
		 			_discoveredComponents.put(c.getId(), c);
		 			_discoveredNodesInNeuroML.put(c.getId(), populateNeuroMLModelTreeUtils.createCellNode(c));
		 		}
		 		for(AdExIaFCell c : adExIaFCells){
		 			_discoveredComponents.put(c.getId(), c);
		 			_discoveredNodesInNeuroML.put(c.getId(), populateNeuroMLModelTreeUtils.createCellNode(c));
		 		}
		 		for(FitzHughNagumoCell c : fitzHughNagumoCells){
		 			_discoveredComponents.put(c.getId(), c);
		 			_discoveredNodesInNeuroML.put(c.getId(), populateNeuroMLModelTreeUtils.createCellNode(c));
		 		}
		 		for(IzhikevichCell c : izhikevichCells){
		 			_discoveredComponents.put(c.getId(), c);
		 			_discoveredNodesInNeuroML.put(c.getId(), populateNeuroMLModelTreeUtils.createCellNode(c));
		 		}
		 		for(IafRefCell c : iafRefCells){
		 			_discoveredComponents.put(c.getId(), c);
		 			_discoveredNodesInNeuroML.put(c.getId(), populateNeuroMLModelTreeUtils.createCellNode(c));
		 		}
		 		for(IafCell c : iaFCells){
		 			_discoveredComponents.put(c.getId(), c);
		 			_discoveredNodesInNeuroML.put(c.getId(), populateNeuroMLModelTreeUtils.createCellNode(c));
		 		}
		 		for(IafTauRefCell c : iafTauRefCells){
		 			_discoveredComponents.put(c.getId(), c);
		 			_discoveredNodesInNeuroML.put(c.getId(), populateNeuroMLModelTreeUtils.createCellNode(c));
		 		}
		 		for(IafTauCell c : iafTauCells){
		 			_discoveredComponents.put(c.getId(), c);
		 			_discoveredNodesInNeuroML.put(c.getId(), populateNeuroMLModelTreeUtils.createCellNode(c));
		 		}
				
				/**
		 		 * CHANNELS
		 		 */
		 		for (IonChannel ionChannel : neuroml.getIonChannel()){
		 			CompositeNode compositeNode = populateNeuroMLModelTreeUtils.createChannelNode(ionChannel);
		 			_discoveredComponents.put(ionChannel.getId(), ionChannel);
		 			_discoveredNodesInNeuroML.put(ionChannel.getId(), compositeNode);
		 		}
		 		for (IonChannelHH ionChannelHH : neuroml.getIonChannelHH()){
		 			CompositeNode compositeNode = populateNeuroMLModelTreeUtils.createChannelNode(ionChannelHH);
		 			_discoveredComponents.put(ionChannelHH.getId(), ionChannelHH);
		 			_discoveredNodesInNeuroML.put(ionChannelHH.getId(), compositeNode);
		 		}
		 		
		 		/**
		 		 * Extracellular Properties
		 		 */
		 		for (ExtracellularProperties extracellularProperties : neuroml.getExtracellularProperties()){
		 			CompositeNode compositeNode = populateNeuroMLModelTreeUtils.createExtracellularPropertiesNode(extracellularProperties);
		 			_discoveredComponents.put(extracellularProperties.getId(), extracellularProperties);
		 			_discoveredNodesInNeuroML.put(extracellularProperties.getId(), compositeNode);
		 		}
		 		
		 		/**
		 		 * Intracellular Properties
		 		 */
		 		for (IntracellularProperties intracellularProperties : neuroml.getIntracellularProperties()){
//		 			_discoveredComponents.put(intracellularProperties.getId(), intracellularProperties);
		 			CompositeNode compositeNode = populateNeuroMLModelTreeUtils.createIntracellularPropertiesNode(intracellularProperties);
		 			_discoveredNodesInNeuroML.put(" ", compositeNode);
		 		}
		 		
		 		/**
		 		 * Concentration Model
		 		 */
		 		for (DecayingPoolConcentrationModel decayingPoolConcentrationModel : neuroml.getDecayingPoolConcentrationModel()){
		 			CompositeNode compositeNode = populateNeuroMLModelTreeUtils.createConcentrationModelNode(decayingPoolConcentrationModel);
		 			_discoveredComponents.put(decayingPoolConcentrationModel.getId(), decayingPoolConcentrationModel);
		 			_discoveredNodesInNeuroML.put(decayingPoolConcentrationModel.getId(), compositeNode);
		 		}
		 		for (FixedFactorConcentrationModel fixedFactorConcentrationModel : neuroml.getFixedFactorConcentrationModel()){
		 			CompositeNode compositeNode = populateNeuroMLModelTreeUtils.createConcentrationModelNode(fixedFactorConcentrationModel);
		 			_discoveredComponents.put(fixedFactorConcentrationModel.getId(), fixedFactorConcentrationModel);
		 			_discoveredNodesInNeuroML.put(fixedFactorConcentrationModel.getId(), compositeNode);
		 		}
		 		
		 		/**
		 		 * Synapses Types
		 		 */
		 		for (ExpTwoSynapse expTwoSynapse : neuroml.getExpTwoSynapse()){
		 			CompositeNode compositeNode = populateNeuroMLModelTreeUtils.createSynapseNode(expTwoSynapse);
		 			_discoveredComponents.put(expTwoSynapse.getId(), expTwoSynapse);
		 			_discoveredNodesInNeuroML.put(expTwoSynapse.getId(), compositeNode);
		 			
		 		}
		 		for (ExpOneSynapse expOneSynapse : neuroml.getExpOneSynapse()){
		 			CompositeNode compositeNode = populateNeuroMLModelTreeUtils.createSynapseNode(expOneSynapse);
		 			_discoveredComponents.put(expOneSynapse.getId(), expOneSynapse);
		 			_discoveredNodesInNeuroML.put(expOneSynapse.getId(), compositeNode);
		 			
		 		}
		 		for (BlockingPlasticSynapse blockingPlasticSynapse : neuroml.getBlockingPlasticSynapse()){
		 			CompositeNode compositeNode = populateNeuroMLModelTreeUtils.createSynapseNode(blockingPlasticSynapse);
		 			_discoveredComponents.put(blockingPlasticSynapse.getId(), blockingPlasticSynapse);
		 			_discoveredNodesInNeuroML.put(blockingPlasticSynapse.getId(), compositeNode);
		 			
		 		}
		 		
		 		/**
		 		 * PyNN Synapses Types
		 		 */
		 		for (AlphaCondSynapse alphaCondSynapse : neuroml.getAlphaCondSynapse()){
		 			CompositeNode compositeNode = populateNeuroMLModelTreeUtils.createPynnSynapseNode(alphaCondSynapse);
		 			_discoveredComponents.put(alphaCondSynapse.getId(), alphaCondSynapse);
		 			_discoveredNodesInNeuroML.put(alphaCondSynapse.getId(), compositeNode);
		 			
		 		}
		 		for (ExpCondSynapse expCondSynapse : neuroml.getExpCondSynapse()){
		 			CompositeNode compositeNode = populateNeuroMLModelTreeUtils.createPynnSynapseNode(expCondSynapse);
		 			_discoveredComponents.put(expCondSynapse.getId(), expCondSynapse);
		 			_discoveredNodesInNeuroML.put(expCondSynapse.getId(), compositeNode);
		 			
		 		}
		 		for (ExpCurrSynapse expCurrSynapse : neuroml.getExpCurrSynapse()){
		 			CompositeNode compositeNode = populateNeuroMLModelTreeUtils.createPynnSynapseNode(expCurrSynapse);
		 			_discoveredComponents.put(expCurrSynapse.getId(), expCurrSynapse);
		 			_discoveredNodesInNeuroML.put(expCurrSynapse.getId(), compositeNode);
		 			
		 		}
		 		for (AlphaCurrSynapse alphaCurrSynapse : neuroml.getAlphaCurrSynapse()){
		 			CompositeNode compositeNode = populateNeuroMLModelTreeUtils.createPynnSynapseNode(alphaCurrSynapse);
		 			_discoveredComponents.put(alphaCurrSynapse.getId(), alphaCurrSynapse);
		 			_discoveredNodesInNeuroML.put(alphaCurrSynapse.getId(), compositeNode);
		 			
		 		}
		 		
		 		/**
		 		 * Biophysical Properties
		 		 */
		 		for (BiophysicalProperties biophysicalProperties : neuroml.getBiophysicalProperties()){
		 			CompositeNode compositeNode = populateNeuroMLModelTreeUtils.createBiophysicalPropertiesNode(biophysicalProperties);
		 			_discoveredComponents.put(biophysicalProperties.getId(), biophysicalProperties);
		 			_discoveredNodesInNeuroML.put(biophysicalProperties.getId(), compositeNode);
		 		}
		 		
		 		/**
		 		 * Check if we have a non-predefined neuroml component
		 		 */
//		 		for (Component component : lems.getComponents()){
//		 			if (!discoveredNodesInNeuroML.containsKey(component.getID())){
//		 				ComponentType ct = component.getComponentType();
//	                    for (ParamValue pv: component.getParamValues()) {
//	                        if (component.hasAttribute(pv.getName())) {
//	                            String orig = component.getStringValue(pv.getName());
//	                            
//	                            //String formatted = formatDimensionalQuantity(orig);
//	                            //elementProps.put(pv.getName(), formatted);
//	                        }
//	                    }
//		 			}
//		 		}
		 		
//			}
//			else{
		 		//TODO: Implement getModelTree for subentities
		 		//TODO: It can be useful to implement a map between subentity and cell
//				int endIndex = entityNode.getId().lastIndexOf("_");
//			    if (endIndex != -1)  
//			    {
//			        String newstr = entityNode.getId().substring(0, endIndex);
//			    }
				
				//neuroMLAccessUtility.getComponent(componentId, model, Resources.CELL);
//				for (CompositeNode compositeNode : getCells(model)){
//	 				discoveredNodesInNeuroML.put(compositeNode.getId(), compositeNode);
//				}
//			}
			
			
		 		for (Map.Entry<String, ANode> entry : _discoveredNodesInNeuroML.entrySet()) {
		 		    String key = entry.getKey();
		 		    
		 		   if (_discoveredNodesInNeuroML.size() == 1){
		 			  Object node = _discoveredNodesInNeuroML.values().toArray()[0];
		 			   if (node instanceof ACompositeNode){
		 				  modelTree.addChildren(((ACompositeNode)node).getChildren());
		 				  break;
		 			   }
					}
		 		   if (!_discoveredNestedComponentsId.contains(key)){
		 			   modelTree.addChild(entry.getValue());
		 		   }
		 		}
	 		
	 		
	 		_populated = true;
		} catch (Exception e) {
			_populated = false;
			throw new ModelInterpreterException(e);
		}
 		
 		return _populated;
	}

	

	
}
