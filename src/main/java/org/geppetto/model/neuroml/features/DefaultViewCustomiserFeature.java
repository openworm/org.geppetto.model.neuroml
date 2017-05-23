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
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;


public class DefaultViewCustomiserFeature implements IDefaultViewCustomiserFeature
{
    private GeppettoFeature type = GeppettoFeature.DEFAULT_VIEW_CUSTOMISER_FEATURE;
    private CanvasCustomisation canvas = new CanvasCustomisation();

    @Override
    public GeppettoFeature getType()
    {
        return type;
    }

    @Override
    public JsonObject getDefaultViewCustomisation(Type type)
    {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        JsonElement je = gson.toJsonTree(canvas);
        JsonObject jo = new JsonObject();
        jo.add("Canvas1", je);
        return jo;
    }

    public String extractPath(Type type, GeppettoLibrary library)
    {
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
        return path.substring(0, path.length()-1);
    }

    public String extractColor(Variable var)
    {
        String[] rgb = ((Text) var.getInitialValues().get(0).getValue()).getText().split(" ");
        String color = String.format("#%02x%02x%02x",
                                     Math.round(Float.parseFloat(rgb[0])*255),
                                     Math.round(Float.parseFloat(rgb[1])*255),
                                     Math.round(Float.parseFloat(rgb[2])*255));
        return color;
    }

    public void buildCustomizationFromType(Type type, GeppettoLibrary library)
    {
        for (Variable var : ((CompositeType) type).getVariables()) {
            switch (var.getId()) {
                case "color":
                    String path = extractPath(type, library);
                    String color = extractColor(var);
                    canvas.addColor(path, color);
                    break;
            }
        }
    }
        
}
