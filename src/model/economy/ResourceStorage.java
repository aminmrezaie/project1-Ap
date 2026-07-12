package model.economy;

import model.enums.ResourceType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
public class ResourceStorage {

    private final Map<ResourceType, Integer> amounts;
    private final Map<ResourceType, Integer> netRates; // net per turn (can be negative)
    private int capacity;

    public ResourceStorage(int capacity) {
        this.capacity = capacity;
        this.amounts = new EnumMap<>(ResourceType.class);
        this.netRates = new EnumMap<>(ResourceType.class);
        for (ResourceType rt : ResourceType.values()) {
            amounts.put(rt, 0);
            netRates.put(rt, 0);
        }
    }
    public int get(ResourceType type) {
        return amounts.getOrDefault(type, 0);
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        if (capacity < 0) throw new IllegalArgumentException("Capacity must be >= 0");
        this.capacity = capacity;
    }

    public void increaseCapacity(int delta) {
        if (delta <= 0) throw new IllegalArgumentException("Delta must be > 0");
        this.capacity += delta;
    }

    public Map<ResourceType, Integer> getAll() {
        return Collections.unmodifiableMap(amounts);
    }


    public void add(ResourceType type, int amount) {
        if (amount < 0) throw new IllegalArgumentException("Amount must be >= 0");
        int current = amounts.get(type);
        amounts.put(type, Math.min(current + amount, capacity));
    }


    public boolean consume(ResourceType type, int amount) {
        if (amount < 0) throw new IllegalArgumentException("Amount must be >= 0");
        int current = amounts.get(type);
        if (current < amount) return false;
        amounts.put(type, current - amount);
        return true;
    }

    public void forceDeduct(ResourceType type, int amount) {
        int current = amounts.get(type);
        amounts.put(type, current - amount);
    }

    public boolean canAfford(ResourceType type, int amount) {
        return amounts.getOrDefault(type, 0) >= amount;
    }


    public void setNetRate(ResourceType type, int rate) {
        netRates.put(type, rate);
    }

    public int getNetRate(ResourceType type) {
        return netRates.getOrDefault(type, 0);
    }

    public Map<ResourceType, Integer> getAllNetRates() {
        return Collections.unmodifiableMap(netRates);
    }


    public boolean isStarving() {
        return amounts.getOrDefault(ResourceType.FOOD, 0) < 0;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Storage[capacity=").append(capacity).append("] {");
        for (ResourceType rt : ResourceType.values()) {
            sb.append(rt.getDisplayName())
                    .append(": ").append(amounts.get(rt))
                    .append(" (").append(netRates.get(rt) >= 0 ? "+" : "").append(netRates.get(rt)).append("/turn), ");
        }
        sb.append("}");
        return sb.toString();
    }
}
