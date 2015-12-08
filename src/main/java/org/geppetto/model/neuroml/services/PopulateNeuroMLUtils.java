package org.geppetto.model.neuroml.services;

import org.geppetto.core.model.GeppettoModelAccess;
import org.geppetto.model.neuroml.utils.modeltree.PopulateGeneralModelTreeUtils;
import org.geppetto.model.types.TypesPackage;
import org.geppetto.model.util.GeppettoVisitingException;
import org.geppetto.model.values.Text;
import org.geppetto.model.values.impl.ValuesFactoryImpl;
import org.geppetto.model.variables.Variable;
import org.geppetto.model.variables.impl.VariablesFactoryImpl;
import org.neuroml.model.Base;


public class PopulateNeuroMLUtils {
	
	static String forbiddenCharacters = "[&\\/\\\\#,+()$~%.'\":*?<>{}\\s]";
	
	public static String getUniqueName(String label, Object base){
		String id = "";
		if (base instanceof Base){
			id = ((Base)base).getId();
		}
		else if (base instanceof Integer) {
			id = String.valueOf((Integer)base);
		}
		else{
			id = ((String)base);
		}	
		return label + " - " + id;
	}
	
	public static String getUniqueId(String id, int index){
		return id + "_" + index;
	}
	
	public static String parseId(String id){
		return id.replaceAll(forbiddenCharacters, "_");
	}
	
	//  For example, ../Pop0[0] returns 0; ../Gran/0/Granule_98 returns 0; Gran/1/Granule_98 returns 1
	public static String parseCellRefStringForCellNum(String cellRef) {
	   if (cellRef.indexOf("[")>=0) {
	       return cellRef.substring(cellRef.indexOf("[")+1, cellRef.indexOf("]"));
	   } else {
	       int loc = cellRef.startsWith("../") ? 2 : 1;
	       String ref = cellRef.split("/")[loc];
	       return ref;
	   }
	}
	
	public static Variable createTextTypeVariable(String id, String value, GeppettoModelAccess access) throws GeppettoVisitingException{
		Text text = ValuesFactoryImpl.eINSTANCE.createText();
		text.setText(value);

		Variable variable = VariablesFactoryImpl.eINSTANCE.createVariable();
		variable.setId(PopulateGeneralModelTreeUtils.parseId(id));
		variable.setName(id);
		variable.getInitialValues().put(access.getType(TypesPackage.Literals.TEXT_TYPE), text);
		variable.getTypes().add(access.getType(TypesPackage.Literals.TEXT_TYPE));
		return variable;
	}
}
