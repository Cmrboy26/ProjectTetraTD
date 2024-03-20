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
    public int[] arguments;
    public PurchaseOption action;

    public enum PurchaseOption implements SerializableEnum {
        TOWER(0), 
        UPGRADE(1), 
        SELL(2);

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

    /**
     * 
     * <ul>
     * <li>
     * PURCHASE A TOWER:
     * <ul>
     *   <li>option should be PurchaseOption.TOWER</li>
     *   <li>type should be the type of tower to purchase</li>
     *   <li>arguments should not be used (null)</li>
     * </ul>
     * </li>
     * <li>
     * UPGRADE A TOWER:
     * <ul>
     *   <li>option should be PurchaseOption.UPGRADE</li>
     *   <li>type should not be used (null)</li>
     *   <li>as of now, arguments should be an array of length 1, with the first element being "1" (more specific conditions will be added later)</li>
     * </ul>
     * </li>
     * <li>
     * SELL A TOWER:
     * <ul>
     *   <li>option should be PurchaseOption.SELL</li>
     *   <li>type should not be used (null)</li>
     *   <li>arguments should not be used (null)</li>
     * </ul>
     * </li>
     * </ul>
     * 
     * @param option what action to perform
     * @param type the type of tower (or enemy) to purchase
     * @param x coordinates of the purchase
     * @param y coordinates of the purchase
     */
    public PurchaseItemPacket(PurchaseOption action, GameType type, int[] arguments, int x, int y) {
        super();
        this.x = x;
        this.y = y;
        this.action = action;
        this.type = type;
        this.arguments = arguments;
    }

    @Override
    public Object[] packetVariables() {
        return toPacketVariables(x, y, action, type, arguments);
    }
}
