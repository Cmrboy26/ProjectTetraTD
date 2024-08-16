package net.cmr.rtd.game.world.entities.towers;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.DataBuffer;

import net.cmr.rtd.game.storage.TeamInventory.Material;
import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.entities.EnemyEntity;
import net.cmr.rtd.game.world.entities.Projectile;
import net.cmr.rtd.game.world.entities.Projectile.ProjectileBuilder;
import net.cmr.rtd.game.world.entities.TowerDescription;
import net.cmr.rtd.game.world.entities.TowerEntity;
import net.cmr.rtd.game.world.entities.effects.FireEffect;
import net.cmr.rtd.game.world.particles.ParticleCatalog;
import net.cmr.rtd.game.world.tile.Tile;
import net.cmr.util.Audio.GameSFX;
import net.cmr.util.Sprites;
import net.cmr.util.Sprites.AnimationType;
import net.cmr.util.Sprites.SpriteType;
import net.cmr.util.StringUtils;

public class FireTower extends TowerEntity {

    float fireballDamage = 2;

    boolean attacking = false;
    float animationDelta = 0;
    float fireballDelta = 0;

    public FireTower() {
        super(GameType.FIRE_TOWER, 0);
    }

    public FireTower(int team) {
        super(GameType.FIRE_TOWER, team);
    }

    @Override
    public boolean attack(UpdateData data) {
        super.attack(data);
        ArrayList<EnemyEntity> entitiesInRange = getEnemiesInRange(getRange(), data, getPreferedSortType());
        boolean launchedFireball = false;
        if (fireballDelta < (Math.max(2f, (6f - ((getLevel() - 1) * .5f)))/getLubricantSpeedBoost()) / Material.getAttackSpeedModifier(getSelectedMaterial())) {
            launchedFireball = true;
        } 

        boolean actionOccured = false;
        for (Entity entity : entitiesInRange) {
            if (entity instanceof EnemyEntity) {
                EnemyEntity enemy = (EnemyEntity) entity;
                Material.attackEnemy(enemy, this, data);
                new FireEffect(data, enemy.getEffects(), getLevel(), getFireEffectLevel());
                actionOccured = true;
                if (!launchedFireball && data.isServer()) {
                    float damage = getDamage(true);
                    if (wasCriticalHit(damage)) {
                        displayCriticalHit(data);
                    }
                    ProjectileBuilder builder = new ProjectileBuilder()
                        .setEntity(enemy)
                        .setAnimation(AnimationType.FIRE)
                        .setPosition(new Vector2(getPosition()))
                        .setScale(3f)
                        .setDamage((int) damage)
                        .setTimeToReachTarget(1)
                        .setAOE(getFireballAOE())
                        .setPrecision(1)
                        .setOnHitSound(GameSFX.FIREBALL_HIT)
                        .setOnLaunchSound(GameSFX.FIREBALL_LAUNCH);
                    Projectile fireball = builder.build();
                    fireball.setParticleOnHit(ParticleCatalog.fireballHitEffect(this));
                    if (fireball.getVelocity().len() > (getRange() + 1)*Tile.SIZE) {
                        // dont launch it
                        continue;
                    }
                    Projectile.launchProjectile(data, fireball);
                    launchedFireball = true;
                    fireballDelta = 0;
                }
            }
        }
        // Fireball can only be fired after a certain amount of time where enemies have been in range
        if (actionOccured) {
            fireballDelta += getAttackSpeed();
        }
        return actionOccured;
    }

    public int getFireEffectLevel() {
        return (int) Math.floor(1+((getLevel() - 1) / 2f));
    }

    public float getFireballAOE() {
        return .5f * getScopeRangeBoost();
    }

    public int getFireballDamage(boolean rollCritical) {
        float fireballDamage = getLevel() * getLevel() + getLevel() + 5;
        return (int) (fireballDamage * Material.getDamageModifier(getSelectedMaterial(), rollCritical));
    }

    @Override
    protected void serializeTower(DataBuffer buffer) throws IOException {
        buffer.writeFloat(fireballDelta);
    }

    @Override
    protected void deserializeTower(TowerEntity entity, DataInputStream input) throws IOException {
        FireTower tower = (FireTower) entity;
        tower.fireballDelta = input.readFloat();
    }

    @Override
    public float getAttackSpeed() {
        return .1f/getLubricantSpeedBoost();
    }

    public float getFireballPeriod() {
        float timeBetweenFireballs = (Math.max(2f, (6f - ((getLevel() - 1) * .5f)))/getLubricantSpeedBoost()) / Material.getAttackSpeedModifier(getSelectedMaterial());
        return timeBetweenFireballs;
    }

    public float calculateApproximateFireballDPS(float enemyDensity) {
        float damagePerFireball = getFireballDamage(false);
        float fireballDPS = damagePerFireball / getFireballPeriod();
        float areaFactor = enemyDensity * getFireballAOE();
        return fireballDPS * areaFactor;
    }

    @Override
    public void render(UpdateData data, Batch batch, float delta) {
        preRender(data, batch, delta);

        animationDelta += delta;
        /*if (attacking) {
            animationDelta += delta;
        } else {
            animationDelta = 0;
        }*/

        Color color = new Color(Color.WHITE);
        color.a = batch.getColor().a;
        batch.setColor(color);
        TextureRegion sprite = Sprites.animation(AnimationType.FIRE_TOWER, animationDelta);
        batch.draw(sprite, getX() - Tile.SIZE / 2, getY() - Tile.SIZE / 2, Tile.SIZE, Tile.SIZE * 1.5f);
        batch.setColor(Color.WHITE);

        postRender(data, batch, delta);
        super.render(data, batch, delta);
    }

    @Override
    public float getRange() {
        return (float) (2 + Math.sqrt(getLevel() / 6f))*getScopeRangeBoost()*Material.getRangeModifier(getSelectedMaterial());
    }

    @Override
    public float getDamage(boolean rollCritical) {
        return getFireballDamage(rollCritical);
    }

    @Override
    public String getDescription() {
        return "This tower sets enemies ablaze, dealing damage over time. It also has a chance to launch a fireball at enemies in range.";
    }
    
    @Override
    public Table getTowerDescription() {
        TowerDescription description = TowerDescription.getFullDescription();
        description.removeDescriptor(TowerDescription.TowerDescriptors.DPS);
        description.removeDescriptor(TowerDescription.TowerDescriptors.DPS_EXTENDED);

        description.createCustomDoubleSection(
            Sprites.animation(AnimationType.FIRE, 0f), "Fireball Damage: " + StringUtils.truncateFloatingPoint(getFireballDamage(false), 2),
            Sprites.sprite(SpriteType.ATTACK_SPEED_ICON), "Fireball Period: " + StringUtils.truncateFloatingPoint(getFireballPeriod(), 2)+"s"  
        );
        description.createCustomSection(
            Sprites.sprite(SpriteType.DPS_ICON), "Fire Effect DPS: " + StringUtils.truncateFloatingPoint(FireEffect.getDPS(getFireEffectLevel()), 2)
        );

        return description.create(this);
    }

    @Override
    public Material[] getValidMaterials() {
        return new Material[] { Material.THORIUM, Material.RUBY, Material.QUARTZ, Material.TOPAZ };
    }
    
}
