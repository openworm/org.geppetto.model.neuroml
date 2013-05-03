/**
 * 
 */
package org.geppetto.model.neuroml.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.net.URL;

import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.model.neuroml.services.NeuroMLModelInterpreterService;
import org.junit.Test;

/**
 * @author matteocantarelli
 *
 */
public class NeuroMLModelInterpreterServiceTest
{

	/**
	 * Test method for {@link org.geppetto.model.neuroml.services.LemsMLModelInterpreterService#readModel(java.net.URL)}.
	 */
	@Test
	public void testReadModel()
	{
		NeuroMLModelInterpreterService modelInterpreter=new NeuroMLModelInterpreterService();
		URL url = this.getClass().getResource("/NML2_FullCell.nml");
		ModelWrapper model;
		try
		{
			model = (ModelWrapper) modelInterpreter.readModel(url);
			assertNotNull(model);
			assertNotNull(model.getModel("url"));
			assertNotNull(model.getModel("lems"));
			assertNotNull(model.getModel("neuroml"));
		}
		catch (ModelInterpreterException e)
		{
			fail(e.getMessage());
		}

	}

}
