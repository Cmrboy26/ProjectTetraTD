package net.cmr.rtd.waves;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Wave {

    final float waveTime;
    boolean warnPlayer = false;
    int additionalPrepTime = 0;
    ArrayList<WaveUnit> waveUnits;

    public Wave(float waveLength) {
        this.waveTime = waveLength;
        this.waveUnits = new ArrayList<WaveUnit>();
    }

    public void addWaveUnit(WaveUnit waveUnit) {
        waveUnits.add(waveUnit);
    }

    public float getWaveTime() {
        return waveTime;
    }

    public ArrayList<WaveUnit> getWaveUnits() {
        return waveUnits;
    }

    public boolean shouldWarnPlayer() {
        return warnPlayer;
    }

    public int getAdditionalPrepTime() {
        return additionalPrepTime;
    }

    public void setWarnPlayer(boolean warnPlayer) {
        this.warnPlayer = warnPlayer;
    }

    public void setAdditionalPrepTime(int additionalPrepTime) {
        this.additionalPrepTime = additionalPrepTime;
    }

    @SuppressWarnings("unchecked")
    public void serialize(JSONObject wave) {
        wave.put("waveTime", waveTime);
        if (warnPlayer) {
            wave.put("warnPlayer", true);
        }
        if (additionalPrepTime != 0) {
            wave.put("additionalPrep", additionalPrepTime);
        }
        JSONArray waveUnitArray = new JSONArray();
        for (WaveUnit unit : waveUnits) {
            JSONObject waveUnit = new JSONObject();
            waveUnit.put("startTime", unit.getTimeStart());
            waveUnit.put("endTime", unit.getTimeEnd());
            waveUnit.put("type", unit.getType().toString());
            waveUnit.put("quantity", unit.getQuantity());
            waveUnitArray.add(waveUnit);
        }
        wave.put("waveUnits", waveUnitArray);
    }
    
}
