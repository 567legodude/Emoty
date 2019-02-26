package com.ssplugins.emoty;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class JSONConfig {
    
    private Gson gson;
    private JsonParser parser;
    
    private File file;
    private JsonObject json;
    
    public JSONConfig(Emoty main, String name) {
        file = new File(main.getDataFolder(), name + ".json");
        gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        parser = new JsonParser();
        loadData();
    }
    
    private void loadData() {
        if (!file.exists()) {
            json = new JsonObject();
            return;
        }
        try (FileReader reader = new FileReader(file)) {
            json = parser.parse(reader).getAsJsonObject();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load configuration file " + file.getName(), e);
        }
    }
    
    public void save() {
        try {
            if (!file.exists()) {
                boolean dirs = file.getParentFile().mkdirs();
                if (dirs) {
                    //noinspection ResultOfMethodCallIgnored
                    file.createNewFile();
                }
            }
        } catch (IOException e) {
            System.err.println("Unable to create configuration file " + file.getName());
            e.printStackTrace();
        }
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(json, writer);
        } catch (IOException e) {
            System.err.println("Unable to save configuration file " + file.getName());
            e.printStackTrace();
        }
    }
    
    public JsonObject getJson() {
        return json;
    }
    
}
