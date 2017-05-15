package org.geppetto.model.neuroml.features;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.Expose;

class CanvasCustomisation {
    @Expose public String widgetType = "CANVAS";
    @Expose private Map<String, Map<String, Integer>> componentSpecific = new HashMap<String, Map<String, Integer>>();
    private Map<String, Integer> colorMap = new HashMap<String, Integer>();

    public CanvasCustomisation() {
        componentSpecific.put("colorMap", colorMap);
    }
    
    public void addColor(String path, Integer color)
    {
        colorMap.put(path, color);
    }

}
