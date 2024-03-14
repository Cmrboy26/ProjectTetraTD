package net.cmr.rtd.game.packets;

public class StatsUpdatePacket extends Packet {

    private int health;
    private long money;
    private int structureHealth;

    public StatsUpdatePacket() { 
        super(); 
    }

    public StatsUpdatePacket(int health, long money, int structureHealth) {
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
    }
    
}
