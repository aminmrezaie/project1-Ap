package model.buildings;

import model.enums.BuildingType;
import model.enums.Position;
import model.enums.ResourceType;


public class Stable extends ProductionBuilding {

    private static final int DEFAULT_RATE = 2;
    private static final int MAX_WORKERS  = 2;
    private static final int UPKEEP_COST  = 1;

    public Stable(Position position) {
        super(BuildingType.STABLE, position, ResourceType.FOOD, DEFAULT_RATE);
    }

    @Override
    public int getMaxWorkers() {
        return MAX_WORKERS;
    }

    @Override
    public int getUpkeepCost() {
        return UPKEEP_COST;
    }

    @Override
    public ResourceType getUpkeepResourceType() {
        return ResourceType.WOOD;
    }
}
