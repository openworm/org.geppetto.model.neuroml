package org.geppetto.model.neuroml.modelInterpreterUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.model.GeppettoModelAccess;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.model.GeppettoLibrary;
import org.geppetto.model.neuroml.utils.CellUtils;
import org.geppetto.model.neuroml.utils.ModelInterpreterUtils;
import org.geppetto.model.neuroml.utils.Resources;
import org.geppetto.model.neuroml.utils.ResourcesDomainType;
import org.geppetto.model.neuroml.visualUtils.ExtractVisualType;
import org.geppetto.model.types.ArrayType;
import org.geppetto.model.types.CompositeType;
import org.geppetto.model.types.CompositeVisualType;
import org.geppetto.model.types.ImportType;
import org.geppetto.model.types.Type;
import org.geppetto.model.types.TypesFactory;
import org.geppetto.model.types.TypesPackage;
import org.geppetto.model.types.VisualType;
import org.geppetto.model.util.GeppettoVisitingException;
import org.geppetto.model.values.ArrayElement;
import org.geppetto.model.values.ArrayValue;
import org.geppetto.model.values.Point;
import org.geppetto.model.values.Sphere;
import org.geppetto.model.values.ValuesFactory;
import org.geppetto.model.variables.Variable;
import org.geppetto.model.variables.VariablesFactory;
import org.lemsml.jlems.core.sim.LEMSException;
import org.lemsml.jlems.core.type.Attribute;
import org.lemsml.jlems.core.type.Component;
import org.lemsml.jlems.core.type.Exposure;
import org.lemsml.jlems.core.type.ParamValue;
import org.neuroml.export.utils.Utils;
import org.neuroml.model.Cell;
import org.neuroml.model.Segment;
import org.neuroml.model.Species;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.util.NeuroMLException;

public class PopulateTypes
{

	private static Log _logger = LogFactory.getLog(PopulateTypes.class);

	private Map<String, Type> types;

	private TypesFactory typesFactory = TypesFactory.eINSTANCE;
	private ValuesFactory valuesFactory = ValuesFactory.eINSTANCE;
	private VariablesFactory variablesFactory = VariablesFactory.eINSTANCE;

	private TypeFactory typeFactory;

	private Map<Component, List<Variable>> cellSegmentMap = new HashMap<Component, List<Variable>>();

	private GeppettoModelAccess access;

	private Map<String, Component> projections = new HashMap<String, Component>();

	private NeuroMLDocument neuroMLDocument;

	private Map<Type, Cell> geppettoCellTypesMap = new HashMap<Type, Cell>();

	public PopulateTypes(Map<String, Type> types, GeppettoModelAccess access, NeuroMLDocument neuroMLDocument)
	{
		super();
		this.types = types;
		this.typeFactory = new TypeFactory(types);
		this.access = access;
		this.neuroMLDocument = neuroMLDocument;
	}

        public CompositeType extractInfoFromComponent(Component component) throws NumberFormatException, NeuroMLException, LEMSException, GeppettoVisitingException,
			ModelInterpreterException
        {
            return extractInfoFromComponent(component, ResourcesDomainType.getValueByComponentType(component.getComponentType()));
        }

