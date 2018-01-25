

package org.geppetto.model.neuroml.summaryUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.emf.common.util.EList;
import org.geppetto.core.model.GeppettoModelAccess;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.services.GeppettoFeature;
import org.geppetto.model.Node;
import org.geppetto.model.neuroml.features.DefaultViewCustomiserFeature;
import org.geppetto.model.neuroml.utils.CellUtils;
import org.geppetto.model.neuroml.utils.ModelInterpreterUtils;
import org.geppetto.model.neuroml.utils.Resources;
import org.geppetto.model.neuroml.utils.ResourcesDomainType;
import org.geppetto.model.neuroml.visualUtils.ModelInterpreterVisualConstants;
import org.geppetto.model.neuroml.visualUtils.PopulateChannelDensityVisualGroups;
import org.geppetto.model.types.ArrayType;
import org.geppetto.model.types.CompositeType;
import org.geppetto.model.types.CompositeVisualType;
import org.geppetto.model.types.Type;
import org.geppetto.model.types.TypesFactory;
import org.geppetto.model.types.TypesPackage;
import org.geppetto.model.types.VisualType;
import org.geppetto.model.util.GeppettoVisitingException;
import org.geppetto.model.values.Argument;
import org.geppetto.model.values.Dynamics;
import org.geppetto.model.values.Expression;
import org.geppetto.model.values.Function;
import org.geppetto.model.values.FunctionPlot;
import org.geppetto.model.values.HTML;
import org.geppetto.model.values.Text;
import org.geppetto.model.values.ValuesFactory;
import org.geppetto.model.values.VisualGroup;
import org.geppetto.model.variables.Variable;
import org.geppetto.model.variables.VariablesFactory;
import org.lemsml.jlems.core.eval.DoubleEvaluator;
import org.lemsml.jlems.core.expression.ParseError;
import org.lemsml.jlems.core.sim.ContentError;
import org.lemsml.jlems.core.sim.LEMSException;
import org.lemsml.jlems.core.type.Component;
import org.neuroml.export.info.model.ExpressionNode;
import org.neuroml.export.info.model.InfoNode;
import org.neuroml.export.info.model.PlotMetadataNode;
import org.neuroml.export.utils.Utils;

import org.neuroml.model.Cell;
import org.neuroml.model.ChannelDensity;
import org.neuroml.model.ChannelDensityGHK;
import org.neuroml.model.ChannelDensityNernst;
import org.neuroml.model.ChannelDensityNonUniform;
import org.neuroml.model.ChannelDensityNonUniformNernst;
import org.neuroml.model.ExpOneSynapse;
import org.neuroml.model.ExpTwoSynapse;
import org.neuroml.model.GateHHInstantaneous;
import org.neuroml.model.GateHHRates;
import org.neuroml.model.GateHHRatesInf;
import org.neuroml.model.GateHHRatesTau;
import org.neuroml.model.GateHHRatesTauInf;
import org.neuroml.model.GateHHTauInf;
import org.neuroml.model.GateHHUndetermined;
import org.neuroml.model.InhomogeneousParameter;
import org.neuroml.model.IonChannel;
import org.neuroml.model.IonChannelHH;
import org.neuroml.model.Izhikevich2007Cell;
import org.neuroml.model.IzhikevichCell;
import org.neuroml.model.IafCell;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.PulseGenerator;
import org.neuroml.model.Segment;
import org.neuroml.model.SegmentGroup;
import org.neuroml.model.Standalone;
import org.neuroml.model.VariableParameter;
import org.neuroml.model.util.NeuroMLException;
import org.neuroml2.modellite.NeuroML2ModelReader;

/**
 * Populates the Model Tree of Aspect
 * 
 * @author Adrian Quintana (adrian.perez@ucl.ac.uk)
 * 
 */

public class PopulateSummaryNodesUtils
{
	private static Log logger = LogFactory.getLog(PopulateSummaryNodesUtils.class);
	private static String NOTES = "Notes";
	TypesFactory typeFactory = TypesFactory.eINSTANCE;
	VariablesFactory variablesFactory = VariablesFactory.eINSTANCE;
	ValuesFactory valuesFactory = ValuesFactory.eINSTANCE;

	GeppettoModelAccess access;
	Map<String, List<Type>> typesMap;
	Map<String, List<Variable>> plottableVariables = new HashMap<String, List<Variable>>();

	Type type;

	URL url;
	private NeuroMLDocument neuroMLDocument;
    
    private InfoNode nml2ModelInfo;

	boolean verbose = true;

	public PopulateSummaryNodesUtils(Map<String, List<Type>> typesMap, Type type, URL url, GeppettoModelAccess access, NeuroMLDocument neuroMLDocument)
	{
		this.access = access;
		this.typesMap = typesMap;
		this.url = url;
		this.type = type;
		this.neuroMLDocument = neuroMLDocument;
	}

	/**
	 * Creates all HTML variables for objects in maps.
	 */
	public void createHTMLVariables() throws ModelInterpreterException, GeppettoVisitingException, NeuroMLException, LEMSException
	{
		this.createCellsHTMLVariable();
		this.createSynapsesHTMLVariable();
		this.createChannelsHTMLVariable();
		this.createInputsHTMLVariable();
	}

	private void addList(List<Type> list, StringBuilder desc)
	{
		for(Type el : list)
		{
			desc.append("<a href=\"#\" instancePath=\"Model.neuroml." + el.getId() + "\">" + el.getName() + "</a>");
			if(el != list.get(list.size() - 1)) desc.append(" | ");
			desc.append("\n");
		}
	}

