package net.bdew.wurm.scanner;

import com.wurmonline.client.renderer.Color;
import com.wurmonline.client.renderer.OutlineColors;

public class Utils {
    public static Color parseColor(String name) {
        if (name == null || name.isEmpty()) return null;
        switch (name.toLowerCase()) {
            case "red":
                return new Color(1, 0, 0);
            case "green":
                return new Color(0, 1, 0);
            case "blue":
                return new Color(0, 0, 1);
            case "white":
                return new Color(1, 1, 1);
            case "black":
                return new Color(0, 0, 0);
            case "purple":
                return new Color(1, 0, 1);
            case "pink":
                return new Color(1, 0, 0.5f);
            case "yellow":
                return new Color(1, 1, 0);
            case "neutral":
                return OutlineColors.NEUTRAL;
            case "ally":
                return OutlineColors.ALLY;
            case "friend":
                return OutlineColors.FRIEND;
            case "hostile":
                return OutlineColors.HOSTILE;
            case "gm":
                return OutlineColors.GM;
            case "dev":
                return OutlineColors.DEV;
        }

        try {
            java.awt.Color color = java.awt.Color.decode(name);
            return new Color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
        } catch (NumberFormatException e) {
            ScannerMod.hud.consoleOutput(String.format("Warning: Invalid color - %s", name));
            return null;
        }
    }
}
