package org.geppetto.model.neuroml.modelInterpreterUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.geppetto.core.model.GeppettoModelAccess;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.model.neuroml.utils.ModelInterpreterUtils;
import org.geppetto.model.neuroml.utils.Resources;
import org.geppetto.model.neuroml.utils.ResourcesDomainType;
import org.geppetto.model.neuroml.visualUtils.ExtractVisualType;
import org.geppetto.model.types.ArrayType;
import org.geppetto.model.types.CompositeType;
import org.geppetto.model.types.CompositeVisualType;
import org.geppetto.model.types.ConnectionType;
import org.geppetto.model.types.Type;
import org.geppetto.model.types.TypesFactory;
import org.geppetto.model.types.TypesPackage;
import org.geppetto.model.types.VisualType;
import org.geppetto.model.util.GeppettoVisitingException;
import org.geppetto.model.util.PointerUtility;
import org.geppetto.model.values.ArrayElement;
import org.geppetto.model.values.ArrayValue;
import org.geppetto.model.values.Connection;
import org.geppetto.model.values.Connectivity;
import org.geppetto.model.values.Point;
import org.geppetto.model.values.Sphere;
import org.geppetto.model.values.Text;
import org.geppetto.model.values.ValuesFactory;
import org.geppetto.model.variables.Variable;
import org.geppetto.model.variables.VariablesFactory;
import org.lemsml.jlems.core.sim.LEMSException;
import org.lemsml.jlems.core.type.Attribute;
import org.lemsml.jlems.core.type.Component;
import org.lemsml.jlems.core.type.Exposure;
import org.lemsml.jlems.core.type.ParamValue;
import org.neuroml.export.utils.Utils;
import org.neuroml.model.util.NeuroMLException;

public class PopulateTypes
{

	private Map<String, Type> types;
	
	private TypesFactory typesFactory = TypesFactory.eINSTANCE;
	private ValuesFactory valuesFactory = ValuesFactory.eINSTANCE;
	private VariablesFactory variablesFactory = VariablesFactory.eINSTANCE;
	
	private TypeFactory typeFactory;

	private Map<Component, List<Variable>> cellSegmentMap = new HashMap<Component, List<Variable>>();

	private GeppettoModelAccess access;
	
	public PopulateTypes(Map<String, Type> types, GeppettoModelAccess access)
	{
		super();
		this.types = types;
		this.typeFactory = new TypeFactory(types);
		this.access = access;
	}

