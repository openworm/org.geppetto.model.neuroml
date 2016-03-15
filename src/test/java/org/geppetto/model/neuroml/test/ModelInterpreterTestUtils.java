package org.geppetto.model.neuroml.test;

import java.net.URL;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.emfjson.jackson.resource.JsonResourceFactory;
import org.geppetto.core.manager.SharedLibraryManager;
import org.geppetto.core.model.GeppettoModelAccess;
import org.geppetto.core.model.IModelInterpreter;
import org.geppetto.model.GeppettoFactory;
import org.geppetto.model.GeppettoLibrary;
import org.geppetto.model.GeppettoModel;
import org.geppetto.model.GeppettoPackage;
import org.geppetto.model.types.Type;

public class ModelInterpreterTestUtils
{
	//private static Log _logger = LogFactory.getLog(NeuroMLModelInterpreterServiceTest.class);
	
	public static void serialise(String modelPath, String typeName, IModelInterpreter modelInterpreter) throws Exception
	{
		GeppettoFactory geppettoFactory = GeppettoFactory.eINSTANCE;
		GeppettoLibrary gl = geppettoFactory.createGeppettoLibrary();
		GeppettoModel gm = geppettoFactory.createGeppettoModel();
		gm.getLibraries().add(gl);

		URL url = ModelInterpreterTestUtils.class.getResource(modelPath);

		gm.getLibraries().add(EcoreUtil.copy(SharedLibraryManager.getSharedCommonLibrary()));
		GeppettoModelAccess commonLibraryAccess = new GeppettoModelAccess(gm);

		Type type = modelInterpreter.importType(url, typeName, gl, commonLibraryAccess);

		//long startTime = System.currentTimeMillis();

		// Initialize the factory and the resource set
		GeppettoPackage.eINSTANCE.eClass();
		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		Map<String, Object> m = reg.getExtensionToFactoryMap();
		m.put("json", new JsonResourceFactory()); // sets the factory for the JSON type
		m.put("xmi", new XMIResourceFactoryImpl()); // sets the factory for the XMI typ
		ResourceSet resSet = new ResourceSetImpl();

		// How to save to JSON
		String baseOutputPath = "./src/test/resources/" + modelPath.substring(modelPath.lastIndexOf("/"));
		String outputPath_all = baseOutputPath + ".xmi";

		Resource resourceAll = resSet.createResource(URI.createURI(outputPath_all));
		resourceAll.getContents().add(gm);
		resourceAll.save(null);

		//long endTime = System.currentTimeMillis();
		//_logger.info("Serialising " + (endTime - startTime) + " milliseconds for url " + url + " and  typename " + typeName);
	}
}
