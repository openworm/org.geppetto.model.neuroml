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

package org.geppetto.model.neuroml.test;

import java.io.File;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map.Entry;

import org.geppetto.model.neuroml.services.NeuroMLModelInterpreterService;
import org.junit.Before;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

import org.geppetto.model.types.Type;
import org.geppetto.model.types.ArrayType;
import org.neuroml.model.util.NeuroMLConverter;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.Population;
import org.neuroml.model.Projection;

import org.geppetto.model.values.ArrayElement;

import org.lemsml.jlems.core.type.Component;
import org.neuroml.model.Instance;


/**
 * @author Adrian Quintana (adrian.perez@ucl.ac.uk)
 * 
 */
public class NeuroMLModelInterpreterServiceTest
{
    private ModelTester mTest;

    @Before
    public void oneTimeSetUp() {
        mTest = new ModelTester();
    }
	/**
	 * Test method for {@link org.geppetto.model.neuroml.services.LemsMLModelInterpreterService#readModel(java.net.URL)}.
	 * 
	 * @throws Exception
	 */
    private class ModelTester {
        public void testModelInterpretation(String modelPath, String typeId) throws Exception
	{
                NeuroMLModelInterpreterService nmlModelInterpreter = new NeuroMLModelInterpreterService();

                ModelInterpreterTestUtils.serialise(modelPath, typeId, nmlModelInterpreter);

                NeuroMLConverter neuromlConverter = new NeuroMLConverter();
                NeuroMLDocument nmlDoc = neuromlConverter.loadNeuroML(new File("./src/test/resources" + modelPath));
                Type geppettoModel = ModelInterpreterTestUtils.readModel(modelPath, typeId, new NeuroMLModelInterpreterService());

                // Make some comparisons between read Geppetto model and NeuroML document as read by org.neuroml.model
                assertEquals(nmlDoc.getId(), geppettoModel.getId());
                System.out.println("Comparing NML model "+nmlDoc.getId()+" to Geppetto: "+geppettoModel.getId());

                // Populations (compare id and size)
                List<Population> docPopulations = nmlDoc.getNetwork().get(0).getPopulation();
                Set<Entry<String, Integer>> docPopSummary = new HashSet<Entry<String, Integer>>();
                Set<Entry<Integer, String>> docPosSummary = new HashSet<Entry<Integer, String>>();
                
                for (Population pop : docPopulations) {
                    docPopSummary.add(new SimpleEntry<String,Integer>(pop.getId(), pop.getSize()));
                    if (!pop.getInstance().isEmpty())
                    {
                        Instance inst = pop.getInstance().get(0);
                        docPosSummary.add(new SimpleEntry<Integer,String>(inst.getId().intValue(), "("+inst.getLocation().getX()+","+inst.getLocation().getY()+","+inst.getLocation().getZ()+")"));
                    }
                }

                List<Type> modelPopulations = nmlModelInterpreter.getPopulateTypes().getTypesMap().get("population");
                
                Set<Entry<String, Integer>> modelPopSummary = new HashSet<Entry<String, Integer>>();
                Set<Entry<Integer, String>> modelPosSummary = new HashSet<Entry<Integer, String>>();
                
                for (Type pop : modelPopulations) {
                    ArrayType popArray = (ArrayType) pop;
                    modelPopSummary.add(new SimpleEntry<String,Integer>(popArray.getId(), popArray.getSize()));
                    ArrayElement el = popArray.getDefaultValue().getElements().get(0);
                    modelPosSummary.add(new SimpleEntry<Integer,String>(el.getIndex(), "("+(float)el.getPosition().getX()+","+(float)el.getPosition().getY()+","+(float)el.getPosition().getZ()+")"));
                }
                
                
                System.out.println("modelPopSummary: "+ modelPopSummary);
                System.out.println("modelPosSummary: "+ modelPosSummary);
                System.out.println("docPopSummary: "+ docPopSummary);
                System.out.println("docPosSummary: "+ docPosSummary);
                
                assertEquals(modelPopSummary, docPopSummary);
                assertEquals(modelPosSummary, docPosSummary);

                // Projections (compare id and size of connections list)
                List<Projection> docProjections = nmlDoc.getNetwork().get(0).getProjection();
                
                HashMap<String, Integer> docProjSummary = new HashMap<String, Integer>();
                for (Projection proj : docProjections) {
                    docProjSummary.put(proj.getId(), proj.getConnection().size()+proj.getConnectionWD().size());
                }

                List<Type> modelProjections = nmlModelInterpreter.getPopulateTypes().getTypesMap().get("projection");
                HashMap<String, Integer> modelProjSummary = new HashMap<String, Integer>();

                try {
                    for (Type proj : modelProjections) {
                        Component projComponent = (Component) proj.getDomainModel().getDomainModel();
                        modelProjSummary.put(projComponent.getID(), projComponent.getStrictChildren().size());
                        System.out.println(projComponent.getID()+" = "+projComponent.getStrictChildren().size()+" = "+docProjSummary.get(projComponent.getID()));
                    }

                    assertEquals(modelProjSummary, docProjSummary);
                } catch (NullPointerException e) {
                    // no projections
                }

                // Compare to model read from HDF5
                // ...
	}
    }

	/**
	 * Test method for {@link org.geppetto.model.neuroml.services.LemsMLModelInterpreterService#readModel(java.net.URL)}.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testReadModelPVDR() throws Exception
	{
            //mTest.testModelInterpretation("/pvdr/PVDR.nml", "PVDR");
            //ModelInterpreterTestUtils.serialise("/pvdr/PVDR.nml", "PVDR", new NeuroMLModelInterpreterService());
            //ModelInterpreterTestUtils.serialise("/pvdr/PVDR.nml", null, new NeuroMLModelInterpreterService());
	}

        @Test
        public void testAcnet2() throws Exception
        {
            mTest.testModelInterpretation("/acnet2/MediumNet.net.nml", null);
        }

	@Test
	public void testCA1() throws Exception
	{
            //ModelInterpreterTestUtils.serialise("/ca1/BigCA1.net.nml", "CA1", new NeuroMLModelInterpreterService());
            mTest.testModelInterpretation("/ca1/BigCA1.net.nml", null);
	}

	@Test
	public void testTraub() throws Exception
	{
            //ModelInterpreterTestUtils.serialise("/ca1/BigCA1.net.nml", "CA1", new NeuroMLModelInterpreterService());
            mTest.testModelInterpretation("/traub/TestSmall.net.nml", null);
	}

}
