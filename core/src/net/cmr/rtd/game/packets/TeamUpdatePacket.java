package net.cmr.rtd.game.packets;

public class TeamUpdatePacket extends Packet {

    public int teamIndex;
    public boolean teamLost = false;

    public TeamUpdatePacket() { }
    public TeamUpdatePacket(int teamIndex, boolean teamLost) {
        this.teamIndex = teamIndex;
        this.teamLost = teamLost;
    }

    public int getTeamIndex() {
        return teamIndex;
    }
    public boolean isTeamLost() {
        return teamLost;
    }
    public boolean isTeamWon() {
        return !teamLost;
    }

    @Override
    public Object[] packetVariables() {
        return toPacketVariables(teamIndex, teamLost);
    }

    
    
}
