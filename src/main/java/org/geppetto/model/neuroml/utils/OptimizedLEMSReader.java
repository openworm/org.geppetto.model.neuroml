/**
 * 
 */
package org.geppetto.model.neuroml.utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.manager.Scope;
import org.geppetto.core.utilities.URLReader;
import org.lemsml.jlems.api.interfaces.ILEMSDocument;
import org.lemsml.jlems.core.sim.LEMSException;
import org.lemsml.jlems.core.sim.Sim;
import org.neuroml.export.utils.Utils;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.util.NeuroMLConverter;
import org.neuroml.model.util.NeuroMLException;
import org.neuroml.model.util.hdf5.NetworkHelper;

/**
 * This class should not exist inside Geppetto and should be replaced when a proper library capable of reading a NeuroML and Lems file exists. This class is called Optimized reader because it uses a
 * local version of the NeuroML components rather than downloading it from the server every time. This class processes all the inclusion found inside a NeuroML document or a Lems one and puts them all
 * together. Regular expressions are used to optimized searching and replacing inside String. Once a proper library is in place there would be no reason to work with strings.
 * 
 * @author matteocantarelli
 * 
 */
public class OptimizedLEMSReader
{
	private static Log _logger = LogFactory.getLog(OptimizedLEMSReader.class);
	private static final List<String> NeuroMLInclusions = Arrays.asList("https://raw.github.com/NeuroML/NeuroML2/master/NeuroML2CoreTypes/NeuroML2CoreTypes.xml", "NeuroML2CoreTypes.xml",
			"NeuroMLCoreCompTypes.xml", "NeuroMLCoreDimensions.xml", "Cells.xml", "Networks.xml", "Synapes.xml", "Inputs.xml", "Channels.xml", "PyNN.xml");
	private static final String NMLHEADER = "<neuroml xmlns=\"http://www.neuroml.org/schema/neuroml2\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.neuroml.org/schema/neuroml2  https://raw.github.com/NeuroML/NeuroML2/development/Schemas/NeuroML2/NeuroML_v2beta4.xsd\" id=\"NeuroML_2_loaded_via_LEMS_by_Geppetto\">";
	private static final String simulationInclusion = "Simulation.xml";

	private List<String> _inclusions = new ArrayList<String>();

	private StringBuffer _LEMSString;
	private String _neuroMLString;
	private List<URL> dependentModels;

	private ILEMSDocument lemsDocument;
    
    private NetworkHelper networkHelper;

	public OptimizedLEMSReader(List<URL> dependentModels) throws NeuroMLException
	{
		super();
		this.dependentModels = dependentModels;
	}

	public void readAllFormats(URL url) throws IOException, NeuroMLException, LEMSException
	{
		// we only need to give a projectID if a remote h5 file is being read
		readAllFormats(url, null);
	}

	public void readAllFormats(URL url, Long projectID) throws IOException, NeuroMLException, LEMSException
	{
		int index = url.toString().lastIndexOf('/');
		String urlBase = url.toString().substring(0, index + 1);
        
                NeuroMLConverter neuromlConverter = new NeuroMLConverter();
        
                if (url.toString().endsWith("hdf5") || url.toString().endsWith("h5"))
                    {
                        String loc;
                        // local h5 file
                        if (url.getProtocol().equals("file")) {
                            loc = url.toString().substring(5);
                        // remote h5 file, create local copy and get its path
                        } else if (url.getProtocol().startsWith("http") && projectID != null) {
				Scope scope = Scope.CONNECTION;
				loc = URLReader.createLocalCopy(scope, projectID, url, false).toString().substring(5);
                        } else {
				throw new IOException("Unrecognized protocol " + url.toString());
                        }
                        File f = new File(loc);
            
                        // load without including includes
                        networkHelper = neuromlConverter.loadNeuroMLOptimized(f, false);

                        _neuroMLString = neuromlConverter.neuroml2ToXml(networkHelper.getNeuroMLDocument());

                        // expand inclusions
                        try {
				_neuroMLString = processLEMSInclusions(_neuroMLString, urlBase).toString();
                        } catch(JAXBException | NeuroMLException | URISyntaxException e) {
				throw new IOException(e);
                        }

                        // refresh the neuroml document with expanded string (does not reprocess populations/projections)
                        networkHelper.setNeuroMLDocument(neuromlConverter.loadNeuroML(_neuroMLString));
                        Sim sim = Utils.readLemsNeuroMLFile(NeuroMLConverter.convertNeuroML2ToLems(_neuroMLString));
                        lemsDocument = sim.getLems();
                    }
                else
                    {
                        read(url, urlBase); // expand it to have all the inclusions

                        // Reading NEUROML file
                        // Let's extract first the neuroml file so that if we have an error resolving the lems object at least we have the neuroml doc to validate it against neuroml.model
                        // We will show warning and error instead of an incomprehensible exception
                        long start = System.currentTimeMillis();
            
                        _neuroMLString = NMLHEADER + System.getProperty("line.separator") + trimOuterElement(getLEMSString()) + System.getProperty("line.separator") + "</neuroml>";

                        networkHelper = neuromlConverter.loadNeuroMLOptimized(_neuroMLString);
            
                        _logger.info("Parsed NeuroML document of size " + getNeuroMLString().length() / 1024 + "KB, took " + (System.currentTimeMillis() - start) + "ms");

                        // Reading LEMS files
                        start = System.currentTimeMillis();
                        Sim sim = Utils.readLemsNeuroMLFile(NeuroMLConverter.convertNeuroML2ToLems(_neuroMLString));
                        lemsDocument = sim.getLems();
                        _logger.info("Parsed LEMS document, took " + (System.currentTimeMillis() - start) + "ms");
                    }

	}

