package controller;

import model.enums.Position;
import model.enums.ResourceType;
import model.enums.TerrainType;
import model.map.GameMap;
import model.map.Hex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


public class MapGenerator {


    private static final double RATIO_PLAIN = 0.35;
    private static final double RATIO_FOREST = 0.30;
    private static final double RATIO_MOUNTAIN = 0.20;
    private static final double RATIO_FARMLAND = 0.15;


    private static final double FOREST_WOOD_CHANCE = 1.0;
    private static final double MOUNTAIN_STONE_CHANCE = 0.6;
    private static final double MOUNTAIN_IRON_CHANCE = 0.4;
    private static final double FARMLAND_FOOD_CHANCE = 1.0;
    private static final double PLAIN_LIVESTOCK_CHANCE = 0.3;

    private final Random random;

    public MapGenerator(long seed) {
        this.random = new Random(seed);
    }

    public MapGenerator() {
        this.random = new Random();
    }


    public GameMap generate(int radius) {
        int diameter = radius * 2 + 1;
        GameMap map = new GameMap(diameter, diameter);

        List<Position> positions = allPositionsInRadius(radius);


        assignTerrain(map, positions, radius);


        assignResources(map, positions);

        revealAroundOrigin(map, 2);

        return map;
    }


    private void assignTerrain(GameMap map, List<Position> positions, int radius) {
        Position origin = new Position(0, 0);
        map.addHex(new Hex(origin, TerrainType.PLAIN, null));

        List<Position> rest = new ArrayList<>(positions);
        rest.remove(origin);
        Collections.shuffle(rest, random);

        int total = rest.size();
        int plains = (int) (total * RATIO_PLAIN);
        int forests = (int) (total * RATIO_FOREST);
        int mountains = (int) (total * RATIO_MOUNTAIN);

        int i = 0;
        for (; i < plains && i < total; i++) map.addHex(new Hex(rest.get(i), TerrainType.PLAIN, null));
        for (; i < plains + forests && i < total; i++) map.addHex(new Hex(rest.get(i), TerrainType.FOREST, null));
        for (; i < plains + forests + mountains && i < total; i++)
            map.addHex(new Hex(rest.get(i), TerrainType.MOUNTAIN, null));
        for (; i < total; i++) map.addHex(new Hex(rest.get(i), TerrainType.FARMLAND, null));
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
            case FOREST -> random.nextDouble() < FOREST_WOOD_CHANCE ? ResourceType.WOOD : null;
            case MOUNTAIN -> {
                double r = random.nextDouble();
                if (r < MOUNTAIN_STONE_CHANCE) yield ResourceType.STONE;
                if (r < MOUNTAIN_STONE_CHANCE + MOUNTAIN_IRON_CHANCE) yield ResourceType.IRON;
                yield null;
            }
            case FARMLAND -> random.nextDouble() < FARMLAND_FOOD_CHANCE ? ResourceType.FOOD : null;
            case PLAIN -> random.nextDouble() < PLAIN_LIVESTOCK_CHANCE ? ResourceType.FOOD : null;
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


    private List<Position> allPositionsInRadius(int radius) {
        return new Position(0, 0).positionsInRadius(radius);
    }
}