package model.enums;

public enum UnitType {
    EXPLORER("Explorer"),
    BUILDER("Builder"),
    WORKER("Worker"),
    BORDER_EXPANDER("Border Expander");

    private final String displayName;

    UnitType(String displayName) {
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
