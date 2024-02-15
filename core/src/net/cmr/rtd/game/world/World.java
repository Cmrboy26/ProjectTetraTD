package net.cmr.rtd.game.world;

import java.awt.geom.Line2D;
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
                    //velocity.y = entityBounds.y + entityBounds.height - tileBounds.y;
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
    }
    /*public void moveHandleCollision(Entity entity, float delta, Vector2 velocity) {
        // Swept AABB collision detection
        Vector2 position = entity.getPosition();
        float tempVelocityX = velocity.x * delta;
        float tempVelocityY = velocity.y * delta;

        final int playerTileX = Entity.getTileX(entity.getX());
        final int playerTileY = Entity.getTileY(entity.getY());

        final int collisionDetectionRange = 3;
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

                // Check to see if the entity will collide with the tile
                Rectangle tileBounds = new Rectangle(tileX * Tile.SIZE, tileY * Tile.SIZE, Tile.SIZE, Tile.SIZE);
                float halfWidth = entity.getWidth() / 2f;
                float halfHeight = entity.getHeight() / 2f;
                Rectangle expanedTileBounds = new Rectangle(tileBounds.x - halfWidth, tileBounds.y - halfHeight, tileBounds.width + entity.getWidth(), tileBounds.height + entity.getHeight());

                // Middle of the entity
                Vector2 pointBegin = new Vector2(position.x + halfWidth, position.y + halfHeight);
                // End of the entity movement
                Vector2 pointEnd = new Vector2(position.x + halfWidth + tempVelocityX, position.y + halfHeight + tempVelocityY);
                
                Vector2[] intersections = segmentRectangleIntersection(new Vector2[] { pointBegin, pointEnd }, expanedTileBounds);
                Vector2 closestIntersection = null;
                for (int i = 0; i < intersections.length; i++) {
                    // find the closest intersection to the entity
                    if (intersections[i] == null) {
                        continue;
                    }
                    if (closestIntersection == null) {
                        closestIntersection = intersections[i];
                    } else {
                        if (pointBegin.dst2(intersections[i]) < pointBegin.dst2(closestIntersection)) {
                            closestIntersection = intersections[i];
                        }
                    }
                }
                if (closestIntersection != null) {
                    System.out.println("Intersection: " + closestIntersection);
                }

                // If it's null, then there is no collision
                if (closestIntersection == null) {
                    continue;
                }
                // Otherwise, snap the entity to the rectangle and subtract the normal from tempVelocity
                Vector2 rectangleNormal = new Vector2();
                System.out.println(tileBounds.x + ", "+ closestIntersection.x + ", ");
                if (closestIntersection.x == tileBounds.x - halfWidth) {
                    rectangleNormal.set(-1, 0);
                } else if (closestIntersection.x == tileBounds.x + tileBounds.width + halfWidth) {
                    rectangleNormal.set(1, 0);
                } else if (closestIntersection.y == tileBounds.y - halfHeight) {
                    rectangleNormal.set(0, -1);
                } else if (closestIntersection.y == tileBounds.y + tileBounds.height + halfHeight) {
                    rectangleNormal.set(0, 1);
                }

                // Subtract the normal from the velocity
                Vector2 tempVelocityVector = new Vector2(tempVelocityX, tempVelocityY);
                float dot = tempVelocityVector.dot(rectangleNormal);

                if (dot < 0) {
                    // TODO: See if this is the correct way to handle this
                    System.out.println("---");
                    System.out.println(tempVelocityX + ", " + tempVelocityY);
                    System.out.println(rectangleNormal.x + ", " + rectangleNormal.y);
                    tempVelocityX -= dot * rectangleNormal.x;
                    tempVelocityY -= dot * rectangleNormal.y;
                    System.out.println(tempVelocityX + ", " + tempVelocityY);
                }

                // Teleport the center of the entity to the closest intersection
                position.x = closestIntersection.x - halfWidth;
                position.y = closestIntersection.y - halfHeight;

                // Continue to check for more collisions
            }
        }
        position.x += tempVelocityX;
        position.y += tempVelocityY;
    }

    public Vector2[] segmentRectangleIntersection(Vector2[] line, Rectangle rectangle) {
        // Find where the segment intersects with the rectangle
        Vector2[] corners = new Vector2[] {
            new Vector2(rectangle.x, rectangle.y),
            new Vector2(rectangle.x + rectangle.width, rectangle.y),
            new Vector2(rectangle.x + rectangle.width, rectangle.y + rectangle.height),
            new Vector2(rectangle.x, rectangle.y + rectangle.height)
        };
        Vector2[] intersections = new Vector2[4];
        for (int i = 0; i < 4; i++) {
            Vector2[] segment = new Vector2[] { corners[i], corners[(i + 1) % 4] };
            Vector2 intersection = segmentIntersection(line, segment);
            if (intersection != null) {
                intersections[i] = intersection;
            }
        }
        return intersections;
    }

    public Vector2 segmentIntersection(Vector2[] lineOne, Vector2[] lineTwo) {
        // Find where the two segments intersect

        float dx1 = lineOne[1].x - lineOne[0].x;
        float dy1 = lineOne[1].y - lineOne[0].y;

        float dx2 = lineTwo[1].x - lineTwo[0].x;
        float dy2 = lineTwo[1].y - lineTwo[0].y;

        // What is the cross product of the two lines?
        // Cross product of the two lines is the determinant of the 2x2 matrix
        float cross = dx1 * dy2 - dy1 * dx2;
        if (cross == 0) {
            return null;
        }

        float t = (lineTwo[0].x - lineOne[0].x) * dy2 - (lineTwo[0].y - lineOne[0].y) * dx2;
        t /= cross;

        float u = (lineTwo[0].x - lineOne[0].x) * dy1 - (lineTwo[0].y - lineOne[0].y) * dx1;
        u /= cross;

        if (t >= 0 && t <= 1 && u >= 0 && u <= 1) {
            return new Vector2(lineOne[0].x + t * dx1, lineOne[0].y + t * dy1);
        }

        return null;
    }*/

    @Override
    public String toString() {
        return "[World]{worldSize=" + worldSize + ", entities=" + entities.size() + "}";
    }

}
