package net.cmr.rtd.game.world;

import java.util.function.Consumer;

import net.cmr.rtd.game.packets.GameObjectPacket;
import net.cmr.rtd.game.world.GameObject.GameType;
import net.cmr.rtd.game.world.entities.BasicEnemy;
import net.cmr.rtd.game.world.entities.EnemyEntity;
import net.cmr.rtd.game.world.entities.HealerEnemy;
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
        this(team, tileX, tileY, data.getWorld());
        this.data = data;
    }

    public EnemyFactory(int team, int tileX, int tileY, World world) {
        this.team = team;
        this.tileX = tileX;
        this.tileY = tileY;
        this.world = world;
    }

    public enum EnemyType {
        BASIC_ONE(4, EnemyFactory::createBasicEnemyOne),
        BASIC_TWO(10, EnemyFactory::createBasicEnemyTwo),
        BASIC_THREE(25, EnemyFactory::createBasicEnemyThree),
        BASIC_FOUR(125, EnemyFactory::createBasicEnemyFour),
        BASIC_FIVE(750, EnemyFactory::createBasicEnemyFive),

        HEALER_ONE(200, EnemyFactory::createHealerEnemyOne),
        ;

        private Consumer<EnemyFactory> factory;
        private int health;

        EnemyType(int health, Consumer<EnemyFactory> factory) {
            this.health = health;
            this.factory = factory;
        }

        public void createEnemy(EnemyFactory factory) {
            this.factory.accept(factory);
        }
        public int getHealth() {
            return health;
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
        entity.totalEnemiesInWave = world.getWavesData().getTotalEnemiesInWave(world.getWave());
        world.addEntity(entity);
        if (data != null && data.isServer()) {
            GameObjectPacket packet = new GameObjectPacket(entity, false);
            data.getManager().sendPacketToAll(packet);
        }
    }

    private BasicEnemy createBasicEnemy(String displayType, EnemyType type, float speed) {
        BasicEnemy enemy = new BasicEnemy(team, displayType, type.health, speed, type);
        return enemy;
    }
    private HealerEnemy createHealerEnemy(String displayType, EnemyType type, float speed) {
        HealerEnemy enemy = new HealerEnemy(team, displayType, type.health, speed, type);
        return enemy;
    }


    private void createBasicEnemyOne() { send(createBasicEnemy("basic1", EnemyType.BASIC_ONE, 1.5f)
                                            .immuneTo(FireEffect.class)); }
    private void createBasicEnemyTwo() { send(createBasicEnemy("basic2", EnemyType.BASIC_TWO, 1f)); }
    private void createBasicEnemyThree() { send(createBasicEnemy("basic3", EnemyType.BASIC_THREE, 1f) 
                                            .immuneTo(FireEffect.class)); }
    private void createBasicEnemyFour() { send(createBasicEnemy("basic4", EnemyType.BASIC_FOUR, .8f)
                                            .immuneTo(SlownessEffect.class)); }
    private void createBasicEnemyFive() { send(createBasicEnemy("basic5", EnemyType.BASIC_FIVE, .5f));}
    private void createHealerEnemyOne() { send(createHealerEnemy("healer1", EnemyType.HEALER_ONE, .7f));}
    private void createProtectorateEnemyOne() {
        /*
        TODO: Implement
        A very slow enemy that makes nearby enemies immune to all effects.
         */
    }
}
