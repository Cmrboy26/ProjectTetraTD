package net.cmr.rtd.game.world.entities.towers;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.DataBuffer;

import net.cmr.rtd.game.storage.TeamInventory.Material;
import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.entities.EnemyEntity;
import net.cmr.rtd.game.world.entities.Projectile;
import net.cmr.rtd.game.world.entities.Projectile.ProjectileBuilder;
import net.cmr.rtd.game.world.entities.TowerEntity;
import net.cmr.rtd.game.world.tile.Tile;
import net.cmr.util.Audio.GameSFX;
import net.cmr.util.Audio;
import net.cmr.util.Sprites;
import net.cmr.util.Sprites.AnimationType;
import net.cmr.util.Sprites.SpriteType;

public class ShooterTower extends TowerEntity {

    public ShooterTower() {
        super(GameType.SHOOTER_TOWER, 0);
    }

    public ShooterTower(int team) {
        super(GameType.SHOOTER_TOWER, team);
    }

    @Override
    public boolean attack(UpdateData data) {
        super.attack(data);
        float damage = getDamage(true);
        float range = getRange();
        ArrayList<EnemyEntity> entitiesInRange = getEnemiesInRange(range, data, getPreferedSortType());
        attacking = false;
        for (Entity entity : entitiesInRange) {
            if (entity instanceof EnemyEntity) {
                EnemyEntity enemy = (EnemyEntity) entity;
                Material.attackEnemy(enemy, this, data);
                boolean piercing = Material.isPiercing(getSelectedMaterial());
                ProjectileBuilder builder = new ProjectileBuilder()
                        .setEntity(enemy)
                        .setSprite(SpriteType.PROJECTILE)
                        .setPosition(new Vector2(getPosition()))
                        .setScale(.5f)
                        .setDamage((int) damage)
                        .setTimeToReachTarget(getProjectileAirTime())
                        .setPrecision(1)
                        .setAOE(piercing ? .25f : 0)
                        .setOnLaunchSound(GameSFX.SHOOT);
                Projectile arrow = builder.build();
                Projectile.launchProjectile(data, arrow);
                animationDelta = 0;
                attacking = true;
                return attacking;
            }
        }
        return attacking;
    }

    public float getProjectileAirTime() {
        return Math.max((.25f - ((getLevel() - 1) * .005f)) / getLubricantSpeedBoost(), 0);
    }

    @Override
    protected void serializeTower(DataBuffer buffer) throws IOException {

    }

    @Override
    protected void deserializeTower(TowerEntity entity, DataInputStream input) throws IOException {

    }

    @Override
    public float getAttackSpeed() {
        return Math.max(((1 - ((getLevel() - 1) * .05f)) / getLubricantSpeedBoost()) / Material.getAttackSpeedModifier(getSelectedMaterial()), 0.1f);
    }

    @Override
    public float getDamage(boolean rollCritical) {
        float damage = calculateIncrementedValue(1, 1, 1);
        damage *= damage * .5f;
        return (float) Math.ceil(damage * getScrapMetalDamageBoost() * Material.getDamageModifier(getSelectedMaterial(), rollCritical));
    }

    @Override
    public float getRange() {
        return calculateIncrementedValue(3, .25f, 2f)*getScopeRangeBoost() * Material.getRangeModifier(getSelectedMaterial());
    }

    @Override
    public String getDescription() {
        return "Shoots pellets at enemies.";
    }

    float animationDelta = 0;
    boolean attacking = false;

    @Override
    public void render(UpdateData data, Batch batch, float delta) {
        preRender(data, batch, delta);
        
        if (attacking) {
            animationDelta += delta/getAttackSpeed();
        } else {
            animationDelta = 0;
        }

        Color color = new Color(Color.WHITE);
        color.a = batch.getColor().a;
        batch.setColor(color);
        AnimationType animation = getTowerAnimationLevelDependent(
            new AnimationType[] {
                AnimationType.SHOOTER_TOWER_1, 
                AnimationType.SHOOTER_TOWER_2, 
                AnimationType.SHOOTER_TOWER_3,
                AnimationType.SHOOTER_TOWER_4,
                AnimationType.SHOOTER_TOWER_5,
            }, 
            new int[] {
                1, 2, 3, 4, 5
            }
        );
        TextureRegion sprite = Sprites.animation(animation, animationDelta); //Sprites.sprite(Sprites.SpriteType.CMRBOY26)
        batch.draw(sprite, getX() - Tile.SIZE / 2, getY() - Tile.SIZE / 2, Tile.SIZE, Tile.SIZE);
        batch.setColor(Color.WHITE);

        postRender(data, batch, delta);
        super.render(data, batch, delta);
    }

    @Override
    public void onAttackClient(UpdateData data) {
        animationDelta = 0;
        attacking = true;
    }

    @Override
    public Material[] getValidMaterials() {
        return new Material[] { Material.DIAMONDS, Material.CRYONITE, Material.THORIUM, Material.RUBY, Material.QUARTZ, Material.TOPAZ };
    }
    
}
