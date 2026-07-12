package model.buildings;

import model.enums.BuildingType;
import model.enums.Position;
import model.enums.ResourceType;


public class Town extends Building {

    public static final int UNIT_CAP_BONUS = 5;

    private static final int MAX_WORKERS = 0;
    private static final int UPKEEP_STONE = 1;
    private static final int UPKEEP_WOOD = 1;

    public Town(Position position) {
        super(BuildingType.TOWN, position);
    }

    @Override
    public int getMaxWorkers() {
        return MAX_WORKERS;
    }

    @Override
    public int getUpkeepCost() {
        return UPKEEP_STONE + UPKEEP_WOOD;
    }

    @Override
    public ResourceType getUpkeepResourceType() {
        return ResourceType.STONE;
    }

    @Override
    public int getProduction() {
        return 0;
    }

    @Override
    public ResourceType getProducedResource() {
        return null;
    }

    public int getWoodUpkeepCost() {
        return UPKEEP_WOOD;
    }

    public int getStoneUpkeepCost() {
        return UPKEEP_STONE;
    }
}
