package net.cmr.rtd.game.world.store;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Objects;

import com.badlogic.gdx.math.Vector2;

import net.cmr.rtd.game.GameManager;
import net.cmr.rtd.game.GamePlayer;
import net.cmr.rtd.game.packets.PurchaseItemPacket;
import net.cmr.rtd.game.packets.PurchaseItemPacket.PurchaseAction;
import net.cmr.rtd.game.storage.TeamInventory;
import net.cmr.rtd.game.storage.TeamInventory.Material;
import net.cmr.rtd.game.storage.TeamInventory.MaterialType;
import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.GameObject.GameType;
import net.cmr.rtd.game.world.TeamData;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.World;
import net.cmr.rtd.game.world.entities.MiningTower;
import net.cmr.rtd.game.world.entities.TowerEntity;
import net.cmr.rtd.game.world.entities.splashes.SplashAOE;
import net.cmr.rtd.game.world.store.ShopManager.StoreException.StoreExceptionType;
import net.cmr.rtd.game.world.tile.Tile;
import net.cmr.rtd.game.world.tile.Tile.TileType;
import net.cmr.rtd.game.world.tile.TileData;
import net.cmr.util.Log;
import net.cmr.util.Sprites.AnimationType;
import net.cmr.util.Sprites.SpriteType;

/**
 * A static class that manages the shop.
 */
public class ShopManager {

    public static final HashMap<GameType, TowerOption> towerCatalog = new HashMap<GameType, TowerOption>();
    public static final HashMap<GameType, UpgradeOption> upgradeCatalog = new HashMap<GameType, UpgradeOption>();
    public static final HashMap<GameType, ConsumableOption> consumableCatalog = new HashMap<GameType, ConsumableOption>();

    static {
        // Register the purchase of towers
        registerTower(new TowerOption(0, GameType.SHOOTER_TOWER, AnimationType.SHOOTER_TOWER_1,
                Cost.money(level -> 35L), "Shooter Tower", "Shoots pellets at enemies."));
        registerTower(new TowerOption(1, GameType.FIRE_TOWER, AnimationType.FIRE_TOWER_1, Cost.money(level -> 70L),
                "Fire Tower", "Sets enemies on fire and\nshoots piercing fireballs."));
        registerTower(new TowerOption(2, GameType.ICE_TOWER, SpriteType.ICE_TOWER_1, Cost.money(level -> 40L),
                "Ice Tower", "Slows enemies."));
        registerTower(new TowerOption(3, GameType.DRILL_TOWER, SpriteType.DRILL_TOWER_ONE, Cost.money(level -> 90L),
                "Drill Tower", "Mines resources from ore veins."));
        registerTower(new TowerOption(4, GameType.GEMSTONE_EXTRACTOR, SpriteType.GEMSTONE_EXTRACTOR_ONE,
                Cost.create(level -> {
                    TeamInventory inventory = new TeamInventory();
                    inventory.setCash(100L);
                    inventory.steel = 5;
                    return inventory;
                }), "Gemstone Extractor", "Extracts various gems from gem veins."));

        // Register the purchase of upgrades
        registerUpgrade(new UpgradeOption(GameType.SHOOTER_TOWER, Cost.create(level -> {
            TeamInventory inventory = new TeamInventory();
            level += 1;
            inventory.setCash(5L * level * level * (level - 1));
            return inventory;
        }), level -> 5f + (level)));
        registerUpgrade(new UpgradeOption(GameType.FIRE_TOWER,
                Cost.money(level -> 50L + (level + 2) * level * level * 50L), level -> 5f + level * 2f));
        registerUpgrade(new UpgradeOption(GameType.ICE_TOWER, Cost.money(level -> level * level * 50L),
                level -> 5f + level * 2f));
        registerUpgrade(new UpgradeOption(GameType.DRILL_TOWER, Cost.create(level -> {
            TeamInventory inventory = new TeamInventory();
            inventory.cash = level * level * 50L;
            if (level >= 2) {
                inventory.titanium = level % 2;
            }
            return inventory;
        }), level -> 10f + level * 2));
        registerUpgrade(new UpgradeOption(GameType.GEMSTONE_EXTRACTOR, Cost.create(level -> {
            TeamInventory inventory = new TeamInventory();
            inventory.setCash(level * level * 150L);
            inventory.steel = level + 2;
            return inventory;
        }), level -> 10f + level * 2));

        // Register Consumables
        registerConsumable(new ConsumableOption(0, GameType.SLOWNESS_AOE, SpriteType.SLOWNESS_BOTTLE, "Slowness Splash", Cost.create(level -> {
            TeamInventory inventory = new TeamInventory();
            inventory.setCash(30L);
            return inventory;
        }), "Inflicts slowness upon enemies in range.", 15));
        registerConsumable(new ConsumableOption(1, GameType.BLINDNESS_AOE, SpriteType.BLINDNESS_BOTTLE, "Blindness Splash", Cost.create(level -> {
            TeamInventory inventory = new TeamInventory();
            inventory.setCash(150L);
            inventory.titanium = 1;
            return inventory;
        }), "Temporarily blinds enemy players in range. Stand back!\nOnly useful in multiplayer games.", 0));
    }

