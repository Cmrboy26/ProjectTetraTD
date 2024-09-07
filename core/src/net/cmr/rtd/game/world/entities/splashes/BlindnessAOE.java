package net.cmr.rtd.game.world.entities.splashes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.entities.effects.BlindnessEffect;
import net.cmr.rtd.game.world.entities.effects.ShaderEffect;
import net.cmr.rtd.game.world.entities.effects.SlownessEffect;
import net.cmr.rtd.shader.ShaderManager.CustomShader;
import net.cmr.util.Sprites.SpriteType;

public class BlindnessAOE extends SplashAOE {
    
    /**
     * Empty constructor for serialization.
     */
    public BlindnessAOE() {
        super(GameType.BLINDNESS_AOE);
    }

    public BlindnessAOE(Vector2 throwPosition, Vector2 targetPosition, int lingerDuration, int team) {
        super(GameType.BLINDNESS_AOE, throwPosition, targetPosition, lingerDuration, team);
    }

    @Override
    public boolean targetsPlayers() {
        return true;
    }

    @Override
    public boolean targetsEnemies() {
        return false;
    }

    @Override
    public void applyEffect(UpdateData data, Entity entity) {
        new BlindnessEffect(data, entity.getEffects(), 13);
    }

    @Override
    public Color getColor() {
        return Color.BLACK;
    }

    @Override
    public SpriteType getThrownSprite() {
        return SpriteType.BLINDNESS_BOTTLE;
    }
    
}
