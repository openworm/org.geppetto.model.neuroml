

package org.geppetto.model.neuroml.test;

import org.geppetto.model.neuroml.services.NeuroMLModelInterpreterService;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Adrian Quintana (adrian.perez@ucl.ac.uk)
 * 
 */
public class PerformanceTest
{
	private ModelInterpreterTestUtils modelInterpreterTestUtils;
	
	@Before
	public void oneTimeSetUp()
	{
		modelInterpreterTestUtils = new ModelInterpreterTestUtils();
	}
	/**
	 * Test method for {@link org.geppetto.model.neuroml.services.LemsMLModelInterpreterService#readModel(java.net.URL)}.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testReadTraubLargeConns() throws Exception
	{
		modelInterpreterTestUtils.serialise("/traub/LargeConns.net.nml", null, new NeuroMLModelInterpreterService());
	}

	

	@AfterClass
	public static void doYourOneTimeTeardown()
	{

	}

}
