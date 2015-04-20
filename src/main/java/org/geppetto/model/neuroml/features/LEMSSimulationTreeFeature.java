package org.geppetto.model.neuroml.features;

import java.util.Map;
import java.util.StringTokenizer;

import org.geppetto.core.features.IWatchableVariableListFeature;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.model.runtime.ACompositeNode;
import org.geppetto.core.model.runtime.ANode;
import org.geppetto.core.model.runtime.AspectNode;
import org.geppetto.core.model.runtime.AspectSubTreeNode;
import org.geppetto.core.model.runtime.AspectSubTreeNode.AspectTreeType;
import org.geppetto.core.model.runtime.CompositeNode;
import org.geppetto.core.model.runtime.EntityNode;
import org.geppetto.core.model.runtime.VariableNode;
import org.geppetto.core.services.GeppettoFeature;
import org.geppetto.model.neuroml.services.ModelFormat;
import org.geppetto.model.neuroml.utils.NeuroMLAccessUtility;
import org.lemsml.jlems.core.expression.ParseError;
import org.lemsml.jlems.core.sim.ContentError;
import org.lemsml.jlems.core.sim.LEMSException;
import org.lemsml.jlems.core.type.Component;
import org.lemsml.jlems.core.type.Exposure;
import org.lemsml.jlems.core.type.Lems;
import org.neuroml.model.BaseCell;
import org.neuroml.model.util.NeuroMLException;

public class LEMSSimulationTreeFeature implements IWatchableVariableListFeature
{
	private Lems lems;
	private AspectSubTreeNode watchTree;
	private Map<String, BaseCell> cellMapping;
	private Map<String, EntityNode> mapping;

	private GeppettoFeature type = GeppettoFeature.WATCHABLE_VARIABLE_LIST_FEATURE;

	@Override
	public GeppettoFeature getType()
	{
		return type;
	}

	@Override
	public boolean listWatchableVariablea(AspectNode aspectNode) throws ModelInterpreterException
	{
		boolean modified = true;

		watchTree = (AspectSubTreeNode) aspectNode.getSubTree(AspectTreeType.SIMULATION_TREE);
		watchTree.setId(AspectTreeType.SIMULATION_TREE.toString());
		watchTree.setModified(modified);

		mapping = (Map<String, EntityNode>) ((ModelWrapper) aspectNode.getModel()).getModel(NeuroMLAccessUtility.SUBENTITIES_MAPPING_ID);
		cellMapping = (Map<String, BaseCell>) ((ModelWrapper) aspectNode.getModel()).getModel(NeuroMLAccessUtility.CELL_SUBENTITIES_MAPPING_ID);
		lems = (Lems) ((ModelWrapper) aspectNode.getModel()).getModel(ModelFormat.LEMS);
		try
		{
			lems.setResolveModeLoose();
			lems.deduplicate();
			lems.resolve();
			lems.evaluateStatic();
		}
		catch(ContentError | ParseError e)
		{
			throw new ModelInterpreterException(e);
		}

		// Check if it is a entity (parse the whole document) or a subentity (create a component node from the cell element)
		if(watchTree.getParent().getParent().getParent().getId().equals("scene"))
		{

			try
			{
				Component simCpt = lems.getTarget().getComponent();
				String targetId = simCpt.getStringValue("target");
				Component tgtComp = lems.getComponent(targetId);
				try
				{
					extractWatchableVariables(tgtComp, "");
				}
				catch(NeuroMLException | LEMSException e)
				{
					// FIXME: Throw proper exception
					throw new ModelInterpreterException(e);
				}
			}
			catch(ContentError e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			try
			{
				BaseCell baseCell = cellMapping.get(watchTree.getParent().getParent().getId());
				Component cellComponent = lems.getComponent(baseCell.getId());
				extractWatchableVariables(cellComponent, cellComponent.getID() + ".");
			}
			catch(NeuroMLException | LEMSException e)
			{
				// FIXME: Throw proper exception
				throw new ModelInterpreterException(e);
			}
		}

		return modified;
	}

	public void extractWatchableVariables(Component component, String instancePath) throws NeuroMLException, LEMSException, ModelInterpreterException
	{
		// Generate Simulation Tree for Subentities (We don't as network as it has been implicit added through the entities structure)
		if(cellMapping != null && cellMapping.size() > 1 && (component.getComponentType().getName().equals("population") || component.getComponentType().getName().equals("populationList")))
		{

			for(Map.Entry<String, BaseCell> entry : cellMapping.entrySet())
			{
				String key = entry.getKey();
				BaseCell value = entry.getValue();

				EntityNode entityNode = mapping.get(key);
				for(AspectNode aspectNode : entityNode.getAspects())
				{
					if(aspectNode.getId() == watchTree.getParent().getId())
					{

						Component cellComponent = lems.getComponent(value.getId());
						this.watchTree = (AspectSubTreeNode) aspectNode.getSubTree(AspectTreeType.SIMULATION_TREE);
						this.watchTree.setId(AspectTreeType.SIMULATION_TREE.toString());

						LEMSSimulationTreeFeature lemsSimulationTreeFeature = new LEMSSimulationTreeFeature();
						lemsSimulationTreeFeature.setWatchTree(this.watchTree);
						lemsSimulationTreeFeature.extractWatchableVariables(cellComponent, cellComponent.getID() + ".");

						this.watchTree.setModified(true);
					}
				}
			}
		}
		else
		{
			for(Exposure exposure : component.getComponentType().getExposures())
			{
				createWatchableVariableNode(instancePath + exposure.getName());
			}
			for(Component componentChild : component.getAllChildren())
			{

				if(cellMapping != null && cellMapping.size() == 1 && componentChild.getComponentType().getName().equals("cell"))
				{
					// FIXME: This a quick and dirty solution but we have to define the way we would like to specify the variable to be watched in the runtime tree
					String newInstancePath = instancePath.substring(0, instancePath.length() - 1) + "[0]" + ".";
					extractWatchableVariables(componentChild, newInstancePath);
				}
				else
				{
					extractWatchableVariables(componentChild, instancePath + ((componentChild.getID() == null) ? componentChild.getTypeName() : componentChild.getID()) + ".");
				}
			}
		}
	}

	public void createWatchableVariableNode(String watchableVariableInstancePath)
	{
		StringTokenizer tokenizer = new StringTokenizer(watchableVariableInstancePath, ".");
		ACompositeNode node = watchTree;
		while(tokenizer.hasMoreElements())
		{
			String current = tokenizer.nextToken();
			boolean found = false;

			for(ANode child : node.getChildren())
			{
				if(child.getId().equals(current))
				{
					if(child instanceof ACompositeNode)
					{
						node = (ACompositeNode) child;
					}
					found = true;
					break;
				}
			}
			if(found)
			{
				continue;
			}
			else
			{
				if(tokenizer.hasMoreElements())
				{
					// not a leaf, create a composite state node
					CompositeNode newNode = new CompositeNode(current);
					newNode.setId(current);
					node.addChild(newNode);
					node = newNode;
				}
				else
				{
					// it's a leaf node
					VariableNode newNode = new VariableNode(current);
					// newNode.setId(current);
					node.addChild(newNode);
				}
			}
		}
	}

	public void setWatchTree(AspectSubTreeNode watchTree)
	{
		this.watchTree = watchTree;
	}

}
