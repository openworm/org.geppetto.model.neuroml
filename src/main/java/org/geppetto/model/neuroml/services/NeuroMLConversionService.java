package org.geppetto.model.neuroml.services;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.log.NullLogChute;
import org.geppetto.core.conversion.AConversion;
import org.geppetto.core.conversion.ConversionException;
import org.geppetto.core.model.IModel;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.services.ModelFormat;
import org.geppetto.model.neuroml.utils.NeuroMLAccessUtility;
import org.lemsml.jlems.core.type.Lems;
import org.neuroml.export.neuron.NeuronWriter;
import org.springframework.stereotype.Service;

/**
 * @author Adrian Quintana (adrian.perez@ucl.ac.uk)
 * 
 */
@Service
public class NeuroMLConversionService extends AConversion{
	
	private static final String NEURON_ID = "Neuron";

	public NeuroMLConversionService() {
		super();
		this.setSupportedInput(new ModelFormat(ConversionUtils.NEUROML_MODELFORMAT));
		this.setSupportedInput(new ModelFormat(ConversionUtils.LEMS_MODELFORMAT));
	}

	@Override
	public List<ModelFormat> getSupportedOutputs(IModel model, ModelFormat input) throws ConversionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IModel convert(IModel model, ModelFormat input, ModelFormat output) throws ConversionException {
		if (!input.equals(ConversionUtils.NEUROML_MODELFORMAT) && !input.equals(ConversionUtils.LEMS_MODELFORMAT)){
			throw new ConversionException("FORMAT NOT SUPPORTED");
		}
		
		
		if (output.equals(ConversionUtils.NEURON_MODELFORMAT)){
			Lems lems = (Lems) ((ModelWrapper) model).getModel(NeuroMLAccessUtility.LEMS_ID);
			try {
				lems.setResolveModeLoose();
				lems.deduplicate();
				lems.resolve();
				lems.evaluateStatic();
				
				
				Properties props = new Properties();
				props.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM_CLASS, NullLogChute.class.getName());                        
				Velocity.init(props);
				
				//TODO: where should we create the tmp file?
				File mainFile = File.createTempFile("temp-file-name", "_nrn.py", new File("c:/home/adrian/tmp/"));
				NeuronWriter nw = new NeuronWriter(lems);
				nw.setNoGui(true);
	
		         List<File> ff = nw.generateMainScriptAndMods(mainFile);
		         for (File f : ff) {
		             System.out.println("Generated: " + f.getAbsolutePath());
		         }
		         
		         ModelWrapper outputModel = new ModelWrapper(UUID.randomUUID().toString());
		         outputModel.setInstancePath(model.getInstancePath());
		         outputModel.wrapModel(NEURON_ID, ff);
	         
			} catch (Exception e) {
				e.printStackTrace();
				throw new ConversionException(e);
			}
			
		}
		else if (output.equals(ConversionUtils.NEURON_MODELFORMAT)){
			
		}
		return null;
	}

	@Override
	public void registerGeppettoService()
	{
		// TODO Auto-generated method stub
		
	}

}