	/*
	 * Generic method to extract info from any component
	 */
	public CompositeType extractInfoFromComponent(Component component, String domainType) throws NumberFormatException, NeuroMLException, LEMSException, GeppettoVisitingException,
			ModelInterpreterException
	{
		// Create composite type depending on type of component and initialise it
		CompositeType compositeType = (CompositeType) typeFactory.getType((domainType == null) ? ResourcesDomainType.getValueByComponentType(component.getComponentType()) : domainType);
		NeuroMLModelInterpreterUtils.initialiseNodeFromComponent(compositeType, component);

		List<String> attributes = new ArrayList<String>();
		// Parameter types
		for(ParamValue pv : component.getParamValues())
		{
			if(component.hasAttribute(pv.getName()))
			{
				attributes.add(pv.getName());
				compositeType.getVariables().add(ModelInterpreterUtils.createParameterTypeVariable(pv.getName(), component.getStringValue(pv.getName()), this.access));
			}
		}

		// Text types
		for(Entry<String, String> entry : component.getTextParamMap().entrySet())
		{
			attributes.add(entry.getKey());
			compositeType.getVariables().add(ModelInterpreterUtils.createTextTypeVariable(entry.getKey(), entry.getValue(), this.access));
		}

		// Composite Type
		for(Entry<String, Component> entry : component.getRefComponents().entrySet())
		{
			Component refComponent = entry.getValue();
			attributes.add(refComponent.getID());
			if(!types.containsKey(refComponent.getDeclaredType() + refComponent.getID()))
			{
				types.put(refComponent.getDeclaredType() + refComponent.getID(), extractInfoFromComponent(refComponent, null));
			}
			Variable variable = variablesFactory.createVariable();
			NeuroMLModelInterpreterUtils.initialiseNodeFromComponent(variable, refComponent);
			variable.getTypes().add(types.get(refComponent.getDeclaredType() + refComponent.getID()));
			compositeType.getVariables().add(variable);
		}

		if(attributes.size() < component.getAttributes().size())
		{
			for(Attribute entry : component.getAttributes())
			{
				if(!attributes.contains(entry.getName()))
				{
					// component.getRelativeComponent("../pyramidals_48/37/pyr_4_sym")
					// component.getRelativeComponent(entry.getValue());
					// connection.getA().add(PointerUtility.getPointer(prePopulationVariable, prePopulationType, Integer.parseInt(preCellId)));

					// AQP For now: let's added as a metatype because I haven't found an easy way to extract the pointer
					compositeType.getVariables().add(ModelInterpreterUtils.createTextTypeVariable(entry.getName(), entry.getValue(), this.access));
				}
			}
		}

		// Extracting populations (this needs to be executed before extracting the projection otherwise we can't get the population)
		for(Component population : component.getChildrenAL("populations"))
		{
			if(!types.containsKey(population.getDeclaredType() + population.getID()))
			{
				createPopulationTypeVariable(population);
			}
			Variable variable = variablesFactory.createVariable();
			NeuroMLModelInterpreterUtils.initialiseNodeFromComponent(variable, population);
			variable.getTypes().add(types.get(population.getDeclaredType() + population.getID()));
			compositeType.getVariables().add(variable);
		}

		// Extracting projection and connections
		for(Component projection : component.getChildrenAL("projections"))
		{
			createConnectionTypeVariablesFromProjection(projection, compositeType);
		}

		// Extracting the rest of the child
		for(Component componentChild : component.getChildHM().values())
		{
			if(componentChild.getDeclaredType().equals(Resources.MORPHOLOGY.getId()))
			{
				createVisualTypeFromMorphology(component, compositeType, componentChild);
			}
			else if(componentChild.getDeclaredType().equals(Resources.ANNOTATION.getId()))
			{
				createCompositeTypeFromAnnotation(compositeType, componentChild);
			}
			else if(componentChild.getDeclaredType().equals(Resources.NOTES.getId()))
			{
				compositeType.getVariables().add(ModelInterpreterUtils.createTextTypeVariable(Resources.NOTES.get(), componentChild.getAbout(), this.access));
			}
			else
			{
				// For the moment all the child are extracted as anonymous types
				CompositeType anonymousCompositeType = extractInfoFromComponent(componentChild, null);
				if(anonymousCompositeType != null)
				{
					Variable variable = variablesFactory.createVariable();
					NeuroMLModelInterpreterUtils.initialiseNodeFromComponent(variable, componentChild);
					variable.getAnonymousTypes().add(anonymousCompositeType);
					compositeType.getVariables().add(variable);
				}
			}
		}

		// Extracting the rest of the children
		for(Component componentChild : component.getStrictChildren())
		{
			if((!componentChild.getComponentType().isOrExtends(Resources.POPULATION.getId()) && !componentChild.getComponentType().isOrExtends(Resources.POPULATION_LIST.getId()))
					&& !componentChild.getComponentType().isOrExtends(Resources.PROJECTION.getId()))
			{
				// If it is not a population, a projection/connection or a morphology, let's deal with it in a generic way
				CompositeType anonymousCompositeType = extractInfoFromComponent(componentChild, null);
				if(anonymousCompositeType != null)
				{
					// For the moment all the children are extracted as anonymous types
					Variable variable = variablesFactory.createVariable();
					NeuroMLModelInterpreterUtils.initialiseNodeFromComponent(variable, componentChild);
					variable.getAnonymousTypes().add(anonymousCompositeType);
					compositeType.getVariables().add(variable);
				}
			}
		}

		// Exposures are the variables that can potentially be watched
		for(Exposure exposure : component.getComponentType().getExposures())
		{
			if(cellSegmentMap.containsKey(component) && cellSegmentMap.get(component).size() > 1 && (exposure.getName().equals("v") || exposure.getName().equals("spiking")))
			{
				if(!types.containsKey("compartment") || ((CompositeType) types.get("compartment")).getVariables().size() == 1)
				{

					if(!types.containsKey("compartment"))
					{
						CompositeType compartmentCompositeType = (CompositeType) typeFactory.getType(null);
						NeuroMLModelInterpreterUtils.initialiseNodeFromString(compartmentCompositeType, "compartment");
						types.put("compartment", compartmentCompositeType);
					}

					if(exposure.getName().equals("v") || exposure.getName().equals("spiking"))
					{
						CompositeType compartmentCompositeType = (CompositeType) types.get("compartment");
						compartmentCompositeType.getVariables().add(
								ModelInterpreterUtils.createExposureTypeVariable(exposure.getName(), Utils.getSIUnitInNeuroML(exposure.getDimension()).getSymbol(), this.access));

					}
				}
			}
			else
			{
				compositeType.getVariables().add(ModelInterpreterUtils.createExposureTypeVariable(exposure.getName(), Utils.getSIUnitInNeuroML(exposure.getDimension()).getSymbol(), this.access));
			}
		}

		if(cellSegmentMap.containsKey(component) && cellSegmentMap.get(component).size() > 1)
		{
			for(Variable compartment : cellSegmentMap.get(component))
			{
				Variable variable = variablesFactory.createVariable();
				variable.setName(Resources.getValueById(compartment.getName()));
				variable.setId(compartment.getId());
				variable.getTypes().add(types.get("compartment"));
				compositeType.getVariables().add(variable);
			}
		}

		return compositeType;

	}

