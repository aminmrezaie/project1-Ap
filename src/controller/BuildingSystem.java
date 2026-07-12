package controller;

import model.buildings.*;
import model.economy.Empire;
import model.enums.BuildingType;
import model.enums.Position;
import model.enums.ResourceType;
import model.enums.TerrainType;
import model.map.GameMap;
import model.map.Hex;
import model.unit.Builder;
import model.unit.Worker;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public class BuildingSystem {


    private static final Map<BuildingType, Map<ResourceType, Integer>> BUILD_COSTS = new EnumMap<>(BuildingType.class);

    static {
        BUILD_COSTS.put(BuildingType.LUMBER_MILL, Map.of(ResourceType.WOOD,  2));
        BUILD_COSTS.put(BuildingType.STONE_MINE,  Map.of(ResourceType.WOOD,  3));
        BUILD_COSTS.put(BuildingType.IRON_MINE,   Map.of(ResourceType.WOOD,  3, ResourceType.STONE, 2));
        BUILD_COSTS.put(BuildingType.FARM,         Map.of(ResourceType.WOOD,  2));
        BUILD_COSTS.put(BuildingType.STABLE,       Map.of(ResourceType.WOOD,  3));
        BUILD_COSTS.put(BuildingType.TOWN,         Map.of(ResourceType.WOOD,  5, ResourceType.STONE, 5));
        BUILD_COSTS.put(BuildingType.VILLAGE,      Map.of(ResourceType.WOOD,  3, ResourceType.STONE, 2));
    }


    public enum BuildResult {
        OK,
        NOT_A_BUILDER,
        NO_CHARGES,
        NOT_ENOUGH_AP,
        HEX_NOT_OWNED,
        HEX_ALREADY_HAS_BUILDING,
        WRONG_TERRAIN,
        WRONG_RESOURCE,
        HEX_MUST_BE_RESOURCE_FREE,
        NOT_ENOUGH_RESOURCES,
        TECH_REQUIRED
    }

    private final UpgradeSystem upgradeSystem;

    public BuildingSystem(UpgradeSystem upgradeSystem) {
        this.upgradeSystem = upgradeSystem;
    }


    public BuildResult validate(Builder builder, BuildingType type,
                                Position targetPos, GameMap map, Empire empire) {
        if (!builder.isAlive())    return BuildResult.NOT_A_BUILDER;
        if (!builder.hasCharges()) return BuildResult.NO_CHARGES;
        if (!builder.hasAP(type.getBuildApCost())) return BuildResult.NOT_ENOUGH_AP;

        Optional<Hex> hexOpt = map.getHex(targetPos);
        if (hexOpt.isEmpty())       return BuildResult.HEX_NOT_OWNED;

        Hex hex = hexOpt.get();
        if (!hex.isOwned())         return BuildResult.HEX_NOT_OWNED;
        if (hex.hasBuilding())      return BuildResult.HEX_ALREADY_HAS_BUILDING;

        BuildResult terrainCheck = checkTerrain(type, hex);
        if (terrainCheck != BuildResult.OK) return terrainCheck;

        if (type == BuildingType.STONE_MINE && !upgradeSystem.isStoneTechUnlocked()) return BuildResult.TECH_REQUIRED;
        if (type == BuildingType.IRON_MINE  && !upgradeSystem.isIronTechUnlocked())  return BuildResult.TECH_REQUIRED;

        Map<ResourceType, Integer> cost = BUILD_COSTS.getOrDefault(type, Map.of());
        for (Map.Entry<ResourceType, Integer> e : cost.entrySet()) {
            if (!empire.canAfford(e.getKey(), e.getValue())) return BuildResult.NOT_ENOUGH_RESOURCES;
        }

        return BuildResult.OK;
    }



    public Building executeBuild(Builder builder, BuildingType type,
                                 Position targetPos, GameMap map, Empire empire) {
        if (validate(builder, type, targetPos, map, empire) != BuildResult.OK) return null;

        Hex hex = map.getHex(targetPos).get();

        Map<ResourceType, Integer> cost = BUILD_COSTS.getOrDefault(type, Map.of());
        for (Map.Entry<ResourceType, Integer> e : cost.entrySet()) {
            empire.spend(e.getKey(), e.getValue());
        }

        Building building = createBuilding(type, targetPos);
        hex.setBuilding(building);
        empire.addBuilding(building);

        if (building instanceof Town) {
            empire.getTownHall().increaseUnitCap(Town.UNIT_CAP_BONUS);
        } else if (building instanceof Village) {
            empire.getTownHall().increaseUnitCap(Village.UNIT_CAP_BONUS);
        }

        builder.build(type);

        return building;
    }


    public enum StationResult { OK, NOT_A_WORKER, NO_BUILDING, BUILDING_FULL, NOT_ENOUGH_AP }

    public StationResult stationWorker(Worker worker, Position targetPos, GameMap map) {
        Optional<Hex> hexOpt = map.getHex(targetPos);
        if (hexOpt.isEmpty() || !hexOpt.get().hasBuilding()) return StationResult.NO_BUILDING;

        Building building = hexOpt.get().getBuilding();
        if (building.isFull()) return StationResult.BUILDING_FULL;

        return worker.station(building) ? StationResult.OK : StationResult.NOT_ENOUGH_AP;
    }


    private BuildResult checkTerrain(BuildingType type, Hex hex) {
        return switch (type) {
            case LUMBER_MILL -> hex.getTerrain() == TerrainType.FOREST    ? BuildResult.OK : BuildResult.WRONG_TERRAIN;
            case STONE_MINE  -> {
                if (hex.getTerrain() != TerrainType.MOUNTAIN) yield BuildResult.WRONG_TERRAIN;
                if (hex.getResource() != ResourceType.STONE)  yield BuildResult.WRONG_RESOURCE;
                yield BuildResult.OK;
            }
            case IRON_MINE   -> {
                if (hex.getTerrain() != TerrainType.MOUNTAIN) yield BuildResult.WRONG_TERRAIN;
                if (hex.getResource() != ResourceType.IRON)   yield BuildResult.WRONG_RESOURCE;
                yield BuildResult.OK;
            }
            case FARM        -> hex.getTerrain() == TerrainType.FARMLAND  ? BuildResult.OK : BuildResult.WRONG_TERRAIN;
            case STABLE      -> {
                if (hex.getTerrain() != TerrainType.PLAIN)    yield BuildResult.WRONG_TERRAIN;
                if (hex.getResource() != ResourceType.FOOD)   yield BuildResult.WRONG_RESOURCE;
                yield BuildResult.OK;
            }
            case TOWN, VILLAGE -> {
                if (hex.getTerrain() != TerrainType.PLAIN) yield BuildResult.WRONG_TERRAIN;
                if (hex.hasResource()) yield BuildResult.HEX_MUST_BE_RESOURCE_FREE;
                yield BuildResult.OK;
            }
            default            -> BuildResult.OK;
        };
    }


    private Building createBuilding(BuildingType type, Position pos) {
        return switch (type) {
            case LUMBER_MILL -> new LumberMill(pos);
            case STONE_MINE  -> new StoneMine(pos);
            case IRON_MINE   -> new IronMine(pos);
            case FARM        -> new Farm(pos);
            case STABLE      -> new Stable(pos);
            case TOWN        -> new Town(pos);
            case VILLAGE     -> new Village(pos);
            default          -> throw new IllegalArgumentException("Cannot build " + type);
        };
    }


    public Map<ResourceType, Integer> getCost(BuildingType type) {
        return BUILD_COSTS.getOrDefault(type, Map.of());
    }
}
