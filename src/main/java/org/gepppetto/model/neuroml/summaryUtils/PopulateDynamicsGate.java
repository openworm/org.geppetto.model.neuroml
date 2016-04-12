package org.gepppetto.model.neuroml.summaryUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.geppetto.core.model.GeppettoModelAccess;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.model.neuroml.modelInterpreterUtils.NeuroMLModelInterpreterUtils;
import org.geppetto.model.neuroml.utils.ModelInterpreterUtils;
import org.geppetto.model.types.CompositeType;
import org.geppetto.model.types.TypesFactory;
import org.geppetto.model.types.TypesPackage;
import org.geppetto.model.util.GeppettoVisitingException;
import org.geppetto.model.values.Argument;
import org.geppetto.model.values.Dynamics;
import org.geppetto.model.values.Expression;
import org.geppetto.model.values.Function;
import org.geppetto.model.values.FunctionPlot;
import org.geppetto.model.values.ValuesFactory;
import org.geppetto.model.variables.Variable;
import org.geppetto.model.variables.VariablesFactory;
import org.lemsml.jlems.core.sim.LEMSException;
import org.lemsml.model.Case;
import org.lemsml.model.ConditionalDerivedVariable;
import org.lemsml.model.exceptions.LEMSCompilerException;
import org.lemsml.model.extended.LemsNode;
import org.lemsml.model.extended.Scope;
import org.lemsml.model.extended.Symbol;
import org.neuroml.export.info.model.ExpressionNode;
import org.neuroml.export.info.model.PlotMetadataNode;
import org.neuroml.model.util.NeuroMLException;
import org.neuroml2.model.BaseGate;
import org.neuroml2.model.BaseVoltageDepRate;
import org.neuroml2.model.BaseVoltageDepTime;
import org.neuroml2.model.BaseVoltageDepVariable;
import org.neuroml2.model.Cell;
import org.neuroml2.model.IonChannelHH;
import org.neuroml2.model.NeuroML2ModelReader;
import org.neuroml2.model.Neuroml2;

import com.google.common.base.Joiner;

import expr_parser.utils.UndefinedSymbolException;
import expr_parser.visitors.ARenderAs;
import expr_parser.visitors.AntlrExpressionParser;
import expr_parser.visitors.RenderMathJS;

public class PopulateDynamicsGate
{
	TypesFactory typeFactory = TypesFactory.eINSTANCE;
	VariablesFactory variablesFactory = VariablesFactory.eINSTANCE;
	ValuesFactory valuesFactory = ValuesFactory.eINSTANCE;
	GeppettoModelAccess access;

	String neuromlContent;
	Neuroml2 neuroml;
	
	//Neuroml2 neuroml;
	Map<String, Map<String,Variable>> gatesExpression = new HashMap<String, Map<String,Variable>>();
	
	public PopulateDynamicsGate(String neuromlContent, GeppettoModelAccess access) throws Throwable{
		this.access = access;
		this.neuromlContent = neuromlContent;
	}
	
	public void getAllExpressionNodes() throws Throwable
	{
		// Create temp file.
	    File temp = File.createTempFile("tmp", ".nml");

	    // Delete temp file when program exits.
	    temp.deleteOnExit();

	    // Write to temp file
	    BufferedWriter out = new BufferedWriter(new FileWriter(temp));
	    out.write(neuromlContent);
	    out.close();
	    
	    this.neuroml = NeuroML2ModelReader.read(temp);
	    
		for(Cell cell : neuroml.getCells())
		{
			for(IonChannelHH chan : cell.getAllOfType(IonChannelHH.class))
			{
				
//				System.out.println("#############################");
//				System.out.println("channel:" + chan.getName());
				for(BaseGate gate : chan.getAllOfType(BaseGate.class))
				{
					gatesExpression.put(chan.getId() + "_" + gate.getId(), gateInfo(gate));
//					System.out.println("gate:" + gate.getName());
//					System.out.println(gateInfo(gate));
				}
//				System.out.println("#############################\n\n");
			}
		}
	}
	
