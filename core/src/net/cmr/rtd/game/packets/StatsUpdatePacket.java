package net.cmr.rtd.game.packets;

import net.cmr.rtd.game.storage.TeamInventory;

public class StatsUpdatePacket extends Packet {

    private int health;
    //private long money;
    private TeamInventory inventory;
    private int structureHealth;

    public StatsUpdatePacket() { 
        super(); 
    }

    public StatsUpdatePacket(int health, TeamInventory inventory, int structureHealth) {
        super();
        this.health = health;
        this.inventory = inventory;
        this.structureHealth = structureHealth;
    }
    public int getHealth() {
        return health;
    }
    public TeamInventory getInventory() {
        return inventory;
    }
    public int getStructureHealth() {
        return structureHealth;
    }
    public Object[] packetVariables() {
        return toPacketVariables(health, inventory, structureHealth);
    }

    /*public StatsUpdatePacket(int health, long money, int structureHealth) {
        super();
        this.health = health;
        this.money = money;
        this.structureHealth = structureHealth;
    }

    public int getHealth() {
        return health;
    }
    public long getMoney() {
        return money;
    }
    public int getStructureHealth() {
        return structureHealth;
    }

    @Override
    public Object[] packetVariables() {
        return toPacketVariables(health, money, structureHealth);
    }*/
    
}
