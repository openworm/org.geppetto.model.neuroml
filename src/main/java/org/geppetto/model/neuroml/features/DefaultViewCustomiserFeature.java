package org.geppetto.model.neuroml.features;

import java.util.HashMap;
import java.util.Map;

import org.geppetto.core.features.IDefaultViewCustomiserFeature;
import org.geppetto.core.services.GeppettoFeature;
import org.geppetto.model.GeppettoLibrary;
import org.geppetto.model.types.CompositeType;
import org.geppetto.model.types.ImportType;
import org.geppetto.model.types.Type;
import org.geppetto.model.values.Text;
import org.geppetto.model.values.Value;
import org.geppetto.model.variables.Variable;
import org.lemsml.jlems.core.type.Component;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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

    public static JsonObject createCustomizationFromType(Type type, GeppettoLibrary library)
    {
        Map<String, String> colorMap = new HashMap<String, String>();
        for (Variable var : ((CompositeType) type).getVariables()) {
            if (var.getId().equals("color")) {
                for (Map.Entry<Type, Value> entry : var.getInitialValues())
                    {
                        String[] rgb = ((Text) entry.getValue()).getText().split(" ");
                        String hexColor = String.format("#%02x%02x%02x",
                                                   Math.round(Float.parseFloat(rgb[0])*255),
                                                   Math.round(Float.parseFloat(rgb[1])*255),
                                                   Math.round(Float.parseFloat(rgb[2])*255));  
                        Component domainModel = (Component) type.getDomainModel().getDomainModel();
                        String path = "";
                        while (domainModel.getParent() != null) {
                            if (domainModel.getParent().getDeclaredType().equals("network")){
                                // so we can get the name of the network
                                ImportType importType = (ImportType)library.getTypes().get(0);
                                path = importType.getReferencedVariables().get(0).getId() + "." + path;
                            } else {
                                path = domainModel.getParent().getID() + "." + path;
                            }
                            domainModel = domainModel.getParent();
                        }
                        colorMap.put(path.substring(0,path.length()-1), hexColor);
                    }
            }
        }
        Gson gson = new Gson();
        String json = "{\"Canvas1\":{\"widgetType\":\"CANVAS\",\"componentSpecific\":{\"colorMap\":" + gson.toJson(colorMap) +"}}}";
        JsonParser jsonParser = new JsonParser();
        JsonObject jo = (JsonObject)jsonParser.parse(json);
        return jo;
    }
}
