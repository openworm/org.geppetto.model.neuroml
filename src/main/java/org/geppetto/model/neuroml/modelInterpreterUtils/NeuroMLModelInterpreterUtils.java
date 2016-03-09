package org.geppetto.model.neuroml.modelInterpreterUtils;

import org.geppetto.core.services.registry.ServicesRegistry;
import org.geppetto.model.DomainModel;
import org.geppetto.model.GeppettoFactory;
import org.geppetto.model.Node;
import org.geppetto.model.neuroml.utils.ModelInterpreterUtils;
import org.geppetto.model.neuroml.utils.Resources;
import org.geppetto.model.types.Type;
import org.lemsml.jlems.core.type.Component;
import org.neuroml.model.Segment;

public class NeuroMLModelInterpreterUtils
{

	static GeppettoFactory geppettoFactory = GeppettoFactory.eINSTANCE;
	
	public static void initialiseNodeFromComponent(Node node, Component component)
	{
		if(node instanceof Type)
		{
			DomainModel domainModel = geppettoFactory.createDomainModel();
			domainModel.setDomainModel(component);
			domainModel.setFormat(ServicesRegistry.getModelFormat("LEMS"));
			((Type) node).setDomainModel(domainModel);
		}
		node.setName(Resources.getValueById(component.getDeclaredType()) + ((component.getID() != null) ? " - " + ModelInterpreterUtils.parseId(component.getID()) : ""));
		node.setId(ModelInterpreterUtils.parseId((component.getID() != null) ? component.getID() : component.getDeclaredType()));
	}

	public static void initialiseNodeFromString(Node node, String attributesName)
	{
		node.setName(Resources.getValueById(attributesName));
		node.setId(ModelInterpreterUtils.parseId(attributesName));
	}

	/**
	 * @param neuromlID
	 * @return
	 */
	public static String getVisualObjectIdentifier(Segment segment)
	{
		return (segment.getName() != null && !segment.getName().equals("")) ? (segment.getName() + "_" + segment.getId()) : "vo" + segment.getId();
	}
}
