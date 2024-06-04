package net.cmr.rtd.game.stream;

import java.util.ArrayList;
import java.util.UUID;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.DefaultSerializers.UUIDSerializer;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;

import net.cmr.rtd.game.packets.AESEncryptionPacket;
import net.cmr.rtd.game.packets.AttackPacket;
import net.cmr.rtd.game.packets.ConnectPacket;
import net.cmr.rtd.game.packets.DisconnectPacket;
import net.cmr.rtd.game.packets.EffectPacket;
import net.cmr.rtd.game.packets.GameInfoPacket;
import net.cmr.rtd.game.packets.GameObjectPacket;
import net.cmr.rtd.game.packets.GameOverPacket;
import net.cmr.rtd.game.packets.GameResetPacket;
import net.cmr.rtd.game.packets.JumpPacket;
import net.cmr.rtd.game.packets.Packet;
import net.cmr.rtd.game.packets.Packet.PacketSerializer;
import net.cmr.rtd.game.packets.PacketEncryption;
import net.cmr.rtd.game.packets.PasswordPacket;
import net.cmr.rtd.game.packets.PlayerInputPacket;
import net.cmr.rtd.game.packets.PlayerPacket;
import net.cmr.rtd.game.packets.PlayerPositionsPacket;
import net.cmr.rtd.game.packets.PurchaseItemPacket;
import net.cmr.rtd.game.packets.RSAEncryptionPacket;
import net.cmr.rtd.game.packets.SkipRequestPacket;
import net.cmr.rtd.game.packets.SortTypePacket;
import net.cmr.rtd.game.packets.StatsUpdatePacket;
import net.cmr.rtd.game.packets.TeamUpdatePacket;
import net.cmr.rtd.game.packets.WavePacket;
import net.cmr.rtd.game.storage.TeamInventory;
import net.cmr.rtd.game.world.GameObject.GameType;
import net.cmr.rtd.game.world.entities.HealerEnemy;
import net.cmr.rtd.game.world.entities.TowerEntity.SortType;

public class OnlineGameStream extends GameStream {

    Object packetLock = new Object();
    ArrayList<Packet> packets;
    Connection connection;

    @SuppressWarnings("unchecked")
    public OnlineGameStream(PacketEncryption encryptor, Connection connection) {
        super(encryptor);
        this.connection = connection;
        this.connection.getEndPoint().getKryo().getContext().put(PacketEncryption.class, encryptor);
        this.packets = new ArrayList<Packet>();
        this.connection.addListener(new Listener() {
            public void received(Connection connection, Object object) {
                if (!(object instanceof Packet)) {
                    return;
                }
                synchronized (packetLock) {
                    packets.add((Packet) object);
                }
            }
            @Override
            public void disconnected(Connection arg0) {
                onClose();
            }
            @Override
            public void connected(Connection arg0) {
                onOpen();
            }
        });
    }

    @Override
    public void update() {
        receivePackets();
        if (!connection.isConnected()) {
            onClose();
            //Log.info("Connection closed: " + this);
        }
    }

    @Override
    public void sendPacket(Packet packet) {
        packet.beforeSend(getEncryptor());
        try {
            connection.sendTCP(packet);
        } catch(Exception e) {
            e.printStackTrace();
            Log.error("Error sending packet: "+packet+" Attempting to send again...");
            try {
                connection.sendTCP(packet);
            } catch (Exception e2) {
                e2.printStackTrace();
                Log.error("Failed to resend again. Packet will not be sent.");
            }
        }
    }

    @Override
    public void receivePackets() {
        synchronized (packetLock) {
            for (Packet packet : packets) {
                notifyListeners(packet);
            }
            packets.clear();
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        connection.close();
    }

    @Override
    public String toString() {
        if (connection.isConnected()) {
            return connection.getRemoteAddressTCP().getAddress().getHostAddress()+":"+connection.getRemoteAddressTCP().getPort();
        } else {
            return "OnlineGameStream:Disconnected";
        }
    }

    public static void registerPackets(Kryo kryo) {
        kryo.setRegistrationRequired(true);
        kryo.register(String.class);
        kryo.register(byte[].class);
        kryo.register(Vector2.class);
        kryo.register(Vector2[].class);
        kryo.register(String[].class);

        kryo.register(GameType.class);

        kryo.register(Packet.class, new PacketSerializer<>(kryo, Packet.class));
        kryo.register(ConnectPacket.class);
        kryo.register(AESEncryptionPacket.class);
        kryo.register(RSAEncryptionPacket.class);
        kryo.register(DisconnectPacket.class);
        kryo.register(GameObjectPacket.class);
        kryo.register(StatsUpdatePacket.class);
        kryo.register(PasswordPacket.class);
        kryo.register(PlayerPacket.class);
        kryo.register(PlayerPositionsPacket.class);
        kryo.register(PlayerInputPacket.class);
        kryo.register(GameInfoPacket.class);
        kryo.register(WavePacket.class);
        kryo.register(PurchaseItemPacket.class);
        kryo.register(PurchaseItemPacket.PurchaseAction.class);
        kryo.register(PlayerPacket.PlayerPacketType.class);
        kryo.register(Packet.EncryptionType.class);
        kryo.register(TeamUpdatePacket.class);
        kryo.register(SkipRequestPacket.class);
        kryo.register(AttackPacket.class);
        kryo.register(EffectPacket.class);
        kryo.register(TeamInventory.class);
        kryo.register(JumpPacket.class);
        kryo.register(UUID.class, new UUIDSerializer());
        kryo.register(HealerEnemy.HealerPacket.class);
        kryo.register(SortTypePacket.class);
        kryo.register(SortType.class, new Serializer<SortType>(true) {
            @Override
            public void write(Kryo kryo, Output output, SortType object) {
                if (object == null) {
                    output.writeInt(-1);
                    return;
                }
                output.writeInt(object.getID());
            }
            @Override
            public SortType read(Kryo kryo, Input input, Class<? extends SortType> type) {
                return SortType.fromID(input.readInt());
            }
        });

        kryo.register(GameOverPacket.class);
        kryo.register(GameResetPacket.class);
    }
    
}
