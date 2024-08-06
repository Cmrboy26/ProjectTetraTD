package net.cmr.rtd.game.world.entities;

import java.util.ArrayList;
import java.util.HashSet;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import net.cmr.rtd.game.storage.TeamInventory.Material;
import net.cmr.util.Sprites;
import net.cmr.util.Sprites.SpriteType;
import net.cmr.util.StringUtils;

public class TowerDescription {

    public enum TowerDescriptors {
        DESCRIPTION,
        LEVEL,
        RANGE,
        DPS,
        DPS_EXTENDED, // Attack speed, damage
        CRITICAL,
        COMPONENT,
        MATERIAL,
    }

    HashSet<TowerDescriptors> descriptors = new HashSet<TowerDescriptors>();
    ArrayList<Table> customSections = new ArrayList<Table>();

    public TowerDescription(TowerDescriptors... descriptiors) {
        for (TowerDescriptors descriptor : descriptiors) {
            descriptors.add(descriptor);
        }
    }

    public void removeDescriptor(TowerDescriptors descriptor) {
        descriptors.remove(descriptor);
    }
    public void addDescriptor(TowerDescriptors descriptor) {
        descriptors.add(descriptor);
    }

    /**
     * To be used for most/all combat towers
     */
    public static TowerDescription getFullDescription() {
        return new TowerDescription(TowerDescriptors.values());
    }
    /**
     * To be used primarily for non-combat towers, like the Drill Tower
     */
    public static TowerDescription getMinimalDescription() {
        return new TowerDescription(TowerDescriptors.LEVEL, TowerDescriptors.COMPONENT, TowerDescriptors.MATERIAL, TowerDescriptors.DESCRIPTION);
    }

    public Table create(TowerEntity tower) {
        Table table = new Table();
        table.setSize(200, 200);
        
        if (descriptors.contains(TowerDescriptors.LEVEL)) {
            table.add(new Image(Sprites.sprite(SpriteType.LEVEL_ICON))).colspan(1).left();
            table.add(descriptionLabel("Level: "+tower.getLevel())).colspan(1).padLeft(3).left().growX().row();
        }

        Table generalStatsTable = new Table();

        if (descriptors.contains(TowerDescriptors.DPS)) {
            Table dpsTable = new Table();
            float damage = tower.getDamage(false) * (1 + (tower.getCritDamagePercent() - 1) * tower.getCritChance());
            float dps = damage / tower.getAttackSpeed();
            dpsTable.add(new Image(Sprites.sprite(SpriteType.DPS_ICON))).colspan(1).left();
            dpsTable.add(descriptionLabel("DPS: "+StringUtils.truncateFloatingPoint(dps, 2))).colspan(1).padLeft(3).left().growX().row();
            generalStatsTable.add(dpsTable).growX();
        }

        if (descriptors.contains(TowerDescriptors.RANGE)) {
            Table rangeTable = new Table();
            rangeTable.add(new Image(Sprites.sprite(SpriteType.RANGE_ICON))).colspan(1).left();
            String range = StringUtils.truncateFloatingPoint(tower.getRange(), 2);
            rangeTable.add(descriptionLabel("Range: "+range)).colspan(1).padLeft(3).left().growX().row();
            generalStatsTable.add(rangeTable).growX();
        }
        table.add(generalStatsTable).colspan(2).growX().row();

        if (descriptors.contains(TowerDescriptors.DPS_EXTENDED)) {
            Table dpsComponentsTable = new Table();
            Table damageTable = new Table();
            damageTable.add(new Image(Sprites.sprite(SpriteType.DAMAGE_ICON))).colspan(1).left();
            String damage = StringUtils.truncateFloatingPoint(tower.getDamage(false), 2);
            damageTable.add(descriptionLabel("Damage: "+damage)).colspan(1).padLeft(3).left().growX().row();
    
            Table speedTable = new Table();
            speedTable.add(new Image(Sprites.sprite(SpriteType.ATTACK_SPEED_ICON))).colspan(1).left();
            String speed = StringUtils.truncateFloatingPoint(1f/tower.getAttackSpeed(), 2);
            speedTable.add(descriptionLabel("Speed: "+speed)).colspan(1).padLeft(3).left().growX().row();
    
            dpsComponentsTable.add(damageTable).growX();
            dpsComponentsTable.add(speedTable).growX();
            table.add(dpsComponentsTable).colspan(2).growX().row();
        }

        if (descriptors.contains(TowerDescriptors.CRITICAL)) {
            if (tower.getCritDamagePercent() != 1) {
                Table critTable = new Table();
                Table critDamageTable = new Table();
                critDamageTable.add(new Image(Sprites.sprite(SpriteType.CRITICAL_ICON))).colspan(1).left();
                String critDamage = StringUtils.truncateFloatingPoint(tower.getDamage(false) * tower.getCritDamagePercent(), 2);
                String multiplierText = "Crit Multiplier: x"+StringUtils.truncateFloatingPoint(tower.getCritDamagePercent(), 2);
                critDamageTable.add(descriptionLabel(multiplierText)).colspan(1).padLeft(3).left().growX().row();
                Table critChanceTable = new Table();
                critChanceTable.add(new Image(Sprites.sprite(SpriteType.CRITICAL_CHANCE_ICON))).colspan(1).left();
                critChanceTable.add(descriptionLabel("Crit Chance: "+StringUtils.truncateFloatingPoint(tower.getCritChance()*100, 2)+"%")).colspan(1).padLeft(3).left().growX().row();
    
                critTable.add(critDamageTable).growX();
                critTable.add(critChanceTable).growX();
                table.add(critTable).colspan(2).growX().row();
            }
        }

        if (descriptors.contains(TowerDescriptors.COMPONENT)) {
            SpriteType component = null;
            SpriteType benefitIcon = null;
            String componentInfo = "";
            componentInfo = tower.getComponentsApplied()+"/"+TowerEntity.MAX_COMPONENTS+" ";
            if (tower.getLubricantApplied() > 0) {
                component = SpriteType.LUBRICANT;
                componentInfo += "Lubricant (+"+tower.getLubricantSpeedBoostPercent()+"%";
                benefitIcon = SpriteType.ATTACK_SPEED_ICON;
            } else if (tower.getScopesApplied() > 0) {
                component = SpriteType.SCOPE;
                componentInfo += "Scope (+"+tower.getScopeRangeBoostPercent()+"%";
                benefitIcon = SpriteType.RANGE_ICON;
            } else if (tower.getScrapMetalApplied() > 0) {
                component = SpriteType.SCRAP;
                componentInfo += "Scrap Metal (+"+tower.getScrapMetalDamageBoostPercent()+"%";
                benefitIcon = SpriteType.DAMAGE_ICON;
            }

            if (component != null) {
                table.add(new Image(Sprites.sprite(component))).size(11, 11).colspan(1).left();
                Table text = new Table();
                text.add(descriptionLabel(componentInfo, false)).colspan(1).padLeft(3).fillX().left();
                text.add(new Image(Sprites.sprite(benefitIcon))).size(9, 9).colspan(1).padLeft(3).left();
                text.add(descriptionLabel(")", false)).colspan(1).padLeft(1).left().growX().row();
                table.add(text).colspan(1).left().growX().row();
            }
        }

        if (descriptors.contains(TowerDescriptors.MATERIAL)) {
            Material selectedMaterial = tower.getSelectedMaterial();
            if (selectedMaterial != null) {
                SpriteType gemSprite = selectedMaterial.image;
                String desc = selectedMaterial.description;
                String name = selectedMaterial.materialName;
                table.add(new Image(Sprites.sprite(gemSprite))).size(11, 11).colspan(1).left();
                String gemContent = name+": "+desc;
                table.add(descriptionLabel(gemContent)).colspan(1).padLeft(3).left().growX().row();
            }
        }

        for (Table section : customSections) {
            table.add(section).colspan(2).growX().row();
        }

        if (descriptors.contains(TowerDescriptors.DESCRIPTION)) {
            table.add(new Image(Sprites.sprite(SpriteType.DESCRIPTION_ICON))).colspan(1).left();
            table.add(descriptionLabel(tower.getDescription())).colspan(1).padLeft(3).left().growX().row();
        }

        return table;
    }

