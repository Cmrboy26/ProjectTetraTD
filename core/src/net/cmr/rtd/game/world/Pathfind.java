package net.cmr.rtd.game.world;

import com.badlogic.gdx.math.Vector2;

import net.cmr.rtd.game.world.tile.Tile;

/**
 * A class containing static methods for pathfinding.
 */
public class Pathfind {
    
    /**
     * Returns a normalized vector pointing from the entity to the specified tile.
     * @param entity the entity
     * @param tileX the x-coordinate of the tile
     * @param tileY the y-coordinate of the tile
     * @return a normalized vector pointing from the entity to the specified tile
     */
    public static Vector2 directPathfind(Entity entity, int tileX, int tileY) {
        float x = tileX * Tile.SIZE + Tile.SIZE / 2;
        float y = tileY * Tile.SIZE + Tile.SIZE / 2;

        return new Vector2(x - entity.getX(), y - entity.getY()).nor();
    }

}
