package net.cmr.rtd.game.world;

import java.io.DataInputStream;
import java.io.IOException;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.DataBuffer;

import net.cmr.rtd.game.GameManager;
import net.cmr.rtd.game.world.tile.Tile;
import net.cmr.rtd.game.world.tile.Tile.TileType;

/**
 * The world class will handle the game world
 * and all of the game objects.
 * The main world will be stored in {@link GameManager},
 * and the manager will send data about the world to
 * the client for them to reconstruct.
 */
public class World extends GameObject {

    public static final int DEFAULT_WORLD_SIZE = 64;
    public static final int LAYERS = 3;
    TileType tiles[][][];
    int worldSize;

    public World() {
        super(GameType.WORLD);
        this.worldSize = DEFAULT_WORLD_SIZE;
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
    public void update(float delta) {
        super.update(delta);
    }

    @Override
    public void render(Batch batch, float delta) {
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
        super.render(batch, delta);
    }

    @Override
    protected void serialize(DataBuffer buffer) throws IOException {
        for (int x = 0; x < worldSize; x++) {
            for (int y = 0; y < worldSize; y++) {
                for (int z = 0; z < LAYERS; z++) {
                    if (tiles[x][y][z] == null) {
                        buffer.writeInt(Tile.NULL_TILE_ID);
                        continue;
                    }
                    buffer.writeInt(tiles[x][y][z].getID());
                }
            }
        }
    }

    @Override
    protected void deserialize(GameObject object, DataInputStream input) throws IOException {
        World world = (World) object;
        for (int x = 0; x < worldSize; x++) {
            for (int y = 0; y < worldSize; y++) {
                for (int z = 0; z < LAYERS; z++) {
                    world.tiles[x][y][z] = TileType.getType(input.readInt());
                }
            }
        }
    }

}
