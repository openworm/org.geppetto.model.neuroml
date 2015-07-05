/*******************************************************************************
 * The MIT License (MIT)
 * 
 * Copyright (c) 2011 - 2015 OpenWorm.
 * http://openworm.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT License
 * which accompanies this distribution, and is available at
 * http://opensource.org/licenses/MIT
 *
 * Contributors:
 *     	OpenWorm - http://openworm.org/people.html
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights 
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
 * copies of the Software, and to permit persons to whom the Software is 
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE 
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/

package org.geppetto.model.neuroml.utils.modeltree;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.model.runtime.ANode;
import org.geppetto.core.model.runtime.CompositeNode;
import org.geppetto.core.model.runtime.FunctionNode;
import org.geppetto.core.model.runtime.ParameterSpecificationNode;
import org.geppetto.core.model.values.IntValue;
import org.geppetto.core.model.values.StringValue;
import org.geppetto.model.neuroml.utils.NeuroMLAccessUtility;
import org.geppetto.model.neuroml.utils.Resources;
import org.geppetto.model.neuroml.utils.ResourcesDomainType;
import org.lemsml.jlems.core.sim.ContentError;
import org.lemsml.jlems.core.type.ComponentType;
import org.neuroml.export.info.model.ExpressionNode;
import org.neuroml.export.info.model.InfoNode;
import org.neuroml.export.info.model.PlotMetadataNode;
import org.neuroml.export.info.model.PlotNode;
import org.neuroml.model.AdExIaFCell;
import org.neuroml.model.AlphaCondSynapse;
import org.neuroml.model.Annotation;
import org.neuroml.model.Base;
import org.neuroml.model.BaseCell;
import org.neuroml.model.BaseConductanceBasedSynapse;
import org.neuroml.model.BasePyNNCell;
import org.neuroml.model.BasePyNNIaFCell;
import org.neuroml.model.BasePynnSynapse;
import org.neuroml.model.BiophysicalProperties;
import org.neuroml.model.BlockMechanism;
import org.neuroml.model.BlockingPlasticSynapse;
import org.neuroml.model.Cell;
import org.neuroml.model.ChannelDensity;
import org.neuroml.model.ChannelDensityGHK;
import org.neuroml.model.ChannelDensityNernst;
import org.neuroml.model.ChannelDensityNonUniform;
import org.neuroml.model.ChannelDensityNonUniformNernst;
import org.neuroml.model.ChannelPopulation;
import org.neuroml.model.DecayingPoolConcentrationModel;
import org.neuroml.model.EIFCondAlphaIsfaIsta;
import org.neuroml.model.EIFCondExpIsfaIsta;
import org.neuroml.model.ExpCondSynapse;
import org.neuroml.model.ExpOneSynapse;
import org.neuroml.model.ExpTwoSynapse;
import org.neuroml.model.ExtracellularProperties;
import org.neuroml.model.FitzHughNagumoCell;
import org.neuroml.model.FixedFactorConcentrationModel;
import org.neuroml.model.GateHHRates;
import org.neuroml.model.GateHHRatesInf;
import org.neuroml.model.GateHHRatesTau;
import org.neuroml.model.GateHHRatesTauInf;
import org.neuroml.model.GateHHTauInf;
import org.neuroml.model.GateHHUndetermined;
import org.neuroml.model.HHCondExp;
import org.neuroml.model.HHRate;
import org.neuroml.model.HHTime;
import org.neuroml.model.HHVariable;
import org.neuroml.model.IFCondAlpha;
import org.neuroml.model.IFCondExp;
import org.neuroml.model.IFCurrAlpha;
import org.neuroml.model.IFCurrExp;
import org.neuroml.model.IafCell;
import org.neuroml.model.IafRefCell;
import org.neuroml.model.IafTauCell;
import org.neuroml.model.IafTauRefCell;
import org.neuroml.model.InitMembPotential;
import org.neuroml.model.Instance;
import org.neuroml.model.IntracellularProperties;
import org.neuroml.model.IonChannel;
import org.neuroml.model.IzhikevichCell;
import org.neuroml.model.MembraneProperties;
import org.neuroml.model.Network;
import org.neuroml.model.PlasticityMechanism;
import org.neuroml.model.Population;
import org.neuroml.model.PopulationTypes;
import org.neuroml.model.PulseGenerator;
import org.neuroml.model.Q10Settings;
import org.neuroml.model.Resistivity;
import org.neuroml.model.Species;
import org.neuroml.model.SpecificCapacitance;
import org.neuroml.model.SpikeThresh;
import org.neuroml.model.Standalone;
import org.neuroml.model.VariableParameter;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Populates the Model Tree of Aspect
 * 
 * @author Adrian Quintana (adrian.perez@ucl.ac.uk)
 * 
 */

public class PopulateNeuroMLModelTreeUtils
{

	private NeuroMLAccessUtility neuroMLAccessUtility = new NeuroMLAccessUtility();

	// Maps parameter spec node to setter method
	private Map<ParameterSpecificationNode, Object> _parameterNodesToObjectMap = new HashMap<ParameterSpecificationNode, Object>();

	// Maps parameter spec node to instance object in NeuroML object
	private Map<ParameterSpecificationNode, Object> _parameterNodesToMethodsMap = new HashMap<ParameterSpecificationNode, Object>();

	private ModelWrapper model;

	public ModelWrapper getModel()
	{
		return model;
	}

	public void setModel(ModelWrapper model)
	{
		this.model = model;
	}

	public Map<ParameterSpecificationNode, Object> getParametersNodeToMethodsMap()
	{
		return _parameterNodesToMethodsMap;
	}

	public Map<ParameterSpecificationNode, Object> getParametersNodeToObjectsMap()
	{
		return _parameterNodesToObjectMap;
	}

