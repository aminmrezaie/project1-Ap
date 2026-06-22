package controller;

import model.map.GameMap;
import model.map.Hex;
import model.enums.Position;
import model.unit.Unit;
import model.unit.Worker;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class MovementSystem {

    private final FogOfWarSystem fogSystem;

    public MovementSystem(FogOfWarSystem fogSystem) {
        this.fogSystem = fogSystem;
    }


    public enum MoveResult {
        OK,
        OUT_OF_BOUNDS,
        NOT_ADJACENT,
        NOT_ENOUGH_AP,
        WORKER_STATIONED,
        UNIT_DEAD
    }


    public MoveResult validate(Unit unit, Position dest, GameMap map) {
        if (!unit.isAlive()) return MoveResult.UNIT_DEAD;
        if (unit instanceof Worker w && w.isStationed()) return MoveResult.WORKER_STATIONED;
        if (!map.inBounds(dest)) return MoveResult.OUT_OF_BOUNDS;
        if (!isAdjacent(unit.getPosition(), dest)) return MoveResult.NOT_ADJACENT;

        int cost = getMoveCost(map, dest);
        if (!unit.hasAP(cost)) return MoveResult.NOT_ENOUGH_AP;

        return MoveResult.OK;
    }


    public boolean executeMove(Unit unit, Position dest, GameMap map) {
        if (validate(unit, dest, map) != MoveResult.OK) return false;

        int cost = getMoveCost(map, dest);
        boolean moved = unit.move(dest, cost);

        if (moved) {
            fogSystem.reveal(map, dest, unit.getVisionRadius());
        }
        return moved;
    }


    public List<Position> getReachablePositions(Unit unit, GameMap map) {
        List<Position> result = new ArrayList<>();
        if (!unit.isAlive()) return result;
        if (unit instanceof Worker w && w.isStationed()) return result;

        for (Position neighbor : unit.getPosition().neighbors()) {
            if (!map.inBounds(neighbor)) continue;
            int cost = getMoveCost(map, neighbor);
            if (unit.hasAP(cost)) result.add(neighbor);
        }
        return result;
    }


    public int getMoveCost(GameMap map, Position dest) {
        return map.getHex(dest)
                .map(h -> h.getTerrain().getMovementCost())
                .orElse(1);
    }

    private boolean isAdjacent(Position from, Position to) {
        return from.distanceTo(to) == 1;
    }
}