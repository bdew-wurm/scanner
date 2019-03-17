package net.bdew.wurm.scanner;

import com.wurmonline.client.renderer.Color;
import com.wurmonline.client.renderer.cell.CellRenderable;

import java.util.function.Predicate;

public class Selectors {
    public static Predicate<CellRenderable> parse(String name) {
        if (name.startsWith("!"))
            return parse(name.substring(1)).negate();
        if (name.startsWith("%"))
            return selectModel(name.substring(1).toLowerCase());
        if (name.startsWith("&"))
            return selectColor(name.substring(1).toLowerCase());
        return selectName(name.toLowerCase());
    }

    private static Predicate<CellRenderable> selectName(String name) {
        return i -> i.getHoverName().toLowerCase().contains(name);
    }

    private static Predicate<CellRenderable> selectModel(String model) {
        return i -> i.getModelWrapper().getResourceUrl().toString().toLowerCase().contains(model);
    }

    private static Predicate<CellRenderable> selectColor(String name) {
        final Color col = Utils.parseColor(name);
        if (col == null) {
            return i -> false;
        } else {
            return i -> {
                Color test = i.getOutlineColor();
                return test.r == col.r && test.g == col.g && test.b == col.b;
            };
        }
    }
}