	/**
	 * Creates general Model description
	 * 
	 * @return
	 * @throws ModelInterpreterException
	 * @throws GeppettoVisitingException
	 * @throws NeuroMLException
	 * @throws LEMSException
	 */
	public Variable getDescriptionNode() throws ModelInterpreterException, GeppettoVisitingException, NeuroMLException, LEMSException
	{

		List<Type> networkComponents = typesMap.containsKey(ResourcesDomainType.NETWORK.get()) ? typesMap.get(ResourcesDomainType.NETWORK.get()) : null;
		List<Type> populationComponents = typesMap.containsKey(ResourcesDomainType.POPULATION.get()) ? typesMap.get(ResourcesDomainType.POPULATION.get()) : null;
		sortNodes(populationComponents);
		List<Type> cellComponents = typesMap.containsKey(ResourcesDomainType.CELL.get()) ? typesMap.get(ResourcesDomainType.CELL.get()) : null;
		sortNodes(cellComponents);
		List<Type> ionChannelComponents = typesMap.containsKey(ResourcesDomainType.IONCHANNEL.get()) ? typesMap.get(ResourcesDomainType.IONCHANNEL.get()) : null;
		sortNodes(ionChannelComponents);
		List<Type> synapseComponents = typesMap.containsKey(ResourcesDomainType.SYNAPSE.get()) ? typesMap.get(ResourcesDomainType.SYNAPSE.get()) : null;
		sortNodes(synapseComponents);
		List<Type> pulseGeneratorComponents = typesMap.containsKey(ResourcesDomainType.PULSEGENERATOR.get()) ? typesMap.get(ResourcesDomainType.PULSEGENERATOR.get()) : null;
		sortNodes(pulseGeneratorComponents);

		StringBuilder modelDescription = new StringBuilder();

		if(networkComponents != null && networkComponents.size() > 0)
		{
			modelDescription.append("Network: ");
			for(Type network : networkComponents)
			{
				modelDescription.append("<a href=\"#\" instancePath=\"Model.neuroml." + network.getId() + "\">" + network.getName() + "</a><br/><br/>\n");
				List<Variable> notesComponents = new ArrayList<Variable>();
				EList<Variable> netVariables = ((CompositeType) network).getVariables();
				for(Variable v : netVariables)
				{
					if(v.getId().equals(NOTES))
					{
						notesComponents.add(v);
					}
				}
				for(Variable note : notesComponents)
				{
					Text notes = (Text) note.getInitialValues().get(access.getType(TypesPackage.Literals.TEXT_TYPE));
					modelDescription.append("<b>Description</b><br/>\n<p instancePath=\"Model.neuroml." + note.getId() + "\">" + formatDescription(notes.getText()) + "</p>\n ");
				}
                
                nml2ModelInfo = NeuroML2ModelReader.extractExpressions(neuroMLDocument);
                
			}

		}
		modelDescription.append("<a target=\"_blank\" href=\"" + url.toString() + "\"><i>View the original NeuroML 2 source file</i></a><br/><br/>\n");

		if(populationComponents != null && populationComponents.size() > 0)
		{
			modelDescription.append("<b>Populations</b><br/>\n");
			for(Type population : populationComponents)
			{
                // TODO
				//modelDescription.append("<span style=\"color:#" + ((ArrayType) population).getVisualType() + "\">XXX</span>\n");
				modelDescription.append("" + population.getName() + ": ");
				// get proper name of population cell with brackets and index # of population
				int size = ((ArrayType) population).getSize();
				String name = ((ArrayType) population).getArrayType().getId().trim();
				modelDescription.append("<a href=\"#\" instancePath=\"Model.neuroml." + name + "\">" + size + " cell" + (size == 1 ? "s" : "") + " of type "
						+ ((ArrayType) population).getArrayType().getName() + "</a><br/>\n");
			}
			modelDescription.append("<br/>\n");
		}

		if(cellComponents != null && cellComponents.size() > 0)
		{
			modelDescription.append("<b>Cells</b><br/>  \n");
			addList(cellComponents, modelDescription);
			modelDescription.append("<br/><br/>\n");
		}

		if(ionChannelComponents != null && ionChannelComponents.size() > 0)
		{
			modelDescription.append("<b>Ion channels</b><br/>\n");
			for(Type ionChannel : ionChannelComponents)
			{
				modelDescription.append("<a href=\"#\" instancePath=\"Model.neuroml." + ionChannel.getId() + "\">" + ionChannel.getName() + "</a>");

				if(ionChannel != ionChannelComponents.get(ionChannelComponents.size() - 1)) modelDescription.append(" | \n");

				// Add expresion nodes from the export library for the gate rates
				//InfoNode in = addExpresionNodes((CompositeType) ionChannel);
                boolean found = false;
                if (nml2ModelInfo!=null)
                {
                    for (Map.Entry<String, Object> entry : nml2ModelInfo.getProperties().entrySet())
                    {
                        //System.out.println("Checking: "+((InfoNode)entry.getValue()).toDetailString("> "));
                        if (entry.getKey().equals(ionChannel.getName()))
                        {
                            found = extractPlottables((CompositeType) ionChannel, (InfoNode)entry.getValue());
                        }
                    }
                }
                //if (!found)
				//modelDescription.append("<br/>Not found!<br/>\n");
			}
			modelDescription.append("<br/><br/>\n");
		}

		if(synapseComponents != null && synapseComponents.size() > 0)
		{
			modelDescription.append("<b>Synapses</b><br/>\n");
			addList(synapseComponents, modelDescription);
			modelDescription.append("<br/><br/>\n");
		}

		if(pulseGeneratorComponents != null && pulseGeneratorComponents.size() > 0)
		{
			// FIXME: Pulse generator? InputList? ExplicitList?
			modelDescription.append("<b>Inputs</b><br/>\n");
			addList(pulseGeneratorComponents, modelDescription);
			modelDescription.append("<br/>\n");
		}

		// If there is nothing at least show a link to open the whole model in a tree visualiser
		if((networkComponents == null || networkComponents.size() == 0) && (populationComponents == null || populationComponents.size() == 0) && (cellComponents == null || cellComponents.size() == 0)
				&& (synapseComponents == null || synapseComponents.size() == 0) && (pulseGeneratorComponents == null || pulseGeneratorComponents.size() == 0))
		{
			modelDescription.insert(0, "Description: <a href=\"#\" instancePath=\"Model.neuroml." + type.getId() + "\">" + type.getName() + "</a><br/><br/>\n");
		}

		HTML html = valuesFactory.createHTML();
		html.setHtml(modelDescription.toString());

		if(verbose) System.out.println("=========== Model ===========\n" + modelDescription.toString());

		Variable descriptionVariable = variablesFactory.createVariable();
		descriptionVariable.setId(Resources.MODEL_DESCRIPTION.getId());
		descriptionVariable.setName(Resources.MODEL_DESCRIPTION.get());
		descriptionVariable.getTypes().add(access.getType(TypesPackage.Literals.HTML_TYPE));
		descriptionVariable.getInitialValues().put(access.getType(TypesPackage.Literals.HTML_TYPE), html);

		return descriptionVariable;
	}

