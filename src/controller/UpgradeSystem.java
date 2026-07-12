package controller;

import model.buildings.IronMine;
import model.buildings.ProductionBuilding;
import model.buildings.StoneMine;
import model.buildings.Town;
import model.buildings.TownHall;
import model.economy.Empire;
import model.economy.Upgrade;
import model.economy.Upgrade.UpgradeId;
import model.enums.ResourceType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class UpgradeSystem {

    private static final int STORAGE_UPGRADE_AMOUNT = 50;
    private static final double PROFESSIONAL_TOOLS_MULTIPLIER = 1.5;

    private final List<Upgrade> upgrades;
    private boolean stoneTechUnlocked = false;
    private boolean ironTechUnlocked  = false;

    public UpgradeSystem() {
        this.upgrades = buildUpgradeList();
    }


    public List<Upgrade> getAvailableUpgrades(Empire empire) {
        List<Upgrade> available = new ArrayList<>();
        for (Upgrade u : upgrades) {
            if (!u.isUnlocked() && u.canAfford(empire.getStorage())
                    && prerequisitesMet(u)) {
                available.add(u);
            }
        }
        return available;
    }

    public List<Upgrade> getAllUpgrades() { return List.copyOf(upgrades); }

    public boolean isStoneTechUnlocked() { return stoneTechUnlocked; }
    public boolean isIronTechUnlocked()  { return ironTechUnlocked; }


    public enum UpgradeResult { OK, ALREADY_UNLOCKED, NOT_ENOUGH_RESOURCES,
        PREREQUISITE_NOT_MET, UNKNOWN }

    public UpgradeResult purchase(UpgradeId id, Empire empire) {
        Upgrade upgrade = findById(id);
        if (upgrade == null)          return UpgradeResult.UNKNOWN;
        if (upgrade.isUnlocked())     return UpgradeResult.ALREADY_UNLOCKED;
        if (!prerequisitesMet(upgrade)) return UpgradeResult.PREREQUISITE_NOT_MET;
        if (!upgrade.canAfford(empire.getStorage())) return UpgradeResult.NOT_ENOUGH_RESOURCES;

        for (Map.Entry<ResourceType, Integer> e : upgrade.getCost().entrySet()) {
            empire.spend(e.getKey(), e.getValue());
        }

        upgrade.unlock();
        applyEffect(upgrade, empire);
        return UpgradeResult.OK;
    }


    private void applyEffect(Upgrade upgrade, Empire empire) {
        switch (upgrade.getId()) {
            case STORAGE_UPGRADE_1, STORAGE_UPGRADE_2 ->
                    empire.getStorage().increaseCapacity(STORAGE_UPGRADE_AMOUNT);

            case STONE_MINE_TECH ->
                    stoneTechUnlocked = true;

            case IRON_MINE_TECH ->
                    ironTechUnlocked = true;

            case PROFESSIONAL_TOOLS -> {
                empire.getBuildings().stream()
                        .filter(b -> b instanceof StoneMine || b instanceof IronMine)
                        .map(b -> (ProductionBuilding) b)
                        .forEach(pb -> pb.setBaseProductionRate(
                                (int)(pb.getBaseProductionRate() * PROFESSIONAL_TOOLS_MULTIPLIER)));
            }

            case TOWN_CONSTRUCTION -> {
                empire.getTownHall().increaseUnitCap(Town.UNIT_CAP_BONUS);
            }
        }
    }


    private boolean prerequisitesMet(Upgrade upgrade) {
        return switch (upgrade.getId()) {
            case IRON_MINE_TECH -> stoneTechUnlocked;
            case STORAGE_UPGRADE_2 -> findById(UpgradeId.STORAGE_UPGRADE_1).isUnlocked();
            default -> true;
        };
    }


    private List<Upgrade> buildUpgradeList() {
        List<Upgrade> list = new ArrayList<>();
        list.add(new Upgrade(UpgradeId.STORAGE_UPGRADE_1,   "Storage Upgrade I",
                "Increases warehouse capacity by 50",
                Map.of(ResourceType.WOOD, 10, ResourceType.STONE, 5)));
        list.add(new Upgrade(UpgradeId.STORAGE_UPGRADE_2,   "Storage Upgrade II",
                "Increases warehouse capacity by 50 more",
                Map.of(ResourceType.WOOD, 20, ResourceType.STONE, 10)));
        list.add(new Upgrade(UpgradeId.STONE_MINE_TECH,     "Stone Mining",
                "Unlocks Stone Mine construction",
                Map.of(ResourceType.WOOD, 8)));
        list.add(new Upgrade(UpgradeId.IRON_MINE_TECH,      "Iron Mining",
                "Unlocks Iron Mine construction (requires Stone Mining)",
                Map.of(ResourceType.WOOD, 10, ResourceType.STONE, 8)));
        list.add(new Upgrade(UpgradeId.PROFESSIONAL_TOOLS,  "Professional Tools",
                "Stone and Iron production ×1.5",
                Map.of(ResourceType.WOOD, 15, ResourceType.IRON, 5)));
        list.add(new Upgrade(UpgradeId.TOWN_CONSTRUCTION,   "Township",
                "Increases unit cap by " + Town.UNIT_CAP_BONUS,
                Map.of(ResourceType.WOOD, 12, ResourceType.STONE, 8)));
        return list;
    }

    private Upgrade findById(UpgradeId id) {
        return upgrades.stream().filter(u -> u.getId() == id).findFirst().orElse(null);
    }
}
