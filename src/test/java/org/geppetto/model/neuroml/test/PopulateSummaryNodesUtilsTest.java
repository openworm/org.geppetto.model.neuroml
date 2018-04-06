
package org.geppetto.model.neuroml.test;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
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
import org.geppetto.model.neuroml.services.NeuroMLModelInterpreterService;
import org.geppetto.model.neuroml.summaryUtils.PopulateSummaryNodesUtils;
import org.geppetto.model.types.Type;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.neuroml.model.NeuroMLDocument;

/**
 * @author Padraig Gleeson
 * 
 */
public class PopulateSummaryNodesUtilsTest
{
	private static Log _logger = LogFactory.getLog(PopulateSummaryNodesUtilsTest.class);
	
    /*
       This is really just a helper class to allow update/testing of generated HTML in
       PopulateSummaryNodesUtils without deploying every time to Geppetto
    */
	private ModelInterpreterTestUtils modelInterpreterTestUtils;
	
	@Before
	public void oneTimeSetUp()
	{
		modelInterpreterTestUtils = new ModelInterpreterTestUtils();
	}
	
    public static void testSummary(String modelPath, String typeName, IModelInterpreter modelInterpreter) throws Exception
    {
        GeppettoFactory geppettoFactory = GeppettoFactory.eINSTANCE;
        GeppettoLibrary gl = geppettoFactory.createGeppettoLibrary();
        GeppettoModel gm = geppettoFactory.createGeppettoModel();

        gm.getLibraries().add(gl);

        URL url = ModelInterpreterTestUtils.class.getResource(modelPath);

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


        Type type = modelInterpreter.importType(url, typeName, gl, geppettoModelAccess);
        geppettoModelAccess.addTypeToLibrary(type,gl);
        resourceAll.save(null);

        Map<String, List<Type>> typesMap =  new HashMap<String, List<Type>>();
        typesMap.put(typeName, Arrays.asList(type));
        NeuroMLDocument neuroMLDocument = null;
        PopulateSummaryNodesUtils psnu = new PopulateSummaryNodesUtils(typesMap, type, url, geppettoModelAccess, neuroMLDocument);

        psnu.createHTMLVariables();

    }


    @Test
    public void testModelL23Smith() throws Exception
    {
        _logger.info("============================================");
        modelInterpreterTestUtils.serialise("/l23smith/L23_One.net.nml", null, new NeuroMLModelInterpreterService());
    }
    
    @Test
    public void testModelACnet() throws Exception
    {
        modelInterpreterTestUtils.serialise("/acnet2/MediumNet.net.nml", null, new NeuroMLModelInterpreterService());
        _logger.info("============================================");
        modelInterpreterTestUtils.serialise("/acnet2/pyr_4_sym.cell.nml", null, new NeuroMLModelInterpreterService());
    }

    @Test
    public void testCA1() throws Exception
    {
        modelInterpreterTestUtils.serialise("/ca1/CA1PyramidalCell.net.nml", null, new NeuroMLModelInterpreterService());
//		modelInterpreterTestUtils.serialise("/ca1/BigCA1.net.nml", null, new NeuroMLModelInterpreterService());  
    }

    @Test
    public void testHH() throws Exception
    {
        modelInterpreterTestUtils.serialise("/hhcell/NML2_SingleCompHHCell.nml", null, new NeuroMLModelInterpreterService());
    }
    
    @Test
    public void testHHTut() throws Exception
    {
        modelInterpreterTestUtils.serialise("/hhtutorial/HHTutorial.net.nml", null, new NeuroMLModelInterpreterService());
    }
    
    @Test
    public void testBBP() throws Exception
    {
        modelInterpreterTestUtils.serialise("/bbp/ManyCells.net.nml", null, new NeuroMLModelInterpreterService());
    }
    
    @Test
    public void testTraub() throws Exception
    {
        modelInterpreterTestUtils.serialise("/traub/TestSmall.net.nml", null, new NeuroMLModelInterpreterService());
    }
    
    @Test
    public void testRetina() throws Exception
    {
        modelInterpreterTestUtils.serialise("/retina/rods_small.net.nml", null, new NeuroMLModelInterpreterService());
    }

    @AfterClass
    public static void doYourOneTimeTeardown()
    {
        File acnet2 = new File("./src/test/resources/MediumNet.net.nml.xmi");
        acnet2.delete();
//		File bask = new File("./src/test/resources/bask.cell.nml.xmi");
//		bask.delete();

//		File pvdr = new File("./src/test/resources/PVDR.nml.xmi");
//		pvdr.delete();

        File ca1 = new File("./src/test/resources/BigCA1.net.nml.xmi");
        ca1.delete();
    }

}
