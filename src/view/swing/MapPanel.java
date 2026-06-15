package view.swing;

import model.economy.Empire;
import model.enums.Position;
import model.map.GameMap;
import model.map.Hex;
import model.unit.Unit;
import view.Camera;
import view.GameRenderer.HighlightType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;


public class MapPanel extends JPanel {


    private final Camera camera;
    private final Map<Position, HighlightType> highlights;

    private GameMap map;
    private Empire  empire;
    private Unit    selectedUnit;

    private Position hoveredHex;



    public interface MapInputListener {
        void onHexClicked(Position pos, int button);
        void onUnitClicked(Unit unit);
    }

    private MapInputListener inputListener;



    public MapPanel(Camera camera, Map<Position, HighlightType> highlights) {
        this.camera     = camera;
        this.highlights = highlights;

        setBackground(GameWindow.BG_DARK);
        setFocusable(true);

        attachListeners();
    }



    public void setMap(GameMap map)       { this.map = map; }
    public void setEmpire(Empire empire)  { this.empire = empire; }
    public void setSelectedUnit(Unit u)   { this.selectedUnit = u; repaint(); }
    public void setInputListener(MapInputListener l) { this.inputListener = l; }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (map == null) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int hexSize = camera.getHexSize();

        for (Hex hex : map.getAllHexes()) {
            int[] screen = hexToScreen(hex.getPosition(), hexSize);
            int cx = screen[0], cy = screen[1];

            if (cx < -hexSize * 2 || cx > getWidth()  + hexSize * 2) continue;
            if (cy < -hexSize * 2 || cy > getHeight() + hexSize * 2) continue;

            HighlightType hl = highlights.get(hex.getPosition());
            if (hex.getPosition().equals(hoveredHex) && hl == null) {
                hl = HighlightType.SELECTED;
            }

            HexRenderer.draw(g2, hex, cx, cy, hexSize, hl);
        }

        if (empire != null) {
            for (Unit unit : empire.getUnits()) {
                if (!unit.isAlive()) continue;
                int[] screen = hexToScreen(unit.getPosition(), hexSize);
                boolean isSelected = unit == selectedUnit;
                UnitRenderer.draw(g2, unit, screen[0], screen[1], hexSize, isSelected);
            }
        }

        if (hoveredHex != null && map.getHex(hoveredHex).isPresent()) {
            drawTooltip(g2, map.getHex(hoveredHex).get());
        }

        g2.dispose();
    }


    private int[] hexToScreen(Position pos, int size) {
        int[] world = camera.hexToWorldPixel(pos);
        return camera.worldToScreen(world[0], world[1]);
    }



    private static final Font  TOOLTIP_FONT = new Font("Monospaced", Font.PLAIN, 11);
    private static final Color TOOLTIP_BG   = new Color(0x000000cc, true);

    private void drawTooltip(Graphics2D g2, Hex hex) {
        if (!hex.isExplored()) return;

        java.util.List<String> lines = new java.util.ArrayList<>();
        lines.add(hex.getTerrain().getDisplayName());
        if (hex.hasResource()) lines.add("Resource: " + hex.getResource().getDisplayName());
        if (hex.hasBuilding()) lines.add("Building: " + hex.getBuilding().getType().getDisplayName());
        if (hex.isOwned())     lines.add("Owned territory");
        lines.add("(" + hex.getPosition().getQ() + ", " + hex.getPosition().getR() + ")");

        g2.setFont(TOOLTIP_FONT);
        FontMetrics fm = g2.getFontMetrics();
        int lineH = fm.getHeight();
        int padX  = 8, padY = 6;
        int boxW  = lines.stream().mapToInt(fm::stringWidth).max().orElse(80) + padX * 2;
        int boxH  = lines.size() * lineH + padY * 2;

        Point mouse = getMousePosition();
        if (mouse == null) return;
        int tx = Math.min(mouse.x + 14, getWidth()  - boxW - 4);
        int ty = Math.min(mouse.y + 14, getHeight() - boxH - 4);

        g2.setColor(TOOLTIP_BG);
        g2.fillRoundRect(tx, ty, boxW, boxH, 8, 8);
        g2.setColor(GameWindow.BORDER_COLOR);
        g2.drawRoundRect(tx, ty, boxW, boxH, 8, 8);

        g2.setColor(GameWindow.TEXT_PRIMARY);
        for (int i = 0; i < lines.size(); i++) {
            g2.drawString(lines.get(i), tx + padX, ty + padY + fm.getAscent() + i * lineH);
        }
    }


    private void attachListeners() {

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                Position pos = camera.screenToHex(e.getX(), e.getY());

                // Check if a unit is at this position
                if (empire != null) {
                    for (Unit u : empire.getUnits()) {
                        if (u.isAlive() && u.getPosition().equals(pos)) {
                            selectedUnit = u;
                            if (inputListener != null) inputListener.onUnitClicked(u);
                            repaint();
                            return;
                        }
                    }
                }

                if (inputListener != null) inputListener.onHexClicked(pos, e.getButton());
            }
        });

        // Mouse hover (tooltip + hover highlight)
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                Position pos = camera.screenToHex(e.getX(), e.getY());
                if (!pos.equals(hoveredHex)) {
                    hoveredHex = pos;
                    repaint();
                }
            }

            @Override public void mouseDragged(MouseEvent e) {
                // Pan with right-drag
                if (SwingUtilities.isRightMouseButton(e)) {
                    // handled via press tracking in a more complete implementation
                }
            }
        });

        addMouseWheelListener(e -> {
            if (e.getWheelRotation() < 0) camera.zoomIn();
            else                           camera.zoomOut();
            repaint();
        });

        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                int step = 32;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT,  KeyEvent.VK_A -> camera.pan( step, 0);
                    case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> camera.pan(-step, 0);
                    case KeyEvent.VK_UP,    KeyEvent.VK_W -> camera.pan(0,  step);
                    case KeyEvent.VK_DOWN,  KeyEvent.VK_S -> camera.pan(0, -step);
                }
                repaint();
            }
        });
    }
}