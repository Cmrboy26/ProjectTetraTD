package net.cmr.rtd.game.world;

import net.cmr.rtd.game.world.tile.EndTileData;
import net.cmr.rtd.game.world.tile.StartTileData;

public class TeamData {

    public int team;
    public EndTileData endTile;
    public StartTileData startTile;

    public TeamData() { }
    public TeamData(World world, int team) {
        this.team = team;
        endTile = new EndTileData(team);
        startTile = new StartTileData(team);
    }
    
}
