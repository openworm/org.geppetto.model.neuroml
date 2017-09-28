package org.geppetto.model.neuroml.summaryUtils;

import java.util.LinkedHashMap;
import java.util.Map;

import org.lemsml.exprparser.utils.SymbolExpander;
import org.lemsml.exprparser.visitors.AntlrExpressionParser;
import org.lemsml.exprparser.visitors.RenderLatex;

public class FunctionNodeHelper {
	private String name;
	private String independentVariable;
	private Double[] xRange;
	private Double deltaX;
	//context must be toposorted!!
	LinkedHashMap<String, String> context = new LinkedHashMap<String, String>(){
		private static final long serialVersionUID = 1242351L;
	{
		//null is allowed in the antlr lems grammar
		put("null", "null");
	}};
	private LinkedHashMap<String, String> expandedContext;


	public void setIndependentVariable(String x) {
		this.independentVariable = x;
		this.context.put(independentVariable, independentVariable);
	}

	public String getExpression() {
		return context.get(getName());
	}

	public void register(String variable, String value) {
		this.context.put(variable, value);
	}

	public void register(Map<String, String> ctxt) {
		this.context.putAll(ctxt);
	}

	public void deRegister(String k) {
		this.context.remove(k);
	}

	String getIndependentVariable() {
		return independentVariable;
	}

	public Double getDeltaX() {
		return deltaX;
	}

	public void setDeltaX(Double deltaX) {
		this.deltaX = deltaX;
	}

	public Double[] getxRange() {
		return xRange;
	}

	public void setxRange(Double[] xRange) {
		this.xRange = xRange;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String toString() {
		return this.getName()  + ": " + this.context;
	}

	public String toTeX() {
		RenderLatex adaptor = new RenderLatex();
		AntlrExpressionParser p = new AntlrExpressionParser(getExpression());
		return p.parseAndVisitWith(adaptor);
	}

	public String getBigFatExpression(String var){
		if(expandedContext == null){
			expandedContext = new LinkedHashMap<String, String>(context);
			SymbolExpander.expandSymbols(expandedContext);
		}
		return "f(" + independentVariable + ")="  + expandedContext.get(var);
	}



}