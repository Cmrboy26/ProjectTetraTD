package net.cmr.rtd.game.packets;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;

import net.cmr.rtd.game.GamePlayer;

public class PlayerPositionsPacket extends Packet {

    public String[] uuids;
    public Vector2[] positions, velocities;

    // TODO: Implement this class. Sync the positions and velocities of all players in the game.
    public PlayerPositionsPacket() { super(); }
    public PlayerPositionsPacket(ArrayList<GamePlayer> players) {
        super();
        players.removeIf(player -> player.getPlayer() == null); // Remove null players
        positions = new Vector2[players.size()];
        velocities = new Vector2[players.size()];
        uuids = new String[players.size()];
        for (int i = 0; i < players.size(); i++) {
            positions[i] = players.get(i).getPlayer().getPosition();
            velocities[i] = players.get(i).getPlayer().getVelocity();
            uuids[i] = players.get(i).getPlayer().getID().toString();
        }
    }

    @Override
    public Object[] packetVariables() {
        return toPacketVariables(uuids, positions, velocities);
    }

}
