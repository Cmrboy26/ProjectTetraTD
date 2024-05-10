package net.cmr.rtd.game.world.entities;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.DataBuffer;

import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.GameObject;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.World;
import net.cmr.rtd.game.world.entities.EnemyEntity.DamageType;
import net.cmr.rtd.game.world.particles.ParticleEffect;
import net.cmr.rtd.game.world.tile.Tile;
import net.cmr.util.Audio;
import net.cmr.util.Audio.GameSFX;
import net.cmr.util.Log;
import net.cmr.util.Sprites;
import net.cmr.util.Sprites.AnimationType;
import net.cmr.util.Sprites.SpriteType;
import net.cmr.util.UUIDUtils;

public class Projectile extends Entity {

    public static final float NO_AOE = 0;

    SpriteType sprite;
    AnimationType animation;

    UUID target;
    int damage;
    float timeToReachTarget;
    float elapsedTime;
    /**
     * In tiles
     */
    float AOE = 0;
    float scale = 1;
    Vector2 velocity = new Vector2();
    float precision;
    ParticleEffect particleOnHit;
    DamageType damageType;
    GameSFX onHitSound = null;
    GameSFX onLaunchSound = null;

    public static class ProjectileBuilder {
        private EnemyEntity entity;
        private SpriteType sprite;
        private AnimationType animation;
        private Vector2 position;
        private float scale = 1;
        private int damage;
        private float timeToReachTarget;
        private float AOE = 0;
        private float precision;
        private GameSFX onHitSound = null;
        private GameSFX onLaunchSound = null;

        public ProjectileBuilder setEntity(EnemyEntity entity) {
            this.entity = entity;
            return this;
        }

        public ProjectileBuilder setSprite(SpriteType sprite) {
            this.sprite = sprite;
            return this;
        }

        public ProjectileBuilder setAnimation(AnimationType animation) {
            this.animation = animation;
            return this;
        }

        public ProjectileBuilder setPosition(Vector2 position) {
            this.position = position;
            return this;
        }

        public ProjectileBuilder setScale(float scale) {
            this.scale = scale;
            return this;
        }

        public ProjectileBuilder setDamage(int damage) {
            this.damage = damage;
            return this;
        }

        public ProjectileBuilder setTimeToReachTarget(float timeToReachTarget) {
            this.timeToReachTarget = timeToReachTarget;
            return this;
        }

        public ProjectileBuilder setAOE(float AOE) {
            this.AOE = AOE;
            return this;
        }

        public ProjectileBuilder setPrecision(float precision) {
            this.precision = precision;
            return this;
        }

        public ProjectileBuilder setOnHitSound(GameSFX onHitSound) {
            this.onHitSound = onHitSound;
            return this;
        }

        public ProjectileBuilder setOnLaunchSound(GameSFX onLaunchSound) {
            this.onLaunchSound = onLaunchSound;
            return this;
        }

        public Projectile build() {
            return new Projectile(entity, sprite, animation, scale, position, damage, timeToReachTarget, AOE, precision, onHitSound, onLaunchSound);
        }
    }

    public Projectile() {
        super(GameType.PROJECTILE);
    }

    /*public Projectile(EnemyEntity entity, SpriteType type, Vector2 position, float scale, int damage, float timeToReachTarget, float AOE, float precision) {
        this(entity, type, null, scale, position, damage, timeToReachTarget, AOE, precision);
    }

    public Projectile(EnemyEntity entity, AnimationType type, Vector2 position, float scale, int damage, float timeToReachTarget, float AOE, float precision) {
        this(entity, null, type, scale, position, damage, timeToReachTarget, AOE, precision);
    }*/

    public Projectile(EnemyEntity entity, SpriteType sprite, AnimationType animation, float scale, Vector2 position, int damage, float timeToReachTarget, float AOE, float precision, GameSFX onHitSound, GameSFX onLaunchSound) {
        super(GameType.PROJECTILE);
        this.animation = animation;
        this.sprite = sprite;
        this.target = entity.getID();
        this.setPosition(position);
        this.damage = damage;
        this.timeToReachTarget = timeToReachTarget;
        this.velocity.set(entity.launchProjectileAt(this, timeToReachTarget, precision));
        this.scale = scale;
        this.AOE = AOE;
        this.precision = precision;
        this.onHitSound = onHitSound;
        this.onLaunchSound = onLaunchSound;
    }