	/**
	 * Gets the ion channels in a cell TODO: replace with call to method in org.neuroml.model.util.CellUtils
	 * 
	 * @returns HashMap with channel id vs [min, max] of channel density in SI units
	 */
	public HashMap<String, Float[]> getIonChannelsInCell(Cell cell) throws NeuroMLException, ContentError, ParseError
	{
		HashMap<String, Float[]> ic = new HashMap<>();

		if(cell == null) return ic;
        
		CellUtils cellUtils = new CellUtils(cell);

		if(cell.getBiophysicalProperties() != null)
		{
			for(ChannelDensity cd : cell.getBiophysicalProperties().getMembraneProperties().getChannelDensity())
			{
				if(!ic.containsKey(cd.getIonChannel()))
				{
					ic.put(cd.getIonChannel(), new Float[] { Float.MAX_VALUE, 0f });
				}
				float densSi = Utils.getMagnitudeInSI(cd.getCondDensity());
				if(densSi < ic.get(cd.getIonChannel())[0]) ic.get(cd.getIonChannel())[0] = densSi;
				if(densSi > ic.get(cd.getIonChannel())[1]) ic.get(cd.getIonChannel())[1] = densSi;
			}

			for(ChannelDensityGHK cd : cell.getBiophysicalProperties().getMembraneProperties().getChannelDensityGHK())
			{
				if(ic.containsKey(cd.getIonChannel()))
				{
					ic.put(cd.getIonChannel(), new Float[] { -1f, -1f });
				}
				// float densSi = cd.getCondDensity();
			}
			/*
			 * for (ChannelDensityGHK2 cd: cell.getBiophysicalProperties().getMembraneProperties().getChannelDensityGHK2()) ic.add(cd.getIonChannel());
			 */
			for(ChannelDensityNernst cd : cell.getBiophysicalProperties().getMembraneProperties().getChannelDensityNernst())
			{
				if(!ic.containsKey(cd.getIonChannel()))
				{
					ic.put(cd.getIonChannel(), new Float[] { Float.MAX_VALUE, 0f });
				}
				float densSi = Utils.getMagnitudeInSI(cd.getCondDensity());
				if(densSi < ic.get(cd.getIonChannel())[0]) ic.get(cd.getIonChannel())[0] = densSi;
				if(densSi > ic.get(cd.getIonChannel())[1]) ic.get(cd.getIonChannel())[1] = densSi;
			}
			for(ChannelDensityNonUniform cd : cell.getBiophysicalProperties().getMembraneProperties().getChannelDensityNonUniform())
			{
                String visId = cd.getIonChannel()+"_"+cd.getVariableParameter().get(0).getSegmentGroup();
				if(!ic.containsKey(visId))
				{
					ic.put(visId, new Float[] { Float.MAX_VALUE, 0f });
				}
                
                for(VariableParameter variableParameter : cd.getVariableParameter())
                {
                    if(variableParameter.getParameter().equals(Resources.COND_DENSITY.getId()))
                    {
                        DoubleEvaluator doubleEvaluator = PopulateChannelDensityVisualGroups.getExpressionEvaluator(variableParameter.getInhomogeneousValue().getValue());
                        
                        String segGrpId = variableParameter.getSegmentGroup();
                        SegmentGroup segmentGroup = null;
                        for (SegmentGroup sg: cell.getMorphology().getSegmentGroup())
                        {
                            if (sg.getId().equals(segGrpId))
                                segmentGroup = sg;
                        }
                        
                        for(InhomogeneousParameter inhomogeneousParameter : segmentGroup.getInhomogeneousParameter())
                        {
                            if(inhomogeneousParameter.getId().equals(variableParameter.getInhomogeneousValue().getInhomogeneousParameter()))
                            {
                                // Get all segments for the subgroup
                                List<Segment> segmentsPerSubgroup = cellUtils.getSegmentsInGroup(segmentGroup.getId());
                                for(Segment sg : segmentsPerSubgroup)
                                {
                                    double distanceAllSegments = cellUtils.calculateDistanceInGroup(0.0, sg);
                                    
                                    HashMap<String, Double> valHM = new HashMap<String, Double>();
                                    valHM.put(inhomogeneousParameter.getVariable(), distanceAllSegments);

                                    float densSi = (float) doubleEvaluator.evalD(valHM);
                                    if(densSi < ic.get(visId)[0]) ic.get(visId)[0] = densSi;
                                    if(densSi > ic.get(visId)[1]) ic.get(visId)[1] = densSi;
                                }
                            }
                        }
                    }
                }
            }
			/*
			 * for (ChannelDensityNonUniformGHK cd: cell.getBiophysicalProperties().getMembraneProperties().getChannelDensityNonUniformGHK()) ic.add(cd.getIonChannel());
			 */
			for(ChannelDensityNonUniformNernst cd : cell.getBiophysicalProperties().getMembraneProperties().getChannelDensityNonUniformNernst())
			{
                String visId = cd.getIonChannel()+"_"+cd.getVariableParameter().get(0).getSegmentGroup();
				if(!ic.containsKey(visId))
				{
					ic.put(visId, new Float[] { Float.MAX_VALUE, 0f });
				}
                
                for(VariableParameter variableParameter : cd.getVariableParameter())
                {
                    if(variableParameter.getParameter().equals(Resources.COND_DENSITY.getId()))
                    {
                        DoubleEvaluator doubleEvaluator = PopulateChannelDensityVisualGroups.getExpressionEvaluator(variableParameter.getInhomogeneousValue().getValue());
                        
                        String segGrpId = variableParameter.getSegmentGroup();
                        SegmentGroup segmentGroup = null;
                        for (SegmentGroup sg: cell.getMorphology().getSegmentGroup())
                        {
                            if (sg.getId().equals(segGrpId))
                                segmentGroup = sg;
                        }
                        
                        for(InhomogeneousParameter inhomogeneousParameter : segmentGroup.getInhomogeneousParameter())
                        {
                            if(inhomogeneousParameter.getId().equals(variableParameter.getInhomogeneousValue().getInhomogeneousParameter()))
                            {
                                // Get all segments for the subgroup
                                List<Segment> segmentsPerSubgroup = cellUtils.getSegmentsInGroup(segmentGroup.getId());
                                for(Segment sg : segmentsPerSubgroup)
                                {
                                    double distanceAllSegments = cellUtils.calculateDistanceInGroup(0.0, sg);
                                    
                                    HashMap<String, Double> valHM = new HashMap<String, Double>();
                                    valHM.put(inhomogeneousParameter.getVariable(), distanceAllSegments);

                                    float densSi = (float) doubleEvaluator.evalD(valHM);
                                    if(densSi < ic.get(visId)[0]) ic.get(visId)[0] = densSi;
                                    if(densSi > ic.get(visId)[1]) ic.get(visId)[1] = densSi;
                                }
                            }
                        }
                    }
                }
			}
		}
		return ic;
	}

	// Could be moved elsewhere...
	/**
	 * Gets an RGB value for ion: Na, Ca, etc. for GUIs, traces etc.
	 * 
	 * @returns int[] with RGB value (3 x 0-255)
	 */
	public static int[] getIonColor(String ion)
	{
		int[] col;
		if(ion.toLowerCase().equals("na")) col = new int[] { 30, 144, 255 };
		else if(ion.toLowerCase().equals("k")) col = new int[] { 205, 92, 92 };
		else if(ion.toLowerCase().equals("ca")) col = new int[] { 143, 188, 143 };
		else col = new int[] { 169, 169, 169 };

		return col;
	}

	/**
	 * Gets an image of a scalebar in SVG representing the conductance density of a named ion
	 * 
	 * @returns String with SVG
	 */
	public static String getSvgScale(float min, float max, String ion)
	{
		int[] col = getIonColor(ion);
		return getSvgScale(min, max, col[0], col[1], col[2]);
	}

