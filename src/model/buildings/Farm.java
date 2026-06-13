package model.buildings;

import model.enums.BuildingType;
import model.enums.Position;
import model.enums.ResourceType;

public class Farm extends ProductionBuilding {
    private static final int DEFAULT_RATE = 3;
    private static final int MAX_WORKERS  = 4;
    private static final int UPKEEP_COST  = 1;

    public Farm(Position position) {
        super(BuildingType.FARM, position, ResourceType.FOOD, DEFAULT_RATE);
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
