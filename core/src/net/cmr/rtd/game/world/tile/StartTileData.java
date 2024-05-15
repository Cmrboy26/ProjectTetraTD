package net.cmr.rtd.game.world.tile;

import net.cmr.rtd.game.world.EnemyFactory;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.World;
import net.cmr.util.Log;

public class StartTileData extends TeamTileData {
    
    public StartTileData() { }
    public StartTileData(int team) {
        super(team);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    //float elapsedTime = 0;
    transient EnemyFactory factory;
    transient boolean alternate = false;

    @Override
    public void update(float delta, int tileX, int tileY, UpdateData data) {
        if (data.isClient()) {
            return;
        }

        if (factory == null) {
            factory = new EnemyFactory(team, tileX, tileY, data);
            Log.info("Created enemy factory for team "+team);
        }
    }

    @Override
    public void reset(World world) {
        //elapsedTime = 0;
        factory = null;
    }
    public EnemyFactory getFactory() {
        return factory;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StartTileData) {
            return ((StartTileData) obj).team == team;
        }
        return false;
    }

}
