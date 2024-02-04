package net.cmr.rtd.game.world;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.DataBuffer;

import net.cmr.util.Log;

public abstract class GameObject {

    final GameType type;
    Vector2 position;
    private static HashMap<Integer, GameType> types = new HashMap<>();

    public enum GameType {

        WORLD(World.class),
        ;

        private final Class<? extends GameObject> clazz;
        GameType(Class<? extends GameObject> clazz) {
            this.clazz = clazz;
            types.put(getID(), this);
        }

        public Class<? extends GameObject> getGameObjectClass() {
            return clazz;
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
    public void update(float delta) {

    }

    /**
     * Called on the client side.
     * This will render the game object.
     * @param delta The time since the last render.
     */
    public void render(Batch batch, float delta) {
        
    }
    
    public void setPosition(float x, float y) {
        this.position.set(x, y);
    }
    public void setPosition(Vector2 position) {
        this.position = position;
    }
    public Vector2 getPosition() { return position; }

    public static byte[] serializeGameObject(GameObject object) {
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

}
