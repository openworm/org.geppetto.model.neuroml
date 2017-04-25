/**
 * *****************************************************************************
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
 ******************************************************************************
 */
package org.geppetto.model.neuroml.test;

import java.io.File;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.geppetto.model.neuroml.modelInterpreterUtils.PopulateProjectionTypes;
import org.geppetto.model.neuroml.services.NeuroMLModelInterpreterService;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.geppetto.model.types.Type;
import org.geppetto.model.types.ArrayType;
import org.geppetto.model.types.CompositeType;
import org.geppetto.model.types.ImportType;
import org.neuroml.model.util.NeuroMLConverter;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.Population;
import org.neuroml.model.Projection;

import org.geppetto.model.values.ArrayElement;
import org.geppetto.model.variables.Variable;
import org.lemsml.jlems.core.type.Component;
import org.lemsml.jlems.core.type.ComponentType;
import org.lemsml.jlems.core.type.Lems;
import org.neuroml.model.Instance;
import org.neuroml.model.util.hdf5.NeuroMLHDF5Reader;


/**
 * @author Adrian Quintana (adrian.perez@ucl.ac.uk)
 *
 */
public class NeuroMLModelInterpreterServiceTest
{

    private ModelTester mTest;

    @Before
    public void oneTimeSetUp()
    {
        mTest = new ModelTester();
    }

    /**
     * Test method for
     * {@link org.geppetto.model.neuroml.services.LemsMLModelInterpreterService#readModel(java.net.URL)}.
     *
     * @throws Exception
     */
    private class ModelTester
    {

