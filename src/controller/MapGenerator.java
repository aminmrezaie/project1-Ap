package controller;

import model.enums.Position;
import model.enums.ResourceType;
import model.enums.TerrainType;
import model.map.GameMap;
import model.map.Hex;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MapGenerator {


    private static final double MOUNTAIN_AREA_RATIO = 0.18;
    private static final double FOREST_AREA_RATIO    = 0.28;
    private static final double FARMLAND_AREA_RATIO  = 0.18;

    private static final int MOUNTAIN_CLUSTER_COUNT = 3;
    private static final int FOREST_CLUSTER_COUNT    = 4;
    private static final int FARMLAND_CLUSTER_COUNT  = 3;

    private static final int FOREST_MAX_DISTANCE_FROM_ORIGIN = 2;


    private static final double FOREST_WOOD_CHANCE     = 1.0;
    private static final double MOUNTAIN_STONE_CHANCE  = 0.55;
    private static final double MOUNTAIN_IRON_CHANCE   = 0.30;
    private static final double FARMLAND_FOOD_CHANCE   = 1.0;
    private static final double PLAIN_LIVESTOCK_CHANCE = 0.25;

    private final Random random;

    public MapGenerator(long seed) { this.random = new Random(seed); }
    public MapGenerator()          { this.random = new Random(); }


    public GameMap generate(int radius) {
        int diameter = radius * 2 + 1;
        GameMap map  = new GameMap(diameter, diameter);

        List<Position> allPositions = new Position(0, 0).positionsInRadius(radius);

        Map<Position, TerrainType> terrainMap = new HashMap<>();
        for (Position p : allPositions) terrainMap.put(p, TerrainType.PLAIN);

        Position origin = new Position(0, 0);


        growClusters(terrainMap, allPositions, origin, radius,
                TerrainType.MOUNTAIN, MOUNTAIN_CLUSTER_COUNT,
                (int)(allPositions.size() * MOUNTAIN_AREA_RATIO), 3);

        growForestNearOrigin(terrainMap, origin);

        growClusters(terrainMap, allPositions, origin, radius,
                TerrainType.FOREST, FOREST_CLUSTER_COUNT - 1,
                (int)(allPositions.size() * FOREST_AREA_RATIO), 0);

        growClusters(terrainMap, allPositions, origin, radius,
                TerrainType.FARMLAND, FARMLAND_CLUSTER_COUNT,
                (int)(allPositions.size() * FARMLAND_AREA_RATIO), 0);


        for (Position p : allPositions) {
            map.addHex(new Hex(p, terrainMap.get(p), null));
        }

        assignResources(map, allPositions);

        revealAroundOrigin(map, 2);

        return map;
    }


    private void growClusters(Map<Position, TerrainType> terrainMap,
                              List<Position> allPositions,
                              Position origin, int mapRadius,
                              TerrainType terrain, int clusterCount,
                              int totalTargetTiles, int minDistanceFromOrigin) {

        if (clusterCount <= 0 || totalTargetTiles <= 0) return;

        int tilesPerCluster = Math.max(1, totalTargetTiles / clusterCount);

        List<Position> candidates = new ArrayList<>(allPositions);
        Collections.shuffle(candidates, random);

        int clustersPlaced = 0;
        for (Position seed : candidates) {
            if (clustersPlaced >= clusterCount) break;
            if (terrainMap.get(seed) != TerrainType.PLAIN) continue;
            if (seed.distanceTo(origin) < minDistanceFromOrigin) continue;
            if (seed.equals(origin)) continue;

            growSingleCluster(terrainMap, seed, terrain, tilesPerCluster, mapRadius, origin);
            clustersPlaced++;
        }
    }

    private void growSingleCluster(Map<Position, TerrainType> terrainMap,
                                   Position seed, TerrainType terrain,
                                   int targetSize, int mapRadius, Position origin) {

        Deque<Position> frontier = new ArrayDeque<>();
        frontier.add(seed);
        terrainMap.put(seed, terrain);
        int placed = 1;

        while (!frontier.isEmpty() && placed < targetSize) {
            Position current = frontier.poll();

            List<Position> neighbors = new ArrayList<>(current.neighbors());
            Collections.shuffle(neighbors, random);

            for (Position n : neighbors) {
                if (placed >= targetSize) break;
                if (n.distanceTo(origin) > mapRadius) continue;
                if (!terrainMap.containsKey(n)) continue;
                if (terrainMap.get(n) != TerrainType.PLAIN) continue;
                if (n.equals(origin)) continue;

                if (random.nextDouble() < 0.75) {
                    terrainMap.put(n, terrain);
                    frontier.add(n);
                    placed++;
                }
            }
        }
    }


    private void growForestNearOrigin(Map<Position, TerrainType> terrainMap, Position origin) {
        List<Position> nearby = origin.positionsInRadius(FOREST_MAX_DISTANCE_FROM_ORIGIN);
        Collections.shuffle(nearby, random);

        Position seed = nearby.stream()
                .filter(p -> !p.equals(origin))
                .filter(p -> terrainMap.get(p) == TerrainType.PLAIN)
                .findFirst()
                .orElse(null);

        if (seed == null) return;

        growSingleCluster(terrainMap, seed, TerrainType.FOREST, 5, FOREST_MAX_DISTANCE_FROM_ORIGIN + 1, origin);
    }


    private void assignResources(GameMap map, List<Position> positions) {
        for (Position pos : positions) {
            map.getHex(pos).ifPresent(hex -> {
                ResourceType resource = pickResource(hex.getTerrain());
                if (resource != null) {
                    map.addHex(new Hex(pos, hex.getTerrain(), resource));
                }
            });
        }
    }

    private ResourceType pickResource(TerrainType terrain) {
        return switch (terrain) {
            case FOREST   -> random.nextDouble() < FOREST_WOOD_CHANCE     ? ResourceType.WOOD  : null;
            case MOUNTAIN -> {
                double r = random.nextDouble();
                if (r < MOUNTAIN_STONE_CHANCE)                            yield ResourceType.STONE;
                if (r < MOUNTAIN_STONE_CHANCE + MOUNTAIN_IRON_CHANCE)    yield ResourceType.IRON;
                yield null;
            }
            case FARMLAND -> random.nextDouble() < FARMLAND_FOOD_CHANCE   ? ResourceType.FOOD  : null;
            case PLAIN    -> random.nextDouble() < PLAIN_LIVESTOCK_CHANCE ? ResourceType.FOOD  : null;
        };
    }


    private void revealAroundOrigin(GameMap map, int radius) {
        Position origin = new Position(0, 0);
        for (Position pos : origin.positionsInRadius(radius)) {
            map.getHex(pos).ifPresent(h -> {
                h.setVisible(true);
                h.setOwned(true);
            });
        }
    }
}