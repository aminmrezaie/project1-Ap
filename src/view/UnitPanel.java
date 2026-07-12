package view;

import model.buildings.Building;
import model.enums.BuildingType;
import model.enums.Position;
import model.unit.Builder;
import model.unit.BorderExpander;
import model.unit.Explorer;
import model.unit.Unit;
import model.unit.Worker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UnitPanel {


    public enum Action {
        MOVE,
        BUILD,
        STATION,
        UNSTATION,
        EXPAND_BORDER,
        AUTO_EXPLORE,
        SKIP_TURN
    }

    public static class ActionButton {
        public final Action action;
        public final String label;
        public final boolean enabled;   // false = greyed out (not enough AP / invalid state)
        public final String tooltip;

        public ActionButton(Action action, String label, boolean enabled, String tooltip) {
            this.action  = action;
            this.label   = label;
            this.enabled = enabled;
            this.tooltip = tooltip;
        }
    }


    private Unit selectedUnit;
    private List<ActionButton> actionButtons;
    private boolean visible;


    public interface PanelListener {
        void onActionClicked(Action action, Unit unit);
    }

    private PanelListener listener;


    public UnitPanel() {
        this.visible       = false;
        this.actionButtons = Collections.emptyList();
    }


    public void selectUnit(Unit unit) {
        this.selectedUnit = unit;
        this.visible      = unit != null;
        this.actionButtons = unit != null ? buildButtons(unit) : Collections.emptyList();
    }

    public void clearSelection() {
        selectUnit(null);
    }


    private List<ActionButton> buildButtons(Unit unit) {
        List<ActionButton> buttons = new ArrayList<>();

        switch (unit.getType()) {

            case EXPLORER -> {
                Explorer e = (Explorer) unit;
                buttons.add(new ActionButton(
                        Action.MOVE, "Move",
                        unit.hasAP(1),
                        "Move to an adjacent hex  (AP: terrain cost)"
                ));
                buttons.add(new ActionButton(
                        Action.AUTO_EXPLORE,
                        e.isAutoExploreEnabled() ? "Stop Auto" : "Auto-Explore",
                        true,
                        "Toggle autonomous exploration"
                ));
                buttons.add(new ActionButton(
                        Action.SKIP_TURN, "Skip",
                        unit.getAP() > 0,
                        "Skip remaining AP this turn"
                ));
            }

            case BUILDER -> {
                Builder b = (Builder) unit;
                buttons.add(new ActionButton(
                        Action.MOVE, "Move",
                        unit.hasAP(1),
                        "Move to an adjacent hex  (AP: terrain cost)"
                ));
                buttons.add(new ActionButton(
                        Action.BUILD, "Build",
                        unit.hasAP(1) && b.hasCharges(),
                        b.hasCharges()
                                ? "Choose a building to construct  (charges: " + b.getCharges() + ")"
                                : "No charges remaining — Builder will be removed"
                ));
                buttons.add(new ActionButton(
                        Action.SKIP_TURN, "Skip",
                        unit.getAP() > 0,
                        "Skip remaining AP this turn"
                ));
            }

            case WORKER -> {
                Worker w = (Worker) unit;
                buttons.add(new ActionButton(
                        Action.MOVE, "Move",
                        unit.hasAP(1) && !w.isStationed(),
                        w.isStationed() ? "Unstation first to move" : "Move to an adjacent hex"
                ));
                if (w.isStationed()) {
                    Building b = w.getAssignedBuilding();
                    buttons.add(new ActionButton(
                            Action.UNSTATION, "Leave Building",
                            true,
                            "Exit " + b.getType().getDisplayName()
                    ));
                } else {
                    buttons.add(new ActionButton(
                            Action.STATION, "Enter Building",
                            unit.hasAP(1),
                            "Station in an adjacent production building  (AP: 1)"
                    ));
                }
                buttons.add(new ActionButton(
                        Action.SKIP_TURN, "Skip",
                        unit.getAP() > 0,
                        "Skip remaining AP this turn"
                ));
            }

            case BORDER_EXPANDER -> {
                buttons.add(new ActionButton(
                        Action.MOVE, "Move",
                        unit.hasAP(1),
                        "Move to an adjacent hex  (AP: terrain cost)"
                ));
                buttons.add(new ActionButton(
                        Action.EXPAND_BORDER, "Expand Here",
                        unit.hasAP(2),
                        "Claim this hex + 6 neighbors as territory  (AP: 2)  —  one-time use"
                ));
                buttons.add(new ActionButton(
                        Action.SKIP_TURN, "Skip",
                        unit.getAP() > 0,
                        "Skip remaining AP this turn"
                ));
            }
        }

        return Collections.unmodifiableList(buttons);
    }


    public List<BuildingType> getAvailableBuildOptions(model.map.Hex targetHex,
                                                       boolean stoneTechUnlocked,
                                                       boolean ironTechUnlocked) {
        if (targetHex == null || targetHex.hasBuilding() || !targetHex.isOwned()) {
            return Collections.emptyList();
        }

        List<BuildingType> options = new ArrayList<>();
        switch (targetHex.getTerrain()) {
            case FOREST   -> options.add(BuildingType.LUMBER_MILL);
            case MOUNTAIN -> {
                if (stoneTechUnlocked && targetHex.getResource() == model.enums.ResourceType.STONE) {
                    options.add(BuildingType.STONE_MINE);
                }
                if (ironTechUnlocked && targetHex.getResource() == model.enums.ResourceType.IRON) {
                    options.add(BuildingType.IRON_MINE);
                }
            }
            case FARMLAND -> options.add(BuildingType.FARM);
            case PLAIN    -> {
                if (targetHex.hasResource()) {
                    options.add(BuildingType.STABLE);
                } else {

                    options.add(BuildingType.VILLAGE);
                    options.add(BuildingType.TOWN);
                }
            }
        }
        return Collections.unmodifiableList(options);
    }


    public void onActionClicked(Action action) {
        if (selectedUnit == null || listener == null) return;
        listener.onActionClicked(action, selectedUnit);
    }


    public Unit getSelectedUnit()           { return selectedUnit; }
    public List<ActionButton> getButtons()  { return actionButtons; }
    public boolean isVisible()              { return visible; }
    public boolean hasSelection()           { return selectedUnit != null; }
    public void setListener(PanelListener l){ this.listener = l; }
}
