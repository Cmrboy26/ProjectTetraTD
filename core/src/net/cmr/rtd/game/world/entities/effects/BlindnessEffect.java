package net.cmr.rtd.game.world.entities.effects;

import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.shader.ShaderManager.CustomShader;

public class BlindnessEffect extends ShaderEffect {

    public BlindnessEffect(UpdateData data, EntityEffects target, float duration) {
        super(data, target, duration, CustomShader.GAMMA);
    }

    /**
     * Constructor for serialization.
     */
    public BlindnessEffect(UpdateData data, EntityEffects target, float duration, float maxDuration, int level) {
        super(data, target, duration, maxDuration, CustomShader.GAMMA);
    }
    
}
