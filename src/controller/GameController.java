package controller;

import model.buildings.Building;
import model.buildings.TownHall;
import model.economy.Empire;
import model.enums.BuildingType;
import model.enums.Position;
import model.map.GameMap;
import model.unit.BorderExpander;
import model.unit.Builder;
import model.unit.Explorer;
import model.unit.Unit;
import model.unit.Worker;
import view.GameRenderer;
import view.GameRenderer.HighlightType;
import view.GameRenderer.NotificationType;
import view.MainMenu;
import view.UnitPanel;
import view.UnitPanel.Action;
import view.UnitPanel.ActionButton;
import view.ViewManager;
import view.swing.SwingGameRenderer;

import java.util.ArrayList;
import java.util.List;

public class GameController {


    private GameMap map;
    private Empire   empire;


    private final MapGenerator     mapGenerator;
    private final MovementSystem   movementSystem;
    private final BuildingSystem   buildingSystem;
    private final BorderSystem     borderSystem;
    private final ProductionSystem productionSystem;
    private final FogOfWarSystem   fogOfWarSystem;
    private final UpgradeSystem    upgradeSystem;
    private final AutoExploreAI    autoExploreAI;
    private final TurnManager      turnManager;


    private final ViewManager        viewManager;
    private final SwingGameRenderer  renderer;


    private enum PendingMode { NONE, MOVE, BUILD_PLACEMENT, EXPAND_BORDER }
    private PendingMode pendingMode = PendingMode.NONE;
    private BuildingType pendingBuildType;

    private Unit selectedUnit;


    private static final int MAP_RADIUS = 10;


    public GameController(int viewportWidth, int viewportHeight) {
        this.mapGenerator     = new MapGenerator();
        this.fogOfWarSystem   = new FogOfWarSystem();
        this.movementSystem   = new MovementSystem(fogOfWarSystem);
        this.upgradeSystem    = new UpgradeSystem();
        this.buildingSystem   = new BuildingSystem(upgradeSystem);
        this.borderSystem     = new BorderSystem();
        this.productionSystem = new ProductionSystem();
        this.autoExploreAI    = new AutoExploreAI(movementSystem);
        this.turnManager      = new TurnManager(productionSystem, fogOfWarSystem);

        this.viewManager = new ViewManager(viewportWidth, viewportHeight);
        this.renderer    = new SwingGameRenderer(viewManager.getCamera(), viewManager.getMinimap());

        wireListeners();
    }


    public void start() {
        renderer.getWindow().showWindow();
        showMainMenu();
    }

    private void showMainMenu() {
        renderer.showMainMenu(viewManager.getMainMenu(), new MainMenu.MenuListener() {
            @Override public void onStartGame() {
                startNewGame();
            }
            @Override public void onExitConfirmed() {
                System.exit(0);
            }
            @Override public void onMusicVolumeChanged(float volume) {
            }
        });
    }


    private void startNewGame() {
        this.map = mapGenerator.generate(MAP_RADIUS);

        Position origin = new Position(0, 0);
        TownHall townHall = new TownHall(origin);
        map.getHex(origin).ifPresent(h -> h.setBuilding(townHall));

        this.empire = new Empire(townHall, townHall.getStorageCapacity());

        spawnInitialUnits(origin);

        viewManager.transitionToGame(map);

        fogOfWarSystem.updateVisibility(map, empire.getUnits(), empire.getBuildings());
        fullRender();
    }

    private void spawnInitialUnits(Position origin) {
        for (int i = 0; i < 2; i++) empire.addUnit(new Builder(origin));
        for (int i = 0; i < 2; i++) empire.addUnit(new Worker(origin));
        empire.addUnit(new Explorer(origin));
    }


    private void wireListeners() {

        renderer.setEndTurnListener(e -> handleEndTurn());

        renderer.setMapInputListener(new view.swing.MapPanel.MapInputListener() {
            @Override public void onHexClicked(Position pos, int button) {
                handleHexClicked(pos);
            }
            @Override public void onUnitClicked(Unit unit) {
                handleUnitSelected(unit);
            }
        });

        renderer.setUnitPanelListener((action, unit) -> handleUnitAction(action, unit));

        renderer.setWindowCloseListener(this::handleWindowClose);
    }


