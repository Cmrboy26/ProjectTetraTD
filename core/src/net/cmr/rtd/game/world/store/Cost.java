package net.cmr.rtd.game.world.store;

import java.util.function.Function;

import net.cmr.rtd.game.storage.TeamInventory;

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

        canPurchase &= testInventory.getScopes() >= neededMaterials.getScopes();
        canPurchase &= testInventory.getScraps() >= neededMaterials.getScraps();
        canPurchase &= testInventory.getWd40() >= neededMaterials.getWd40();

        canPurchase &= testInventory.steel >= neededMaterials.steel;
        canPurchase &= testInventory.titanium >= neededMaterials.titanium;
        canPurchase &= testInventory.diamonds >= neededMaterials.diamonds;
        canPurchase &= testInventory.cryonite >= neededMaterials.cryonite;
        canPurchase &= testInventory.thorium >= neededMaterials.thorium;
        canPurchase &= testInventory.ruby >= neededMaterials.ruby;
        canPurchase &= testInventory.quartz >= neededMaterials.quartz;
        canPurchase &= testInventory.topaz >= neededMaterials.topaz;

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
        inventory.steel -= neededMaterials.steel;
        inventory.titanium -= neededMaterials.titanium;
        inventory.diamonds -= neededMaterials.diamonds;
        inventory.cryonite -= neededMaterials.cryonite;
        inventory.thorium -= neededMaterials.thorium;
        inventory.ruby -= neededMaterials.ruby;
        inventory.quartz -= neededMaterials.quartz;
        inventory.topaz -= neededMaterials.topaz;
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
    
    public static Cost create(Function<Integer, TeamInventory> costFunction) {
        return new Cost() {
            @Override
            public TeamInventory apply(Integer t) {
                return costFunction.apply(t);
            }
        };
    }

}
