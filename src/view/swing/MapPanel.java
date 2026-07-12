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

    private static final long serialVersionUID = 1L;


    private final Camera camera;
    private final Map<Position, HighlightType> highlights;

    private GameMap map;
    private Empire  empire;
    private Unit    selectedUnit;
    private Position hoveredHex;

    private int dragStartX, dragStartY;
    private boolean dragging;


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
        startGameLoop();
    }


    private void startGameLoop() {
        Timer timer = new Timer(16, e -> repaint());
        timer.setCoalesce(true);
        timer.start();
    }


    public void setMap(GameMap map)      { this.map    = map; }
    public void setEmpire(Empire empire) { this.empire = empire; }
    public void setSelectedUnit(Unit u)  { this.selectedUnit = u; }
    public void setInputListener(MapInputListener l) { this.inputListener = l; }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_SPEED);

        if (map == null) {
            drawPlaceholder(g2);
            g2.dispose();
            return;
        }

        int hexSize = camera.getHexSize();

        for (Hex hex : map.getAllHexes()) {
            int[] sc = toScreen(hex.getPosition());
            int cx = sc[0], cy = sc[1];

            if (cx + hexSize < 0 || cx - hexSize > getWidth())  continue;
            if (cy + hexSize < 0 || cy - hexSize > getHeight()) continue;

            HighlightType hl = highlights.get(hex.getPosition());
            if (hex.getPosition().equals(hoveredHex) && hl == null)
                hl = HighlightType.SELECTED;

            HexRenderer.draw(g2, hex, cx, cy, hexSize, hl);
        }

        if (empire != null) {
            for (Unit u : empire.getUnits()) {
                if (!u.isAlive()) continue;
                int[] sc = toScreen(u.getPosition());
                UnitRenderer.draw(g2, u, sc[0], sc[1], hexSize, u == selectedUnit);
            }
        }

        if (hoveredHex != null) {
            map.getHex(hoveredHex).ifPresent(h -> drawTooltip(g2, h));
        }

        g2.dispose();
    }



    private void drawPlaceholder(Graphics2D g2) {
        g2.setColor(GameWindow.TEXT_DIM);
        g2.setFont(new Font("SansSerif", Font.ITALIC, 14));
        String msg = "در انتظار بارگذاری نقشه...";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(msg,
                (getWidth()  - fm.stringWidth(msg)) / 2,
                (getHeight() + fm.getAscent())       / 2);
    }


    private int[] toScreen(Position pos) {
        int[] world = camera.hexToWorldPixel(pos);
        return camera.worldToScreen(world[0], world[1]);
    }


    private static final Font  TIP_FONT = new Font("Monospaced", Font.PLAIN, 11);
    private static final Color TIP_BG   = new Color(0x000000cc, true);

    private void drawTooltip(Graphics2D g2, Hex hex) {
        if (!hex.isExplored()) return;

        java.util.List<String> lines = new java.util.ArrayList<>();
        lines.add(hex.getTerrain().getDisplayName());
        if (hex.hasResource()) lines.add("Resource: " + hex.getResource().getDisplayName());
        if (hex.hasBuilding()) lines.add("Building: " + hex.getBuilding().getType().getDisplayName());
        if (hex.isOwned())     lines.add("✓ Owned");
        lines.add("(" + hex.getPosition().getQ() + ", " + hex.getPosition().getR() + ")");

        g2.setFont(TIP_FONT);
        FontMetrics fm = g2.getFontMetrics();
        int lh   = fm.getHeight();
        int padX = 8, padY = 5;
        int bw   = lines.stream().mapToInt(fm::stringWidth).max().orElse(80) + padX * 2;
        int bh   = lines.size() * lh + padY * 2;

        Point mouse = getMousePosition();
        if (mouse == null) return;
        int tx = Math.min(mouse.x + 16, getWidth()  - bw - 4);
        int ty = Math.min(mouse.y + 16, getHeight() - bh - 4);

        g2.setColor(TIP_BG);
        g2.fillRoundRect(tx, ty, bw, bh, 8, 8);
        g2.setColor(GameWindow.BORDER_COLOR);
        g2.drawRoundRect(tx, ty, bw, bh, 8, 8);

        g2.setColor(GameWindow.TEXT_PRIMARY);
        for (int i = 0; i < lines.size(); i++)
            g2.drawString(lines.get(i), tx + padX, ty + padY + fm.getAscent() + i * lh);
    }


    private void attachListeners() {

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1) return;
                Position pos = camera.screenToHex(e.getX(), e.getY());

                if (empire != null) {
                    for (Unit u : empire.getUnits()) {
                        if (u.isAlive() && u.getPosition().equals(pos)) {
                            selectedUnit = u;
                            if (inputListener != null) inputListener.onUnitClicked(u);
                            return;
                        }
                    }
                }
                if (inputListener != null) inputListener.onHexClicked(pos, e.getButton());
            }

            @Override public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    dragStartX = e.getX();
                    dragStartY = e.getY();
                    dragging   = true;
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
            }

            @Override public void mouseReleased(MouseEvent e) {
                if (dragging) {
                    dragging = false;
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                Position pos = camera.screenToHex(e.getX(), e.getY());
                if (!pos.equals(hoveredHex)) hoveredHex = pos;
            }

            @Override public void mouseDragged(MouseEvent e) {
                if (!dragging) return;
                int dx = e.getX() - dragStartX;
                int dy = e.getY() - dragStartY;
                camera.pan(dx, dy);
                dragStartX = e.getX();
                dragStartY = e.getY();
            }
        });

        addMouseWheelListener(e -> {
            int mx = e.getX(), my = e.getY();
            Position before = camera.screenToHex(mx, my);
            if (e.getWheelRotation() < 0) camera.zoomIn();
            else                           camera.zoomOut();
            int[] after = camera.hexToWorldPixel(before);
            int[] scAfter = camera.worldToScreen(after[0], after[1]);
            camera.pan(mx - scAfter[0], my - scAfter[1]);
        });

        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                int step = 40;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT,  KeyEvent.VK_A -> camera.pan( step, 0);
                    case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> camera.pan(-step, 0);
                    case KeyEvent.VK_UP,    KeyEvent.VK_W -> camera.pan(0,  step);
                    case KeyEvent.VK_DOWN,  KeyEvent.VK_S -> camera.pan(0, -step);
                    case KeyEvent.VK_PLUS,  KeyEvent.VK_EQUALS -> camera.zoomIn();
                    case KeyEvent.VK_MINUS               -> camera.zoomOut();
                }
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                camera.setViewportSize(getWidth(), getHeight());
            }
        });
    }
}
