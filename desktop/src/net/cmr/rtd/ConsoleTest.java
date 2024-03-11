package net.cmr.rtd;

import java.security.KeyPair;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3NativesLoader;

import net.cmr.rtd.game.GameManager;
import net.cmr.rtd.game.GameManager.GameManagerDetails;
import net.cmr.rtd.game.GameSave;
import net.cmr.rtd.game.packets.AESEncryptionPacket;
import net.cmr.rtd.game.packets.ConnectPacket;
import net.cmr.rtd.game.packets.DisconnectPacket;
import net.cmr.rtd.game.packets.GameObjectPacket;
import net.cmr.rtd.game.packets.Packet;
import net.cmr.rtd.game.packets.PacketEncryption;
import net.cmr.rtd.game.packets.PasswordPacket;
import net.cmr.rtd.game.packets.PlayerPacket;
import net.cmr.rtd.game.packets.RSAEncryptionPacket;
import net.cmr.rtd.game.stream.GameStream.PacketListener;
import net.cmr.rtd.game.stream.LocalGameStream;
import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.GameObject;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.World;
import net.cmr.rtd.game.world.entities.Player;
import net.cmr.rtd.screen.GameScreen;
import net.cmr.util.Log;
import net.cmr.util.Log.LogLevel;

public class ConsoleTest {

	static final long RUN_TIME = 10000;
	static LocalGameStream ioStream;
	static LocalGameStream serversideStream;
	static World world;
	static UpdateData data;

	public static void main (String[] arg) throws InterruptedException {
		Log.initializeLog();
		Log.setLogLevel(LogLevel.DEBUG);

        Lwjgl3NativesLoader.load();
        Gdx.files = new Lwjgl3Files();
		GameManagerDetails details = new GameManagerDetails();
		details.actAsServer(false);
		details.setMaxPlayers(4);
		details.setPassword("passward");
		GameManager manager = new GameManager(details);
		manager.initialize(new GameSave("e"));
		manager.start();

		LocalGameStream[] pair = LocalGameStream.createStreamPair();
		ioStream = pair[0];
		serversideStream = pair[1];
		PacketEncryption encryptor = new PacketEncryption();
		ioStream.setEncryptor(encryptor);
		serversideStream.setEncryptor(encryptor);

		String password = manager.getDetails().getPassword();

		serversideStream.addListener(new PacketListener() {
			@Override
			public void packetReceived(Packet packet) {
				Log.debug("Server received packet: " + packet);
			}
		});
		ioStream.addListener(new PacketListener() {
			@Override
			public void packetReceived(Packet packet) {
				Log.debug("Server received packet: " + packet);
				if (packet instanceof RSAEncryptionPacket) {
					// Set the RSA public key and send our RSA public key.
					RSAEncryptionPacket rsaPacket = (RSAEncryptionPacket) packet;
					ioStream.getEncryptor().setRSAPublic(PacketEncryption.publicKeyFromBytes(rsaPacket.RSAData));
		
					KeyPair keyPair = PacketEncryption.createRSAKeyPair();
					ioStream.getEncryptor().setRSAPrivate(keyPair.getPrivate());
		
					RSAEncryptionPacket rsaResponse = new RSAEncryptionPacket(keyPair.getPublic());
					ioStream.sendPacket(rsaResponse);
		
					// Create AES data.
					SecretKey secretKey = PacketEncryption.createAESKey();
					IvParameterSpec iv = PacketEncryption.createIV();
					ioStream.getEncryptor().setAESData(secretKey, iv);
		
					// Send AES data.
					AESEncryptionPacket aesPacket = new AESEncryptionPacket(secretKey, iv);
					ioStream.sendPacket(aesPacket);
		
					// If the server requires a password, send it.
					if (password != null) {
						ioStream.sendPacket(new PasswordPacket(password));
					}
				}
		
				if (packet instanceof GameObjectPacket) {
					GameObjectPacket gameObjectPacket = (GameObjectPacket) packet;
					Log.debug("Received GameObject");
					GameObject object = gameObjectPacket.getObject();
					Log.debug("Object: " + object.getClass().getSimpleName(), object);
					if (object instanceof World) {
						world = (World) object;
					}
					if (object instanceof Entity) {
						Entity entity = (Entity) object;
						if (world == null) {
							Log.error("World is null", new NullPointerException());
							return;
						}
						if (gameObjectPacket.shouldRemove()) {
							world.removeEntity(entity);
						} else {
							world.addEntity(entity);
						}
					}
					return;
				}
		
				if (packet instanceof PlayerPacket) {
					PlayerPacket playerPacket = (PlayerPacket) packet;
					if (playerPacket.isConnecting()) {
						// Add a player object to the world
						Player player = new Player(playerPacket.username);
						world.addEntity(player);
					} else {
						// Remove the player object from the world
						Player player = new Player(playerPacket.username);
						world.removeEntity(player);
					}
					if (!playerPacket.isInitializingWorld()) {
						// If it isn't initializing the world, then notify
						// this screen that a player has joined or left.
						Log.info("Player " + playerPacket.username + " has " + (playerPacket.isConnecting() ? "joined" : "left")); 
					}
					return;
				}

				if (packet instanceof DisconnectPacket) {
					// The player was disconnected, set the screen to the main menu.
					DisconnectPacket disconnectPacket = (DisconnectPacket) packet;
					Log.info("Client disconnected: " + disconnectPacket.reason);
				}
			}
		});
		manager.onNewConnection(serversideStream);
		ioStream.sendPacket(new ConnectPacket("Usernamee", 0));
		data = new UpdateData((GameScreen) null) {
			@Override
			public World getWorld() {
				return world;
			}
		};

		long now = System.currentTimeMillis();
		while (System.currentTimeMillis() - now < RUN_TIME) {
			float delta = (System.currentTimeMillis() - now) / 1000f;
			update(delta);
		}

		manager.stop();
		update(.01f);
	}

	public static void update(float delta) {
		ioStream.update();
		if (world != null) {
			world.update(delta, data);
		}
	} 
}
