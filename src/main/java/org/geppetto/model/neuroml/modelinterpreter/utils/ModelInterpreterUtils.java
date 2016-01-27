package org.geppetto.model.neuroml.modelinterpreter.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geppetto.core.model.GeppettoModelAccess;
import org.geppetto.core.services.registry.ServicesRegistry;
import org.geppetto.model.DomainModel;
import org.geppetto.model.GeppettoFactory;
import org.geppetto.model.Node;
import org.geppetto.model.neuroml.utils.Resources;
import org.geppetto.model.types.Type;
import org.geppetto.model.types.TypesPackage;
import org.geppetto.model.util.GeppettoVisitingException;
import org.geppetto.model.values.PhysicalQuantity;
import org.geppetto.model.values.Text;
import org.geppetto.model.values.Unit;
import org.geppetto.model.values.ValuesFactory;
import org.geppetto.model.variables.Variable;
import org.geppetto.model.variables.VariablesFactory;
import org.lemsml.jlems.core.expression.ParseError;
import org.lemsml.jlems.core.sim.ContentError;
import org.lemsml.jlems.core.type.Component;
import org.lemsml.jlems.core.type.Lems;

public class ModelInterpreterUtils
{

	static String forbiddenCharacters = "[&\\/\\\\#,+()$~%.'\":*?<>{}\\s]";

	static GeppettoFactory geppettoFactory = GeppettoFactory.eINSTANCE;
	static ValuesFactory valuesFactory = ValuesFactory.eINSTANCE;
	static VariablesFactory variablesFactory = VariablesFactory.eINSTANCE;

	public static String parseId(String id)
	{
		return id.replaceAll(forbiddenCharacters, "_");
	}

	// For example, ../Pop0[0] returns 0; ../Gran/0/Granule_98 returns 0; Gran/1/Granule_98 returns 1
	public static String parseCellRefStringForCellNum(String cellRef)
	{
		if(cellRef.indexOf("[") >= 0)
		{
			return cellRef.substring(cellRef.indexOf("[") + 1, cellRef.indexOf("]"));
		}
		else
		{
			int loc = cellRef.startsWith("../") ? 2 : 1;
			String ref = cellRef.split("/")[loc];
			return ref;
		}
	}

	public static Variable createTextTypeVariable(String id, String value, GeppettoModelAccess access) throws GeppettoVisitingException
	{
		Text text = valuesFactory.createText();
		text.setText(value);

		Variable variable = variablesFactory.createVariable();
		initialiseNodeFromString(variable, id);
		variable.getInitialValues().put(access.getType(TypesPackage.Literals.TEXT_TYPE), text);
		variable.getTypes().add(access.getType(TypesPackage.Literals.TEXT_TYPE));
		return variable;
	}

	public static Variable createParameterTypeVariable(String id, String value, GeppettoModelAccess access) throws GeppettoVisitingException
	{
		String regExp = "\\s*([0-9-]*\\.?[0-9]*[eE]?[-+]?[0-9]+)?\\s*(\\w*)";
		Pattern pattern = Pattern.compile(regExp);
		Matcher matcher = pattern.matcher(value);

		if(matcher.find())
		{
			PhysicalQuantity physicalQuantity = valuesFactory.createPhysicalQuantity();
			Unit unit = valuesFactory.createUnit();
			unit.setUnit(matcher.group(2));
			physicalQuantity.setUnit(unit);

			physicalQuantity.setValue(Float.parseFloat(matcher.group(1)));

			Variable variable = variablesFactory.createVariable();
			variable.getInitialValues().put(access.getType(TypesPackage.Literals.PARAMETER_TYPE), physicalQuantity);
			initialiseNodeFromString(variable, id);
			variable.getTypes().add(access.getType(TypesPackage.Literals.PARAMETER_TYPE));
			return variable;
		}
		return null;
	}

	public static Variable createExposureTypeVariable(String id, String unitSymbol, GeppettoModelAccess access) throws GeppettoVisitingException
	{
		if(unitSymbol.equals("none")) unitSymbol = "";

		PhysicalQuantity physicalQuantity = valuesFactory.createPhysicalQuantity();
		Unit unit = valuesFactory.createUnit();
		unit.setUnit(unitSymbol);
		physicalQuantity.setUnit(unit);

		Variable variable = variablesFactory.createVariable();
		variable.getInitialValues().put(access.getType(TypesPackage.Literals.STATE_VARIABLE_TYPE), physicalQuantity);
		initialiseNodeFromString(variable, id);
		variable.getTypes().add(access.getType(TypesPackage.Literals.STATE_VARIABLE_TYPE));
		return variable;
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
		node.setName(Resources.getValueById(component.getDeclaredType()) + ((component.getID() != null) ? " - " + parseId(component.getID()) : ""));
		node.setId((component.getID() != null) ? component.getID() : component.getDeclaredType());
	}

	public static void initialiseNodeFromString(Node node, String attributesName)
	{
		node.setName(Resources.getValueById(attributesName));
		node.setId(parseId(attributesName));
	}

	public static void processLems(Lems lems) throws ContentError, ParseError
	{
		lems.setResolveModeLoose();
		lems.deduplicate();
		lems.resolve();
		lems.evaluateStatic();
	}
}
