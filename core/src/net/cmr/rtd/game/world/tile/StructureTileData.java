package net.cmr.rtd.game.world.tile;

import net.cmr.rtd.game.world.UpdateData;
import net.cmr.util.Log;

public class StructureTileData extends TeamTileData {
    
    int health = 100;
    long money = 50;
    boolean healthChanged = false;

    public StructureTileData() { }
    public StructureTileData(int team) {
        super(team);
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
            data.getManager().updateTeamStats(team);
            System.out.println("HEALTH: "+health);
            if (health <= 0) {
                // TODO: The team has lost
                Log.info("Team "+team+" has lost");
            }
        }

    }

    public void damage(int damage) {
        health -= damage;
        healthChanged = true;
    }

    @Override
    public void reset() {
        health = 100;
        healthChanged = true;
    }

    public long getMoney() {
        return money;
    }

    public int getHealth() {
        return health;
    }

}