	/**
	 * Gets an image of a scalebar in SVG representing the conductance density of a named ion
	 * 
	 * @returns String with SVG
	 */
	public static String getSvgScale(float min, float max, int r, int g, int b)
	{
		int height = 18;
		int width_o = 18;
		int order = 8;
		int width = width_o * order;
		float start = -2;
		float stop = start + order;
		float lmin = (float) Math.log10(min);
		float lmax = (float) Math.log10(max);
		float xmin = width * (lmin - start) / order;
		float xmax = width * (lmax - start) / order;

		StringBuilder sb = new StringBuilder("<!-- SVG for "+min+"->"+max+" RGB: ("+r+","+g+","+b+")-->\n<svg width=\"" + width + "\" height=\"" + height + "\">\n");
		sb.append("<rect width=\"" + width + "\" height=\"" + height + "\" style=\"fill:rgb(" + r + "," + g + "," + b + ");stroke-width:0;stroke:rgb(10,10,10)\"/>\n");
		for(int i = 1; i < order; i++)
		{
			int x = width_o * i;
			sb.append("<line x1=\"" + x + "\" y1=\"0\" x2=\"" + x + "\" y2=\"" + height + "\" style=\"stroke:rgb(100,100,100);stroke-width:0.5\" />\n");
		}
		if(xmin == xmax)
		{
			sb.append("<circle cx=\"" + xmin + "\" cy=\"" + (height / 2) + "\" r=\"2\" style=\"stroke:red;fill:red;stroke-width:2\" />\n");
		}
		else
		{
			sb.append("<line x1=\"" + xmin + "\" y1=\"" + (height / 2) + "\" x2=\"" + xmax + "\" y2=\"" + (height / 2) + "\" style=\"stroke:black;stroke-width:1\" />\n");
			sb.append("<circle cx=\"" + xmin + "\" cy=\"" + (height / 2) + "\" r=\"2\" style=\"stroke:yellow;fill:yellow;stroke-width:2\" />\n");
			sb.append("<circle cx=\"" + xmax + "\" cy=\"" + (height / 2) + "\" r=\"2\" style=\"stroke:red;fill:red;stroke-width:2\" />\n");
		}
		sb.append("</svg>\n");

		return sb.toString();
	}

	/**
	 * Sorts a list of Nodes
	 * 
	 */
	private static <T extends Node> void sortNodes(List<T> l)
	{
		if(l != null)
		{
			Collections.sort(l, new Comparator<T>()
			{
				@Override
				public int compare(T o1, T o2)
				{
					if(o1.getId().equals(o2.getId())) return 0;
					return o1.getId().compareTo(o2.getId());
				}
			});
		}
	}

	/**
	 * Temporary method to guess at ion from name of ion channel It would be better to get ion attribute from channelDensity...
	 */
	private String getIon(String ionChannel)
	{
		String ion = "";
        
		if(ionChannel.toLowerCase().startsWith("na")) ion = "na";
		else if(ionChannel.toLowerCase().startsWith("k")) ion = "k";
		else if(ionChannel.toLowerCase().startsWith("ca")) ion = "ca";
		else if(ionChannel.toLowerCase().startsWith("h")) ion = "h";
		else if(ionChannel.toLowerCase().startsWith("ih")) ion = "h";
		else if(ionChannel.toLowerCase().startsWith("im")) ion = "k";
        
		else if(ionChannel.toLowerCase().indexOf("na") > 0) ion = "na";
		else if(ionChannel.toLowerCase().indexOf("k") > 0) ion = "k";
		else if(ionChannel.toLowerCase().indexOf("ca") > 0) ion = "ca";
		else if(ionChannel.toLowerCase().indexOf("h") > 0) ion = "h";
		else ion = "o";
		return ion;
	}

