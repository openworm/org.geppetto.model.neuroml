/**
 * 
 */
package org.geppetto.model.neuroml.utils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.utilities.URLReader;
import org.lemsml.jlems.api.LEMSDocumentReader;
import org.lemsml.jlems.api.interfaces.ILEMSDocument;
import org.lemsml.jlems.api.interfaces.ILEMSDocumentReader;
import org.lemsml.jlems.core.sim.ContentError;
import org.lemsml.jlems.core.sim.LEMSException;
import org.lemsml.jlems.core.sim.Sim;
import org.neuroml.export.utils.Utils;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.util.NeuroMLConverter;
import org.neuroml.model.util.NeuroMLException;

import com.amazonaws.services.cloudfront.model.Paths;

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
	public enum NMLDOCTYPE
	{
		LEMS, NEUROML
	}

	private static Log _logger = LogFactory.getLog(OptimizedLEMSReader.class);
	private static final List<String> NeuroMLInclusions = Arrays.asList("https://raw.github.com/NeuroML/NeuroML2/master/NeuroML2CoreTypes/NeuroML2CoreTypes.xml", "NeuroML2CoreTypes.xml",
			"NeuroMLCoreCompTypes.xml", "NeuroMLCoreDimensions.xml", "Cells.xml", "Networks.xml", "Synapes.xml", "Inputs.xml", "Channels.xml", "PyNN.xml");
	private static final String NMLHEADER = "<neuroml xmlns=\"http://www.neuroml.org/schema/neuroml2\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.neuroml.org/schema/neuroml2  http://neuroml.svn.sourceforge.net/viewvc/neuroml/NeuroML2/Schemas/NeuroML2/NeuroML_v2alpha.xsd\">";
	private static final String simulationInclusion = "Simulation.xml";

	private List<String> _inclusions = new ArrayList<String>();

	private StringBuffer _LEMSString;
	private StringBuffer _neuroMLString;
	private List<URL> dependentModels;

	private ILEMSDocument lemsDocument;
	private NeuroMLDocument neuromlDocument;

	public OptimizedLEMSReader(List<URL> dependentModels) throws NeuroMLException
	{
		super();
		this.dependentModels = dependentModels;
	}

	public void readAllFormats(URL url, NMLDOCTYPE type) throws IOException, NeuroMLException, LEMSException
	{
		int index = url.toString().lastIndexOf('/');
		String urlBase = url.toString().substring(0, index + 1);
		read(url, urlBase, type); // expand it to have all the inclusions

		// Reading NEUROML file 
		// Let's extract first the neuroml file so that if we have an error resolving the lems object at least we have the neuroml doc to validate it against neuroml.model
		// We will show warning and error instead of an incomprehensible exception 
		long start = System.currentTimeMillis();
		NeuroMLConverter neuromlConverter = new NeuroMLConverter();
		neuromlDocument = neuromlConverter.loadNeuroML(getNeuroMLString());
		_logger.info("Parsed NeuroML document of size " + getNeuroMLString().length() / 1024 + "KB, took " + (System.currentTimeMillis() - start) + "ms");

		// Reading LEMS files
		start = System.currentTimeMillis();
		Sim sim = Utils.readLemsNeuroMLFile(NeuroMLConverter.convertNeuroML2ToLems(getNeuroMLString()));
		lemsDocument = sim.getLems();
		_logger.info("Parsed LEMS document, took " + (System.currentTimeMillis() - start) + "ms");

	}

	/**
	 * @param url
	 *            A url which can point to either a Lems file or a NeuroML one
	 * @param includeNeuroML
	 *            if specified forces the inclusion of the NeuroML libraries even if no includes for them are found
	 * @return A string containing all the models included via the root one (and in nested children) in the same file
	 * @throws IOException
	 */
	public void read(URL url, String urlBase, NMLDOCTYPE type) throws IOException
	{
		try
		{
			long start = System.currentTimeMillis();
			dependentModels.add(url);
			Map<NMLDOCTYPE, StringBuffer> returned = processLEMSInclusions(URLReader.readStringFromURL(url).replaceAll("<\\?xml(.*)\\?>", "").trim(), urlBase, type);
			_neuroMLString = returned.get(NMLDOCTYPE.NEUROML);
			_LEMSString = returned.get(NMLDOCTYPE.LEMS);
			_logger.info("Processed all inclusions, took " + (System.currentTimeMillis() - start) + "ms");

		}
		catch(JAXBException | NeuroMLException | URISyntaxException e)
		{
			throw new IOException(e);
		}
	}

	public String getNeuroMLString()
	{
		return _neuroMLString.toString();
	}

	public String getLEMSString()
	{
		return _LEMSString.toString();
	}

	public ILEMSDocument getLEMSDocument()
	{
		return lemsDocument;
	}

	public NeuroMLDocument getNeuroMLDocument()
	{
		return neuromlDocument;
	}

	/**
	 * @param documentString
	 * @return
	 * @throws IOException
	 * @throws JAXBException
	 * @throws NeuroMLException
	 * @throws URISyntaxException 
	 */
	private Map<NMLDOCTYPE, StringBuffer> processLEMSInclusions(String documentString, String urlBase, NMLDOCTYPE type) throws IOException, JAXBException, NeuroMLException, URISyntaxException
	{
		// 1. We receive a document, it could be NeuroML or LEMS, we remove useless parts as optimization
		String smallerDocumentString = cleanLEMSNeuroMLDocument(documentString);

		// We create two string buffers one which will contain the NML representation of this include and one that will include the LEMS one
		Map<NMLDOCTYPE, StringBuffer> processedDocs = new HashMap<OptimizedLEMSReader.NMLDOCTYPE, StringBuffer>();
		StringBuffer processedNMLString = new StringBuffer();
		StringBuffer processedLEMSString = new StringBuffer();
		processedDocs.put(NMLDOCTYPE.NEUROML, processedNMLString);
		processedDocs.put(NMLDOCTYPE.LEMS, processedLEMSString);

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
			String nmlInclusion = "";

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

			// Let's figure out if it's a NML document or a LEMS one
			NMLDOCTYPE inclusionType = null;
			if(urlPath.endsWith(".nml") || urlPath.endsWith(".nml?dl=1"))
			{
				inclusionType = NMLDOCTYPE.NEUROML;
			}
			else if(urlPath.endsWith(".xml") || urlPath.endsWith(".xml?dl=1"))
			{
				inclusionType = NMLDOCTYPE.LEMS;
			}

			_logger.info("Check inclusion " + urlPath);

			if(!_inclusions.contains(new URI(urlPath).normalize().toString()))
			{
				URL url = new URL(urlPath);

				// Check if it's the inclusion of some NML standard component types
				if(!isNeuroMLInclusion(url.toExternalForm()) && !url.toExternalForm().equals(simulationInclusion) && !url.toExternalForm().endsWith("/" + simulationInclusion))
				{

					// OK It's something else
					try
					{
						
						_inclusions.add(new URI(urlPath).normalize().toString());
						dependentModels.add(new URL(urlPath));
						String s = URLReader.readStringFromURL(url);

						// If it is file and is not found, try to read at url base + file name
						if(s.equals("") && kind.equals("file"))
						{
							urlPath = urlBase + urlPath.replace("file:///", "");
							url = new URL(urlPath);
							_inclusions.add(new URI(urlPath).normalize().toString());
							s = URLReader.readStringFromURL(url);
						}

						int index = url.toString().lastIndexOf('/');
						String newUrlBase = url.toString().substring(0, index + 1);
						Map<NMLDOCTYPE, StringBuffer> included = processLEMSInclusions(s, newUrlBase, inclusionType);
						lemsInclusion = trimOuterElement(included.get(NMLDOCTYPE.LEMS).toString()); // lems representation of the inclusion
						nmlInclusion = trimOuterElement(included.get(NMLDOCTYPE.NEUROML).toString()); // nml representation of the inclusion

					}
					catch(IOException | NeuroMLException e)
					{
						_logger.warn(e.toString());
					}
				}
			}

			switch(type)
			{
				case LEMS:
					matcher.appendReplacement(processedLEMSString, lemsInclusion);
					addToNeuroML(processedNMLString, nmlInclusion);
					break;

				case NEUROML:
					matcher.appendReplacement(processedNMLString, nmlInclusion);
					// NML is also LEMS
					addToLems(processedLEMSString, lemsInclusion);
					break;
			}

			_logger.info("Inclusion iteration completed, took " + (System.currentTimeMillis() - start) + "ms");
		}

		switch(type)
		{
			case LEMS:
				matcher.appendTail(processedLEMSString);
				break;
			case NEUROML:
				int end = processedNMLString.length();
				matcher.appendTail(processedNMLString);
				addToLems(processedLEMSString, trimOuterElement(processedNMLString.toString().substring(end)));
				break;
		}

		return processedDocs;
	}

	/**
	 * @param NMLString
	 * @param toAdd
	 */
	private void addToNeuroML(StringBuffer NMLString, String toAdd)
	{
		if(!toAdd.isEmpty())
		{
			if(NMLString.toString().isEmpty())
			{
				NMLString.append(NMLHEADER + System.getProperty("line.separator") + toAdd + System.getProperty("line.separator") + "</neuroml>");
			}
			else
			{
				String neuroMLRegex = "<(neuroml)([\\s\\S]*?)>";
				Pattern nmlpattern = Pattern.compile(neuroMLRegex, Pattern.CASE_INSENSITIVE);
				Matcher nmlMatcher = nmlpattern.matcher(NMLString);
				if(nmlMatcher.find())
				{
					// NeuroML definitions are added only to
					NMLString.insert(nmlMatcher.end(), System.getProperty("line.separator") + toAdd + System.getProperty("line.separator"));
				}
			}
		}
	}

	private void addToLems(StringBuffer LEMSString, String toAdd)
	{
		if(!toAdd.isEmpty())
		{
			if(LEMSString.toString().isEmpty())
			{
				LEMSString.append("<Lems>" + System.getProperty("line.separator") + toAdd + System.getProperty("line.separator") + "</Lems>");
			}
			else
			{
				String neuroMLRegex = "<(Lems)([\\s\\S]*?)>";
				Pattern nmlpattern = Pattern.compile(neuroMLRegex, Pattern.CASE_INSENSITIVE);
				Matcher nmlMatcher = nmlpattern.matcher(LEMSString);
				if(nmlMatcher.find())
				{
					// NeuroML definitions are added only to
					LEMSString.insert(nmlMatcher.end(), System.getProperty("line.separator") + toAdd + System.getProperty("line.separator"));
				}
			}
		}
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

}
