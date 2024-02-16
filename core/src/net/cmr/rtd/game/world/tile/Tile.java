package net.cmr.rtd.game.world.tile;

import java.util.HashMap;
import java.util.Objects;
import java.util.function.Function;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;

import net.cmr.rtd.game.world.Collidable;
import net.cmr.rtd.game.world.World;
import net.cmr.util.Sprites;

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

        FLOOR(1, "wallSprites20"),
        WALL(2, true)
        ;

        private final int id;
        private final String spriteName;
        private final boolean solid;
        private final Tile tile;

        TileType(int id) {
            this.id = id;
            types.put(id, this);
            this.spriteName = "tile"+id;
            this.solid = false;
            this.tile = new Tile(this);
        }

        TileType(int id, boolean solid) {
            this.id = id;
            types.put(id, this);
            this.spriteName = "tile"+id;
            this.solid = solid;
            this.tile = new Tile(this);
        }

        TileType(int id, Function<TileType, Tile> function) {
            this.id = id;
            types.put(id, this);
            this.spriteName = "tile"+id;
            this.solid = false;
            this.tile = function.apply(this);
        }

        TileType(int id, boolean solid, Function<TileType, Tile> function) {
            this.id = id;
            types.put(id, this);
            this.spriteName = "tile"+id;
            this.solid = solid;
            this.tile = function.apply(this);
        }

        TileType(int id, String spriteName) {
            this.id = id;
            types.put(id, this);
            this.spriteName = spriteName;
            this.solid = false;
            this.tile = new Tile(this);
        }

        TileType(int id, String spriteName, boolean solid) {
            this.id = id;
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
    }

    protected TileType type;

    public Tile(TileType type) {
        Objects.requireNonNull(type);
        this.type = type;
    }

    public void render(Batch batch, float delta, World world, int x, int y, int z) {
        switch (type) {
            case FLOOR:
                batch.draw(Sprites.sprite(type.getSpriteName()), x * SIZE, y * SIZE, SIZE, SIZE); 
                break;
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

                final String spriteName = "wallSprites";
                String drawSprite = spriteName;
                // 16 tile combinations
                // https://www.google.com/search?q=16+rule+tilemap&tbm=isch#imgrc=LqdrH_1mTnn3pM
                // TODO: Make sure that the column and row sprites take into account the floor
                
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

                if (!n && !s && e && w && floorNeighbors[1][0] == TileType.FLOOR) {
                    drawSprite = spriteName+17; // down wall
                }
                if (n && s && !e && !w) {
                    if (floorNeighbors[0][1] == TileType.FLOOR && floorNeighbors[2][1] != TileType.FLOOR) {
                        drawSprite = spriteName+18; // left wall
                    } else if (floorNeighbors[2][1] == TileType.FLOOR && floorNeighbors[0][1] != TileType.FLOOR) {
                        drawSprite = spriteName+19; // right wall
                    }
                }
                if (n && !s && !e && w && floorNeighbors[1][0] == TileType.FLOOR) {
                    drawSprite = spriteName+5; // up wall
                }
                if (n && !s && e && !w && floorNeighbors[1][0] == TileType.FLOOR) {
                    drawSprite = spriteName+4; // up wall
                } 

                if (!n && s && e && !w && floorNeighbors[0][1] == TileType.FLOOR && floorNeighbors[1][2] == TileType.FLOOR) { 
                    drawSprite = spriteName+21; 
                } 
                if (!n && s && !e && w && floorNeighbors[2][1] == TileType.FLOOR && floorNeighbors[1][2] == TileType.FLOOR) { 
                    drawSprite = spriteName+22; 
                } 
                
                batch.draw(Sprites.sprite(drawSprite), x * SIZE, y * SIZE, SIZE, SIZE);

                /*boolean northIsWall = neighbors[1][2] == TileType.WALL;
                boolean southIsWall = neighbors[1][0] == TileType.WALL;
                boolean eastIsWall = neighbors[2][1] == TileType.WALL;
                boolean westIsWall = neighbors[0][1] == TileType.WALL;

                boolean northIsFloor = floorNeighbors[1][2] == TileType.FLOOR;
                boolean southIsFloor = floorNeighbors[1][0] == TileType.FLOOR;
                boolean eastIsFloor = floorNeighbors[2][1] == TileType.FLOOR;
                boolean westIsFloor = floorNeighbors[0][1] == TileType.FLOOR;

                if (!northIsWall && !southIsWall && !eastIsWall && !westIsWall) {
                    // if there are no walls around us, draw top
                    batch.draw(Sprites.sprite("tile2"), x * SIZE, y * SIZE, SIZE, SIZE);
                    return;
                }

                if (!northIsWall && !southIsWall && (eastIsWall || westIsWall)) {
                    // if there is floor in front of us, draw top
                    if (floorNeighbors[1][0] == TileType.FLOOR) {
                        batch.draw(Sprites.sprite("tile2"), x * SIZE, y * SIZE, SIZE, SIZE);
                        return;
                    }
                }

                if (southIsFloor) {
                    if (northIsWall && (eastIsWall ^ westIsWall)) {
                        // if there is floor in front of us, draw top
                        Sprite cornerSprite = Sprites.sprite("tile7");
                        boolean flipX = westIsWall;
                        batch.draw(cornerSprite.getTexture(), x * SIZE, y * SIZE, SIZE/2, SIZE/2, SIZE, SIZE, 1, 1, 0, cornerSprite.getRegionX(), cornerSprite.getRegionY(), cornerSprite.getRegionWidth(), cornerSprite.getRegionHeight(), flipX, false);
                        return;
                    }
                }

                // if y-1, z-1 is floor, draw top

                boolean drawLines = false;
                Sprite lineSprite = Sprites.sprite("tile3");

                if (eastIsFloor && !eastIsWall) {
                    batch.draw(lineSprite.getTexture(), x * SIZE, y * SIZE, SIZE/2, SIZE/2, SIZE, SIZE, 1, 1, 90, lineSprite.getRegionX(), lineSprite.getRegionY(), lineSprite.getRegionWidth(), lineSprite.getRegionHeight(), false, false);
                    drawLines = true;
                }
                if (westIsFloor && !westIsWall) {
                    batch.draw(lineSprite.getTexture(), x * SIZE, y * SIZE, SIZE/2, SIZE/2, SIZE, SIZE, 1, 1, 270, lineSprite.getRegionX(), lineSprite.getRegionY(), lineSprite.getRegionWidth(), lineSprite.getRegionHeight(), false, false);
                    drawLines = true;
                }
                if (southIsFloor && !southIsWall) {
                    batch.draw(lineSprite.getTexture(), x * SIZE, y * SIZE, SIZE/2, SIZE/2, SIZE, SIZE, 1, 1, 180, lineSprite.getRegionX(), lineSprite.getRegionY(), lineSprite.getRegionWidth(), lineSprite.getRegionHeight(), false, true);
                    drawLines = true;
                }
                if (northIsFloor && !northIsWall) {
                    batch.draw(lineSprite.getTexture(), x * SIZE, y * SIZE, SIZE/2, SIZE/2, SIZE, SIZE, 1, 1, 180, lineSprite.getRegionX(), lineSprite.getRegionY(), lineSprite.getRegionWidth(), lineSprite.getRegionHeight(), false, false);
                    drawLines = true;
                }

                

                boolean drewNubs = false;
                Sprite nubSprite = Sprites.sprite("tile4");
                if (northIsWall && eastIsWall && floorNeighbors[2][2] == TileType.FLOOR) {
                    // rightTop nub
                    draw(batch, nubSprite, x, y, false, true);
                    drewNubs = true;
                }
                if (northIsWall && westIsWall && floorNeighbors[0][2] == TileType.FLOOR) {
                    // leftTop nub
                    draw(batch, nubSprite, x, y, true, true);
                    drewNubs = true;
                }
                if (southIsWall && eastIsWall && floorNeighbors[2][0] == TileType.FLOOR) {
                    // rightBottom nub
                    draw(batch, nubSprite, x, y, false, false);
                    drewNubs = true;
                }
                if (southIsWall && westIsWall && floorNeighbors[0][0] == TileType.FLOOR) {
                    // leftBottom nub
                    draw(batch, nubSprite, x, y, true, false);
                    drewNubs = true;
                }
                if (drewNubs) {
                    return;
                }*/

                /*if (northIsWall && southIsWall && !eastIsWall && !westIsWall) {
                    // if there are floors on the right, draw right line
                    if (floorNeighbors[2][1] == TileType.FLOOR) {
                        batch.draw(lineSprite.getTexture(), x * SIZE, y * SIZE, SIZE/2, SIZE/2, SIZE, SIZE, 1, 1, 90, lineSprite.getRegionX(), lineSprite.getRegionY(), lineSprite.getRegionWidth(), lineSprite.getRegionHeight(), false, false);
                        drawLines = true;
                    }
                    // if there are floors on the left, draw left line
                    if (floorNeighbors[0][1] == TileType.FLOOR) {
                        batch.draw(lineSprite.getTexture(), x * SIZE, y * SIZE, SIZE/2, SIZE/2, SIZE, SIZE, 1, 1, -90, lineSprite.getRegionX(), lineSprite.getRegionY(), lineSprite.getRegionWidth(), lineSprite.getRegionHeight(), false, false);
                        drawLines = true;
                    }
                    return;
                }
                if (eastIsWall && westIsWall && !northIsWall && !southIsWall) {
                    // if there are floors on the top, draw top line
                    if (floorNeighbors[1][2] == TileType.FLOOR) {
                        batch.draw(lineSprite.getTexture(), x * SIZE, y * SIZE, SIZE/2, SIZE/2, SIZE, SIZE, 1, 1, 180, lineSprite.getRegionX(), lineSprite.getRegionY(), lineSprite.getRegionWidth(), lineSprite.getRegionHeight(), false, false);
                        drawLines = true;
                    }
                    // if there are floors on the bottom, draw bottom line
                    if (floorNeighbors[1][0] == TileType.FLOOR) {
                        batch.draw(lineSprite.getTexture(), x * SIZE, y * SIZE, SIZE/2, SIZE/2, SIZE, SIZE, 1, 1, 0, lineSprite.getRegionX(), lineSprite.getRegionY(), lineSprite.getRegionWidth(), lineSprite.getRegionHeight(), false, false);
                        drawLines = true;
                    }
                    return;
                }*/

                /*// if y-1, z-1 is null and y-1, z is null, draw bottom
                if (world.getTile(x, y-1, z-1) == null && world.getTile(x, y-1, z) == null) {
                    Sprite sprite = Sprites.sprite("tile3");
                    sprite.flip(false, true);
                    batch.draw(Sprites.sprite("tile3"), x * SIZE, y * SIZE, SIZE, SIZE);
                    sprite.flip(false, true);
                    return;
                }*/

                break;
            default:
                break;
        }
    }
    
    /*private void draw(Batch batch, Sprite sprite, int x, int y, boolean flipX, boolean flipY) {
        sprite.flip(flipX, flipY);
        batch.draw(sprite, x * SIZE, y * SIZE, SIZE, SIZE);
        sprite.flip(flipX, flipY);
    }
    private void draw(Batch batch, Sprite sprite, int x, int y, boolean flipX, boolean flipY, float rotation) {
        sprite.setSize(SIZE, SIZE);
        sprite.flip(flipX, flipY);
        sprite.setOriginCenter();
        sprite.setRotation(rotation);
        sprite.draw(batch);
        sprite.setRotation(0);
        sprite.flip(flipX, flipY);
    }*/

    @Override
    public boolean isCollidable() {
        return type.solid;
    }

}
