package view.swing;

import model.enums.ResourceType;
import model.enums.TerrainType;
import model.map.Hex;
import view.GameRenderer.HighlightType;

import java.awt.*;
import java.awt.geom.Path2D;


public class HexRenderer {


    private static final Color C_PLAIN = new Color(0x4a7c40);
    private static final Color C_FOREST = new Color(0x1e4d1e);
    private static final Color C_MOUNTAIN = new Color(0x5a4a3a);
    private static final Color C_FARMLAND = new Color(0x7a9a30);


    private static final Color C_FOOD = new Color(0xffd700);
    private static final Color C_WOOD = new Color(0x8b4513);
    private static final Color C_STONE = new Color(0xaaaaaa);
    private static final Color C_IRON = new Color(0x5588aa);


    private static final Color C_UNEXPLORED = new Color(0x0d0d1a);
    private static final Color C_FOG = new Color(0x0d0d1aaa, true); // semi-transparent


    private static final Color C_HL_MOVE = new Color(0x00cfff55, true);
    private static final Color C_HL_BUILD = new Color(0xffd70055, true);
    private static final Color C_HL_BORDER = new Color(0xff444455, true);
    private static final Color C_HL_SELECT = new Color(0xffffff44, true);


    private static final Color C_BORDER_OWNED = new Color(0x4a8fc4);
    private static final Color C_BORDER_DEFAULT = new Color(0x1a2a3a);
    private static final Stroke STROKE_NORMAL = new BasicStroke(1f);
    private static final Stroke STROKE_OWNED = new BasicStroke(2f);
    private static final Stroke STROKE_SELECTED = new BasicStroke(2.5f);

    public static void draw(Graphics2D g2, Hex hex, int cx, int cy, int size, HighlightType hlType) {

        Path2D.Double shape = hexShape(cx, cy, size);

        if (!hex.isExplored()) {
            g2.setColor(C_UNEXPLORED);
            g2.fill(shape);
        } else {
            g2.setColor(terrainColor(hex.getTerrain()));
            g2.fill(shape);

            if (hex.isOwned()) {
                g2.setColor(new Color(0x4a8fc422, true));
                g2.fill(shape);
            }

            if (!hex.isVisible()) {
                g2.setColor(C_FOG);
                g2.fill(shape);
            }
        }

        if (hlType != null) {
            g2.setColor(highlightColor(hlType));
            g2.fill(shape);
        }

        if (hex.isExplored() && hex.hasResource() && hex.isVisible()) {
            drawResourceDot(g2, hex, cx, cy, size);
        }

        if (hlType == HighlightType.SELECTED) {
            g2.setStroke(STROKE_SELECTED);
            g2.setColor(GameWindow.GOLD);
        } else if (hex.isOwned()) {
            g2.setStroke(STROKE_OWNED);
            g2.setColor(C_BORDER_OWNED);
        } else {
            g2.setStroke(STROKE_NORMAL);
            g2.setColor(C_BORDER_DEFAULT);
        }
        g2.draw(shape);
        g2.setStroke(STROKE_NORMAL);
    }


    public static Path2D.Double hexShape(int cx, int cy, int size) {
        Path2D.Double path = new Path2D.Double();
        for (int i = 0; i < 6; i++) {
            double angle = Math.PI / 180 * (60 * i);
            double x = cx + size * Math.cos(angle);
            double y = cy + size * Math.sin(angle);
            if (i == 0) path.moveTo(x, y);
            else path.lineTo(x, y);
        }
        path.closePath();
        return path;
    }


    private static void drawResourceDot(Graphics2D g2, Hex hex, int cx, int cy, int size) {
        int r = Math.max(4, size / 5);
        g2.setColor(resourceColor(hex.getResource()));
        g2.fillOval(cx - r, cy - r, r * 2, r * 2);
        g2.setColor(Color.BLACK);
        g2.drawOval(cx - r, cy - r, r * 2, r * 2);
    }


    private static Color terrainColor(TerrainType t) {
        return switch (t) {
            case PLAIN -> C_PLAIN;
            case FOREST -> C_FOREST;
            case MOUNTAIN -> C_MOUNTAIN;
            case FARMLAND -> C_FARMLAND;
        };
    }

    private static Color resourceColor(ResourceType r) {
        if (r == null) return Color.WHITE;
        return switch (r) {
            case FOOD -> C_FOOD;
            case WOOD -> C_WOOD;
            case STONE -> C_STONE;
            case IRON -> C_IRON;
        };
    }

    private static Color highlightColor(HighlightType hl) {
        return switch (hl) {
            case MOVEMENT_RANGE -> C_HL_MOVE;
            case BUILD_TARGET -> C_HL_BUILD;
            case BORDER_EXPAND_PREVIEW -> C_HL_BORDER;
            case SELECTED -> C_HL_SELECT;
        };
    }
}