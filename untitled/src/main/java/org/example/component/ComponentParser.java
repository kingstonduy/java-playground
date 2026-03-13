package org.example.component;

import com.google.gson.*;

import java.lang.reflect.Type;

public class ComponentParser implements JsonDeserializer<IComponent>, JsonSerializer<IComponent> {

    @Override
    public IComponent deserialize(JsonElement json,
                                  Type typeOfT,
                                  JsonDeserializationContext context)
            throws JsonParseException {

        JsonObject obj = json.getAsJsonObject();

        if (!obj.has("type")) {
            throw new JsonParseException("Component missing type field: " + obj);
        }

        String type = obj.get("type").getAsString();

        EComponent componentType = EComponent.findByValue(type);

        return context.deserialize(
                obj,
                componentType.getClazz()
        );
    }

    @Override
    public JsonElement serialize(IComponent src,
                                 Type typeOfSrc,
                                 JsonSerializationContext context) {

        JsonObject obj = context.serialize(src, src.getClass()).getAsJsonObject();

        String componentName = src.getName();

        EComponent componentType = EComponent.findByValue(componentName);

        obj.addProperty("type", componentType.getValue());

        return obj;
    }
}