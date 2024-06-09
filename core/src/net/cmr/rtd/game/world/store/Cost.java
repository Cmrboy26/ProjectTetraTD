package net.cmr.rtd.game.world.store;

import java.util.function.Function;

import net.cmr.rtd.game.storage.TeamInventory;
import net.cmr.rtd.game.storage.TeamInventory.Material;

public abstract class Cost implements Function<Integer, TeamInventory> {

    private Cost() {
        
    }

    public boolean canPurchase(TeamInventory inventory) {
        return canPurchase(inventory, 0);
    }

    public boolean canPurchase(TeamInventory testInventory, int level) {
        TeamInventory neededMaterials = apply(level);
        boolean canPurchase = true;
        canPurchase &= testInventory.getCash() >= neededMaterials.getCash();
        System.out.println(testInventory.getCash()+" >= "+neededMaterials.getCash() + " = " + (testInventory.getCash() >= neededMaterials.getCash()));

        canPurchase &= testInventory.getScopes() >= neededMaterials.getScopes();
        canPurchase &= testInventory.getScraps() >= neededMaterials.getScraps();
        canPurchase &= testInventory.getWd40() >= neededMaterials.getWd40();

        for (Material material : Material.values()) {
            int needed = neededMaterials.getMaterial(material);
            int current = testInventory.getMaterial(material);
            //System.out.println(current+" >= "+needed + " = " + (current >= needed));
            canPurchase &= current >= needed;
        }

        return canPurchase;
    }

    public void purchase(TeamInventory inventory) {
        purchase(inventory, 0);
    }

    public void purchase(TeamInventory inventory, int level) {
        TeamInventory neededMaterials = apply(level);
        inventory.removeCash(neededMaterials.getCash());
        inventory.removeScopes(neededMaterials.getScopes());
        inventory.removeScrapMetal(neededMaterials.getScraps());
        inventory.removeWd40(neededMaterials.getWd40());

        for (Material material : Material.values()) {
            int needed = neededMaterials.getMaterial(material);
            inventory.removeMaterial(material, needed);
        }
    }

    public static Cost money(Function<Integer, Long> costFunction) {
        return new Cost() {
            @Override
            public TeamInventory apply(Integer t) {
                TeamInventory inventory = new TeamInventory();
                inventory.setCash(costFunction.apply(t));
                return inventory;
            }
        };
    }

    public static Cost material(Material material, int count) {
        return new Cost() {
            @Override
            public TeamInventory apply(Integer t) {
                TeamInventory inventory = new TeamInventory();
                inventory.setMaterial(material, count);
                return inventory;
            }
        };
    
    }
    
    public static Cost create(Function<Integer, TeamInventory> costFunction) {
        return new Cost() {
            @Override
            public TeamInventory apply(Integer t) {
                return costFunction.apply(t);
            }
        };
    }

}
