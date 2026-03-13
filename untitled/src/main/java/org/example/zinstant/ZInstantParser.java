package org.example.zinstant;

import com.google.gson.*;
import org.example.zinstant.components.*;

import java.lang.reflect.Type;
import java.util.Map;

public class ZInstantParser implements JsonDeserializer<IZinstantComponent>, JsonSerializer<IZinstantComponent> {

    @Override
    public IZinstantComponent deserialize(JsonElement jsonElement,
                                          Type type,
                                          JsonDeserializationContext context) throws JsonParseException {

        JsonObject obj = jsonElement.getAsJsonObject();

        if (obj.entrySet().size() != 1) {
            throw new JsonParseException("Invalid component wrapper: " + obj);
        }

        Map.Entry<String, JsonElement> entry = obj.entrySet().iterator().next();

        String key = entry.getKey();
        JsonElement value = entry.getValue();

        EZInstantComponent componentType = EZInstantComponent.findByValue(key);

        if (componentType == null) {
            throw new JsonParseException("Unknown component type: " + key);
        }

        Class<? extends IZinstantComponent> clazz = componentType.getClazz();

        // Primitive (title, paragraph, image)
        if (value.isJsonPrimitive()) {

            JsonObject wrapper = new JsonObject();

            if ("title".equals(key) || "image".equals(key)) {
                wrapper.add("data", value);
            } else {
                wrapper.add("value", value);
            }

            return context.deserialize(wrapper, clazz);
        }

        // Array (carousel, buttons)
        if (value.isJsonArray()) {

            JsonObject wrapper = new JsonObject();
            wrapper.add("value", value);

            return context.deserialize(wrapper, clazz);
        }

        // Object (logo)
        return context.deserialize(value, clazz);
    }


    @Override
    public JsonElement serialize(IZinstantComponent component,
                                 Type type,
                                 JsonSerializationContext context) {

        JsonObject wrapper = new JsonObject();

        if (component instanceof ISingleAttribute single) {
            wrapper.add(component.getName(), context.serialize(single.getAttribute()));
            return wrapper;
        }

        if (component instanceof IArrayAttribute array) {
            wrapper.add(component.getName(), context.serialize(array.getArray()));
            return wrapper;
        }

        if (component instanceof IObjectAttribute obj) {
            wrapper.add(component.getName(), context.serialize(obj.getObject()));
            return wrapper;
        }

        throw new JsonParseException("Unknown component class: " + component.getClass());
    }
}