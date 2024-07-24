package net.cmr.rtd.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import net.cmr.rtd.ProjectTetraTD;
import net.cmr.util.AbstractScreenEX;
import net.cmr.util.Audio;
import net.cmr.util.Settings;
import net.cmr.util.Sprites;

public class SettingsScreen extends AbstractScreenEX {

    public SettingsScreen() {
        super(INITIALIZE_ALL);



		add(Align.center, getSettingsTable(() -> {
            ProjectTetraTD game = ProjectTetraTD.getInstance(ProjectTetraTD.class);
            game.setScreen(new MainMenuScreen());
        }, true));
    }

    public static Table getSettingsTable(Runnable onBack, boolean allowUsernameChange) {

		Table table = new Table();
		table.setFillParent(true);

		Label label = new Label("Settings", Sprites.skin(), "default");
		label.setAlignment(Align.center);
		table.add(label).top().padTop(20.0f).padBottom(20.0f);

		String labelType = "small";

		table.row();

        Table settingsTable = new Table();
        table.add(settingsTable).expand().fill();

        // Settings Begin
        
        // Volume Sliders
        settingsTable.add(new Label("Master Volume", Sprites.skin(), labelType)).left().padRight(10.0f).colspan(1);
        settingsTable.add(new Label("Music Volume", Sprites.skin(), labelType)).left().padRight(10.0f).colspan(1);
        settingsTable.add(new Label("SFX Volume", Sprites.skin(), labelType)).left().padRight(10.0f).colspan(1);

        settingsTable.row();

        Slider masterVolume = new Slider(0.0f, 1.0f, 0.01f, false, Sprites.skin());
        masterVolume.setValue(Settings.getPreferences().getFloat(Settings.MASTER_VOLUME));
        settingsTable.add(masterVolume).expandX().fillX().colspan(1).pad(3);

        Slider musicVolume = new Slider(0.0f, 1.0f, 0.01f, false, Sprites.skin());
        musicVolume.setValue(Settings.getPreferences().getFloat(Settings.MUSIC_VOLUME));
        settingsTable.add(musicVolume).expandX().fillX().colspan(1).pad(3);

        Slider sfxVolume = new Slider(0.0f, 1.0f, 0.01f, false, Sprites.skin());
        sfxVolume.setValue(Settings.getPreferences().getFloat(Settings.SFX_VOLUME));
        settingsTable.add(sfxVolume).expandX().fillX().colspan(1).pad(3).row();

        // Username
        settingsTable.add(new Label("Username", Sprites.skin(), labelType)).left().padRight(10.0f).colspan(1);
        settingsTable.add(new Label("Show FPS", Sprites.skin(), labelType)).left().padRight(10.0f).colspan(1);
        settingsTable.add(new Label("Show Placement Grid", Sprites.skin(), labelType)).left().padRight(10.0f).colspan(1);

        settingsTable.row();
        
        TextFieldStyle textFieldStyle = new TextFieldStyle(Sprites.skin().get("small",  TextField.TextFieldStyle.class));
        textFieldStyle.disabledFontColor = Color.GRAY;

        TextField username = new TextField("Username", textFieldStyle);
        username.setDisabled(!allowUsernameChange);
        username.setMessageText("Username");
        username.setText(Settings.getPreferences().getString(Settings.USERNAME));
        username.setAlignment(Align.center);
        username.setMaxLength(16);
        settingsTable.add(username).expandX().fillX().colspan(3).pad(3).colspan(1);

        CheckBox showFPS = new CheckBox("", Sprites.skin());
        Audio.addClickSFX(showFPS);
        showFPS.setChecked(Settings.getPreferences().getBoolean(Settings.SHOW_FPS));
        settingsTable.add(showFPS).expandX().fillX().colspan(1).pad(3);

        CheckBox showPlacementGrid = new CheckBox("", Sprites.skin());
        Audio.addClickSFX(showPlacementGrid);
        showPlacementGrid.setChecked(Settings.getPreferences().getBoolean(Settings.SHOW_PLACEMENT_GRID));
        settingsTable.add(showPlacementGrid).expandX().fillX().colspan(1).pad(3).row();

        Slider fpsSlider = new Slider(-10.0f, 120.0f, 10.0f, false, Sprites.skin());
        fpsSlider.setValue(Settings.getPreferences().getInteger(Settings.FPS));
        if (ProjectTetraTD.isMobile()) {
            TextTooltip tooltip = new TextTooltip("FPS changes may not function on mobile devices.", Sprites.skin(), "small");
            tooltip.getContainer().pad(5);
            tooltip.getContainer().setScale(.25f);
            tooltip.getActor().setFontScale(.25f);
            tooltip.setInstant(true);
            fpsSlider.addListener(tooltip);
        }

        CheckBox fullscreen = new CheckBox("", Sprites.skin());
        Audio.addClickSFX(fullscreen);
        fullscreen.setChecked(Settings.getPreferences().getBoolean(Settings.FULLSCREEN));

        Slider gammaSlider = new Slider(1f, 2f, 0.01f, false, Sprites.skin());
        gammaSlider.setValue(Settings.getPreferences().getFloat(Settings.GAMMA));
        if (!ProjectTetraTD.isMobile()) {
            TextTooltip tooltip = new TextTooltip("Changing this slider is only recommended for mobile.\nFor PC, it's recommended to keep it at 0.", Sprites.skin(), "small");
            tooltip.getContainer().pad(5);
            tooltip.getContainer().setScale(.35f);
            tooltip.getActor().setFontScale(.35f);
            tooltip.setInstant(true);
            gammaSlider.addListener(tooltip);
        }

        Label fpsLabel = new Label("", Sprites.skin(), labelType) {
            @Override
            public void act(float delta) {
                int fps = (int) fpsSlider.getValue();
                if (fps < 0) {
                    setText("FPS: VSync");
                } else if(fps == 0) {
                    setText("FPS: " + "Unlimited");
                } else {
                    setText("FPS: " + fps);
                }
                super.act(delta);
            }
        };
        Label fullscreenLabel = new Label("Fullscreen", Sprites.skin(), labelType);
        Label gammaLabel = new Label("Gamma", Sprites.skin(), labelType);

        settingsTable.add(fpsLabel).left().padRight(10.0f).colspan(1);
        settingsTable.add(fullscreenLabel).left().padRight(10.0f).colspan(1);
        settingsTable.add(gammaLabel).left().padRight(10.0f).colspan(1);

        settingsTable.row();

        settingsTable.add(fpsSlider).expandX().fillX().colspan(1).pad(3);
        settingsTable.add(fullscreen).expandX().fillX().colspan(1).pad(3);
        settingsTable.add(gammaSlider).expandX().fillX().colspan(1).pad(3).row();

        // Settings End

        table.row();
        
        Table horizontalGroup = new Table();

        TextButton backButton = new TextButton("Back", Sprites.skin(), labelType);
        Audio.addClickSFX(backButton);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onBack.run();
                backButton.setChecked(false);
            }
        });
        horizontalGroup.add(backButton).width(200).padLeft(20f).padRight(20).growX();

        TextButton apply = new TextButton("Apply", Sprites.skin(), labelType);
        Audio.addClickSFX(apply);
        apply.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Save settings
                Settings.getPreferences().putFloat(Settings.MASTER_VOLUME, masterVolume.getValue());
                Settings.getPreferences().putFloat(Settings.MUSIC_VOLUME, musicVolume.getValue());
                Settings.getPreferences().putFloat(Settings.SFX_VOLUME, sfxVolume.getValue());
                Settings.getPreferences().putString(Settings.USERNAME, username.getText());
                Settings.getPreferences().putBoolean(Settings.SHOW_FPS, showFPS.isChecked());
                Settings.getPreferences().putBoolean(Settings.SHOW_PLACEMENT_GRID, showPlacementGrid.isChecked());
                Settings.getPreferences().putInteger(Settings.FPS, (int)fpsSlider.getValue());
                Settings.getPreferences().putBoolean(Settings.FULLSCREEN, fullscreen.isChecked());
                Settings.getPreferences().putFloat(Settings.GAMMA, gammaSlider.getValue());

                Settings.getPreferences().flush();
                Settings.applySettings();
                apply.setChecked(false);
            }
        });
        horizontalGroup.add(apply).width(200).padLeft(20f).padRight(20).growX();

        table.add(horizontalGroup).bottom().pad(5f).width(100).expandX().row();
        return table;
    }

    @Override
    public void render(float delta) {
        game.batch().setColor(Color.WHITE);
        super.render(delta);
    }
    
    @Override
    public void hide() {
        super.hide();
    }
}
