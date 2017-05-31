/**
 * 
 */
package org.geppetto.model.neuroml.summaryUtils;

import com.google.common.base.Joiner;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.lemsml.exprparser.utils.ExpressionParser;
import org.neuroml.export.info.model.*;
import org.neuroml.model.Base;
import org.neuroml.model.IonChannel;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.util.NeuroMLConverter;
import org.neuroml.model.util.NeuroMLException;


import org.lemsml.model.exceptions.LEMSCompilerException;

import org.lemsml.exprparser.utils.UndefinedSymbolException;
import org.lemsml.exprparser.visitors.ARenderAs;
import org.lemsml.exprparser.visitors.AntlrExpressionParser;
import org.lemsml.exprparser.visitors.RenderMathJS;
import org.lemsml.model.Case;
import org.lemsml.model.ConditionalDerivedVariable;
import org.lemsml.model.extended.Component;
import org.lemsml.model.extended.LemsNode;
import org.lemsml.model.extended.Scope;
import org.lemsml.model.extended.Symbol;
import org.neuroml.model.IonChannelHH;
import org.neuroml.model.IonChannelKS;
import org.neuroml2.model.BaseGate;
import org.neuroml2.model.NeuroML2ModelReader;
import org.neuroml2.model.Neuroml2;

/**
 * @author borismarin
 * 
 */
public class ChannelInfoExtractor2
{
	private InfoNode gates = new InfoNode();
    
	private Neuroml2 nml2Doc;
    NeuroMLConverter nmlConverter = new NeuroMLConverter();
    static NeuroML2ModelReader nmlReader2 = null;

    private NeuroML2ModelReader getNML2Reader() throws Throwable 
    {
        if (nmlReader2==null)
            nmlReader2 = new NeuroML2ModelReader();
        
        return nmlReader2;
    }

