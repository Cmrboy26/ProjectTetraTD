package net.cmr.rtd.game.packets;

import java.util.HashMap;

import net.cmr.rtd.game.world.GameObject.GameType;
import net.cmr.util.SerializableEnum;

/**
 * Sent from client to server when a player purchases an item or tower from the shop.
 */
public class PurchaseItemPacket extends Packet {

    public int x, y;
    public GameType type;
    public PurchaseAction option;

    public enum PurchaseAction implements SerializableEnum {
        TOWER(0), 
        UPGRADE(1), 
        SELL(2),
        APPLY_LUBRICANT(3),
        APPLY_SCOPE(4),
        APPLY_SCRAP_METAL(5)
        ;

        private final int id;
        private static final HashMap<Integer, SerializableEnum> map = new HashMap<Integer, SerializableEnum>();

        static {
            for (PurchaseAction item : PurchaseAction.values()) {
                map.put(item.id, item);
            }
        }

        private PurchaseAction(int id) {
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

    public PurchaseItemPacket(PurchaseAction action, GameType type, int x, int y) {
        super();
        this.option = action;
        this.type = type;
        this.x = x;
        this.y = y;
    }

    @Override
    public Object[] packetVariables() {
        return toPacketVariables(x, y, option, type);
    }
}
