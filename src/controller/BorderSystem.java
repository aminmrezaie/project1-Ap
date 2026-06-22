package controller;

import model.economy.Empire;
import model.enums.Position;
import model.map.GameMap;
import model.map.Hex;
import model.unit.BorderExpander;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class BorderSystem {

    public enum ExpandResult {
        OK,
        NOT_ENOUGH_AP,
        HEX_NOT_EXPLORED,
        UNIT_DEAD
    }


    public ExpandResult validate(BorderExpander expander,
                                 Position targetCenter, GameMap map) {
        if (!expander.isAlive()) return ExpandResult.UNIT_DEAD;
        if (!expander.hasAP(2)) return ExpandResult.NOT_ENOUGH_AP;

        for (Position pos : targetCenter.positionsInRadius(1)) {
            Optional<Hex> hex = map.getHex(pos);
            if (hex.isEmpty() || !hex.get().isExplored()) {
                return ExpandResult.HEX_NOT_EXPLORED;
            }
        }
        return ExpandResult.OK;
    }


    public List<Position> executeExpand(BorderExpander expander,
                                        Position targetCenter,
                                        GameMap map, Empire empire) {
        if (validate(expander, targetCenter, map) != ExpandResult.OK) return null;

        List<Position> cluster = expander.expand(targetCenter);
        if (cluster == null) return null;

        List<Position> added = new ArrayList<>();
        for (Position pos : cluster) {
            map.getHex(pos).ifPresent(h -> {
                h.setOwned(true);
                empire.addToTerritory(pos);
                added.add(pos);
            });
        }

        empire.removeDeadUnits();
        return added;
    }


    public List<Position> previewExpand(Position targetCenter, GameMap map) {
        List<Position> result = new ArrayList<>();
        for (Position pos : targetCenter.positionsInRadius(1)) {
            if (map.inBounds(pos)) result.add(pos);
        }
        return result;
    }
}