	/**
	 * Create Variable with HTML value for a Cell
	 * 
	 * @param cell
	 *            - Cell used to create this html element
	 * @return
	 * @throws ModelInterpreterException
	 * @throws GeppettoVisitingException
	 * @throws NeuroMLException
	 * @throws LEMSException
	 */
	private void createCellsHTMLVariable() throws ModelInterpreterException, GeppettoVisitingException, NeuroMLException, LEMSException
	{
		try
		{
			List<Type> cellComponents = typesMap.containsKey(ResourcesDomainType.CELL.get()) ? typesMap.get(ResourcesDomainType.CELL.get()) : null;
			sortNodes(cellComponents);

			if(cellComponents != null && cellComponents.size() > 0)
			{
				for(Type cell : cellComponents)
				{
					List<Variable> notesComponents = new ArrayList<>();
					List<Type> ionChannelComponents = typesMap.containsKey(ResourcesDomainType.IONCHANNEL.get()) ? typesMap.get(ResourcesDomainType.IONCHANNEL.get()) : null;
					sortNodes(ionChannelComponents);

					EList<Variable> cellVariables = ((CompositeType) cell).getVariables();
					for(Variable v : cellVariables)
					{
						if(v.getId().equals(NOTES))
						{
							notesComponents.add(v);
						}
					}

					StringBuilder htmlText0 = new StringBuilder();
					htmlText0.append("<b>Cell: </b> <a href=\"#\" instancePath=\"Model.neuroml." + cell.getId() + "\">" + cell.getId() + "</a><br/><br/>\n");

					if(notesComponents != null && notesComponents.size() > 0)
					{

						htmlText0.append("<b>Description</b><br/>\n");
						for(Variable note : notesComponents)
						{
							Text notes = (Text) note.getInitialValues().get(access.getType(TypesPackage.Literals.TEXT_TYPE));
							htmlText0.append("<p instancePath=\"Model.neuroml." + note.getId() + "\">" + formatDescription(notes.getText()) + "</p>\n ");
						}
						htmlText0.append("<br/>\n");
					}

					Variable htmlVariable0 = variablesFactory.createVariable();
					htmlVariable0.setId(Resources.NOTES.getId());
					htmlVariable0.setName(Resources.NOTES.get());
					Cell nmlCell = null;

					// TODO: replace this hard coding!!
					for(IafCell c : neuroMLDocument.getIafCell())
					{
						if(c.getId().equals(cell.getId()))
						{
							htmlText0.append("Type: NeuroML IaFCell<br/>\n");
							htmlText0.append("Leak reversal potential: " + c.getLeakReversal() + "<br/>\n");
							htmlText0.append("Threshold voltage: " + c.getThresh() + "<br/>\n");
							htmlText0.append("Reset voltage: " + c.getReset() + "<br/>\n");
							htmlText0.append("Capacitance: " + c.getC() + "<br/>\n");
							htmlText0.append("Leak conductance: " + c.getLeakConductance() + "<br/>\n");
						}
					}
					for(Izhikevich2007Cell c : neuroMLDocument.getIzhikevich2007Cell())
					{
						if(c.getId().equals(cell.getId()))
						{
							htmlText0.append("Type: NeuroML Izhikevich2007Cell<br/>\n");
							htmlText0.append("a: " + c.getA() + "<br/>\n");
							htmlText0.append("b: " + c.getB() + "<br/>\n");
							htmlText0.append("c: " + c.getC() + "<br/>\n");
							htmlText0.append("d: " + c.getD() + "<br/>\n");
							htmlText0.append("k: " + c.getK() + "<br/>\n");
							htmlText0.append("v0: " + c.getV0() + "<br/>\n");
							htmlText0.append("v peak: " + c.getVpeak() + "<br/>\n");
							htmlText0.append("v reset: " + c.getVr() + "<br/>\n");
							htmlText0.append("v threshold: " + c.getVt() + "<br/>\n");
						}
					}
					for(IzhikevichCell c : neuroMLDocument.getIzhikevichCell())
					{
						if(c.getId().equals(cell.getId()))
						{
							htmlText0.append("Type: NeuroML IzhikevichCell<br/>\n");
							htmlText0.append("a: " + c.getA() + "<br/>\n");
							htmlText0.append("b: " + c.getB() + "<br/>\n");
							htmlText0.append("c: " + c.getC() + "<br/>\n");
							htmlText0.append("d: " + c.getD() + "<br/>\n");
							htmlText0.append("v0: " + c.getV0() + "<br/>\n");
							htmlText0.append("v threshold: " + c.getThresh() + "<br/>\n");
						}
					}
					for(Cell c : neuroMLDocument.getCell())
					{
						if(c.getId().equals(cell.getId()))
						{
							nmlCell = c;
							htmlText0.append("Number of segments: " + c.getMorphology().getSegment().size() + "<br/>\n");
							htmlText0.append("Number of segment groups: " + c.getMorphology().getSegmentGroup().size() + "<br/><br/>\n");
						}
					}
					HashMap<String, Float[]> ionChannelInfo = getIonChannelsInCell(nmlCell);

					if(ionChannelComponents != null && ionChannelComponents.size() > 0)
					{
						htmlText0.append("<b>Ion channels</b><br/>\n");
						for(Type ionChannel : ionChannelComponents)
						{
							if(ionChannelInfo.keySet().contains(ionChannel.getId()))
							{
								htmlText0.append("<a href=\"#\" instancePath=\"Model.neuroml." + ionChannel.getId() + "\">" + ionChannel.getName() + "</a>");
								if(!ionChannel.getId().equals(ionChannelComponents.get(ionChannelComponents.size() - 1).getId())) htmlText0.append(" |\n");
							}
						}
						htmlText0.append("<br/><br/>\n");
					}

					// Add Visual Group to model cell description
					VisualType visualType = cell.getVisualType();
					if(visualType != null)
					{
						List<VisualGroup> visualGroups = ((CompositeVisualType) visualType).getVisualGroups();
						// sortNodes(visualGroups);
						if(visualGroups != null && visualGroups.size() > 0)
						{
							htmlText0.append("\n<b>Click to apply colouring to the cell morphology</b><br/>\n");

							htmlText0.append("\n<table>\n");
							HashMap<String, String> ionsVsHtml = new HashMap<>();
							ionsVsHtml.put("na", "");
							ionsVsHtml.put("k", "");
							ionsVsHtml.put("ca", "");
							ionsVsHtml.put("h", "");
							ionsVsHtml.put("o", "");
							for(VisualGroup visualGroup : visualGroups)
							{

                                htmlText0.append("<!-- VT = "+visualGroup.getName()+" -->\n");
								if(visualGroup.getName().equals("Cell Regions"))
								{
									htmlText0.append("<tr><td>\n<a href=\"#Model.neuroml." + visualType.getId() + "." + visualGroup.getId() + "\" type=\"visual\" instancePath=\"Model.neuroml." + visualType.getId() + "." + visualGroup.getId() + "\">Highlight "
											+ visualGroup.getName().toLowerCase() + "</a>&nbsp;");
									htmlText0.append("<td/><td>( "
                                        + "<b><span style=\"color:#" + ModelInterpreterVisualConstants.SOMA_COLOR.substring(2) + "\">soma</span>, " 
                                        + "<span style=\"color:#" + ModelInterpreterVisualConstants.DENDRITES_COLOR.substring(2) + "\">dendrites</span>, " 
                                        + "<span style=\"color:#" + ModelInterpreterVisualConstants.AXONS_COLOR.substring(2) + "\">axon</span></b> )\n");

									htmlText0.append("<td/><tr/>\n");
								}
								else
								{
									String ion = getIon(visualGroup.getName().toLowerCase());
                                    String condName = visualGroup.getName();
                                    if (condName.length()>20)
                                        condName = condName.substring(0,20)+"...";

									Float[] minMax = ionChannelInfo.get(visualGroup.getName());
									if(minMax == null)
									{
										minMax = new Float[] { -2f, -1f };
									}
                                    
                                    if (minMax[1]!=0)
                                    {
                                        String info = ("\n<!-- Ion: "+ion+", condName: "+condName+", minMax: ("+minMax[0]+","+minMax[1]+")-->\n"
                                            + "<tr><td>\n<a href=\"#Model.neuroml." + visualType.getId() + "." + visualGroup.getId() + "\" type=\"visual\" instancePath=\"Model.neuroml." + visualType.getId() + "." + visualGroup.getId() + "\">"
                                            + condName + "</a> <td/>\n");

                                        String min = minMax[0].intValue() != minMax[0].floatValue() ? minMax[0].toString() : minMax[0].intValue() + "";
                                        String max = minMax[1].intValue() != minMax[1].floatValue() ? minMax[1].toString() : minMax[1].intValue() + "";
                                        info += " \n<td>\n" + getSvgScale(minMax[0], minMax[1], ion)+"<td/>";
                                        info += "<td>&nbsp;";
                                        if(!min.equals(max)) info += "<span style=\"color:#" + ModelInterpreterVisualConstants.HIGH_SPECTRUM.substring(2) + "\">" + min + " S/m<sup>2</sup></span> -> ";
                                        info += "<span style=\"color:#" + ModelInterpreterVisualConstants.LOW_SPECTRUM.substring(2) + "\">" + max + " S/m<sup>2</sup></span><td/>";
                                        //info += ", <span style=\"color:#FFFFFF\">none</span>)";

                                        info += ("<tr/>\n");
                                        ionsVsHtml.put(ion, ionsVsHtml.get(ion) + info);
                                    }

								}
							}
							htmlText0.append(ionsVsHtml.get("na"));
							htmlText0.append(ionsVsHtml.get("k"));
							htmlText0.append(ionsVsHtml.get("ca"));
							htmlText0.append(ionsVsHtml.get("h"));
							htmlText0.append(ionsVsHtml.get("o"));

							htmlText0.append("</table><br/><br/>");
						}
					}
                    else
                    {
						htmlText0.append("<!-- VT = null -->\n");
                    }

					// Create HTML Value object and set HTML text
					HTML html0 = valuesFactory.createHTML();
					if(verbose)
					{
						System.out.println("========== Cell ============\n" + htmlText0.toString());
					}

					html0.setHtml(htmlText0.toString());
					htmlVariable0.getTypes().add(access.getType(TypesPackage.Literals.HTML_TYPE));
					htmlVariable0.getInitialValues().put(access.getType(TypesPackage.Literals.HTML_TYPE), html0);

					((CompositeType) cell).getVariables().add(htmlVariable0);
				}
			}
		}
		catch(Exception e)
		{
			throw new ModelInterpreterException(e);
		}
	}

