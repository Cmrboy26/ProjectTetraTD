package net.cmr.rtd.game.world;

import java.awt.Point;

import net.cmr.rtd.game.storage.TeamInventory;
import net.cmr.rtd.game.world.EnemyFactory.EnemyType;
import net.cmr.rtd.game.world.tile.StartTileData;
import net.cmr.rtd.game.world.tile.StructureTileData;
import net.cmr.rtd.game.world.tile.TileData;
import net.cmr.rtd.waves.Wave;
import net.cmr.rtd.waves.WaveUnit;
import net.cmr.rtd.waves.WavesData;

public class TeamData {

    public int team;
    public World world;
    public StructureTileData structure;
    public StartTileData startTile;
    public Point structurePosition, startTilePosition;

    public TeamData() { }
    public TeamData(World world, int team) throws NullTeamException {
        this.team = team;
        this.world = world;

        for (int x = 0; x < world.getWorldSize(); x++) {
            for (int y = 0; y < world.getWorldSize(); y++) {
                TileData data = world.getTileData(x, y, 1);
                if (data instanceof StructureTileData) {
                    StructureTileData tile = (StructureTileData) data;
                    if (tile.team == team) {
                        System.out.println("Team "+team+" has a structure at "+x+", "+y);
                        if (structurePosition != null) {
                            throw new NullTeamException("Team "+team+" has multiple end structures");
                        }
                        if (tile.getMoney() == -1 && tile.getHealth() == -1 && world.getWavesData() != null) {
                            tile.getInventory().setCash(world.getWavesData().startingMoney);
                            tile.health = world.getWavesData().startingHealth;
                        }
                        structure = tile;
                        structurePosition = new Point(x, y);
                    }
                }
                if (data instanceof StartTileData) {
                    StartTileData tile = (StartTileData) data;
                    if (tile.team == team) {
                        System.out.println("Team "+team+" has a start tile at "+x+", "+y);
                        if (startTilePosition != null) {
                            throw new NullTeamException("Team "+team+" has multiple start tiles");
                        }
                        startTile = tile;
                        startTilePosition = new Point(x, y);
                    }
                }
            }
        }

        if (structure == null || startTile == null) {
            throw new NullTeamException("Team "+team+" does not exist");
        }
    }

    public class NullTeamException extends Exception {
        public NullTeamException(String message) {
            super(message);
        }
    }

    public long getMoney() {
        return structure.getMoney();
    }
    public TeamInventory getInventory() {
        return structure.getInventory();
    }
    public int getHealth() {
        return structure.getHealth();
    }
    public void payMoney(long amount) {
        structure.inventory.addCash(-amount);
    }
    public void addMoney(long amount) {
        structure.inventory.addCash(amount);
    }
    public Point getStructurePosition() {
        return structurePosition;
    }
    public Point getStartTilePosition() {
        return startTilePosition;
    }
    public void spawnEnemy(EnemyType type) {
        startTile.getFactory().createEnemy(type);
    }

    public void depositMoney(long amount, UpdateData data) {
        if (data.isClient()) {
            return;
        }
        structure.inventory.addCash(amount);
        data.getManager().updateTeamStats(team);
    }

    public void rollRandomItem(UpdateData data) {

        int totalEnemies = 0;
        Wave wave = data.getManager().getWorld().getCurrentWave();
        if (wave == null) {
            return;
        }
        WavesData wavesData = data.getManager().getWorld().getWavesData();
        for (WaveUnit unit : wave.getWaveUnits()) {
            totalEnemies += unit.getQuantity();
        }

        float random = (float) Math.random();
        float chance = 1f / totalEnemies;
        float desiredWavesPerDrop = wavesData.wavesPerComponentTarget;
        float threshold = chance / desiredWavesPerDrop;

        if (random <= threshold) {
            int randomItem = (int) Math.floor(Math.random() * 3);
            randomItem++;
            switch (randomItem) {
                case 1:
                    structure.inventory.addScope();
                    break;
                case 2:
                    structure.inventory.addWd40();
                    break;
                case 3:
                    structure.inventory.addScrapMetal();
                    break;
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TeamData) {
            return ((TeamData) obj).team == team;
        }
        return false;
    }

}
