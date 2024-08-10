package net.cmr.rtd.game.world.entities.towers;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.DataBuffer;

import net.cmr.rtd.game.storage.TeamInventory.Material;
import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.entities.EnemyEntity;
import net.cmr.rtd.game.world.entities.TowerDescription;
import net.cmr.rtd.game.world.entities.TowerDescription.TowerDescriptors;
import net.cmr.rtd.game.world.entities.TowerEntity;
import net.cmr.rtd.game.world.entities.effects.SlownessEffect;
import net.cmr.rtd.game.world.particles.ParticleCatalog;
import net.cmr.rtd.game.world.particles.ParticleEffect;
import net.cmr.rtd.game.world.tile.Tile;
import net.cmr.util.Sprites;
import net.cmr.util.Sprites.AnimationType;
import net.cmr.util.Sprites.SpriteType;

public class IceTower extends TowerEntity {

    boolean attacking = false;
    float animationDelta = 0;
    final float persistence = 1;
    float range = 2;

    public IceTower() {
        super(GameType.ICE_TOWER, 0);
    }

    public IceTower(int team) {
        super(GameType.ICE_TOWER, team);
    }

    @Override
    public void update(float delta, UpdateData data) {
        super.update(delta, data);
        
        if (animationDelta > .5f && attacking) {
            animationDelta = 0;
            // Ice particle around the tower
            ParticleEffect effect = ParticleCatalog.iceTowerFrozenEffect(this);
            if (data.isClient()) {
                data.getScreen().addEffect(effect);
            }
        }
    }

    @Override
    public boolean attack(UpdateData data) {
        super.attack(data);
        ArrayList<EnemyEntity> entitiesInRange = getEnemiesInRange(getRange(), data);
        attacking = false;
        for (Entity entity : entitiesInRange) {
            attacking = true;
            if (entity instanceof EnemyEntity) {
                EnemyEntity enemy = (EnemyEntity) entity;
                Material.attackEnemy(enemy, this, data);
                new SlownessEffect(data, enemy.getEffects(), getAttackSpeed() + (persistence * getLevel() / 2f), getLevel());
            }
        }
        return attacking;
    }
    
    @Override
    public float getAttackSpeed() {
        return .5f/getLubricantSpeedBoost();
    }

    @Override
    public float getRange() {
        return calculateIncrementedValue(3, .25f, range)*getScopeRangeBoost()*Material.getRangeModifier(getSelectedMaterial());
    }

    @Override
    protected void serializeTower(DataBuffer buffer) throws IOException {
        
    }

    @Override
    protected void deserializeTower(TowerEntity entity, DataInputStream input) throws IOException {

    }

    @Override
    public void render(UpdateData data, Batch batch, float delta) {
        preRender(data, batch, delta);

        animationDelta += delta;

        Color color = new Color(Color.WHITE);
        color.a = batch.getColor().a;
        batch.setColor(color);
        SpriteType spriteType = getTowerSpriteLevelDependent(
            new SpriteType[] {
                SpriteType.ICE_TOWER_1, 
                SpriteType.ICE_TOWER_2, 
                SpriteType.ICE_TOWER_3,
                SpriteType.ICE_TOWER_4,
                SpriteType.ICE_TOWER_5,
                SpriteType.ICE_TOWER_6,
            }, 
            new int[] {
                1, 2, 3, 4, 5, 6
            }
        );
        TextureRegion sprite = Sprites.sprite(spriteType);
        batch.draw(sprite, getX() - Tile.SIZE / 2, getY() - Tile.SIZE / 2, Tile.SIZE, Tile.SIZE);
        batch.setColor(Color.WHITE);
        
        postRender(data, batch, delta);
        super.render(data, batch, delta);
    }

    @Override
    public float getDamage(boolean rollCritical) { return 0*getScrapMetalDamageBoost(); }
    @Override
    public String getDescription() { return "Slows down enemies within range.\nSlowness increases as level increases."; }

    @Override
    public Table getTowerDescription() {
        TowerDescription description = TowerDescription.getFullDescription();
        description.removeDescriptor(TowerDescriptors.DPS);
        description.removeDescriptor(TowerDescriptors.DPS_EXTENDED);

        float speedMultiplier = SlownessEffect.getSlowdownMultiplier(getLevel());
        String speedReductionPercent = ((int)((1f-speedMultiplier)*100f))+"%";
        String slowdownPercent = "Speed Reduction: "+speedReductionPercent;
        description.createCustomSection(Sprites.sprite(SpriteType.FROZEN), slowdownPercent);
        /*if (getSelectedMaterial() == Material.CRYONITE) {
            String cryoniteSpecialAbility = "Special Ability: Enemies in range of the tower have a chance to be temporarily stunned.";
            description.createCustomSection(Sprites.drawable(SpriteType.CRYONITE), cryoniteSpecialAbility);
        }*/
        return description.create(this);
    }

    @Override
    public boolean canEditSortType() {
        return false;
    }

    @Override
    public Material[] getValidMaterials() {
        return new Material[] { Material.CRYONITE, Material.QUARTZ };
    }
    
}
