package com.dooji.clipboard;

import java.awt.GraphicsEnvironment;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;

public class ClipboardHelper {

    public static void copyToSystemClipboard(String text) {
        if (!GraphicsEnvironment.isHeadless()) {
            try {
                var systemClipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
                systemClipboard.setContents(new StringSelection(text), null);
            } catch (Exception ignored) {}
        }
    }

    public static String getSystemClipboardText() {
        if (GraphicsEnvironment.isHeadless()) {
            return null;
        }

        try {
            var systemClipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();

            if (systemClipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                String data = (String) systemClipboard.getData(DataFlavor.stringFlavor);
                return (data != null && !data.isEmpty()) ? data : null;
            }
        } catch (Exception ignored) {}

        return null;
    }
}