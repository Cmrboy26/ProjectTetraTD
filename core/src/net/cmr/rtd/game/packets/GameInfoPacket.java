package net.cmr.rtd.game.packets;

public class GameInfoPacket extends Packet {

    public int teams;
    public int players;
    public int maxPlayers;

    public GameInfoPacket() { /* Sent by the client to receive the info. */ }
    public GameInfoPacket(int teams, int players, int maxPlayers) {
        this.teams = teams;
        this.players = players;
        this.maxPlayers = maxPlayers;
    }

    @Override
    public Object[] packetVariables() {
        return toPacketVariables(teams, players, maxPlayers);
    }
    
    
}
