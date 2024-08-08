package net.cmr.rtd.game.world.entities;

import java.util.ArrayList;
import java.util.HashSet;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import net.cmr.rtd.game.storage.TeamInventory;
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
            addSection(table, Sprites.sprite(SpriteType.LEVEL_ICON), "Level: "+tower.getLevel());
            //table.add(new Image(Sprites.sprite(SpriteType.LEVEL_ICON))).colspan(1).left();
            //table.add(descriptionLabel("Level: "+tower.getLevel())).colspan(1).padLeft(3).left().growX().row();
        }

        //Table generalStatsTable = new Table();

        float damage = tower.getDamage(false) * (1 + (tower.getCritDamagePercent() - 1) * tower.getCritChance());
        float dps = damage / tower.getAttackSpeed();
        String range = StringUtils.truncateFloatingPoint(tower.getRange(), 2);
        String rangeString = "Range: "+range;
        String dpsString = "DPS: "+StringUtils.truncateFloatingPoint(dps, 2);
        boolean showDPS = descriptors.contains(TowerDescriptors.DPS);
        boolean showRange = descriptors.contains(TowerDescriptors.RANGE);

        if (showDPS ^ showRange) {
            String singleAddText = showDPS ? dpsString : rangeString;
            Sprite singleImage = showDPS ? Sprites.sprite(SpriteType.DPS_ICON) : Sprites.sprite(SpriteType.RANGE_ICON); 
            addSection(table, singleImage, singleAddText);
        } else if (showDPS && showRange) {
            addDoubleSection(table, Sprites.sprite(SpriteType.DPS_ICON), dpsString, Sprites.sprite(SpriteType.RANGE_ICON), rangeString);
        }

        if (descriptors.contains(TowerDescriptors.DPS_EXTENDED)) {
            String damageString = "Damage: "+StringUtils.truncateFloatingPoint(tower.getDamage(false), 2);
            String speedString = "Speed: "+StringUtils.truncateFloatingPoint(1f/tower.getAttackSpeed(), 2);
            addDoubleSection(table, Sprites.sprite(SpriteType.DAMAGE_ICON), damageString, Sprites.sprite(SpriteType.ATTACK_SPEED_ICON), speedString);
        }

        if (descriptors.contains(TowerDescriptors.CRITICAL)) {
            if (tower.getCritDamagePercent() != 1) {
                String critDamageString = "Crit Multiplier: "+StringUtils.truncateFloatingPoint(tower.getDamage(false) * tower.getCritDamagePercent(), 2);
                String critChanceString = "Crit Chance: "+StringUtils.truncateFloatingPoint(tower.getCritChance()*100, 2)+"%";
                addDoubleSection(table, Sprites.sprite(SpriteType.CRITICAL_ICON), critDamageString, Sprites.sprite(SpriteType.CRITICAL_CHANCE_ICON), critChanceString);
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
                Table text = new Table();
                text.add(new Image(Sprites.sprite(component))).size(11, 11).colspan(1).left();
                text.add(descriptionLabel(componentInfo, false)).colspan(1).padLeft(3).fillX().left();
                text.add(new Image(Sprites.sprite(benefitIcon))).size(9, 9).colspan(1).padLeft(3).left();
                text.add(descriptionLabel(")", false)).colspan(1).padLeft(1).left().growX().row();
                addSection(table, text);
            }
        }

        if (descriptors.contains(TowerDescriptors.MATERIAL)) {
            Material selectedMaterial = tower.getSelectedMaterial();
            if (selectedMaterial != null) {
                SpriteType gemSprite = selectedMaterial.image;
                String desc = selectedMaterial.description;
                String name = selectedMaterial.materialName;
                String gemContent = name+": "+desc;
                addSection(table, Sprites.sprite(gemSprite), gemContent);
            }
        }

        for (Table section : customSections) {
            table.add(section).colspan(2).growX().row();
        }

        if (descriptors.contains(TowerDescriptors.DESCRIPTION)) {
            //table.add(new Image(Sprites.sprite(SpriteType.DESCRIPTION_ICON))).colspan(1).left();
            //table.add(descriptionLabel(tower.getDescription())).colspan(1).padLeft(3).left().growX().row();
            addSection(table, Sprites.sprite(SpriteType.DESCRIPTION_ICON), tower.getDescription());
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

    private void addSection(Table table, Sprite icon, String text) {
        createCustomSection(icon, text);
        Table section = customSections.getLast();
        customSections.removeLast();
        table.add(section).colspan(1).growX().row();
    }

    private void addSection(Table table, Table section) {
        table.add(section).colspan(1).growX().row();
    }
    
    private void addDoubleSection(Table table, Sprite icon1, String text1, Sprite icon2, String text2) {
        createCustomDoubleSection(icon1, text1, icon2, text2);
        Table section = customSections.getLast();
        customSections.removeLast();
        table.add(section).colspan(2).growX().row();
    }

    public void addCustomSection(Table section) {
        customSections.add(section);
    }

    public void createCustomSection(Drawable icon, String text) {
        Table table = new Table();
        table.add(new Image(icon)).colspan(1).size(10).left();
        table.add(descriptionLabel(text)).colspan(1).padLeft(3).left().growX();
        customSections.add(table);
    }

    public void createCustomSection(TextureRegion icon, String text) {
        Table table = new Table();
        table.add(new Image(icon)).colspan(1).size(10).left();
        table.add(descriptionLabel(text)).colspan(1).padLeft(3).left().growX();
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
        table.add(left).growX().colspan(1);
        table.add(right).growX().colspan(1);
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
        table.add(left).growX().colspan(1);
        table.add(right).growX().colspan(1);
        customSections.add(table);
    }
    
}