	public CompositeNode createRateGateNode(Resources name, HHRate rate) throws ModelInterpreterException, ContentError
	{
		if(rate != null)
		{
			CompositeNode rateGateNode = new CompositeNode(name.getId(), name.get());
			if(rate.getType() != null)
			{
				rateGateNode.addChild(PopulateLEMSModelTreeUtils.createCompositeNodeFromComponentType(rate.getType(), rate.getType(),
						(ComponentType) neuroMLAccessUtility.getComponent(rate.getType(), model, Resources.COMPONENT_TYPE)));
			}
			ParameterSpecificationNode rateN = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.RATE.getId(), Resources.RATE.get(), rate.getRate());
			rateGateNode.addChild(rateN);
			this.addToMaps(rate, "setRate", rateN);
			ParameterSpecificationNode mid = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.MIDPOINT.getId(), Resources.MIDPOINT.get(), rate.getMidpoint());
			rateGateNode.addChild(mid);
			this.addToMaps(rate, "setMidpoint", mid);
			ParameterSpecificationNode scale = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.SCALE.getId(), Resources.SCALE.get(), rate.getScale());
			rateGateNode.addChild(scale);
			this.addToMaps(rate, "setScale", scale);

			return rateGateNode;
		}
		return null;
	}

	public CompositeNode createSteadyStateNode(HHVariable variable) throws ModelInterpreterException, ContentError
	{
		if(variable != null)
		{
			CompositeNode steadyStateNode = new CompositeNode(Resources.STEADY_STATE.getId(), Resources.STEADY_STATE.get());

			if(variable.getType() != null)
			{
				ComponentType typeSteadyState = (ComponentType) neuroMLAccessUtility.getComponent(variable.getType(), model, Resources.COMPONENT_TYPE);
				steadyStateNode.addChild(PopulateLEMSModelTreeUtils.createCompositeNodeFromComponentType(variable.getType(), variable.getType(), typeSteadyState));
			}
			if(variable.getRate() != null)
			{
				ParameterSpecificationNode rateN = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.RATE.getId(), Resources.RATE.get(), Float.toString(variable.getRate()));
				steadyStateNode.addChild(rateN);
				this.addToMaps(variable, "setRate", rateN);
			}
			ParameterSpecificationNode midPoint = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.MIDPOINT.getId(), Resources.MIDPOINT.get(), variable.getMidpoint());
			steadyStateNode.addChild(midPoint);
			this.addToMaps(variable, "setMidpoint", midPoint);
			ParameterSpecificationNode scale = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.SCALE.getId(), Resources.SCALE.get(), variable.getScale());
			steadyStateNode.addChild(scale);
			this.addToMaps(variable, "setScale", scale);
			return steadyStateNode;
		}
		return null;
	}

	public CompositeNode createTimeCourseNode(HHTime timeCourse) throws ModelInterpreterException, ContentError
	{

		if(timeCourse != null)
		{
			CompositeNode timeCourseNode = new CompositeNode(Resources.TIMECOURSE.getId(), Resources.TIMECOURSE.get());

			if(timeCourse.getType() != null)
			{
				ComponentType typeTimeCourse = (ComponentType) neuroMLAccessUtility.getComponent(timeCourse.getType(), model, Resources.COMPONENT_TYPE);
				timeCourseNode.addChild(PopulateLEMSModelTreeUtils.createCompositeNodeFromComponentType(timeCourse.getType(), timeCourse.getType(), typeTimeCourse));
			}
			ParameterSpecificationNode rate = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.RATE.getId(), Resources.RATE.get(), timeCourse.getRate());
			timeCourseNode.addChild(rate);
			this.addToMaps(timeCourse, "setRate", rate);
			ParameterSpecificationNode midpoint = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.MIDPOINT.getId(), Resources.MIDPOINT.get(), timeCourse.getMidpoint());
			timeCourseNode.addChild(midpoint);
			this.addToMaps(timeCourse, "setMidpoint", midpoint);
			ParameterSpecificationNode scale = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.SCALE.getId(), Resources.SCALE.get(), timeCourse.getScale());
			timeCourseNode.addChild(scale);
			this.addToMaps(timeCourse, "setScale", scale);
			ParameterSpecificationNode tau = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.TAU.getId(), Resources.TAU.get(), timeCourse.getTau());
			timeCourseNode.addChild(tau);
			this.addToMaps(timeCourse, "setTau", tau);

			return timeCourseNode;
		}
		return null;
	}

	public CompositeNode createCellNode(BaseCell c) throws ModelInterpreterException, ContentError
	{
		CompositeNode cellNode = new CompositeNode(Resources.CELL.getId(), PopulateGeneralModelTreeUtils.getUniqueName(Resources.CELL.get(), c));
		cellNode.setDomainType(ResourcesDomainType.CELL.get());
		// Cell types
		if(c instanceof Cell)
		{
			Cell cell = (Cell) c;
			if(cell.getBiophysicalProperties() != null)
			{
				cellNode.addChild(createBiophysicalPropertiesNode(cell.getBiophysicalProperties()));
			}
		}
		else if(c instanceof AdExIaFCell)
		{
			AdExIaFCell cell = (AdExIaFCell) c;
			ParameterSpecificationNode capacitance = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.CAPACITANCE.getId(), Resources.CAPACITANCE.get(), cell.getC());
			cellNode.addChild(capacitance);
			this.addToMaps(cell, "setC", capacitance);
			ParameterSpecificationNode el = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.EL.getId(), Resources.EL.get(), cell.getEL());
			cellNode.addChild(el);
			this.addToMaps(cell, "setEL", el);
			ParameterSpecificationNode vt = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.VT.getId(), Resources.VT.get(), cell.getVT());
			cellNode.addChild(vt);
			this.addToMaps(cell, "setVT", vt);
			ParameterSpecificationNode a = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.A.getId(), Resources.A.get(), cell.getA());
			cellNode.addChild(a);
			this.addToMaps(cell, "setA", a);
			ParameterSpecificationNode b = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.B.getId(), Resources.B.get(), cell.getB());
			cellNode.addChild(b);
			this.addToMaps(cell, "setB", b);
			ParameterSpecificationNode delt = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.DELT.getId(), Resources.DELT.get(), cell.getDelT());
			cellNode.addChild(delt);
			this.addToMaps(cell, "setDelT", delt);
			ParameterSpecificationNode gl = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.GL.getId(), Resources.GL.get(), cell.getGL());
			cellNode.addChild(gl);
			this.addToMaps(cell, "setGL", gl);
			ParameterSpecificationNode refract = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.REFRACT.getId(), Resources.REFRACT.get(), cell.getRefract());
			cellNode.addChild(refract);
			this.addToMaps(cell, "setRefract", refract);
			ParameterSpecificationNode reset = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.RESET.getId(), Resources.RESET.get(), cell.getReset());
			cellNode.addChild(reset);
			this.addToMaps(cell, "setReset", reset);
			ParameterSpecificationNode tauw = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.TAUW.getId(), Resources.TAUW.get(), cell.getTauw());
			cellNode.addChild(tauw);
			this.addToMaps(cell, "setTauw", tauw);
			ParameterSpecificationNode thresh = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.THRESH.getId(), Resources.THRESH.get(), cell.getThresh());
			cellNode.addChild(thresh);
			this.addToMaps(cell, "setThresh", thresh);
		}
		else if(c instanceof FitzHughNagumoCell)
		{
			FitzHughNagumoCell cell = (FitzHughNagumoCell) c;
			ParameterSpecificationNode i = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.I.getId(), Resources.I.get(), cell.getI());
			cellNode.addChild(i);
			this.addToMaps(cell, "setI", i);
		}
		else if(c instanceof IzhikevichCell)
		{
			IzhikevichCell cell = (IzhikevichCell) c;
			ParameterSpecificationNode aNode = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.A.getId(), Resources.A.get(), cell.getA());
			cellNode.addChild(aNode);
			this.addToMaps(cell, "setA", aNode);
			ParameterSpecificationNode bNode = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.B.getId(), Resources.B.get(), cell.getB());
			cellNode.addChild(bNode);
			this.addToMaps(cell, "setB", bNode);
			ParameterSpecificationNode cNode = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.C.getId(), Resources.C.get(), cell.getC());
			cellNode.addChild(cNode);
			this.addToMaps(cell, "setC", cNode);
			ParameterSpecificationNode dNode = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.D.getId(), Resources.D.get(), cell.getD());
			cellNode.addChild(dNode);
			this.addToMaps(cell, "setD", dNode);
			ParameterSpecificationNode vNode = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.v0.getId(), Resources.v0.get(), cell.getV0());
			cellNode.addChild(vNode);
			this.addToMaps(cell, "setV0", vNode);
			ParameterSpecificationNode thresh = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.THRESH.getId(), Resources.THRESH.get(), cell.getThresh());
			cellNode.addChild(thresh);
			this.addToMaps(cell, "setThresh", thresh);
		}
		else if(c instanceof IafRefCell)
		{
			IafRefCell cell = (IafRefCell) c;
			cellNode.addChildren(createIafCellChildren(cell));
			ParameterSpecificationNode refract = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.REFRACT.getId(), Resources.REFRACT.get(), cell.getRefract());
			cellNode.addChild(refract);
			this.addToMaps(cell, "setRefract", refract);
		}
		else if(c instanceof IafCell)
		{
			IafCell cell = (IafCell) c;
			cellNode.addChildren(createIafCellChildren(cell));
		}
		else if(c instanceof IafTauRefCell)
		{
			IafTauRefCell cell = (IafTauRefCell) c;
			cellNode.addChildren(createIafTauCellChildren(cell));
			ParameterSpecificationNode refract = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.REFRACT.getId(), Resources.REFRACT.get(), cell.getRefract());
			cellNode.addChild(refract);
			this.addToMaps(cell, "setRefract", refract);
		}
		else if(c instanceof IafTauCell)
		{
			IafTauCell cell = (IafTauCell) c;
			cellNode.addChildren(createIafTauCellChildren(cell));
		}
		// Pynn cell types
		else if(c instanceof IFCurrAlpha)
		{
			IFCurrAlpha cell = (IFCurrAlpha) c;
			cellNode.addChildren(createBasePyNNIaFCellChildren(cell));
		}
		else if(c instanceof IFCurrExp)
		{
			IFCurrExp cell = (IFCurrExp) c;
			cellNode.addChildren(createBasePyNNIaFCellChildren(cell));
		}
		else if(c instanceof IFCondAlpha)
		{
			IFCondAlpha cell = (IFCondAlpha) c;
			cellNode.addChildren(createBasePyNNIaFCellChildren(cell));
		}
		else if(c instanceof IFCondExp)
		{
			IFCondExp cell = (IFCondExp) c;
			cellNode.addChildren(createBasePyNNIaFCellChildren(cell));
		}
		else if(c instanceof EIFCondExpIsfaIsta)
		{
			EIFCondExpIsfaIsta cell = (EIFCondExpIsfaIsta) c;
			ParameterSpecificationNode a = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.A.getId(), Resources.A.get(), cell.getA().toString());
			cellNode.addChild(a);
			this.addToMaps(cell, "setA", a);
			ParameterSpecificationNode b = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.B.getId(), Resources.B.get(), cell.getB().toString());
			cellNode.addChild(b);
			this.addToMaps(cell, "setB", b);
			ParameterSpecificationNode deltat = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.DELTAT.getId(), Resources.DELTAT.get(), cell.getDeltaT().toString());
			cellNode.addChild(deltat);
			this.addToMaps(cell, "setDeltaT", deltat);
			ParameterSpecificationNode tauw = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.TAUW.getId(), Resources.TAUW.get(), cell.getTauW().toString());
			cellNode.addChild(tauw);
			this.addToMaps(cell, "setTauW", tauw);
			ParameterSpecificationNode spike = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.V_SPIKE.getId(), Resources.V_SPIKE.get(), cell.getVSpike().toString());
			cellNode.addChild(spike);
			this.addToMaps(cell, "setVSpike", spike);
			cellNode.addChildren(createBasePyNNIaFCellChildren(cell));
		}
		else if(c instanceof EIFCondAlphaIsfaIsta)
		{
			EIFCondAlphaIsfaIsta cell = (EIFCondAlphaIsfaIsta) c;
			ParameterSpecificationNode a = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.A.getId(), Resources.A.get(), cell.getA().toString());
			cellNode.addChild(a);
			this.addToMaps(cell, "setA", a);
			ParameterSpecificationNode b = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.B.getId(), Resources.B.get(), cell.getB().toString());
			cellNode.addChild(b);
			this.addToMaps(cell, "setB", b);
			ParameterSpecificationNode deltat = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.DELTAT.getId(), Resources.DELTAT.get(), cell.getDeltaT().toString());
			cellNode.addChild(deltat);
			this.addToMaps(cell, "setDeltaT", deltat);
			ParameterSpecificationNode tauw = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.TAUW.getId(), Resources.TAUW.get(), cell.getTauW().toString());
			cellNode.addChild(tauw);
			this.addToMaps(cell, "setTauW", tauw);
			ParameterSpecificationNode spike = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.V_SPIKE.getId(), Resources.V_SPIKE.get(), cell.getVSpike().toString());
			cellNode.addChild(spike);
			this.addToMaps(cell, "setVSpike", spike);
			cellNode.addChildren(createBasePyNNIaFCellChildren(cell));
		}
		else if(c instanceof HHCondExp)
		{
			HHCondExp cell = (HHCondExp) c;
			ParameterSpecificationNode v = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.V_OFFSET.getId(), Resources.V_OFFSET.get(), cell.getVOffset().toString());
			cellNode.addChild(v);
			this.addToMaps(cell, "setVOffset", v);
			ParameterSpecificationNode reve = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.E_REV_E.getId(), Resources.E_REV_E.get(), cell.getERevE().toString());
			cellNode.addChild(reve);
			this.addToMaps(cell, "setERevE", reve);
			ParameterSpecificationNode revi = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.E_REV_I.getId(), Resources.E_REV_I.get(), cell.getERevI().toString());
			cellNode.addChild(revi);
			this.addToMaps(cell, "setERevI", revi);
			ParameterSpecificationNode revk = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.E_REV_K.getId(), Resources.E_REV_K.get(), cell.getERevK().toString());
			cellNode.addChild(revk);
			this.addToMaps(cell, "setERevK", revk);
			ParameterSpecificationNode revna = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.E_REV_NA.getId(), Resources.E_REV_NA.get(), cell.getERevNa().toString());
			cellNode.addChild(revna);
			this.addToMaps(cell, "setERevNa", revna);
			ParameterSpecificationNode revleak = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.E_REV_LEAK.getId(), Resources.E_REV_LEAK.get(), cell.getERevLeak().toString());
			cellNode.addChild(revleak);
			this.addToMaps(cell, "setERevLeak", revleak);
			ParameterSpecificationNode gleak = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.G_LEAK.getId(), Resources.G_LEAK.get(), cell.getGLeak().toString());
			cellNode.addChild(gleak);
			this.addToMaps(cell, "setGLeak", gleak);
			ParameterSpecificationNode gbark = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.G_BARK.getId(), Resources.G_BARK.get(), cell.getGbarK().toString());
			cellNode.addChild(gbark);
			this.addToMaps(cell, "setGbarK", gbark);
			ParameterSpecificationNode gbarkna = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.G_BAR_NA.getId(), Resources.G_BAR_NA.get(), cell.getGbarNa().toString());
			cellNode.addChild(gbarkna);
			this.addToMaps(cell, "setGbarNa", gbarkna);
			cellNode.addChildren(createBasePyNNCellChildren(cell));
		}

		cellNode.addChildren(createStandaloneChildren(c));

		return cellNode;
	}

	public Collection<ParameterSpecificationNode> createBasePyNNIaFCellChildren(BasePyNNIaFCell c) throws ModelInterpreterException
	{
		Collection<ParameterSpecificationNode> basePyNNIaFCellChildren = new ArrayList<ParameterSpecificationNode>();
		ParameterSpecificationNode tauM = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.TAU_M.getId(), Resources.TAU_M.get(), c.getTauM().toString());
		basePyNNIaFCellChildren.add(tauM);
		this.addToMaps(c, "setTauM", tauM);
		ParameterSpecificationNode tauR = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.TAU_REFRAC.getId(), Resources.TAU_REFRAC.get(), c.getTauRefrac().toString());
		basePyNNIaFCellChildren.add(tauR);
		this.addToMaps(c, "setTauRefrac", tauR);
		ParameterSpecificationNode reset = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.V_RESET.getId(), Resources.V_RESET.get(), c.getVReset().toString());
		basePyNNIaFCellChildren.add(reset);
		this.addToMaps(c, "setVReset", reset);
		ParameterSpecificationNode rest = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.V_REST.getId(), Resources.V_REST.get(), c.getVRest().toString());
		basePyNNIaFCellChildren.add(rest);
		this.addToMaps(c, "setVRest", rest);
		ParameterSpecificationNode thresh = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.V_THRESH.getId(), Resources.V_THRESH.get(), c.getVThresh().toString());
		basePyNNIaFCellChildren.add(thresh);
		this.addToMaps(c, "setVThresh", thresh);

		basePyNNIaFCellChildren.addAll(createBasePyNNCellChildren(c));

		return basePyNNIaFCellChildren;
	}

	public Collection<ParameterSpecificationNode> createBasePyNNCellChildren(BasePyNNCell c) throws ModelInterpreterException
	{
		Collection<ParameterSpecificationNode> basePyNNCellChildren = new ArrayList<ParameterSpecificationNode>();
		ParameterSpecificationNode cm = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.CM.getId(), Resources.CM.get(), c.getCm().toString());
		basePyNNCellChildren.add(cm);
		this.addToMaps(c, "setCm", cm);
		ParameterSpecificationNode offset = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.I_OFFSET.getId(), Resources.I_OFFSET.get(), c.getIOffset().toString());
		basePyNNCellChildren.add(offset);
		this.addToMaps(c, "setIOffset", offset);
		ParameterSpecificationNode tau = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.TAU_SYN_E.getId(), Resources.TAU_SYN_E.get(), c.getTauSynE().toString());
		basePyNNCellChildren.add(tau);
		this.addToMaps(c, "setTauSynE", tau);
		ParameterSpecificationNode tau2 = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.TAU_SYN_I.getId(), Resources.TAU_SYN_I.get(), c.getTauSynI().toString());
		basePyNNCellChildren.add(tau2);
		this.addToMaps(c, "setTauSynI", tau2);
		ParameterSpecificationNode vinit = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.V_INIT.getId(), Resources.V_INIT.get(), c.getVInit().toString());
		basePyNNCellChildren.add(vinit);
		this.addToMaps(c, "setVInit", vinit);
		return basePyNNCellChildren;
	}

	public CompositeNode createPulseGeneratorNode(PulseGenerator pulseGenerator) throws ModelInterpreterException
	{

		CompositeNode pulseGeneratorNode = new CompositeNode(pulseGenerator.getId(), Resources.PULSE_GENERATOR.get());

		// Amplitude
		ParameterSpecificationNode amplitude = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.AMPLITUDE.getId(), Resources.AMPLITUDE.get(), pulseGenerator.getAmplitude());
		pulseGeneratorNode.addChild(amplitude);
		this.addToMaps(pulseGenerator, "setAmplitude", amplitude);

		// Delay
		ParameterSpecificationNode delay = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.DELAY.getId(), Resources.DELAY.get(), pulseGenerator.getDelay());
		pulseGeneratorNode.addChild(delay);
		this.addToMaps(pulseGenerator, "setDelay", delay);

		// Amplitude
		ParameterSpecificationNode duration = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.DURATION.getId(), Resources.DURATION.get(), pulseGenerator.getDuration());
		pulseGeneratorNode.addChild(duration);
		this.addToMaps(pulseGenerator, "setDuration", duration);

		// Standalone Properties
		pulseGeneratorNode.addChildren(createStandaloneChildren(pulseGenerator));

		// TODO Auto-generated method stub
		return pulseGeneratorNode;
	}

	public CompositeNode createBiophysicalPropertiesNode(BiophysicalProperties biophysicalProperties) throws ModelInterpreterException, ContentError
	{
		CompositeNode biophysicalPropertiesNode = new CompositeNode(biophysicalProperties.getId(), Resources.BIOPHYSICAL_PROPERTIES.get());

		// Membrane Properties
		MembraneProperties membraneProperties = biophysicalProperties.getMembraneProperties();
		if(membraneProperties != null)
		{
			CompositeNode membranePropertiesNode = new CompositeNode(Resources.MEMBRANE_P.getId(), Resources.MEMBRANE_P.get());

			// Channel Population
			List<ChannelPopulation> channelPopulations = membraneProperties.getChannelPopulation();
			for(ChannelPopulation channelPopulation : channelPopulations)
			{
				CompositeNode channelPopulationNode = new CompositeNode(channelPopulation.getId(), PopulateGeneralModelTreeUtils.getUniqueName(Resources.CHANNEL_DENSITY.get(), channelPopulation));

				// Ion Channel
				channelPopulationNode.addChild(createChannelNode((IonChannel) neuroMLAccessUtility.getComponent(channelPopulation.getIonChannel(), model, Resources.ION_CHANNEL)));

				// Ion
				channelPopulationNode.addChild(PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.ION.getId(), Resources.ION.get(), new StringValue(channelPopulation.getIon())));

				// Reverse Potential
				ParameterSpecificationNode erev = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.EREV.getId(), Resources.EREV.get(), channelPopulation.getErev());
				channelPopulationNode.addChild(erev);
				this.addToMaps(channelPopulation, "setErev", erev);

				// Number
				channelPopulationNode.addChild(PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.NUMBER_CHANNEL.getId(), Resources.NUMBER_CHANNEL.get(), new IntValue(channelPopulation
						.getNumber().intValue())));

				// Variable Parameter
				channelPopulationNode.addChild(createVariableParameterNode(channelPopulation.getVariableParameter()));

				// Base
				channelPopulationNode.addChildren(createBaseChildren(channelPopulation));

				membranePropertiesNode.addChild(channelPopulationNode);
			}

			// Channel Density
			List<ChannelDensity> channelDensities = membraneProperties.getChannelDensity();
			for(ChannelDensity channelDensity : channelDensities)
			{
				CompositeNode channelDensityNode = new CompositeNode(channelDensity.getId(), PopulateGeneralModelTreeUtils.getUniqueName(Resources.CHANNEL_DENSITY.get(), channelDensity));

				// Ion Channel
				channelDensityNode.addChild(createChannelNode((IonChannel) neuroMLAccessUtility.getComponent(channelDensity.getIonChannel(), model, Resources.ION_CHANNEL)));

				ParameterSpecificationNode pcd = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.COND_DENSITY.getId(), Resources.COND_DENSITY.get(),
						channelDensity.getCondDensity());
				// Passive conductance density
				channelDensityNode.addChild(pcd);

				this.addToMaps(channelDensity, "setCondDensity", pcd);

				// Ion
				channelDensityNode.addChild(PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.ION.getId(), Resources.ION.get(), new StringValue(channelDensity.getIon())));

				ParameterSpecificationNode erev = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.EREV.getId(), Resources.EREV.get(), channelDensity.getErev());
				// Reverse Potential
				channelDensityNode.addChild(erev);

				this.addToMaps(channelDensity, "setErev", erev);

				// Variable Parameter
				channelDensityNode.addChild(createVariableParameterNode(channelDensity.getVariableParameter()));

				// Base
				channelDensityNode.addChildren(createBaseChildren(channelDensity));

				membranePropertiesNode.addChild(channelDensityNode);
			}

			// Channel Density Nernst
			List<ChannelDensityNernst> channelDensitiesNernst = membraneProperties.getChannelDensityNernst();
			for(ChannelDensityNernst channelDensityNernst : channelDensitiesNernst)
			{
				CompositeNode channelDensityNernstNode = new CompositeNode(channelDensityNernst.getId(), PopulateGeneralModelTreeUtils.getUniqueName(Resources.CHANNEL_DENSITY_NERNST.get(),
						channelDensityNernst));

				// Ion Channel
				channelDensityNernstNode.addChild(createChannelNode((IonChannel) neuroMLAccessUtility.getComponent(channelDensityNernst.getIonChannel(), model, Resources.ION_CHANNEL)));

				ParameterSpecificationNode density = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.COND_DENSITY.getId(), Resources.COND_DENSITY.get(),
						channelDensityNernst.getCondDensity());
				// Passive conductance density
				channelDensityNernstNode.addChild(density);

				this.addToMaps(channelDensityNernstNode, "setCondDensity", density);

				// Ion
				channelDensityNernstNode.addChild(PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.ION.getId(), Resources.ION.get(), new StringValue(channelDensityNernst.getIon())));

				// Variable Parameter
				channelDensityNernstNode.addChild(createVariableParameterNode(channelDensityNernst.getVariableParameter()));

				// Base
				channelDensityNernstNode.addChildren(createBaseChildren(channelDensityNernst));

				membranePropertiesNode.addChild(channelDensityNernstNode);
			}

			// Channel Density GHK
			List<ChannelDensityGHK> channelDensitiesGHK = membraneProperties.getChannelDensityGHK();
			for(ChannelDensityGHK channelDensityGHK : channelDensitiesGHK)
			{
				CompositeNode channelDensityGHKNode = new CompositeNode(channelDensityGHK.getId(), PopulateGeneralModelTreeUtils.getUniqueName(Resources.CHANNEL_DENSITY_GHK.get(), channelDensityGHK));

				// Ion Channel
				channelDensityGHKNode.addChild(createChannelNode((IonChannel) neuroMLAccessUtility.getComponent(channelDensityGHK.getIonChannel(), model, Resources.ION_CHANNEL)));

				ParameterSpecificationNode perm = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.PERMEABILITY.getId(), Resources.PERMEABILITY.get(),
						channelDensityGHK.getPermeability());
				// Permeability
				channelDensityGHKNode.addChild(perm);

				this.addToMaps(channelDensityGHK, "setPermeability", perm);

				// Ion
				channelDensityGHKNode.addChild(PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.ION.getId(), Resources.ION.get(), new StringValue(channelDensityGHK.getIon())));

				// Variable Parameter
				channelDensityGHKNode.addChild(createVariableParameterNode(channelDensityGHK.getVariableParameter()));

				// Base
				channelDensityGHKNode.addChildren(createBaseChildren(channelDensityGHK));

				membranePropertiesNode.addChild(channelDensityGHKNode);
			}

			// Channel Density Non Uniform
			List<ChannelDensityNonUniform> channelDensitiesNonUniform = membraneProperties.getChannelDensityNonUniform();
			for(ChannelDensityNonUniform channelDensityNonUniform : channelDensitiesNonUniform)
			{
				CompositeNode channelDensityNonUniformNode = new CompositeNode(channelDensityNonUniform.getId(), PopulateGeneralModelTreeUtils.getUniqueName(
						Resources.CHANNEL_DENSITY_NON_UNIFORM.get(), channelDensityNonUniform));

				// Ion Channel
				channelDensityNonUniformNode.addChild(createChannelNode((IonChannel) neuroMLAccessUtility.getComponent(channelDensityNonUniform.getIonChannel(), model, Resources.ION_CHANNEL)));

				ParameterSpecificationNode n = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.EREV.getId(), Resources.EREV.get(), channelDensityNonUniform.getErev());
				// Reverse Potential
				channelDensityNonUniformNode.addChild(n);

				this.addToMaps(channelDensityNonUniform, "setErev", n);

				// Ion
				channelDensityNonUniformNode
						.addChild(PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.ION.getId(), Resources.ION.get(), new StringValue(channelDensityNonUniform.getIon())));

				// Variable Parameter
				channelDensityNonUniformNode.addChild(createVariableParameterNode(channelDensityNonUniform.getVariableParameter()));

				// Base
				channelDensityNonUniformNode.addChildren(createBaseChildren(channelDensityNonUniform));

				membranePropertiesNode.addChild(channelDensityNonUniformNode);
			}

			// Channel Density Non Uniform Nernst
			List<ChannelDensityNonUniformNernst> channelDensitiesNonUniformNernst = membraneProperties.getChannelDensityNonUniformNernst();
			for(ChannelDensityNonUniformNernst channelDensityNonUniformNernst : channelDensitiesNonUniformNernst)
			{
				CompositeNode channelDensityNonUniformNernstNode = new CompositeNode(channelDensityNonUniformNernst.getId(), PopulateGeneralModelTreeUtils.getUniqueName(
						Resources.CHANNEL_DENSITY_NON_UNIFORM_NERNST.get(), channelDensityNonUniformNernst));

				// Ion Channel
				channelDensityNonUniformNernstNode.addChild(createChannelNode((IonChannel) neuroMLAccessUtility.getComponent(channelDensityNonUniformNernst.getIonChannel(), model,
						Resources.ION_CHANNEL)));

				// Ion
				channelDensityNonUniformNernstNode.addChild(PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.ION.getId(), Resources.ION.get(), new StringValue(
						channelDensityNonUniformNernst.getIon())));

				// Variable Parameter
				channelDensityNonUniformNernstNode.addChild(createVariableParameterNode(channelDensityNonUniformNernst.getVariableParameter()));

				// Base
				channelDensityNonUniformNernstNode.addChildren(createBaseChildren(channelDensityNonUniformNernst));

				membranePropertiesNode.addChild(channelDensityNonUniformNernstNode);
			}

			// Spike threshold
			List<SpikeThresh> spikeThreshs = membraneProperties.getSpikeThresh();
			for(int i = 0; i < spikeThreshs.size(); i++)
			{
				ParameterSpecificationNode n = PopulateNodesModelTreeUtils.createParameterSpecificationNode(PopulateGeneralModelTreeUtils.getUniqueId(Resources.SPIKE_THRESHOLD.getId(), i),
						PopulateGeneralModelTreeUtils.getUniqueName(Resources.SPIKE_THRESHOLD.get(), i), spikeThreshs.get(i).getValue());
				membranePropertiesNode.addChild(n);
				this.addToMaps(spikeThreshs.get(i), "setValue", n);
			}

			// Specific Capacitance
			List<SpecificCapacitance> specificCapacitances = membraneProperties.getSpecificCapacitance();
			for(int i = 0; i < specificCapacitances.size(); i++)
			{
				ParameterSpecificationNode n = PopulateNodesModelTreeUtils.createParameterSpecificationNode(PopulateGeneralModelTreeUtils.getUniqueId(Resources.SPECIFIC_CAPACITANCE.getId(), i),
						PopulateGeneralModelTreeUtils.getUniqueName(Resources.SPECIFIC_CAPACITANCE.get(), i), specificCapacitances.get(i).getValue());
				membranePropertiesNode.addChild(n);
				this.addToMaps(specificCapacitances.get(i), "setValue", n);
			}

			// Initial Membrance Potentials
			List<InitMembPotential> initMembPotentials = membraneProperties.getInitMembPotential();
			for(int i = 0; i < initMembPotentials.size(); i++)
			{
				ParameterSpecificationNode n = PopulateNodesModelTreeUtils.createParameterSpecificationNode(PopulateGeneralModelTreeUtils.getUniqueId(Resources.INIT_MEMBRANE_POTENTIAL.getId(), i),
						PopulateGeneralModelTreeUtils.getUniqueName(Resources.INIT_MEMBRANE_POTENTIAL.get(), i), initMembPotentials.get(i).getValue());
				membranePropertiesNode.addChild(n);
				this.addToMaps(initMembPotentials.get(i), "setValue", n);
			}

			biophysicalPropertiesNode.addChild(membranePropertiesNode);
		}

		// Intracellular Properties
		biophysicalPropertiesNode.addChild(createIntracellularPropertiesNode(biophysicalProperties.getIntracellularProperties()));

		// Extracellular Properties
		biophysicalPropertiesNode.addChild(createExtracellularPropertiesNode(biophysicalProperties.getExtracellularProperties()));

		// Standalone Properties
		biophysicalPropertiesNode.addChildren(createStandaloneChildren(biophysicalProperties));

		return biophysicalPropertiesNode;
	}

	private CompositeNode createVariableParameterNode(List<VariableParameter> variableParameters)
	{
		if(variableParameters != null && variableParameters.size() > 0)
		{
			CompositeNode variableParametersNode = new CompositeNode(Resources.VARIABLE_PARAMETER.getId(), Resources.VARIABLE_PARAMETER.get());
			for(int i = 0; i < variableParameters.size(); i++)
			{
				VariableParameter variableParameter = variableParameters.get(i);

				CompositeNode variableParameterNode = new CompositeNode(PopulateGeneralModelTreeUtils.getUniqueId(Resources.VARIABLE_PARAMETER.getId(), i),
						PopulateGeneralModelTreeUtils.getUniqueName(Resources.VARIABLE_PARAMETER.get(), i));

				variableParameterNode.addChild(PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.PARAMETER.getId(), Resources.PARAMETER.get(),
						new StringValue(variableParameter.getParameter())));

				CompositeNode inhomogeneousValueNode = new CompositeNode(Resources.INHOMOGENEOUS_VALUE.getId(), Resources.INHOMOGENEOUS_VALUE.get());
				inhomogeneousValueNode.addChild(PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.PARAMETER.getId(), Resources.PARAMETER.get(), new StringValue(variableParameter
						.getInhomogeneousValue().getInhomogeneousParameter())));
				inhomogeneousValueNode.addChild(PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.VALUE.getId(), Resources.VALUE.get(), new StringValue(variableParameter
						.getInhomogeneousValue().getValue())));
				variableParameterNode.addChild(inhomogeneousValueNode);

				variableParametersNode.addChild(variableParameterNode);
			}

			return variableParametersNode;
		}
		return null;
	}

	public CompositeNode createIntracellularPropertiesNode(IntracellularProperties intracellularProperties) throws ModelInterpreterException
	{
		if(intracellularProperties != null)
		{
			CompositeNode intracellularPropertiesNode = new CompositeNode(Resources.INTRACELLULAR_P.getId(), Resources.INTRACELLULAR_P.get());

			// Resistivity
			List<Resistivity> resistivities = intracellularProperties.getResistivity();
			if(resistivities != null && resistivities.size() > 0)
			{
				CompositeNode resistivitiesNode = new CompositeNode(Resources.RESISTIVITY.getId(), Resources.RESISTIVITY.get());
				for(int i = 0; i < resistivities.size(); i++)
				{
					ParameterSpecificationNode node = PopulateNodesModelTreeUtils.createParameterSpecificationNode(PopulateGeneralModelTreeUtils.getUniqueId(Resources.RESISTIVITY.getId(), i),
							PopulateGeneralModelTreeUtils.getUniqueName(Resources.RESISTIVITY.get(), i), resistivities.get(i).getValue());
					resistivitiesNode.addChild(node);
					this.addToMaps(resistivities.get(i), "setValue", node);
				}
				intracellularPropertiesNode.addChild(resistivitiesNode);
			}

			// Species
			List<Species> species = intracellularProperties.getSpecies();
			if(species != null && species.size() > 0)
			{
				CompositeNode speciesNode = new CompositeNode(Resources.SPECIES.getId(), Resources.SPECIES.get());
				speciesNode.addChildren(createSpeciesNode(species));
				intracellularPropertiesNode.addChild(speciesNode);
			}
			return intracellularPropertiesNode;
		}
		return null;
	}

	public CompositeNode createExtracellularPropertiesNode(ExtracellularProperties extracellularProperties) throws ModelInterpreterException
	{
		if(extracellularProperties != null)
		{
			CompositeNode extracellularPropertiesNode = new CompositeNode(extracellularProperties.getId(), Resources.EXTRACELLULAR_P.get());
			extracellularPropertiesNode.addChildren(createBaseChildren(extracellularProperties));

			List<Species> species = extracellularProperties.getSpecies();
			if(species != null)
			{
				CompositeNode speciesNode = new CompositeNode(Resources.SPECIES.getId(), Resources.SPECIES.get());
				speciesNode.addChildren(createSpeciesNode(species));
				extracellularPropertiesNode.addChild(speciesNode);
			}
			return extracellularPropertiesNode;
		}
		return null;
	}

	private List<CompositeNode> createSpeciesNode(List<Species> species) throws ModelInterpreterException
	{
		List<CompositeNode> speciesNodeList = new ArrayList<CompositeNode>();
		for(Species specie : species)
		{
			CompositeNode speciesNodeItem = new CompositeNode(specie.getId(), PopulateGeneralModelTreeUtils.getUniqueName(Resources.SPECIES.get(), specie.getId()));

			// Initial Concentration
			ParameterSpecificationNode ic = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.INIT_CONCENTRATION.getId(), Resources.INIT_CONCENTRATION.get(),
					specie.getInitialConcentration());
			speciesNodeItem.addChild(ic);
			this.addToMaps(specie, "setInitialConcentration", ic);

			ParameterSpecificationNode ie = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.INIT_EXT_CONCENTRATION.getId(), Resources.INIT_EXT_CONCENTRATION.get(),
					specie.getInitialExtConcentration());
			// Initial External Concentration
			speciesNodeItem.addChild(ie);
			this.addToMaps(specie, "setInitialExtConcentration", ie);

			// Ion
			speciesNodeItem.addChild(PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.ION.getId(), Resources.ION.get(), new StringValue(specie.getIon())));

			// Concentration Model
			speciesNodeItem.addChild(createConcentrationModelNode(neuroMLAccessUtility.getComponent(specie.getConcentrationModel(), model, Resources.CONCENTRATION_MODEL)));

			speciesNodeList.add(speciesNodeItem);
		}
		return speciesNodeList;
	}

	public CompositeNode createConcentrationModelNode(Object concentrationModel) throws ModelInterpreterException
	{
		if(concentrationModel != null)
		{
			CompositeNode concentrationModelNode = new CompositeNode(Resources.CONCENTRATION_MODEL.getId(), Resources.CONCENTRATION_MODEL.get());
			if(concentrationModel instanceof DecayingPoolConcentrationModel)
			{
				DecayingPoolConcentrationModel decayingPoolConcentrationModel = (DecayingPoolConcentrationModel) concentrationModel;
				ParameterSpecificationNode decay = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.DECAY_CONSTANT.getId(), Resources.DECAY_CONSTANT.get(),
						decayingPoolConcentrationModel.getDecayConstant());
				concentrationModelNode.addChild(decay);
				this.addToMaps(concentrationModel, "setDecayConstant", decay);
				ParameterSpecificationNode resting = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.RESTING_CONC.getId(), Resources.RESTING_CONC.get(),
						decayingPoolConcentrationModel.getRestingConc());
				concentrationModelNode.addChild(resting);
				this.addToMaps(concentrationModel, "setRestingConc", resting);
				ParameterSpecificationNode thickness = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.SHELL_THICKNESS.getId(), Resources.SHELL_THICKNESS.get(),
						decayingPoolConcentrationModel.getShellThickness());
				concentrationModelNode.addChild(thickness);
				this.addToMaps(concentrationModel, "setShellThickness", thickness);
				concentrationModelNode
						.addChild(PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.ION.getId(), Resources.ION.get(), new StringValue(decayingPoolConcentrationModel.getIon())));
			}
			else
			{
				FixedFactorConcentrationModel fixedFactorConcentrationModel = (FixedFactorConcentrationModel) concentrationModel;
				ParameterSpecificationNode decay = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.DECAY_CONSTANT.getId(), Resources.DECAY_CONSTANT.get(),
						fixedFactorConcentrationModel.getDecayConstant());
				concentrationModelNode.addChild(decay);
				this.addToMaps(fixedFactorConcentrationModel, "setDecayConstant", decay);
				ParameterSpecificationNode resting = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.RESTING_CONC.getId(), Resources.RESTING_CONC.get(),
						fixedFactorConcentrationModel.getRestingConc());
				concentrationModelNode.addChild(resting);
				this.addToMaps(fixedFactorConcentrationModel, "setRestingConc", resting);
				ParameterSpecificationNode rho = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.RHO.getId(), Resources.RHO.get(), fixedFactorConcentrationModel.getRho());
				concentrationModelNode.addChild(rho);
				this.addToMaps(fixedFactorConcentrationModel, "setRho", rho);
				concentrationModelNode
						.addChild(PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.ION.getId(), Resources.ION.get(), new StringValue(fixedFactorConcentrationModel.getIon())));
			}
			concentrationModelNode.addChildren(createStandaloneChildren((Standalone) concentrationModel));

			return concentrationModelNode;
		}
		return null;
	}

	public CompositeNode createGateNode(Base gate) throws ModelInterpreterException, ContentError
	{
		CompositeNode gateNode = new CompositeNode(gate.getId(), PopulateGeneralModelTreeUtils.getUniqueName(Resources.GATE.get(), gate));
		gateNode.addChildren(createBaseChildren(gate));

		if(gate instanceof GateHHUndetermined)
		{
			GateHHUndetermined gateHHUndetermined = (GateHHUndetermined) gate;

			if(gateHHUndetermined.getNotes() != null)
			{
				gateNode.addChild(PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.NOTES.getId(), Resources.NOTES.get(), new StringValue(gateHHUndetermined.getNotes())));
			}
			gateNode.addChild(createQ10SettingsNode(gateHHUndetermined.getQ10Settings()));
			gateNode.addChild(createRateGateNode(Resources.FW_RATE, gateHHUndetermined.getForwardRate()));
			gateNode.addChild(createRateGateNode(Resources.BW_RATE, gateHHUndetermined.getReverseRate()));
			gateNode.addChild(createTimeCourseNode(gateHHUndetermined.getTimeCourse()));
			gateNode.addChild(createSteadyStateNode(gateHHUndetermined.getSteadyState()));
			if(gateHHUndetermined.getInstances() != null)
			{
				gateNode.addChild(PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.INSTANCES.getId(), Resources.INSTANCES.get(), new IntValue(gateHHUndetermined.getInstances().intValue())));
			}
			if(gateHHUndetermined.getType() != null)
			{
				ComponentType typeRate = (ComponentType) neuroMLAccessUtility.getComponent(gateHHUndetermined.getType().value(), model, Resources.COMPONENT_TYPE);
				gateNode.addChild(PopulateLEMSModelTreeUtils.createCompositeNodeFromComponentType(Resources.TYPE.getId(), Resources.TYPE.get(), typeRate));
			}
		}
		else if(gate instanceof GateHHRates)
		{
			GateHHRates gateHHRates = (GateHHRates) gate;

			if(gateHHRates.getNotes() != null)
			{
				gateNode.addChild(PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.NOTES.getId(), Resources.NOTES.get(), new StringValue(gateHHRates.getNotes())));
			}
			gateNode.addChild(createQ10SettingsNode(gateHHRates.getQ10Settings()));
			gateNode.addChild(createRateGateNode(Resources.FW_RATE, gateHHRates.getForwardRate()));
			gateNode.addChild(createRateGateNode(Resources.BW_RATE, gateHHRates.getReverseRate()));
			if(gateHHRates.getInstances() != null)
			{
				gateNode.addChild(PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.INSTANCES.getId(), Resources.INSTANCES.get(), new IntValue(gateHHRates.getInstances().intValue())));
			}
		}
		else if(gate instanceof GateHHRatesInf)
		{
			GateHHRatesInf gateHHRatesInf = (GateHHRatesInf) gate;

			if(gateHHRatesInf.getNotes() != null)
			{
				gateNode.addChild(PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.NOTES.getId(), Resources.NOTES.get(), new StringValue(gateHHRatesInf.getNotes())));
			}
			gateNode.addChild(createQ10SettingsNode(gateHHRatesInf.getQ10Settings()));
			gateNode.addChild(createRateGateNode(Resources.FW_RATE, gateHHRatesInf.getForwardRate()));
			gateNode.addChild(createRateGateNode(Resources.BW_RATE, gateHHRatesInf.getReverseRate()));
			gateNode.addChild(createSteadyStateNode(gateHHRatesInf.getSteadyState()));
			if(gateHHRatesInf.getInstances() != null)
			{
				gateNode.addChild(PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.INSTANCES.getId(), Resources.INSTANCES.get(), new IntValue(gateHHRatesInf.getInstances().intValue())));
			}

		}
		else if(gate instanceof GateHHRatesTau)
		{
			GateHHRatesTau gateHHRatesTau = (GateHHRatesTau) gate;

			if(gateHHRatesTau.getNotes() != null)
			{
				gateNode.addChild(PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.NOTES.getId(), Resources.NOTES.get(), new StringValue(gateHHRatesTau.getNotes())));
			}
			gateNode.addChild(createQ10SettingsNode(gateHHRatesTau.getQ10Settings()));
			gateNode.addChild(createRateGateNode(Resources.FW_RATE, gateHHRatesTau.getForwardRate()));
			gateNode.addChild(createRateGateNode(Resources.BW_RATE, gateHHRatesTau.getReverseRate()));
			gateNode.addChild(createTimeCourseNode(gateHHRatesTau.getTimeCourse()));
			if(gateHHRatesTau.getInstances() != null)
			{
				gateNode.addChild(PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.INSTANCES.getId(), Resources.INSTANCES.get(), new IntValue(gateHHRatesTau.getInstances().intValue())));
			}
		}
		else if(gate instanceof GateHHTauInf)
		{
			GateHHTauInf gateHHTauInf = (GateHHTauInf) gate;

			if(gateHHTauInf.getNotes() != null)
			{
				gateNode.addChild(PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.NOTES.getId(), Resources.NOTES.get(), new StringValue(gateHHTauInf.getNotes())));
			}
			gateNode.addChild(createQ10SettingsNode(gateHHTauInf.getQ10Settings()));
			gateNode.addChild(createTimeCourseNode(gateHHTauInf.getTimeCourse()));
			gateNode.addChild(createSteadyStateNode(gateHHTauInf.getSteadyState()));
			gateNode.addChild(PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.INSTANCES.getId(), Resources.INSTANCES.get(), new IntValue(gateHHTauInf.getInstances().intValue())));
		}
		else if(gate instanceof GateHHRatesTauInf)
		{
			GateHHRatesTauInf gateHHRatesTauInf = (GateHHRatesTauInf) gate;

			if(gateHHRatesTauInf.getNotes() != null)
			{
				gateNode.addChild(PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.NOTES.getId(), Resources.NOTES.get(), new StringValue(gateHHRatesTauInf.getNotes())));
			}
			gateNode.addChild(createQ10SettingsNode(gateHHRatesTauInf.getQ10Settings()));
			gateNode.addChild(createRateGateNode(Resources.FW_RATE, gateHHRatesTauInf.getForwardRate()));
			gateNode.addChild(createRateGateNode(Resources.BW_RATE, gateHHRatesTauInf.getReverseRate()));
			gateNode.addChild(createTimeCourseNode(gateHHRatesTauInf.getTimeCourse()));
			gateNode.addChild(createSteadyStateNode(gateHHRatesTauInf.getSteadyState()));
			if(gateHHRatesTauInf.getInstances() != null)
			{
				gateNode.addChild(PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.INSTANCES.getId(), Resources.INSTANCES.get(), new IntValue(gateHHRatesTauInf.getInstances().intValue())));
			}
		}

		return gateNode;
	}

	private CompositeNode createQ10SettingsNode(Q10Settings q10Settings) throws ModelInterpreterException, ContentError
	{
		if(q10Settings != null)
		{
			CompositeNode q10SettingsNode = new CompositeNode(Resources.Q10SETTINGS.getId(), Resources.Q10SETTINGS.get());

			ComponentType typeQ10Settings = (ComponentType) neuroMLAccessUtility.getComponent(q10Settings.getType(), model, Resources.COMPONENT_TYPE);
			q10SettingsNode.addChild(PopulateLEMSModelTreeUtils.createCompositeNodeFromComponentType(Resources.TYPE.getId(), Resources.TYPE.get(), typeQ10Settings));

			q10SettingsNode.addChild(PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.FIXEDQ10.getId(), Resources.FIXEDQ10.get(), new StringValue(q10Settings.getFixedQ10())));
			q10SettingsNode.addChild(PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.Q10FACTOR.getId(), Resources.Q10FACTOR.get(), new StringValue(q10Settings.getQ10Factor())));
			q10SettingsNode.addChild(PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.EXPERIMENTAL_TEMP.getId(), Resources.EXPERIMENTAL_TEMP.get(),
					new StringValue(q10Settings.getExperimentalTemp())));

			return q10SettingsNode;
		}
		return null;
	}

	public CompositeNode createChannelNode(IonChannel ionChannelBase) throws ModelInterpreterException, ContentError
	{
		if(ionChannelBase != null)
		{
			// Ion Channel
			CompositeNode ionChannelNode = new CompositeNode(ionChannelBase.getId());
			ionChannelNode.setDomainType(ResourcesDomainType.IONCHANNEL.get());

			if(ionChannelBase instanceof IonChannel)
			{
				ionChannelNode.setName(Resources.ION_CHANNEL.get());
			}
			else
			{
				ionChannelNode.setName(Resources.ION_CHANNEL_HH.get());
			}

			IonChannel ionChannel = (IonChannel) ionChannelBase;
			ionChannelNode.addChildren(createStandaloneChildren(ionChannel));

			// Gates
			for(GateHHUndetermined gateHHUndetermined : ionChannel.getGate())
			{
				ionChannelNode.addChild(createGateNode(gateHHUndetermined));
			}
			for(GateHHRates gateHHRates : ionChannel.getGateHHrates())
			{
				ionChannelNode.addChild(createGateNode(gateHHRates));
			}
			for(GateHHRatesTau gateHHRatesTau : ionChannel.getGateHHratesTau())
			{
				ionChannelNode.addChild(createGateNode(gateHHRatesTau));
			}
			for(GateHHTauInf gateHHTauInf : ionChannel.getGateHHtauInf())
			{
				ionChannelNode.addChild(createGateNode(gateHHTauInf));
			}
			for(GateHHRatesInf gateHHRatesInf : ionChannel.getGateHHratesInf())
			{
				ionChannelNode.addChild(createGateNode(gateHHRatesInf));
			}
			for(GateHHRatesTauInf gateHHRatesTauInf : ionChannel.getGateHHratesTauInf())
			{
				ionChannelNode.addChild(createGateNode(gateHHRatesTauInf));
			}

			// Conductance
			ionChannelNode.addChild(PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.CONDUCTANCE.getId(), Resources.CONDUCTANCE.get(), ionChannel.getConductance()));

			// Species
			ionChannelNode.addChild(PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.SPECIES.getId(), Resources.SPECIES.get(), new StringValue(ionChannel.getSpecies())));

			// Type
			if(ionChannel.getType() != null)
			{
				ComponentType typeIonChannel = (ComponentType) neuroMLAccessUtility.getComponent(ionChannel.getType().value(), model, Resources.COMPONENT_TYPE);
				ionChannelNode.addChild(PopulateLEMSModelTreeUtils.createCompositeNodeFromComponentType(Resources.IONCHANNEL_DYNAMICS.getId(), Resources.IONCHANNEL_DYNAMICS.get(), typeIonChannel));
			}

			return ionChannelNode;
		}
		return null;
	}

	public Collection<ParameterSpecificationNode> createIafTauCellChildren(IafTauCell c) throws ModelInterpreterException
	{
		Collection<ParameterSpecificationNode> iafTauCellChildren = new ArrayList<ParameterSpecificationNode>();
		ParameterSpecificationNode leak = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.LEAK_REVERSAL.getId(), Resources.LEAK_REVERSAL.get(), c.getLeakReversal());
		iafTauCellChildren.add(leak);
		this.addToMaps(c, "setLeakReversal", leak);
		ParameterSpecificationNode tau = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.TAU.getId(), Resources.TAU.get(), c.getTau());
		iafTauCellChildren.add(tau);
		this.addToMaps(c, "setTau", tau);
		ParameterSpecificationNode reset = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.RESET.getId(), Resources.RESET.get(), c.getReset());
		iafTauCellChildren.add(reset);
		this.addToMaps(c, "setReset", reset);
		ParameterSpecificationNode thresh = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.THRESH.getId(), Resources.THRESH.get(), c.getThresh());
		iafTauCellChildren.add(thresh);
		this.addToMaps(c, "setThresh", thresh);
		return iafTauCellChildren;
	}

	public Collection<ParameterSpecificationNode> createIafCellChildren(IafCell c) throws ModelInterpreterException
	{
		Collection<ParameterSpecificationNode> iafCellChildren = new ArrayList<ParameterSpecificationNode>();
		ParameterSpecificationNode leak = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.LEAK_REVERSAL.getId(), Resources.LEAK_REVERSAL.get(), c.getLeakReversal());
		iafCellChildren.add(leak);
		this.addToMaps(c, "setLeakReversal", leak);
		ParameterSpecificationNode leakC = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.LEAK_CONDUCTANCE.getId(), Resources.LEAK_CONDUCTANCE.get(), c.getLeakConductance());
		iafCellChildren.add(leakC);
		this.addToMaps(c, "setLeakConductance", leakC);
		ParameterSpecificationNode capacitance = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.CAPACITANCE.getId(), Resources.CAPACITANCE.get(), c.getC());
		iafCellChildren.add(capacitance);
		this.addToMaps(c, "setC", capacitance);
		ParameterSpecificationNode reset = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.RESET.getId(), Resources.RESET.get(), c.getReset());
		iafCellChildren.add(reset);
		this.addToMaps(c, "setReset", reset);
		ParameterSpecificationNode thresh = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.THRESH.getId(), Resources.THRESH.get(), c.getThresh());
		iafCellChildren.add(thresh);
		this.addToMaps(c, "setThresh", thresh);
		return iafCellChildren;
	}

	public CompositeNode createNetworkNode(Network n) throws ModelInterpreterException, ContentError
	{

		CompositeNode networkNode = new CompositeNode(Resources.NETWORK.getId(), Resources.NETWORK.get());

		networkNode.addChildren(createStandaloneChildren(n));

		// for(InputList i : n.getInputList()){
		//
		// }
		//
		// for(ExplicitInput e : n.getExplicitInput()){
		//
		// }
		//
		// for(Region r : n.getRegion()){
		//
		// }

		List<Population> populations = n.getPopulation();
		for(Population p : populations)
		{
			CompositeNode populationNode = new CompositeNode(p.getId(), PopulateGeneralModelTreeUtils.getUniqueName(Resources.POPULATION.get(), p));

			populationNode.addChildren(createStandaloneChildren(p));

			BaseCell baseCell = (BaseCell) neuroMLAccessUtility.getComponent(p.getComponent(), model, Resources.CELL);
			populationNode.addChild(createCellNode(baseCell));

			if(p.getSize() != null)
			{
				populationNode.addChild(PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.SIZE.getId(), Resources.SIZE.get(), new IntValue(p.getSize().intValue())));
			}

			PopulationTypes populationType = p.getType();
			if(populationType != null)
			{
				populationNode
						.addChild(PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.POPULATION_TYPE.getId(), Resources.POPULATION_TYPE.get(), new StringValue(populationType.value())));
			}

			// TODO: Just reading the number of instances and displaying as a text metadata node
			List<Instance> instanceList = p.getInstance();
			if(instanceList != null && instanceList.size() != 0)
			{
				populationNode.addChild(PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.INSTANCES.getId(), Resources.INSTANCES.get(), new IntValue(instanceList.size())));
			}
			networkNode.addChild(populationNode);
		}

		return networkNode;
	}

	public CompositeNode createSynapseNode(BaseConductanceBasedSynapse synapse) throws ContentError, ModelInterpreterException
	{
		if(synapse != null)
		{
			CompositeNode synapseNode = new CompositeNode(synapse.getId());

			synapseNode.addChildren(createStandaloneChildren(synapse));

			ParameterSpecificationNode erev = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.EREV.getId(), Resources.EREV.get(), synapse.getErev());
			synapseNode.addChild(erev);
			ParameterSpecificationNode gate = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.GBASE.getId(), Resources.GBASE.get(), synapse.getGbase());
			synapseNode.addChild(gate);
			this.addToMaps(synapse, "setErev", erev);
			this.addToMaps(synapse, "setGbase", gate);

			if(synapse instanceof ExpTwoSynapse)
			{
				synapseNode.setName(PopulateGeneralModelTreeUtils.getUniqueName(Resources.EXPTWOSYNAPSE.get(), synapse));

				ExpTwoSynapse expTwoSynapse = (ExpTwoSynapse) synapse;
				synapseNode.addChildren(createExpTwoSynapseChildren(expTwoSynapse));
			}
			else if(synapse instanceof ExpOneSynapse)
			{
				synapseNode.setName(PopulateGeneralModelTreeUtils.getUniqueName(Resources.EXPONESYNAPSE.get(), synapse));

				ExpOneSynapse expOneSynapse = (ExpOneSynapse) synapse;
				ParameterSpecificationNode tauDecay = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.TAUDECAY.getId(), Resources.TAUDECAY.get(), expOneSynapse.getTauDecay());
				synapseNode.addChild(tauDecay);
				this.addToMaps(expOneSynapse, "setTauDecay", tauDecay);
			}
			else if(synapse instanceof BlockingPlasticSynapse)
			{
				synapseNode.setName(PopulateGeneralModelTreeUtils.getUniqueName(Resources.BLOCKINGPLASTICSYNAPSE.get(), synapse));

				BlockingPlasticSynapse blockingPlasticSynapse = (BlockingPlasticSynapse) synapse;

				synapseNode.addChildren(createExpTwoSynapseChildren(blockingPlasticSynapse));

				PlasticityMechanism plasticityMechanism = blockingPlasticSynapse.getPlasticityMechanism();
				if(plasticityMechanism != null)
				{
					CompositeNode plasticityMechanismNode = new CompositeNode(Resources.PLASTICITYMECHANISM.getId(), Resources.PLASTICITYMECHANISM.get());
					ParameterSpecificationNode init = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.INITRELEASEPROB.getId(), Resources.INITRELEASEPROB.get(),
							String.valueOf(plasticityMechanism.getInitReleaseProb()));
					plasticityMechanismNode.addChild(init);
					this.addToMaps(plasticityMechanism, "setInitReleaseProb", init);
					ParameterSpecificationNode taufac = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.TAUFAC.getId(), Resources.TAUFAC.get(), plasticityMechanism.getTauFac());
					plasticityMechanismNode.addChild(taufac);
					this.addToMaps(plasticityMechanism, "setTauFac", taufac);
					ParameterSpecificationNode taurec = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.TAUREC.getId(), Resources.TAUREC.get(), plasticityMechanism.getTauRec());
					plasticityMechanismNode.addChild(taurec);
					this.addToMaps(plasticityMechanism, "setTauRec", taurec);

					plasticityMechanismNode.addChild(PopulateLEMSModelTreeUtils.createCompositeNodeFromComponentType(Resources.TYPE.getId(), Resources.TYPE.get(),
							(ComponentType) neuroMLAccessUtility.getComponent(plasticityMechanism.getType().value(), model, Resources.COMPONENT_TYPE)));

					synapseNode.addChild(plasticityMechanismNode);

				}
				BlockMechanism blockMechanism = blockingPlasticSynapse.getBlockMechanism();
				if(blockMechanism != null)
				{
					CompositeNode blockMechanismNode = new CompositeNode(Resources.BLOCKMECHANISM.getId(), Resources.BLOCKMECHANISM.get());
					ParameterSpecificationNode conc = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.SCALINGCONC.getId(), Resources.SCALINGCONC.get(),
							blockMechanism.getScalingConc());
					blockMechanismNode.addChild(conc);
					this.addToMaps(blockMechanism, "setScalingConc", conc);
					ParameterSpecificationNode concentration = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.BLOCKCONCENTRATION.getId(), Resources.BLOCKCONCENTRATION.get(),
							blockMechanism.getBlockConcentration());
					blockMechanismNode.addChild(concentration);
					this.addToMaps(blockMechanism, "setBlockConcentration", concentration);
					ParameterSpecificationNode volt = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.SCALINGVOLT.getId(), Resources.SCALINGVOLT.get(),
							blockMechanism.getScalingVolt());
					blockMechanismNode.addChild(volt);
					this.addToMaps(blockMechanism, "setScalingVolt", volt);
					ParameterSpecificationNode speciesN = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.SPECIES.getId(), Resources.SPECIES.get(), blockMechanism.getSpecies());
					blockMechanismNode.addChild(speciesN);
					this.addToMaps(blockMechanism, "setSpecies", speciesN);

					blockMechanismNode.addChild(PopulateLEMSModelTreeUtils.createCompositeNodeFromComponentType(Resources.TYPE.getId(), Resources.TYPE.get(),
							(ComponentType) neuroMLAccessUtility.getComponent(blockMechanism.getType().value(), model, Resources.COMPONENT_TYPE)));

					synapseNode.addChild(blockMechanismNode);
				}
			}

			return synapseNode;
		}
		return null;
	}

	public CompositeNode createPynnSynapseNode(BasePynnSynapse pynnSynapse) throws ContentError, ModelInterpreterException
	{
		CompositeNode pynnSynapsesNode = new CompositeNode(pynnSynapse.getId(), PopulateGeneralModelTreeUtils.getUniqueName(Resources.PYNN_SYNAPSE.get(), pynnSynapse));

		pynnSynapsesNode.addChildren(createStandaloneChildren(pynnSynapse));
		ParameterSpecificationNode tauSyn = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.TAUSYN.getId(), Resources.TAUSYN.get(), String.valueOf(pynnSynapse.getTauSyn()));
		pynnSynapsesNode.addChild(tauSyn);
		this.addToMaps(pynnSynapse, "setTauSyn", tauSyn);

		ParameterSpecificationNode erev;
		if(pynnSynapse instanceof AlphaCondSynapse)
		{
			erev = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.EREV.getId(), Resources.EREV.get(), String.valueOf(((AlphaCondSynapse) pynnSynapse).getERev()));
			pynnSynapsesNode.addChild(erev);
			this.addToMaps(pynnSynapse, "setERev", erev);
		}
		else if(pynnSynapse instanceof ExpCondSynapse)
		{
			erev = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.EREV.getId(), Resources.EREV.get(), String.valueOf(((AlphaCondSynapse) pynnSynapse).getERev()));
			pynnSynapsesNode.addChild(erev);
			this.addToMaps(pynnSynapse, "setERev", erev);
		}
		return pynnSynapsesNode;
	}

	public Collection<ANode> createExpTwoSynapseChildren(ExpTwoSynapse expTwoSynapse) throws ModelInterpreterException
	{
		Collection<ANode> expTwoSynapseChildren = new ArrayList<ANode>();
		ParameterSpecificationNode tauDecay = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.TAUDECAY.getId(), Resources.TAUDECAY.get(), expTwoSynapse.getTauDecay());
		expTwoSynapseChildren.add(tauDecay);
		this.addToMaps(expTwoSynapse, "setTauDecay", tauDecay);
		ParameterSpecificationNode tauRise = PopulateNodesModelTreeUtils.createParameterSpecificationNode(Resources.TAURISE.getId(), Resources.TAURISE.get(), expTwoSynapse.getTauRise());
		expTwoSynapseChildren.add(tauRise);
		this.addToMaps(expTwoSynapse, "setTauRise", tauRise);
		return expTwoSynapseChildren;
	}

	public Collection<ANode> createStandaloneChildren(Standalone standaloneComponent)
	{
		Collection<ANode> standaloneChildren = new ArrayList<ANode>();

		standaloneChildren.addAll(createBaseChildren(standaloneComponent));

		// TODO: Improve to parse all the attribute in an annotation
		Annotation annotation = standaloneComponent.getAnnotation();
		if(annotation != null)
		{
			CompositeNode annotationNode = new CompositeNode(Resources.ANOTATION.getId(), Resources.ANOTATION.get());
			for(Element element : annotation.getAny())
			{
				for(int i = 0; i < element.getChildNodes().getLength(); i++)
				{
					Node node = element.getChildNodes().item(i);
					if(node.getLocalName() != null && node.getLocalName().equals(Resources.DESCRIPTION.get()))
					{
						// TODO: Still need to extract about from node component
						CompositeNode descriptionNode = new CompositeNode(Resources.DESCRIPTION.getId(), Resources.DESCRIPTION.get());
						for(int j = 0; j < node.getChildNodes().getLength(); j++)
						{
							descriptionNode.addChild(createAnnotationChild(node.getChildNodes().item(j)));
						}
						annotationNode.addChild(descriptionNode);
					}
				}
			}
			standaloneChildren.add(annotationNode);
		}

		standaloneChildren.add(PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.NOTES.getId(), Resources.NOTES.get(), new StringValue(standaloneComponent.getNotes())));
		standaloneChildren.add(PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.METAID.getId(), Resources.METAID.get(), new StringValue(standaloneComponent.getMetaid())));

		return standaloneChildren;
	}

	private CompositeNode createAnnotationChild(Node subNode)
	{
		if(subNode.getLocalName() != null)
		{
			CompositeNode compositeNode = new CompositeNode(subNode.getLocalName(), subNode.getLocalName());

			for(int k = 0; k < subNode.getChildNodes().getLength(); k++)
			{
				Node subsubNode = subNode.getChildNodes().item(k);
				if(subsubNode.getLocalName() != null && subsubNode.getLocalName().equals("Bag"))
				{
					for(int l = 0; l < subsubNode.getChildNodes().getLength(); l++)
					{
						Node subsubsubNode = subsubNode.getChildNodes().item(l);
						NamedNodeMap nnm = subsubsubNode.getAttributes();
						if(nnm != null && nnm.getLength() > 0)
						{
							for(int m = 0; m < nnm.getLength(); m++)
							{
								Node nnmNode = nnm.item(m);
								if(nnmNode.getLocalName().equals("resource"))
								{
									compositeNode.addChild(PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.RESOURCE.getId(), Resources.RESOURCE.get(),
											new StringValue(nnmNode.getNodeValue())));
								}
							}
						}
						else if(subsubsubNode.getLocalName() != null && subsubsubNode.getLocalName().equals("li"))
						{
							compositeNode.addChild(PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.ELEMENT.getId(), Resources.ELEMENT.get(),
									new StringValue(subsubsubNode.getTextContent())));
						}
					}
				}
			}
			return compositeNode;
		}
		return null;
	}

	public Collection<ANode> createBaseChildren(Base baseComponent)
	{

		Collection<ANode> baseChildren = new ArrayList<ANode>();
		baseChildren.add(PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.ID.getId(), Resources.ID.get(), new StringValue(baseComponent.getId())));
		baseChildren.add(PopulateNodesModelTreeUtils.createTextMetadataNode(Resources.NEUROLEX_ID.getId(), Resources.NEUROLEX_ID.get(), new StringValue(baseComponent.getNeuroLexId())));
		return baseChildren;
	}

	public List<ANode> createInfoNode(InfoNode infoNode) throws ModelInterpreterException
	{
		List<ANode> summaryElementList = new ArrayList<ANode>();
		for(Map.Entry<String, Object> properties : ((InfoNode) infoNode).getProperties().entrySet())
		{
			String keyProperties = properties.getKey();
			Object valueProperties = properties.getValue();
			if(!keyProperties.equals("ID"))
			{

				if(valueProperties == null)
				{
					summaryElementList.add(PopulateNodesModelTreeUtils.createTextMetadataNode(PopulateGeneralModelTreeUtils.parseId(keyProperties), keyProperties, new StringValue("")));
				}
				else if(valueProperties instanceof String)
				{
					summaryElementList.add(PopulateNodesModelTreeUtils.createTextMetadataNode(PopulateGeneralModelTreeUtils.parseId(keyProperties), keyProperties, new StringValue(
							(String) valueProperties)));
				}
				else if(valueProperties instanceof BigInteger)
				{
					summaryElementList.add(PopulateNodesModelTreeUtils.createTextMetadataNode(PopulateGeneralModelTreeUtils.parseId(keyProperties), keyProperties, new StringValue(
							((BigInteger) valueProperties).toString())));
				}
				else if(valueProperties instanceof Integer)
				{
					summaryElementList.add(PopulateNodesModelTreeUtils.createTextMetadataNode(PopulateGeneralModelTreeUtils.parseId(keyProperties), keyProperties,
							new StringValue(Integer.toString((Integer) valueProperties))));
				}
				else if(valueProperties instanceof PlotNode)
				{

				}
				else if(valueProperties instanceof ExpressionNode)
				{
					ExpressionNode expressionNode = ((ExpressionNode) valueProperties);

					FunctionNode functionNode = new FunctionNode(PopulateGeneralModelTreeUtils.parseId(keyProperties), keyProperties);
					functionNode.setExpression(expressionNode.getExpression());
					functionNode.getArgument().add("v");

					PlotMetadataNode plotMetadataNode = expressionNode.getPlotMetadataNode();
					if(plotMetadataNode != null)
					{
						functionNode.getPlotMetadata().put("PlotTitle", plotMetadataNode.getPlotTitle());
						functionNode.getPlotMetadata().put("XAxisLabel", plotMetadataNode.getXAxisLabel());
						functionNode.getPlotMetadata().put("YAxisLabel", plotMetadataNode.getYAxisLabel());
						functionNode.getPlotMetadata().put("InitialValue", Double.toString(plotMetadataNode.getInitialValue()));
						functionNode.getPlotMetadata().put("FinalValue", Double.toString(plotMetadataNode.getFinalValue()));
						functionNode.getPlotMetadata().put("StepValue", Double.toString(plotMetadataNode.getStepValue()));
					}
					summaryElementList.add(functionNode);
				}
				else if(valueProperties instanceof InfoNode)
				{
					CompositeNode subSummaryElementNode = new CompositeNode(PopulateGeneralModelTreeUtils.parseId(keyProperties), keyProperties);
					subSummaryElementNode.addChildren(createInfoNode((InfoNode) valueProperties));
					summaryElementList.add(subSummaryElementNode);
				}
				else
				{
					throw new ModelInterpreterException("Info Writer Node type not supported. Object: " + keyProperties + ". Java class" + valueProperties.getClass());
				}
			}
		}
		return summaryElementList;
	}

	/**
	 * Adds node to maps
	 * 
	 * @param instance
	 * @param method
	 * @param node
	 * @throws ModelInterpreterException
	 */
	/**
	 * @param instance
	 * @param methodName
	 * @param node
	 * @throws ModelInterpreterException
	 */
	private void addToMaps(Object instance, String methodName, ParameterSpecificationNode node) throws ModelInterpreterException
	{
		try
		{
			Method setter = null;
			Class<?> current = instance.getClass();
			while(current != null)
			{
				for(Method m : current.getDeclaredMethods())
				{
					if(m.getName().equals(methodName))
					{
						setter = m;
						break;
					}
				}
				if(setter == null)
				{
					current = current.getSuperclass();
				}
				else
				{
					break;
				}
			}
			if(setter != null)
			{
				this._parameterNodesToMethodsMap.put(node, setter);
				this._parameterNodesToObjectMap.put(node, instance);
			}
			else
			{
				throw new ModelInterpreterException("Cannot find the method " + methodName + "in the NeuroML object " + instance.toString());
			}
		}
		catch(Exception e)
		{
			throw new ModelInterpreterException(e);
		}
	}

}
