package net.cmr.rtd.game.world;

import net.cmr.rtd.game.packets.GameObjectPacket;
import net.cmr.rtd.game.world.entities.BasicEnemy;
import net.cmr.rtd.game.world.entities.EnemyEntity;
import net.cmr.rtd.game.world.tile.Tile;

/**
 * The enemy factory is used to create enemies on the specified team and start position.
 */
public class EnemyFactory {

    private int team;
    private int tileX;
    private int tileY;
    private UpdateData data;
    private World world;

    /**
     * Constructs a new enemy factory with the given team and start position.
     * @param team the team of the enemies
     * @param tileX the x position of the start tile
     * @param tileY the y position of the start tile
     * @param world the world to spawn the enemies in
     */
    public EnemyFactory(int team, int tileX, int tileY, UpdateData data) {
        this.team = team;
        this.tileX = tileX;
        this.tileY = tileY;
        this.data = data;
        this.world = data.getWorld();
    }

    private void addEnemy(EnemyEntity entity) {
        entity.setHealth(entity.getMaxHealth());
        entity.setPosition(tileX * Tile.SIZE + Tile.SIZE / 2, tileY * Tile.SIZE + Tile.SIZE / 2);
        world.addEntity(entity);
        if (data.isServer()) {
            GameObjectPacket packet = new GameObjectPacket(entity, false);
            data.getManager().sendPacketToAll(packet);
        }
    }

    private void createBasicEnemy(String displayType, int maxHealth) {
        BasicEnemy enemy = new BasicEnemy(team, displayType, maxHealth);
        addEnemy(enemy);
    }

    public void createBasicEnemyOne() { createBasicEnemy("basic1", 5); }
    public void createBasicEnemyTwo() { createBasicEnemy("basic2", 10); }

}
