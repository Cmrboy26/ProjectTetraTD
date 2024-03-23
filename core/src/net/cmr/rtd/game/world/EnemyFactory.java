package net.cmr.rtd.game.world;

import java.util.function.Consumer;

import net.cmr.rtd.game.packets.GameObjectPacket;
import net.cmr.rtd.game.world.entities.BasicEnemy;
import net.cmr.rtd.game.world.entities.EnemyEntity;
import net.cmr.rtd.game.world.entities.effects.FireEffect;
import net.cmr.rtd.game.world.entities.effects.SlownessEffect;
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

    public enum EnemyType {
        BASIC_ONE((factory) -> factory.createBasicEnemyOne()),
        BASIC_TWO((factory) -> factory.createBasicEnemyTwo()),
        BASIC_THREE((factory) -> factory.createBasicEnemyThree()),
        BASIC_FOUR((factory) -> factory.createBasicEnemyFour()),
        ;

        private Consumer<EnemyFactory> factory;

        EnemyType(Consumer<EnemyFactory> factory) {
            this.factory = factory;
        }

        public void createEnemy(EnemyFactory factory) {
            this.factory.accept(factory);
        }

        public static EnemyType fromString(String type) {
            for (EnemyType t : values()) {
                if (t.name().equalsIgnoreCase(type)) {
                    return t;
                }
            }
            return null;
        }
    }

    /**
     * Creates an enemy of the given type and adds it to the world at the position this factory is associated with.
     * @param type the type of enemy to create
     */
    public void createEnemy(EnemyType type) {
        type.createEnemy(this);
    }

    private void send(EnemyEntity entity) {
        entity.setHealth(entity.getMaxHealth());
        entity.setPosition(tileX * Tile.SIZE + Tile.SIZE / 2, tileY * Tile.SIZE + Tile.SIZE / 2);
        world.addEntity(entity);
        if (data.isServer()) {
            GameObjectPacket packet = new GameObjectPacket(entity, false);
            data.getManager().sendPacketToAll(packet);
        }
    }

    private BasicEnemy createBasicEnemy(String displayType, int maxHealth, float speed) {
        BasicEnemy enemy = new BasicEnemy(team, displayType, maxHealth, speed);
        return enemy;
    }


    private void createBasicEnemyOne() { send(createBasicEnemy("basic1", 5, 1.5f)
                                            .immuneTo(FireEffect.class)); }
    private void createBasicEnemyTwo() { send(createBasicEnemy("basic2", 10, 1f)); }
    private void createBasicEnemyThree() { send(createBasicEnemy("basic3", 25, 1f) 
                                            .immuneTo(FireEffect.class)
                                            .immuneTo(SlownessEffect.class)); }
    private void createBasicEnemyFour() { send(createBasicEnemy("basic4", 125, .9f)
                                            .immuneTo(SlownessEffect.class)); }
    private void createHealerEnemyOne() { 
        /* 
        TODO: Implement
        ALSO, healer should not be immune to anything. Slowness could be a downside for the player, so they must consider it when using the slowness tower.
        */
    }
}
