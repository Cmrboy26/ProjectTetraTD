package net.cmr.rtd.game.world;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.DataBuffer;

import net.cmr.rtd.game.world.entities.effects.EntityEffects;
import net.cmr.rtd.game.world.tile.Tile;
import net.cmr.util.UUIDUtils;

public abstract class Entity extends GameObject {

    private UUID entityUUID; // Unique ID for the entity. (WILL NEVER CHANGE, EVEN BETWEEN RELOADS)
    private World world;
    private EntityEffects effects;

    /**
     * Constructs a new entity with the given type.
     * The given type must be created in {@link GameType}.
     * @param type the type of the entity
     */
    protected Entity(GameType type) {
        super(type);
        this.entityUUID = UUID.randomUUID();
        this.effects = new EntityEffects(this);
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

    @Override
    public void update(float delta, UpdateData data) {
        super.update(delta, data);
        effects.update(delta);
    }
    
    protected abstract void serializeEntity(DataBuffer buffer) throws IOException;
    protected abstract void deserializeEntity(GameObject object, DataInputStream input) throws IOException;

    @Override
    protected final void serialize(DataBuffer buffer) throws IOException {
        //buffer.writeUTF(entityUUID.toString());
        UUIDUtils.serializeUUID(buffer, entityUUID);
        effects.serialize(buffer);
        serializeEntity(buffer);
    }

    @Override
    protected final void deserialize(GameObject object, DataInputStream input) throws IOException {
        Entity entity = (Entity) object;
        //entity.entityUUID = UUID.fromString(input.readUTF());
        entity.entityUUID = UUIDUtils.deserializeUUID(input);
        entity.effects = EntityEffects.deserialize(entity, input);
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
    public Vector2 getPosition() { return position; }
    public EntityEffects getEffects() { return effects; }

    public void move(float dx, float dy) { position.add(dx, dy); }
    public void move(float x, float y, float delta) { position.add(x * delta, y * delta); }

    @Override
    public String toString() {
        return "Entity [entityUUID=" + entityUUID + ", world=" + world + "]";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Entity && ((Entity) obj).entityUUID.equals(entityUUID);
    }
    
}