	private void createCompositeTypeFromAnnotation(CompositeType compositeType, Component annotation) throws LEMSException, NeuroMLException, GeppettoVisitingException
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

	private void createVisualTypeFromMorphology(Component component, CompositeType compositeType, Component morphology) throws GeppettoVisitingException, LEMSException, NeuroMLException,
			ModelInterpreterException
	{
		if(!types.containsKey(morphology.getDeclaredType() + morphology.getParent().getID() + "_" + morphology.getID()))
		{
			ExtractVisualType extractVisualType = new ExtractVisualType(component, access);
			types.put(morphology.getDeclaredType() + morphology.getParent().getID() + "_" + morphology.getID(), extractVisualType.createTypeFromCellMorphology());

			cellSegmentMap.put(component, extractVisualType.getVisualObjectsSegments());
		}
		compositeType.setVisualType((VisualType) types.get(morphology.getDeclaredType() + morphology.getParent().getID() + "_" + morphology.getID()));
	}

	public void createConnectionTypeVariablesFromProjection(Component projection, CompositeType compositeType) throws GeppettoVisitingException, LEMSException, NeuroMLException,
			NumberFormatException, ModelInterpreterException
	{
		// get/create the projection type and variable
		CompositeType projectionType = null;
		if(!types.containsKey(projection.getDeclaredType() + projection.getID()))
		{
			projectionType = (CompositeType) typeFactory.getType(ResourcesDomainType.PROJECTION.getId());
			NeuroMLModelInterpreterUtils.initialiseNodeFromComponent(projectionType, projection);
			types.put(projection.getDeclaredType() + projection.getID(), projectionType);
		}
		else
		{
			projectionType = (CompositeType) types.get(projection.getDeclaredType() + projection.getID());
		}
		Variable projectionVariable = variablesFactory.createVariable();
		NeuroMLModelInterpreterUtils.initialiseNodeFromComponent(projectionVariable, projection);
		projectionVariable.getTypes().add(types.get(projection.getDeclaredType() + projection.getID()));
		compositeType.getVariables().add(projectionVariable);

		// Create synapse type
		Component synapse = projection.getRefComponents().get(Resources.SYNAPSE.getId());
		if(!types.containsKey(synapse.getDeclaredType() + synapse.getID()))
		{
			types.put(synapse.getDeclaredType() + synapse.getID(), extractInfoFromComponent(projection.getRefComponents().get(Resources.SYNAPSE.getId()), ResourcesDomainType.SYNAPSE.getId()));
		}
		Variable synapsesVariable = variablesFactory.createVariable();
		NeuroMLModelInterpreterUtils.initialiseNodeFromComponent(synapsesVariable, synapse);
		synapsesVariable.getTypes().add(types.get(synapse.getDeclaredType() + synapse.getID()));
		projectionType.getVariables().add(synapsesVariable);

		// Create pre synaptic population
		ArrayType prePopulationType = (ArrayType) types.get(Resources.POPULATION.getId() + projection.getAttributeValue(Resources.PRE_SYNAPTIC_POPULATION.getId()));
		Variable prePopulationVariable = null;
		for(Variable variable : compositeType.getVariables())
		{
			if(variable.getId().equals(prePopulationType.getId()))
			{
				prePopulationVariable = variable;
				break;
			}
		}
		Variable preSynapticPopulationVariable = variablesFactory.createVariable();
		NeuroMLModelInterpreterUtils.initialiseNodeFromString(preSynapticPopulationVariable, Resources.PRE_SYNAPTIC_POPULATION.getId());
		preSynapticPopulationVariable.getTypes().add(access.getType(TypesPackage.Literals.POINTER_TYPE));
		preSynapticPopulationVariable.getInitialValues().put(access.getType(TypesPackage.Literals.POINTER_TYPE), PointerUtility.getPointer(prePopulationVariable, prePopulationType, null));
		projectionType.getVariables().add(preSynapticPopulationVariable);

		// Create post synaptic population
		ArrayType postPopulationType = (ArrayType) types.get(Resources.POPULATION.getId() + projection.getAttributeValue(Resources.POST_SYNAPTIC_POPULATION.getId()));
		Variable postPopulationVariable = null;
		for(Variable variable : compositeType.getVariables())
		{
			if(variable.getId().equals(postPopulationType.getId()))
			{
				postPopulationVariable = variable;
				break;
			}
		}
		Variable postSynapticPopulationVariable = variablesFactory.createVariable();
		NeuroMLModelInterpreterUtils.initialiseNodeFromString(postSynapticPopulationVariable, Resources.POST_SYNAPTIC_POPULATION.getId());
		postSynapticPopulationVariable.getTypes().add(access.getType(TypesPackage.Literals.POINTER_TYPE));
		postSynapticPopulationVariable.getInitialValues().put(access.getType(TypesPackage.Literals.POINTER_TYPE), PointerUtility.getPointer(postPopulationVariable, postPopulationType, null));
		projectionType.getVariables().add(postSynapticPopulationVariable);

		for(Component projectionChild : projection.getChildHM().values())
		{
			CompositeType anonymousCompositeType = extractInfoFromComponent(projectionChild, null);
			if(anonymousCompositeType != null)
			{
				Variable variable = variablesFactory.createVariable();
				NeuroMLModelInterpreterUtils.initialiseNodeFromComponent(variable, projectionChild);
				variable.getAnonymousTypes().add(anonymousCompositeType);
				projectionType.getVariables().add(variable);
			}
		}

		// Iterate over all the children. Most of them are connections
		for(Component projectionChild : projection.getStrictChildren())
		{
			if(projectionChild.getComponentType().isOrExtends(Resources.CONNECTION.getId()) || projectionChild.getComponentType().isOrExtends(Resources.CONNECTION_WD.getId()))
			{
				ConnectionType connectionType = (ConnectionType) typeFactory.getType(ResourcesDomainType.CONNECTION.getId());
				NeuroMLModelInterpreterUtils.initialiseNodeFromComponent(connectionType, projectionChild);

				Connection connection = valuesFactory.createConnection();
				connection.setConnectivity(Connectivity.DIRECTIONAL);

				for(Attribute attribute : projectionChild.getAttributes())
				{
					if(attribute.getName().equals("preCellId"))
					{
						String preCellId = ModelInterpreterUtils.parseCellRefStringForCellNum(attribute.getValue());
						connection.getA().add(PointerUtility.getPointer(prePopulationVariable, prePopulationType, Integer.parseInt(preCellId)));
					}
					else if(attribute.getName().equals("postCellId"))
					{
						String postCellId = ModelInterpreterUtils.parseCellRefStringForCellNum(attribute.getValue());
						connection.getB().add(PointerUtility.getPointer(postPopulationVariable, postPopulationType, Integer.parseInt(postCellId)));
					}
					else
					{
						// preSegmentId, preFractionAlong, postSegmentId, postFractionAlong
						connectionType.getVariables().add(ModelInterpreterUtils.createTextTypeVariable(attribute.getName(), attribute.getValue(), access));
					}
				}

				Variable variable = variablesFactory.createVariable();
				NeuroMLModelInterpreterUtils.initialiseNodeFromComponent(variable, projectionChild);
				variable.getAnonymousTypes().add(connectionType);
				variable.getInitialValues().put(connectionType, connection);
				projectionType.getVariables().add(variable);

			}
			else
			{
				CompositeType anonymousCompositeType = extractInfoFromComponent(projectionChild, null);
				if(anonymousCompositeType != null)
				{
					Variable variable = variablesFactory.createVariable();
					NeuroMLModelInterpreterUtils.initialiseNodeFromComponent(variable, projectionChild);
					variable.getAnonymousTypes().add(anonymousCompositeType);
					projectionType.getVariables().add(variable);
				}
			}
		}

	}

