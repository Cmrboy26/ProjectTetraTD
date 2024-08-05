package net.cmr.rtd.game.storage;

import com.esotericsoftware.kryo.serializers.VersionFieldSerializer.Since;

import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.entities.EnemyEntity;
import net.cmr.rtd.game.world.entities.TowerEntity;
import net.cmr.rtd.game.world.entities.effects.SlownessEffect;
import net.cmr.rtd.game.world.entities.effects.StunEffect;
import net.cmr.rtd.game.world.entities.towers.IceTower;
import net.cmr.util.Sprites.SpriteType;

/**
 * Used to store the inventory of a team (i.e. items collected from enemies, money, etc.)
 */
public class TeamInventory {
    
    public static final int DIAMOND_MAX_AOE_TARGETS = 5;

    public long cash = 0;
    public int scopes = 0; // used to increase range of towers
    public int wd40 = 0; // used to increase speed of towers
    public int scrapMetal = 0; // used to increase damage of towers
    
    // Materials for special towers
    @Since(1) public int steel = 0;
    @Since(1) public int titanium = 0;

    @Since(1) private int diamonds = 0; // piercing
    @Since(1) private int cryonite = 0; // freezing
    @Since(1) private int thorium = 0; // halves damage, doubles attack speed and AOE
    @Since(1) private int ruby = 0; // flaming
    @Since(1) private int quartz = 0; // increases range
    @Since(1) private int topaz = 0; // halves attack speed, doubles range and damage

    public static enum Material {
        STEEL(0, "Steel", SpriteType.STEEL, MaterialType.RESOURCE), 
        TITANIUM(1, "Titanium", SpriteType.TITANIUM, MaterialType.RESOURCE), 
        
        DIAMONDS(2, "Diamond", SpriteType.DIAMOND, MaterialType.GEMSTONE, 
            "The unmatched durability of the diamond enables bullets to pierce through a maximum of "+DIAMOND_MAX_AOE_TARGETS+" enemies."    
        ), 
        CRYONITE(3, "Cryonite", SpriteType.CRYONITE, MaterialType.GEMSTONE, 
            "Cryonite inflicts a slowness debuff through its arctic properties. It also has a chance to freeze enemies in place."
        ), 
        THORIUM(4, "Thorium", SpriteType.THORIUM, MaterialType.GEMSTONE, 
            "Thorium supercharges towers by increasing speed by 125% but halving its damage and range."
        ), 
        RUBY(5, "Ruby", SpriteType.RUBY, MaterialType.GEMSTONE, 
            "Rubies provide towers with a chance to deal double damage to enemies."
        ), 
        QUARTZ(6, "Quartz", SpriteType.QUARTZ, MaterialType.GEMSTONE, 
            "The optical properties of quartz allows towers to see further and attack slightly faster."
        ), 
        TOPAZ(7, "Topaz", SpriteType.TOPAZ, MaterialType.GEMSTONE, 
            "Topaz nearly halves a tower's attacking speed but doubles its damage. In addition, it has a chance to deal 50% more damage to enemies."    
        ), 
        ;

        public final int id;
        public final String materialName;
        public final SpriteType image;
        public final MaterialType materialType;
        public final String description;
        Material(int id, String name, SpriteType image, MaterialType type) {
            this.id = id;
            this.materialName = name;
            this.image = image;
            this.materialType = type;
            this.description = "";
        }
        Material(int id, String name, SpriteType image, MaterialType type, String description) {
            this.id = id;
            this.materialName = name;
            this.image = image;
            this.materialType = type;
            this.description = description;
        }
        public static Material[] getMaterialType(MaterialType type) {
            Material[] materials = Material.values();
            int count = 0;
            for (Material material : materials) {
                if (material.materialType == type) {
                    count++;
                }
            }
            Material[] result = new Material[count];
            count = 0;
            for (Material material : materials) {
                if (material.materialType == type) {
                    result[count] = material;
                    count++;
                }
            }
            return result;
        }
        public static Material getMaterial(int id) {
            for (Material material : Material.values()) {
                if (material.id == id) {
                    return material;
                }
            }
            return null;
        }

        // Tower attribute modification