    private static void registerTower(TowerOption item) {
        towerCatalog.put(item.type, item);
    }

    private static void registerUpgrade(UpgradeOption item) {
        upgradeCatalog.put(item.type, item);
    }

    private static void registerConsumable(ConsumableOption item) {
        consumableCatalog.put(item.type, item);
    }

    public static HashMap<GameType, StoreOption> getAllCatalog() {
        HashMap<GameType, StoreOption> all = new HashMap<>();
        all.putAll(towerCatalog);
        all.putAll(consumableCatalog);
        return all;
    }

    public static HashMap<GameType, TowerOption> getTowerCatalog() {
        return towerCatalog;
    }

    public static HashMap<GameType, UpgradeOption> getUpgradeCatalog() {
        return upgradeCatalog;
    }

    public static HashMap<GameType, ConsumableOption> getConsumableCatalog() {
        return consumableCatalog;
    }

    public static GameType[] getAllTowerTypes() {
        return towerCatalog.keySet().toArray(new GameType[0]);
    }

    public static void processPurchase(GameManager manager, GamePlayer player, PurchaseItemPacket packet) {
        int team = player.getTeam();
        int x = packet.x;
        int y = packet.y;
        PurchaseAction action = packet.option;
        if (team == -1) {
            return;
        }

        // player.getManager().getTeam(player.getTeam()).getInventory().setMaterial(Material.DIAMONDS,
        // 1000);
        // player.getManager().sendStatsUpdatePacket(player);

        TowerEntity towerAt = towerAt(manager.getWorld(), x, y);
        if (towerAt != null && towerAt.getTeam() != team) {
            // Not your tower
            return;
        }
        GameType type = packet.type;

        switch (action) {
            case TOWER: {
                StoreOption option = getAllCatalog().get(type);

                UpdateData data = manager.getUpdateData();
                try {
                    canPlace(option, packet.x, packet.y, Entity.getTileX(player.getPlayer()), Entity.getTileY(player.getPlayer()), player.getTeam(), data);
                } catch (StoreException e) {
                    Log.debug("StoreException: " + e.getMessage());
                    return;
                }

                Vector2 placePosition = new Vector2((packet.x + .5f) * Tile.SIZE, (packet.y + .5f) * Tile.SIZE);
                Entity endEntity = option.createEntity(player, placePosition);
                TeamData teamData = manager.getTeam(player.getTeam());
                if (option instanceof TowerOption) {
                    TowerEntity tower = (TowerEntity) endEntity;
                    if (tower == null) {
                        // Not a tower
                        Log.debug("Not a tower");
                        return;
                    }
                    if (tower instanceof MiningTower) {
                        // Mining tower
                        MiningTower miningTower = (MiningTower) tower;
                        if (!miningTower
                                .validMiningTarget(manager.getUpdateData().getWorld().getTile(packet.x, packet.y, 0))) {
                            // Invalid mining target
                            Log.debug("Invalid mining target");
                            return;
                        }
                    }
                    tower.setBuildDelta(3); // 3 seconds to construct tower
                } else {

                }

                endEntity.setPosition(placePosition);
                manager.getWorld().addEntity(endEntity);
                endEntity.updatePresenceOnClients(manager);
                // Construction particle?
                // Update the team's money.
                option.cost.purchase(teamData.getInventory());
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
                Cost cost = option.cost;
                TeamData data = manager.getTeam(player.getTeam());
                if (!cost.canPurchase(data.getInventory(), currentLevel)) {
                    // Not enough money
                    System.out.println("Not enough money or resources");
                    return;
                }
                boolean success = towerAt.levelUp(option.levelUpTime.apply(currentLevel));
                if (!success) {
                    // Tower is already upgrading
                    return;
                }
                if (towerAt.isBeingBuilt()) {
                    // Tower is still being built
                    return;
                }
                towerAt.updatePresenceOnClients(manager);
                // Construction particle?
                // Update the team's money.
                cost.purchase(data.getInventory(), currentLevel);
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
            case APPLY_LUBRICANT: {
                // Increase tower speed
                if (towerAt == null) {
                    return;
                }
                TeamData data = manager.getTeam(player.getTeam());
                boolean successful = towerAt.applyLubricant(data.getInventory());
                if (!successful) {
                    return;
                }
                manager.updateTeamStats(player.getTeam());
                towerAt.updatePresenceOnClients(manager);
                break;
            }
            case APPLY_SCOPE: {
                // Increase tower range
                if (towerAt == null) {
                    return;
                }
                TeamData data = manager.getTeam(player.getTeam());
                boolean successful = towerAt.applyScope(data.getInventory());
                if (!successful) {
                    return;
                }
                manager.updateTeamStats(player.getTeam());
                towerAt.updatePresenceOnClients(manager);
                break;
            }
            case APPLY_SCRAP_METAL: {
                // Increase tower damage
                if (towerAt == null) {
                    return;
                }
                TeamData data = manager.getTeam(player.getTeam());
                boolean successful = towerAt.applyScrapMetal(data.getInventory());
                if (!successful) {
                    return;
                }
                manager.updateTeamStats(player.getTeam());
                towerAt.updatePresenceOnClients(manager);
                break;
            }
            default:
                break;
        }
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
            int towerX = Entity.getTileX(tower);
            int towerY = Entity.getTileY(tower);
            if (towerX == x && towerY == y) {
                return tower;
            }
        }
        return null;
    }