	public void createPopulationTypeVariable(Component populationComponent) throws GeppettoVisitingException, LEMSException, NeuroMLException, NumberFormatException, ModelInterpreterException
	{
		Component refComponent = populationComponent.getRefComponents().get("component");
		if(!types.containsKey(refComponent.getDeclaredType() + refComponent.getID()))
		{
			types.put(refComponent.getDeclaredType() + refComponent.getID(), extractInfoFromComponent(refComponent, ResourcesDomainType.CELL.getId()));
		}
		CompositeType refCompositeType = (CompositeType) types.get(refComponent.getDeclaredType() + refComponent.getID());

		ArrayType arrayType = (ArrayType) typeFactory.getType(ResourcesDomainType.POPULATION.getId());
		NeuroMLModelInterpreterUtils.initialiseNodeFromComponent(arrayType, populationComponent);

		// If it is not of type cell, it won't have morphology and we can assume an sphere in the
		if(!refComponent.getComponentType().isOrExtends(Resources.CELL.getId()))
		{
			if(!types.containsKey("morphology_sphere"))
			{
				CompositeVisualType visualCompositeType = typesFactory.createCompositeVisualType();
				NeuroMLModelInterpreterUtils.initialiseNodeFromString(visualCompositeType, "morphology_sphere");

				Sphere sphere = valuesFactory.createSphere();
				sphere.setRadius(1.2d);
				Point point = valuesFactory.createPoint();
				point.setX(0);
				point.setY(0);
				point.setZ(0);
				sphere.setPosition(point);

				Variable variable = variablesFactory.createVariable();
				NeuroMLModelInterpreterUtils.initialiseNodeFromString(variable, refComponent.getID());
				variable.getTypes().add(access.getType(TypesPackage.Literals.VISUAL_TYPE));
				variable.getInitialValues().put(access.getType(TypesPackage.Literals.VISUAL_TYPE), sphere);

				visualCompositeType.getVariables().add(variable);

				types.put("morphology_sphere", visualCompositeType);
			}

			refCompositeType.setVisualType((VisualType) types.get("morphology_sphere"));

		}
		arrayType.setArrayType(refCompositeType);

		ArrayValue arrayValue = valuesFactory.createArrayValue();

		String populationType = populationComponent.getTypeName();
		// If it is not of type populationList we don't have to do anything in particular
		if(populationType != null && populationType.equals("populationList"))
		{

			int size = 0;
			for(Component populationChild : populationComponent.getAllChildren())
			{
				if(populationChild.getDeclaredType().equals("instance"))
				{
					Point point = null;
					for(Component instanceChild : populationChild.getAllChildren())
					{
						if(instanceChild.getDeclaredType().equals("location"))
						{
							point = valuesFactory.createPoint();
							point.setX(Double.parseDouble(instanceChild.getStringValue("x")));
							point.setY(Double.parseDouble(instanceChild.getStringValue("y")));
							point.setZ(Double.parseDouble(instanceChild.getStringValue("z")));
						}
					}

					ArrayElement arrayElement = valuesFactory.createArrayElement();
					arrayElement.setIndex(Integer.parseInt(populationChild.getID()));
					arrayElement.setPosition(point);
					arrayValue.getElements().add(arrayElement);

					size++;
				}
			}
			arrayType.setSize(size);
		}
		else
		{
			// If it has size attribute we read it otherwise we count the number of instances
			if(populationComponent.hasStringValue(Resources.SIZE.getId())) arrayType.setSize(Integer.parseInt(populationComponent.getStringValue(Resources.SIZE.getId())));
		}
		arrayType.setDefaultValue(arrayValue);
		types.put(Resources.POPULATION.getId() + populationComponent.getID(), arrayType);
	}
	
	public Map<ResourcesDomainType, List<Type>> getTypesMap()
	{
		return typeFactory.getTypesMap();
	}
}
