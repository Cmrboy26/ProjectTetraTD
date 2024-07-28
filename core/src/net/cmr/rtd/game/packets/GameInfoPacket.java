package net.cmr.rtd.game.packets;

public class GameInfoPacket extends Packet {

    public int[] teams;
    public int[] playersOnTeam;
    public int players;
    public int maxPlayers;
    public boolean hasPassword;

    public GameInfoPacket() { /* Sent by the client to receive the info. */ }
    public GameInfoPacket(int[] teams, int[] playersOnTeam, int players, int maxPlayers, boolean hasPassword) {
        this.teams = teams;
        this.players = players;
        this.playersOnTeam = playersOnTeam;
        this.maxPlayers = maxPlayers;
        this.hasPassword = hasPassword;
    }

    @Override
    public Object[] packetVariables() {
        return toPacketVariables(teams, playersOnTeam, players, maxPlayers, hasPassword);
    }
    
    
}
