package net.cmr.rtd.game.world.tile;

import net.cmr.rtd.game.world.UpdateData;
import net.cmr.util.Log;

public class EndTileData extends TeamTileData {
    
    int health = 100;
    boolean healthChanged = false;

    public EndTileData() { }
    public EndTileData(int team) {
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

}
