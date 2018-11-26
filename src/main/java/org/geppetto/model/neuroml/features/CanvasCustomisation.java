package org.geppetto.model.neuroml.features;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.Expose;

public class CanvasCustomisation {
    @Expose public String widgetType = "CANVAS";
    @Expose private Map<String, Map<String, String>> componentSpecific = new HashMap<String, Map<String, String>>();
    private Map<String, String> colorMap = new HashMap<String, String>();
    private Map<String, String> radiusMap = new HashMap<String, String>();

    public CanvasCustomisation() {
        componentSpecific.put("colorMap", colorMap);
        componentSpecific.put("radiusMap", radiusMap);
    }

    public Map<String, String> getColorMap() {
        return colorMap;
    }

    public void addColor(String path, String color)
    {
        colorMap.put(path, color);
    }

    public void addRadius(String path, String radius)
    {
        radiusMap.put(path, radius);
    }

}
