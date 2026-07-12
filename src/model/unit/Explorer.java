package model.unit;

import model.enums.Position;
import model.enums.UnitType;

public class Explorer extends Unit {


    private static final int MAX_AP        = 6;
    private static final int VISION_RADIUS = 3;  // largest vision of all units


    private boolean autoExploreEnabled;


    public Explorer(Position position) {
        super(UnitType.EXPLORER, position, MAX_AP, VISION_RADIUS);
        this.autoExploreEnabled = false;
    }


    public boolean isAutoExploreEnabled() { return autoExploreEnabled; }

    public void setAutoExplore(boolean enabled) {
        this.autoExploreEnabled = enabled;
    }

    public void toggleAutoExplore() {
        this.autoExploreEnabled = !this.autoExploreEnabled;
    }

    @Override
    public String toString() {
        return super.toString() + "[autoExplore=" + autoExploreEnabled + "]";
    }
}
