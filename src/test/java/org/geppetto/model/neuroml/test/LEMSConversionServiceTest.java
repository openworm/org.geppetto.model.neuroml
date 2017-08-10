
package org.geppetto.model.neuroml.test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.beans.PathConfiguration;
import org.geppetto.core.conversion.ConversionException;
import org.geppetto.core.data.model.local.LocalAspectConfiguration;
import org.geppetto.core.data.model.local.LocalExperiment;
import org.geppetto.core.data.model.local.LocalSimulatorConfiguration;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.services.registry.ServicesRegistry;
import org.geppetto.model.DomainModel;
import org.geppetto.model.GeppettoFactory;
import org.geppetto.model.ModelFormat;
import org.geppetto.model.neuroml.services.LEMSConversionService;
import org.geppetto.model.neuroml.services.LEMSModelInterpreterService;
import org.geppetto.model.neuroml.services.NeuroMLModelInterpreterService;
import org.geppetto.model.neuroml.utils.OptimizedLEMSReader;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lemsml.jlems.api.LEMSBuildException;
import org.lemsml.jlems.core.sim.ContentError;
import org.lemsml.jlems.core.sim.LEMSException;
import org.lemsml.jlems.core.type.Lems;
import org.neuroml.model.util.NeuroMLException;

/**
 * @author Adrian Quintana (adrian.perez@ucl.ac.uk)
 * 
 */
public class LEMSConversionServiceTest
{
	
	private static Log _logger = LogFactory.getLog(LEMSConversionServiceTest.class);
	
	// FIXME: We have to use OSGI text or spring app context initialization
	@BeforeClass
	public static void initializeServiceRegistry() throws Exception
	{
		LEMSConversionService lemsConversionService = new LEMSConversionService();
		lemsConversionService.registerGeppettoService();

		LEMSModelInterpreterService lemsModelInterpreter = new LEMSModelInterpreterService();
		lemsModelInterpreter.registerGeppettoService();

		NeuroMLModelInterpreterService neuromlModelInterpreter = new NeuroMLModelInterpreterService();
		neuromlModelInterpreter.registerGeppettoService();
	}

	/**
	 * "" Test method for {@link org.geppetto.model.neuroml.services.LEMSConversionService#readModel(java.net.URL)}.
	 * 
	 * @throws ModelInterpreterException
	 * @throws IOException
	 * @throws LEMSException
	 * @throws NeuroMLException
	 * @throws URISyntaxException
	 * @throws LEMSBuildException
	 */
	@Test
	public void testNeuron() throws ConversionException, ModelInterpreterException, LEMSException, IOException, NeuroMLException, URISyntaxException
	{
		LEMSConversionService lemsConversionService = new LEMSConversionService();
		lemsConversionService.setProjectId(1);
		lemsConversionService.setExperiment(new LocalExperiment(1, null, null, null, null, null, null, null, null, null, null));
		DomainModel inputModel = createDomainModel(new URL("https://raw.githubusercontent.com/openworm/org.geppetto.samples/development/LEMS/SingleComponentHH/LEMS_NML2_Ex5_DetCell.xml"), "net1");
		DomainModel outputModel = convertModelTo(lemsConversionService, inputModel, "net1", "NEURON");

		compareGeneratedDomainModel(outputModel, "/neuron/hhcell/");

		List<ModelFormat> modelFormats = lemsConversionService.getSupportedOutputs();
		Assert.assertEquals(18, modelFormats.size());

		modelFormats = lemsConversionService.getSupportedOutputs(inputModel);
		Assert.assertEquals(7, modelFormats.size());

	}

	/**
	 * "" Test method for {@link org.geppetto.model.neuroml.services.LEMSConversionService#readModel(java.net.URL)}.
	 * 
	 * @throws ModelInterpreterException
	 * @throws IOException
	 * @throws LEMSException
	 * @throws NeuroMLException
	 * @throws URISyntaxException
	 * @throws LEMSBuildException
	 */
	@Test
	public void testNetPyNE() throws ConversionException, ModelInterpreterException, LEMSException, IOException, NeuroMLException, URISyntaxException
	{
		LEMSConversionService lemsConversionService = new LEMSConversionService();
		lemsConversionService.setProjectId(1);
		lemsConversionService.setExperiment(new LocalExperiment(1, null, null, null, null, null, null, null, null, null, null));
		DomainModel inputModel = createDomainModel(new URL("https://raw.githubusercontent.com/openworm/org.geppetto.samples/development/LEMS/SingleComponentHH/LEMS_NML2_Ex5_DetCell.xml"), "net1");
		DomainModel outputModel = convertModelTo(lemsConversionService, inputModel, "net1", "NETPYNE");

		compareGeneratedDomainModel(outputModel, "/netpyne/hhcell/");

		List<ModelFormat> modelFormats = lemsConversionService.getSupportedOutputs();
		Assert.assertEquals(18, modelFormats.size());

		modelFormats = lemsConversionService.getSupportedOutputs(inputModel);
		Assert.assertEquals(7, modelFormats.size());

    }
    
