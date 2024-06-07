package net.cmr.rtd.game.packets;

import net.cmr.rtd.game.storage.TeamInventory.Material;

public class ApplyMaterialPacket extends Packet {

    public int x, y;
    public Material material;
    
    public ApplyMaterialPacket() { super(); }
    public ApplyMaterialPacket(int x, int y, Material material) {
        super();
        this.x = x;
        this.y = y;
        this.material = material;
    }

    @Override
    public Object[] packetVariables() {
        return toPacketVariables(x, y, material);
    }
    
}
