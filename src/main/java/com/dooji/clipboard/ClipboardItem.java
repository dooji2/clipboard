package com.dooji.clipboard;

public class ClipboardItem {
    private final String text;
    private final String date;

    public ClipboardItem(String text, String date) {
        this.text = text;
        this.date = date;
    }

    public String getText() {
        return text;
    }

    public String getDate() {
        return date;
    }
}