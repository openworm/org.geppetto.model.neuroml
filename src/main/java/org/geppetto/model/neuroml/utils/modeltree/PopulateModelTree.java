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
package org.geppetto.model.neuroml.utils.modeltree;


/**
 * Populates the Model Tree of Aspect
 * 
 * @author  Jesus R. Martinez (jesus@metacell.us)
 * @author Adrian Quintana (adrian.perez@ucl.ac.uk)
 * 
 */
public class PopulateModelTree {

//	private boolean _populated = false;
//	private PopulateNeuroMLModelTreeUtils populateNeuroMLModelTreeUtils;
//	
//	private static Log _logger = LogFactory.getLog(PopulateModelTree.class);
//
//	private Map<String, ParameterSpecificationNode> _parameterNodes = new HashMap<String, ParameterSpecificationNode>();
//	private Map<ParameterSpecificationNode, Object> _parametersToMethodsMap = new HashMap<ParameterSpecificationNode,Object>();
//	private Map<ParameterSpecificationNode, Object> _parametersToObjectssMap = new HashMap<ParameterSpecificationNode,Object>();
//	
//	public PopulateModelTree() {		
//	}
//	
//	/**
//	 * Method that is contacted to start populating the model tree
//	 * 
//	 * @param modelTree - Model tree that is to be populated
//	 * @param neuroml - NeuroMLDocument used to populate the tree, values are in here
//	 * @return 
//	 * @throws ModelInterpreterException 
//	 */
//	public boolean populateModelTree(AspectSubTreeNode modelTree, ModelWrapper model) throws ModelInterpreterException
//	{		
//		long start = System.currentTimeMillis();
//		
//		populateNeuroMLModelTreeUtils = new PopulateNeuroMLModelTreeUtils((ModelWrapper) model);
//		
//		NeuroMLDocument neuroml = (NeuroMLDocument) ((ModelWrapper) model).getModel(ServicesRegistry.getModelFormat("NEUROML"));
//
//		Map<String, EntityNode> mapping = (Map<String, EntityNode>) ((ModelWrapper) model).getModel(NeuroMLAccessUtility.SUBENTITIES_MAPPING_ID);
//		Map<String, BaseCell> cellMapping = (Map<String, BaseCell>) ((ModelWrapper) model).getModel(NeuroMLAccessUtility.CELL_SUBENTITIES_MAPPING_ID);
//		
//		List<String> _discoveredNestedComponentsId = ((ArrayList<String>)((ModelWrapper) model).getModel(NeuroMLAccessUtility.DISCOVERED_NESTED_COMPONENTS_ID));
//		Map<String, Base> _discoveredComponents = ((HashMap<String, Base>)((ModelWrapper) model).getModel(NeuroMLAccessUtility.DISCOVERED_COMPONENTS));
//		Map<String, ANode> _discoveredNodesInNeuroML =  new HashMap<String, ANode>();
//		
//		/*
//		 * According to the Geppetto NeuroML model, we can have any (NeuroML)
//		 * standalone element in the ModelTree root. All hildren entities of the
//		 * root node will correspond to NeuroML Cells.
//		 * 
//		 * Here we need to check if we are getting the ModelTree of the root
//		 * entity -- in which case we have to parse the whole document -- or of
//		 * one of its children -- in which case we will create a Component Node
//		 * from the cell element
//		 */
//		try {
//			if (modelTree.getParent().getParent().getParent().getId().equals("scene")){
//				// if the grandgrandfather of the node is "scene", we can assume
//				// that this is the root node
//
//				//Generate Model Tree for Subentities (We don't add network as it has been
//				//implicitly added through the entities structure), so we go straight for cells
//				if (cellMapping.size() > 1){
//					for (Map.Entry<String, BaseCell> entry : cellMapping.entrySet()) {
//					    String key = entry.getKey();
//					    BaseCell value = entry.getValue();
//					    
//					    EntityNode entityNode = mapping.get(key);
//					    for (AspectNode aspectNode : entityNode.getAspects()){
//							if (aspectNode.getId() == modelTree.getParent().getId()){
//								AspectSubTreeNode modelTreeSubEntity = (AspectSubTreeNode)aspectNode.getSubTree(AspectTreeType.MODEL_TREE);
//								modelTreeSubEntity.addChildren(populateNeuroMLModelTreeUtils.createCellNode(value).getChildren());
//								modelTreeSubEntity.setModified(true);
//							}
//					    }	
//					}
//				}
//				
//				//First parse non standalone elements
//				for (ExtracellularProperties element : neuroml.getExtracellularProperties()){
//					_discoveredNodesInNeuroML.put(element.getId(), populateNeuroMLModelTreeUtils.createExtracellularPropertiesNode((ExtracellularProperties)element));
//				}
//				for (int i = 0; i < neuroml.getIntracellularProperties().size(); i++){
//					//Theoretically you can have more than one intracellular property in a neuroml doc but it does not make sense as it doesnt have an id at the moment
//					_discoveredNodesInNeuroML.put(Resources.INTRACELLULAR_P.getId(), populateNeuroMLModelTreeUtils.createIntracellularPropertiesNode(neuroml.getIntracellularProperties().get(i)));
//				}
//				
//				
//				//Iterate through all standalone elements
//		        Map<String,Standalone> standalones = NeuroMLConverter.getAllStandaloneElements(neuroml);
//		        
//		        for (Standalone element: standalones.values())
//		        {
//		        	
//		        	//Add element to component cache
//		        	_discoveredComponents.put(element.getId(), element);
//		        	
//		        	//Add node to sumary node
//		        	populateNeuroMLModelTreeUtils.addInfoNode(element);
//					
//					// Points to CellTypes group and PynnCellTypes
//					if(element instanceof BaseCell)
//			        {
//			 			_discoveredNodesInNeuroML.put(element.getId(), populateNeuroMLModelTreeUtils.createCellNode((BaseCell)element));
//			        }
//					// One of these should be removed in next release
//					else if(element instanceof IonChannel || element instanceof IonChannelHH)
//			        {
//						_discoveredNodesInNeuroML.put(element.getId(), populateNeuroMLModelTreeUtils.createChannelNode((IonChannel)element));
//			        }
//					//Concentration Model Types group
//					else if(element instanceof DecayingPoolConcentrationModel || element instanceof FixedFactorConcentrationModel)
//			        {
//						_discoveredNodesInNeuroML.put(element.getId(), populateNeuroMLModelTreeUtils.createConcentrationModelNode(element));
//			        }
//					//Points to Synapse Types group
//					else if(element instanceof BaseConductanceBasedSynapse)
//			        {
//						_discoveredNodesInNeuroML.put(element.getId(), populateNeuroMLModelTreeUtils.createSynapseNode((BaseConductanceBasedSynapse)element));
//			        }
//					//Points to Pynn Synapse Types group
//					else if(element instanceof BasePynnSynapse)
//					{
//						_discoveredNodesInNeuroML.put(element.getId(), populateNeuroMLModelTreeUtils.createPynnSynapseNode((BasePynnSynapse)element));
//					}
//					else if(element instanceof BiophysicalProperties)
//					{
//						_discoveredNodesInNeuroML.put(element.getId(), populateNeuroMLModelTreeUtils.createBiophysicalPropertiesNode((BiophysicalProperties)element));
//					}
//					else if(element instanceof PulseGenerator)
//					{
//						_discoveredNodesInNeuroML.put(element.getId(), populateNeuroMLModelTreeUtils.createPulseGeneratorNode((PulseGenerator)element));
//					}
//					else if(element instanceof Network)
//					{
//						if (mapping.size() == 1){
//							//TODO: We are not adding the network as it is implicitly in the entities/subentities (unless there is only one cell) structure but we can be losing some info
//							_discoveredNodesInNeuroML.put(element.getId(), populateNeuroMLModelTreeUtils.createNetworkNode((Network)element));
//						}
//						else{
//							populateNeuroMLModelTreeUtils.createNetworkNode((Network)element);
//						}
//					}
//					else{
//						
//				 		/**
//				 		 * Check if we have a non-predefined neuroml component
//				 		 */
//						try {
//							CompositeNode compositeNode = new CompositeNode(element.getId(), element.getId());
//			                Component comp = Utils.convertNeuroMLToComponent(element);
//			                //ComponentType ct = comp.getComponentType();
//			                for (ParamValue pv: comp.getParamValues()) {
//			                    if (comp.hasAttribute(pv.getName())) {
//			                        String orig = comp.getStringValue(pv.getName());
//			                        compositeNode.addChild(new TextMetadataNode(pv.getName().replaceAll("[&\\/\\\\#,+()$~%.'\":*?<>{}\\s]", "_"), pv.getName(), new StringValue(orig)));
//			                    }
//			                }
//			                _discoveredNodesInNeuroML.put(element.getId(), compositeNode);
//			            } catch (LEMSException ce) {
//			                throw new NeuroMLException("Problem extracting info from NeuroML component",ce);
//			            }
//					}
//		        }
//		 		
//		        //Add Summary Node
//				_discoveredNodesInNeuroML.put(Resources.SUMMARY.getId(), populateNeuroMLModelTreeUtils.getSummaryNode());
//				
//				//Add only nodes which are not pointed by any other node
//		 		for (Map.Entry<String, ANode> entry : _discoveredNodesInNeuroML.entrySet()) {
//		 		   if (!_discoveredNestedComponentsId.contains(entry.getKey())){
//		 			   modelTree.addChild(entry.getValue());
//		 		   }
//		 		}
//
//				//Add Description Node
//		 		//modelTree.addChild(populateNeuroMLModelTreeUtils.getDescriptionNode());
//			}
//			else{
//				//Populate model tree for a subentity
//				BaseCell baseCell = cellMapping.get(modelTree.getParent().getParent().getId());
//				modelTree.addChildren(populateNeuroMLModelTreeUtils.createCellNode(baseCell).getChildren());
//				
//				//Add Sumary Node
//				populateNeuroMLModelTreeUtils.addInfoNode(baseCell);
//				modelTree.addChild(populateNeuroMLModelTreeUtils.getSummaryNode());
//				
//				//Add population properties
//				boolean found=false;
//				for(Network n:neuroml.getNetwork())
//				{
//					for(Population p : n.getPopulation())
//					{
//						//This code needs to change during the instance type refactoring. We are going up to the network
//						//to find an instance property. Only thing in this case the property in NeuroML is not even defined at
//						//an instance level but at a population level, so it's neither a type or an instance. Discuss with Padraig.
//				        //<population id="ADAL" type="populationList" component="generic_iaf_cell">
//			            //<property tag="OpenWormBackerAssignedName" value="Xabe"/> <--############# WHAT WE WANT
//			            //<instance id="0">
//			            //    <location y="8.65" x="-239.25" z="31.050000000000001"/>
//			            //</instance>
//			            //</population>
//						if(modelTree.getParent().getParent().getId().startsWith(p.getId()))
//						{
//							for (Property property : p.getProperty()){
//								modelTree.addChild(PopulateNodesModelTreeUtils.createTextMetadataNode(property.getTag(), property.getTag(), new StringValue(property.getValue())));
//							}
//							found=true;
//							break;
//						}
//					}
//					if(found)
//					{
//						break;
//					}
//				}
//
//				modelTree.setModified(true);
//			}
//			
//	 		_populated = true;
//	 		
//	 		//store parameter specs nodes in map for easy access using visitor
//	 		TrackParameterSpecsNodesVisitors visitor = new TrackParameterSpecsNodesVisitors();
//	 		modelTree.apply(visitor);
//	 		this._parameterNodes = visitor.getParametersMap();
//	 		this._parametersToObjectssMap = this.populateNeuroMLModelTreeUtils.getParametersNodeToObjectsMap();
//	 		this._parametersToMethodsMap = this.populateNeuroMLModelTreeUtils.getParametersNodeToMethodsMap();
//		} catch (Exception e) {
//			_populated = false;
//			throw new ModelInterpreterException(e);
//		}
//		
//		_logger.info("Populate model tree completed, took " + (System.currentTimeMillis() - start) + "ms");
//		
// 		return _populated;
//	}
//	
//	public Map<String, ParameterSpecificationNode> getParametersNode(){
//		return _parameterNodes ;
//	}
//	
//	public Map<ParameterSpecificationNode,Object> getParametersNodeToMethodsMap(){
//		return _parametersToMethodsMap ;
//	}
//	
//	public Map<ParameterSpecificationNode,Object> getParametersNodeToObjectsMap(){
//		return _parametersToObjectssMap ;
//	}
	
}
