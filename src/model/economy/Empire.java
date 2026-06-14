package model.economy;

import model.buildings.Building;
import model.buildings.TownHall;
import model.enums.Position;
import model.enums.ResourceType;
import model.unit.Unit;

import java.util.*;

public class Empire {

    private final TownHall townHall;
    private final ResourceStorage storage;

    private final List<Unit> units;
    private final List<Building> buildings;
    private final Set<Position> territory;

    public Empire(TownHall townHall, int initialStorageCapacity) {
        this.townHall = townHall;
        this.storage = new ResourceStorage(initialStorageCapacity);
        this.units = new ArrayList<>();
        this.buildings = new ArrayList<>();
        this.territory = new HashSet<>();

        buildings.add(townHall);
        territory.add(townHall.getPosition());
    }

    public ResourceStorage getStorage() {
        return storage;
    }

    public boolean canAfford(ResourceType type, int amount) {
        return storage.canAfford(type, amount);
    }

    public boolean spend(ResourceType type, int amount) {
        return storage.consume(type, amount);
    }

    public void addResource(ResourceType type, int amount) {
        storage.add(type, amount);
    }

    public boolean isStarving() {
        return storage.isStarving();
    }

    public List<Unit> getUnits() {
        return Collections.unmodifiableList(units);
    }

    public int getUnitCount() {
        return units.size();
    }

    public int getUnitCap() {
        return townHall.getUnitCap();
    }

    public boolean isAtUnitCap() {
        return units.size() >= townHall.getUnitCap();
    }

    public boolean addUnit(Unit unit) {
        if (isAtUnitCap()) return false;
        units.add(unit);
        return true;
    }

    public void removeDeadUnits() {
        units.removeIf(u -> !u.isAlive());
    }

    public void removeUnit(Unit unit) {
        units.remove(unit);
    }

    public List<Building> getBuildings() {
        return Collections.unmodifiableList(buildings);
    }

    public void addBuilding(Building building) {
        buildings.add(building);
    }

    public void removeBuilding(Building building) {
        buildings.remove(building);
    }

    public TownHall getTownHall() {
        return townHall;
    }

    public Set<Position> getTerritory() {
        return Collections.unmodifiableSet(territory);
    }

    public boolean isInTerritory(Position pos) {
        return territory.contains(pos);
    }

    public void addToTerritory(Position pos) {
        territory.add(pos);
    }

    public void addAllToTerritory(List<Position> positions) {
        territory.addAll(positions);
    }

    public void refreshAllUnitAP() {
        units.stream().filter(Unit::isAlive).forEach(Unit::refreshAP);
    }

    @Override
    public String toString() {
        return "Empire[units=" + units.size() + "/" + getUnitCap()
                + ", buildings=" + buildings.size()
                + ", territory=" + territory.size() + " hexes"
                + ", " + storage + "]";
    }
}
