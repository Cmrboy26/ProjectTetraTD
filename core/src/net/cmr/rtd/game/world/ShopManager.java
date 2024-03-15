package net.cmr.rtd.game.world;

import net.cmr.rtd.game.GameManager;
import net.cmr.rtd.game.GamePlayer;
import net.cmr.rtd.game.packets.PurchaseItemPacket;
import net.cmr.rtd.game.world.entities.TowerEntity;
import net.cmr.rtd.game.world.entities.towers.FireTower;
import net.cmr.rtd.game.world.tile.Tile;
import net.cmr.rtd.game.world.tile.Tile.TileType;
import net.cmr.rtd.game.world.tile.TileData;
import net.cmr.rtd.screen.GameScreen;

/**
 * A static class that manages the shop.
 */
public class ShopManager {
    
    public static void makePurchase(GameScreen screen) {
        
    }

    public static void processPurchase(GameManager manager, GamePlayer player, PurchaseItemPacket packet) {
        if (player.getTeam() == -1) { return; }

        // Temporary code:
        // Purchase a fire tower.
        if (packet.option == PurchaseItemPacket.PurchaseOption.TOWER) {  
            TowerEntity towerAt = towerAt(manager, packet.x, packet.y);
            if (towerAt == null) {
                int cost = 10;
                TeamData data = manager.getTeam(player.getTeam());
                if (data.getMoney() < cost) {
                    System.out.println("NOT ENOUGH MONEY");
                    return;
                }
                if (areTilesBlocking(manager, packet.x, packet.y)) {
                    System.out.println("TILES BLOCKING");
                    return;
                }
                FireTower tower = new FireTower(player.getTeam());
                tower.setPosition((packet.x + .5f) * Tile.SIZE, (packet.y + .5f) * Tile.SIZE);
                manager.getWorld().addEntity(tower);
                tower.updatePresenceOnClients(manager);
                // Update the team's money.
                data.payMoney(cost);
                manager.updateTeamStats(player.getTeam());
            } else {
                System.out.println("TOWER AT THE POSITION ARLEADY");
            }
        }
    }

    /**
     * @return null if no tower exists at that spot, otherwise the tower is returned
     */
    private static TowerEntity towerAt(GameManager manager, int x, int y) {
        World world = manager.getWorld();
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

    public static boolean areTilesBlocking(GameManager manager, int x, int y) {
        World world = manager.getWorld();
        TileType at = world.getTile(x, y, 1);
        TileData data = world.getTileData(x, y, 1);
        return at == TileType.WALL || data != null;
    }

}
