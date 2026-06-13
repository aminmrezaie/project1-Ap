package model.buildings;

import model.enums.BuildingType;
import model.enums.Position;
import model.enums.ResourceType;

public abstract class ProductionBuilding extends Building {
    private final ResourceType producedResource;
    private int baseProductionRate;

    public ProductionBuilding(BuildingType type,
                              Position position,
                              ResourceType producedResource,
                              int baseProductionRate) {
        super(type, position);
        this.producedResource = producedResource;
        this.baseProductionRate = baseProductionRate;
    }

    @Override
    public ResourceType getProducedResource() {
        return producedResource;
    }

    public int getBaseProductionRate() {
        return baseProductionRate;
    }

    public void setBaseProductionRate(int baseProductionRate) {
        if (baseProductionRate < 0) throw new IllegalArgumentException("Rate must be >= 0");
        this.baseProductionRate = baseProductionRate;
    }

    @Override
    public int getProduction() {
        return getWorkerCount() * baseProductionRate;
    }

    @Override
    public String toString() {
        return super.toString()
                + " produces=" + producedResource
                + " rate=" + baseProductionRate + "/worker/turn";
    }
}