        public static boolean isPiercing(Material material) {
            return material == DIAMONDS;
        }
        public static float getCritChance(Material material) {
            if (material == null) return 0;
            switch (material) {
                case TOPAZ: return 0.1f;
                case RUBY: return 0.2f;
                default: return 0;
            }
        }
        public static float getCritDamagePercent(Material material) {
            if (material == null) return 1;
            switch (material) {
                case TOPAZ: return 1.5f;
                case RUBY: return 2.0f;
                default: return 1;
            }
        }
        public static float getDamageModifier(Material material, boolean rollCritical) {
            if (material == null) return 1;
            switch (material) {
                case THORIUM: {
                    return 0.5f;
                }
                case TOPAZ: {
                    if (rollCritical) {
                        if (Math.random() < getCritChance(material)) {
                            return 2.0f * getCritDamagePercent(material);
                        }
                    }
                    return 2.0f;
                }
                case RUBY: {
                    if (rollCritical) {
                        if (Math.random() < getCritChance(material)) {
                            return getCritDamagePercent(material);
                        }
                    }
                    return 1.0f;
                }
                default: return 1.0f;
            }
        }
        public static float getAttackSpeedModifier(Material material) {
            if (material == null) return 1;
            switch (material) {
                case THORIUM: {
                    return 2.25f;
                }
                case TOPAZ: {
                    return 0.6f;
                }
                case QUARTZ: {
                    return 1.25f;
                }
                default: return 1.0f;
            }
        }
        public static float getRangeModifier(Material material) {
            if (material == null) return 1;
            switch (material) {
                case QUARTZ: {
                    return 2.0f;
                }
                case THORIUM: {
                    return 0.7f;
                }
                default: return 1.0f;
            }
        }
        public static void attackEnemy(EnemyEntity entity, TowerEntity tower, UpdateData data) {
            if (tower.getSelectedMaterial() == null) return;
            switch (tower.getSelectedMaterial()) {
                case CRYONITE: {
                    float chance = 0.15f;
                    if (tower instanceof IceTower) {
                        chance = 0.05f;
                    }
                    if (Math.random() < chance) {
                        new StunEffect(data, entity.getEffects(), 2, 1);
                    }
                    new SlownessEffect(data, entity.getEffects(), 5, 2);
                    break;
                }
                default:
                    break;
            }
        }
    }
    public static enum MaterialType {
        RESOURCE,
        GEMSTONE;
    }

    public TeamInventory() {

    }

    public TeamInventory(TeamInventory clone) {
        this.cash = clone.cash;
        this.scopes = clone.scopes;
        this.wd40 = clone.wd40;
        this.scrapMetal = clone.scrapMetal;
        for (Material material : Material.values()) {
            setMaterial(material, clone.getMaterial(material));
        }
    }

    public void setCash(long amount) { cash = amount; }
    public long addCash(long amount) { cash += amount; return cash; }
    public void removeCash(long amount) { cash -= amount; }
    public long getCash() { return cash; }
    
    public int getScopes() { return scopes; }
    public void addScope() { scopes++; }
    public void setScopes(int amount) { scopes = amount; }
    public void removeScope() { scopes--; }
    public void removeScopes(int amount) { scopes -= amount; }
    
    public int getWd40() { return wd40; }
    public void addWd40() { wd40++; }
    public void setWd40(int amount) { wd40 = amount; }
    public void removeWd40() { wd40--; }
    public void removeWd40(int amount) { wd40 -= amount; }

    public int getScraps() { return scrapMetal; }
    public void addScrapMetal() { scrapMetal++; }
    public void setScrapMetal(int amount) { scrapMetal = amount; }
    public void removeScrapMetal() { scrapMetal--; }
    public void removeScrapMetal(int amount) { scrapMetal -= amount; }

    public void setMaterial(Material material, int amount) {
        switch (material) {
            case STEEL: steel = amount; break;
            case TITANIUM: titanium = amount; break;
            case DIAMONDS: diamonds = amount; break;
            case CRYONITE: cryonite = amount; break;
            case THORIUM: thorium = amount; break;
            case RUBY: ruby = amount; break;
            case QUARTZ: quartz = amount; break;
            case TOPAZ: topaz = amount; break;
        }
    }
    public void addMaterial(Material material, int amount) {
        switch (material) {
            case STEEL: steel+=amount; break;
            case TITANIUM: titanium+=amount; break;
            case DIAMONDS: diamonds+=amount; break;
            case CRYONITE: cryonite+=amount; break;
            case THORIUM: thorium+=amount; break;
            case RUBY: ruby+=amount; break;
            case QUARTZ: quartz+=amount; break;
            case TOPAZ: topaz+=amount; break;
        }
    }
    public void removeMaterial(Material material, int amount) {
        addMaterial(material, -amount);
    }
    public int getMaterial(Material material) {
        switch (material) {
            case STEEL: return steel;
            case TITANIUM: return titanium;
            case DIAMONDS: return diamonds;
            case CRYONITE: return cryonite;
            case THORIUM: return thorium;
            case RUBY: return ruby;
            case QUARTZ: return quartz;
            case TOPAZ: return topaz;
        }
        return 0;
    }

}
