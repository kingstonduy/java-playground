package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.component.ComponentParser;
import org.example.component.IComponent;
import org.example.component.TemplateDTO;

public class TemplateParser {

    private final Gson gson;

    // Private constructor prevents instantiation from other classes
    private TemplateParser() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(IComponent.class, new ComponentParser())
                // Add any other configuration like Date formats or Null handling here
                .create();
    }

    public static TemplateParser getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Converts JSON string to CentralizedSection object
     */
    public TemplateDTO parse(String json) {
        return gson.fromJson(json, TemplateDTO.class);
    }

    /**
     * Converts CentralizedSection object to JSON string
     */
    public String toJson(Object section) {
        return gson.toJson(section);
    }

    // Static inner class - loaded only when getInstance() is called
    private static class Holder {
        private static final TemplateParser INSTANCE = new TemplateParser();
    }
}