package net.cmr.rtd.game.world;

import java.awt.Point;

import net.cmr.rtd.game.world.tile.StartTileData;
import net.cmr.rtd.game.world.tile.StructureTileData;

public class TeamData {

    public int team;
    public StructureTileData structure;
    public StartTileData startTile;
    public Point structurePosition, startTilePosition;

    public TeamData() { }
    public TeamData(World world, int team) throws TeamNonExistentException {
        this.team = team;

        for (int x = 0; x < world.getWorldSize(); x++) {
            for (int y = 0; y < world.getWorldSize(); y++) {
                if (world.getTileData(x, y, 1) instanceof StructureTileData) {
                    StructureTileData tile = (StructureTileData) world.getTileData(x, y, 1);
                    if (tile.team == team) {
                        structure = tile;
                        structurePosition = new Point(x, y);
                    }
                }
                if (world.getTileData(x, y, 1) instanceof StartTileData) {
                    StartTileData tile = (StartTileData) world.getTileData(x, y, 1);
                    if (tile.team == team) {
                        startTile = tile;
                        startTilePosition = new Point(x, y);
                    }
                }
            }
        }

        if (structure == null || startTile == null) {
            throw new TeamNonExistentException("Team "+team+" does not exist");
        }
    }

    public class TeamNonExistentException extends Exception {
        public TeamNonExistentException(String message) {
            super(message);
        }
    }

    public long getMoney() {
        return structure.getMoney();
    }
    public int getHealth() {
        return structure.getHealth();
    }
    public Point getStructurePosition() {
        return structurePosition;
    }
    public Point getStartTilePosition() {
        return startTilePosition;
    }

}
