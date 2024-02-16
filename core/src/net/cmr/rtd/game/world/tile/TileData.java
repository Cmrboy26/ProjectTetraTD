package net.cmr.rtd.game.world.tile;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.esotericsoftware.kryo.Kryo;

import net.cmr.rtd.game.world.World.Point3D;
import net.cmr.util.Sprites;

/**
 * A class that stores data for specific tiles.
 */
public abstract class TileData {

    public TileData() { }

    public static void registerKryo(Kryo kryo) {
        kryo.register(Point3D.class);
        kryo.register(TileData.class);
        kryo.register(TeamTileData.class);
    }

    public void render(Batch batch, int tileX, int tileY) {
        // Draw toString() on the tile
        Sprites.getInstance().smallFont().getData().scale(-.25f);
        Sprites.getInstance().smallFont().draw(batch, toString(), tileX * Tile.SIZE, tileY * Tile.SIZE + Tile.SIZE / 2);
        Sprites.getInstance().smallFont().getData().scale(.25f);
    }
}
