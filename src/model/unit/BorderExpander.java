package model.unit;

import model.enums.Position;
import model.enums.UnitType;

import java.util.List;


public class BorderExpander extends Unit {


    private static final int MAX_AP        = 3;
    private static final int VISION_RADIUS = 2;
    private static final int EXPAND_AP_COST = 2;


    public BorderExpander(Position position) {
        super(UnitType.BORDER_EXPANDER, position, MAX_AP, VISION_RADIUS);
    }

    public List<Position> expand(Position targetCenter) {
        if (!spendAP(EXPAND_AP_COST)) return null;

        List<Position> cluster = targetCenter.positionsInRadius(1);

        consume();
        return cluster;
    }
}