    private void handleUnitSelected(Unit unit) {
        this.selectedUnit = unit;
        pendingMode = PendingMode.NONE;
        viewManager.clearSelection();
        renderer.clearHighlights();

        List<ActionButton> buttons = buildActionButtons(unit);
        renderer.selectUnit(unit, buttons);
    }

    private List<ActionButton> buildActionButtons(Unit unit) {
        UnitPanel panel = viewManager.getUnitPanel();
        panel.selectUnit(unit);
        return panel.getButtons();
    }


    private void handleHexClicked(Position pos) {
        if (selectedUnit == null) return;

        switch (pendingMode) {
            case MOVE              -> doMove(pos);
            case BUILD_PLACEMENT   -> doBuild(pos);
            case EXPAND_BORDER     -> doExpandBorder(pos);
            case NONE              -> {  }
        }
    }



    private void handleUnitAction(Action action, Unit unit) {
        switch (action) {
            case MOVE -> {
                pendingMode = PendingMode.MOVE;
                List<Position> reachable = movementSystem.getReachablePositions(unit, map);
                renderer.highlight(reachable, HighlightType.MOVEMENT_RANGE);
            }
            case BUILD -> startBuildSelection((Builder) unit);
            case STATION -> doStation(unit);
            case UNSTATION -> doUnstation((Worker) unit);
            case EXPAND_BORDER -> {
                pendingMode = PendingMode.EXPAND_BORDER;
                List<Position> preview = borderSystem.previewExpand(unit.getPosition(), map);
                renderer.highlight(preview, HighlightType.BORDER_EXPAND_PREVIEW);
            }
            case AUTO_EXPLORE -> doAutoExploreToggle((Explorer) unit);
            case SKIP_TURN -> {
                unit.skipTurn();
                refreshSelection(unit);
            }
        }
    }


    private void doMove(Position dest) {
        boolean moved = movementSystem.executeMove(selectedUnit, dest, map);
        if (moved) {
            fogOfWarSystem.updateVisibility(map, empire.getUnits(), empire.getBuildings());
            renderer.clearHighlights();
            pendingMode = PendingMode.NONE;
            refreshSelection(selectedUnit);
            fullRender();
        } else {
            renderer.showNotification("Cannot move there", NotificationType.WARNING);
        }
    }

    private void startBuildSelection(Builder builder) {
        var hexOpt = map.getHex(builder.getPosition());
        if (hexOpt.isEmpty()) {
            renderer.showNotification("Invalid position", NotificationType.WARNING);
            return;
        }

        List<BuildingType> options = viewManager.getUnitPanel().getAvailableBuildOptions(
                hexOpt.get(),
                upgradeSystem.isStoneTechUnlocked(),
                upgradeSystem.isIronTechUnlocked()
        );

        if (options.isEmpty()) {
            renderer.showNotification("Nothing can be built on this tile", NotificationType.WARNING);
            return;
        }

        if (options.size() == 1) {
            confirmBuildType(builder, options.get(0));
            return;
        }

        BuildingType choice = (BuildingType) javax.swing.JOptionPane.showInputDialog(
                renderer.getWindow(),
                "Choose a building to construct:",
                "Build",
                javax.swing.JOptionPane.PLAIN_MESSAGE,
                null,
                options.toArray(),
                options.get(0)
        );

        if (choice != null) {
            confirmBuildType(builder, choice);
        }
    }

    private void confirmBuildType(Builder builder, BuildingType type) {
        pendingMode      = PendingMode.BUILD_PLACEMENT;
        pendingBuildType = type;
        renderer.highlight(List.of(builder.getPosition()), HighlightType.BUILD_TARGET);
    }