        public void testModelInterpretation(String modelPath, String typeId) throws Exception
        {
        	NeuroMLModelInterpreterService nmlModelInterpreter = new NeuroMLModelInterpreterService();
        	ModelInterpreterTestUtils modelInterpreterTestUtils = new ModelInterpreterTestUtils(nmlModelInterpreter);
            
            modelInterpreterTestUtils.serialise(modelPath, typeId);

            NeuroMLConverter neuromlConverter = new NeuroMLConverter();

            NeuroMLDocument nmlDoc;
            File file = new File("./src/test/resources" + modelPath);
            if (modelPath.endsWith("nml") || modelPath.endsWith("xml"))
            {
                nmlDoc = neuromlConverter.loadNeuroML(file);
            }
            else
            {
                NeuroMLHDF5Reader nmlH5 = new NeuroMLHDF5Reader();
                nmlDoc = nmlH5.parse(file, true);
            }

            NeuroMLModelInterpreterService nmlModelInterpreter2 = new NeuroMLModelInterpreterService();
            Type geppettoModel = modelInterpreterTestUtils.readModel(modelPath, typeId, nmlModelInterpreter2);
        	
            // Make some comparisons between read Geppetto model and NeuroML document as read by org.neuroml.model
            assertEquals(nmlDoc.getId(), geppettoModel.getId());
            System.out.println("Comparing NML model " + nmlDoc.getId() + " to Geppetto: " + geppettoModel.getId());

            // Populations (compare id and size)
            List<Population> docPopulations = nmlDoc.getNetwork().get(0).getPopulation();
            Set<Entry<String, Integer>> docPopSummary = new HashSet<Entry<String, Integer>>();
            Set<Entry<Integer, String>> docPosSummary = new HashSet<Entry<Integer, String>>();

            for (Population pop : docPopulations)
            {
                docPopSummary.add(new SimpleEntry<String, Integer>(pop.getId(), pop.getSize()));
                if (!pop.getInstance().isEmpty())
                {
                    Instance inst = pop.getInstance().get(0);
                    docPosSummary.add(new SimpleEntry<Integer, String>(inst.getId().intValue(), "(" + inst.getLocation().getX() + "," + inst.getLocation().getY() + "," + inst.getLocation().getZ() + ")"));
                }
            }

            List<Type> modelPopulations = nmlModelInterpreter.getPopulateTypes().getTypesMap().get("population");

            Set<Entry<String, Integer>> modelPopSummary = new HashSet<Entry<String, Integer>>();
            Set<Entry<Integer, String>> modelPosSummary = new HashSet<Entry<Integer, String>>();

            for (Type pop : modelPopulations)
            {
                ArrayType popArray = (ArrayType) pop;
                modelPopSummary.add(new SimpleEntry<String, Integer>(popArray.getId(), popArray.getSize()));
                ArrayElement el = popArray.getDefaultValue().getElements().get(0);
                modelPosSummary.add(new SimpleEntry<Integer, String>(el.getIndex(), "(" + (float) el.getPosition().getX() + "," + (float) el.getPosition().getY() + "," + (float) el.getPosition().getZ() + ")"));
            }

            System.out.println("modelPopSummary: " + modelPopSummary);
            System.out.println("modelPosSummary: " + modelPosSummary);
            System.out.println("docPopSummary: " + docPopSummary);
            System.out.println("docPosSummary: " + docPosSummary);

            assertEquals(modelPopSummary, docPopSummary);
            assertEquals(modelPosSummary, docPosSummary);

            // Projections (compare id and size of connections list)
            List<Projection> docProjections = nmlDoc.getNetwork().get(0).getProjection();

            Set<Entry<Integer, String>> docConnSummary = new HashSet<Entry<Integer, String>>();

            HashMap<String, Integer> docProjSummary = new HashMap<String, Integer>();
            for (Projection proj : docProjections)
            {
                int total = proj.getConnection().size() + proj.getConnectionWD().size();
                System.out.println("Model has proj " + proj.getId() + " with " + total + " conns");
                docProjSummary.put(proj.getId(), total);
            }

            HashMap<String, Integer> modelProjSummary = new HashMap<String, Integer>();
            PopulateProjectionTypes ppt = new PopulateProjectionTypes(nmlModelInterpreter.getPopulateTypes(), nmlModelInterpreter.getAccess(), modelInterpreterTestUtils.getLibrary());
            try
            {
                List<Type> modelProjections = new ArrayList<Type>(nmlModelInterpreter.getPopulateTypes().getTypesMap().get("projection"));
                for (Type proj : modelProjections)
                {
                    Component projComponent = (Component) proj.getDomainModel().getDomainModel();
                    CompositeType resolvedProj = (CompositeType) ppt.resolveProjectionImportType(projComponent, (ImportType) proj);

                    int resolvedProjSize = 0;
                    for (Variable var : resolvedProj.getVariables())
                    	if (var.getId().startsWith("id"))
                    		resolvedProjSize++;

                    modelProjSummary.put(resolvedProj.getId(), resolvedProjSize);

                    System.out.println(projComponent.getID() + " = " + projComponent.getStrictChildren().size() + " = " + docProjSummary.get(projComponent.getID()));
                }

                assertEquals(modelProjSummary, docProjSummary);
            }
            catch (NullPointerException e)
            {
                // no projections
            }
            // Compare to model read from HDF5
            // ...
        }
    }

    /**
     * Test method for
     * {@link org.geppetto.model.neuroml.services.LemsMLModelInterpreterService#readModel(java.net.URL)}.
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

    /*
    @Test
    public void testAcnet2HDF5() throws Exception
    {
        mTest.testModelInterpretation("/acnet2/MediumNet.net.nml.h5", null);
    }
    
    @Test
    public void testBalancedHDF5() throws Exception
    {
        mTest.testModelInterpretation("/Balanced/Balanced.net.nml.h5", null);
    }*/

    @Test
    public void testAcnet2() throws Exception
    {
        mTest.testModelInterpretation("/acnet2/MediumNet.net.nml", null);
    }

    @Test
    public void testBalanced() throws Exception
    {
        mTest.testModelInterpretation("/Balanced/Balanced.net.nml", null);
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
