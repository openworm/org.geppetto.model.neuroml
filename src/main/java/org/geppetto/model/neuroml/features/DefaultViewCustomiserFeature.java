package org.geppetto.model.neuroml.features;

import com.google.gson.JsonObject;
import org.geppetto.model.types.Type;
import org.geppetto.core.services.GeppettoFeature;
import org.geppetto.core.features.IDefaultViewCustomiserFeature;

public class DefaultViewCustomiserFeature implements IDefaultViewCustomiserFeature
{
    private GeppettoFeature type = GeppettoFeature.DEFAULT_VIEW_CUSTOMISER_FEATURE;

    @Override
    public GeppettoFeature getType()
    {
        return type;
    }

    @Override
    public JsonObject getDefaultViewCustomisation(Type type)
    {
        return new JsonObject();
    }
}
