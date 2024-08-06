package net.cmr.rtd.game.world.particles;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.DataBuffer;

import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.tile.Tile;
import net.cmr.rtd.screen.GameScreen;
import net.cmr.util.Sprites;
import net.cmr.util.Sprites.AnimationType;
import net.cmr.util.Sprites.SpriteType;

public class SpreadEmitterEffect extends ParticleEffect {

    public static final int VERSION = 2;

    SpriteType spriteType;
    AnimationType animationType;
    float duration, scale, emissionRate, particleLife, animationSpeed, areaSize, randomVelocityImpact = 1, gravity = 0, xRandomImpact = 0;
    boolean followEntity;

    public static class SpreadEmitterFactory {

        Entity attachedEntity = null;
        SpriteType spriteType = null;
        AnimationType animationType = null;
        float duration = 1, scale = 1, emissionRate = 1, particleLife = 1, animationSpeed = 1, areaSize = 1, randomVelocityImpact = 1;
        float gravity = 0, xRandomImpact = 0;
        boolean followEntity = false;

        public SpreadEmitterFactory() {
            
        }

        public SpreadEmitterFactory setEntity(Entity attachedEntity) {
            this.attachedEntity = attachedEntity;
            return this;
        }

        public SpreadEmitterFactory setParticle(SpriteType spriteType) {
            this.spriteType = spriteType;
            return this;
        }

        public SpreadEmitterFactory setParticle(AnimationType animationType) {
            this.animationType = animationType;
            return this;
        }

        public SpreadEmitterFactory setDuration(float duration) {
            this.duration = duration;
            return this;
        }

        public SpreadEmitterFactory setScale(float scale) {
            this.scale = scale;
            return this;
        }

        public SpreadEmitterFactory setEmissionRate(float emissionRate) {
            this.emissionRate = emissionRate;
            return this;
        }

        public SpreadEmitterFactory setParticleLife(float particleLife) {
            this.particleLife = particleLife;
            return this;
        }

        public SpreadEmitterFactory setFollowEntity(boolean followEntity) {
            this.followEntity = followEntity;
            return this;
        }

        public SpreadEmitterFactory setAnimationSpeed(float animationSpeed) {
            this.animationSpeed = animationSpeed;
            return this;
        }

        public SpreadEmitterFactory setAreaSize(float areaSize) {
            this.areaSize = areaSize;
            return this;
        }

        public SpreadEmitterFactory setRandomVelocityImpact(float impact) {
            this.randomVelocityImpact = impact;
            return this;
        }

        public SpreadEmitterFactory setGravity(float gravity) {
            this.gravity = gravity;
            return this;
        }

        public SpreadEmitterFactory setXRandomImpact(float ximpact) {
            this.xRandomImpact = ximpact;
            return this;
        }

        public SpreadEmitterEffect create() {
            SpreadEmitterEffect effect = new SpreadEmitterEffect(attachedEntity);
            effect.spriteType = spriteType;
            effect.animationType = animationType;
            effect.duration = duration;
            effect.scale = scale;
            effect.emissionRate = emissionRate;
            effect.particleLife = particleLife;
            effect.followEntity = followEntity;
            effect.animationSpeed = animationSpeed;
            effect.areaSize = areaSize;
            effect.randomVelocityImpact = randomVelocityImpact;
            effect.gravity = gravity;
            effect.xRandomImpact = xRandomImpact;
            return effect;
        }
    }

    public SpreadEmitterEffect() {
        super(null);
    }

    public SpreadEmitterEffect(Entity attachedEntity) {
        super(attachedEntity);
    }

    public static SpreadEmitterFactory factory() {
        return new SpreadEmitterFactory();
    }

    private static class Particle {
        Vector2 position;
        Vector2 velocity;
        float elapsedTime;
        public Particle(Vector2 position, Vector2 velocity) {
            this.position = position;
            this.velocity = velocity;
        }
    }
    ArrayList<Particle> particles = new ArrayList<Particle>();
    float particleDelta = -1;

