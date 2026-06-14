package view;

import model.economy.Empire;
import model.map.GameMap;
import model.unit.Unit;

import java.util.List;

public class ViewManager {

    private final MainMenu mainMenu;
    private final HUD hud;
    private final Minimap minimap;
    private final UnitPanel unitPanel;
    private final Camera camera;
    private final NotificationOverlay notifications;

    public enum Scene {MAIN_MENU, GAME}

    private Scene currentScene;

    public ViewManager(int viewportWidth, int viewportHeight) {
        this.mainMenu = new MainMenu();
        this.hud = new HUD();
        this.minimap = new Minimap();
        this.unitPanel = new UnitPanel();
        this.camera = new Camera(viewportWidth, viewportHeight);
        this.notifications = new NotificationOverlay();
        this.currentScene = Scene.MAIN_MENU;
    }


    public void transitionToGame(GameMap map) {
        this.currentScene = Scene.GAME;
        minimap.calibrate(map.getWidth(), map.getHeight());
        camera.setMapPixelSize(
                map.getWidth() * camera.getHexSize(),
                map.getHeight() * camera.getHexSize()
        );
    }

    public void transitionToMainMenu() {
        this.currentScene = Scene.MAIN_MENU;
        unitPanel.clearSelection();
    }

    public void tick() {
        notifications.tick();
    }

    public HUD buildHUD(Empire empire, int currentTurn, List<Unit> idleUnitsWithAP) {
        return HUD.buildFrom(empire, currentTurn, idleUnitsWithAP);
    }

    public model.enums.Position onMapClick(int screenX, int screenY) {
        return camera.screenToHex(screenX, screenY);
    }

    public model.enums.Position onMinimapClick(int minimapX, int minimapY) {
        model.enums.Position target = minimap.onClick(minimapX, minimapY);
        if (target != null) camera.centerOn(target);
        return target;
    }

    public void notify(String message, GameRenderer.NotificationType type) {
        notifications.show(message, type);
    }

    public void notifyStarvation() {
        notifications.crisis("⚠ STARVATION — units losing AP!");
    }

    public void notifyLowFood() {
        notifications.warning("Food supply running low");
    }

    public void notifyUnitCapHit() {
        notifications.warning("Unit cap reached — build Town or Village");
    }

    public void notifyBuildDone(String buildingName) {
        notifications.success(buildingName + " constructed");
    }

    public void selectUnit(Unit unit) {
        unitPanel.selectUnit(unit);
    }

    public void clearSelection() {
        unitPanel.clearSelection();
    }

    public Scene getCurrentScene() {
        return currentScene;
    }

    public MainMenu getMainMenu() {
        return mainMenu;
    }

    public Minimap getMinimap() {
        return minimap;
    }

    public UnitPanel getUnitPanel() {
        return unitPanel;
    }

    public Camera getCamera() {
        return camera;
    }

    public NotificationOverlay getNotifications() {
        return notifications;
    }

    public boolean isInGame() {
        return currentScene == Scene.GAME;
    }

    public boolean isInMainMenu() {
        return currentScene == Scene.MAIN_MENU;
    }
}
