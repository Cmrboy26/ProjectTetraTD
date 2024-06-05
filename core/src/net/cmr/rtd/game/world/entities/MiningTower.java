package net.cmr.rtd.game.world.entities;

import java.io.DataInputStream;
import java.io.IOException;

import com.badlogic.gdx.utils.DataBuffer;

import net.cmr.rtd.game.GamePlayer;
import net.cmr.rtd.game.storage.TeamInventory;
import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.TeamData;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.tile.Tile.TileType;

public abstract class MiningTower extends TowerEntity {

    float miningTimeRemaining = -1;

    public MiningTower(GameType type) {
        super(type, 0);
    }
    public MiningTower(GameType type, int team) {
        super(type, team);
    }

    @Override
    public void update(float delta, UpdateData data) {
        super.update(delta, data);
        if (data.isServer() && !data.getManager().areWavesPaused()) {
            miningUpdate(data, delta);
        }
    }

    @Override
    public float getAttackSpeed() {
        return 0;
    }

    @Override
    public float getDisplayRange() {
        return 0;
    }

    @Override
    public float getDisplayDamage() {
        return 0;
    }

    /**
     * @return The time in seconds it take to receive resources from the tower
     */
    public abstract float getMiningTime();

    /**
     * Called every frame while the game is unpaused.
     */
    public void miningUpdate(UpdateData data, float delta) {
        if (miningTimeRemaining == -1) {
            miningTimeRemaining = getMiningTime();
        }
        miningTimeRemaining -= delta;
        if (miningTimeRemaining <= 0) {
            miningTimeRemaining = getMiningTime();
            onMiningComplete(data);
        }
    }

    @Override
    protected void serializeTower(DataBuffer buffer) throws IOException {
        
    }

    @Override
    protected void deserializeTower(TowerEntity entity, DataInputStream input) throws IOException {
        
    }

    public void onMiningComplete(UpdateData data) {
        // Override this method to handle the mining completion event
    }

    public boolean validMiningTarget(TileType type) {
        return type == TileType.TITANIUM_VEIN || type == TileType.IRON_VEIN || type == TileType.GEMSTONE_VEIN;
    }

    // Helper methods
    public TeamInventory getTeamInventory(UpdateData data) {
        return data.getManager().getTeam(team).getInventory();
    }

    public void updateInventoryOnClients(UpdateData data) {
        for (GamePlayer player : data.getManager().getPlayers()) {
            if (player.getTeam() == getTeam()) {
                data.getManager().sendStatsUpdatePacket(player);
            }
        }
    }

    public TileType getTileBelow(UpdateData data) {
        return data.getWorld().getTile(Entity.getTileX(this), Entity.getTileY(this), 0);
    }
    
}
