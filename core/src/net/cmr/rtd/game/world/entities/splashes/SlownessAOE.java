package net.cmr.rtd.game.world.entities.splashes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.entities.effects.SlownessEffect;
import net.cmr.util.Sprites.SpriteType;

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
        return false;
    }

    @Override
    public boolean targetsEnemies() {
        return true;
    }

    @Override
    public void applyEffect(UpdateData data, Entity entity) {
        new SlownessEffect(data, entity.getEffects(), 1, 10);
    }

    @Override
    public Color getColor() {
        return Color.BLUE.cpy().lerp(Color.WHITE, 0.5f);
    }

    @Override
    public SpriteType getThrownSprite() {
        return SpriteType.SLOWNESS_BOTTLE;
    }
    
}