    /**
     * Same as @see#towerAt(World, int, int) but also checks the team of the tower
     */
    public static TowerEntity towerAt(World world, int x, int y, int team) {
        if (world == null) {
            return null;
        }
        for (Entity entity : world.getEntities()) {
            if (!(entity instanceof TowerEntity)) {
                continue;
            }
            TowerEntity tower = (TowerEntity) entity;
            int towerX = Entity.getTileX(tower);
            int towerY = Entity.getTileY(tower);
            if (towerX == x && towerY == y) {
                if (tower != null && tower.getTeam() != team) {
                    tower = null;
                }
                return tower;
            }
        }
        return null;
    }

    private static final DecimalFormat df = new DecimalFormat("#.#");

    public static String costToString(long cost) {
        // Format the cost
        // > 1000 -> 1.0k
        // > 1000000 -> 1.0m

        if (cost < 1000) {
            return "$" + cost + "";
        }
        if (cost < 1000000) {
            return "$" + df.format(cost / 1000.0) + "k";
        }
        return "$" + df.format(cost / 1000000.0) + "m";
    }

    private static long towerSellValue(TowerEntity tower) {
        return towerCashValue(tower);
    }

    private static long towerCashValue(TowerEntity tower) {
        int level = tower.getLevel();
        long purchaseCost = towerCatalog.get(tower.type).cost.apply(0).getCash();
        long upgradesCost = Math.max(0, upgradeCatalog.get(tower.type).cost.apply(level - 1).getCash()) / 2L; // only
                                                                                                              // the
                                                                                                              // cost of
                                                                                                              // the
                                                                                                              // latest
                                                                                                              // upgrade
                                                                                                              // is
                                                                                                              // accounted
                                                                                                              // for
        if (level == 1) {
            upgradesCost = 0;
        }
        return purchaseCost + upgradesCost;
    }

