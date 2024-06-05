package net.cmr.rtd.game.world.tile;

import java.util.HashMap;
import java.util.Objects;
import java.util.function.Function;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;

import net.cmr.rtd.game.world.Collidable;
import net.cmr.rtd.game.world.World;
import net.cmr.util.CMRGame;
import net.cmr.util.Sprites;
import net.cmr.util.Sprites.SpriteType;

/**
 * A static tile in the game world.
 */
public class Tile implements Collidable {

    public static int NULL_TILE_ID = 0;
    public static final int SIZE = 32;
    public static HashMap<Integer, TileType> types = new HashMap<>();

    // TODO: Find a better way to handle tiles.
    // Maybe its best to just make tile objects for each type.
    // Maybe tiles should be stored in a Tile object with an id, rotation, inverted flag, and collision
    // Probably don't store just the TileType (because it can't store any rotation without creating a lot of new types)
    // Or you could just 
    public enum TileType {

        FLOOR(1, 0, "hires-wallSprites20"),
        WALL(2, 1, true),
        PATH(3, 1, "pathDebug"), // NOTE: have entities prefer to continue on paths that they're facing towards rather than paths that are perpendicular to them
        START (4, 1, "startDebug"),
        END (5, 1, "endDebug"),

        GEMSTONE_VEIN(6, 0, "gemstoneVein"),
        IRON_VEIN(7, 0, "ironVein"),
        TITANIUM_VEIN(8, 0, "titaniumVein"),
        ;

        private final int id;
        private final String spriteName;
        private final boolean solid;
        private final Tile tile;
        public final int layer;

        TileType(int id, int layer) {
            this.id = id;
            this.layer = layer;
            types.put(id, this);
            this.spriteName = "tile"+id;
            this.solid = false;
            this.tile = new Tile(this);
        }

        TileType(int id, int layer, boolean solid) {
            this.id = id;
            this.layer = layer;
            types.put(id, this);
            this.spriteName = "tile"+id;
            this.solid = solid;
            this.tile = new Tile(this);
        }

        TileType(int id, int layer, Function<TileType, Tile> function) {
            this.id = id;
            this.layer = layer;
            types.put(id, this);
            this.spriteName = "tile"+id;
            this.solid = false;
            this.tile = function.apply(this);
        }

        TileType(int id, int layer, boolean solid, Function<TileType, Tile> function) {
            this.id = id;
            this.layer = layer;
            types.put(id, this);
            this.spriteName = "tile"+id;
            this.solid = solid;
            this.tile = function.apply(this);
        }

        TileType(int id, int layer, String spriteName) {
            this.id = id;
            this.layer = layer;
            types.put(id, this);
            this.spriteName = spriteName;
            this.solid = false;
            this.tile = new Tile(this);
        }

        TileType(int id, int layer, String spriteName, boolean solid) {
            this.id = id;
            this.layer = layer;
            types.put(id, this);
            this.spriteName = spriteName;
            this.solid = solid;
            this.tile = new Tile(this);
        }

        public int getID() {
            return id;
        }
        public String getSpriteName() {
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
        public boolean isSolid() {
            return solid;
        }
        public static boolean isFloor(TileType type) {
            return type != null && type.layer == 0;
        }
    }

    protected TileType type;

    public Tile(TileType type) {
        Objects.requireNonNull(type);
        this.type = type;
    }

    public void render(Batch batch, float delta, World world, int x, int y, int z) {
        final String spriteName = "hires-wallSprites";
        if (TileType.isFloor(type)) {
            batch.draw(Sprites.sprite(TileType.FLOOR.getSpriteName()), x * SIZE, y * SIZE, SIZE, SIZE); 
        }
        switch (type) {
            case WALL:
                TileType[][] floorNeighbors = new TileType[3][3];
                TileType[][] neighbors = new TileType[3][3];
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        neighbors[i][j] = world.getTile(x-1+i, y-1+j, z);
                        floorNeighbors[i][j] = world.getTile(x-1+i, y-1+j, z-1);
                    }
                }

                boolean n = neighbors[1][2] == TileType.WALL;
                boolean s = neighbors[1][0] == TileType.WALL;
                boolean e = neighbors[2][1] == TileType.WALL;
                boolean w = neighbors[0][1] == TileType.WALL;

                String drawSprite = spriteName;
                // 16 tile combinations
                // https://www.google.com/search?q=16+rule+tilemap&tbm=isch#imgrc=LqdrH_1mTnn3pM
                
