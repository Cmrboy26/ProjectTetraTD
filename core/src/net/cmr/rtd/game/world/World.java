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
import com.badlogic.gdx.math.Shape2D;
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
        this.tiles = new TileType[DEFAULT_WORLD_SIZE][DEFAULT_WORLD_SIZE][LAYERS];
        
        for (int x = 1; x < worldSize-1; x++) {
            for (int y = 1; y < worldSize-1; y++) {
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
        
        tiles[worldSize/2][worldSize/2][1] = TileType.WALL;
        tiles[worldSize/2 + 1][worldSize/2 - 1][1] = TileType.WALL;
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

        Vector2 temporaryVelocity = new Vector2(velocity.x * delta, velocity.y * delta);
        Vector2 position = entity.getPosition();

        final int playerTileX = Entity.getTileX(entity.getX() + velocity.x * delta);
        final int playerTileY = Entity.getTileY(entity.getY() + velocity.y * delta);

        final int collisionDetectionRange = 3;
        final float threshold = 1f;

        for (int x = -collisionDetectionRange; x <= collisionDetectionRange; x++) {
            for (int y = -collisionDetectionRange; y <= collisionDetectionRange; y++) {
                int tileX = playerTileX + x;
                int tileY = playerTileY + y;
                TileType type = getTile(tileX, tileY, 1);

                if (type == null) {
                    continue;
                }
                if (!type.isSolid()) {
                    continue;
                }

                Rectangle tileBounds = new Rectangle(tileX * Tile.SIZE, tileY * Tile.SIZE, Tile.SIZE, Tile.SIZE);

                TileType above = getTile(tileX, tileY + 1, 1);
                TileType below = getTile(tileX, tileY - 1, 1);
                TileType left = getTile(tileX - 1, tileY, 1);
                TileType right = getTile(tileX + 1, tileY, 1);
                boolean tileAbove = above != null && above.isSolid();
                boolean tileBelow = below != null && below.isSolid();
                boolean tileLeft = left != null && left.isSolid();
                boolean tileRight = right != null && right.isSolid();
                if (tileAbove) { tileBounds.merge(new Rectangle(tileX * Tile.SIZE, (tileY + 1) * Tile.SIZE, Tile.SIZE, Tile.SIZE)); }
                if (tileBelow) { tileBounds.merge(new Rectangle(tileX * Tile.SIZE, (tileY - 1) * Tile.SIZE, Tile.SIZE, Tile.SIZE)); }
                if (tileLeft) { tileBounds.merge(new Rectangle((tileX - 1) * Tile.SIZE, tileY * Tile.SIZE, Tile.SIZE, Tile.SIZE)); }
                if (tileRight) { tileBounds.merge(new Rectangle((tileX + 1) * Tile.SIZE, tileY * Tile.SIZE, Tile.SIZE, Tile.SIZE)); }

                // Collision detection and response: use minkowski sums of the entity and the tile to ensure that the entity does not intersect with the tile
                // Allow the entity to "slide" along the tiles
                Rectangle entityBounds = entity.getBounds();
                entityBounds.x += velocity.x * delta;
                entityBounds.y += velocity.y * delta;
                Rectangle minkowskiTileBounds = new Rectangle(tileBounds.x - entityBounds.width / 2, tileBounds.y - entityBounds.height / 2, tileBounds.width + entityBounds.width, tileBounds.height + entityBounds.height);
                Vector2 entityCenter = new Vector2(entityBounds.x + entityBounds.width / 2, entityBounds.y + entityBounds.height / 2);

                // Clamp the entity to the outside of the tile
                if (minkowskiTileBounds.contains(entityCenter)) {
                    // The entity is inside the tile
                    float[] distances = new float[] {
                        Math.abs(minkowskiTileBounds.x - entityBounds.x - entityBounds.width),
                        Math.abs(minkowskiTileBounds.x + minkowskiTileBounds.width - entityBounds.x),
                        Math.abs(minkowskiTileBounds.y - entityBounds.y - entityBounds.height),
                        Math.abs(minkowskiTileBounds.y + minkowskiTileBounds.height - entityBounds.y)
                    };
                    
                    float minDistance = Float.MAX_VALUE;
                    int minIndex = -1;
                    for (int i = 0; i < distances.length; i++) {
                        if (distances[i] < minDistance) {
                            minDistance = distances[i];
                            minIndex = i;
                        }
                    }

                    System.out.println(minIndex);

                    if (minIndex == 0 && !tileLeft) {
                        // The entity is colliding with the left face of the tile
                        temporaryVelocity.x = 0;
                        position.x = tileBounds.x - entityBounds.width - threshold;
                    } else if (minIndex == 1 && !tileRight) {
                        // The entity is colliding with the right face of the tile
                        temporaryVelocity.x = 0;
                        position.x = tileBounds.x + tileBounds.width + threshold;
                    } else if (minIndex == 2 && !tileBelow) {
                        // The entity is colliding with the bottom face of the tile
                        temporaryVelocity.y = 0;
                        position.y = tileBounds.y - entityBounds.height - threshold;
                    } else if (minIndex == 3 && !tileAbove) {
                        // The entity is colliding with the top face of the tile
                        temporaryVelocity.y = 0;
                        position.y = tileBounds.y + tileBounds.height + threshold;
                    }
                }
            }
        }

        entity.position.x = position.x;
        entity.position.y = position.y;
        entity.position.x += temporaryVelocity.x;
        entity.position.y += temporaryVelocity.y;

    }

    /*public void moveHandleCollision(Entity entity, float delta, Vector2 velocity) {
        Vector2 position = entity.getPosition();
        float tempVelocityX = velocity.x * delta;
        float tempVelocityY = velocity.y * delta;

        final int playerTileX = Entity.getTileX(entity.getX());
        final int playerTileY = Entity.getTileY(entity.getY());

        final int collisionDetectionRange = 3;
        final float threshold = 0.1f;
        for (int x = -collisionDetectionRange; x <= collisionDetectionRange; x++) {
            for (int y = -collisionDetectionRange; y <= collisionDetectionRange; y++) {
                // TODO: AABB SWEEP a https://www.youtube.com/watch?v=Z9KB28mADfo
                int tileX = playerTileX + x;
                int tileY = playerTileY + y;
                TileType type = getTile(tileX, tileY, 1);

                if (type == null) {
                    continue;
                }
                if (!type.isSolid()) {
                    continue;
                }

                // Check to see if the entity will collide with the tile
                Rectangle entityBounds = new Rectangle(position.x + tempVelocityX, position.y + tempVelocityY, entity.getWidth(), entity.getHeight());
                Rectangle tileBounds = new Rectangle(tileX * Tile.SIZE - threshold / 2f, tileY * Tile.SIZE - threshold / 2f, Tile.SIZE - threshold, Tile.SIZE - threshold);

                Vector2[] collisionDirection = null;
                Vector2 endTemp = null;

                // Calculate x direction collision
                entityBounds = new Rectangle(position.x + tempVelocityX, position.y, entity.getWidth(), entity.getHeight());
                if (entityBounds.overlaps(tileBounds)) {
                    collisionDirection = getCollisionDirection(entityBounds, tileBounds);
                    endTemp = processCollision(tempVelocityX, 0, position, collisionDirection, entityBounds, tileBounds);
                    tempVelocityX = endTemp.x;
                }

                // Calculate y direction collision
                entityBounds = new Rectangle(position.x, position.y + tempVelocityY, entity.getWidth(), entity.getHeight());
                if (entityBounds.overlaps(tileBounds)) {
                    collisionDirection = getCollisionDirection(entityBounds, tileBounds);
                    endTemp = processCollision(0, tempVelocityY, position, collisionDirection, entityBounds, tileBounds);
                    tempVelocityY = endTemp.y;
                }
            }
        }
        position.x += tempVelocityX;
        position.y += tempVelocityY;
    }

    private Vector2[] getCollisionDirection(Rectangle entityBounds, Rectangle tileBounds) {
        Vector2 tileCenterToEntityCenter = new Vector2(
            (entityBounds.x + entityBounds.width / 2) - (tileBounds.x + tileBounds.width / 2),
            (entityBounds.y + entityBounds.height / 2) - (tileBounds.y + tileBounds.height / 2)
        );
        tileCenterToEntityCenter.nor();
        Vector2[] directions = new Vector2[] {
            new Vector2(0, 1), new Vector2(1, 0), new Vector2(0, -1), new Vector2(-1, 0)
        };
        for (int i = 0; i < directions.length; i++) {
            float dot = tileCenterToEntityCenter.dot(directions[i]);
            if (dot >= Math.sqrt(2d) / 2d) {
                return new Vector2[] { directions[i] };
            }
        }
        return new Vector2[] {};
    }

    private Vector2 processCollision(float tempX, float tempY, Vector2 position, Vector2[] collisionDirections, Rectangle entityBounds, Rectangle tileBounds) {
        Vector2 velocity = new Vector2(tempX, tempY);
        if (collisionDirections == null) {
            return velocity;
        }
        for (int i = 0; i < collisionDirections.length; i++) {
            Vector2 collisionDirection = collisionDirections[i];
            if (collisionDirection.x == 0) {
                // The entity is colliding with the top or bottom face of the tile
                if (collisionDirection.y > 0) {
                    // The entity is colliding with the top face of the tile
                    velocity.y = 0;
                    position.y = tileBounds.y + tileBounds.height;
                } else {
                    // The entity is colliding with the bottom face of the tile
                    velocity.y = 0;
                    position.y = tileBounds.y - entityBounds.height;
                }
            } else {
                // The entity is colliding with the left or right face of the tile
                if (collisionDirection.x > 0) {
                    // The entity is colliding with the right face of the tile
                    velocity.x = 0;
                    position.x = tileBounds.x + tileBounds.width;
                } else {
                    // The entity is colliding with the left face of the tile
                    velocity.x = 0;
                    position.x = tileBounds.x - entityBounds.width;
                }
            }
        }
        return velocity;
    }*/

    @Override
    public String toString() {
        return "[World]{worldSize=" + worldSize + ", entities=" + entities.size() + "}";
    }

}