    public void setParticleOnHit(ParticleEffect effect) {
        this.particleOnHit = effect;
    }

    @Override
    public float getRenderOffset() {
        return -Tile.SIZE;
    }

    boolean playedLaunchSound = false;

    @Override
    public void update(float delta, UpdateData data) {
        super.update(delta, data);
        elapsedTime += delta;
        
        if (!playedLaunchSound) {
            if (data.isClient() && onLaunchSound != null) {
                Audio.getInstance().playSFX(onLaunchSound, 1, 1);
            }
            playedLaunchSound = true;
        }

        if (elapsedTime >= timeToReachTarget) {
            World world = data.getWorld();
            Entity entity = world.getEntity(target);
            if (entity != null && entity instanceof EnemyEntity) {
                EnemyEntity enemy = (EnemyEntity) entity;

                if (AOE > 0) {
                    for (Entity e : world.getEntities()) {
                        if (e instanceof EnemyEntity) {
                            EnemyEntity enemyEntity = (EnemyEntity) e;
                            if (enemyEntity.getPosition().dst(getPosition()) < AOE) {
                                enemyEntity.damage(damage, data, DamageType.PHYSICAL);
                            }
                        }
                    }
                } else {
                    // Damage the target enemy
                    enemy.damage(damage, data, DamageType.PHYSICAL);
                    Log.debug("PROJECTILE HIT "+enemy);
                }
            }
            removeFromWorld();
            if (particleOnHit != null) {
                particleOnHit.setPosition(getPosition());
                if (data.isClient()) {
                    data.getScreen().addEffect(particleOnHit);
                }
            }
            if (data.isClient()) {
                if (onHitSound != null) {
                    Audio.getInstance().playSFX(onHitSound, 1, 1);
                }
            }
            // Display a particle?
            return;
        }

        if (velocity.isZero()) {
            World world = data.getWorld();
            Entity entity = world.getEntity(target);
            if (entity != null && entity instanceof EnemyEntity) {
                EnemyEntity enemy = (EnemyEntity) entity;
                velocity.set(enemy.launchProjectileAt(this, timeToReachTarget-elapsedTime, precision));
            } else {
                // No target, remove the projectile
                removeFromWorld();
                return;
            }
        }

        move(velocity.x, velocity.y, delta);
    }

    @Override
    protected void serializeEntity(DataBuffer buffer) throws IOException {
        UUIDUtils.serializeUUID(buffer, target);
        buffer.writeInt(damage);
        buffer.writeFloat(timeToReachTarget);
        buffer.writeFloat(elapsedTime);
        buffer.writeFloat(velocity.x);
        buffer.writeFloat(velocity.y);
        buffer.writeFloat(AOE);
        buffer.writeFloat(precision);
        buffer.writeFloat(scale);
        buffer.writeUTF(sprite == null ? "" : sprite.name());
    }

    @Override
    protected void deserializeEntity(GameObject object, DataInputStream input) throws IOException {
        Projectile projectile = (Projectile) object;
        projectile.target = UUIDUtils.deserializeUUID(input);
        projectile.damage = input.readInt();
        projectile.timeToReachTarget = input.readFloat();
        projectile.elapsedTime = input.readFloat();
        projectile.velocity.set(input.readFloat(), input.readFloat());
        projectile.AOE = input.readFloat();
        projectile.precision = input.readFloat();
        projectile.scale = input.readFloat();
        projectile.sprite = SpriteType.valueOf(input.readUTF());
    }
    
    float renderDelta = 0;

    @Override
    public void render(UpdateData data, Batch batch, float delta) {
        float size = .20f * scale;
        renderDelta += delta;
        if (animation != null) {
            batch.draw(Sprites.animation(animation, renderDelta), getX() - Tile.SIZE * size / 2, getY() - Tile.SIZE * size / 2, Tile.SIZE * size, Tile.SIZE * size);
        } else if (sprite != null) {
            batch.draw(Sprites.sprite(sprite), getX() - Tile.SIZE * size / 2, getY() - Tile.SIZE * size / 2, Tile.SIZE * size, Tile.SIZE * size);
        } else {
            batch.draw(Sprites.sprite(SpriteType.PROJECTILE), getX() - Tile.SIZE * size / 2, getY() - Tile.SIZE * size / 2, Tile.SIZE * size, Tile.SIZE * size);
        }
        super.render(data, batch, delta);
    }

    public Vector2 getVelocity() {
        return velocity;
    }

}
