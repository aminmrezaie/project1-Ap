package controller;

import model.buildings.Building;
import model.economy.Empire;
import model.enums.ResourceType;
import model.unit.Unit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TurnManager {

    private final ProductionSystem productionSystem;
    private final FogOfWarSystem   fogOfWarSystem;

    private int currentTurn;

    private final Map<Building, Integer> unpaidUpkeepStreak = new HashMap<>();
    private static final int NEGLECT_LIMIT = 3;

    public TurnManager(ProductionSystem productionSystem, FogOfWarSystem fogOfWarSystem) {
        this.productionSystem = productionSystem;
        this.fogOfWarSystem   = fogOfWarSystem;
        this.currentTurn      = 1;
    }


    public static class TurnReport {
        public final int turnNumber;
        public final ProductionSystem.ProductionReport production;
        public final boolean starvationTriggered;
        public final String unitProduced;
        public final List<Building> destroyedBuildings;

        public TurnReport(int turnNumber, ProductionSystem.ProductionReport production,
                          boolean starvationTriggered, String unitProduced,
                          List<Building> destroyedBuildings) {
            this.turnNumber          = turnNumber;
            this.production          = production;
            this.starvationTriggered = starvationTriggered;
            this.unitProduced        = unitProduced;
            this.destroyedBuildings  = destroyedBuildings;
        }
    }

    public TurnReport endTurn(Empire empire) {

        empire.refreshAllUnitAP();

        ProductionSystem.ProductionReport report = productionSystem.processTurn(empire);

        List<Building> destroyed = trackNeglect(empire);

        String unitProduced = productionSystem.tickUnitProduction(empire);

        boolean starving = empire.isStarving();
        if (starving) {
            applyStarvationPenalty(empire);
        }

        empire.removeDeadUnits();

        currentTurn++;

        return new TurnReport(currentTurn - 1, report, starving, unitProduced, destroyed);
    }


    private void applyStarvationPenalty(Empire empire) {
        for (Unit unit : empire.getUnits()) {
            if (unit.isAlive()) {
                unit.applyStarvationPenalty();
            }
        }

    }


    private List<Building> trackNeglect(Empire empire) {
        List<Building> destroyed = new ArrayList<>();

        for (Building b : new ArrayList<>(empire.getBuildings())) {
            int cost = b.getUpkeepCost();
            if (cost == 0) {
                unpaidUpkeepStreak.remove(b);
                continue;
            }

            boolean hadEnough = empire.getStorage().get(b.getUpkeepResourceType()) >= 0;


            if (empire.getStorage().get(b.getUpkeepResourceType()) < 0) {
                int streak = unpaidUpkeepStreak.merge(b, 1, Integer::sum);
                if (streak >= NEGLECT_LIMIT) {
                    empire.removeBuilding(b);
                    destroyed.add(b);
                    unpaidUpkeepStreak.remove(b);
                }
            } else {
                unpaidUpkeepStreak.remove(b);
            }
        }
        return destroyed;
    }


    public List<Unit> getIdleUnitsWithAP(Empire empire) {
        List<Unit> idle = new ArrayList<>();
        for (Unit u : empire.getUnits()) {
            if (u.isAlive() && u.getAP() > 0) {
                idle.add(u);
            }
        }
        return idle;
    }


    public int getCurrentTurn() { return currentTurn; }
}