package model.enums;

public enum TerrainType {
    PLAIN(1, "Plain"),
    FOREST(2, "Forest"),
    MOUNTAIN(4, "Mountain"),
    FARMLAND(1, "Farmland");

    private final int movementCost;
    private final String displayName;

    TerrainType(int movementCost, String displayName) {
        this.movementCost = movementCost;
        this.displayName = displayName;
    }

    public int getMovementCost() {
        return movementCost;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
