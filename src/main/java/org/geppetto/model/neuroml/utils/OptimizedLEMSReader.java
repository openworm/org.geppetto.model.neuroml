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
 * This class should not exist inside Geppetto and should be replaced when a proper library capable of reading a NeuroML and Lems file exists.
 * This class is called Optimized reader because it uses a local version of the NeuroML components rather than downloading it from the server every time.
 * This class processes all the inclusion found inside a NeuroML document or a Lems one and puts them all together.
 * Regular expressions are used to optimized searching and replacing inside String. Once a proper library is in place there would be no reason to work with strings.
 * 
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
	private NeuroMLConverter _neuromlConverter = null;
	private List<String> _inclusions = new ArrayList<String>();
	private String _urlBase;
	private boolean _includeNeuroML = false;

	public OptimizedLEMSReader() throws NeuroMLException
	{
		super();
	}

	public OptimizedLEMSReader(String urlBase) throws NeuroMLException
	{
		super();
		this._urlBase = urlBase;
	}


	/**
	 * @param url A url which can point to either a Lems file or a NeuroML one
	 * @param includeNeuroML if specified forces the inclusion of the NeuroML libraries even if no includes for them are found
	 * @return A string containing all the models included via the root one (and in nested children) in the same file 
	 * @throws IOException
	 */
	public String read(URL url, boolean includeNeuroML) throws IOException
	{
		return read(URLReader.readStringFromURL(url), includeNeuroML);

	}


	/**
	 * @param content A string containing either a Lems model or a NeuroML one
	 * @param includeNeuroML if specified forces the inclusion of the NeuroML libraries even if no includes for them are found
	 * @return A string containing all the models included via the root one (and in nested children) in the same file 
	 * @throws IOException
	 */
	public String read(String content, boolean includeNeuroML) throws IOException
	{
		_includeNeuroML = includeNeuroML;
		try
		{
			return processLEMSInclusions(content);
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
	private String processLEMSInclusions(String lemsString) throws IOException, JAXBException, NeuroMLException
	{
		String smallerLemsString = lemsString.replaceAll("(?s)<!--.*?-->", ""); // remove comments
		smallerLemsString = smallerLemsString.replaceAll("<notes>([\\s\\S]*?)</notes>", ""); // remove notes
		smallerLemsString = smallerLemsString.replaceAll("(?m)^[ \t]*\r?\n", "").trim();// remove empty lines

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
				if(this._urlBase != null)
				{
					urlPath = this._urlBase + matcher.group(2);
				}
			}
			else if(kind.equals("url"))
			{
				urlPath = matcher.group(2);
			}

			_logger.info("LEMS check inclusion " + urlPath);
			if(!_inclusions.contains(urlPath))
			{
				URL url = new URL(urlPath);

				if(!_neuroMLIncluded && isNeuroMLInclusion(url.toExternalForm()))
				{
					content = URLReader.readStringFromURL(this.getClass().getResource("/NEUROML2BETA"));
					_neuroMLIncluded = true;
					_simulationIncluded = true;

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
						_inclusions.add(urlPath);
						long startRead = System.currentTimeMillis();
						String s = URLReader.readStringFromURL(url);
						_logger.info("Reading of " + url.toString() + " took " + (System.currentTimeMillis() - startRead) + "ms");

						if(url.toExternalForm().endsWith("nml"))
						{
							startRead = System.currentTimeMillis();
							// it's a neuroML file
							_neuromlConverter = new NeuroMLConverter(); // It throws a NPE in some situations (Celegans.net) if the instance is reused :S
							NeuroMLDocument neuroml = _neuromlConverter.loadNeuroML(s);
							_neuroMLs.put(url.getFile(), neuroml);
							_logger.info("NeuroML parsing of " + url.toString() + " took " + (System.currentTimeMillis() - startRead) + "ms");
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
		
		//if tge flag was forcing to include neuroML component AND we haven't done it yet add them after the <neuroml> or <Lems> tag
		if(_includeNeuroML && !_neuroMLIncluded)
		{
			String neuroMLRegex="<(Lems|neuroml)([\\s\\S]*?)>";
			Pattern nmlpattern = Pattern.compile(neuroMLRegex, Pattern.CASE_INSENSITIVE);
			Matcher nmlMatcher = nmlpattern.matcher(processedLEMSString);
			if(nmlMatcher.find())
			{
				processedLEMSString.insert(nmlMatcher.end(), URLReader.readStringFromURL(this.getClass().getResource("/NEUROML2BETA")));
			}
		}
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
		String processedString = s.replaceAll("<\\?xml(.*)\\?>", ""); // remove xml tag
		processedString = processedString.replaceAll("<(Lems|neuroml)([\\s\\S]*?)>", "");// remove neuroml or lems tags
		processedString = processedString.replaceAll("</(Lems|neuroml)([\\s\\S]*?)>", ""); // remove close neuroml or lems tags
		return processedString;
	}

	/**
	 * @return
	 */
	public Map<String, NeuroMLDocument> getNeuroMLs()
	{
		return _neuroMLs;
	}

}
