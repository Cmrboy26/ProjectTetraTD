package net.cmr.rtd.game.packets;

/**
 * Sent from client to server when a player purchases an item or tower from the shop.
 */
public class PurchaseItemPacket extends Packet {

    public int x, y;

    public PurchaseItemPacket() { super(); }
    public PurchaseItemPacket(int x, int y) {
        super();
        this.x = x;
        this.y = y;
    }

    @Override
    public Object[] packetVariables() {
        return toPacketVariables(x, y);
    }
}
