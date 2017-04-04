package org.geppetto.model.neuroml.features;

import java.util.Map;
import java.util.HashMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.geppetto.model.types.Type;
import org.geppetto.core.services.GeppettoFeature;
import org.geppetto.core.features.IDefaultViewCustomiserFeature;
import org.lemsml.jlems.core.type.Component;

public class DefaultViewCustomiserFeature implements IDefaultViewCustomiserFeature
{
    private GeppettoFeature type = GeppettoFeature.DEFAULT_VIEW_CUSTOMISER_FEATURE;
    private Map<Type, JsonObject> defaultViewCustomisation = new HashMap<Type, JsonObject>();

    @Override
    public GeppettoFeature getType()
    {
        return type;
    }

    @Override
    public JsonObject getDefaultViewCustomisation(Type type)
    {
        return defaultViewCustomisation.get(type);
    }

    public void setDefaultViewCustomisation(Type type, JsonObject view)
    {
        defaultViewCustomisation.put(type, view);
    }

    public void addDefaultViewCustomisation(Type type, JsonObject view)
    {
        defaultViewCustomisation.get(type);
    }

    public static JsonObject createCustomizationFromType(Type type)
    {
        // TEST STRING
        String json = "{\"views\":{\"Popup1\":{\"widgetType\":1,\"name\":\"fix\",\"position\":{\"left\":358,\"top\":66},\"dataType\":\"string\",\"componentSpecific\":{\"customHandlers\":[\"test1\",\"test2\"]},\"data\":\"bugs\"},\"Plot1\":{\"name\":\"Recorded Variables\",\"size\":{\"height\":314.8,\"width\":628.8},\"dataType\":\"object\",\"data\":[\"hhcell.hhpop[0].v1\",\"hhcell.hhpop[0].v2\"],\"widgetType\":0},\"PopupX\":{\"name\":\"abc\",\"widgetType\":1,\"data\":\"xxx\"}}}";
        Gson gson = new Gson();
        JsonParser jsonParser = new JsonParser();
        JsonObject jo = (JsonObject)jsonParser.parse(json);
        return jo;
    }
}
