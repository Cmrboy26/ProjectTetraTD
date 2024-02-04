package net.cmr.rtd.game.world.tile;

import java.util.HashMap;
import java.util.Objects;
import java.util.function.Function;

import com.badlogic.gdx.graphics.g2d.Batch;

import net.cmr.rtd.game.world.Collidable;
import net.cmr.rtd.game.world.World;
import net.cmr.util.Sprites;
import net.cmr.util.Sprites.SpriteType;

/**
 * A static tile in the game world.
 */
public class Tile implements Collidable {

    public static int NULL_TILE_ID = -1;
    public static final int SIZE = 32;
    public static HashMap<Integer, TileType> types = new HashMap<>();

    public enum TileType {

        FLOOR(0, SpriteType.FLOOR),
        WALL(1, SpriteType.WALL, true),
        ;

        private final int id;
        private final SpriteType spriteName;
        private final boolean solid;
        private final Tile tile;

        TileType(int id, SpriteType spriteName) {
            this.id = id;
            types.put(id, this);
            this.spriteName = spriteName;
            this.solid = false;
            this.tile = new Tile(this);
        }

        TileType(int id, SpriteType spriteName, boolean solid) {
            this.id = id;
            types.put(id, this);
            this.spriteName = spriteName;
            this.solid = solid;
            this.tile = new Tile(this);
        }

        TileType(int id, SpriteType spriteName, Function<TileType, Tile> function) {
            this.id = id;
            types.put(id, this);
            this.spriteName = spriteName;
            this.solid = false;
            this.tile = function.apply(this);
        }

        TileType(int id, SpriteType spriteName, boolean solid, Function<TileType, Tile> function) {
            this.id = id;
            types.put(id, this);
            this.spriteName = spriteName;
            this.solid = solid;
            this.tile = function.apply(this);
        }

        public int getID() {
            return id;
        }
        public SpriteType getSpriteType() {
            return spriteName;
        }
        public static TileType getType(int id) {
            if (id == NULL_TILE_ID) {
                return null;
            }
            TileType type = types.get(id);
            if (type == null) {
                throw new IllegalArgumentException("Invalid tile type id: " + id);
            }
            return type;
        }
        public Tile getTile() {
            return tile;
        }
    }*/

    protected TileType type;

    public Tile(TileType type) {
        Objects.requireNonNull(type);
        this.type = type;
    }

    public void render(Batch batch, float delta, World world, int x, int y, int z) {
        batch.draw(Sprites.sprite(type.getSpriteType()), x * SIZE, y * SIZE, SIZE, SIZE); 
    }

    @Override
    public boolean isCollidable() {
        return type.solid;
    }

}
