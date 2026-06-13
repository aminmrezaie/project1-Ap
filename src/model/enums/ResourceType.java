package model.enums;

public enum ResourceType {
    FOOD("Food"),
    WOOD("Wood"),
    STONE("Stone"),
    IRON("Iron");

    private final String displayName;

    ResourceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
