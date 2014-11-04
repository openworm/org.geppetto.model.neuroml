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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.model.runtime.ACompositeNode;
import org.geppetto.core.model.runtime.ANode;
import org.geppetto.core.model.runtime.AspectNode;
import org.geppetto.core.model.runtime.AspectSubTreeNode;
import org.geppetto.core.model.runtime.CompositeNode;
import org.geppetto.core.model.runtime.EntityNode;
import org.geppetto.core.model.runtime.TextMetadataNode;
import org.geppetto.core.model.runtime.AspectSubTreeNode.AspectTreeType;
import org.geppetto.core.model.runtime.FunctionNode;
import org.geppetto.core.model.values.IntValue;
import org.geppetto.core.model.values.StringValue;
import org.geppetto.core.utilities.VariablePathSerializer;
import org.geppetto.model.neuroml.utils.NeuroMLAccessUtility;
import org.geppetto.model.neuroml.utils.Resources;
import org.lemsml.jlems.core.sim.LEMSException;
import org.lemsml.jlems.core.type.Component;
import org.lemsml.jlems.core.type.ComponentType;
import org.lemsml.jlems.core.type.ParamValue;
import org.neuroml.export.Utils;
import org.neuroml.export.info.InfoTreeCreator;
import org.neuroml.export.info.model.ExpressionNode;
import org.neuroml.export.info.model.InfoNode;
import org.neuroml.export.info.model.PlotNode;
import org.neuroml.model.AdExIaFCell;
import org.neuroml.model.AlphaCondSynapse;
import org.neuroml.model.AlphaCurrSynapse;
import org.neuroml.model.Base;
import org.neuroml.model.BaseCell;
import org.neuroml.model.BaseConductanceBasedSynapse;
import org.neuroml.model.BasePynnSynapse;
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
import org.neuroml.model.PopulationTypes;
import org.neuroml.model.Region;
import org.neuroml.model.Standalone;
import org.neuroml.model.SynapticConnection;
import org.neuroml.model.util.NeuroMLConverter;
import org.neuroml.model.util.NeuroMLException;

/**
 * Populates the Model Tree of Aspect
 * 
 * @author  Jesus R. Martinez (jesus@metacell.us)
 * @author Adrian Quintana (adrian.perez@ucl.ac.uk)
 * 
 */
public class PopulateModelTree {

	private boolean _populated = false;
	
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
		Map<String, BaseCell> cellMapping = (Map<String, BaseCell>) ((ModelWrapper) model).getModel(NeuroMLAccessUtility.CELL_SUBENTITIES_MAPPING_ID);
		
		List<String> _discoveredNestedComponentsId = ((ArrayList<String>)((ModelWrapper) model).getModel(NeuroMLAccessUtility.DISCOVERED_NESTED_COMPONENTS_ID));
		HashMap<String, Base> _discoveredComponents = ((HashMap<String, Base>)((ModelWrapper) model).getModel(NeuroMLAccessUtility.DISCOVERED_COMPONENTS));
		HashMap<String, ANode> _discoveredNodesInNeuroML =  new HashMap<String, ANode>();
		
		populateNeuroMLModelTreeUtils.setModel((ModelWrapper) model);
		