	private Variable processExpression(String id, Symbol resolved) throws LEMSCompilerException, UndefinedSymbolException, GeppettoVisitingException
	{

		LemsNode type = resolved.getType();
		FunctionNodeHelper f = new FunctionNodeHelper();
		f.setName(resolved.getName());
		f.register(depsToMathJS(resolved));
		f.setIndependentVariable("v");

		if(type instanceof ConditionalDerivedVariable)
		{
			ConditionalDerivedVariable cdv = (ConditionalDerivedVariable) resolved.getType();
			f.register(f.getName(), conditionalDVToMathJS(cdv));
		}

		//return f.getBigFatExpression(f.getName()) + "\n";
		
		Argument argument = valuesFactory.createArgument();
		argument.setArgument("v");

		Expression expression = valuesFactory.createExpression();
		expression.setExpression(f.getExpression());

		Function function = valuesFactory.createFunction();
		function.setExpression(expression);
		function.getArguments().add(argument);
//		PlotMetadataNode plotMetadataNode = expressionNode.getPlotMetadataNode();
//		if(plotMetadataNode != null)
//		{
//			FunctionPlot functionPlot = valuesFactory.createFunctionPlot();
//			functionPlot.setTitle(plotMetadataNode.getPlotTitle());
//			functionPlot.setXAxisLabel(plotMetadataNode.getXAxisLabel());
//			functionPlot.setYAxisLabel(plotMetadataNode.getYAxisLabel());
//			functionPlot.setInitialValue(plotMetadataNode.getInitialValue());
//			functionPlot.setFinalValue(plotMetadataNode.getFinalValue());
//			functionPlot.setStepValue(plotMetadataNode.getStepValue());
//			function.setFunctionPlot(functionPlot);
//		}

		Dynamics dynamics = valuesFactory.createDynamics();
		dynamics.setDynamics(function);

		Variable variable = variablesFactory.createVariable();
		NeuroMLModelInterpreterUtils.initialiseNodeFromString(variable, id);
		variable.getInitialValues().put(access.getType(TypesPackage.Literals.DYNAMICS_TYPE), dynamics);
		variable.getTypes().add(access.getType(TypesPackage.Literals.DYNAMICS_TYPE));

		return variable;
	}

//	private Variable getExpressionVariable(String expressionNodeId, ExpressionNode expressionNode) throws GeppettoVisitingException
//	{
//
//		Argument argument = valuesFactory.createArgument();
//		argument.setArgument("v");
//
//		Expression expression = valuesFactory.createExpression();
//		expression.setExpression(expressionNode.getExpression());
//
//		Function function = valuesFactory.createFunction();
//		function.setExpression(expression);
//		function.getArguments().add(argument);
//		PlotMetadataNode plotMetadataNode = expressionNode.getPlotMetadataNode();
//		if(plotMetadataNode != null)
//		{
//			FunctionPlot functionPlot = valuesFactory.createFunctionPlot();
//			functionPlot.setTitle(plotMetadataNode.getPlotTitle());
//			functionPlot.setXAxisLabel(plotMetadataNode.getXAxisLabel());
//			functionPlot.setYAxisLabel(plotMetadataNode.getYAxisLabel());
//			functionPlot.setInitialValue(plotMetadataNode.getInitialValue());
//			functionPlot.setFinalValue(plotMetadataNode.getFinalValue());
//			functionPlot.setStepValue(plotMetadataNode.getStepValue());
//			function.setFunctionPlot(functionPlot);
//		}
//
//		Dynamics dynamics = valuesFactory.createDynamics();
//		dynamics.setDynamics(function);
//
//		Variable variable = variablesFactory.createVariable();
//		variable.setId(ModelInterpreterUtils.parseId(expressionNodeId));
//		variable.setName(expressionNodeId);
//		variable.getInitialValues().put(access.getType(TypesPackage.Literals.DYNAMICS_TYPE), dynamics);
//		variable.getTypes().add(access.getType(TypesPackage.Literals.DYNAMICS_TYPE));
//
//		return variable;
//	}
	
	public void addExpresionNodes(CompositeType ionChannel) throws NeuroMLException, LEMSException, GeppettoVisitingException, ModelInterpreterException
	{
		
		
		
		
		for(Variable gateVariable : ionChannel.getVariables())
		{
			
			if(gatesExpression.containsKey(ionChannel.getId() + "_" + gateVariable.getId()))
			{
				Map<String,Variable> gateExpression = gatesExpression.get(ionChannel.getId() + "_" + gateVariable.getId());
				
				CompositeType gateType = (CompositeType) gateVariable.getAnonymousTypes().get(0);
				for(Variable rateVariable : gateType.getVariables())
				{
					//ResourcesSummary gatePropertyResources = ResourcesSummary.getValueByValue(gateProperties.getKey());
					if(gateExpression.containsKey(rateVariable.getId()))
					{
						CompositeType rateType = (CompositeType) rateVariable.getAnonymousTypes().get(0);
						// Create expression node
						rateType.getVariables().add(gateExpression.get(rateVariable.getId()));
					}
				}
			}
		}
	}
	