    private void doBuild(Position pos) {
        if (!(selectedUnit instanceof Builder builder)) return;
        if (pendingBuildType == null) {
            renderer.showNotification("No buildable structure here", NotificationType.WARNING);
            return;
        }

        Building built = buildingSystem.executeBuild(builder, pendingBuildType, pos, map, empire);
        if (built != null) {
            renderer.clearHighlights();
            pendingMode = PendingMode.NONE;
            pendingBuildType = null;
            renderer.showNotification(built.getType().getDisplayName() + " constructed", NotificationType.SUCCESS);
            refreshSelection(selectedUnit);
            fullRender();
        } else {
            renderer.showNotification("Cannot build here", NotificationType.WARNING);
        }
    }


    private void doStation(Unit unit) {
        if (!(unit instanceof Worker worker)) return;

        for (Position neighbor : worker.getPosition().neighbors()) {
            var hexOpt = map.getHex(neighbor);
            if (hexOpt.isPresent() && hexOpt.get().hasBuilding() && !hexOpt.get().getBuilding().isFull()) {
                BuildingSystem.StationResult result = buildingSystem.stationWorker(worker, neighbor, map);
                if (result == BuildingSystem.StationResult.OK) {
                    renderer.showNotification("Worker stationed", NotificationType.SUCCESS);
                    refreshSelection(unit);
                    fullRender();
                    return;
                }
            }
        }
        renderer.showNotification("No adjacent building available", NotificationType.WARNING);
    }

    private void doUnstation(Worker worker) {
        worker.unstation();
        refreshSelection(worker);
        fullRender();
    }



    private void doExpandBorder(Position center) {
        if (!(selectedUnit instanceof BorderExpander expander)) return;

        List<Position> added = borderSystem.executeExpand(expander, center, map, empire);
        if (added != null) {
            renderer.clearHighlights();
            pendingMode = PendingMode.NONE;
            renderer.showNotification("Territory expanded (+" + added.size() + " hexes)", NotificationType.SUCCESS);
            viewManager.clearSelection();
            selectedUnit = null;
            renderer.selectUnit(null, List.of());
            fullRender();
        } else {
            renderer.showNotification("Cannot expand here — unexplored hexes nearby", NotificationType.WARNING);
        }
    }


    private void doAutoExploreToggle(Explorer explorer) {
        explorer.toggleAutoExplore();
        if (explorer.isAutoExploreEnabled()) {
            autoExploreAI.runFullTurn(explorer, map);
            fogOfWarSystem.updateVisibility(map, empire.getUnits(), empire.getBuildings());
        }
        refreshSelection(explorer);
        fullRender();
    }


    private void handleEndTurn() {
        for (Unit u : empire.getUnits()) {
            if (u instanceof Explorer ex && ex.isAutoExploreEnabled()) {
                autoExploreAI.runFullTurn(ex, map);
            }
        }

        TurnManager.TurnReport report = turnManager.endTurn(empire);

        fogOfWarSystem.updateVisibility(map, empire.getUnits(), empire.getBuildings());

        if (report.starvationTriggered) {
            renderer.showNotification("⚠ STARVATION — units losing AP!", NotificationType.CRISIS);
        }
        if (report.unitProduced != null) {
            renderer.showNotification(report.unitProduced + " produced", NotificationType.SUCCESS);
        }
        for (Building b : report.destroyedBuildings) {
            renderer.showNotification(b.getType().getDisplayName() + " collapsed (no upkeep)", NotificationType.WARNING);
        }

        viewManager.clearSelection();
        selectedUnit = null;
        renderer.selectUnit(null, List.of());
        fullRender();
    }


    private void handleWindowClose() {
        int result = javax.swing.JOptionPane.showConfirmDialog(
                renderer.getWindow(),
                "Are you sure you want to exit?",
                "Exit Game",
                javax.swing.JOptionPane.YES_NO_OPTION
        );
        if (result == javax.swing.JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }


    private void refreshSelection(Unit unit) {
        List<ActionButton> buttons = buildActionButtons(unit);
        renderer.selectUnit(unit, buttons);
    }

    private void fullRender() {
        renderer.render(map, empire);
        renderer.renderHUD(empire, turnManager.getCurrentTurn());
    }
}

