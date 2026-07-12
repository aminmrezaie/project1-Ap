package model.buildings;

import model.enums.BuildingType;
import model.enums.Position;
import model.enums.ResourceType;

import java.util.ArrayDeque;
import java.util.Deque;

public class TownHall extends Building {


    private int storageCapacity;
    private static final int BASE_STORAGE = 100;


    private int unitCap;
    private static final int BASE_UNIT_CAP = 5;

    private final Deque<String> productionQueue;
    private int turnsRemainingForCurrent;


    private static final int SAFEGUARD_FOOD = 1;
    private static final int SAFEGUARD_WOOD = 1;


    public TownHall(Position position) {
        super(BuildingType.TOWN_HALL, position);
        this.storageCapacity = BASE_STORAGE;
        this.unitCap = BASE_UNIT_CAP;
        this.productionQueue = new ArrayDeque<>();
        this.turnsRemainingForCurrent = 0;
    }


    @Override
    public int getMaxWorkers() {
        return 0;
    }

    @Override
    public int getUpkeepCost() {
        return 0;
    }

    @Override
    public int getProduction() {
        return 0;
    }

    @Override
    public ResourceType getProducedResource() {
        return null;
    }


    public int getSafeguardFood() {
        return SAFEGUARD_FOOD;
    }

    public int getSafeguardWood() {
        return SAFEGUARD_WOOD;
    }


    public int getStorageCapacity() {
        return storageCapacity;
    }

    public void upgradeStorage(int additionalCapacity) {
        if (additionalCapacity <= 0) throw new IllegalArgumentException("Must be > 0");
        this.storageCapacity += additionalCapacity;
    }


    public int getUnitCap() {
        return unitCap;
    }

    public void increaseUnitCap(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Must be > 0");
        this.unitCap += amount;
    }


    public void enqueueUnit(String unitTypeName) {
        productionQueue.addLast(unitTypeName);
    }

    public String currentProduction() {
        return productionQueue.peekFirst();
    }

    public String tickProduction() {
        if (productionQueue.isEmpty()) return null;
        if (turnsRemainingForCurrent <= 0) {
            turnsRemainingForCurrent = getProductionCostForNext();
        }
        turnsRemainingForCurrent--;
        if (turnsRemainingForCurrent <= 0) {
            return productionQueue.pollFirst();
        }
        return null;
    }

    public int getTurnsRemainingForCurrent() {
        return turnsRemainingForCurrent;
    }

    public boolean isQueueEmpty() {
        return productionQueue.isEmpty();
    }

    public int getQueueSize() {
        return productionQueue.size();
    }

    private int getProductionCostForNext() {
        return 3;
    }

    @Override
    public String toString() {
        return "TownHall@" + getPosition()
                + "[unitCap=" + unitCap
                + ", storage=" + storageCapacity
                + ", queue=" + productionQueue.size() + "]";
    }
}
