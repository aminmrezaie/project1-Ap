package view.swing;

import model.economy.Empire;
import model.enums.Position;
import model.map.GameMap;
import model.map.Hex;
import model.unit.Unit;
import view.Camera;
import view.GameRenderer;
import view.MainMenu;
import view.Minimap;
import view.UnitPanel;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SwingGameRenderer implements GameRenderer {


    private final GameWindow    window;
    private final MapPanel      mapPanel;
    private final HUDPanel      hudPanel;
    private final UnitInfoPanel unitInfoPanel;
    private final MinimapPanel  minimapPanel;

    private final Camera  camera;
    private final Minimap minimap;

    private final Map<Position, HighlightType> highlights = new HashMap<>();


    public SwingGameRenderer(Camera camera, Minimap minimap) {
        this.camera  = camera;
        this.minimap = minimap;

        this.mapPanel      = new MapPanel(camera, highlights);
        this.hudPanel      = new HUDPanel();
        this.unitInfoPanel = new UnitInfoPanel();
        this.minimapPanel  = new MinimapPanel(minimap);

        this.window = new GameWindow(mapPanel, hudPanel, unitInfoPanel, minimapPanel);

        minimapPanel.setClickListener(pos -> {
            camera.centerOn(pos);
            mapPanel.requestFocusInWindow();
        });
    }


    @Override
    public void render(GameMap map, Empire empire) {
        SwingUtilities.invokeLater(() -> {
            mapPanel.setMap(map);
            mapPanel.setEmpire(empire);
            if (minimap != null) {
                Minimap.MinimapSnapshot snap = minimap.snapshot(map, empire);
                minimapPanel.updateSnapshot(snap);
            }
        });
    }

    @Override
    public void renderHex(Hex hex) {
    }

    @Override
    public void renderUnit(Unit unit) {
    }

    @Override
    public void renderHUD(Empire empire, int currentTurn) {
        SwingUtilities.invokeLater(() -> hudPanel.update(empire, currentTurn));
    }

    @Override
    public void highlight(List<Position> positions, HighlightType type) {
        highlights.clear();
        for (Position p : positions) highlights.put(p, type);
    }

    @Override
    public void clearHighlights() {
        highlights.clear();
    }

    @Override
    public void showNotification(String message, NotificationType type) {
        SwingUtilities.invokeLater(() -> hudPanel.showNotification(message, type));
    }


    public void setEndTurnListener(java.awt.event.ActionListener l) {
        hudPanel.setEndTurnListener(l);
    }

    public void setMapInputListener(MapPanel.MapInputListener l) {
        mapPanel.setInputListener(l);
    }

    public void setUnitPanelListener(UnitPanel.PanelListener l) {
        unitInfoPanel.setListener(l);
    }

    public void setWindowCloseListener(Runnable onClose) {
        window.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                onClose.run();
            }
        });
    }


    public void selectUnit(Unit unit, List<view.UnitPanel.ActionButton> buttons) {
        SwingUtilities.invokeLater(() -> {
            mapPanel.setSelectedUnit(unit);
            if (unit != null) unitInfoPanel.showUnit(unit, buttons);
            else              unitInfoPanel.showEmpty();
        });
    }

    public void clearSelection() {
        SwingUtilities.invokeLater(() -> {
            mapPanel.setSelectedUnit(null);
            unitInfoPanel.showEmpty();
        });
    }


    public void showMainMenu(MainMenu model, MainMenu.MenuListener listener) {
        SwingUtilities.invokeLater(() -> {
            MainMenuPanel menuPanel = new MainMenuPanel(model, listener);
            window.showMainMenu(menuPanel);
        });
    }


    public GameWindow    getWindow()        { return window; }
    public MapPanel      getMapPanel()      { return mapPanel; }
    public HUDPanel      getHudPanel()      { return hudPanel; }
    public UnitInfoPanel getUnitInfoPanel() { return unitInfoPanel; }
    public MinimapPanel  getMinimapPanel()  { return minimapPanel; }
}
