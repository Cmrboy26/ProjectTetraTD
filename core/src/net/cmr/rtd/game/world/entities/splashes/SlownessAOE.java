package net.cmr.rtd.game.world.entities.splashes;

import com.badlogic.gdx.math.Vector2;

import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.entities.effects.SlownessEffect;

public class SlownessAOE extends SplashAOE {
    
    /**
     * Empty constructor for serialization.
     */
    public SlownessAOE() {
        super(GameType.SLOWNESS_AOE);
    }

    public SlownessAOE(Vector2 throwPosition, Vector2 targetPosition, int lingerDuration, int team) {
        super(GameType.SLOWNESS_AOE, throwPosition, targetPosition, lingerDuration, team);
    }

    @Override
    public boolean targetsPlayers() {
        return true;
    }

    @Override
    public boolean targetsEnemies() {
        return true;
    }

    @Override
    public void applyEffect(UpdateData data, Entity entity) {
        // TODO: This must be implemented correctly.
        new SlownessEffect(data, entity.getEffects(), 3, 1);
    }
    
}