    public static boolean areTilesBlocking(UpdateData data, int x, int y) {
        World world = data.getWorld();
        if (world == null) {
            return true;
        }
        TileType at = world.getTile(x, y, 1);
        TileType below = world.getTile(x, y, 0);
        if (!TileType.isFloor(below)) {
            return true;
        }
        TileData tdata = world.getTileData(x, y, 1);
        // System.out.println("TileType: " + at + ", TileData: " + tdata);
        return at == TileType.WALL || tdata != null;
    }

    public static boolean canApplyMaterial(Material material, World world, TeamInventory testInventory, int x, int y,
            int team) {
        if (testInventory == null || material == null) {
            return false;
        }
        if (material.materialType == MaterialType.RESOURCE) {
            return false;
        }
        TowerEntity at = towerAt(world, x, y);
        if (at == null) {
            return false;
        }
        if (at.getTeam() != team) {
            return false;
        }
        if (!Cost.material(material, 1).canPurchase(testInventory)) {
            return false;
        }
        return at.canApplyMaterial(material);
    }

    public static void applyMaterial(Material material, GameManager manager, TeamInventory inventory, int x, int y,
            int team) {
        if (!canApplyMaterial(material, manager.getWorld(), inventory, x, y, team)) {
            return;
        }
        TowerEntity at = towerAt(manager.getWorld(), x, y);
        boolean appliedSuccessfully = at.applyMaterial(material);
        inventory.removeMaterial(material, 1);
        at.updatePresenceOnClients(manager);
    }

    /**
     * Attempts to place a GameType in the world.
     * Will throw an exception if the placement is invalid.
     * Can be called on the client and server side.
     * 
     * @param data Called from the server side and the client side.
     * @return True if the placement was successful.
     */
    public static boolean canPlace(StoreOption option, int x, int y, int playerX, int playerY, int team, UpdateData data) throws StoreException {
        Objects.requireNonNull(option);
        Objects.requireNonNull(data);

        Cost cost = option.cost;
        TeamInventory teamInventory = data.getInventory(team);
        if (!cost.canPurchase(teamInventory)) {
            // Not enough money
            throw new StoreException(StoreExceptionType.TOO_POOR, "Not enough money or resources");
        }

        if (option instanceof TowerOption) {
            // Check if the tower can be placed at the target position.
            TowerOption towerOption = (TowerOption) option;
            TowerEntity towerAt = towerAt(data.getWorld(), x, y);

            if (towerAt != null) {
                // Tower exists at the position
                throw new StoreException(StoreExceptionType.INVALID_PLACEMENT, "Tower exists at the position");
            }
            if (areTilesBlocking(data, x, y)) {
                // Tiles are blocking
                throw new StoreException(StoreExceptionType.INVALID_PLACEMENT, "Tiles are blocking");
            }
        }
        if (option instanceof ConsumableOption) {
            ConsumableOption consumableOption = (ConsumableOption) option;
            Vector2 placePosition = new Vector2(x, y);
            Vector2 playerPosition = new Vector2(playerX, playerY);
            
            float distance = placePosition.dst(playerPosition);
            if (distance > SplashAOE.MAX_THROW_DISTANCE) {
                // Too far away
                throw new StoreException(StoreExceptionType.INVALID_PLACEMENT, "Too far away from target");
            }
        }

        return true;
    }

    public static class StoreException extends Exception {
        StoreExceptionType type;

        public StoreException(StoreExceptionType type, String message) {
            super(message);
            this.type = type;
        }

        public enum StoreExceptionType {
            INVALID_PLACEMENT,
            TOO_POOR,
        }
    }

}
