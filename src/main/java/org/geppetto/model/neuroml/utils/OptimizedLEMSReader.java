/**
 * 
 */
package org.geppetto.model.neuroml.utils;

import java.io.IOException;
import java.net.URL;
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
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.util.NeuroMLConverter;
import org.neuroml.model.util.NeuroMLException;

/**
 * @author matteocantarelli
 * 
 */
public class OptimizedLEMSReader
{
	private static Log _logger = LogFactory.getLog(OptimizedLEMSReader.class);
	private static final List<String> NeuroMLInclusions = Arrays.asList("https://raw.github.com/NeuroML/NeuroML2/master/NeuroML2CoreTypes/NeuroML2CoreTypes.xml", "NeuroML2CoreTypes.xml",
			"NeuroMLCoreCompTypes.xml", "NeuroMLCoreDimensions.xml", "Cells.xml", "Networks.xml", "Synapes.xml", "Inputs.xml", "Channels.xml", "PyNN.xml");
	private static final String simulationInclusion = "Simulation.xml";
	private boolean _neuroMLIncluded = false;
	private boolean _simulationIncluded = false;
	private Map<String, NeuroMLDocument> _neuroMLs = new HashMap<String, NeuroMLDocument>();
	private NeuroMLConverter neuromlConverter =null;
	private List<String> inclusions = new ArrayList<String>();

	private String urlBase;

	public OptimizedLEMSReader() throws NeuroMLException
	{
		super();
	}

	public OptimizedLEMSReader(String urlBase) throws NeuroMLException
	{
		super();
		this.urlBase = urlBase;
	}

	/**
	 * @param url
	 * @return
	 * @throws IOException
	 * @throws NeuroMLException
	 */
	public String read(URL url) throws IOException, NeuroMLException
	{
		try
		{
			return processLEMSInclusions(URLReader.readStringFromURL(url));
		}
		catch(JAXBException | NeuroMLException e)
		{
			throw new IOException(e);
		}
	}

	/**
	 * @param lemsString
	 * @return
	 * @throws IOException
	 * @throws JAXBException
	 * @throws NeuroMLException
	 */
	public String processLEMSInclusions(String lemsString) throws IOException, JAXBException, NeuroMLException
	{
		String smallerLemsString = lemsString.replaceAll( "(?s)<!--.*?-->", "" ); //remove comments
		smallerLemsString = smallerLemsString.replaceAll("<notes>([\\s\\S]*?)</notes>", ""); //remove notes
		smallerLemsString = smallerLemsString.replaceAll("(?m)^[ \t]*\r?\n", "").trim();//remove empty lines
		
		StringBuffer processedLEMSString = new StringBuffer(smallerLemsString.length());

		String regExp = "\\<include\\s*(href|file|url)\\s*=\\s*\\\"(.*)\\\"\\s*\\/>";
		Pattern pattern = Pattern.compile(regExp, Pattern.CASE_INSENSITIVE);

		Matcher matcher = pattern.matcher(smallerLemsString);

		while(matcher.find())
		{
			String kind = matcher.group(1);
			String urlPath = "";
			String content = "";
			if(kind.equals("file"))
			{
				urlPath = "file:///" + matcher.group(2);
			}
			else if(kind.equals("href"))
			{
				if(this.urlBase != null)
				{
					urlPath = this.urlBase + matcher.group(2);
				}
			}
			else if(kind.equals("url"))
			{
				urlPath = matcher.group(2);
			}

			_logger.info("LEMS check inclusion " + urlPath);
			if(!inclusions.contains(urlPath))
			{
				URL url = new URL(urlPath);

				if(isNeuroMLInclusion(url.toExternalForm()))
				{
					// use the local version
					if(!_neuroMLIncluded)
					{
						content = URLReader.readStringFromURL(this.getClass().getResource("/NEUROML2BETA"));

						_neuroMLIncluded = true;
						_simulationIncluded = true;
					}
				}
				else if(url.toExternalForm().equals(simulationInclusion) || url.toExternalForm().endsWith("/" + simulationInclusion))
				{
					// use the local version
					if(!_simulationIncluded)
					{
						content = URLReader.readStringFromURL(this.getClass().getResource("/SIMULATION"));
						_simulationIncluded = true;
					}
				}
				else
				{
					try
					{
						inclusions.add(urlPath);
						String s = URLReader.readStringFromURL(url);
						if(url.toExternalForm().endsWith("nml"))
						{
							// it's a neuroML file
							neuromlConverter= new NeuroMLConverter(); //It throws a NPE if the instance is reused :S
							NeuroMLDocument neuroml = neuromlConverter.urlToNeuroML(url);
							_neuroMLs.put(url.getFile(), neuroml);
						}
						content = trimOuterElement(processLEMSInclusions(s));

					}
					catch(IOException e)
					{
						_logger.warn(e.toString());
						content = "";
					}

				}
			}
			matcher.appendReplacement(processedLEMSString, content);

		}
		matcher.appendTail(processedLEMSString);
		return processedLEMSString.toString();
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
		String processedString = s.replaceAll("<\\?xml(.*)\\?>", ""); //remove xml tag
		processedString = processedString.replaceAll("<(lems|neuroml)([\\s\\S]*?)>", "");//remove neuroml or lems tags
		processedString = processedString.replaceAll("</(lems|neuroml)([\\s\\S]*?)>", ""); //remove close neuroml or lems tags
		return processedString;
	}


	/**
	 * @return
	 */
	public Map<String, NeuroMLDocument> getNeuroMLs()
	{
		return _neuroMLs;
	}

	/**
	 * @param inclusions
	 */
	public void setInclusions(List<String> inclusions)
	{
		this.inclusions = inclusions;
	}

}
