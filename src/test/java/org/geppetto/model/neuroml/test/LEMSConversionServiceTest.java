package org.geppetto.model.neuroml.test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.geppetto.core.conversion.ConversionException;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.services.ModelFormat;
import org.geppetto.core.utilities.URLReader;
import org.geppetto.model.neuroml.services.ConversionUtils;
import org.geppetto.model.neuroml.services.LEMSModelInterpreterService;
import org.geppetto.model.neuroml.services.LEMSConversionService;
import org.geppetto.model.neuroml.services.NeuroMLModelInterpreterService;
import org.junit.Test;
import org.lemsml.jlems.api.LEMSBuildException;
import org.lemsml.jlems.core.sim.LEMSException;
import org.lemsml.jlems.core.type.Lems;
import org.neuroml.export.Utils;
import org.neuroml.export.neuron.NeuronWriter;

/**
 * @author Adrian Quintana (adrian.perez@ucl.ac.uk)
 * 
 */
public class LEMSConversionServiceTest {

	/**
	 * "" Test method for {@link org.geppetto.model.neuroml.services.LEMSConversionService#readModel(java.net.URL)}.
	 * 
	 * @throws ModelInterpreterException
	 * @throws IOException 
	 * @throws LEMSException 
	 * @throws LEMSBuildException
	 */
	@Test
	public void testTargetedLemsCellModel() throws ConversionException, ModelInterpreterException, LEMSException, IOException
	{
		LEMSConversionService neuroMLConversionService = new LEMSConversionService();
		
		//HH
		LEMSModelInterpreterService modelInterpreter = new LEMSModelInterpreterService();
		URL url = new URL("https://raw.githubusercontent.com/openworm/org.geppetto.samples/development/LEMS/SingleComponentHH/LEMS_NML2_Ex5_DetCell.xml");
		
		ModelWrapper model = (ModelWrapper) modelInterpreter.readModel(url, null, "");
		neuroMLConversionService.convert(model, new ModelFormat(ConversionUtils.LEMS_MODELFORMAT), new ModelFormat(ConversionUtils.NEURON_MODELFORMAT));
		
	}
	
	/**
	 * "" Test method for {@link org.geppetto.model.neuroml.services.LEMSConversionService#readModel(java.net.URL)}.
	 * 
	 * @throws ModelInterpreterException
	 * @throws IOException 
	 * @throws LEMSException 
	 * @throws LEMSBuildException
	 */
	@Test
	public void testNeuroMLCellModel() throws ConversionException, ModelInterpreterException, LEMSException, IOException
	{
//		NeuroMLConversionService neuroMLConversionService = new NeuroMLConversionService();
//		
//		//Purkinje
//		NeuroMLModelInterpreterService modelInterpreter = new NeuroMLModelInterpreterService();
//		URL url = new URL("https://raw.github.com/openworm/org.geppetto.samples/master/NeuroML/Purkinje/purk.nml");
//		
//		ModelWrapper model = (ModelWrapper) modelInterpreter.readModel(url, null, "");
//		neuroMLConversionService.convert(model, new ModelFormat(ConversionUtils.NEUROML_MODELFORMAT), new ModelFormat(ConversionUtils.NEURON_MODELFORMAT));
	}
	
	/**
	 * "" Test method for {@link org.geppetto.model.neuroml.services.LEMSConversionService#readModel(java.net.URL)}.
	 * 
	 * @throws ModelInterpreterException
	 * @throws IOException 
	 * @throws LEMSException 
	 * @throws LEMSBuildException
	 */
	@Test
	public void testNeuroMLChannelModel() throws ConversionException, ModelInterpreterException, LEMSException, IOException
	{
//		NeuroMLConversionService neuroMLConversionService = new NeuroMLConversionService();
//		
//		//Channel
//		NeuroMLModelInterpreterService modelInterpreter = new NeuroMLModelInterpreterService();
//		URL url = new URL("https://raw.githubusercontent.com/OpenSourceBrain/GranCellLayer/master/neuroConstruct/generatedNeuroML2/Gran_CaHVA_98.channel.nml");
//		
//		ModelWrapper model = (ModelWrapper) modelInterpreter.readModel(url, null, "");
//		neuroMLConversionService.convert(model, new ModelFormat(ConversionUtils.NEUROML_MODELFORMAT), new ModelFormat(ConversionUtils.NEURON_MODELFORMAT));
		
	}
	

}
