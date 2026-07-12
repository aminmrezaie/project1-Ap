package model.economy;

import model.enums.ResourceType;

import java.util.Map;

public class Upgrade {

    public enum UpgradeId {
        STORAGE_UPGRADE_1,
        STORAGE_UPGRADE_2,
        STONE_MINE_TECH,
        IRON_MINE_TECH,
        PROFESSIONAL_TOOLS,
        TOWN_CONSTRUCTION
    }

    private final UpgradeId id;
    private final String name;
    private final String description;
    private final Map<ResourceType, Integer> cost;
    private boolean unlocked;

    public Upgrade(UpgradeId id, String name, String description,
                   Map<ResourceType, Integer> cost) {
        this.id          = id;
        this.name        = name;
        this.description = description;
        this.cost        = cost;
        this.unlocked    = false;
    }


    public UpgradeId getId()          { return id; }
    public String getName()           { return name; }
    public String getDescription()    { return description; }
    public Map<ResourceType, Integer> getCost() { return cost; }
    public boolean isUnlocked()       { return unlocked; }


    public void unlock() {
        if (unlocked) throw new IllegalStateException(name + " is already unlocked");
        this.unlocked = true;
    }

    public boolean canAfford(ResourceStorage storage) {
        for (Map.Entry<ResourceType, Integer> entry : cost.entrySet()) {
            if (!storage.canAfford(entry.getKey(), entry.getValue())) return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Upgrade[" + name + ", unlocked=" + unlocked + ", cost=" + cost + "]";
    }
}
