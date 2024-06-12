package net.cmr.rtd.screen;

import java.util.function.Consumer;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import net.cmr.util.AbstractScreenEX;
import net.cmr.util.Audio;
import net.cmr.util.Sprites;

public class TeamSelectionScreen extends AbstractScreenEX {
    
    Consumer<ConnectionAttempt> joinGameCallback;
    int[] availableTeams;
    int selectedTeam = -1;
    float countdown = 0.0f;
    boolean usePassword = false;
    TextField passwordField;

    public TeamSelectionScreen(Consumer<ConnectionAttempt> joinGameCallback, int[] availableTeams, boolean usePassword) {
        super(INITIALIZE_ALL);
        this.joinGameCallback = joinGameCallback;
        this.availableTeams = availableTeams;
        this.usePassword = usePassword;
    }

    @Override
    public void show() {
        super.show();
        System.out.println(availableTeams.length);
        if (availableTeams.length == 1 && !usePassword) {
            joinGameCallback.accept(new ConnectionAttempt("", availableTeams[0]));
            return;
        }

        Table table = new Table();
		table.setFillParent(true);

        Label label = new Label("Select a team", Sprites.skin(), "default");
        label.setAlignment(Align.center);
        table.add(label).padTop(20.0f).padBottom(20.0f).colspan(availableTeams.length).expandX().fillX().row();

        ButtonGroup<TextButton> buttonGroup = new ButtonGroup<TextButton>();
        int[] indexToTeam = new int[availableTeams.length];
        buttonGroup.setMaxCheckCount(1);
        buttonGroup.setMinCheckCount(0);
        if (availableTeams.length > 1) {
            for (int team : availableTeams) {
                TextButton button = new TextButton("Team " + (team + 1), Sprites.skin(), "toggle-small");
                buttonGroup.add(button);
                indexToTeam[buttonGroup.getButtons().size - 1] = team;
                /*button.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (team != -1 && selectedTeam == -1) {
                            table.row();
                            Label label = new Label("Joining game...", Sprites.skin(), "small");
                            label.setAlignment(Align.center);
                            table.add(label).padTop(20.0f).padBottom(20.0f).colspan(availableTeams.length).expandX().fillX().row();
                            selectedTeam = team;
                        }
                    }
                });*/
                int sidePad = 0;
                table.add(button).pad(sidePad).padTop(20.0f).padBottom(20.0f).space(10.0f).growX();
            }
        }

        passwordField = new TextField("", Sprites.skin(), "small");
        passwordField.setMessageText("Password");
        passwordField.setAlignment(Align.center);
        if (usePassword) {
            table.row();
            Label passwordLabel = new Label("Enter password:", Sprites.skin(), "default");
            passwordLabel.setAlignment(Align.center);
            table.add(passwordLabel).padTop(20.0f).padBottom(20.0f).colspan(availableTeams.length).expandX().fillX().row();
            table.add(passwordField).padTop(20.0f).padBottom(20.0f).colspan(availableTeams.length).expandX().fillX().row();
        }

            // Add a join button
        TextButton joinButton = new TextButton("Join", Sprites.skin(), "small");
        joinButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (buttonGroup.getCheckedIndex() == -1) {
                    selectedTeam = availableTeams[0];
                } else {
                    selectedTeam = indexToTeam[buttonGroup.getCheckedIndex()];
                }
            };
        });
        Audio.addClickSFX(joinButton);
        joinButton.pad(0, 50, 0, 50);

        table.row();
        table.add(joinButton).padTop(20.0f).padBottom(20.0f).space(10.0f).colspan(availableTeams.length);

        add(Align.center, table);
    }

    public static class ConnectionAttempt {
        public String passwordAttempt;
        public int team;

        public ConnectionAttempt(String passwordAttempt, int team) {
            this.passwordAttempt = passwordAttempt;
            this.team = team;
        }
    }

    @Override
    public void render(float delta) {
        if (selectedTeam != -1) {
            countdown += delta;
        }
        if (countdown > .1f) {
            joinGameCallback.accept(new ConnectionAttempt(passwordField.getText(), selectedTeam));
        }
        super.render(delta);
    }

}
