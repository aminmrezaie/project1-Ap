package model.enums;

public enum BuildingType {
    TOWN_HALL("Town Hall", 0),
    LUMBER_MILL("Lumber Mill", 1),
    STONE_MINE("Stone Mine", 1),
    IRON_MINE("Iron Mine", 2),
    FARM("Farm", 1),
    STABLE("Stable", 1),
    TOWN("Town", 2),
    VILLAGE("Village", 1);

    private final String displayName;
    private final int buildApCost;

    BuildingType(String displayName, int buildApCost) {
        this.displayName = displayName;
        this.buildApCost = buildApCost;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getBuildApCost() {
        return buildApCost;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
