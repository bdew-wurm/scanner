package net.bdew.wurm.scanner;

import com.wurmonline.client.renderer.Color;
import com.wurmonline.client.renderer.cell.CellRenderable;

import java.util.function.Predicate;

public class CommandHandler {
    public static boolean handleInput(final String cmd, final String[] data) {
        boolean first = true;
        if (cmd.equals("scan")) {
            OutlineRender.entries.clear();

            Predicate<CellRenderable> current = null;

            for (String term : data) {
                if (first) {
                    first = false;
                    continue;
                }
                if (term.startsWith("#") || term.startsWith("@")) {
                    ScanEntry ent = finalize(term, current);
                    if (ent != null) OutlineRender.entries.add(ent);
                    current = null;
                } else if (term.startsWith("|")) {
                    Predicate<CellRenderable> parsed = selector(term.substring(1));
                    if (current == null) {
                        ScannerMod.hud.consoleOutput(String.format("Warning: no previous conditions specified for %s", term));
                    } else {
                        current = parsed.or(current);
                    }
                } else if (current == null) {
                    current = selector(term);
                } else {
                    current = current.and(selector(term));
                }
            }

            if (current != null) {
                OutlineRender.entries.add(finalize(null, current));
            }

            if (OutlineRender.entries.isEmpty()) {
                ScannerMod.hud.consoleOutput("Scanner disabled");
            } else {
                ScannerMod.hud.consoleOutput(String.format("Scanner setup with %d entries", OutlineRender.entries.size()));
            }

            return true;
        }
        return false;
    }

    private static ScanEntry finalize(String term, Predicate<CellRenderable> cond) {
        if (cond == null) {
            if (term != null)
                ScannerMod.hud.consoleOutput(String.format("Warning: no conditions specified for %s - skipped", term));
            return null;
        }

        if (term == null) {
            return new ScanEntry(cond, null, false);
        }

        boolean notify = term.startsWith("#");
        term = term.substring(1);
        Color col = parseColor(term);
        return new ScanEntry(cond, col, notify);
    }

    private static Predicate<CellRenderable> selector(String name) {
        if (name.startsWith("!"))
            return selector(name.substring(1)).negate();
        if (name.startsWith("%"))
            return selectModel(name.substring(1).toLowerCase());
        return selectName(name.toLowerCase());
    }

    private static Predicate<CellRenderable> selectName(String name) {
        return i -> i.getHoverName().toLowerCase().contains(name);
    }

    private static Predicate<CellRenderable> selectModel(String model) {
        return i -> i.getModelWrapper().getResourceUrl().toString().toLowerCase().contains(model);
    }

    private static Color parseColor(String c) {
        if (c == null || c.isEmpty()) return null;
        switch (c.toLowerCase()) {
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
        }

        try {
            java.awt.Color color = java.awt.Color.decode(c);
            return new Color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
        } catch (NumberFormatException e) {
            ScannerMod.hud.consoleOutput(String.format("Warning: Invalid color - %s", c));
            return null;
        }
    }
}