		try {
			//Check if it is a entity (parse the whole document) or a subentity (create a component node from the cell element)
			if (modelTree.getParent().getParent().getParent().getId().equals("scene")){

		 		//Generate Model Tree for Subentities (We don't as network as it has been implicit added through the entities structure)
				if (cellMapping.size() > 1){
					for (Map.Entry<String, BaseCell> entry : cellMapping.entrySet()) {
					    String key = entry.getKey();
					    BaseCell value = entry.getValue();
					    
					    EntityNode entityNode = mapping.get(key);
					    for (AspectNode aspectNode : entityNode.getAspects()){
							if (aspectNode.getId() == modelTree.getParent().getId()){
								AspectSubTreeNode modelTreeSubEntity = (AspectSubTreeNode)aspectNode.getSubTree(AspectTreeType.MODEL_TREE);
								modelTreeSubEntity.addChildren(populateNeuroMLModelTreeUtils.createCellNode(value).getChildren());
								modelTreeSubEntity.setModified(true);
							}
					    }	
					}
				}
				
				//Iterate through all standalone elements
		        LinkedHashMap<String,Standalone> standalones = NeuroMLConverter.getAllStandaloneElements(neuroml);
		        InfoNode infoNode = new InfoNode();
		        for (Standalone element: standalones.values())
		        {
		        	
		        	//Add element to component cache
		        	_discoveredComponents.put(element.getId(), element);
		        	
		        	//Populate sumary node
					infoNode.putAll(InfoTreeCreator.createPropertiesFromStandaloneComponent(element));
					
					if(element instanceof BaseCell)
			        {
			 			_discoveredNodesInNeuroML.put(element.getId(), populateNeuroMLModelTreeUtils.createCellNode((BaseCell)element));
			        }
					else if(element instanceof IonChannel)
			        {
						_discoveredNodesInNeuroML.put(element.getId(), populateNeuroMLModelTreeUtils.createChannelNode((IonChannel)element));
			        }
					else if(element instanceof IonChannelHH)
			        {
						_discoveredNodesInNeuroML.put(element.getId(), populateNeuroMLModelTreeUtils.createChannelNode((IonChannelHH)element));
			        }
//					else if(element instanceof ExtracellularProperties)
//			        {
//						_discoveredNodesInNeuroML.put(element.getId(), populateNeuroMLModelTreeUtils.createExtracellularPropertiesNode((ExtracellularProperties)element));
//			        }
//					else if(element instanceof IntracellularProperties)
//			        {
//						_discoveredNodesInNeuroML.put(element.getId(), populateNeuroMLModelTreeUtils.createIntracellularPropertiesNode((IntracellularProperties)element));
//			        }
					else if(element instanceof DecayingPoolConcentrationModel)
			        {
						_discoveredNodesInNeuroML.put(element.getId(), populateNeuroMLModelTreeUtils.createConcentrationModelNode((DecayingPoolConcentrationModel)element));
			        }
					else if(element instanceof FixedFactorConcentrationModel)
			        {
						_discoveredNodesInNeuroML.put(element.getId(), populateNeuroMLModelTreeUtils.createConcentrationModelNode((FixedFactorConcentrationModel)element));
			        }
					else if(element instanceof BaseConductanceBasedSynapse)
			        {
						_discoveredNodesInNeuroML.put(element.getId(), populateNeuroMLModelTreeUtils.createSynapseNode((BaseConductanceBasedSynapse)element));
			        }
					else if(element instanceof BasePynnSynapse)
					{
						_discoveredNodesInNeuroML.put(element.getId(), populateNeuroMLModelTreeUtils.createPynnSynapseNode((BasePynnSynapse)element));
					}
					else if(element instanceof BiophysicalProperties)
					{
						_discoveredNodesInNeuroML.put(element.getId(), populateNeuroMLModelTreeUtils.createBiophysicalPropertiesNode((BiophysicalProperties)element));
					}
					else if(element instanceof Network)
					{
						if (mapping.size() == 1){
							//TODO: We are not adding the network as it is implicitly in the entities/subentities (unless there is only one cell) structure but we can be losing some info
							_discoveredNodesInNeuroML.put(element.getId(), populateNeuroMLModelTreeUtils.createNetworkNode((Network)element));
						}
					}
					else{
				 		/**
				 		 * Check if we have a non-predefined neuroml component
				 		 */
						try {
							CompositeNode compositeNode = new CompositeNode(element.getId(), element.getId());
			                Component comp = Utils.convertNeuroMLToComponent(element);
			                ComponentType ct = comp.getComponentType();
			                for (ParamValue pv: comp.getParamValues()) {
			                    if (comp.hasAttribute(pv.getName())) {
			                        String orig = comp.getStringValue(pv.getName());
			                        compositeNode.addChild(new TextMetadataNode(pv.getName().replaceAll("[&\\/\\\\#,+()$~%.'\":*?<>{}\\s]", "_"), pv.getName(), new StringValue(orig)));
			                    }
			                }
			                _discoveredNodesInNeuroML.put(element.getId(), compositeNode);
			            } catch (LEMSException ce) {
			                throw new NeuroMLException("Problem extracting info from NeuroML component",ce);
			            }
					}
		        }
		 		
		        //Add Sumary Node
		        CompositeNode summaryNode = new CompositeNode(Resources.SUMMARY.getId(), Resources.SUMMARY.get());
				summaryNode.addChildren(populateNeuroMLModelTreeUtils.createInfoNode(infoNode));
				_discoveredNodesInNeuroML.put(Resources.SUMMARY.getId(), summaryNode);
		        
				//Add only nodes which are not pointed by any other node
		 		for (Map.Entry<String, ANode> entry : _discoveredNodesInNeuroML.entrySet()) {
		 		   if (!_discoveredNestedComponentsId.contains(entry.getKey())){
		 			   modelTree.addChild(entry.getValue());
		 		   }
		 		}
		 		
			}
			else{
				//Populate model tree for a subentity
				modelTree.addChildren(populateNeuroMLModelTreeUtils.createCellNode(cellMapping.get(modelTree.getParent().getParent().getId())).getChildren());
				modelTree.setModified(true);
			}
			
	 		_populated = true;
		} catch (Exception e) {
			_populated = false;
			throw new ModelInterpreterException(e);
		}
 		return _populated;
	}
	
}
