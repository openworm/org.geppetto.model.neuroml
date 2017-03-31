package org.geppetto.model.neuroml.test;

import java.net.URL;
import java.util.Map;

import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
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

    public static Type readModel(String modelPath, String typeName, IModelInterpreter modelInterpreter, GeppettoLibrary gl, GeppettoModel gm) throws Exception
    {
                gm.getLibraries().add(gl);
                gm.getLibraries().add(EcoreUtil.copy(SharedLibraryManager.getSharedCommonLibrary()));
                GeppettoModelAccess geppettoModelAccess = new GeppettoModelAccess(gm);

		URL url = ModelInterpreterTestUtils.class.getResource(modelPath);
		Type type = modelInterpreter.importType(url, typeName, gl, geppettoModelAccess);
		geppettoModelAccess.addTypeToLibrary(type,gl);

                return type;
    }
    
        public static Type readModel(String modelPath, String typeName, IModelInterpreter modelInterpreter) throws Exception
	{
                GeppettoFactory geppettoFactory = GeppettoFactory.eINSTANCE;
		GeppettoLibrary gl = geppettoFactory.createGeppettoLibrary();
		GeppettoModel gm = geppettoFactory.createGeppettoModel();

                return readModel(modelPath, typeName, modelInterpreter, gl, gm);
	}

	public static void serialise(String modelPath, String typeName, IModelInterpreter modelInterpreter) throws Exception
	{
                GeppettoFactory geppettoFactory = GeppettoFactory.eINSTANCE;
		GeppettoLibrary gl = geppettoFactory.createGeppettoLibrary();
		GeppettoModel gm = geppettoFactory.createGeppettoModel();
		
                gm.getLibraries().add(gl);

		gm.getLibraries().add(EcoreUtil.copy(SharedLibraryManager.getSharedCommonLibrary()));
		GeppettoModelAccess geppettoModelAccess = new GeppettoModelAccess(gm);

		// Initialize the factory and the resource set
		GeppettoPackage.eINSTANCE.eClass();
		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		Map<String, Object> m = reg.getExtensionToFactoryMap();
		m.put("json", new JsonResourceFactory()); // sets the factory for the JSON type
		m.put("xmi", new XMIResourceFactoryImpl()); // sets the factory for the XMI typ

		// How to save to JSON
		String baseOutputPath = "./src/test/resources/" + modelPath.substring(modelPath.lastIndexOf("/"));
		String outputPath_all = baseOutputPath + ".xmi";

		AdapterFactoryEditingDomain domain = new AdapterFactoryEditingDomain(new ComposedAdapterFactory(), new BasicCommandStack());
		Resource resourceAll = domain.createResource(URI.createURI(outputPath_all).toString());
		resourceAll.getContents().add(gm);

                Type type = readModel(modelPath, typeName, modelInterpreter, gl, gm);

                geppettoModelAccess.addTypeToLibrary(type,gl);
		resourceAll.save(null);
	}
}
