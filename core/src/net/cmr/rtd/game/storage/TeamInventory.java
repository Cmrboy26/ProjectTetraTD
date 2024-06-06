package net.cmr.rtd.game.storage;

import com.esotericsoftware.kryo.serializers.VersionFieldSerializer.Since;

/**
 * Used to store the inventory of a team (i.e. items collected from enemies, money, etc.)
 */
public class TeamInventory {
    
    public long cash = 0;
    public int scopes = 0; // used to increase range of towers
    public int wd40 = 0; // used to increase speed of towers
    public int scrapMetal = 0; // used to increase damage of towers
    
    // Materials for special towers
    @Since(1) public int steel = 0;
    @Since(1) public int titanium = 0;

    @Since(1) public int diamonds = 0; // piercing
    @Since(1) public int cryonite = 0; // freezing
    @Since(1) public int thorium = 0; // halves damage, doubles attack speed and AOE
    @Since(1) public int ruby = 0; // flaming
    @Since(1) public int quartz = 0; // increases range
    @Since(1) public int topaz = 0; // halves attack speed, doubles range and damage
    // NOTE: When adding new materials, make sure to update the canPurchase method in Cost.java

    public TeamInventory() {
        
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

}
