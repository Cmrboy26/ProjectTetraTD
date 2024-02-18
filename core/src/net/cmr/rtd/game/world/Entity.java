package net.cmr.rtd.game.world;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.DataBuffer;

import net.cmr.rtd.game.world.tile.Tile;

public abstract class Entity extends GameObject {

    private UUID entityUUID; // Unique ID for the entity. (WILL NEVER CHANGE, EVEN BETWEEN RELOADS)
    private World world;

    /**
     * Constructs a new entity with the given type.
     * The given type must be created in {@link GameType}.
     * @param type the type of the entity
     */
    protected Entity(GameType type) {
        super(type);
        this.entityUUID = UUID.randomUUID();
    }

    @Override
    public void create() {
        super.create();
    }

    @Override
    public void remove() {
        super.remove();
    }

    public void removeFromWorld() {
        world.removeEntity(this);
    }

    public abstract void update(float delta, UpdateData data);
    
    protected abstract void serializeEntity(DataBuffer buffer) throws IOException;
    protected abstract void deserializeEntity(GameObject object, DataInputStream input) throws IOException;

    @Override
    protected final void serialize(DataBuffer buffer) throws IOException {
        buffer.writeUTF(entityUUID.toString());
        serializeEntity(buffer);
    }

    @Override
    protected final void deserialize(GameObject object, DataInputStream input) throws IOException {
        Entity entity = (Entity) object;
        entity.entityUUID = UUID.fromString(input.readUTF());
        deserializeEntity(object, input);
    }

    public UUID getID() { return entityUUID; }
    public void setID(UUID id) { this.entityUUID = id; }
    public void setWorld(World world) { this.world = world; }
    public World getWorld() { return world; }
    public Rectangle getBounds() { return new Rectangle(getX(), getY(), Tile.SIZE, Tile.SIZE); }

    public float getRenderOffset() { return 0; }

    public static int getTileX(float x) { return (int) Math.floor(x/Tile.SIZE); }
    public static int getTileY(float y) { return (int) Math.floor(y/Tile.SIZE); }
    public static int getTileX(Entity entity) { return getTileX(entity.getX()); }
    public static int getTileY(Entity entity) { return getTileY(entity.getY()); }

    @Override
    public String toString() {
        return "Entity [entityUUID=" + entityUUID + ", world=" + world + "]";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Entity && ((Entity) obj).entityUUID.equals(entityUUID);
    }
    
}
