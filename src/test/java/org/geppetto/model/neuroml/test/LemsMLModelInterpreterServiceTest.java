/**
 * 
 */
package org.geppetto.model.neuroml.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.List;

import org.geppetto.model.neuroml.services.LemsMLModelInterpreterService;
import org.junit.Test;
import org.openworm.simulationengine.core.model.IModel;
import org.openworm.simulationengine.core.model.ModelInterpreterException;

/**
 * @author matteocantarelli
 *
 */
public class LemsMLModelInterpreterServiceTest
{

	/**
	 * Test method for {@link org.geppetto.model.neuroml.services.LemsMLModelInterpreterService#readModel(java.net.URL)}.
	 */
	@Test
	public void testReadModel()
	{
		LemsMLModelInterpreterService modelInterpreter=new LemsMLModelInterpreterService();
		URL url = this.getClass().getResource("/NML2_FullCell.nml");
		List<IModel> models;
		try
		{
			models = modelInterpreter.readModel(url);
			assertNotNull(models);
			assertFalse(models.isEmpty());
			assertNotNull(models.get(0));
		}
		catch (ModelInterpreterException e)
		{
			fail(e.getMessage());
		}

	}

	/**
	 * Test method for {@link org.geppetto.model.neuroml.services.LemsMLModelInterpreterService#getSceneFromModel(java.util.List)}.
	 */
	@Test
	public void testGetSceneFromModel()
	{
		fail("Not yet implemented");
	}

}
