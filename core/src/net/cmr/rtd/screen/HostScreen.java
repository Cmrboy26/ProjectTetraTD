package net.cmr.rtd.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Null;

import net.cmr.rtd.game.GameConnector;
import net.cmr.rtd.game.GameManager.GameManagerDetails;
import net.cmr.rtd.game.files.QuestFile;
import net.cmr.util.AbstractScreenEX;
import net.cmr.util.Audio.GameMusic;
import net.cmr.util.Settings;
import net.cmr.util.Sprites;

public class HostScreen extends AbstractScreenEX {

    GameManagerDetails details;
    QuestFile file;

    TextField maxPlayersField, portField, passwordField;
    SelectBox<String> portForwardOptions;

    public HostScreen(@Null GameManagerDetails details, QuestFile file) {
        super(INITIALIZE_ALL);
        if (details == null) details = new GameManagerDetails();
        this.details = details;
        this.file = file;
    }

    @Override
    public void show() {
        super.show();
        Table leftBottomTable = new Table();
        leftBottomTable.setFillParent(true);
        leftBottomTable.bottom().left();
        
        TextButton backButton = new TextButton("Back", Sprites.skin(), "small");
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new SelectionScreen());
            }
        });
        backButton.pad(0, 50, 0, 50);
        backButton.addListener(BUTTON_AUDIO_LISTENER);
        leftBottomTable.add(backButton).pad(10).bottom().left();

        add(Align.bottomLeft, leftBottomTable);

        Table rightBottomTable = new Table();
        rightBottomTable.setFillParent(true);
        rightBottomTable.bottom().right();

        TextButton startButton = new TextButton("Start", Sprites.skin(), "small");
        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                startGame();
            }
        });
        startButton.pad(0, 50, 0, 50);
        startButton.addListener(BUTTON_AUDIO_LISTENER);
        rightBottomTable.add(startButton).pad(10).bottom().right();

        add(Align.bottomRight, rightBottomTable);

        Table contentTable = new Table();
        contentTable.setFillParent(true);
        contentTable.center();
        add(Align.center, contentTable);

        Label maxPlayers = new Label("Max Players: ", Sprites.skin(), "small");
        maxPlayers.setAlignment(Align.center);
        contentTable.add(maxPlayers).width(200).pad(5).center();
        maxPlayersField = new TextField(Settings.getPreferences().getInteger(Settings.MAX_PLAYERS)+"", Sprites.skin(), "small");
        maxPlayersField.setAlignment(Align.center);
        maxPlayersField.setMessageText("4");
        maxPlayersField.setMaxLength(3);
        maxPlayersField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        contentTable.add(maxPlayersField).width(200).pad(5).center();

        contentTable.row();

        // TODO: Fix online passwords before reimplmenting
        Label passwordLabel = new Label("Password: ", Sprites.skin(), "small");
        passwordLabel.setAlignment(Align.center);
        //contentTable.add(passwordLabel).width(200).pad(5).center();
        passwordField = new TextField("", Sprites.skin(), "small");
        passwordField.setMessageText("password123");
        passwordField.setAlignment(Align.center);
        passwordField.setPasswordCharacter('*');
        passwordField.setPasswordMode(true);
        //contentTable.add(passwordField).width(200).pad(5).center();

        //contentTable.row();

        Label portLabel = new Label("Port: ", Sprites.skin(), "small");
        portLabel.setAlignment(Align.center);
        contentTable.add(portLabel).width(100).pad(5).center();
        portField = new TextField(Settings.getPreferences().getInteger(Settings.PORT)+"", Sprites.skin(), "small");
        portField.setMessageText("11265");
        portField.setAlignment(Align.center);
        portField.setMaxLength(5);
        portField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        contentTable.add(portField).width(100).pad(5).center();

        contentTable.row();

        Label portForwardType = new Label("Port Forwarding: ", Sprites.skin(), "small");
        portForwardType.setAlignment(Align.center);
        contentTable.add(portForwardType).width(200).pad(5).center();

        portForwardOptions = new SelectBox<String>(Sprites.skin(), "small");
        portForwardOptions.setAlignment(Align.center);
        portForwardOptions.setItems("Automatic (UPnP)", "Manual (Port Forwarding)");
        if (Settings.getPreferences().getBoolean(Settings.USE_UPNP)) {
            portForwardOptions.setSelected("Automatic (UPnP)");
        } else {
            portForwardOptions.setSelected("Manual (Port Forwarding)");
        }
        contentTable.add(portForwardOptions).width(200).pad(5).colspan(1).center();

        contentTable.row();
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        if (Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) {
            for (Stage stage : stages.getStageMap().values()) {
                //stage.clear();
            }
            //show();
        }
    }

    public void startGame() {
        // Update the details according to the user input
        details.setMaxPlayers(Integer.parseInt(maxPlayersField.getText()));
        details.setPassword(passwordField.getText());
        details.usePassword();
        details.setTCPPort(Integer.parseInt(portField.getText()));
        details.setUseUPNP(portForwardOptions.getSelectedIndex() == 0);

        Settings.getPreferences().putInteger(Settings.PORT, details.getTCPPort());
        Settings.getPreferences().putInteger(Settings.MAX_PLAYERS, details.getMaxPlayers());
        Settings.getPreferences().putBoolean(Settings.USE_UPNP, details.useUPNP());
        Settings.getPreferences().flush();

        // Join the game
        GameConnector.hostMultiplayerGame(file, details);
    }

    @Override
    public GameMusic getScreenMusic() {
        return GameMusic.menuMusic();
    }
    
}
