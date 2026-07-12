package controller;

import model.enums.Position;
import model.map.GameMap;
import model.map.Hex;
import model.unit.Explorer;

import java.util.*;

public class AutoExploreAI {

    private final MovementSystem movementSystem;

    public AutoExploreAI(MovementSystem movementSystem) {
        this.movementSystem = movementSystem;
    }


    public boolean step(Explorer explorer, GameMap map) {
        if (!explorer.isAlive() || !explorer.isAutoExploreEnabled()) return false;
        if (!explorer.hasAP(1)) return false;

        Position target = findNearestFrontier(explorer.getPosition(), map);
        if (target == null) {
            explorer.setAutoExplore(false);
            return false;
        }

        Position nextStep = stepToward(explorer.getPosition(), target, map);
        if (nextStep == null) return false;

        return movementSystem.executeMove(explorer, nextStep, map);
    }


    public void runFullTurn(Explorer explorer, GameMap map) {
        while (explorer.hasAP(1) && explorer.isAutoExploreEnabled()) {
            if (!step(explorer, map)) break;
        }
    }

    private Position findNearestFrontier(Position start, GameMap map) {
        Queue<Position> queue   = new LinkedList<>();
        Set<Position>   visited = new HashSet<>();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Position current = queue.poll();

            Optional<Hex> hexOpt = map.getHex(current);
            if (hexOpt.isPresent() && !hexOpt.get().isExplored()) {
                return current;
            }

            for (Position neighbor : current.neighbors()) {
                if (visited.contains(neighbor)) continue;
                if (!map.inBounds(neighbor)) continue;
                visited.add(neighbor);

                map.getHex(neighbor).ifPresent(h -> {
                    if (h.isExplored()) queue.add(neighbor);
                });

                map.getHex(neighbor).ifPresent(h -> {
                    if (!h.isExplored()) queue.add(neighbor);
                });
            }
        }
        return null;
    }


    private Position stepToward(Position from, Position target, GameMap map) {
        Map<Position, Position> parent = new HashMap<>();
        Queue<Position> queue = new LinkedList<>();
        queue.add(from);
        parent.put(from, null);

        while (!queue.isEmpty()) {
            Position current = queue.poll();
            if (current.equals(target)) break;

            for (Position neighbor : current.neighbors()) {
                if (parent.containsKey(neighbor)) continue;
                if (!map.inBounds(neighbor)) continue;

                boolean canPass = neighbor.equals(target) ||
                        map.getHex(neighbor).map(Hex::isExplored).orElse(false);
                if (!canPass) continue;

                int cost = movementSystem.getMoveCost(map, neighbor);
                if (!map.getHex(neighbor).isPresent()) continue;

                parent.put(neighbor, current);
                queue.add(neighbor);
            }
        }

        if (!parent.containsKey(target)) return null;

        Position step = target;
        while (parent.get(step) != null && !parent.get(step).equals(from)) {
            step = parent.get(step);
        }
        return parent.get(step) != null ? step : null;
    }

    private Optional<Hex> getHex(GameMap map, Position pos) {
        return map.getHex(pos);
    }
}
