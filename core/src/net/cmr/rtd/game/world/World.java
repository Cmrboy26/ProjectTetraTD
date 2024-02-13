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
import com.badlogic.gdx.math.Rectangle;
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
                // TODO: Implement proper collision detection (consider using AABB or SAT)
                // https://developer.mozilla.org/en-US/docs/Games/Techniques/2D_collision_detection
                // https://www.sevenson.com.au/programming/sat/ TRY THIS!

                // NOTE: Everything is a rectangle, so we can use AABB for now.
                // Get the tile
                int tileX = Entity.getTileX(entity.getX() + tempVelocityX) + x;
                int tileY = Entity.getTileY(entity.getY() + tempVelocityY) + y;
                TileType type = getTile(tileX, tileY, 1);
                if (!type.isSolid()) {
                    continue;
                }

                // Check to see if the entity will collide with the tile
                Rectangle entityBounds = new Rectangle(position.x + tempVelocityX, position.y + tempVelocityY, entity.getWidth(), entity.getHeight());
                Rectangle tileBounds = new Rectangle(tileX * Tile.SIZE, tileY * Tile.SIZE, Tile.SIZE, Tile.SIZE);

                // Double check to see if this vector is correct
                Vector2 tileCenterToEntityCenter = new Vector2(
                    (entityBounds.x + entityBounds.width / 2) - (tileBounds.x + tileBounds.width / 2),
                    (entityBounds.y + entityBounds.height / 2) - (tileBounds.y + tileBounds.height / 2)
                );
                tileCenterToEntityCenter.nor();
                Vector2[] directions = new Vector2[] {
                    new Vector2(0, 1), new Vector2(1, 0), new Vector2(0, -1), new Vector2(-1, 0)
                };

                // If calculated is false after the loop, then the player is
                // EXACTLY in a corner of a tile (unlikely)
                boolean calculated = false;
                for (int i = 0; i < directions.length; i++) {
                    float dot = tileCenterToEntityCenter.dot(directions[i]);
                    if (dot > Math.sqrt(2d) / 2d) {
                        // The specified direction vector is the face the entity is colliding with
                        // Snap the opposite face of the entity to this side of the tile
                        // Assume that the x and y coordinates of the tile and entity are the left bottom corner
                        // of the tile and the bottom left corner of the entity, respectively.
                        calculated = true;
                        if (directions[i].x == 0) {
                            // The entity is colliding with the top or bottom face of the tile
                            if (directions[i].y > 0) {
                                // The entity is colliding with the top face of the tile
                                tempVelocityY = tileBounds.y + tileBounds.height - position.y;
                            } else {
                                // The entity is colliding with the bottom face of the tile
                                tempVelocityY = tileBounds.y - entityBounds.height - position.y;
                            }
                        } else {
                            // The entity is colliding with the left or right face of the tile
                            if (directions[i].x > 0) {
                                // The entity is colliding with the right face of the tile
                                tempVelocityX = tileBounds.x + tileBounds.width - position.x;
                            } else {
                                // The entity is colliding with the left face of the tile
                                tempVelocityX = tileBounds.x - entityBounds.width - position.x;
                            }
                        }
                    }
                }
                if (!calculated) {
                    Log.warning("CORNER COLLISION DETECTED!! (Unlikely to happen)");
                }


                /*final int steps = 5; // Higher steps = more accurate collision detection
                final float threshold = 0.5f;
                Rectangle tileBounds = new Rectangle(tileX, tileY, Tile.SIZE, Tile.SIZE);
                for (int i = 0; i < steps; i++) {
                    boolean lastIteration = i == steps - 1;
                    float stepIncrement = 1f / steps;
                    float vx = i * tempVelocityX * stepIncrement;
                    float vy = i * tempVelocityY * stepIncrement;
                    
                    // X collision detection
                    Rectangle entityXBounds = new Rectangle(position.x + vx, position.y, entity.getWidth(), entity.getHeight());
                    if (Math.abs(vx) > threshold && entityXBounds.overlaps(tileBounds)) {
                        if (!lastIteration) {
                            tempVelocityX = vx;
                        } else {
                            tempVelocityX = vx - stepIncrement;
                        }
                    }

                    // Y collision detection
                    Rectangle entityYBounds = new Rectangle(position.x, position.y + vy, entity.getWidth(), entity.getHeight());
                    if (Math.abs(vy) > threshold && entityYBounds.overlaps(tileBounds)) {
                        if (!lastIteration) {
                            tempVelocityY = vy;
                        } else {
                            tempVelocityY = vy - stepIncrement;
                        }
                    }
                }*/

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