	public ChannelInfoExtractor2(IonChannel chan, NeuroMLDocument nmlDoc0) throws NeuroMLException
	{
		// TODO: use jlems to simulate channels and generate traces to plot
		// Sim simchan = Utils.convertNeuroMLToSim(chan);
        
        NeuroMLDocument nmlDoc = new NeuroMLDocument();
        nmlDoc.setId("temp");
        for (IonChannel ic: nmlDoc0.getIonChannel())
            nmlDoc.getIonChannel().add(ic);
        for (IonChannelHH ic: nmlDoc0.getIonChannelHH())
            nmlDoc.getIonChannelHH().add(ic);
        for (IonChannelKS ic: nmlDoc0.getIonChannelKS())
            nmlDoc.getIonChannelKS().add(ic);
        for (org.neuroml.model.ComponentType ct: nmlDoc0.getComponentType())
            nmlDoc.getComponentType().add(ct);
        
        String xml = nmlConverter.neuroml2ToXml(nmlDoc);
        try 
        {
            nml2Doc = getNML2Reader().read(xml);
        }
        catch (Throwable t)
        {
            throw new NeuroMLException("Error reading ion channel "+chan.getId()+" with NeuroML/LEMS v2 libraries\n\n"+xml+"\nError: "+t+";\n"+t.getStackTrace(), t);
        }
        
        for(BaseGate g: nml2Doc.getAllOfType(BaseGate.class))
        {
			InfoNode gate = new InfoNode();
            gates.put("gate " + g.getId(), gate);
			gate.put("instances", g.getInstances());
            for(Component c: g.getChildren())
            {
                try 
                {
                    if (c.getId().equals("forwardRate"))
                    {
                        String fwd = processExpression(c.getScope().resolve("r"));
                        gate.put("forward rate", getExpressionNode(fwd,chan.getId(), g.getId()));
                        //gate.put("forward rate plot", PlotNodeGenerator.createPlotNode(c.getScope().resolve("r"), -0.08, 0.1, 0.005, "V", "ms-1"));
                    }
                    else if (c.getId().equals("reverseRate"))
                    {
                        String rev = processExpression(c.getScope().resolve("r"));
                        gate.put("reverse rate", getExpressionNode(processExpression(c.getScope().resolve("r")),chan.getId(), g.getId()));
                    }
                    else if (c.getId().equals("timeCourse"))
                    {
                        gate.put("time constant", getExpressionNode(processExpression(c.getScope().resolve("t")),chan.getId(), g.getId()));
                        
                        //gate.put("time constant plot", PlotNodeGenerator.createPlotNode(tau.getExpression(), -0.08, 0.1, 0.005, "V", "ms-1"));
                    }
                    else if (c.getId().equals("steadyState"))
                    {
                        gate.put("steady state", getExpressionNode(processExpression(c.getScope().resolve("x")),chan.getId(), g.getId()));
                        //gate.put("steady state plot", PlotNodeGenerator.createPlotNode(inf.getExpression(), -0.08, 0.1, 0.005, "V", "ms-1"));
                    }
                }
                catch (Exception e)
                {
                    throw new NeuroMLException("Error reading ion channel with NeuroML/LEMS v2 libraries", e);
                }
            }
        }
        /*
		for(GateHHUndetermined g : chan.getGate())
		{
			InfoNode gate = new InfoNode();
            
			if (g.getType() == GateTypes.GATE_H_HRATES){
				HHRateProcessor rateinfo = new HHRateProcessor(g);
	
				gate.put("instances", g.getInstances());
				generateRatePlots(chan, g, gate, rateinfo);
			}
            else if (g.getType() == GateTypes.GATE_H_HTAU_INF){
                
				gate.put("instances", g.getInstances());

                HHTauInfProcessor tii = new HHTauInfProcessor(g);
                ChannelMLHHExpression inf = tii.getSteadyState();
                ChannelMLHHExpression tau = tii.getTimeCourse();

                if(inf.toString().contains("ChannelMLGenericHHExpression"))
                {
                    gate.put("steady state", new ExpressionNode(inf.toString()));
                }
                else
                {
                    gate.put("steady state ", new ExpressionNode(inf.toString(), chan.getId() + " - " + g.getId() + " - Steady State Activation", "V", "ms-1", -0.08, 0.1,
                            0.005));
                }
                if(tau.toString().contains("ChannelMLGenericHHExpression"))
                {
                    gate.put("time constant", new ExpressionNode(tau.toString()));
                }
                else
                {
                    gate.put("time constant", new ExpressionNode(tau.toString(), chan.getId() + " - " + g.getId() + " - Time Co", "V", "ms-1", -0.08, 0.1, 0.005));
                }
                gate.put("steady state plot", PlotNodeGenerator.createPlotNode(inf.getExpression(), -0.08, 0.1, 0.005, "V", "ms-1"));
                gate.put("time constant plot", PlotNodeGenerator.createPlotNode(tau.getExpression(), -0.08, 0.1, 0.005, "V", "ms-1"));
			}
			else{
				gate.put("instances", g.getInstances());
			}

			gates.put("gate " + g.getId(), gate);
		}

		for(GateHHRates g : chan.getGateHHrates())
		{
            
			HHRateProcessor rateinfo = new HHRateProcessor(g);

			InfoNode gate = new InfoNode();
			
			gate.put("instances", g.getInstances());
			generateRatePlots(chan, g, gate, rateinfo);

			gates.put("gate " + g.getId(), gate);

		}

		for(GateHHRatesInf g : chan.getGateHHratesInf())
		{
			InfoNode gateinfo = new InfoNode();

			gateinfo.put("instances", g.getInstances());
			gates.put("gate " + g.getId(), gateinfo);
		}

		for(GateHHRatesTau g : chan.getGateHHratesTau())
		{
			InfoNode gate = new InfoNode();

			gate.put("instances", g.getInstances());
			gates.put("gate " + g.getId(), gate);
		}

		for(GateHHTauInf g : chan.getGateHHtauInf())
		{
			InfoNode gate = new InfoNode();

			HHTauInfProcessor tii = new HHTauInfProcessor(g);
			ChannelMLHHExpression inf = tii.getSteadyState();
			ChannelMLHHExpression tau = tii.getTimeCourse();

			if(inf.toString().contains("ChannelMLGenericHHExpression"))
			{
				gate.put("steady state", new ExpressionNode(inf.toString()));
			}
			else
			{
				gate.put("steady state ", new ExpressionNode(inf.toString(), chan.getId() + " - " + g.getId() + " - Steady State Activation", "V", "ms-1", -0.08, 0.1,
						0.005));
			}
			if(tau.toString().contains("ChannelMLGenericHHExpression"))
			{
				gate.put("time constant", new ExpressionNode(tau.toString()));
			}
			else
			{
				gate.put("time constant", new ExpressionNode(tau.toString(), chan.getId() + " - " + g.getId() + " - Time Co", "V", "ms-1", -0.08, 0.1, 0.005));
			}
			gate.put("steady state plot", PlotNodeGenerator.createPlotNode(inf.getExpression(), -0.08, 0.1, 0.005, "V", "ms-1"));
			gate.put("time constant plot", PlotNodeGenerator.createPlotNode(tau.getExpression(), -0.08, 0.1, 0.005, "V", "ms-1"));

			gate.put("instances", g.getInstances());
			gates.put("gate " + g.getId(), gate);
		}*/
	}

