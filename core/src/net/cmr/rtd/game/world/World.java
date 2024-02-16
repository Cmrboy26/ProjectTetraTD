package net.cmr.rtd.game.world;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.DataBuffer;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import net.cmr.rtd.game.GameManager;
import net.cmr.rtd.game.world.entities.Player;
import net.cmr.rtd.game.world.entities.WorldSerializationExempt;
import net.cmr.rtd.game.world.tile.Tile;
import net.cmr.rtd.game.world.tile.Tile.TileType;
import net.cmr.rtd.game.world.tile.TileData;
import net.cmr.util.CMRGame;
import net.cmr.util.Log;

/**
 * The world class will handle the game world
 * and all of the game objects.
 * The main world will be stored in {@link GameManager},
 * and the manager will send data about the world to
 * the client for them to reconstruct.
 */
public class World extends GameObject {

    public static final int DEFAULT_WORLD_SIZE = 32;
    public static final int LAYERS = 3;

    private int worldSize;
    private HashMap<UUID, Entity> entities; // <Entity ID, Entity>
    private TileType[][][] tiles;
    private HashMap<Point3D, TileData> tileDataMap;

    public static class Point3D {
        public int x, y, z;
        public Point3D(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
        public Point3D() {
            this(0, 0, 0);
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, z);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (obj == this) return true;
            if (obj.getClass() != getClass()) return false;
            Point3D other = (Point3D) obj;
            return other.x == x && other.y == y && other.z == z;
        }

        @Override
        public String toString() {
            return "Point3D{x=" + x + ", y=" + y + ", z=" + z + "}";
        }
    }

    public World() {
        super(GameType.WORLD);
        this.worldSize = DEFAULT_WORLD_SIZE;
        this.entities = new HashMap<>();
        this.tiles = new TileType[DEFAULT_WORLD_SIZE][DEFAULT_WORLD_SIZE][LAYERS];
        this.tileDataMap = new HashMap<>();
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
        setTileData(x, y, z, null);
    }
    public void setTileData(int x, int y, int z, TileData data) {
        if (data == null) {
            tileDataMap.remove(new Point3D(x, y, z));
            return;
        }
        tileDataMap.put(new Point3D(x, y, z), data);
    }
    public TileData getTileData(int x, int y, int z) {
        return tileDataMap.get(new Point3D(x, y, z));
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

        // Serialize entity data.
        int entityCount = validEntities.size();
        buffer.writeInt(entityCount);
        for (Entity entity : validEntities) {
            byte[] data = GameObject.serializeGameObject(entity);
            buffer.writeInt(data.length);
            buffer.write(data);
        }

        // Serialize tile data.
        int tileDataCount = tileDataMap.size();
        buffer.writeInt(tileDataCount);
        Kryo kryo = new Kryo();
        TileData.registerKryo(kryo);
        Output output = new Output(buffer);
        for (Point3D point : tileDataMap.keySet()) {
            TileData data = tileDataMap.get(point);
            kryo.writeClassAndObject(output, point);
            kryo.writeClassAndObject(output, data);
        }
        output.flush();
        output.close();
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

        // Deserialize entity data.
        int entityCount = input.readInt();
        for (int i = 0; i < entityCount; i++) {
            int length = input.readInt();
            byte[] data = new byte[length];
            input.read(data);
            Entity entity = (Entity) GameObject.deserializeGameObject(data);
            world.addEntity(entity);
        }

        // Deserialize tile data.
        int tileDataCount = input.readInt();
        Kryo kryo = new Kryo();
        TileData.registerKryo(kryo);
        Input kryoInput = new Input(input);
        for (int i = 0; i < tileDataCount; i++) {
            Point3D point = (Point3D) kryo.readClassAndObject(kryoInput);
            TileData data = (TileData) kryo.readClassAndObject(kryoInput);
            world.tileDataMap.put(point, data);
        }
        kryoInput.close();
    }

    @Override
    public void render(Batch batch, float delta) {
        Color worldColor = Color.valueOf("#6663ff");
        // TODO: Implement smarter tile rendering (with proper entity support)
        batch.setColor(worldColor);
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

                    if (CMRGame.isDebug()) {
                        if (tileDataMap.containsKey(new Point3D(x, y, z))) {
                            TileData data = tileDataMap.get(new Point3D(x, y, z));
                            data.render(batch, x, y);
                        }
                    }
                }
            }
        }
        batch.setColor(Color.WHITE);
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
        linearCollisionHandle(entity, velocity.scl(delta));
    }

        
    final float steps = 3;
    final int detectionRadius = 3;

    private Vector2 linearCollisionHandle(Entity entity, Vector2 velocity) {

        for (int i = 0; i < steps; i++) {
            float velX = velocity.x / steps;
            Vector2 x = linearCollisionHandleStep(entity, new Vector2(velX, 0));
            if (x.x == 0) {
                break;
            }
            entity.position.add(x.x, 0);
        }

        for (int i = 0; i < steps; i++) {
            float velY = velocity.y / steps;
            Vector2 y = linearCollisionHandleStep(entity, new Vector2(0, velY));
            if (y.y == 0) {
                break;
            }
            entity.position.add(0, y.y);
        }

        return null;
    }

    private Vector2 linearCollisionHandleStep(Entity entity, Vector2 velocity) {
        int entityTX = Entity.getTileX(entity);
        int entityTY = Entity.getTileY(entity);

        Vector2 endVelocity = new Vector2(velocity);
        Rectangle entityBounds = entity.getBounds();
        entityBounds.x += velocity.x;
        entityBounds.y += velocity.y;

        for (int tx = -detectionRadius; tx < detectionRadius; tx++) {
            for (int ty = -detectionRadius; ty < detectionRadius; ty++) {
                int x = entityTX + tx;
                int y = entityTY + ty;
                TileType type = getTile(x, y, 1);
                if (type == null) {
                    continue;
                }
                if (!type.isSolid()) {
                    continue;
                }
                Rectangle bounds = new Rectangle(x*Tile.SIZE, y*Tile.SIZE, Tile.SIZE, Tile.SIZE);
                if (bounds.overlaps(entityBounds)) {
                    endVelocity.x = 0;
                    endVelocity.y = 0;
                    return endVelocity;
                }
            }
        }

        return endVelocity;
    }

    public void updateWorldSize(int size) {
        if (size < 1) {
            throw new IllegalArgumentException("World size must be at least 1.");
        }
        this.worldSize = size;

        // Copy the old tiles to the new tiles.
        TileType[][][] newTiles = new TileType[size][size][LAYERS];
        for (int x = 0; x < worldSize; x++) {
            for (int y = 0; y < worldSize; y++) {
                for (int z = 0; z < LAYERS; z++) {
                    if (x < tiles.length && y < tiles[x].length && z < tiles[x][y].length) {
                        newTiles[x][y][z] = tiles[x][y][z];
                    }
                }
            }
        }
        tiles = newTiles;
    }

    @Override
    public String toString() {
        return "[World]{worldSize=" + worldSize + ", entities=" + entities.size() + "}";
    }

}
