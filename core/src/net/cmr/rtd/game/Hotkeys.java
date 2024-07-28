package net.cmr.rtd.game;

import java.util.HashMap;
import java.util.Map.Entry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.esotericsoftware.kryo.serializers.FieldSerializer.NotNull;

import net.cmr.rtd.ProjectTetraTD;

public class Hotkeys {
    public enum Key {
        UP(Hotkey.keyboard(Input.Keys.W)), 
        DOWN(Hotkey.keyboard(Input.Keys.S)), 
        LEFT(Hotkey.keyboard(Input.Keys.A)), 
        RIGHT(Hotkey.keyboard(Input.Keys.D)), 
        SPRINT(Hotkey.keyboard(Input.Keys.SHIFT_LEFT)), 
        SELECT(Hotkey.mouse(Input.Buttons.LEFT)),
        INVENTORY(Hotkey.keyboard(Input.Keys.E)), 
        SHOP(Hotkey.keyboard(Input.Keys.Q)), 
        UPGRADE(Hotkey.keyboard(Input.Keys.U)), 
        MULTIPLACE(Hotkey.keyboard(Input.Keys.SHIFT_LEFT)),

        TARGETING_COPY(Hotkey.keyboard(Input.Keys.C)), 
        TARGETING_PASTE(Hotkey.keyboard(Input.Keys.V)),
        QUICK_SELL(Hotkey.keyboard(Input.Keys.R)), 
        QUICK_UPGRADE(Hotkey.mouse(Input.Buttons.RIGHT)),
        CLONE_TOWER(Hotkey.mouse(Input.Buttons.MIDDLE)), 

        EMOTE(Hotkey.keyboard(Input.Keys.SPACE)), 
        CHAT(Hotkey.keyboard(Input.Keys.T)),
        PAN(Hotkey.keyboard(Input.Keys.CONTROL_LEFT)),
        TOGGLE_PAUSE(Hotkey.keyboard(Input.Keys.P)),
        SKIP_WAVE(Hotkey.keyboard(Input.Keys.ENTER)),
        ;

        public Hotkey defaultHotkey;

        Key(Hotkey defaultHotkey) {
            this.defaultHotkey = defaultHotkey;
        }
    }    

    private static HashMap<Key, Hotkey> hotkeys = new HashMap<Key, Hotkey>();   
    private static final String FILE = "hotkeys.json";

    /**
     * Loads hotkeys from file
     */
    public static void load() {
        hotkeys.clear();
        for (Key key : Key.values()) {
            hotkeys.put(key, key.defaultHotkey);
        }
        if (!getFile().exists()) {
            save();
            return;
        }
        String readString = getFile().readString();
        String[] values = readString.split("\n");
        for (String value : values) {
            String[] split = value.split(",");
            Key key = Key.valueOf(split[0]);
            Hotkey hotkey = null;
            if (split[1].equals("KEYBOARD")) {
                hotkey = Hotkey.keyboard(Integer.parseInt(split[2]));
            } else if (split[1].equals("MOUSE")) {
                hotkey = Hotkey.mouse(Integer.parseInt(split[2]));
            }
            hotkeys.put(key, hotkey);
        }

        for (Entry<Key, Hotkey> entry : hotkeys.entrySet()) {
            Key key = entry.getKey();
            Hotkey hotkey = entry.getValue();
            System.out.println(key.name() + " " + hotkey.type.name() + " " + hotkey.key);
        }
    }
    /**
     * Saves the hotkeys to a file
     */
    public static void save() {
        StringBuilder builder = new StringBuilder();
        for (Entry<Key, Hotkey> entry : hotkeys.entrySet()) {
            Key key = entry.getKey();
            Hotkey hotkey = entry.getValue();
            builder.append(key.name()).append(",").append(hotkey.type.name()).append(",").append(hotkey.key).append("\n");
        }
        // Omits the last newline
        builder.substring(0, builder.length()-1);
        getFile().writeString(builder.toString(), false);
    }
    /**
     * Changes the hotkey for a key and saves it to file
     */
    public static void change(Key key, Hotkey hotkey) {
        hotkeys.put(key, hotkey);
        save();
    }
    /**
     * Format:
     * KEY,INPUTTYPE,KEYCODE\n
     * ...
     * 
     * @return the file handle for the hotkeys file
     */
    private static FileHandle getFile() {
        return Gdx.files.external(ProjectTetraTD.EXTERNAL_FILE_NAME+FILE);
    }

    public static Hotkey get(Key hotkey) {
        return hotkeys.getOrDefault(hotkey, Hotkey.emptyHotkey());
    }
    public static boolean justPressed(Key hotkey) {
        return get(hotkey).justPressed();
    }
    public static boolean pressed(Key hotkey) {
        return get(hotkey).pressed();
    }

    public static class Hotkey {
        public enum InputType {
            KEYBOARD, MOUSE;
        }

        public static Hotkey keyboard(int key) {
            return new Hotkey(InputType.KEYBOARD, key);
        }
        public static Hotkey mouse(int button) {
            return new Hotkey(InputType.MOUSE, button);
        }

        int key;
        InputType type;

        private Hotkey(InputType type, int key) {
            this.type = type;
            this.key = key;
        }

        public boolean justPressed() {
            if (type == InputType.KEYBOARD) {
                return Gdx.input.isKeyJustPressed(key);
            } else if (type == InputType.MOUSE) {
                return Gdx.input.isButtonJustPressed(key);
            }
            return false;
        }
        public boolean pressed() {
            if (type == InputType.KEYBOARD) {
                return Gdx.input.isKeyPressed(key);
            } else if (type == InputType.MOUSE) {
                return Gdx.input.isButtonPressed(key);
            }
            return false;
        }

        public static Hotkey emptyHotkey() {
            return keyboard(-1);
        }
    }
    
}