	private Map<String,Variable> gateInfo(BaseGate gate) throws LEMSCompilerException, UndefinedSymbolException, GeppettoVisitingException
	{
		Map<String,Variable> ret = new HashMap<String,Variable>();
		for(BaseVoltageDepRate r : gate.getAllOfType(BaseVoltageDepRate.class))
		{
			ret.put(r.getName(), processExpression(r.getId(), r.getScope().resolve("r")));
		}
		for(BaseVoltageDepTime t : gate.getAllOfType(BaseVoltageDepTime.class))
		{
			ret.put(t.getName(), processExpression(t.getId(), t.getScope().resolve("t")));
		}
		for(BaseVoltageDepVariable x : gate.getAllOfType(BaseVoltageDepVariable.class))
		{
			ret.put(x.getName(), processExpression(x.getId(), x.getScope().resolve("x")));
		}

		return ret;
	}

	private Map<String, String> depsToMathJS(Symbol resolved) throws LEMSCompilerException, UndefinedSymbolException
	{
		Map<String, String> ret = new LinkedHashMap<String, String>();
		Scope scope = resolved.getScope();
		Map<String, String> sortedContext = scope.buildTopoSortedContext(resolved);
		for(Entry<String, String> kv : sortedContext.entrySet())
		{
			String var = kv.getKey();
			String def = kv.getValue();
			ret.put(var, adaptToMathJS(def));
		}
		return ret;
	}

	private String adaptToMathJS(String expression)
	{
		ARenderAs adaptor = new RenderMathJS(neuroml.getSymbolToUnit());
		AntlrExpressionParser p = new AntlrExpressionParser(expression);
		return p.parseAndVisitWith(adaptor);
	}

	private String conditionalDVToMathJS(ConditionalDerivedVariable cdv)
	{
		List<String> condsVals = new ArrayList<String>();
		String defaultCase = null;

		for(Case c : cdv.getCase())
		{
			if(null == c.getCondition()) // undocumented LEMS feature: no
											// condition, "catch-all" case
			defaultCase = adaptToMathJS(c.getValueDefinition());
			else condsVals.add(adaptToMathJS(c.getCondition()) + " ? " + adaptToMathJS(c.getValueDefinition()));
		}
		if(null != defaultCase) condsVals.add(defaultCase);
		else condsVals.add("null"); // no case satisfied, no default

		return Joiner.on(" : ").join(condsVals);
	}
	
//	private void addExpresionNodes(CompositeType ionChannel) throws NeuroMLException, LEMSException, GeppettoVisitingException, ModelInterpreterException
//	{
//		// Get lems component and convert to neuroml
//		Component component = ((Component) ionChannel.getDomainModel().getDomainModel());
//		LinkedHashMap<String, Standalone> ionChannelMap = Utils.convertLemsComponentToNeuroML(component);
//		if(ionChannelMap.get(component.getID()) instanceof IonChannel)
//		{
//			IonChannel neuromlIonChannel = (IonChannel) ionChannelMap.get(component.getID());
//			if(neuromlIonChannel != null)
//			{
//				// Create channel info extractor from export library
//				ChannelInfoExtractor channelInfoExtractor = new ChannelInfoExtractor(neuromlIonChannel);
//				InfoNode gatesNode = channelInfoExtractor.getGates();
//				for(Map.Entry<String, Object> entry : gatesNode.getProperties().entrySet())
//				{
//					String id = entry.getKey().substring(entry.getKey().lastIndexOf(" ") + 1);
//					for(Variable gateVariable : ionChannel.getVariables())
//					{
//						if(gateVariable.getId().equals(id))
//						{
//							InfoNode gateNode = (InfoNode) entry.getValue();
//							for(Map.Entry<String, Object> gateProperties : gateNode.getProperties().entrySet())
//							{
//								if(gateProperties.getValue() instanceof ExpressionNode)
//								{
//									// Match property id in export lib with neuroml id
//									ResourcesSummary gatePropertyResources = ResourcesSummary.getValueByValue(gateProperties.getKey());
//									if(gatePropertyResources != null)
//									{
//										CompositeType gateType = (CompositeType) gateVariable.getAnonymousTypes().get(0);
//										for(Variable rateVariable : gateType.getVariables())
//										{
//											if(rateVariable.getId().equals(gatePropertyResources.getNeuromlId()))
//											{
//												CompositeType rateType = (CompositeType) rateVariable.getAnonymousTypes().get(0);
//												// Create expression node
//												rateType.getVariables().add(getExpressionVariable(gateProperties.getKey(), (ExpressionNode) gateProperties.getValue()));
//											}
//										}
//
//									}
//									else
//									{
//										throw new ModelInterpreterException("No node matches summary gate rate");
//									}
//								}
//							}
//						}
//					}
//
//				}
//			}
//		}
//	}
}
