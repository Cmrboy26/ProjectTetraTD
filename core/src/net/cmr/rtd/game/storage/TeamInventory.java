package net.cmr.rtd.game.storage;

/**
 * Used to store the inventory of a team (i.e. items collected from enemies, money, etc.)
 */
public class TeamInventory {
    
    public long cash = 0;
    public int scopes = 0; // used to increase range of towers
    public int wd40 = 0; // used to increase speed of towers
    public int scrapMetal = 0; // used to increase damage of towers

    public TeamInventory() {
        
    }

    public void setCash(long amount) { cash = amount; }
    public long addCash(long amount) { cash += amount; return cash; }
    public long getCash() { return cash; }
    
    public int getScopes() { return scopes; }
    public void addScope() { scopes++; }
    public void removeScope() { scopes--; }
    
    public int getWd40() { return wd40; }
    public void addWd40() { wd40++; }
    public void removeWd40() { wd40--; }

    public int getScraps() { return scrapMetal; }
    public void addScrapMetal() { scrapMetal++; }
    public void removeScrapMetal() { scrapMetal--; }

}
