package org.geppetto.model.neuroml.modelInterpreterUtils;

import java.util.Map;

import org.geppetto.core.model.GeppettoModelAccess;
import org.geppetto.core.services.registry.ServicesRegistry;
import org.geppetto.model.DomainModel;
import org.geppetto.model.GeppettoFactory;
import org.geppetto.model.Node;
import org.geppetto.model.neuroml.utils.ModelInterpreterUtils;
import org.geppetto.model.neuroml.utils.Resources;
import org.geppetto.model.types.CompositeType;
import org.geppetto.model.types.Type;
import org.geppetto.model.types.TypesFactory;
import org.geppetto.model.types.TypesPackage;
import org.geppetto.model.util.GeppettoVisitingException;
import org.geppetto.model.values.Text;
import org.geppetto.model.values.ValuesFactory;
import org.geppetto.model.variables.Variable;
import org.geppetto.model.variables.VariablesFactory;
import org.lemsml.jlems.core.sim.LEMSException;
import org.lemsml.jlems.core.type.Attribute;
import org.lemsml.jlems.core.type.Component;
import org.neuroml.model.Segment;
import org.neuroml.model.util.NeuroMLException;

public class NeuroMLModelInterpreterUtils
{

	static GeppettoFactory geppettoFactory = GeppettoFactory.eINSTANCE;
	static TypesFactory typesFactory = TypesFactory.eINSTANCE;
	static ValuesFactory valuesFactory = ValuesFactory.eINSTANCE;
	static VariablesFactory variablesFactory = VariablesFactory.eINSTANCE;
	
	
	public static void createCompositeTypeFromAnnotation(CompositeType compositeType, Component annotation, GeppettoModelAccess access) throws LEMSException, NeuroMLException, GeppettoVisitingException
	{
		CompositeType annotationType = typesFactory.createCompositeType();
		NeuroMLModelInterpreterUtils.initialiseNodeFromComponent(annotationType, annotation);
		for(Map.Entry<String, Component> entry : annotation.getChildHM().entrySet())
		{
			if(entry.getKey().equals("property"))
			{
				Component property = entry.getValue();
				Text text = valuesFactory.createText();
				text.setText(property.getTextParam("value"));

				Variable variable = variablesFactory.createVariable();
				NeuroMLModelInterpreterUtils.initialiseNodeFromString(variable, property.getTextParam("tag"));
				variable.getTypes().add(access.getType(TypesPackage.Literals.TEXT_TYPE));
				variable.getInitialValues().put(access.getType(TypesPackage.Literals.TEXT_TYPE), text);
				annotationType.getVariables().add(variable);
			}
			else
			{
				Component rdf = entry.getValue();
				Component rdfDescription = rdf.getChild("rdf:Description");
				for(Map.Entry<String, Component> rdfDescriptionChild : rdfDescription.getChildHM().entrySet())
				{
					CompositeType annotationTypeChild = typesFactory.createCompositeType();
					NeuroMLModelInterpreterUtils.initialiseNodeFromString(annotationType, rdfDescriptionChild.getKey());

					Variable variable = variablesFactory.createVariable();
					NeuroMLModelInterpreterUtils.initialiseNodeFromString(variable, rdfDescriptionChild.getKey());
					variable.getAnonymousTypes().add(annotationTypeChild);
					annotationType.getVariables().add(variable);

					for(Component singleChildren : rdfDescriptionChild.getValue().getChild("rdf:Bag").getStrictChildren())
					{
						for(Attribute attr : singleChildren.getAttributes())
						{
							annotationTypeChild.getVariables().add(ModelInterpreterUtils.createTextTypeVariable(attr.getName(), attr.getValue(), access));
						}
						if(!singleChildren.getAbout().equals("")) annotationTypeChild.getVariables().add(
								ModelInterpreterUtils.createTextTypeVariable(rdfDescriptionChild.getKey(), singleChildren.getAbout(), access));

					}
				}
			}
		}

		Variable variable = variablesFactory.createVariable();
		NeuroMLModelInterpreterUtils.initialiseNodeFromComponent(variable, annotation);
		variable.getAnonymousTypes().add(annotationType);
		compositeType.getVariables().add(variable);
	}
	
	public static void initialiseNodeFromComponent(Node node, Component component)
	{
		if(node instanceof Type)
		{
			DomainModel domainModel = geppettoFactory.createDomainModel();
			domainModel.setDomainModel(component);
			domainModel.setFormat(ServicesRegistry.getModelFormat("LEMS"));
			((Type) node).setDomainModel(domainModel);
		}
		
		if(component.getID() != null){
			node.setName(ModelInterpreterUtils.parseId(component.getID()));	
		}
		else{
			node.setName(Resources.getValueById(component.getDeclaredType())); 
		}
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

	public static Variable getVisualVariable(String preSegmentId)
	{
		// TODO Auto-generated method stub
		return null;
	}
}