                if (!n && !s && !e && !w) { drawSprite = spriteName+1; } 
                else if (n && !s && !e && !w) { drawSprite = spriteName+2; } 
                else if (!n && s && !e && !w) { drawSprite = spriteName+3; } 
                else if (!n && !s && e && !w) { drawSprite = spriteName+4; } 
                else if (!n && !s && !e && w) { drawSprite = spriteName+5; } 
                else if (n && !s && !e && w) { drawSprite = spriteName+6; } 
                else if (n && !s && e && !w) { drawSprite = spriteName+7; } 
                else if (!n && s && e && !w) { drawSprite = spriteName+8; } 
                
                else if (!n && s && !e && w) { drawSprite = spriteName+9; } 
                else if (n && !s && e && w) { drawSprite = spriteName+10; } 
                else if (!n && !s && e && w) { drawSprite = spriteName+11; } 
                else if (!n && s && e && w) { drawSprite = spriteName+12; } 
                else if (n && !s && e && w) { drawSprite = spriteName+13; } 
                else if (n && !s && e && w) { drawSprite = spriteName+14; } 
                else if (n && s && !e && !w) { drawSprite = spriteName+15; } 
                else { drawSprite = spriteName+16; }

                if (!n && !s && e && w && TileType.isFloor(floorNeighbors[1][0])) {
                    drawSprite = spriteName+17; // down wall
                }
                if (n && s && !e && !w) {
                    if (TileType.isFloor(floorNeighbors[0][1]) && !TileType.isFloor(floorNeighbors[2][1])) {
                        drawSprite = spriteName+18; // left wall
                    } else if (TileType.isFloor(floorNeighbors[2][1]) && !TileType.isFloor(floorNeighbors[0][1])) {
                        drawSprite = spriteName+19; // right wall
                    }
                }
                if (n && !s && !e && w && TileType.isFloor(floorNeighbors[1][0])) {
                    drawSprite = spriteName+5; // up wall
                }
                if (n && !s && e && !w && TileType.isFloor(floorNeighbors[1][0])) {
                    drawSprite = spriteName+4; // up wall
                } 

                if (!n && s && e && !w && TileType.isFloor(floorNeighbors[0][1]) && TileType.isFloor(floorNeighbors[1][2])) { 
                    drawSprite = spriteName+21; 
                } 
                if (!n && s && !e && w && TileType.isFloor(floorNeighbors[2][1]) && TileType.isFloor(floorNeighbors[1][2])) { 
                    drawSprite = spriteName+22; 
                } 
                
                batch.draw(Sprites.sprite(drawSprite), x * SIZE, y * SIZE, SIZE, SIZE);
                break;
            case PATH:
                /*if (CMRGame.isDebug()) {
                    batch.draw(Sprites.sprite(type.getSpriteName()), x * SIZE, y * SIZE, SIZE, SIZE);
                }*/
                batch.draw(Sprites.sprite(SpriteType.DARKENED), x * SIZE, y * SIZE, SIZE, SIZE);
                break;
            case END:
                batch.draw(Sprites.sprite(SpriteType.DARKENED), x * SIZE, y * SIZE, SIZE, SIZE);
                if (CMRGame.isDebug()) {
                    batch.draw(Sprites.sprite(type.getSpriteName()), x * SIZE, y * SIZE, SIZE, SIZE);
                }
                batch.draw(Sprites.sprite(SpriteType.STRUCTURE), x * SIZE, y * SIZE, SIZE, SIZE);
                break;
            case START:   
                batch.draw(Sprites.sprite(SpriteType.DARKENED), x * SIZE, y * SIZE, SIZE, SIZE);
                if (CMRGame.isDebug()) {
                    batch.draw(Sprites.sprite(type.getSpriteName()), x * SIZE, y * SIZE, SIZE, SIZE);
                }
                batch.draw(Sprites.sprite(spriteName+23), x * SIZE, y * SIZE, SIZE, SIZE);
                break;
            case IRON_VEIN:
            case TITANIUM_VEIN:
            case GEMSTONE_VEIN:
                batch.draw(Sprites.sprite(type.getSpriteName()), x * SIZE, y * SIZE, SIZE, SIZE);
                break;
            default:
                break;
        }
    }

    public TileType getType() {
        return type;
    }

    @Override
    public boolean isCollidable() {
        return type.solid;
    }

}