	private String formatDescription(String desc)
	{
		desc = parseForHyperlinks(desc);
		desc = desc.replaceAll("\n", "<br/>\n");
		return desc;
	}

	private static String replaceToken(String line, String oldToken, String newToken, int fromIndex)
	{
		StringBuilder sb = new StringBuilder(line);
		sb.replace(line.indexOf(oldToken, fromIndex), line.indexOf(oldToken, fromIndex) + oldToken.length(), newToken);
		return sb.toString();
	}

	private static String parseForHyperlinks(String text)
	{
		String[] prefixes = { "http://", "https://" };
		int checkpoint = 0;

		for(String prefix : prefixes)
		{
			while(text.indexOf(prefix, checkpoint) >= 0)
			{
				int start = text.indexOf(prefix, checkpoint);
				int end = text.length();
				if(text.indexOf(" ", start) > 0) end = text.indexOf(" ", start);
				if(text.indexOf("\n", start) > 0) end = Math.min(end, text.indexOf("\n", start));
				if(text.indexOf(")", start) > 0) end = Math.min(end, text.indexOf(")", start));

				String url = text.substring(start, end);
				if(url.endsWith("."))
				{
					url = url.substring(0, url.length() - 1);
				}

				String link = "<u><i><a href=\"" + url + "\"  target=\"_blank\">" + url + "</a></i></u>";

				text = replaceToken(text, url, link, start);

				checkpoint = start + link.length();
			}
		}
		return text;
	}

	private void extractDescription(Type t, StringBuilder htmlText) throws GeppettoVisitingException
	{
		List<Variable> notesComponents = new ArrayList<>();

		EList<Variable> channelVariables = ((CompositeType) t).getVariables();
		for(Variable v : channelVariables)
		{
			if(v.getId().equals(NOTES))
			{
				notesComponents.add(v);
			}
		}
		if(notesComponents.size() > 0)
		{
			htmlText.append("<b>Description</b><br/>\n");
			for(Variable note : notesComponents)
			{
				Text notes = (Text) note.getInitialValues().get(access.getType(TypesPackage.Literals.TEXT_TYPE));
				htmlText.append("<p instancePath=\"Model.neuroml." + note.getId() + "\">" + formatDescription(notes.getText()) + "</p> ");
			}
			htmlText.append("<br/>\n");
		}
	}

	private void createChannelsHTMLVariable() throws ModelInterpreterException, GeppettoVisitingException, NeuroMLException, LEMSException
	{
		List<Type> ionChannelComponents = typesMap.containsKey(ResourcesDomainType.IONCHANNEL.get()) ? typesMap.get(ResourcesDomainType.IONCHANNEL.get()) : null;
		sortNodes(ionChannelComponents);

		if(ionChannelComponents != null && ionChannelComponents.size() > 0)
		{
			for(Type ionChannel : ionChannelComponents)
			{
				StringBuilder htmlText = new StringBuilder();

				htmlText.append("<b>Ion channel: </b> <a href=\"#\" instancePath=\"Model.neuroml." + ionChannel.getId() + "\">" + ionChannel.getId() + "</a><br/><br/>\n");

				extractDescription(ionChannel, htmlText);

				Component component = ((Component) ionChannel.getDomainModel().getDomainModel());
				IonChannel chan = (IonChannel) getNeuroMLIonChannel(component);

				if(chan != null)
				{
					htmlText.append("<b>Ion: </b>" + (chan.getSpecies() != null ? chan.getSpecies() : "Non specific") + "<br/>\n");
					htmlText.append("<b>Conductance: </b>" + createIonChannelExpression(chan) + "<br/><br/>\n");
				}

				// Adds plot activation variables
				List<Variable> variables = this.plottableVariables.get(ionChannel.getName());
				if(variables != null)
				{
					htmlText.append("<b>Plot activation variables</b><br/>\n");
					for(Variable v : variables)
					{
						String[] split = v.getPath().split("\\.");
						String info = v.getPath();
                        
						if(split.length >= 5)
						{
							info = "Gate: " + split[2] + ", " + split[split.length - 1].replace("_", " ");
                            if (info.contains("forward"))
                                info += " (alpha<sub>" + split[2] + "</sub>)";
                            if (info.contains("reverse"))
                                info += " (beta<sub>" + split[2] + "</sub>)";
                            if (info.contains("time"))
                                info += " (tau<sub>" + split[2] + "</sub>)";
                            if (info.contains("steady"))
                                info += " (inf<sub>" + split[2] + "</sub>)";
                            
						}
                        String ip = "Model." + v.getPath();
                        
						htmlText.append("<a href=\"#" + ip + "\" type=\"variable\" instancePath=\"" + ip /*+ "\" hover=\""+v.toString()*/+"\">" + info + "</a><br/>\n");
					}
				}
				Variable htmlVariable = variablesFactory.createVariable();
				htmlVariable.setId(ionChannel.getId());
				htmlVariable.setName(ionChannel.getName());

				HTML html = valuesFactory.createHTML();
				if(verbose) System.out.println("======= Channel ===============\n" + htmlText.toString());
				html.setHtml(htmlText.toString());
				htmlVariable.getTypes().add(access.getType(TypesPackage.Literals.HTML_TYPE));
				htmlVariable.getInitialValues().put(access.getType(TypesPackage.Literals.HTML_TYPE), html);
				((CompositeType) ionChannel).getVariables().add(htmlVariable);
			}
		}
	}

