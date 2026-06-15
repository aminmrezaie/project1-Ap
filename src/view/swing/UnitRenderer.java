package view.swing;

import model.enums.UnitType;
import model.unit.Builder;
import model.unit.Explorer;
import model.unit.Unit;
import model.unit.Worker;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;


public class UnitRenderer {


    private static final Color C_EXPLORER = new Color(0x00cfff);
    private static final Color C_BUILDER = new Color(0xffa500);
    private static final Color C_WORKER = new Color(0xaaddaa);
    private static final Color C_EXPANDER = new Color(0xff6688);

    private static final Color C_SELECTED_RING = new Color(0xffd700);
    private static final Color C_AP_HIGH = new Color(0x44ff88);
    private static final Color C_AP_MID = new Color(0xffdd00);
    private static final Color C_AP_LOW = new Color(0xff4444);

    private static final Font UNIT_FONT = new Font("SansSerif", Font.BOLD, 11);


    public static void draw(Graphics2D g2, Unit unit, int cx, int cy, int hexSize, boolean selected) {
        if (!unit.isAlive()) return;

        int r = Math.max(8, hexSize / 3);


        if (selected) {
            g2.setColor(C_SELECTED_RING);
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawOval(cx - r - 3, cy - r - 3, (r + 3) * 2, (r + 3) * 2);
            g2.setStroke(new BasicStroke(1f));
        }

        Color bodyColor = unitColor(unit.getType());
        g2.setColor(bodyColor);
        g2.fillOval(cx - r, cy - r, r * 2, r * 2);

        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawOval(cx - r, cy - r, r * 2, r * 2);
        g2.setStroke(new BasicStroke(1f));

        g2.setColor(Color.BLACK);
        g2.setFont(UNIT_FONT);
        FontMetrics fm = g2.getFontMetrics();
        String letter = unitLetter(unit.getType());
        int tx = cx - fm.stringWidth(letter) / 2;
        int ty = cy + fm.getAscent() / 2 - 1;
        g2.drawString(letter, tx, ty);

        drawAPBar(g2, unit, cx, cy + r + 4, r * 2);


        if (unit.getType() == UnitType.BUILDER) {
            drawCharges(g2, (Builder) unit, cx, cy - r - 4);
        }
    }


    private static void drawAPBar(Graphics2D g2, Unit unit, int cx, int top, int width) {
        int barH = 4;
        int barW = width;
        int bx = cx - barW / 2;

        g2.setColor(new Color(0x333333));
        g2.fillRect(bx, top, barW, barH);


        float ratio = unit.getMaxAP() == 0 ? 0f : (float) unit.getAP() / unit.getMaxAP();
        int fillW = (int) (barW * ratio);
        Color apColor = ratio > 0.6f ? C_AP_HIGH : ratio > 0.3f ? C_AP_MID : C_AP_LOW;
        g2.setColor(apColor);
        g2.fillRect(bx, top, fillW, barH);

        g2.setColor(Color.BLACK);
        g2.drawRect(bx, top, barW, barH);
    }


    private static void drawCharges(Graphics2D g2, Builder builder, int cx, int top) {
        int total = 3;
        int current = builder.getCharges();
        int dotSize = 5;
        int spacing = 8;
        int startX = cx - (total * spacing) / 2;

        for (int i = 0; i < total; i++) {
            g2.setColor(i < current ? new Color(0xffa500) : new Color(0x555555));
            g2.fillOval(startX + i * spacing, top - dotSize, dotSize, dotSize);
            g2.setColor(Color.BLACK);
            g2.drawOval(startX + i * spacing, top - dotSize, dotSize, dotSize);
        }
    }


    private static Color unitColor(UnitType type) {
        return switch (type) {
            case EXPLORER -> C_EXPLORER;
            case BUILDER -> C_BUILDER;
            case WORKER -> C_WORKER;
            case BORDER_EXPANDER -> C_EXPANDER;
        };
    }

    private static String unitLetter(UnitType type) {
        return switch (type) {
            case EXPLORER -> "E";
            case BUILDER -> "B";
            case WORKER -> "W";
            case BORDER_EXPANDER -> "X";
        };
    }
}