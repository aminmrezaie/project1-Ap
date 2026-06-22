package controller;

import model.buildings.Building;
import model.buildings.ProductionBuilding;
import model.buildings.Town;
import model.buildings.TownHall;
import model.buildings.Village;
import model.economy.Empire;
import model.enums.ResourceType;

import java.util.ArrayList;
import java.util.List;


public class ProductionSystem {


    private static final int FOOD_PER_UNIT = 1;



    public static class ProductionReport {
        public final int foodProduced;
        public final int woodProduced;
        public final int stoneProduced;
        public final int ironProduced;
        public final int foodConsumedByUnits;
        public final int upkeepWood;
        public final int upkeepStone;
        public final List<Building> destroyedBuildings;

        public ProductionReport(int fp, int wp, int sp, int ip,
                                int fc, int uw, int us,
                                List<Building> destroyed) {
            this.foodProduced        = fp;
            this.woodProduced        = wp;
            this.stoneProduced       = sp;
            this.ironProduced        = ip;
            this.foodConsumedByUnits = fc;
            this.upkeepWood          = uw;
            this.upkeepStone         = us;
            this.destroyedBuildings  = destroyed;
        }

        public int netFood()  { return foodProduced  - foodConsumedByUnits; }
        public int netWood()  { return woodProduced   - upkeepWood; }
        public int netStone() { return stoneProduced  - upkeepStone; }
    }

    public ProductionReport processTurn(Empire empire) {

        int foodProduced  = 0;
        int woodProduced  = 0;
        int stoneProduced = 0;
        int ironProduced  = 0;
        int upkeepWood    = 0;
        int upkeepStone   = 0;
        List<Building> destroyed = new ArrayList<>();

        for (Building b : empire.getBuildings()) {
            if (b instanceof TownHall th) {
                woodProduced += th.getSafeguardWood();
                foodProduced += th.getSafeguardFood();
                continue;
            }
            if (b instanceof ProductionBuilding pb && pb.hasWorkers()) {
                int amount = pb.getProduction();
                switch (pb.getProducedResource()) {
                    case FOOD  -> foodProduced  += amount;
                    case WOOD  -> woodProduced  += amount;
                    case STONE -> stoneProduced += amount;
                    case IRON  -> ironProduced  += amount;
                }
            }
        }

        empire.addResource(ResourceType.FOOD,  foodProduced);
        empire.addResource(ResourceType.WOOD,  woodProduced);
        empire.addResource(ResourceType.STONE, stoneProduced);
        empire.addResource(ResourceType.IRON,  ironProduced);

        int foodConsumed = empire.getUnitCount() * FOOD_PER_UNIT;
        empire.getStorage().forceDeduct(ResourceType.FOOD, foodConsumed);

        for (Building b : new ArrayList<>(empire.getBuildings())) {
            if (b instanceof TownHall) continue;
            int cost = b.getUpkeepCost();
            if (cost == 0) continue;

            ResourceType upkeepType = b.getUpkeepResourceType();
            boolean paid = empire.spend(upkeepType, cost);

            if (upkeepType == ResourceType.WOOD)  upkeepWood  += cost;
            if (upkeepType == ResourceType.STONE) upkeepStone += cost;


            if (!paid) {
            }
        }


        updateNetRates(empire, foodProduced, woodProduced, stoneProduced, ironProduced,
                foodConsumed, upkeepWood, upkeepStone);

        return new ProductionReport(foodProduced, woodProduced, stoneProduced, ironProduced,
                foodConsumed, upkeepWood, upkeepStone, destroyed);
    }


    private void updateNetRates(Empire empire,
                                int fp, int wp, int sp, int ip,
                                int fc, int uw, int us) {
        empire.getStorage().setNetRate(ResourceType.FOOD,  fp - fc);
        empire.getStorage().setNetRate(ResourceType.WOOD,  wp - uw);
        empire.getStorage().setNetRate(ResourceType.STONE, sp - us);
        empire.getStorage().setNetRate(ResourceType.IRON,  ip);
    }


    public String tickUnitProduction(Empire empire) {
        return empire.getTownHall().tickProduction();
    }
}