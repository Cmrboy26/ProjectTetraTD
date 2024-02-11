package net.cmr.rtd.game.world;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.DataBuffer;

import net.cmr.rtd.game.GameManager;
import net.cmr.rtd.game.world.entities.Player;
import net.cmr.rtd.game.world.entities.WorldSerializationExempt;
import net.cmr.rtd.game.world.tile.Tile;
import net.cmr.rtd.game.world.tile.Tile.TileType;
import net.cmr.util.Log;

/**
 * The world class will handle the game world
 * and all of the game objects.
 * The main world will be stored in {@link GameManager},
 * and the manager will send data about the world to
 * the client for them to reconstruct.
 */
public class World extends GameObject {

    public static final int DEFAULT_WORLD_SIZE = 10;
    public static final int LAYERS = 3;

    private int worldSize;
    private HashMap<UUID, Entity> entities; // <Entity ID, Entity>
    private TileType[][][] tiles;

    public World() {
        super(GameType.WORLD);
        this.worldSize = DEFAULT_WORLD_SIZE;
        this.entities = new HashMap<>();
        this.tiles = new TileType[DEFAULT_WORLD_SIZE][DEFAULT_WORLD_SIZE][5];
        
        for (int x = 0; x < worldSize; x++) {
            for (int y = 0; y < worldSize; y++) {
                tiles[x][y][0] = TileType.FLOOR;
            }
        }
        for (int x = 0; x < worldSize; x++) {
            tiles[x][worldSize-1][1] = TileType.WALL;
            tiles[x][0][1] = TileType.WALL;
        }
        for (int y = 0; y < worldSize; y++) {
            tiles[0][y][1] = TileType.WALL;
            tiles[worldSize-1][y][1] = TileType.WALL;
        }
    }

    @Override
    public void update(float delta, UpdateData data) {
        super.update(delta, data);
        for (Entity entity : entities.values()) {
            entity.update(delta, data);
        }
    }

    public void addEntity(Entity entity) {
        Objects.requireNonNull(entity);
        entities.put(entity.getID(), entity);
        entity.setWorld(this);
        entity.create();
    }

    public void removeEntity(Entity entity) {
        Objects.requireNonNull(entity);
        removeEntity(entity);
    }

    public void removeEntity(UUID id) {
        Entity removed = entities.remove(id);
        if (removed == null) return;
        removed.setWorld(null);
        removed.remove();
    }

    public HashMap<UUID, Entity> getEntityMap() { return entities; }
    public Set<UUID> getEntityIDs() { return entities.keySet(); }
    public Entity getEntity(UUID id) { return entities.get(id); }
    public Collection<Entity> getEntities() { return entities.values(); }

    public TileType getTile(int x, int y, int z) {
        if (x < 0 || y < 0 || x >= worldSize || y >= worldSize) {
            return null;
        }
        if (z < 0 || z >= LAYERS) {
            return null;
        }

        return tiles[x][y][z];
    }

    public void setTile(int x, int y, int z, TileType type) {
        tiles[x][y][z] = type;
    }

    public int getWorldSize() {
        return worldSize;
    }

    public void serializePlayerData(DataBuffer buffer) throws IOException {
        ArrayList<Entity> players = new ArrayList<>();
        for (Entity entity : entities.values()) {
            if (entity instanceof Player) {
                players.add(entity);
            }
        }
        buffer.writeInt(players.size());
        for (Entity player : players) {
            byte[] data = GameObject.serializeGameObject(player);
            buffer.writeInt(data.length);
            buffer.write(data);
        }
    }

    public HashMap<String, Player> deserializePlayerData(DataInputStream input) throws IOException {
        HashMap<String, Player> players = new HashMap<>();
        int playerCount = input.readInt();
        for (int i = 0; i < playerCount; i++) {
            int length = input.readInt();
            byte[] data = new byte[length];
            input.read(data);
            Player player = (Player) GameObject.deserializeGameObject(data);
            players.put(player.getName(), player);
        }
        return players;
    }

    public void setPlayerData(HashMap<String, Player> players) {
        for (Player player : players.values()) {
            Log.info("Player " + player.getName() + " was saved.");
        }
    }