	/*
	 * Generic method to extract info from any component
	 */
        public CompositeType extractInfoFromComponent(Component component, String domainType) throws NumberFormatException, NeuroMLException, LEMSException, GeppettoVisitingException,
			ModelInterpreterException
	{
		long start = System.currentTimeMillis();

		// Create composite type depending on type of component and initialise it
		CompositeType compositeType = (CompositeType) typeFactory.createType(domainType);
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
				types.put(refComponent.getDeclaredType() + refComponent.getID(), extractInfoFromComponent(refComponent));
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
			createSynapseType(projection);
			createProjectionImportType(projection, compositeType);
		}
		for(Component projection : component.getChildrenAL("electricalProjection"))
		{
			createProjectionImportType(projection, compositeType);
		}
		for(Component projection : component.getChildrenAL("continuousProjection"))
		{
			createProjectionImportType(projection, compositeType);
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
				NeuroMLModelInterpreterUtils.createCompositeTypeFromAnnotation(compositeType, componentChild, access);
			}
			else if(componentChild.getDeclaredType().equals(Resources.NOTES.getId()))
			{
				compositeType.getVariables().add(ModelInterpreterUtils.createTextTypeVariable(Resources.NOTES.get(), componentChild.getAbout(), this.access));
			}
			else
			{
				// For the moment all the child are extracted as anonymous types
				CompositeType anonymousCompositeType = extractInfoFromComponent(componentChild);
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
					&& !componentChild.getComponentType().isOrExtends(Resources.PROJECTION.getId()) && !componentChild.getComponentType().isOrExtends(Resources.ELECTRICAL_PROJECTION.getId())
					&& !componentChild.getComponentType().isOrExtends(Resources.CONTINUOUS_PROJECTION.getId()))
			{
				// If it is not a population, a projection/connection or a morphology, let's deal with it in a generic way
				CompositeType anonymousCompositeType = extractInfoFromComponent(componentChild);
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

                boolean allSegs = false;

		// Exposures are the variables that can potentially be watched
		for(Exposure exposure : component.getComponentType().getExposures())
                    {
                        if(cellSegmentMap.containsKey(component) && cellSegmentMap.get(component).size() > 1 && (exposure.getName().equals(Resources.POTENTIAL.getId()) || exposure.getName().equals(Resources.SPIKING.getId())))
                            {
                                if(!types.containsKey(Resources.COMPARTMENT.getId()) || ((CompositeType) types.get(Resources.COMPARTMENT.getId())).getVariables().size() == 1)
                                    {
                                        // we create four compartment types, one with v+spiking only, another with caConc in addition,
                                        // and copies of these to designate root comparments (no parent) segment
                                        Resources[] compartments = {Resources.COMPARTMENT, Resources.ROOT_COMPARTMENT, Resources.CA_COMPARTMENT, Resources.CA_ROOT_COMPARTMENT};
                                        if(!types.containsKey(Resources.COMPARTMENT.getId()))
                                            {
                                                for (Resources compartment : compartments) {
                                                    CompositeType compartmentType = (CompositeType) typeFactory.createType(null);
                                                    // all our compartment types must have the id Resources.COMPARTMENT so Neuron recognizes them
                                                    NeuroMLModelInterpreterUtils.initialiseNodeFromString(compartmentType, Resources.COMPARTMENT.getId());
                                                    compartmentType.setName(compartment.getId());
                                                    types.put(compartment.getId(), compartmentType);
                                                }
                                            }

                                        if(exposure.getName().equals(Resources.POTENTIAL.getId()) || exposure.getName().equals(Resources.SPIKING.getId()))
                                            {
                                                for (Resources compartment : compartments) {
                                                    CompositeType compartmentType = (CompositeType) types.get(compartment.getId());
                                                    compartmentType.getVariables().add(ModelInterpreterUtils.createExposureTypeVariable(exposure.getName(), Utils.getSIUnitInNeuroML(exposure.getDimension()).getSymbol(), this.access));
                                                  }
                                            }
                                    }
                            }
			else
                            {
                                // only expose caConc where ca species present in cell
                                if (exposure.getName().equals(Resources.CA_CONC.getId())  ||
                                    exposure.getName().equals(Resources.CA_CONC_EXT.getId())) {
                                    if (geppettoCellTypesMap.get(compositeType) != null){
                                        for(Species species : geppettoCellTypesMap.get(compositeType).getBiophysicalProperties().getIntracellularProperties().getSpecies()) {
                                            if (species.getId().equals(Resources.CALCIUM.getId())) {

                                                // if we have not yet added caConc exposure ... this should be generalized
                                                if (((CompositeType) types.get(Resources.CA_COMPARTMENT.getId())).getVariables().size() <= 2)
                                                    {
                                                        CompositeType ca_compartment = (CompositeType) types.get(Resources.CA_COMPARTMENT.getId());
                                                        ca_compartment.getVariables().add(ModelInterpreterUtils.createExposureTypeVariable(exposure.getName(),
                                                                                                                                           Utils.getSIUnitInNeuroML(exposure.getDimension()).getSymbol(), this.access));
                                                        CompositeType ca_root_compartment = (CompositeType) types.get(Resources.CA_ROOT_COMPARTMENT.getId());
                                                        ca_root_compartment.getVariables().add(ModelInterpreterUtils.createExposureTypeVariable(exposure.getName(),
                                                                                                                                           Utils.getSIUnitInNeuroML(exposure.getDimension()).getSymbol(), this.access));
                                                    }

                                                Cell cell = getNeuroMLCell(component);

                                                CellUtils cellUtils = new CellUtils(cell);
                                                List<Segment> ca_segments = cellUtils.getSegmentsInGroup(species.getSegmentGroup());

                                                // set flag so we do not duplicate compartments later
                                                if (species.getSegmentGroup() == "all")
                                                    allSegs = true;

                                                for (Segment seg : ca_segments)
                                                    {
                                                        Variable variable = variablesFactory.createVariable();
                                                        variable.setName(Resources.getValueById(seg.getName()));
                                                        variable.setId(seg.getName() + "_" + seg.getId());
                                                        if (seg.getParent() != null) {
                                                            variable.getTypes().add(types.get(Resources.CA_COMPARTMENT.getId()));
                                                        } else {
                                                            variable.getTypes().add(types.get(Resources.CA_ROOT_COMPARTMENT.getId()));
                                                        }
                                                        boolean varExists = false;
                                                        for (Variable v : compositeType.getVariables()) {
                                                            if(v.getName() == variable.getName()) {
                                                                varExists = true;
                                                                break;
                                                            }
                                                        }
                                                        if (!varExists)
                                                            compositeType.getVariables().add(variable);
                                                    }
                                            }
                                        }
                                    }
                                } else {
                                    compositeType.getVariables().add(ModelInterpreterUtils.createExposureTypeVariable(exposure.getName(), Utils.getSIUnitInNeuroML(exposure.getDimension()).getSymbol(), this.access));
                                }
                            }
                    }

                if (!allSegs){
                    if(cellSegmentMap.containsKey(component) && cellSegmentMap.get(component).size() > 1)
                        {
                            for(Variable compartment : cellSegmentMap.get(component))
                                {
                                    Variable variable = variablesFactory.createVariable();
                                    variable.setName(Resources.getValueById(compartment.getName()));
                                    variable.setId(compartment.getId());
                                    Cell cell = getNeuroMLCell(component);
                                    for (Segment seg : cell.getMorphology().getSegment()) {
                                        if (compartment.getId().equals(seg.getName() + "_" + seg.getId()))
                                            if (seg.getParent() == null)
                                                variable.getTypes().add(types.get(Resources.ROOT_COMPARTMENT.getId()));
                                            else
                                                variable.getTypes().add(types.get(Resources.COMPARTMENT.getId()));
                                    }
                                    boolean varExists = false;
                                    for (Variable v : compositeType.getVariables()) {
                                        if(v.getId().equals(variable.getId())) {
                                            varExists = true;
                                            break;
                                        }
                                    }
                                    if (!varExists)
                                        compositeType.getVariables().add(variable);
                                }
                        }
                }

		_logger.info("Creating composite type for " + component.getID() + ", took " + (System.currentTimeMillis() - start) + "ms");

		return compositeType;

	}

