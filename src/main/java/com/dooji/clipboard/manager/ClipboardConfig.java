package com.dooji.clipboard.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClipboardConfig {
    private static final Path CONFIG_FILE = Path.of("config/Clipboard/config.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean persistent = true;
    public String prioritize = "custom";
    public boolean enabled = true;
    public boolean firstTime = true;

    public static ClipboardConfig load() {
        if (Files.exists(CONFIG_FILE)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {
                return GSON.fromJson(reader, ClipboardConfig.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new ClipboardConfig();
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_FILE.getParent());
            
            try (Writer writer = Files.newBufferedWriter(CONFIG_FILE)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}