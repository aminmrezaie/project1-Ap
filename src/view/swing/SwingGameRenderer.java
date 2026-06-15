package view.swing;

import model.economy.Empire;
import model.enums.Position;
import model.map.GameMap;
import model.map.Hex;
import model.unit.Unit;
import view.Camera;
import view.GameRenderer;
import view.HUD;
import view.Minimap;
import view.NotificationOverlay;
import view.UnitPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SwingGameRenderer implements GameRenderer {

    private final GameWindow window;
    private final MapPanel mapPanel;
    private final HUDPanel hudPanel;
    private final UnitInfoPanel unitInfoPanel;
    private final MinimapPanel minimapPanel;

    private final Camera camera;
    private final Minimap minimap;


    private java.util.Map<Position, HighlightType> highlights = new java.util.HashMap<>();

    public SwingGameRenderer(Camera camera, Minimap minimap) {
        this.camera = camera;
        this.minimap = minimap;

        this.mapPanel = new MapPanel(camera, highlights);
        this.hudPanel = new HUDPanel();
        this.unitInfoPanel = new UnitInfoPanel();
        this.minimapPanel = new MinimapPanel(minimap);
        this.window = new GameWindow(mapPanel, hudPanel, unitInfoPanel, minimapPanel);
    }


    @Override
    public void render(GameMap map, Empire empire) {
        mapPanel.setMap(map);
        mapPanel.setEmpire(empire);
        mapPanel.repaint();
    }

    @Override
    public void renderHex(Hex hex) {
        mapPanel.repaint();
    }

    @Override
    public void renderUnit(Unit unit) {
        mapPanel.repaint();
    }

    @Override
    public void renderHUD(Empire empire, int currentTurn) {
        hudPanel.update(empire, currentTurn);
        hudPanel.repaint();
    }

    @Override
    public void highlight(List<Position> positions, HighlightType type) {
        highlights.clear();
        for (Position p : positions) highlights.put(p, type);
        mapPanel.repaint();
    }

    @Override
    public void clearHighlights() {
        highlights.clear();
        mapPanel.repaint();
    }

    @Override
    public void showNotification(String message, NotificationType type) {
        hudPanel.showNotification(message, type);
    }


    public GameWindow getWindow() {
        return window;
    }

    public MapPanel getMapPanel() {
        return mapPanel;
    }

    public HUDPanel getHudPanel() {
        return hudPanel;
    }

    public UnitInfoPanel getUnitInfoPanel() {
        return unitInfoPanel;
    }

    public MinimapPanel getMinimapPanel() {
        return minimapPanel;
    }

    public void repaintAll() {
        SwingUtilities.invokeLater(() -> {
            mapPanel.repaint();
            hudPanel.repaint();
            unitInfoPanel.repaint();
            minimapPanel.repaint();
        });
    }
}