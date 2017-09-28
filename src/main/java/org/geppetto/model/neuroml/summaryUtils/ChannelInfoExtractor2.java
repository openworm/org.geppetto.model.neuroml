/**
 * 
 */
package org.geppetto.model.neuroml.summaryUtils;

import java.io.File;
import org.neuroml.export.info.model.*;
import org.neuroml.model.Base;
import org.neuroml.model.GateHHRates;
import org.neuroml.model.GateHHRatesInf;
import org.neuroml.model.GateHHRatesTau;
import org.neuroml.model.GateHHTauInf;
import org.neuroml.model.GateHHUndetermined;
import org.neuroml.model.GateTypes;
import org.neuroml.model.IonChannel;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.util.NeuroMLConverter;
import org.neuroml.model.util.NeuroMLException;

/**
 * @author borismarin
 * 
 */
public class ChannelInfoExtractor2
{
	private InfoNode gates = new InfoNode();

	public ChannelInfoExtractor2(IonChannel chan) throws NeuroMLException
	{
		// TODO: use jlems to simulate channels and generate traces to plot
		// Sim simchan = Utils.convertNeuroMLToSim(chan);
        
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
		}
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
    
    
	public static void main(String[] args) throws Exception 
    {
        
        File[] fs = new File[]{new File("src/test/resources/traub/cal.channel.nml"),
                               new File("src/test/resources/traub/kdr.channel.nml"),
                               new File("src/test/resources/traub/k2.channel.nml")};
        for (File f: fs)
        {
            System.out.println("===============================\nOpening: "+f.getAbsolutePath());
            NeuroMLConverter nmlc = new NeuroMLConverter();
            NeuroMLDocument nmlDocument = nmlc.loadNeuroML(f);
            IonChannel ic = nmlDocument.getIonChannel().get(0);
            ChannelInfoExtractor2 cie = new ChannelInfoExtractor2(ic);
            System.out.println(cie.getGates().toString());
        }
    }

}
