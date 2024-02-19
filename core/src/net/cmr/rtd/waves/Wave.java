package net.cmr.rtd.waves;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Wave {

    final float waveTime;
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

    @SuppressWarnings("unchecked")
    public void serialize(JSONObject wave) {
        wave.put("waveTime", waveTime);
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