    @Override
    protected void serialize(DataBuffer buffer) throws IOException {
        int tempWorldSize = worldSize;
        // Serialized WorldSize: AXXXXXXX XXXXXXXX XXXXXXXX XXXXXXXX.
        // If A is true, then the world size is the default size.
        // (If worldsize is negative, then the world size is the default size.)
        if (worldSize == DEFAULT_WORLD_SIZE) {
            tempWorldSize *= -1;
        }
        buffer.writeInt(tempWorldSize);

        for (int x = 0; x < worldSize; x++) {
            for (int y = 0; y < worldSize; y++) {
                for (int z = 0; z < LAYERS; z++) {
                    if (tiles[x][y][z] == null) {
                        buffer.writeInt(Tile.NULL_TILE_ID);
                    } else {
                        buffer.writeInt(tiles[x][y][z].getID());
                    }
                }
            }
        }

        // Check if any entity is exempt from serialization.
        ArrayList<Entity> validEntities = new ArrayList<>();
        for (Entity entity : entities.values()) {
            if (entity.getClass().isAnnotationPresent(WorldSerializationExempt.class)) {
                continue;
            }
            validEntities.add(entity);
        }

        int entityCount = validEntities.size();
        buffer.writeInt(entityCount);
        for (Entity entity : validEntities) {
            byte[] data = GameObject.serializeGameObject(entity);
            buffer.writeInt(data.length);
            buffer.write(data);
        }
    }

    /**
     * Deserializes the world from the given input stream.
     * @implNote This method does NOT save/load players.
     * @param object The object to deserialize into.
     * @param input The input stream to read from.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    protected void deserialize(GameObject object, DataInputStream input) throws IOException {
        World world = (World) object;

        // Serialized WorldSize: AXXXXXXX XXXXXXXX XXXXXXXX XXXXXXXX.
        // If A is true, then the world size is the default size.
        // (If worldsize is negative, then the world size is the default size.)
        int tempWorldSize = input.readInt();
        if (tempWorldSize < 0) {
            worldSize = DEFAULT_WORLD_SIZE;
        } else {
            worldSize = tempWorldSize;
        }

        for (int x = 0; x < worldSize; x++) {
            for (int y = 0; y < worldSize; y++) {
                for (int z = 0; z < LAYERS; z++) {
                    world.tiles[x][y][z] = TileType.getType(input.readInt());
                }
            }
        }
        int entityCount = input.readInt();
        for (int i = 0; i < entityCount; i++) {
            int length = input.readInt();
            byte[] data = new byte[length];
            input.read(data);
            Entity entity = (Entity) GameObject.deserializeGameObject(data);
            world.addEntity(entity);
        }
    }

    @Override
    public void render(Batch batch, float delta) {
        // TODO: Implement smarter tile rendering (with proper entity support)
        for (int x = 0; x < worldSize; x++) {
            for (int y = 0; y < worldSize; y++) {
                for (int z = 0; z < LAYERS; z++) {
                    if (tiles[x][y][z] == null) {
                        continue;
                    }
                    TileType type = tiles[x][y][z];
                    if (type == null) {
                        continue;
                    }
                    Tile tile = type.getTile();
                    if (tile == null) {
                        continue;
                    }
                    tile.render(batch, delta, this, x, y, z);
                }
            }
        }
        for (Entity entity : entities.values()) {
            entity.render(batch, delta);
        }
        super.render(batch, delta);
    }

    /**
     * Moves the entity and handles with collision.
     * @param entity The entity to move.
     * @param delta The time since the last frame.
     * @param velocity The velocity of the entity.
     */
    public void moveHandleCollision(Entity entity, float delta, Vector2 velocity) {
        Vector2 position = entity.getPosition();
        float tempVelocityX = velocity.x;
        float tempVelocityY = velocity.y;
        final int collisionDetectionRange = 2;
        for (int x = -collisionDetectionRange; x <= collisionDetectionRange; x++) {
            for (int y = -collisionDetectionRange; y <= collisionDetectionRange; y++) {
                // TODO: Make tileX and tileY actually correct
                int tileX = (int) (position.x + x);
                int tileY = (int) (position.y + y);
                TileType type = getTile(tileX, tileY, 1);
                if (type == null) {
                    continue;
                }
                if (type.getSolid()) {
                    if (x == 0) {
                        tempVelocityY = 0;
                    } else if (y == 0) {
                        tempVelocityX = 0;
                    }
                }
            }
        }
        position.x += tempVelocityX * delta;
        position.y += tempVelocityY * delta;
    }

    @Override
    public String toString() {
        return "[World]{worldSize=" + worldSize + ", entities=" + entities.size() + "}";
    }

}
