package net.cmr.rtd.game.world.entities.effects;

import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.entities.EnemyEntity;
import net.cmr.rtd.game.world.entities.EnemyEntity.DamageType;
import net.cmr.rtd.game.world.entities.effects.EntityEffects.EntityStat;
import net.cmr.rtd.game.world.particles.ParticleCatalog;
import net.cmr.rtd.game.world.particles.ParticleEffect;
import net.cmr.rtd.shader.ShaderManager.CustomShader;

public abstract class ShaderEffect extends Effect {

    private final CustomShader shader;
    private final float[] input;

    public ShaderEffect(UpdateData data, EntityEffects target, float duration, float maxDuration, CustomShader shader, float... input) {
        super(data, target, duration, maxDuration, 1);
        this.shader = shader;
        this.input = input;
    }

    public ShaderEffect(UpdateData data, EntityEffects target, float duration, CustomShader shader, float... input) {
        super(data, target, duration, 1);
        this.shader = shader;
        this.input = input;
    }

    public CustomShader getShader() {
        return shader;
    }

    @Override
    public void onInflict(UpdateData data) {
        // Handled by the game renderer.
    }

    @Override
    public void update(float delta, UpdateData data) {
        super.update(delta, data);
    }

    @Override
    public float getStatModifier(EntityStat stat) {
        return 1;
    }

}
