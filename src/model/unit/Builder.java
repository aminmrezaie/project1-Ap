package model.unit;

import model.enums.BuildingType;
import model.enums.Position;
import model.enums.UnitType;

public class Builder extends Unit {



    private static final int MAX_AP         = 4;
    private static final int VISION_RADIUS  = 2;
    private static final int DEFAULT_CHARGES = 3;


    private int charges;



    public Builder(Position position) {
        this(position, DEFAULT_CHARGES);
    }

    public Builder(Position position, int charges) {
        super(UnitType.BUILDER, position, MAX_AP, VISION_RADIUS);
        if (charges <= 0) throw new IllegalArgumentException("Charges must be > 0");
        this.charges = charges;
    }


    public int getCharges() { return charges; }

    public boolean hasCharges() { return charges > 0; }


    public boolean build(BuildingType buildingType) {
        int apCost = buildingType.getBuildApCost();
        if (!hasCharges()) return false;
        if (!spendAP(apCost)) return false;

        charges--;
        if (charges == 0) consume();
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + "[charges=" + charges + "]";
    }
}
