package net.cmr.rtd.screen;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Consumer;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import net.cmr.rtd.game.Hotkeys;
import net.cmr.rtd.game.Hotkeys.Hotkey;
import net.cmr.rtd.game.Hotkeys.Key;
import net.cmr.util.AbstractScreenEX;
import net.cmr.util.Audio;
import net.cmr.util.Sprites;
import net.cmr.util.StringUtils;
import net.cmr.util.Audio.GameMusic;

public class KeybindScreen extends AbstractScreenEX {
    
    public KeybindScreen() {
        super(INITIALIZE_ALL);
    }

    ArrayList<Consumer<Hotkey>> setKeybindButtonDisplayCallbackList;
    ButtonGroup<TextButton> keybindChangeButtons;
    InputListener hotkeyChangeListener;

    @Override
    public void show() {
        super.show();

        Label titleLabel = new Label("Hotkeys", Sprites.skin());
        titleLabel.setHeight((360-275)/2f);
        titleLabel.setAlignment(Align.center);
        titleLabel.setPosition(640/2, 360, Align.top);
        add(Align.center, titleLabel);

        Table hotkeyTable = new Table();
        float width = 450;
        Map<Key, Hotkey> hotkeysMap = Hotkeys.getHotkeysMap();
        setKeybindButtonDisplayCallbackList = new ArrayList<>();

        keybindChangeButtons = new ButtonGroup<>();
        keybindChangeButtons.setMinCheckCount(0);
        keybindChangeButtons.setMaxCheckCount(1);

        for (final Key key : Key.values()) {
            Hotkey value = hotkeysMap.get(key);

            String name = key.name();
            name = name.toLowerCase().replace('_', ' ');
            name = StringUtils.capitalizeAllWords(name);

            Table section = new Table();
            Label nameLabel = new Label(name, Sprites.skin(), "small");
            nameLabel.setAlignment(Align.center);
            section.add(nameLabel).colspan(1).growX();
            final TextButton keybindButton = new TextButton("...", Sprites.skin(), "toggle-small");
            keybindChangeButtons.add(keybindButton);
            Consumer<Hotkey> setKeybindButtonDisplayCallback = (Hotkey hotkey) -> {
                // With the hotkey, set the text of the TextButton
                String readableKeybind = hotkey.getReadableValue();
                if (hotkey.isEmptyHotkey()) {
                    // Display the default hotkey with gray text
                    keybindButton.getLabel().setColor(Color.GRAY);
                    readableKeybind = "Default: "+key.defaultHotkey.getReadableValue();
                } else {
                    keybindButton.getLabel().setColor(Color.WHITE);
                }
                keybindButton.setText(readableKeybind);
            };
            setKeybindButtonDisplayCallback.accept(value);
            setKeybindButtonDisplayCallbackList.add(setKeybindButtonDisplayCallback);

            section.add(keybindButton).colspan(1).width(width / 2).right();

            hotkeyTable.add(section).growX().row();
        }
        ScrollPane scroll = new ScrollPane(hotkeyTable, Sprites.skin());
        scroll.setSize(width, 275);
        scroll.setPosition(640/2, 360/2, Align.center);
        scroll.setScrollbarsVisible(true);
        scroll.setFadeScrollBars(false);

        add(Align.center, scroll);

        Table buttons = new Table();
        int pad = 10;

        TextButton backButton = new TextButton("Back", Sprites.skin(), "small");
        Audio.addClickSFX(backButton);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new SettingsScreen());
            }
        });
        buttons.add(backButton).width(150).pad(0, pad, 0, pad);

        TextButton defaultsButton = new TextButton("Reset to Defaults", Sprites.skin(), "small");
        Audio.addClickSFX(defaultsButton);
        defaultsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Hotkeys.resetToDefaults();
                defaultsButton.setText("Successfully reset!");
                defaultsButton.addAction(Actions.sequence(
                    Actions.delay(3), 
                    Actions.run(() -> {defaultsButton.setText("Reset to Defaults");})
                ));
                updateAllTextButtons();
            }
        });
        buttons.add(defaultsButton).width(200).pad(0, pad, 0, pad);


        buttons.pack();
        buttons.setPosition(640/2, 10, Align.bottom);
        add(Align.center, buttons);
    }

    /*
     * Should be called whenever a keybind changes.
     */
    public void updateAllTextButtons() {
        Map<Key, Hotkey> hotkeysMap = Hotkeys.getHotkeysMap();
        for (int i = 0; i < Key.values().length; i++) {
            assert i < setKeybindButtonDisplayCallbackList.size();
            Key key = Key.values()[i];
            setKeybindButtonDisplayCallbackList.get(i).accept(hotkeysMap.get(key));
        }
    }

    @Override
    public void render(float delta) {
        super.render(delta);

        // Process keybind setting
        int selectedIndex = keybindChangeButtons.getCheckedIndex();
        if (selectedIndex == -1) {
            if (hotkeyChangeListener != null) {
                stages.get(Align.center).removeListener(hotkeyChangeListener);
                hotkeyChangeListener = null;
            }
            return;
        }
        keybindChangeButtons.getButtons().forEach(button -> {
            button.setDisabled(true);  
            if (!button.isChecked()) {
                button.getLabel().setColor(Color.GRAY);
            } else {
                button.getLabel().setColor(Color.WHITE);
            }
        });
        TextButton button = keybindChangeButtons.getChecked();
        final Key changingKey = Key.values()[selectedIndex];
        button.setText("Change keybind now...");

        if (hotkeyChangeListener == null) {
            hotkeyChangeListener = new InputListener() {
                @Override
                public boolean keyDown(InputEvent event, int keycode) {
                    if (keycode == Keys.ESCAPE) {
                        // Sets hotkey equal to nothing
                        Hotkeys.change(changingKey, Hotkey.emptyHotkey());
                        onKeybindSet();
                        return true;
                    }
                    Hotkeys.change(changingKey, Hotkey.keyboard(keycode));
                    onKeybindSet();
                    return true;
                }

                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    Hotkeys.change(changingKey, Hotkey.mouse(button));
                    onKeybindSet();
                    return true;
                }
            };
            stages.get(Align.center).addListener(hotkeyChangeListener);
        }
        
    }

    public void onKeybindSet() {
        keybindChangeButtons.getButtons().forEach(button -> {
            button.setDisabled(false);  
        });
        keybindChangeButtons.getChecked().setChecked(false);
        updateAllTextButtons();
    }

    @Override
    public GameMusic getScreenMusic() {
        return GameMusic.menuMusic();
    }

}