	private void generateRatePlots(IonChannel chan, Base g, InfoNode gate, HHRateProcessor rateinfo)
	{

		ChannelMLHHExpression fwd = rateinfo.getForwardRate();
		ChannelMLHHExpression rev = rateinfo.getReverseRate();

		if(fwd.toString().contains("ChannelMLGenericHHExpression"))
		{
			gate.put("forward rate", new ExpressionNode(fwd.toString()));
		}
		else
		{
			gate.put("forward rate", new ExpressionNode(fwd.toString(), chan.getId() + " - " + g.getId() + " - Forward Rate", "V", "ms-1", -0.08, 0.1, 0.005));
		}
		if(rev.toString().contains("ChannelMLGenericHHExpression"))
		{
			gate.put("reverse rate", new ExpressionNode(rev.toString()));
		}
		else
		{
			gate.put("reverse rate", new ExpressionNode(rev.toString(), chan.getId() + " - " + g.getId() + " - Reverse Rate", "V", "ms-1", -0.08, 0.1, 0.005));
		}
		gate.put("forward rate plot", PlotNodeGenerator.createPlotNode(fwd.getExpression(), -0.08, 0.1, 0.005, "V", "ms-1"));
		gate.put("reverse rate plot", PlotNodeGenerator.createPlotNode(rev.getExpression(), -0.08, 0.1, 0.005, "V", "ms-1"));

	}

	public InfoNode getGates()
	{
		return gates;
	}
    
    
    private ExpressionNode getExpressionNode(String f, String ionChannelId, String gateId)
    {
        return new ExpressionNode(f, ionChannelId + " - " + gateId + " - Forward Rate", "V", "ms-1", -0.08, 0.1, 0.005);
    }
    
	private String processExpression(Symbol resolved)
			throws LEMSCompilerException, UndefinedSymbolException {

		LemsNode type = resolved.getType();
		FunctionNodeHelper f = new FunctionNodeHelper();
		f.setName(resolved.getName());
		f.register(depsToMathJS(resolved));
		f.setIndependentVariable("v");

		if (type instanceof ConditionalDerivedVariable) {
			ConditionalDerivedVariable cdv = (ConditionalDerivedVariable) resolved.getType();
			f.register(f.getName(), conditionalDVToMathJS(cdv));
		}

		return f.getExpression(f.getName());
	}
    
	public Set<String> findIndependentVariables(String expression,
			Map<String, String> context) {
		Set<String> vars = ExpressionParser.listSymbolsInExpression(expression);
		vars.removeAll(context.keySet());
		return vars;
	}

	private String conditionalDVToMathJS(ConditionalDerivedVariable cdv) {
		List<String> condsVals = new ArrayList<String>();
		String defaultCase = null;

		for (Case c : cdv.getCase()) {
			if (null == c.getCondition()) // undocumented LEMS feature: no
											// condition, "catch-all" case
				defaultCase = adaptToMathJS(c.getValueDefinition());
			else
				condsVals.add(adaptToMathJS(c.getCondition()) + " ? "
						+ adaptToMathJS(c.getValueDefinition()));
		}
		if (null != defaultCase)
			condsVals.add(defaultCase);
		else
			condsVals.add("null"); // no case satisfied, no default

		return Joiner.on(" : ").join(condsVals);
	}

	private Map<String, String> depsToMathJS(Symbol resolved)
			throws LEMSCompilerException, UndefinedSymbolException {
		Map<String, String> ret = new LinkedHashMap<String, String>();
		Scope scope = resolved.getScope();
		Map<String, String> sortedContext = scope.buildTopoSortedContext(resolved);
		for(Map.Entry<String, String> kv : sortedContext.entrySet()){
			String var = kv.getKey();
			String def = kv.getValue();
			ret.put(var, adaptToMathJS(def));
		}
		return ret;
	}

	private String adaptToMathJS(String expression) {
		ARenderAs adaptor = new RenderMathJS(nml2Doc.getSymbolToUnit());
		AntlrExpressionParser p = new AntlrExpressionParser(expression);
		return p.parseAndVisitWith(adaptor);
	}
    
    
	public static void main(String[] args) throws Exception 
    {
        
        File[] fs = new File[]{new File("src/test/resources/traub/cal.channel.nml"),
                               new File("src/test/resources/traub/kdr.channel.nml"),
                               new File("src/test/resources/traub/k2.channel.nml"),
                               new File("src/test/resources/acnet2/Kdr_pyr.channel.nml")};
        for (File f: fs)
        {
            System.out.println("===============================\nOpening: "+f.getAbsolutePath());
            NeuroMLConverter nmlc = new NeuroMLConverter();
            NeuroMLDocument nmlDocument = nmlc.loadNeuroML(f);
            IonChannel ic = nmlDocument.getIonChannel().get(0);
            
            ChannelInfoExtractor cie0 = new ChannelInfoExtractor(ic);
            System.out.println(cie0.getGates().toDetailString(" Old > "));
            ChannelInfoExtractor2 cie = new ChannelInfoExtractor2(ic, nmlDocument);
            System.out.println(cie.getGates().toDetailString(" New > "));
        }
    }

}
