package net.cmr.rtd;

import java.util.Iterator;

import net.cmr.rtd.game.GameManager;
import net.cmr.rtd.game.GameManager.GameManagerDetails;
import net.cmr.rtd.game.packets.ConnectPacket;
import net.cmr.rtd.game.packets.Packet;
import net.cmr.rtd.game.stream.GameStream.PacketListener;
import net.cmr.rtd.game.stream.LocalGameStream;
import net.cmr.util.Log;

public class ConsoleTest {

	static LocalGameStream clientsideStream;
	static LocalGameStream serversideStream;

	public static void main (String[] arg) throws InterruptedException {
		Log.initializeLog();
		GameManagerDetails details = new GameManagerDetails();
		details.actAsServer(false);
		details.setMaxPlayers(4);
		GameManager manager = new GameManager(details);
		manager.start();

		LocalGameStream[] pair = LocalGameStream.createStreamPair();
		clientsideStream = pair[0];
		serversideStream = pair[1];

		serversideStream.addListener(new PacketListener() {
			@Override
			public void packetReceived(Packet packet, Iterator<PacketListener> it) {
				System.out.println("Server received packet: " + packet);
			}
		});
		clientsideStream.addListener(new PacketListener() {
			@Override
			public void packetReceived(Packet packet, Iterator<PacketListener> it) {
				System.out.println("Client received packet: " + packet);
			}
		});
		manager.onPlayerConnect(serversideStream);
		
		clientsideStream.sendPacket(new ConnectPacket("Usernamee"));
		update();

		Thread.sleep(1000);
		manager.stop();
		
	}

	public static void update() {
		clientsideStream.update();
		serversideStream.update();
	} 
}
