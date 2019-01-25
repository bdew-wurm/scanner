package net.bdew.wurm.scanner;


import com.wurmonline.client.renderer.Color;
import com.wurmonline.client.renderer.cell.CellRenderable;

import java.util.function.Predicate;

public class ScanEntry {
    public final Predicate<CellRenderable> condition;
    public final Color color;
    public final boolean notify;

    public ScanEntry(Predicate<CellRenderable> condition, Color color, boolean notify) {
        this.condition = condition;
        this.color = color;
        this.notify = notify;
    }
}
