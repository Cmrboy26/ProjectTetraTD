package net.cmr.rtd.game.world.tile;

import com.badlogic.gdx.graphics.g2d.Batch;

public class TeamTileData extends TileData {
    
    public int team;

    public TeamTileData() { }
    public TeamTileData(int team) {
        this.team = team;
    }

    @Override
    public String toString() {
        return "team " + team;
    }

    @Override
    public void render(Batch batch, int tileX, int tileY) {
        //batch.draw(Sprites.sprite(SpriteType.DARKENED), tileX * Tile.SIZE, tileY * Tile.SIZE, Tile.SIZE, Tile.SIZE);
        super.render(batch, tileX, tileY);
    }
    

}
