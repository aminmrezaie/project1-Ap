package model.buildings;

import model.enums.BuildingType;
import model.enums.Position;
import model.enums.ResourceType;
import model.unit.Worker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Building {

    private final BuildingType type;
    private final Position position;

    private final List<Worker> workers;

    public Building(BuildingType type, Position position) {
        this.type = type;
        this.position = position;
        this.workers = new ArrayList<>();
    }


    public BuildingType getType() {
        return type;
    }

    public Position getPosition() {
        return position;
    }


    public abstract int getMaxWorkers();

    public int getWorkerCount() {
        return workers.size();
    }

    public boolean isFull() {
        return workers.size() >= getMaxWorkers();
    }

    public boolean hasWorkers() {
        return !workers.isEmpty();
    }

    public boolean addWorker(Worker worker) {
        if (isFull()) return false;
        workers.add(worker);
        return true;
    }

    public boolean removeWorker(Worker worker) {
        return workers.remove(worker);
    }

    public List<Worker> getWorkers() {
        return Collections.unmodifiableList(workers);
    }


    public ResourceType getUpkeepResourceType() {
        return ResourceType.WOOD;
    }


    public abstract int getUpkeepCost();


    public abstract int getProduction();

    public abstract ResourceType getProducedResource();

    public boolean isActive() {
        return hasWorkers();
    }

    @Override
    public String toString() {
        return type.getDisplayName() + "@" + position
                + "[workers=" + workers.size() + "/" + getMaxWorkers() + "]";
    }
}
