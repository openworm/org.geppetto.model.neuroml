

package org.geppetto.model.neuroml.test;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.lang.InterruptedException;
import java.util.concurrent.ExecutionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
	private static Log _logger = LogFactory.getLog(OptimizedLEMSReaderTest.class);
	
    private void loadNeuroMLFile(String modelPath) throws IOException, NeuroMLException, LEMSException, InterruptedException, ExecutionException
    {

        List<URL> dependentModels = new ArrayList<URL>();

        URL url = ModelInterpreterTestUtils.class.getResource(modelPath);
        OptimizedLEMSReader olr = new OptimizedLEMSReader(dependentModels);
        _logger.info("Loading: "+modelPath);
        olr.readAllFormats(url);

        _logger.info("Done: "+olr.getNetworkHelper());

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
