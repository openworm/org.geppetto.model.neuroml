

package org.geppetto.model.neuroml.test;

import java.io.File;
import java.net.MalformedURLException;

import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.model.neuroml.services.LEMSModelInterpreterService;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.lemsml.jlems.api.LEMSBuildException;

/**
 * @author Adrian Quintana (adrian.perez@ucl.ac.uk)
 * 
 */
public class LEMSModelInterpreterServiceTest
{
    private ModelInterpreterTestUtils modelInterpreterTestUtils;

	@Before
	public void oneTimeSetUp()
	{
		modelInterpreterTestUtils = new ModelInterpreterTestUtils();
	}
	/**
	 * "" Test method for {@link org.geppetto.model.neuroml.services.LemsMLModelInterpreterService#readModel(java.net.URL)}.
	 * 
	 * @throws MalformedURLException
	 * @throws ModelInterpreterException
	 * @throws LEMSBuildException
	 */
	@Test
	public void testReadModelHHCell() throws Exception
	{
		modelInterpreterTestUtils.serialise("/hhcell/LEMS_NML2_Ex5_DetCell.xml", "hhcell", new LEMSModelInterpreterService());
//		ModelInterpreterTestUtils.serialise("/hhcell/LEMS_NML2_Ex5_DetCell.xml", null, new LEMSModelInterpreterService());
//		ModelInterpreterTestUtils.serialise("/hhcell/LEMS_NML2_Ex5_DetCell.xml", "net1", new LEMSModelInterpreterService());
	}

	
	@Test
	public void testReadModelc302() throws Exception
	{
//		ModelInterpreterTestUtils.serialise("/c302/LEMS_c302_A.xml", "c302_A", new LEMSModelInterpreterService());
		modelInterpreterTestUtils.serialise("/c302/LEMS_c302_A.xml", null, new LEMSModelInterpreterService());
	}
	
	@Test
	public void testReadModelMuscle() throws Exception
	{
//		ModelInterpreterTestUtils.serialise("/muscle/LEMS_NeuronMuscle.xml", "net1", new LEMSModelInterpreterService());
//		ModelInterpreterTestUtils.serialise("/muscle/LEMS_NeuronMuscle.xml", null, new LEMSModelInterpreterService());
	}
	
	@AfterClass
	public static void doYourOneTimeTeardown()
	{
		 File c302 = new File("./src/test/resources/LEMS_c302_A.xml.xmi");
		 c302.delete();
		 
//		 File muscle = new File("./src/test/resources/LEMS_NeuronMuscle.xml.xmi");
//		 muscle.delete();
		
		 File hhcell = new File("./src/test/resources/LEMS_NML2_Ex5_DetCell.xml.xmi");
		 hhcell.delete();

	}
}
