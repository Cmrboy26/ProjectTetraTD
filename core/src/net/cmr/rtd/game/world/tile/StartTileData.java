package net.cmr.rtd.game.world.tile;

import net.cmr.rtd.game.world.EnemyFactory;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.EnemyFactory.EnemyType;
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

    float elapsedTime = 0;
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

        elapsedTime += delta;
        if (elapsedTime > 1) {
            elapsedTime = 0;
            Log.info("Creating basic enemy... "+team);
            if (alternate) {
                factory.createEnemy(EnemyType.BASIC_ONE);
            } else {
                factory.createEnemy(EnemyType.BASIC_ONE);
            }
            alternate = !alternate;
        }
    }

    @Override
    public void reset() {
        elapsedTime = 0;
        factory = null;
    }

}
