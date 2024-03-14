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
import net.cmr.rtd.game.world.tile.Tile;
import net.cmr.util.Log;
import net.cmr.util.Sprites;
import net.cmr.util.Sprites.SpriteType;
import net.cmr.util.UUIDUtils;

public class Projectile extends Entity {

    public static final float NO_AOE = 0;

    UUID target;
    int damage;
    float timeToReachTarget;
    float elapsedTime;
    /**
     * In tiles
     */
    float AOE = 0;
    Vector2 velocity = new Vector2();

    public Projectile(EnemyEntity entity, int damage, float timeToReachTarget, float AOE) {
        super(GameType.PROJECTILE);
        this.target = entity.getID();
        this.damage = damage;
        this.timeToReachTarget = timeToReachTarget;
        this.velocity.set(entity.launchProjectileAt(this, timeToReachTarget));
        this.AOE = AOE;
    }

    @Override
    public void update(float delta, UpdateData data) {
        super.update(delta, data);
        elapsedTime += delta;
        System.out.println(elapsedTime+" "+timeToReachTarget+" "+getPosition());

        if (elapsedTime >= timeToReachTarget) {
            World world = data.getWorld();
            Entity entity = world.getEntity(target);
            System.out.println("PROJECTILE HIT! "+entity+System.currentTimeMillis());
            if (entity != null && entity instanceof EnemyEntity) {
                EnemyEntity enemy = (EnemyEntity) entity;
                enemy.damage(damage);

                if (AOE > 0) {
                    for (Entity e : world.getEntities()) {
                        if (e instanceof EnemyEntity) {
                            EnemyEntity enemyEntity = (EnemyEntity) e;
                            if (enemyEntity.getPosition().dst(getPosition()) < AOE) {
                                enemyEntity.damage(damage);
                                Log.debug("PROJECTILE HIT AOE "+enemy);
                            }
                        }
                    }
                } else {
                    // Damage the target enemy
                    enemy.damage(damage);
                    Log.debug("PROJECTILE HIT "+enemy);
                }
            }
            removeFromWorld();
            // Display a particle?
            return;
        }

        if (velocity.isZero()) {
            World world = data.getWorld();
            Entity entity = world.getEntity(target);
            if (entity != null && entity instanceof EnemyEntity) {
                EnemyEntity enemy = (EnemyEntity) entity;
                velocity.set(enemy.launchProjectileAt(this, timeToReachTarget-elapsedTime));
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
    }
    
    @Override
    public void render(Batch batch, float delta) {
        float size = .20f;
        batch.draw(Sprites.sprite(SpriteType.PROJECTILE), getX() - Tile.SIZE * size / 2, getY() - Tile.SIZE * size / 2, Tile.SIZE * size, Tile.SIZE * size);
        super.render(batch, delta);
    }

}
