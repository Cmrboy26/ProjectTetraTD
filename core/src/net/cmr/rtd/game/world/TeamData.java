package net.cmr.rtd.game.world;

import java.io.IOException;

import net.cmr.rtd.game.packets.ParticlePacket;
import net.cmr.rtd.game.storage.TeamInventory;
import net.cmr.rtd.game.world.EnemyFactory.EnemyType;
import net.cmr.rtd.game.world.entities.EnemyEntity;
import net.cmr.rtd.game.world.particles.ParticleCatalog;
import net.cmr.rtd.game.world.particles.ParticleEffect;
import net.cmr.rtd.game.world.tile.StartTileData;
import net.cmr.rtd.game.world.tile.StructureTileData;
import net.cmr.rtd.game.world.tile.TileData;
import net.cmr.rtd.waves.Wave;
import net.cmr.rtd.waves.WaveUnit;
import net.cmr.rtd.waves.WavesData;
import net.cmr.util.Point;
import net.cmr.util.Sprites.SpriteType;

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
    public long getScore() {
        return structure.getScore();
    }
    public void addScore(long score) {
        structure.addScore(score);
    }
    public void setScore(long score) {
        structure.setScore(score);
    }
    public void onEnemyDeath(EnemyEntity entity, UpdateData data) {
        addScore(entity.getMaxHealth());
    }

    public void depositMoney(long amount, UpdateData data) {
        if (data.isClient()) {
            return;
        }
        structure.inventory.addCash(amount);
        data.getManager().updateTeamStats(team);
    }

    int totalEnemies = 0;

    public void rollRandomItem(Entity entity, UpdateData data) {
        Wave wave = data.getManager().getWorld().getCurrentWave();
        if (wave == null) {
            return;
        }

        WavesData wavesData = data.getManager().getWorld().getWavesData();
        if (wave.getWaveUnits().size() != 0) {
            totalEnemies = 0;
            for (WaveUnit unit : wave.getWaveUnits()) {
                totalEnemies += unit.getQuantity();
            }
        }

        if (totalEnemies == 0) {
            // When reloading a save between rounds, totalEnemies is 0, which results in all enemies giving components.
            return;
        }
    
        float random = (float) Math.random();
        float chance = 1f / totalEnemies;
        
        float desiredWavesPerDrop = wavesData.wavesPerComponentTarget;
        float threshold = chance / desiredWavesPerDrop;

        if (random <= threshold) {
            int randomItem = (int) Math.floor(Math.random() * 3);
            randomItem++;
            SpriteType type = null;
            switch (randomItem) {
                case 1:
                    structure.inventory.addScope();
                    type = SpriteType.SCOPE;
                    break;
                case 2:
                    structure.inventory.addWd40();
                    type = SpriteType.LUBRICANT;
                    break;
                case 3:
                    structure.inventory.addScrapMetal();
                    type = SpriteType.SCRAP;
                    break;
            }
            ParticleEffect effect = ParticleCatalog.resourceCollectedEffect(entity.getPosition(), type);
            try {
                ParticlePacket packet = new ParticlePacket(effect);
                data.getManager().sendPacketToAll(packet);
            } catch (IOException e) {
                e.printStackTrace();
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
