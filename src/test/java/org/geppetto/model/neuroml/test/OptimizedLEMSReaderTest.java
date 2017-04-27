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

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import org.geppetto.model.neuroml.utils.OptimizedLEMSReader;
import org.junit.Test;
import org.lemsml.jlems.core.sim.LEMSException;

import org.neuroml.model.util.NeuroMLException;


/**
 * @author Adrian Quintana (adrian.perez@ucl.ac.uk)
 * 
 */
public class OptimizedLEMSReaderTest
{
    private void loadNeuroMLFile(String modelPath) throws IOException, NeuroMLException, LEMSException
    {

        List<URL> dependentModels = new ArrayList<URL>();

        URL url = ModelInterpreterTestUtils.class.getResource(modelPath);
        OptimizedLEMSReader olr = new OptimizedLEMSReader(dependentModels);
        System.out.println("Loading: "+modelPath);
        olr.readAllFormats(url);

        System.out.println("Done: "+olr.getNetworkHelper());

    }


    @Test
    public void testReadAcnet() throws Exception
    {
        loadNeuroMLFile("/acnet2/MediumNet.net.nml");
    }

    @Test
    public void testReadAcnetH5() throws Exception
    {
        loadNeuroMLFile("/acnet2/MediumNet.net.nml.h5");
    }

    @Test
    public void testBalancedHDF5() throws Exception
    {
        loadNeuroMLFile("/Balanced/Balanced.net.nml.h5");
    }

    @Test
    public void testBalanced() throws Exception
    {
        loadNeuroMLFile("/Balanced/Balanced.net.nml");
    }

    @Test
    public void testCA1() throws Exception
    {
        loadNeuroMLFile("/ca1/BigCA1.net.nml");
    }

    @Test
    public void testTraub() throws Exception
    {
        loadNeuroMLFile("/traub/TestSmall.net.nml");
    }


}
