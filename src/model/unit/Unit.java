package model.unit;

import model.enums.Position;
import model.enums.UnitType;

public abstract class Unit {

    private static int nextId = 1;

    private final int id;
    private final UnitType type;
    private Position position;

    private int ap;
    private final int maxAP;
    private final int visionRadius;

    private boolean alive;

    protected Unit(UnitType type, Position position, int maxAP, int visionRadius) {
        this.id           = nextId++;
        this.type         = type;
        this.position     = position;
        this.maxAP        = maxAP;
        this.ap           = maxAP;
        this.visionRadius = visionRadius;
        this.alive        = true;
    }


    public static void resetIdCounter() { nextId = 1; }



    public int getId()       { return id; }
    public UnitType getType(){ return type; }
    public boolean isAlive() { return alive; }


    public Position getPosition() { return position; }

    public void setPosition(Position position) {
        if (position == null) throw new IllegalArgumentException("Position cannot be null");
        this.position = position;
    }


    public int getAP()    { return ap; }
    public int getMaxAP() { return maxAP; }

    public void refreshAP() { this.ap = maxAP; }

    public boolean spendAP(int amount) {
        if (amount < 0) throw new IllegalArgumentException("AP cost must be >= 0");
        if (ap < amount) return false;
        ap -= amount;
        return true;
    }

    public boolean hasAP(int amount) { return ap >= amount; }

    public void skipTurn() { this.ap = 0; }


    public void applyStarvationPenalty() {
        this.ap = ap / 2;
    }



    public int getVisionRadius() { return visionRadius; }

    public boolean move(Position dest, int apCost) {
        if (!spendAP(apCost)) return false;
        setPosition(dest);
        return true;
    }

    public void consume() { this.alive = false; }

    @Override
    public String toString() {
        return type.getDisplayName() + "#" + id
                + "@" + position + "[AP=" + ap + "/" + maxAP + "]";
    }
}