	/**
	 * @param url
	 *            A url which can point to either a Lems file or a NeuroML one
	 * @param includeNeuroML
	 *            if specified forces the inclusion of the NeuroML libraries even if no includes for them are found
	 * @return A string containing all the models included via the root one (and in nested children) in the same file
	 * @throws IOException
	 */
	public void read(URL url, String urlBase) throws IOException
	{
		try
		{
			long start = System.currentTimeMillis();
			dependentModels.add(url);
			_LEMSString = processLEMSInclusions(URLReader.readStringFromURL(url).replaceAll("<\\?xml(.*)\\?>", "").trim(), urlBase);
			_logger.info("Processed all inclusions, took " + (System.currentTimeMillis() - start) + "ms");

		}
		catch(JAXBException | NeuroMLException | URISyntaxException e)
		{
			throw new IOException(e);
		}
	}

	public String getNeuroMLString()
	{
		return _neuroMLString;
	}

	public String getLEMSString()
	{
		return _LEMSString.toString();
	}

	public ILEMSDocument getPartialLEMSDocument()
	{
		return lemsDocument;
	}

	public NeuroMLDocument getPartialNeuroMLDocument()
	{
		return networkHelper.getNeuroMLDocument();
	}

    public NetworkHelper getNetworkHelper()
    {
        return networkHelper;
    }
    
    

