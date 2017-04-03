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
import org.geppetto.model.DomainModel;
import org.geppetto.model.types.impl.ArrayTypeImpl;

import org.lemsml.jlems.core.type.Component;


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

                // Populations (compare id and size)
                List<Population> docPopulations = nmlDoc.getNetwork().get(0).getPopulation();
                Set<Entry<String, Integer>> docPopSummary = new HashSet<Entry<String, Integer>>();
                for (Population pop : docPopulations) {
                    docPopSummary.add(new SimpleEntry<String,Integer>(pop.getId(), pop.getSize()));
                }

                List<Type> modelPopulations = nmlModelInterpreter.getPopulateTypes().getTypesMap().get("population");
                Set<Entry<String, Integer>> modelPopSummary = new HashSet<Entry<String, Integer>>();
                for (Type pop : modelPopulations) {
                    ArrayType popArray = (ArrayType) pop;
                    modelPopSummary.add(new SimpleEntry<String,Integer>(popArray.getId(), popArray.getSize()));
                }

                assertEquals(modelPopSummary, docPopSummary);

                // Projections (compare id and size of connections list)
                List<Projection> docProjections = nmlDoc.getNetwork().get(0).getProjection();
                Set<Entry<String, Integer>> docProjSummary = new HashSet<Entry<String, Integer>>();
                for (Projection proj : docProjections) {
                    docProjSummary.add(new SimpleEntry<String,Integer>(proj.getId(), proj.getConnection().size()));
                }

                List<Type> modelProjections = nmlModelInterpreter.getPopulateTypes().getTypesMap().get("projection");
                Set<Entry<String, Integer>> modelProjSummary = new HashSet<Entry<String, Integer>>();

                try {
                    for (Type proj : modelProjections) {
                        Component projComponent = (Component) proj.getDomainModel().getDomainModel();
                        modelProjSummary.add(new SimpleEntry<String,Integer>(projComponent.getID(), projComponent.getStrictChildren().size()));
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

}
