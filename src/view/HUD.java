package view;

import model.economy.Empire;
import model.economy.ResourceStorage;
import model.enums.ResourceType;
import model.enums.UnitType;
import model.unit.Unit;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;


public class HUD {


    public static class ResourceDisplay {
        public final ResourceType type;
        public final int current;
        public final int capacity;
        public final int netRate;
        public final boolean isNegative;

        public ResourceDisplay(ResourceType type, int current, int capacity, int netRate) {
            this.type = type;
            this.current = current;
            this.capacity = capacity;
            this.netRate = netRate;
            this.isNegative = netRate < 0;
        }

        public String format() {
            String sign = netRate >= 0 ? "+" : "";
            return current + " / " + capacity + "  |  " + sign + netRate;
        }
    }


    public static class UnitCountDisplay {
        public final int total;
        public final int cap;
        public final Map<UnitType, Integer> byType;

        public UnitCountDisplay(int total, int cap, Map<UnitType, Integer> byType) {
            this.total = total;
            this.cap = cap;
            this.byType = byType;
        }

        public String formatTotal() {
            return total + " / " + cap;
        }

        public boolean isAtCap() {
            return total >= cap;
        }
    }


    public static class ProductionQueueDisplay {
        public final String currentItemName;
        public final int turnsRemaining;
        public final int queueSize;

        public ProductionQueueDisplay(String currentItemName, int turnsRemaining, int queueSize) {
            this.currentItemName = currentItemName;
            this.turnsRemaining = turnsRemaining;
            this.queueSize = queueSize;
        }

        public boolean isProducing() {
            return currentItemName != null;
        }

        public String format() {
            if (!isProducing()) return "Idle";
            return currentItemName + "  (" + turnsRemaining + " turn" + (turnsRemaining != 1 ? "s" : "") + ")";
        }
    }


    public enum WarningType {
        STARVATION,
        LOW_FOOD,
        LOW_WOOD,
        LOW_STONE,
        LOW_IRON,
        STORAGE_FULL,
        UNIT_CAP_REACHED,
        IDLE_UNITS_WITH_AP
    }

    public static class Warning {
        public final WarningType type;
        public final String message;

        public Warning(WarningType type, String message) {
            this.type = type;
            this.message = message;
        }
    }


    private int currentTurn;
    private List<ResourceDisplay> resources;
    private UnitCountDisplay unitCount;
    private ProductionQueueDisplay productionQueue;
    private List<Warning> activeWarnings;
    private boolean endTurnLocked;
    private boolean idleUnitWarning;


    public static HUD buildFrom(Empire empire, int currentTurn,
                                List<Unit> idleUnitsWithAP) {
        HUD hud = new HUD();
        hud.currentTurn = currentTurn;

        // Resources
        ResourceStorage storage = empire.getStorage();
        hud.resources = new java.util.ArrayList<>();
        for (ResourceType rt : ResourceType.values()) {
            hud.resources.add(new ResourceDisplay(
                    rt,
                    storage.get(rt),
                    storage.getCapacity(),
                    storage.getNetRate(rt)
            ));
        }

        Map<UnitType, Integer> byType = new EnumMap<>(UnitType.class);
        for (UnitType ut : UnitType.values()) byType.put(ut, 0);
        for (Unit u : empire.getUnits()) {
            byType.merge(u.getType(), 1, Integer::sum);
        }
        hud.unitCount = new UnitCountDisplay(empire.getUnitCount(), empire.getUnitCap(), byType);

        String producing = empire.getTownHall().currentProduction();
        int turnsLeft = empire.getTownHall().getTurnsRemainingForCurrent();
        int queueSize = empire.getTownHall().getQueueSize();
        hud.productionQueue = new ProductionQueueDisplay(producing, turnsLeft, queueSize);

        hud.activeWarnings = new java.util.ArrayList<>();
        if (empire.isStarving()) {
            hud.activeWarnings.add(new Warning(WarningType.STARVATION, "⚠ STARVATION — units losing AP!"));
        }
        if (storage.get(ResourceType.FOOD) < 20) {
            hud.activeWarnings.add(new Warning(WarningType.LOW_FOOD, "Low food supply"));
        }
        if (empire.getUnitCount() >= empire.getUnitCap()) {
            hud.activeWarnings.add(new Warning(WarningType.UNIT_CAP_REACHED, "Unit cap reached"));
        }

        hud.idleUnitWarning = idleUnitsWithAP != null && !idleUnitsWithAP.isEmpty();
        hud.endTurnLocked = false;

        return hud;
    }


    public int getCurrentTurn() {
        return currentTurn;
    }

    public List<ResourceDisplay> getResources() {
        return resources;
    }

    public UnitCountDisplay getUnitCount() {
        return unitCount;
    }

    public ProductionQueueDisplay getProductionQueue() {
        return productionQueue;
    }

    public List<Warning> getActiveWarnings() {
        return activeWarnings;
    }

    public boolean isEndTurnLocked() {
        return endTurnLocked;
    }

    public boolean hasIdleUnitWarning() {
        return idleUnitWarning;
    }

    public void setEndTurnLocked(boolean locked) {
        this.endTurnLocked = locked;
    }

    public boolean hasWarnings() {
        return !activeWarnings.isEmpty();
    }

    public boolean hasCrisis() {
        return activeWarnings.stream()
                .anyMatch(w -> w.type == WarningType.STARVATION);
    }
}