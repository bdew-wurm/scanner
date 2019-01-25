package net.bdew.wurm.scanner;

import com.wurmonline.client.comm.ServerConnectionListenerClass;
import com.wurmonline.client.game.PlayerPosition;
import com.wurmonline.client.renderer.Color;
import com.wurmonline.client.renderer.PickRenderer;
import com.wurmonline.client.renderer.PickableUnit;
import com.wurmonline.client.renderer.WorldRender;
import com.wurmonline.client.renderer.backend.Primitive;
import com.wurmonline.client.renderer.backend.Queue;
import com.wurmonline.client.renderer.backend.RenderState;
import com.wurmonline.client.renderer.cell.CellRenderable;
import com.wurmonline.client.renderer.cell.CreatureCellRenderable;
import com.wurmonline.client.renderer.cell.GroundItemCellRenderable;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


public class OutlineRender {
    public static List<ScanEntry> entries = new LinkedList<>();

    private static PickRenderer pickRenderer;
    private static PickRenderer.CustomPickFillRender customPickFill;
    private static PickRenderer.CustomPickFillDepthRender customPickFillDepth;
    private static PickRenderer.CustomPickOutlineRender customPickOutline;

    private static Field groundItemsField;

    private final static String[] headings = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
    private final static double PI = Math.PI;
    private static long nextNotify = Long.MIN_VALUE;

    static public void render(WorldRender wr, Queue queue) {
        if (entries.isEmpty()) return;
        if (pickRenderer == null) {
            try {
                pickRenderer = ReflectionUtil.getPrivateField(wr, ReflectionUtil.getField(WorldRender.class, "pickRenderer"));
                customPickFill = ReflectionUtil.getPrivateField(wr, ReflectionUtil.getField(WorldRender.class, "customPickFill"));
                customPickFillDepth = ReflectionUtil.getPrivateField(wr, ReflectionUtil.getField(WorldRender.class, "customPickFillDepth"));
                customPickOutline = ReflectionUtil.getPrivateField(wr, ReflectionUtil.getField(WorldRender.class, "customPickOutline"));
                groundItemsField = ReflectionUtil.getField(ServerConnectionListenerClass.class, "groundItems");
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            ServerConnectionListenerClass conn = ScannerMod.hud.getWorld().getServerConnection().getServerConnectionListener();
            @SuppressWarnings("unchecked")
            Collection<GroundItemCellRenderable> items = ((Map<Long, GroundItemCellRenderable>) ReflectionUtil.getPrivateField(conn, groundItemsField)).values();
            Collection<CreatureCellRenderable> creatures = conn.getCreatures().values();

            final boolean doNotify = nextNotify < System.currentTimeMillis();
            if (doNotify) nextNotify = System.currentTimeMillis() + 5000;

            Consumer<CellRenderable> process = item -> {
                for (ScanEntry entry : entries) {
                    if (entry.condition.test(item)) {
                        renderOutline(queue, item, entry.color);
                        if (doNotify && entry.notify) doNotify(item, entry.color);
                        break;
                    }
                }
            };

            items.forEach(process);
            creatures.forEach(process);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    private static void doNotify(CellRenderable item, Color color) {
        if (color == null) color = new Color(1, 0, 1);
        PlayerPosition plPos = ScannerMod.hud.getWorld().getPlayer().getPos();
        double at = Math.atan2(plPos.getY() - item.getYPos(), plPos.getX() - item.getXPos());
        double degrees = (at > 0 ? at : (2 * PI + at)) * 360 / (2 * PI) - 90;
        double slice = 360.0f / headings.length;
        double a = degrees + slice / 2.0f;
        int idx = (int) (a / slice) % headings.length;
        if (idx < 0) {
            idx += headings.length;
        }
        String heading = headings[idx];
        String distance = String.format("%.1f", Math.sqrt((item.getXPos() - plPos.getX()) * (item.getXPos() - plPos.getX()) + (item.getYPos() - plPos.getY()) * (item.getYPos() - plPos.getY())));
        ScannerMod.hud.addOnscreenMessage(String.format("%s %s %s!", item.getHoverName(), distance, heading), color.r, color.g, color.b, (byte) 1);
    }

    private static void renderOutline(Queue queue, PickableUnit item, Color outlineOverride) {
        RenderState renderStateFill = new RenderState();
        RenderState renderStateFillDepth = new RenderState();
        RenderState renderStateOutline = new RenderState();
        Color color = new Color(item.getOutlineColor());
        if (outlineOverride != null) color = outlineOverride;
        renderStateFill.alphaval = color.a;
        renderStateFill.twosided = false;
        renderStateFill.depthtest = Primitive.TestFunc.ALWAYS;
        renderStateFill.depthwrite = true;
        renderStateFill.customstate = customPickFill;
        item.renderPicked(queue, renderStateFill, color);
        renderStateOutline.alphaval = color.a;
        renderStateOutline.twosided = false;
        renderStateOutline.depthtest = Primitive.TestFunc.LESS;
        renderStateOutline.depthwrite = false;
        renderStateOutline.blendmode = Primitive.BlendMode.ALPHABLEND;
        renderStateOutline.customstate = customPickOutline;
        item.renderPicked(queue, renderStateOutline, color);
        renderStateFillDepth.customstate = customPickFillDepth;
        renderStateFillDepth.depthtest = Primitive.TestFunc.ALWAYS;
        item.renderPicked(queue, renderStateFillDepth, color);
    }
}
