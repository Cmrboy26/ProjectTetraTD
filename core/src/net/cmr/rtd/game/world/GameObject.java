package net.cmr.rtd.game.world;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Objects;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.DataBuffer;

import net.cmr.rtd.game.world.entities.BasicEnemy;
import net.cmr.rtd.game.world.entities.HealerEnemy;
import net.cmr.rtd.game.world.entities.Player;
import net.cmr.rtd.game.world.entities.Projectile;
import net.cmr.rtd.game.world.entities.towers.DrillTower;
import net.cmr.rtd.game.world.entities.towers.FireTower;
import net.cmr.rtd.game.world.entities.towers.GemstoneExtractor;
import net.cmr.rtd.game.world.entities.towers.IceTower;
import net.cmr.rtd.game.world.entities.towers.ShooterTower;
import net.cmr.util.Log;

public abstract class GameObject {

    public final GameType type;
    Vector2 position;
    private static HashMap<Integer, GameType> types = new HashMap<>();

    public enum GameType {

        WORLD(World.class),
        PLAYER(Player.class),
        BASIC_ENEMY(BasicEnemy.class),
        HEALER_ENEMY(HealerEnemy.class),
        
        ICE_TOWER(IceTower.class),
        FIRE_TOWER(FireTower.class),
        SHOOTER_TOWER(ShooterTower.class),
        DRILL_TOWER(DrillTower.class),
        GEMSTONE_EXTRACTOR(GemstoneExtractor.class),

        PROJECTILE(Projectile.class), 
        ;

        private final Class<? extends GameObject> clazz;
        GameType(Class<? extends GameObject> clazz) {
            this.clazz = clazz;
            types.put(getID(), this);
        }

        public Class<? extends GameObject> getGameObjectClass() {
            return clazz;
        }

        /**
         * Creates a shell of an entity. This should primarily be used to render holograms of the entity.
         * @return The entity.
         */
        public Entity createEntity() {
            try {
                return (Entity) clazz.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                Log.error("Failed to create entity", e);
            }
            return null;
        }
        public int getID() {
            return name().hashCode();
        }
        public static GameType getType(int id) {
            return types.get(id);
        }
    }

    protected GameObject(GameType type) {
        this.type = type;
        this.position = new Vector2();
    }

    /**
     * Called on the server/manager and client side.
     * This will create the game object.
     * When the client receives the game object, it will call this method.
     */
    public void create() {

    }

    /**
     * Called on the server/manager and client side.
     * This will remove the game object.
     * When the client is notified that the game object has
     * been removed, it will call this method.
     */
    public void remove() {

    }

    /**
     * Called on the server/manager and client side.
     * This will update the game object.
     * @param delta The time since the last update.
     */
    public void update(float delta, UpdateData data) {

    }

    /**
     * Called on the client side.
     * This will render the game object.
     * @param delta The time since the last render.
     */
    public void render(UpdateData data, Batch batch, float delta) {
        
    }
    
    public void setPosition(float x, float y) {
        this.position.set(x, y);
    }
    public void setPosition(Vector2 position) {
        this.position = position;
    }
    public void translate(float x, float y) {
        this.position.add(x, y);
    }
    public void translate(Vector2 translation) {
        this.position.add(translation);
    }
    public Vector2 getPosition() { return position; }

    public float getX() { return position.x; }
    public float getY() { return position.y; }

    /**
     * Serialization:
     * int id, float x, float y, GameObject serializedObject
     * 
     * @param object
     * @return
     */
    public static byte[] serializeGameObject(GameObject object) {
        Objects.requireNonNull(object);
        DataBuffer buffer = new DataBuffer();
        try {
            buffer.writeInt(object.type.getID());
            buffer.writeFloat(object.position.x);
            buffer.writeFloat(object.position.y);
            object.serialize(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer.toArray();
    }
    protected abstract void serialize(DataBuffer buffer) throws IOException;

    /**
     * Deserialization:
     * int id, float x, float y, GameObject serializedObject
     * 
     * @param data
     * @return
     */
    public static GameObject deserializeGameObject(byte[] data) {
        DataInputStream input = new DataInputStream(new ByteArrayInputStream(data));
        try {
            int id = input.readInt();
            GameType type = GameType.getType(id);
            if (type == null) {
                throw new IOException("Invalid game object type: " + id);
            }
            float x = input.readFloat();
            float y = input.readFloat();
            GameObject object = type.getGameObjectClass().getConstructor().newInstance();
            object.setPosition(x, y);
            object.deserialize(object, input);

            input.close();
            return object;
        } catch (IOException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            Log.error("Illegal argument exception (make sure the GameObject has a no-args constructor)", e);
        }
        return null;
    }

    /**
     * Used to deserialize the data inside of the DataInputStream
     * and store it in the GameObject. 
     * @param object The object to deserialize into.
     * @param input The input stream to read from.
     * @throws IOException If an I/O error occurs.
     */
    protected abstract void deserialize(GameObject object, DataInputStream input) throws IOException;

    @Override
    public String toString() {
        return "[GameObject]{type=" + type.name()
         + ", position=" + position + "}";
    }

}
