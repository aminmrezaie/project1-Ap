package model.buildings;

import model.enums.BuildingType;
import model.enums.Position;
import model.enums.ResourceType;

public class Village extends Building {

    public static final int UNIT_CAP_BONUS = 2;

    private static final int MAX_WORKERS = 0;
    private static final int UPKEEP_COST = 1;

    public Village(Position position) {
        super(BuildingType.VILLAGE, position);
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

    @Override
    public int getProduction() {
        return 0;
    }

    @Override
    public ResourceType getProducedResource() {
        return null;
    }
}
