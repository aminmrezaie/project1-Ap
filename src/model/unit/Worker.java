package model.unit;

import model.buildings.Building;
import model.enums.Position;
import model.enums.UnitType;

public class Worker extends Unit {

    private static final int MAX_AP = 4;
    private static final int VISION_RADIUS = 1;
    private static final int STATION_AP_COST = 1;
    private Building assignedBuilding;

    public Worker(Position position) {
        super(UnitType.WORKER, position, MAX_AP, VISION_RADIUS);
        this.assignedBuilding = null;
    }

    public boolean isStationed() {
        return assignedBuilding != null;
    }

    public Building getAssignedBuilding() {
        return assignedBuilding;
    }

    public boolean station(Building building) {
        if (isStationed()) return false;
        if (building.isFull()) return false;
        if (!spendAP(STATION_AP_COST)) return false;

        building.addWorker(this);
        this.assignedBuilding = building;
        return true;
    }

    public void unstation() {
        if (assignedBuilding == null) return;
        assignedBuilding.removeWorker(this);
        assignedBuilding = null;
    }

    @Override
    public String toString() {
        String buildingInfo = assignedBuilding != null
                ? "[stationed@" + assignedBuilding.getType().getDisplayName() + "]"
                : "[idle]";
        return super.toString() + buildingInfo;
    }
}
