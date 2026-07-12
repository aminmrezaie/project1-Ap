package controller;

import model.map.GameMap;
import model.map.Hex;
import model.enums.Position;
import model.unit.Unit;
import model.buildings.Building;

import java.util.Collection;

public class FogOfWarSystem {

    private static final int BUILDING_VISION_RADIUS = 2;


    public void updateVisibility(GameMap map,
                                 Collection<Unit> units,
                                 Collection<Building> buildings) {
        for (Hex hex : map.getAllHexes()) {
            hex.setVisible(false);
        }

        for (Unit unit : units) {
            if (unit.isAlive()) {
                reveal(map, unit.getPosition(), unit.getVisionRadius());
            }
        }

        for (Building building : buildings) {
            reveal(map, building.getPosition(), BUILDING_VISION_RADIUS);
        }
    }


    public void reveal(GameMap map, Position center, int radius) {
        for (Position pos : center.positionsInRadius(radius)) {
            map.getHex(pos).ifPresent(h -> h.setVisible(true));
        }
    }


    public boolean isVisible(GameMap map, Position pos) {
        return map.getHex(pos).map(Hex::isVisible).orElse(false);
    }

    public boolean isExplored(GameMap map, Position pos) {
        return map.getHex(pos).map(Hex::isExplored).orElse(false);
    }
}
