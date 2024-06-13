package net.cmr.rtd.game.files;

import java.util.Objects;

import org.json.simple.JSONObject;

import net.cmr.rtd.game.storage.TeamInventory.Material;
import net.cmr.rtd.game.world.UpdateData;

public abstract class QuestTask {
    
    public final TaskType type;

    private QuestTask(TaskType type) {
        this.type = type;
    }

    public enum TaskType {
        REACH_WAVE,
        COLLECT_MATERIAL,
        AMASS_MONEY
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
        long value = (long) object.get("value");
        switch (type) {
            case REACH_WAVE:
                return new ReachWaveTask(value);
            case COLLECT_MATERIAL:
                String material = (String) object.get("material");
                return new CollectMaterialTask(material, value);
            case AMASS_MONEY:
                return new AmassMoneyTask(value);
            default:
                throw new IllegalArgumentException("Unsupported task type");
        }
    }

    public abstract String getReadableTaskDescription(); // For example: "Reach Wave 10", "Collect 5 Steel", etc.
    public abstract boolean isTaskComplete(UpdateData data, int team); // Returns true if the task is complete given the current game state, false otherwise

    public static QuestTask getTask(QuestFile file, long id) {
        for (QuestTask task : file.getTasks()) {
            if (task.hashCode() == id) {
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
        return obj instanceof QuestTask && obj.hashCode() == hashCode();
    }

    private static class ReachWaveTask extends QuestTask {
        private final long wave;

        private ReachWaveTask(long wave) {
            super(TaskType.REACH_WAVE);
            this.wave = wave;
        }

        @Override
        public String getReadableTaskDescription() {
            return "Reach Wave " + wave;
        }

        @Override
        public boolean isTaskComplete(UpdateData data, int team) {
            return data.getWorld() != null && data.getWorld().getWave() >= wave;
        }

        @Override
        public int hashCode() {
            return Objects.hash(wave, type.name());
        }
    }

    private static class CollectMaterialTask extends QuestTask {
        private final Material material;
        private final long amount;

        private CollectMaterialTask(String material, long amount) {
            super(TaskType.COLLECT_MATERIAL);
            this.material = Material.valueOf(material);
            this.amount = amount;
        }

        @Override
        public String getReadableTaskDescription() {
            return "Hold " + amount + " " + material + " at once";
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

        private AmassMoneyTask(long amount) {
            super(TaskType.AMASS_MONEY);
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

}
