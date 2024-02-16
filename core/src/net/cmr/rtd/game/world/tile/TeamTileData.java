package net.cmr.rtd.game.world.tile;

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

    

}
