package net.cmr.rtd.game.world.entities;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

import com.badlogic.gdx.utils.DataBuffer;

import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.GameObject;
import net.cmr.rtd.game.world.UpdateData;

@WorldSerializationExempt
public class Player extends Entity {
    
    String username;

    public Player() {
        super(GameType.PLAYER);
    }

    public Player(final String username) {
        this();
        this.username = username;
        this.setID(UUID.nameUUIDFromBytes(username.getBytes()));
        // System.out.println(username + " > " + this.getID().toString());
    }

    @Override
    public void create() {
        super.create();
    }

    @Override
    public void remove() {
        super.remove();
    }

    @Override
    protected void serializeEntity(DataBuffer buffer) throws IOException {
        buffer.writeUTF(username);
    }

    @Override
    public void update(float delta, UpdateData data) {
        
    }

    @Override
    protected void deserializeEntity(GameObject object, DataInputStream input) throws IOException {
        Player player = (Player) object;
        player.username = input.readUTF();
        player.setID(UUID.nameUUIDFromBytes(player.username.getBytes()));
    }

    public String getName() { return username; }

}
