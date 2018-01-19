

package org.geppetto.model.neuroml.summaryUtils;

/**
 * Class to hold resources used in the visualiser. This elements will be displayed to the user.
 * @author matteocantarelli
 * @author Adrian Quintana (adrian.perez@ucl.ac.uk)
 *
 */
public enum ResourcesSummary
{
	
	FORWARD_RATE ("forwardRate", "forward rate"),
	REVERSE_RATE ("reverseRate", "reverse rate"),
	STEADY_STATE("steadyState", "steady state"),
	TIME_COURSE("timeCourse", "time course");
	
	
	private String _neuromlId;
	private String _summaryId;
	
	private ResourcesSummary(String neuromlId, String summaryId)
	{
		_neuromlId = neuromlId;
		_summaryId = summaryId;
	}
	
	public String getNeuromlId()
	{
		return _neuromlId;
	}
	
	public String getSummaryId()
	{
		return _summaryId;
	}
	
	public static ResourcesSummary getValueByValue(String value){
		for(ResourcesSummary e : ResourcesSummary.values()){
            if(value.trim().equals(e._summaryId)) return e;
        }
		//If we can't find a value, return the id
		return null;
	}
	
}
