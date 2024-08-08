package net.cmr.rtd.screen;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldFilter;
import com.badlogic.gdx.utils.Align;

import net.cmr.util.AbstractScreenEX;
import net.cmr.util.Settings;
import net.cmr.util.Sprites;

public class FirstTimePlayingScreen extends AbstractScreenEX {
    
    public FirstTimePlayingScreen() {
        super(INITIALIZE_ALL);
    }

    Table content;

    @Override
    public void show() {
        super.show();

        content = new Table();

        Label task = new Label("Please enter a username:", Sprites.skin());
        task.setAlignment(Align.center);
        content.add(task).height(40).growX().padBottom(20).padTop(20).row();

        TextField usernameInput = new TextField("",Sprites.skin());
        usernameInput.setMessageText("touch here to type...");
        usernameInput.setAlignment(Align.center);
        usernameInput.setWidth(300);
        usernameInput.setMaxLength(16);
        usernameInput.setTextFieldFilter(new TextFieldFilter() {
            @Override
            public boolean acceptChar(TextField textField, char c) {
                return Character.isLetterOrDigit(c) || c == '_';
            }
        });
        content.add(usernameInput).height(40).growX().padBottom(20).row();

        TextButton nextButton = new TextButton("Next", Sprites.skin());
        nextButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                processUsername(usernameInput.getText());
                return super.touchDown(event, x, y, pointer, button);
            }
        });
        nextButton.pad(0, 50, 0, 50);
        nextButton.addListener(BUTTON_AUDIO_LISTENER);
        content.add(nextButton).height(40).growX().padBottom(20).row();

        content.setHeight(60 + 20 + 60 + 60 + 40);
        content.setOrigin(Align.top);
        content.setPosition(640 / 2, 360, Align.top);
        add(Align.top, content);
    }

    @Override
    public void render(float delta) {
        actDrawAll(delta);
    }

    private void processUsername(String username) {
        boolean validUsername = username.length() >= 3;
        if (validUsername) {
            // Save username
            // Go to next screen
            Settings.getPreferences().putString(Settings.USERNAME, username);
            Settings.getPreferences().flush();

            fadeToScreen(new MainMenuScreen(), .5f, Interpolation.linear, true);
        } else {
            // Show error message
            Label existingError = content.findActor("error");

            String error = "Error: Username must be at least 3 characters long.";
            Label errorLabel = new Label(error, Sprites.skin(), "small");
            if (existingError != null) {
                errorLabel = (Label) existingError;
            } else {
                content.add(errorLabel).height(40).growX().padBottom(20).row();
            }
            errorLabel.setAlignment(Align.center);
            errorLabel.setName("error");
            errorLabel.addAction(Actions.sequence(Actions.delay(5), Actions.fadeOut(1)));
        }
    }

}
