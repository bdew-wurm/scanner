package net.bdew.wurm.scanner;

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
                    if (current == null) {
                        ScannerMod.hud.consoleOutput(String.format("Warning: no conditions specified for %s - skipped", term));
                    } else {
                        boolean notify = term.startsWith("#");
                        term = term.substring(1);
                        OutlineRender.entries.add(new ScanEntry(current, Utils.parseColor(term), notify));
                    }
                    current = null;
                } else if (term.startsWith("|")) {
                    Predicate<CellRenderable> parsed = Selectors.parse(term.substring(1));
                    if (current == null) {
                        ScannerMod.hud.consoleOutput(String.format("Warning: no previous conditions specified for %s", term));
                        current = Selectors.parse(term);
                    } else {
                        current = parsed.or(current);
                    }
                } else if (current == null) {
                    current = Selectors.parse(term);
                } else {
                    current = current.and(Selectors.parse(term));
                }
            }

            if (current != null) {
                OutlineRender.entries.add(new ScanEntry(current, null, false));
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

}