	/**
	 * "" Test method for {@link org.geppetto.model.neuroml.services.LEMSConversionService#readModel(java.net.URL)}.
	 * 
	 * @throws ModelInterpreterException
	 * @throws IOException
	 * @throws LEMSException
	 * @throws NeuroMLException
	 * @throws URISyntaxException
	 * @throws LEMSBuildException
	 */
	@Test
	public void testJNeuroML() throws ConversionException, ModelInterpreterException, LEMSException, IOException, NeuroMLException, URISyntaxException
	{
		LEMSConversionService lemsConversionService = new LEMSConversionService();
		lemsConversionService.setProjectId(1);
		lemsConversionService.setExperiment(new LocalExperiment(1, null, null, null, null, null, null, null, null, null, null));
		DomainModel inputModel = createDomainModel(new URL("https://raw.githubusercontent.com/openworm/org.geppetto.samples/development/LEMS/SingleComponentHH/LEMS_NML2_Ex5_DetCell.xml"), "net1");
        //DomainModel inputModel = createDomainModel(new URL("https://raw.githubusercontent.com/openworm/org.geppetto.samples/development/NeuroML/Pyramidal/L5bPyrCellHayEtAl2011.net.nml"), "net1");
		
		DomainModel outputModel = convertModelTo(lemsConversionService, inputModel, "net1", "jNeuroML");

		compareGeneratedDomainModel(outputModel, "/jneuroml/hhcell/");

		List<ModelFormat> modelFormats = lemsConversionService.getSupportedOutputs();
	    Assert.assertEquals(18, modelFormats.size());

		modelFormats = lemsConversionService.getSupportedOutputs(inputModel);
		Assert.assertEquals(7, modelFormats.size());

	}

	/**
	 * @param lemsConversionService
	 * @param model
	 * @param targetModel
	 * @param targetFormat
	 * @return
	 * @throws ConversionException
	 */
	private DomainModel convertModelTo(LEMSConversionService lemsConversionService, DomainModel model, String targetModel, String targetFormat) throws ConversionException
	{
		Map<String, String> parametersSimulatorConfiguration = new HashMap<String, String>();
		parametersSimulatorConfiguration.put("target", targetModel);
		LocalSimulatorConfiguration localSimulatorConfiguration = new LocalSimulatorConfiguration(0, "0", "0", 0.00005f, 1f, parametersSimulatorConfiguration);
        ModelFormat mf = ServicesRegistry.getModelFormat(targetFormat);
        if (mf==null)
            throw new ConversionException("Error converting to "+targetFormat+"\nRegistered model formats: "+ServicesRegistry.getRegisteredModelFormats());
		DomainModel outputModel = lemsConversionService.convert(model, ServicesRegistry.getModelFormat(targetFormat), new LocalAspectConfiguration(0, null, null, null, localSimulatorConfiguration),
				null);
		return outputModel;
	}

	/**
	 * @param url
	 * @param component
	 * @return
	 * @throws NeuroMLException
	 * @throws IOException
	 * @throws LEMSException
	 * @throws ContentError
	 */
	private DomainModel createDomainModel(URL url, String component) throws NeuroMLException, IOException, LEMSException, ContentError
	{
		List<URL> dependentModels = new ArrayList<URL>();
		OptimizedLEMSReader reader = new OptimizedLEMSReader(dependentModels);
		reader.readAllFormats(url);
		DomainModel model = GeppettoFactory.eINSTANCE.createDomainModel();
		Lems partialLems = ((Lems) reader.getPartialLEMSDocument());
		model.setDomainModel(partialLems.getComponent(component));
		model.setFormat(ServicesRegistry.getModelFormat("LEMS"));
		return model;
	}

	/**
	 * @param outputModel
	 * @param modelFolder
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	private void compareGeneratedDomainModel(DomainModel outputModel, String modelFolder) throws URISyntaxException, IOException
	{
		String outputFolder = ((String) outputModel.getDomainModel()).substring(0, ((String) outputModel.getDomainModel()).lastIndexOf(File.separator) + 1);
		String expectedFolder = "expected" + modelFolder;
		File output = new File(outputFolder);
		File[] directoryListing = output.listFiles();
		if(directoryListing != null)
		{
			for(File child : directoryListing)
			{
				_logger.info("= Comparing: "+child.getAbsolutePath()+"...");
				File expectedFile = new File(LEMSConversionServiceTest.class.getClassLoader().getResource(expectedFolder + child.getName()).toURI());
				_logger.info("= ...to: "+expectedFile.getAbsolutePath());
                List exp = FileUtils.readLines(expectedFile);
                List found = FileUtils.readLines(child);
                for (int i=0; i<exp.size(); i++)
                {
                    Assert.assertEquals(exp.get(i),found.get(i));
			}
		}
	}
    }

	@AfterClass
	public static void teardown() throws Exception
	{
		File tmp = new File(PathConfiguration.getPathInTempFolder(""));
		if(tmp.exists())
		{
			deleteDirectory(tmp);
		}
	}

	public static boolean deleteDirectory(File directory)
	{
		if(directory.exists())
		{
			File[] files = directory.listFiles();
			if(null != files)
			{
				for(int i = 0; i < files.length; i++)
				{
					if(files[i].isDirectory())
					{
						deleteDirectory(files[i]);
					}
					else
					{
						files[i].delete();
					}
				}
			}
		}
		return (directory.delete());
	}

}
