package net.cmr.rtd.game.world.entities;

import java.io.DataInputStream;
import java.io.IOException;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.DataBuffer;

import net.cmr.rtd.game.GameManager;
import net.cmr.rtd.game.world.EnemyFactory.EnemyType;
import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.GameObject;
import net.cmr.rtd.game.world.TeamData;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.World;
import net.cmr.rtd.game.world.entities.effects.Effect;
import net.cmr.rtd.game.world.tile.StructureTileData;
import net.cmr.rtd.game.world.tile.Tile;
import net.cmr.rtd.game.world.tile.TileData;
import net.cmr.util.Audio;
import net.cmr.util.Audio.GameSFX;

public abstract class EnemyEntity extends Entity {

    public int team;
    public int health;
    public static final int VERSION = 2;
    public float distanceTraveled = 0;
    public EnemyType enemyType;
    public int totalEnemiesInWave; // Used to calculate probability of getting a component

    protected EnemyEntity(int team, GameType type, EnemyType enemyType) {
        super(type);
        this.team = team;
        this.enemyType = enemyType;
    }

    @Override
    public void update(float delta, UpdateData data) {
        super.update(delta, data);
        distanceTraveled += delta * getSpeed();
        if (health <= 0) {
            onDeath(data);
            if (data.isServer()) {
                removeFromWorld();
                updatePresenceOnClients(data.getManager());
            }
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(getX() - Tile.SIZE / 2, getY() - Tile.SIZE / 2, Tile.SIZE, Tile.SIZE);
    }

    protected abstract void serializeEnemy(DataBuffer buffer) throws IOException;
    protected abstract void deserializeEnemy(GameObject object, DataInputStream input) throws IOException;

    // Used to calculate if the entity is closest to the tower
    public float getDistanceTraveled() {
        return distanceTraveled;
    }

    public abstract int getMaxHealth();
    public final float getSpeed() {
        return getTargetSpeed();
    }
    public abstract float getTargetSpeed();

    public int getHealth() {
        return health;
    }
    public void setHealth(int health) {
        this.health = health;
    }

    public enum DamageType {
        PHYSICAL,
        FIRE,
    }

    public final void damage(int damage, UpdateData data, DamageType type) {
        health -= damage;
        onDamage(damage, data, type);
    }
    public final void heal(int heal, UpdateData data, DamageType type) {
        health += heal;
        onHeal(heal, data, type);
    }
    public int getTeam() {
        return team;
    }

    public void onDamage(int damage, UpdateData data, DamageType type) {
        // Override this method to add custom behavior when the entity is damaged
    }

    public void onHeal(int heal, UpdateData data, DamageType type) {
        // Override this method to add custom behavior when the entity is healed
    }

    public void playHitSound(UpdateData data, DamageType type) {
        int randomNumber = (int) Math.floor(Math.random() * 3);
        randomNumber++;
        // play the sound effect
        if (data.isClient()) {
            if (type == DamageType.FIRE) {
                Audio.getInstance().worldSFX(GameSFX.FIRE_DAMAGE, .4f, .25f, getPosition(), data.getScreen());
            } else {
                if (randomNumber == 1) {
                    Audio.getInstance().worldSFX(GameSFX.HIT1, 1, .8f, getPosition(), data.getScreen());
                } else if (randomNumber == 2) {
                    Audio.getInstance().worldSFX(GameSFX.HIT2, 1, .8f, getPosition(), data.getScreen());
                } else {
                    Audio.getInstance().worldSFX(GameSFX.HIT3, 1, .8f, getPosition(), data.getScreen());
                }
            }
        }
    }

    public void attackStructure(int tileX, int tileY, UpdateData data, int damage) {
        // Get the endTileData from the tile at the given coordinates
        // Call the damage method on the endTileData
        World world = data.getWorld();
        TileData tileData = world.getTileData(tileX, tileY, 1);
        if (tileData instanceof StructureTileData) {
            StructureTileData endTileData = (StructureTileData) tileData;
            endTileData.damage(damage);
        }
    }

    public void onDeath(UpdateData data) {
        if (data.isServer()) {
            TeamData team = data.getManager().getTeam(this.team);
            team.onEnemyDeath(this, data);
            team.rollRandomItem(this, data);
        }
    }

    @Override
    protected final void serializeEntity(DataBuffer buffer) throws IOException {
        buffer.writeInt(VERSION);
        buffer.writeInt(team);
        buffer.writeInt(health);
        buffer.writeFloat(distanceTraveled);
        buffer.writeInt(enemyType.ordinal());
        buffer.writeInt(totalEnemiesInWave);
        serializeEnemy(buffer);
    }

    @Override
    protected final void deserializeEntity(GameObject object, DataInputStream input) throws IOException {
        EnemyEntity entity = (EnemyEntity) object;
        int version = input.readInt();
        entity.team = input.readInt();
        entity.health = input.readInt();
        if (version >= 0) {
            entity.distanceTraveled = input.readFloat();
        }
        if (version >= 1) {
            entity.enemyType = EnemyType.values()[input.readInt()];
        }
        if (version >= 2) {
            entity.totalEnemiesInWave = input.readInt();
        }
        deserializeEnemy(object, input);
    }

    /**
     * Helper method: Deposit money into the team's structure when they successfully kill it
     */
    public void moneyOnDeath(EnemyEntity entity, UpdateData data, long quantity) {
        if (data.isServer()) {
            GameManager manager = data.getManager();
            manager.getTeam(entity.getTeam()).depositMoney(quantity, data);
        }
    }

    /**
     * A helper method to launch a projectile at this entity.
     * Uses the given target and speed to calculate the velocity of the projectile.
     * @param projectile the projectile to launch
     * @param timeToReach the time it should take for the projectile to reach the target
     * @return the calculated velocity of the projectile
     */
    public Vector2 launchProjectileAt(Entity projectile, float timeToReach, float precision) {
        if (timeToReach <= 0) {
            throw new IllegalArgumentException("Time to reach must be greater than 0");
        }
        // TODO: Implement a better way to calculate the ending position of the enemy
        precision = Math.min(1, Math.max(0, precision));
        float velX = ((getX() + getVelocity().x * timeToReach * Tile.SIZE * precision) - projectile.getX()) / timeToReach;
        float velY = ((getY() + getVelocity().y * timeToReach * Tile.SIZE * precision) - projectile.getY()) / timeToReach;
        Vector2 velocity = new Vector2(velX, velY);
        return velocity;
    }

    public abstract Vector2 getVelocity();

    public EnemyEntity immuneTo(Class<? extends Effect> effect) {
        getEffects().addImmunity(effect);
        return this;
    }

}
