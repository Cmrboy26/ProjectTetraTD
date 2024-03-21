package net.cmr.rtd.game.world.store;

import java.util.HashMap;

import net.cmr.rtd.game.GameManager;
import net.cmr.rtd.game.GamePlayer;
import net.cmr.rtd.game.packets.PurchaseItemPacket;
import net.cmr.rtd.game.packets.PurchaseItemPacket.PurchaseAction;
import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.GameObject;
import net.cmr.rtd.game.world.GameObject.GameType;
import net.cmr.rtd.game.world.TeamData;
import net.cmr.rtd.game.world.World;
import net.cmr.rtd.game.world.entities.TowerEntity;
import net.cmr.rtd.game.world.tile.Tile;
import net.cmr.rtd.game.world.tile.Tile.TileType;
import net.cmr.util.Sprites.AnimationType;
import net.cmr.util.Sprites.SpriteType;
import net.cmr.rtd.game.world.tile.TileData;

/**
 * A static class that manages the shop.
 */
public class ShopManager {

    public static final HashMap<GameType, TowerOption> towerCatalog = new HashMap<GameType, TowerOption>();
    public static final HashMap<GameType, UpgradeOption> upgradeCatalog = new HashMap<GameType, UpgradeOption>();

    static {
        // Register the purchase of towers
        registerTower(new TowerOption(GameType.SHOOTER_TOWER, AnimationType.SHOOTER_TOWER, 30, "Shooter Tower", "Slows enemies."));
        registerTower(new TowerOption(GameType.FIRE_TOWER, AnimationType.FIRE, 70, "Fire Tower", "Sets enemies ablaze and\noccasionally shoots fireballs."));
        registerTower(new TowerOption(GameType.ICE_TOWER, SpriteType.FROZEN, 20, "Ice Tower", "Slows enemies."));

        // Register the purchase of upgrades
        registerUpgrade(new UpgradeOption(GameType.SHOOTER_TOWER, level -> 10L + level * level * 15L,         level -> 5f + (level * level) / 3f));
        registerUpgrade(new UpgradeOption(GameType.FIRE_TOWER, level -> 20L + level * level * 25L,  level -> 5f + level * 1.5f));
        registerUpgrade(new UpgradeOption(GameType.ICE_TOWER, level -> level * level * 30L,         level -> 5f + level / 3f));
    }

    private static void registerTower(TowerOption item) {
        towerCatalog.put(item.type, item);
    }
    private static void registerUpgrade(UpgradeOption item) {
        upgradeCatalog.put(item.type, item);
    }

    public static HashMap<GameType, TowerOption> getTowerCatalog() {
        return towerCatalog;
    }
    public static HashMap<GameType, UpgradeOption> getUpgradeCatalog() {
        return upgradeCatalog;
    }

    public static void processPurchase(GameManager manager, GamePlayer player, PurchaseItemPacket packet) {
        int team = player.getTeam();
        int x = packet.x;
        int y = packet.y;
        PurchaseAction action = packet.option;
        if (team == -1) { return; }

        TowerEntity towerAt = towerAt(manager, x, y);
        if (towerAt != null && towerAt.getTeam() != team) {
            // Not your tower
            return;
        }
        GameType type = packet.type;

        switch (action) {
            case TOWER: {
                if (towerAt != null) {
                    // Tower exists at the position
                    return;
                }
                TowerOption option = towerCatalog.get(type);
                long cost = option.cost;
                TeamData data = manager.getTeam(player.getTeam());
                if (data.getMoney() < cost) {
                    // Not enough money
                    return;
                }
                if (areTilesBlocking(manager, packet.x, packet.y)) {
                    // Tiles are blocking
                    return;
                }
                TowerEntity tower = newTower(packet.type, player.getTeam());
                if (tower == null) {
                    // Not a tower
                    return; 
                }
                tower.setPosition((packet.x + .5f) * Tile.SIZE, (packet.y + .5f) * Tile.SIZE);
                manager.getWorld().addEntity(tower);
                tower.updatePresenceOnClients(manager);
                // Construction particle?
                // Update the team's money.
                data.payMoney(cost);
                manager.updateTeamStats(player.getTeam());
                break;
            }
            case UPGRADE: {
                if (towerAt == null) {
                    // No tower to upgrade
                    System.out.println("No tower to upgrade");
                    return;
                }
                int currentLevel = towerAt.getLevel();
                UpgradeOption option = upgradeCatalog.get(towerAt.type);
                long cost = option.cost.apply(currentLevel);
                TeamData data = manager.getTeam(player.getTeam());
                if (data.getMoney() < cost) {
                    // Not enough money
                    System.out.println("Not enough money");
                    return;
                }
                boolean success = towerAt.levelUp(option.levelUpTime.apply(currentLevel));
                if (!success) {
                    // Tower is already upgrading
                    System.out.println("Tower is already upgrading");
                    return;
                }
                towerAt.updatePresenceOnClients(manager);
                // Construction particle?
                // Update the team's money.
                data.payMoney(cost);
                manager.updateTeamStats(player.getTeam());
                break;
            }
            case SELL: {
                if (towerAt == null) {
                    // No tower to sell
                    return;
                }
                // Remove
                towerAt.removeFromWorld();
                towerAt.updatePresenceOnClients(manager);
                // Update the team's money.
                TeamData data = manager.getTeam(player.getTeam());
                long value = towerSellValue(towerAt);
                data.addMoney(value);
                manager.updateTeamStats(player.getTeam());
                break;
            }
            default:
                break; 
        }
    }
    
    private static TowerEntity newTower(GameType type, int team) {
        Class<? extends GameObject> clazz = type.getGameObjectClass();  
        if (TowerEntity.class.isAssignableFrom(clazz)) {
            try {
                return (TowerEntity) clazz.getConstructor(int.class).newInstance(team);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    } 

    
    private static TowerEntity towerAt(GameManager manager, int x, int y) {
        return towerAt(manager.getWorld(), x, y);
    }

    /**
     * @return null if no tower exists at that spot, otherwise the tower is returned
     */
    public static TowerEntity towerAt(World world, int x, int y) {
        if (world == null) {
            return null;
        }
        for (Entity entity : world.getEntities()) {
            if (!(entity instanceof TowerEntity)) {
                continue;
            }
            TowerEntity tower = (TowerEntity) entity;
            float positionX = (x + .5f) * Tile.SIZE;
            float positionY = (y + .5f) * Tile.SIZE;
            if (tower.getX() == positionX && tower.getY() == positionY) {
                return tower;
            }
        }
        return null;
    }

    public static String costToString(long cost) {
        String suffix = "";
        if (cost >= 1000000) {
            cost /= 1000000;
            suffix = "M";
        } else if (cost >= 1000) {
            cost /= 1000;
            suffix = "K";
        }

        return "$" + cost + suffix;
    }

    private static long towerSellValue(TowerEntity tower) {
        return towerValue(tower) / 2L;
    }

    private static long towerValue(TowerEntity tower) {
        int level = tower.getLevel();
        long purchaseCost = towerCatalog.get(tower.type).cost;
        long upgradesCost = upgradeCatalog.get(tower.type).cost.apply(level); // only the cost of the latest upgrade is accounted for
        return purchaseCost + upgradesCost;
    }

    public static boolean areTilesBlocking(GameManager manager, int x, int y) {
        World world = manager.getWorld();
        TileType at = world.getTile(x, y, 1);
        TileData data = world.getTileData(x, y, 1);
        return at == TileType.WALL || data != null;
    }

}
