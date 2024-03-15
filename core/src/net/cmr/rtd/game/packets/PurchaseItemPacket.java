package net.cmr.rtd.game.packets;

import java.util.HashMap;

import net.cmr.util.SerializableEnum;

/**
 * Sent from client to server when a player purchases an item or tower from the shop.
 */
public class PurchaseItemPacket extends Packet {

    public int x, y;
    public PurchaseOption option;

    public enum PurchaseOption implements SerializableEnum {
        TOWER(0), UPGRADE(1), SELL(2);

        private final int id;
        private static final HashMap<Integer, SerializableEnum> map = new HashMap<Integer, SerializableEnum>();

        static {
            for (PurchaseOption item : PurchaseOption.values()) {
                map.put(item.id, item);
            }
        }

        private PurchaseOption(int id) {
            this.id = id;
        }

        @Override
        public int getID() {
            return id;
        }

        @Override
        public HashMap<Integer, SerializableEnum> getEnumMap() {
            return map;
        }
    }

    public PurchaseItemPacket() { super(); }
    public PurchaseItemPacket(PurchaseOption option, int x, int y) {
        super();
        this.x = x;
        this.y = y;
        this.option = option;
    }

    @Override
    public Object[] packetVariables() {
        return toPacketVariables(x, y);
    }
}
