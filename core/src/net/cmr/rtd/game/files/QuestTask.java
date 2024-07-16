package net.cmr.rtd.game.files;

import java.util.Objects;

import org.json.simple.JSONObject;

import net.cmr.rtd.game.storage.TeamInventory.Material;
import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.entities.TowerEntity;

public abstract class QuestTask {
    
    public final TaskType type;
    public final long id;

    private QuestTask(TaskType type, long id) {
        this.type = type;
        if (id == -1) {
            id = hashCode();
        }
        this.id = id;
    }

    public enum TaskType {
        REACH_WAVE,
        COLLECT_MATERIAL,
        AMASS_MONEY,
        HAVE_HEALTH,
        NO_DAMAGE,
        TOWER_LIMIT
    }

    /**
     * Reads a task from a JSON object
     * Structure:
     * {
     *    "type": "REACH_WAVE",
     *    "value": 10,
     *    // Additional fields for specific task types
     * }
     */
    public static QuestTask readTask(JSONObject object) {
        TaskType type = TaskType.valueOf((String) object.get("type"));
        @SuppressWarnings("unchecked")
        long value = (long) object.getOrDefault("value", 0L);
        @SuppressWarnings("unchecked")
        long id = (long) object.getOrDefault("id", -1L);
        switch (type) {
            case REACH_WAVE:
                return new ReachWaveTask(id, value);
            case COLLECT_MATERIAL:
                String material = (String) object.get("material");
                return new CollectMaterialTask(id, material, value);
            case AMASS_MONEY:
                return new AmassMoneyTask(id, value);
            case HAVE_HEALTH:
                return new HaveHealthTask(id, value);
            case NO_DAMAGE:
                return new DamagelessTask(id);
            case TOWER_LIMIT:
                return new TowerLimitTask(id, value);
            default:
                throw new IllegalArgumentException("Unsupported task type");
        }
    }

    public abstract String getReadableTaskDescription(); // For example: "Reach Wave 10", "Collect 5 Steel", etc.
    public abstract boolean isTaskComplete(UpdateData data, int team); // Returns true if the task is complete given the current game state, false otherwise

    boolean isGameOver(UpdateData data) {
        return data.getWorld().getWave() > data.getWorld().getWavesData().getTotalWaves();
    }

    public static QuestTask getTask(QuestFile file, long id) {
        for (QuestTask task : file.getTasks()) {
            if (task.id == id) {
                return task;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return getReadableTaskDescription();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof QuestTask && ((QuestTask) obj).id == id;
    }

    private static class ReachWaveTask extends QuestTask {
        private final long wave;

        private ReachWaveTask(long id, long wave) {
            super(TaskType.REACH_WAVE, id);
            this.wave = wave;
        }

        @Override
        public String getReadableTaskDescription() {
            return "Reach wave " + wave;
        }

        @Override
        public boolean isTaskComplete(UpdateData data, int team) {
            return data.getWorld() != null && data.getWorld().getWave() >= wave;
        }

        @Override
        public int hashCode() {
            Thread.dumpStack();
            return Objects.hash(wave, type.name());
        }
    }

    private static class CollectMaterialTask extends QuestTask {
        private final Material material;
        private final long amount;

        private CollectMaterialTask(long id, String material, long amount) {
            super(TaskType.COLLECT_MATERIAL, id);
            this.material = Material.valueOf(material);
            this.amount = amount;
        }

        @Override
        public String getReadableTaskDescription() {
            return "Hold " + amount + " " + material.materialName.toLowerCase() + " at once";
        }

        @Override
        public boolean isTaskComplete(UpdateData data, int team) {
            return data.getInventory(team).getMaterial(material) >= amount;
        }

        @Override
        public int hashCode() {
            return Objects.hash(amount, type.name(), material.name());
        }
    }

    private static class AmassMoneyTask extends QuestTask {
        private final long amount;

        private AmassMoneyTask(long id, long amount) {
            super(TaskType.AMASS_MONEY, id);
            this.amount = amount;
        }

        @Override
        public String getReadableTaskDescription() {
            return "Amass $" + amount;
        }

        @Override
        public boolean isTaskComplete(UpdateData data, int team) {
            return data.getInventory(team).getCash() >= amount;
        }

        @Override
        public int hashCode() {
            return Objects.hash(amount, type.name());
        }
    }

    private static class HaveHealthTask extends QuestTask {
        private final long health;

        private HaveHealthTask(long id, long health) {
            super(TaskType.HAVE_HEALTH, id);
            this.health = health;
        }

        @Override
        public String getReadableTaskDescription() {
            return "Survive with >" + health + " health";
        }

        @Override
        public boolean isTaskComplete(UpdateData data, int team) {
            return isGameOver(data) && data.getManager().getTeam(team).getHealth() >= health;
        }

        @Override
        public int hashCode() {
            return Objects.hash(health, type.name());
        }
    }

    private static class DamagelessTask extends QuestTask {
        private DamagelessTask(long id) {
            super(TaskType.NO_DAMAGE, id);
        }

        @Override
        public String getReadableTaskDescription() {
            return "Survive without taking damage";
        }

        @Override
        public boolean isTaskComplete(UpdateData data, int team) {
            return isGameOver(data) && data.getManager().getTeam(team).getHealth() == data.getWorld().getWavesData().startingHealth;
        }

        @Override
        public int hashCode() {
            return Objects.hash(type.name());
        }
    }

    private static class TowerLimitTask extends QuestTask {
        private final long limit;
        private boolean limitExceeded = false;

        private TowerLimitTask(long id, long limit) {
            super(TaskType.TOWER_LIMIT, id);
            this.limit = limit;
        }

        @Override
        public String getReadableTaskDescription() {
            return "Survive by placing less than " + limit + " towers";
        }

        @Override
        public boolean isTaskComplete(UpdateData data, int team) {
            if (data.getWorld().getWave() == 0) {
                limitExceeded = false;
                return false;
            }
            if (limitExceeded) {
                return false;
            }
            int placedTowers = 0;
            for (Entity entity : data.getWorld().getEntities()) {
                if (entity instanceof TowerEntity) {
                    TowerEntity tower = (TowerEntity) entity;
                    if (tower.getTeam() == team) {
                        placedTowers++;
                    }
                }
            }
            limitExceeded |= placedTowers > limit;
            return !limitExceeded && isGameOver(data);
        }

        @Override
        public int hashCode() {
            return Objects.hash(limit, type.name());
        }
    }

}