	/**
	 * @param projection
	 * @param projectionType
	 * @throws NeuroMLException
	 * @throws LEMSException
	 * @throws GeppettoVisitingException
	 * @throws ModelInterpreterException
	 */
	protected void createSynapseType(Component projection) throws NeuroMLException, LEMSException, GeppettoVisitingException, ModelInterpreterException
	{
		Component synapse = projection.getRefComponents().get(Resources.SYNAPSE.getId());
		if(!types.containsKey(synapse.getDeclaredType() + synapse.getID()))
		{
			Type synapseType = extractInfoFromComponent(projection.getRefComponents().get(Resources.SYNAPSE.getId()), ResourcesDomainType.SYNAPSE.getId());
			types.put(synapse.getDeclaredType() + synapse.getID(), synapseType);
		}
	}

	private void createVisualTypeFromMorphology(Component component, CompositeType compositeType, Component morphology) throws GeppettoVisitingException, LEMSException, NeuroMLException,
			ModelInterpreterException
	{
		if(!types.containsKey(morphology.getDeclaredType() + morphology.getParent().getID() + "_" + morphology.getID()))
		{
			Cell neuroMLCell = getNeuroMLCell(component);
			ExtractVisualType extractVisualType = new ExtractVisualType(neuroMLCell, access);
			types.put(morphology.getDeclaredType() + morphology.getParent().getID() + "_" + morphology.getID(), extractVisualType.createTypeFromCellMorphology());

			cellSegmentMap.put(component, extractVisualType.getVisualObjectsSegments());
			geppettoCellTypesMap.put(compositeType, neuroMLCell);
		}
		compositeType.setVisualType((VisualType) types.get(morphology.getDeclaredType() + morphology.getParent().getID() + "_" + morphology.getID()));

	}

