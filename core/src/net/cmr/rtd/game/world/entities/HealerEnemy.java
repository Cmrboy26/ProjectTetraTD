package net.cmr.rtd.game.world.entities;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import com.badlogic.gdx.utils.DataBuffer;

import net.cmr.rtd.game.packets.Packet;
import net.cmr.rtd.game.world.GameObject;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.EnemyFactory.EnemyType;

public class HealerEnemy extends BasicEnemy {

    public static final float PERIOD = 5f;
    public static final float EFFECT_RADIUS = 1f;
    public static final float HEALTH_PERCENTAGE = 0.1f;
    public static final float MAX_ABSORPTION_FACTOR = 1.2f; // can go 20% over max health 

    float specialCooldown = PERIOD;

    public HealerEnemy() {
        super(GameType.HEALER_ENEMY, null);
    }
    public HealerEnemy(int team, String displayType, int maxHealth, float speed, EnemyType type) {
        super(GameType.HEALER_ENEMY, team, displayType, maxHealth, speed, type);
    }

    @Override
    public void update(float delta, UpdateData data) {
        super.update(delta, data);
        if (data.isServer()) {
            specialCooldown -= delta;
            if (specialCooldown <= 0) {
                specialCooldown = PERIOD;
                healNearby(data);
            }
        }
    }

    @Override
    protected void serializeEnemy(DataBuffer buffer) throws IOException {
        super.serializeEnemy(buffer);
        buffer.writeFloat(specialCooldown);
    }

    @Override
    protected void deserializeEnemy(GameObject object, DataInputStream input) throws IOException {
        super.deserializeEnemy(object, input);
        HealerEnemy healer = (HealerEnemy) object;
        healer.specialCooldown = input.readFloat();
    }

    public void healNearby(UpdateData data) {
        ArrayList<EnemyEntity> entitiesInRange = TowerEntity.getEnemiesInRange(team, velocity, EFFECT_RADIUS, data);
        for (EnemyEntity temp : entitiesInRange) {
            if (!(temp instanceof BasicEnemy)) {
                continue;
            }
            BasicEnemy entity = (BasicEnemy) temp;
            if (entity.health < entity.maxHealth * MAX_ABSORPTION_FACTOR) {
                boolean aboveMaxHealth = entity.health * (1f + HEALTH_PERCENTAGE) > entity.maxHealth;
                float appliedHealthPercentage = HEALTH_PERCENTAGE;
                if (aboveMaxHealth) {
                    appliedHealthPercentage = 0.5f * HEALTH_PERCENTAGE;
                }

                entity.health += entity.maxHealth * appliedHealthPercentage;
                if (entity.health > entity.maxHealth * MAX_ABSORPTION_FACTOR) {
                    entity.health = (int) (entity.maxHealth * MAX_ABSORPTION_FACTOR);
                }
                entity.updatePresenceOnClients(data.getManager());
            }
        }   
        HealerPacket packet = new HealerPacket(getID());
        data.getManager().sendPacketToAll(packet);
    }

    public static class HealerPacket extends Packet {
        public UUID healerId;
        
        public HealerPacket() { }
        public HealerPacket(UUID healerId) {
            this.healerId = healerId;
        }

        @Override
        public Object[] packetVariables() {
            return toPacketVariables(healerId);
        }
    }

}
