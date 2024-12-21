package com.dooji.clipboard.manager;

import net.minecraft.client.MinecraftClient;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Date;

import com.dooji.clipboard.ClipboardItem;

public class ClipboardManager {
    private static final List<ClipboardItem> history = new LinkedList<>();
    private static final int MAX_HISTORY_SIZE = 50;
    private static final Path HISTORY_FILE = MinecraftClient.getInstance().runDirectory.toPath().resolve("config/Clipboard/history.dat");
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static ClipboardConfig config;

    public static void initialize() {
        config = ClipboardConfig.load();
        
        if (config.persistent) {
            loadHistory();
        }
    }

    public static void addEntry(String text) {
        if (!config.enabled) return;
        addEntry(text, DATE_FORMAT.format(new Date()));
    }

    public static void addEntry(String text, String date) {
        if (!config.enabled) return;

        ClipboardItem existingItem = null;
        for (ClipboardItem item : history) {
            if (item.getText().equals(text)) {
                existingItem = item;
                break;
            }
        }

        if (existingItem != null) {
            history.remove(existingItem);
            history.add(0, new ClipboardItem(existingItem.getText(), existingItem.getDate()));
        } else {
            if (history.size() >= MAX_HISTORY_SIZE) {
                history.remove(history.size() - 1);
            }

            history.add(0, new ClipboardItem(text, date));
        }

        if (config.persistent) {
            saveHistory();
        }
    }

    public static void removeEntry(String text) {
        history.removeIf(item -> item.getText().equals(text));
        if (config.persistent) {
            saveHistory();
        }
    }

    public static String getLastEntry() {
        if (history.isEmpty()) return null;

        return history.get(0).getText();
    }

    public static List<ClipboardItem> getHistory() {
        return new LinkedList<>(history);
    }

    private static void saveHistory() {
        if (!config.persistent) return;
        try {
            Files.createDirectories(HISTORY_FILE.getParent());

            try (Writer writer = new FileWriter(HISTORY_FILE.toFile())) {
                for (ClipboardItem entry : history) {
                    writer.write(entry.getText() + "|" + entry.getDate() + "\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadHistory() {
        if (!config.persistent || !Files.exists(HISTORY_FILE)) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(HISTORY_FILE.toFile()))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");

                if (parts.length == 1) {
                    history.add(new ClipboardItem(parts[0], DATE_FORMAT.format(new Date())));
                } else if (parts.length == 2) {
                    history.add(new ClipboardItem(parts[0], parts[1]));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ClipboardConfig getConfig() {
        return config;
    }
}