package com.dooji.clipboard.ui;

import java.util.function.Consumer;

public class ClipboardOption {
    private final String label;
    private final String description;
    private String currentValue;
    private final String[] possibleValues;
    private int currentIndex;
    private final Consumer<String> onChange;

    public ClipboardOption(String label, String description, String[] possibleValues, int defaultIndex, Consumer<String> onChange) {
        this.label = label;
        this.description = description;
        this.possibleValues = possibleValues;
        this.currentIndex = defaultIndex;
        this.currentValue = possibleValues[currentIndex];
        this.onChange = onChange;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public String getCurrentValue() {
        return currentValue;
    }

    public void toggle() {
        currentIndex = (currentIndex + 1) % possibleValues.length;
        currentValue = possibleValues[currentIndex];
        if (onChange != null) {
            onChange.accept(currentValue);
        }
    }

    public void setValue(String value) {
        for (int i = 0; i < possibleValues.length; i++) {
            if (possibleValues[i].equals(value)) {
                currentIndex = i;
                currentValue = value;
                if (onChange != null) {
                    onChange.accept(value);
                }
                return;
            }
        }
    }
}