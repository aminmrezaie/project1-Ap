package model.buildings;

import model.enums.BuildingType;
import model.enums.Position;
import model.enums.ResourceType;

public class StoneMine extends ProductionBuilding {

    private static final int DEFAULT_RATE = 2;
    private static final int MAX_WORKERS  = 3;
    private static final int UPKEEP_COST  = 1;

    public StoneMine(Position position) {
        super(BuildingType.STONE_MINE, position, ResourceType.STONE, DEFAULT_RATE);
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