	// Temp method before LEMS2...
	private String createIonChannelExpression(IonChannel chan)
	{
		StringBuilder htmlText = new StringBuilder();
		StringBuilder postText = new StringBuilder();
		htmlText.append("G<sub>" + chan.getId() + "</sub>(v,t) = G<sub>max</sub> ");
		// neuroMLDocument.
		// ArrayList<String> gates = new ArrayList<>();
		for(GateHHUndetermined g : chan.getGate())
		{
			htmlText.append(" * " + g.getId() + "(v,t)" + (g.getInstances() != 1 ? ("<sup>" + g.getInstances() + "</sup>") : ""));
			// postText.append(" d"+g.getId()+"/dt = alpha<sub>"+g.getId()+"</sub>(v) * (1 - "+g.getId()+") + beta<sub>"+g.getId()+"</sub>(v) * "+g.getId()+"");
		}
		for(GateHHInstantaneous g : chan.getGateHHInstantaneous())
			htmlText.append(" * " + g.getId() + "(v,t)" + (g.getInstances() != 1 ? ("<sup>" + g.getInstances() + "</sup>") : ""));

		for(GateHHRates g : chan.getGateHHrates())
		{
			htmlText.append(" * " + g.getId() + "(v,t)" + (g.getInstances() != 1 ? ("<sup>" + g.getInstances() + "</sup>") : ""));
			// postText.append("d"+g.getId()+"/dt = alpha(v) * (1 - "+g.getId()+") + beta(v) * "+g.getId()+"");
		}

		for(GateHHRatesInf g : chan.getGateHHratesInf())
			htmlText.append(" * " + g.getId() + "(v,t)" + (g.getInstances() != 1 ? ("<sup>" + g.getInstances() + "</sup>") : ""));
		for(GateHHRatesTau g : chan.getGateHHratesTau())
			htmlText.append(" * " + g.getId() + "(v,t)" + (g.getInstances() != 1 ? ("<sup>" + g.getInstances() + "</sup>") : ""));
		for(GateHHRatesTauInf g : chan.getGateHHratesTauInf())
			htmlText.append(" * " + g.getId() + "(v,t)" + (g.getInstances() != 1 ? ("<sup>" + g.getInstances() + "</sup>") : ""));
		for(GateHHTauInf g : chan.getGateHHtauInf())
			htmlText.append(" * " + g.getId() + "(v,t)" + (g.getInstances() != 1 ? ("<sup>" + g.getInstances() + "</sup>") : ""));

		return htmlText.toString() + "<br/>\n" + postText.toString();
	}

	private void createSynapsesHTMLVariable() throws ModelInterpreterException, GeppettoVisitingException, NeuroMLException, LEMSException
	{
		List<Type> synapseComponents = typesMap.containsKey(ResourcesDomainType.SYNAPSE.get()) ? typesMap.get(ResourcesDomainType.SYNAPSE.get()) : null;
		sortNodes(synapseComponents);

		if(synapseComponents != null && synapseComponents.size() > 0)
		{
			for(Type synapse : synapseComponents)
			{
				StringBuilder htmlText = new StringBuilder();

				Variable htmlVariable = variablesFactory.createVariable();
				htmlVariable.setId(synapse.getId());
				htmlVariable.setName(synapse.getName());

				htmlText.append("<b>Synapse: </b> <a href=\"#\" instancePath=\"Model.neuroml." + synapse.getId() + "\">" + synapse.getId() + "</a><br/><br/>\n");

				extractDescription(synapse, htmlText);

				// Create HTML Value object and set HTML text
				HTML html = valuesFactory.createHTML();
				// BaseConductanceBasedSynapse syn = null;
				for(ExpOneSynapse syn : neuroMLDocument.getExpOneSynapse())
				{
					if(syn.getId().equals(synapse.getId()))
					{
						htmlText.append("Base conductance: " + syn.getGbase() + "<br/>\n");
						htmlText.append("Decay time: " + syn.getTauDecay() + "<br/>\n");
						htmlText.append("Reversal potential: " + syn.getErev() + "<br/>\n");
					}
				}
				for(ExpTwoSynapse syn : neuroMLDocument.getExpTwoSynapse())
				{
					if(syn.getId().equals(synapse.getId()))
					{
						htmlText.append("Base conductance: " + syn.getGbase() + "<br/>\n");
						htmlText.append("Rise time: " + syn.getTauRise() + "<br/>\n");
						htmlText.append("Decay time: " + syn.getTauDecay() + "<br/>\n");
						htmlText.append("Reversal potential: " + syn.getErev() + "<br/>\n");
					}
				}
				html.setHtml(htmlText.toString());
				if(verbose) System.out.println("======= Synapse ===============\n" + htmlText.toString());
				htmlVariable.getTypes().add(access.getType(TypesPackage.Literals.HTML_TYPE));
				htmlVariable.getInitialValues().put(access.getType(TypesPackage.Literals.HTML_TYPE), html);
				((CompositeType) synapse).getVariables().add(htmlVariable);
			}
		}
	}

	private void createInputsHTMLVariable() throws ModelInterpreterException, GeppettoVisitingException, NeuroMLException, LEMSException
	{
		List<Type> pulseGeneratorComponents = typesMap.containsKey(ResourcesDomainType.PULSEGENERATOR.get()) ? typesMap.get(ResourcesDomainType.PULSEGENERATOR.get()) : null;
		sortNodes(pulseGeneratorComponents);

		if(pulseGeneratorComponents != null && pulseGeneratorComponents.size() > 0)
		{
			for(Type pulseGenerator : pulseGeneratorComponents)
			{
				PulseGenerator pg = null;
				for(PulseGenerator pg0 : neuroMLDocument.getPulseGenerator())
				{
					if(pg0.getId().equals(pulseGenerator.getId())) pg = pg0;
				}

				StringBuilder htmlText = new StringBuilder();

				Variable htmlVariable = variablesFactory.createVariable();
				htmlVariable.setId(pulseGenerator.getId());
				htmlVariable.setName(pulseGenerator.getName());

				// Create HTML Value object and set HTML text
				HTML html = valuesFactory.createHTML();
				htmlText.append("<a href=\"#\" instancePath=\"Model.neuroml." + pulseGenerator.getId() + "\">" + pulseGenerator.getName() + "</a> ");
				htmlText.append("<br/><br/>\n");

				htmlText.append("Delay: " + pg.getDelay() + "<br/>\n");
				htmlText.append("Duration: " + pg.getDuration() + "<br/>\n");
				htmlText.append("Amplitude: " + pg.getAmplitude() + "<br/>\n");

				html.setHtml(htmlText.toString());

				if(verbose) System.out.println("======= Input ===============\n" + htmlText.toString());
				htmlVariable.getTypes().add(access.getType(TypesPackage.Literals.HTML_TYPE));
				htmlVariable.getInitialValues().put(access.getType(TypesPackage.Literals.HTML_TYPE), html);
				((CompositeType) pulseGenerator).getVariables().add(htmlVariable);
			}
		}
	}

	/**
	 * @param component
	 * @return the NeuroML cell corresponding to a given LEMS component
	 */
	private Standalone getNeuroMLIonChannel(Component component)
	{
		String lemsId = component.getID();
		for(IonChannel c : neuroMLDocument.getIonChannel())
		{
			if(c.getId().equals(lemsId))
			{
				return c;
			}
		}
		for(IonChannelHH c : neuroMLDocument.getIonChannelHH())
		{
			if(c.getId().equals(lemsId))
			{
				return c;
			}
		}
		// for(IonChannelKS c : neuroMLDocument.getIonChannelKS())
		// {
		// if(c.getId().equals(lemsId))
		// {
		// return c;
		// }
		// }
		return null;
	}
    
