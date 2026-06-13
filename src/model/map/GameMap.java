package model.map;

import model.enums.Position;
import model.enums.TerrainType;

import java.util.*;
import java.util.stream.Collectors;

public class GameMap {

    private final Map<Position, Hex> hexes;
    private final int width;
    private final int height;

    public GameMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.hexes = new HashMap<>();
    }

    public void addHex(Hex hex) {
        hexes.put(hex.getPosition(), hex);
    }

    public Optional<Hex> getHex(Position pos) {
        return Optional.ofNullable(hexes.get(pos));
    }

    public Optional<Hex> getHex(int q, int r) {
        return getHex(new Position(q, r));
    }

    public boolean isOwned(Position pos) {
        return getHex(pos).map(Hex::isOwned).orElse(false);
    }

    public boolean isBounds(Position pos) {
        return hexes.containsKey(pos);
    }

    public List<Hex> getNeighbors(Position pos) {
        return pos.neighbors().stream()
                .map(hexes::get)
                .filter(h -> h != null)
                .collect(Collectors.toList());
    }

    public List<Hex> getHexesInRadius(Position center, int radius) {
        return center.positionsInRadius(radius).stream()
                .map(hexes::get)
                .filter(h -> h != null)
                .collect(Collectors.toList());
    }

    public List<Hex> getVisibleHexes() {
        return hexes.values().stream()
                .filter(Hex::isVisible)
                .collect(Collectors.toList());
    }

    public List<Hex> getExploredHexes() {
        return hexes.values().stream()
                .filter(Hex::isExplored)
                .collect(Collectors.toList());
    }

    public List<Hex> getOwnedHexes() {
        return hexes.values().stream()
                .filter(Hex::isOwned)
                .collect(Collectors.toList());
    }

    public List<Hex> getHexesWithBuildings() {
        return hexes.values().stream()
                .filter(Hex::hasBuilding)
                .collect(Collectors.toList());
    }

    public List<Hex> getHexesByTerrain(TerrainType terrain) {
        return hexes.values().stream()
                .filter(h -> h.getTerrain() == terrain)
                .collect(Collectors.toList());
    }

    public Collection<Hex> getAllHexes() {
        return Collections.unmodifiableCollection(hexes.values());
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int size() {
        return hexes.size();
    }

    @Override
    public String toString() {
        return "GameMap[" + width + "x" + height + ", hexes=" + hexes.size() + "]";
    }
}