    public Label descriptionLabel(String text) {
        return descriptionLabel(text, true);
    }

    public Label descriptionLabel(String text, boolean wrap) {
        Label label = new Label(text, Sprites.skin(), "small");
        label.setWrap(wrap);
        label.setFontScale(.25f);
        return label;
    }

    public void addCustomSection(Table section) {
        customSections.add(section);
    }

    public void createCustomSection(Drawable icon, String text) {
        Table table = new Table();
        table.add(new Image(icon)).colspan(1).size(10).left();
        table.add(descriptionLabel(text)).colspan(1).padLeft(3).left().growX().row();
        customSections.add(table);
    }

    public void createCustomSection(TextureRegion icon, String text) {
        Table table = new Table();
        table.add(new Image(icon)).colspan(1).size(10).left();
        table.add(descriptionLabel(text)).colspan(1).padLeft(3).left().growX().row();
        customSections.add(table);
    }

    public void createCustomDoubleSection(Drawable icon1, String text1, Drawable icon2, String text2) {
        Table table = new Table();
        Table left = new Table();
        Table right = new Table();
        left.add(new Image(icon1)).colspan(1).size(10).left();
        left.add(descriptionLabel(text1)).colspan(1).padLeft(3).left().growX();
        right.add(new Image(icon2)).colspan(1).size(10).left();
        right.add(descriptionLabel(text2)).colspan(1).padLeft(3).left().growX();
        table.add(left).growX();
        table.add(right).growX();
        customSections.add(table);
    }

    public void createCustomDoubleSection(TextureRegion icon1, String text1, TextureRegion icon2, String text2) {
        Table table = new Table();
        Table left = new Table();
        Table right = new Table();
        left.add(new Image(icon1)).colspan(1).size(10).left();
        left.add(descriptionLabel(text1)).colspan(1).padLeft(3).left().growX();
        right.add(new Image(icon2)).colspan(1).size(10).left();
        right.add(descriptionLabel(text2)).colspan(1).padLeft(3).left().growX();
        table.add(left).growX();
        table.add(right).growX();
        customSections.add(table);
    }
    
}
