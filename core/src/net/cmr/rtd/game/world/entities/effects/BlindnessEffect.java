package net.cmr.rtd.game.world.entities.effects;

import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.shader.ShaderManager.CustomShader;
import net.cmr.util.Sprites.SpriteType;

public class BlindnessEffect extends ShaderEffect {

    public BlindnessEffect(UpdateData data, EntityEffects target, float duration) {
        super(data, target, duration, CustomShader.BLINDNESS);
    }

    /**
     * Constructor for serialization.
     */
    public BlindnessEffect(UpdateData data, EntityEffects target, float duration, float maxDuration, int level) {
        super(data, target, duration, maxDuration, CustomShader.BLINDNESS);
    }
    
}
