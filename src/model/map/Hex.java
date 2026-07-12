package model.map;

import model.buildings.Building;
import model.enums.Position;
import model.enums.ResourceType;
import model.enums.TerrainType;


public class Hex {

    private final Position position;
    private final TerrainType terrain;

    private ResourceType resource;

    private Building building;

    private boolean visible;

    private boolean explored;

    private boolean owned;

    private boolean resourceDepleted;

    public Hex(Position position, TerrainType terrain, ResourceType resource) {
        this.position = position;
        this.terrain = terrain;
        this.resource = resource;
        this.visible = false;
        this.explored = false;
        this.owned = false;
        this.resourceDepleted = false;
    }

    public Hex(Position position, TerrainType terrain) {
        this(position, terrain, null);
    }


    public Position getPosition() {
        return position;
    }

    public TerrainType getTerrain() {
        return terrain;
    }

    public ResourceType getResource() {
        return resource;
    }

    public boolean hasResource() {
        return resource != null && !resourceDepleted;
    }

    public Building getBuilding() {
        return building;
    }

    public boolean hasBuilding() {
        return building != null;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean isExplored() {
        return explored;
    }

    public boolean isOwned() {
        return owned;
    }

    public boolean isResourceDepleted() {
        return resourceDepleted;
    }


    public void setBuilding(Building building) {
        if (this.building != null) {
            throw new IllegalStateException(
                    "Hex " + position + " already has a building: " + this.building.getType());
        }
        this.building = building;
    }

    public void removeBuilding() {
        this.building = null;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        if (visible) this.explored = true;
    }

    public void setExplored(boolean explored) {
        this.explored = explored;
    }

    public void setOwned(boolean owned) {
        this.owned = owned;
    }

    public void depleteResource() {
        this.resourceDepleted = true;
    }

    public int getMovementCost() {
        return terrain.getMovementCost();
    }

    @Override
    public String toString() {
        return "Hex[" + position + ", terrain=" + terrain
                + ", resource=" + (resource != null ? resource : "none")
                + ", building=" + (building != null ? building.getType() : "none")
                + ", visible=" + visible + ", owned=" + owned + "]";
    }
}