	/**
	 * @param documentString
	 * @return
	 * @throws IOException
	 * @throws JAXBException
	 * @throws NeuroMLException
	 * @throws URISyntaxException
	 */
	private StringBuffer processLEMSInclusions(String documentString, String urlBase) throws IOException, JAXBException, NeuroMLException, URISyntaxException
	{
		// 1. We receive a document, it could be NeuroML or LEMS, we remove useless parts as optimization
		String smallerDocumentString = cleanLEMSNeuroMLDocument(documentString);
		StringBuffer processedLEMSString = new StringBuffer();

		// 2. We look for includes, they could be includes of a LEMS or a NeuroML file
		// String regExp = "\\<include\\s*(href|file|url)\\s*=\\s*\\\"(.*)\\\"\\s*\\/>";
		String regExp = "\\<include\\s*(href|file|url)\\s*=\\s*\\\"(.*)\\\"\\s*(\\/>|><\\/include>)";
		Pattern pattern = Pattern.compile(regExp, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(smallerDocumentString);

		while(matcher.find())
		{
			long start = System.currentTimeMillis();
			String kind = matcher.group(1);
			String urlPath = "";
			String lemsInclusion = "";

			if(kind.equals("file"))
			{
				urlPath = "file:///" + matcher.group(2);
			}
			else if(kind.equals("href"))
			{
				if(urlBase != null)
				{
					urlPath = urlBase + matcher.group(2);
				}
			}
			else if(kind.equals("url"))
			{
				urlPath = matcher.group(2);
			}

			_logger.info("Check inclusion " + urlPath);

			if(!_inclusions.contains(new URI(urlPath).normalize().toString()))
			{
				URL urlToProcess = new URL(urlPath);
				String domain = null;
				if(urlToProcess.getProtocol().equals("file"))
				{
					domain = "file:///";
				}
				else
				{
					domain = urlToProcess.getProtocol() + "://";
				}

				if(urlToProcess.getAuthority() != null)
				{
					domain += urlToProcess.getAuthority() + "/";
				}

				String spec = urlToProcess.getPath().substring(1);
				String os =  System.getProperty("os.name");
				//In Windows, the spec variable returned above starts with char 'C', throwing a malformed
				//exception, this piece of code forces adding 'file:///' to avoid this issue
				if(os.startsWith("Windows")||os.startsWith("win32")){
					if(spec.startsWith("C" )||spec.startsWith("c")){
						spec= "file:///"+spec;
					}
				}
				URL url = new URL(new URL(domain),spec);

				// Check if it's the inclusion of some NML standard component types
				if(!isNeuroMLInclusion(url.toExternalForm()) && !url.toExternalForm().equals(simulationInclusion) && !url.toExternalForm().endsWith("/" + simulationInclusion))
				{
					// OK It's something else
					try
					{
						_inclusions.add(new URI(urlPath).normalize().toString());
						String s = URLReader.readStringFromURL(url);

						// If it is file and is not found, try to read at url base + file name
						if(s.equals("") && kind.equals("file"))
						{
							//a relative path has a / at the beginning, let's check for it and 
							//remove it to avoid having an extra / that throws file not found exception
							String urlPathBase = urlPath.replace("file:///", "");
							if(urlPathBase.charAt(0)=='/'){
								urlPathBase = urlPathBase.substring(1, urlPathBase.length());
							}
							urlPath = urlBase +urlPathBase ;
							url = new URL(urlPath);
							_inclusions.add(new URI(urlPath).normalize().toString());
							s = URLReader.readStringFromURL(url);
						}

						dependentModels.add(new URL(urlPath));

						int index = url.toString().lastIndexOf('/');
						String newUrlBase = url.toString().substring(0, index + 1);
						lemsInclusion = trimOuterElement(processLEMSInclusions(s, newUrlBase).toString());

					}
					catch(IOException | NeuroMLException e)
					{
						_logger.warn(e.toString());
					}
				}
			}

			matcher.appendReplacement(processedLEMSString, lemsInclusion);
			_logger.info("Inclusion iteration completed, took " + (System.currentTimeMillis() - start) + "ms");
		}

		matcher.appendTail(processedLEMSString);
		return processedLEMSString;
	}

	/**
	 * @param s
	 * @return
	 */
	private String cleanLEMSNeuroMLDocument(String lemsString)
	{
		String smallerLemsString = lemsString.replaceAll("(?s)<!--.*?-->", ""); // remove comments
		smallerLemsString = smallerLemsString.replaceAll("(?m)^[ \t]*\r?\n", "").trim();// remove empty lines
		return smallerLemsString;
	}

	/**
	 * @param include
	 * @return
	 */
	private boolean isNeuroMLInclusion(String include)
	{
		for(String n : NeuroMLInclusions)
		{
			if(include.contains(n))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * @param s
	 * @return
	 */
	private String trimOuterElement(String s)
	{
		String processedString = s.replaceAll("<\\?xml(.*)\\?>", ""); // remove xml tag
		processedString = processedString.replaceAll("<(Lems|neuroml)([\\s\\S]*?)>", "");// remove neuroml or lems tags
		processedString = processedString.replaceAll("</(Lems|neuroml)([\\s\\S]*?)>", ""); // remove close neuroml or lems tags
		return cleanLEMSNeuroMLDocument(processedString);
	}
    
    /*
    ****    TEMP: to be removed before merging...
    */
	public static void main(String[] args) throws Exception
	{
        
        String modelPath = "/acnet2/MediumNet.net.nml";
        modelPath = "/acnet2/TwoCell.net.nml";
        modelPath = "/acnet2/MediumNet.net.nml";
        //modelPath = "/acnet2/MediumNet.net.nml.h5";
        
        List<URL> dependentModels = new ArrayList<URL>();

        //URL url = new URL("file:///home/padraig/geppetto/org.geppetto.model.neuroml/src/test/resources/Balanced/Balanced.net.nml.h5");
        URL url = new URL("file:///home/padraig/geppetto/org.geppetto.model.neuroml/src/test/resources"+modelPath);
        System.out.println("URL: "+url);
        OptimizedLEMSReader olr = new OptimizedLEMSReader(dependentModels);
        System.out.println("Loading: "+modelPath);
        olr.readAllFormats(url);

        System.out.println("Done: "+olr.getNetworkHelper());
        
        NeuroMLConverter nmlConv = new NeuroMLConverter();
        System.out.println("NML2 Info: "+nmlConv.summary(olr.getPartialNeuroMLDocument()));
        System.out.println("One conn "+ NeuroMLConverter.connectionInfo(olr.getNetworkHelper().getConnection("SmallNet_bask_bask", 0)));
        System.out.println("LEMS Info: "+olr.getPartialLEMSDocument().toString());
        
    }

}
