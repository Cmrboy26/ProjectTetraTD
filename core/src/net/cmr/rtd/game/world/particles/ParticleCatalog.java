package net.cmr.rtd.game.world.particles;

import com.badlogic.gdx.math.Vector2;

import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.entities.HealerEnemy;
import net.cmr.rtd.game.world.entities.towers.FireTower;
import net.cmr.rtd.game.world.entities.towers.IceTower;
import net.cmr.util.Sprites.AnimationType;
import net.cmr.util.Sprites.SpriteType;

public class ParticleCatalog {

    public static ParticleEffect frozenEffect(Entity entity) {
        ParticleEffect effect = SpreadEmitterEffect.factory()
                .setEntity(entity)
                .setParticle(SpriteType.FROZEN)
                .setDuration(1)
                .setEmissionRate(1)
                .setScale(.20f)
                .setParticleLife(.5f)
                .setFollowEntity(true)
                .setAnimationSpeed(2f)
                .create();
        return effect;
    }

    public static ParticleEffect fireEffect(Entity entity) {
        return SpreadEmitterEffect.factory()
                .setEntity(entity)
                .setParticle(AnimationType.FIRE)
                .setDuration(1)
                .setEmissionRate(1)
                .setScale(.25f)
                .setParticleLife(.5f)
                .setFollowEntity(true)
                .setAnimationSpeed(2f)
                .create();
    }

    public static ParticleEffect iceTowerFrozenEffect(IceTower tower) {
        return SpreadEmitterEffect.factory()
                .setEntity(tower)
                .setParticle(SpriteType.FROZEN)
                .setDuration(2f)
                .setEmissionRate(2 + tower.getLevel())
                .setScale(.15f)
                .setParticleLife(2f)
                .setFollowEntity(true)
                .setAnimationSpeed(2f)
                .create();
    }

    public static ParticleEffect healEffect(Entity entity) {
        return SpreadEmitterEffect.factory()
                .setParticle(AnimationType.SPARKLE)
                .setDuration(1.5f)
                .setEmissionRate(15)
                .setScale(.2f)
                .setParticleLife(.5f)
                .setAnimationSpeed(1.5f)
                .setAreaSize(HealerEnemy.EFFECT_RADIUS * 2)
                .setFollowEntity(true)
                .setEntity(entity)
                .create();
    }

    public static ParticleEffect upgradeEffect(Entity entity) {
        ParticleEffect effect = SpreadEmitterEffect.factory()
                .setParticle(AnimationType.SPARKLE)
                .setDuration(1.5f)
                .setEmissionRate(19)
                .setScale(.225f)
                .setParticleLife(.8f)
                .setAnimationSpeed(1.5f)
                .setAreaSize(1.2f)
                .create();
        effect.setPosition(new Vector2(entity.getX(), entity.getY()));
        return effect;
    }

    public static ParticleEffect criticalEffect(Entity entity) {
        ParticleEffect effect = SpreadEmitterEffect.factory()
                .setParticle(SpriteType.CRITICAL_ICON)
                .setDuration(.75f)
                .setEmissionRate(79)
                .setScale(.12f)
                .setParticleLife(.4f)
                .setAnimationSpeed(1.5f)
                .setAreaSize(.7f)
                .setGravity(3f)
                .setXRandomImpact(1f)
                .create();
        effect.setPosition(new Vector2(entity.getX(), entity.getY()));
        return effect;
    }

    public static ParticleEffect fireballHitEffect(FireTower tower) {
        return SpreadEmitterEffect.factory()
                .setParticle(AnimationType.FIRE)
                .setDuration(1)
                .setEmissionRate(20)
                .setScale(.45f)
                .setParticleLife(.5f)
                .setFollowEntity(true)
                .setAnimationSpeed(2f)
                .setAreaSize(tower.getFireballAOE())
                .create();
    }

    public static ParticleEffect resourceCollectedEffect(Vector2 position, SpriteType resourceSprite) {
        ParticleEffect effect = SpreadEmitterEffect.factory()
                .setParticle(resourceSprite)
                .setDuration(3)
                .setEmissionRate(.01f)
                .setScale(.7f)
                .setParticleLife(3)
                .setFollowEntity(false)
                .setAreaSize(0)
                .setRandomVelocityImpact(0)
                .create();
        effect.setPosition(position.cpy());
        return effect;
    }

}