	/**
	 * @param projection
	 * @param compositeType
	 * @return
	 */
	public ImportType createProjectionImportType(Component projection, CompositeType compositeType)
	{
		// get/create the projection type and variable
		ImportType projectionType = null;
		if(!getTypesMap().containsKey(projection.getDeclaredType() + projection.getID()))
		{
			projectionType = getTypeFactory().createImportType(ResourcesDomainType.PROJECTION.getId());
			NeuroMLModelInterpreterUtils.initialiseNodeFromComponent(projectionType, projection);
			getTypes().put(projection.getDeclaredType() + projection.getID(), projectionType);
		}
		else
		{
			projectionType = (ImportType) getTypes().get(projection.getDeclaredType() + projection.getID());
		}

		Variable projectionVariable = variablesFactory.createVariable();
		NeuroMLModelInterpreterUtils.initialiseNodeFromComponent(projectionVariable, projection);
		projectionVariable.getTypes().add(getTypes().get(projection.getDeclaredType() + projection.getID()));
		compositeType.getVariables().add(projectionVariable);
		projections.put(projection.id, projection);
		return projectionType;
	}

	/**
	 * @param component
	 * @return the NeuroML cell corresponding to a given LEMS component
	 */
	private Cell getNeuroMLCell(Component component)
	{
		String lemsId = component.getID();
		for(Cell c : neuroMLDocument.getCell())
		{
			if(c.getId().equals(lemsId))
			{
				return c;
			}
		}
		for(Cell c : neuroMLDocument.getCell2CaPools())
		{
			if(c.getId().equals(lemsId))
			{
				return c;
			}
		}
		return null;
	}

	public void createPopulationTypeVariable(Component populationComponent) throws GeppettoVisitingException, LEMSException, NeuroMLException, NumberFormatException, ModelInterpreterException
	{
		Component refComponent = populationComponent.getRefComponents().get("component");
		if(!types.containsKey(refComponent.getDeclaredType() + refComponent.getID()))
		{
			types.put(refComponent.getDeclaredType() + refComponent.getID(), extractInfoFromComponent(refComponent, ResourcesDomainType.CELL.getId()));
		}
		CompositeType refCompositeType = (CompositeType) types.get(refComponent.getDeclaredType() + refComponent.getID());

		ArrayType arrayType = (ArrayType) typeFactory.createType(ResourcesDomainType.POPULATION.getId());
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
				} else if (populationChild.getDeclaredType().equals("annotation"))
                                    {
                                        // extract population annotation
                                        NeuroMLModelInterpreterUtils.createCompositeTypeFromAnnotation(refCompositeType, populationChild, access);
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

	public Map<String, List<Type>> getTypesMap()
	{
		return typeFactory.getTypesMap();
	}

	public TypeFactory getTypeFactory()
	{
		return typeFactory;
	}

	public Map<String, Type> getTypes()
	{
		return types;
	}

	public Type resolveType(String typeId, GeppettoLibrary library) throws ModelInterpreterException
	{

		Component projection = projections.get(typeId);
		if(projection != null)
		{
			ImportType importType = (ImportType) getTypes().get(projection.getDeclaredType() + projection.getID());
			switch(projection.getDeclaredType())
			{
				case "projection":
					PopulateProjectionTypes populateProjectionTypes = new PopulateProjectionTypes(this, access, library);
					return populateProjectionTypes.resolveProjectionImportType(projection, importType);
				case "electricalProjection":
					PopulateElectricalProjectionTypes populateElectricalProjectionTypes = new PopulateElectricalProjectionTypes(this, access, library);
					return populateElectricalProjectionTypes.resolveProjectionImportType(projection, importType);
				case "continuousProjection":
					PopulateContinuousProjectionTypes populateContinuousProjectionTypes = new PopulateContinuousProjectionTypes(this, access, library);
					return populateContinuousProjectionTypes.resolveProjectionImportType(projection, importType);
			}
		}
		throw new ModelInterpreterException("Can't resolve the type " + typeId);
	}

	public Map<Type, Cell> getGeppettoCellTypesMap()
	{
		return this.geppettoCellTypesMap;
	}

}