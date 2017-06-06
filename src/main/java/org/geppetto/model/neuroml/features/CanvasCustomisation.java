package org.geppetto.model.neuroml.features;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.Expose;

class CanvasCustomisation {
    @Expose public String widgetType = "CANVAS";
    @Expose private Map<String, Map<String, String>> componentSpecific = new HashMap<String, Map<String, String>>();
    private Map<String, String> colorMap = new HashMap<String, String>();

    public CanvasCustomisation() {
        componentSpecific.put("colorMap", colorMap);
    }
    
    public void addColor(String path, String color)
    {
        colorMap.put(path, color);
    }

}