    private boolean extractPlottables(CompositeType ionChannel, InfoNode node) throws GeppettoVisitingException, ModelInterpreterException
    {
        boolean found = false;
			for(Map.Entry<String, Object> entry : node.getProperties().entrySet())
			{
				String id = entry.getKey().substring(entry.getKey().lastIndexOf(" ") + 1);
				for(Variable gateVariable : ionChannel.getVariables())
				{
					if(gateVariable.getId().equals(id))
					{
						InfoNode gateNode = (InfoNode) entry.getValue();
						for(Map.Entry<String, Object> gateProperties : gateNode.getProperties().entrySet())
						{
							if(gateProperties.getValue() instanceof ExpressionNode)
							{
								// Match property id in export lib with neuroml id
								ResourcesSummary gatePropertyResources = ResourcesSummary.getValueByValue(gateProperties.getKey());
								if(gatePropertyResources != null)
								{
									CompositeType gateType = (CompositeType) gateVariable.getAnonymousTypes().get(0);
									for(Variable rateVariable : gateType.getVariables())
									{
										if(rateVariable.getId().equals(gatePropertyResources.getNeuromlId()))
										{
											CompositeType rateType = (CompositeType) rateVariable.getAnonymousTypes().get(0);
											// Create expression node
                                            
                                            ExpressionNode en = (ExpressionNode) gateProperties.getValue();
											Variable variable = getExpressionVariable(gateProperties.getKey(), en);
											rateType.getVariables().add(variable);

											if(!((ExpressionNode) gateProperties.getValue()).getExpression().startsWith("org.neuroml.export"))
											{
												List<Variable> variables = this.plottableVariables.get(ionChannel.getName());
												if(variables == null) variables = new ArrayList<Variable>();
												variables.add(variable);
												this.plottableVariables.put(ionChannel.getName(), variables);
                                                found = true;
											}
										}
									}

								}
								else
								{
									System.out.println("No node matches summary gate rate!!!");
								}
							}
						}
					}
				}
			}
            return found;
    }


/*
	private InfoNode addExpresionNodes(CompositeType ionChannel) throws NeuroMLException, LEMSException, GeppettoVisitingException, ModelInterpreterException
	{
		// Get lems component and convert to neuroml
		Component component = ((Component) ionChannel.getDomainModel().getDomainModel());
		Standalone neuromlIonChannel = getNeuroMLIonChannel(component);

		// Create channel info extractor from export library
		if(neuromlIonChannel != null)
		{
			ChannelInfoExtractor2 channelInfoExtractor = new ChannelInfoExtractor2((IonChannel) neuromlIonChannel);
			InfoNode gatesNode = channelInfoExtractor.getGates();
			for(Map.Entry<String, Object> entry : gatesNode.getProperties().entrySet())
			{
				String id = entry.getKey().substring(entry.getKey().lastIndexOf(" ") + 1);
				for(Variable gateVariable : ionChannel.getVariables())
				{
					if(gateVariable.getId().equals(id))
					{
						InfoNode gateNode = (InfoNode) entry.getValue();
						for(Map.Entry<String, Object> gateProperties : gateNode.getProperties().entrySet())
						{
							if(gateProperties.getValue() instanceof ExpressionNode)
							{
								// Match property id in export lib with neuroml id
								ResourcesSummary gatePropertyResources = ResourcesSummary.getValueByValue(gateProperties.getKey());
								if(gatePropertyResources != null)
								{
									CompositeType gateType = (CompositeType) gateVariable.getAnonymousTypes().get(0);
									for(Variable rateVariable : gateType.getVariables())
									{
										if(rateVariable.getId().equals(gatePropertyResources.getNeuromlId()))
										{
											CompositeType rateType = (CompositeType) rateVariable.getAnonymousTypes().get(0);
											// Create expression node
											Variable variable = getExpressionVariable(gateProperties.getKey(), (ExpressionNode) gateProperties.getValue());
											rateType.getVariables().add(variable);

											if(!((ExpressionNode) gateProperties.getValue()).getExpression().startsWith("org.neuroml.export"))
											{
                                                //System.out.println("Adding ............."+ionChannel.getName()+" "+gateVariable.getId());
												List<Variable> variables = this.plottableVariables.get(ionChannel.getName());
												if(variables == null) variables = new ArrayList<Variable>();
												variables.add(variable);
												this.plottableVariables.put(ionChannel.getName(), variables);
											}
										}
									}

								}
								else
								{
									throw new ModelInterpreterException("No node matches summary gate rate");
								}
							}
						}
					}
				}
			}
            return gatesNode;
		}
        return new InfoNode();
    }*/

	private Variable getExpressionVariable(String expressionNodeId, ExpressionNode expressionNode) throws GeppettoVisitingException
	{

		Argument argument = valuesFactory.createArgument();
		argument.setArgument("v");

		Expression expression = valuesFactory.createExpression();
		expression.setExpression(expressionNode.getExpression());

		Function function = valuesFactory.createFunction();
		function.setExpression(expression);
		function.getArguments().add(argument);
		PlotMetadataNode plotMetadataNode = expressionNode.getPlotMetadataNode();
		if(plotMetadataNode != null)
		{
			FunctionPlot functionPlot = valuesFactory.createFunctionPlot();
			functionPlot.setTitle(plotMetadataNode.getPlotTitle());
			functionPlot.setXAxisLabel(plotMetadataNode.getXAxisLabel());
			functionPlot.setYAxisLabel(plotMetadataNode.getYAxisLabel());
			functionPlot.setInitialValue(plotMetadataNode.getInitialValue());
			functionPlot.setFinalValue(plotMetadataNode.getFinalValue());
			functionPlot.setStepValue(plotMetadataNode.getStepValue());
			function.setFunctionPlot(functionPlot);
		}

		Dynamics dynamics = valuesFactory.createDynamics();
		dynamics.setDynamics(function);

		Variable variable = variablesFactory.createVariable();
		variable.setId(ModelInterpreterUtils.parseId(expressionNodeId));
		variable.setName(expressionNodeId);
		variable.getInitialValues().put(access.getType(TypesPackage.Literals.DYNAMICS_TYPE), dynamics);
		variable.getTypes().add(access.getType(TypesPackage.Literals.DYNAMICS_TYPE));

		return variable;
	}

	public static void main(String[] args) throws Exception
	{

		StringBuilder sb = new StringBuilder("<html>\n<body>\n");
		sb.append(getSvgScale(0.1f, 22, "na"));
		sb.append("<br/><br/>\n");
		sb.append(getSvgScale(3333, 3333, "k"));
		sb.append("<br/><br/>\n");
		sb.append(getSvgScale(0, 1, "ca"));
		sb.append("<br/><br/>\n");
		sb.append(getSvgScale(1e-1f, 1e-2f, "h"));
		sb.append("<body/>\n<html>\n");
		System.out.println(sb);
		String fn = "/tmp/test_svg.html";
		File f = new File(fn);
		FileOutputStream fos = new FileOutputStream(f);
		fos.write(sb.toString().getBytes());
		System.out.println("Written to: " + f.getAbsolutePath());
		fos.close();

	}

}
