package net.cmr.rtd.game.world.tile;

import net.cmr.rtd.game.world.TeamData;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.World;

public class StructureTileData extends TeamTileData {
    
    public int health;
    public long money;
    boolean healthChanged = false;

    public StructureTileData() { }
    public StructureTileData(int team) {
        super(team);
        health = -1;
        money = -1;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public void update(float delta, int tileX, int tileY, UpdateData data) {
        if (data.isClient()) {
            return;
        }

        if (healthChanged) {
            healthChanged = false;
            
            TeamData teamm = data.getManager().getTeam(team);

            data.getManager().updateTeamStats(team);
            if (health <= 0) {
                data.getManager().teamLost(team);
            }
        }

    }

    public void damage(int damage) {
        health -= damage;
        healthChanged = true;
    }

    @Override
    public void reset(World world) {
        health = world.getWavesData().startingHealth;
        money = world.getWavesData().startingMoney;

        healthChanged = true;
    }

    public long getMoney() {
        return money;
    }

    public int getHealth() {
        return health;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StructureTileData) {
            return ((StructureTileData) obj).team == team;
        }
        return false;
    }

}