    @Override
    public void updateParticle(GameScreen screen, float delta) {
        if (particleDelta == -1) {
            // On the first frame of the particle, create a particle right away.
            particleDelta = 1f / emissionRate;
        }

        particleDelta += delta;
        if (particleDelta >= 1f / emissionRate) {
            particleDelta -= 1f / emissionRate;
            Vector2 particlePosition = new Vector2(position);
            if (followEntity) {
                particlePosition.set(0, 0);
            }
            float range = Tile.SIZE * this.areaSize;
            particlePosition.x += (float) (Math.random() * range - range / 2);
            particlePosition.y += (float) (Math.random() * range - range / 2);
            float random = (float) Math.random();
            float calculatedRandom = 2f * random * randomVelocityImpact - randomVelocityImpact + 1f;
            calculatedRandom /= 2f;
            float randomx = (float) Math.random();
            float calculatedRandomx = 2f * randomx * xRandomImpact - xRandomImpact;
            calculatedRandomx /= 2f;
            Vector2 velocity = new Vector2((float) (calculatedRandomx), (float) (calculatedRandom));
            velocity.scl(.5f);
            particles.add(new Particle(particlePosition, velocity));
        }
        for (int i = 0; i < particles.size(); i++) {
            Particle particle = particles.get(i);
            particle.velocity.sub(0, gravity*delta);
            particle.position.add(particle.velocity.cpy().scl(delta*60));
            particle.elapsedTime += delta;
            if (particle.elapsedTime >= particleLife) {
                particles.remove(i);
                i--;
            }
        }
    }

    @Override
    public void render(Batch batch) {
        float mainAlpha = 1 - elapsedTime / duration;
        mainAlpha = Interpolation.exp5.apply(mainAlpha);
        for (int i = 0; i < particles.size(); i++) {
            Particle particle = particles.get(i);
            float alpha = 1 - particle.elapsedTime / particleLife;
            batch.setColor(1, 1, 1, alpha * mainAlpha);
            float x = particle.position.x - Tile.SIZE / 2;
            float y = particle.position.y - Tile.SIZE / 2;
            if (followEntity) {
                x += position.x;
                y += position.y;
            }
            batch.draw(getParticleTexture(particle.elapsedTime), x, y, Tile.SIZE / 2, Tile.SIZE / 2, Tile.SIZE, Tile.SIZE, scale, scale, 0);
        }
        //batch.draw(Sprites.sprite(spriteType), position.x - Tile.SIZE / 2, position.y - Tile.SIZE / 2, Tile.SIZE, Tile.SIZE);
        super.render(batch);
    }

    private TextureRegion getParticleTexture(float delta) {
        if (spriteType != null) {
            return Sprites.sprite(spriteType);
        }
        if (animationType != null) {
            return Sprites.animation(animationType, delta * animationSpeed);
        }
        return null;
    }
    

    @Override
    public float getDuration() {
        return duration;
    }

    @Override
    public void serialize(DataBuffer buffer) throws IOException {
        super.serialize(buffer);
        buffer.writeInt(VERSION);
        buffer.writeFloat(duration);
        buffer.writeFloat(scale);
        buffer.writeFloat(emissionRate);
        buffer.writeFloat(particleLife);
        buffer.writeFloat(animationSpeed);
        buffer.writeBoolean(followEntity);
        if (spriteType != null) {
            buffer.writeUTF(spriteType.name());
        } else {
            buffer.writeUTF("");
        }
        if (animationType != null) {
            buffer.writeUTF(animationType.name());
        } else {
            buffer.writeUTF("");
        }
        buffer.writeFloat(areaSize);
        buffer.writeFloat(randomVelocityImpact);
        buffer.writeFloat(gravity);
        buffer.writeFloat(xRandomImpact);
    }

    @Override
    public void deserialize(DataInputStream input) throws IOException {
        super.deserialize(input);
        int version = input.readInt();
        duration = input.readFloat();
        scale = input.readFloat();
        emissionRate = input.readFloat();
        particleLife = input.readFloat();
        animationSpeed = input.readFloat();
        followEntity = input.readBoolean();
        String spriteName = input.readUTF();
        if (!spriteName.equals("")) {
            spriteType = SpriteType.valueOf(spriteName);
        }
        String animationName = input.readUTF();
        if (!animationName.equals("")) {
            animationType = AnimationType.valueOf(animationName);
        }
        areaSize = input.readFloat();
        if (version >= 1) {
            randomVelocityImpact = input.readFloat();
        }
        if (version >= 2) {
            gravity = input.readFloat();
            xRandomImpact = input.readFloat();
        }
    }

}
