/**
 * 
 */
package org.geppetto.model.neuroml.utils;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.geppetto.core.utilities.URLReader;
import org.lemsml.jlems.core.logging.E;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.util.NeuroMLConverter;

/**
 * @author matteocantarelli
 * 
 */
public class OptimizedLEMSReader
{
	private static final List<String> NeuroMLInclusions = Arrays.asList("https://raw.github.com/NeuroML/NeuroML2/master/NeuroML2CoreTypes/NeuroML2CoreTypes.xml", "NeuroML2CoreTypes.xml",
			"NeuroMLCoreCompTypes.xml", "NeuroMLCoreDimensions.xml", "Cells.xml", "Networks.xml", "Synapes.xml", "Inputs.xml", "Channels.xml");
	private static final String simulationInclusion = "Simulation.xml";
	private boolean _neuroMLIncluded = false;
	private boolean _simulationIncluded = false;
	private Map<String,NeuroMLDocument> _neuroMLs = new HashMap<String,NeuroMLDocument>();
			



	/**
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public String read(URL url) throws IOException
	{
		try
		{
			return processLEMSInclusions(URLReader.readStringFromURL(url));
		}
		catch(JAXBException e)
		{
			throw new IOException(e);
		}
	}

	/**
	 * @param lemsString
	 * @return
	 * @throws IOException
	 * @throws JAXBException 
	 */
	private String processLEMSInclusions(String lemsString) throws IOException, JAXBException
	{
		String processedLEMSString = lemsString;
		String URLInclusion = "<Include ";
		while(processedLEMSString.contains(URLInclusion))
		{
			int inclusionStart = processedLEMSString.indexOf(URLInclusion);
			int urlStart = processedLEMSString.indexOf("\"", inclusionStart) + 1;
			int urlEnd = processedLEMSString.indexOf("\"", urlStart + 1);
			int inclusionEnd = processedLEMSString.indexOf(">", urlEnd);
			String inclusion = processedLEMSString.substring(inclusionStart, inclusionEnd + 1);
			String urlPath = processedLEMSString.substring(urlStart, urlEnd);
			if(inclusion.startsWith("<Include file"))
			{
				urlPath = "file:///" + urlPath;
			}
			URL url = new URL(urlPath);
			String content = "";
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
			else if(url.toExternalForm().equals(simulationInclusion) || url.toExternalForm().endsWith("/"+simulationInclusion))
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
				String s=URLReader.readStringFromURL(url);
				if(url.toExternalForm().endsWith("nml"))
				{
					//it's a neuroML file
					NeuroMLConverter neuromlConverter = new NeuroMLConverter();
					NeuroMLDocument neuroml = neuromlConverter.urlToNeuroML(url);
					_neuroMLs.put(url.getFile(), neuroml);
				}
				content = trimOuterElement(processLEMSInclusions(s));
			}
			processedLEMSString = processedLEMSString.replace(inclusion, content);

		}
		return processedLEMSString;
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
		String ret = "";
		String processedString = removeXMLComments(s);
		processedString = processedString.trim();

		if(processedString.startsWith("<?xml"))
		{
			int index = processedString.indexOf(">");
			processedString = processedString.substring(index + 1).trim();
		}

		if(processedString.startsWith("<"))
		{
			int isp = processedString.indexOf(" ");
			int ict = processedString.indexOf(">");
			String eltname = processedString.substring(1, (ict < isp ? ict : isp));
			int sco = processedString.indexOf(">");

			String ctag = "</" + eltname + ">";
			int ice = processedString.lastIndexOf(ctag);

			if(ice > sco)
			{
				ret = processedString.substring(sco + 1, ice);

			}
			else
			{
				int l = processedString.length();
				E.error("non matching XML close in include: open tag=" + eltname + " end= ..." + processedString.substring(l - 15, l));
			}
		}
		else
		{
			int l = processedString.length();
			E.error("Cant extract content from " + processedString.substring(0, (20 < l ? 20 : l)));
		}

		return ret;
	}

	/**
	 * @param xml
	 * @return
	 */
	private String removeXMLComments(String xml)
	{
		String ret = xml;
		while(ret.indexOf("<!--") >= 0)
		{
			int start = ret.indexOf("<!--");
			int end = ret.indexOf("-->") + 3;
			ret = ret.substring(0, start) + ret.substring(end);
		}
		return ret;
	}

	/**
	 * @return
	 */
	public Map<String, NeuroMLDocument> getNeuroMLs()
	{
		return _neuroMLs;
	}

}
