package net.cmr.rtd.screen;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import net.cmr.rtd.game.Feedback;
import net.cmr.rtd.game.Feedback.FeedbackForm;
import net.cmr.util.AbstractScreenEX;
import net.cmr.util.Audio;
import net.cmr.util.Log;
import net.cmr.util.Sprites;

public class FeedbackScreen extends AbstractScreenEX {

    FeedbackForm form;
    boolean formRetrievalProcessed = false;
    Thread formThread;

    Label statusLabel;

    public FeedbackScreen() {
        super(INITIALIZE_ALL);
    }

    @Override
    public void show() {
        super.show();

        TextButton backButton = new TextButton("Back", Sprites.skin(), "small");
        Audio.addClickSFX(backButton);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen());
            }
        });
        backButton.setPosition(10, 10);
        backButton.pad(0, 20, 0, 20);
        backButton.setWidth(100);
        add(Align.bottomLeft, backButton);

        statusLabel = new Label("Retrieving feedback form...", Sprites.skin(), "small");
        statusLabel.setAlignment(Align.center);
        statusLabel.setPosition(640 / 2f, 360 / 2f, Align.center);
        add(Align.center, statusLabel);

        formThread = getFormObtainThread();
        formThread.start();
    }

    @Override
    public void render(float delta) {
        if (form != null && !formRetrievalProcessed) {
            formRetrievalProcessed = true;

            Table formTable = form.getForm();
            ScrollPane scrollPane = new ScrollPane(formTable, Sprites.skin());
            scrollPane.setFadeScrollBars(false);
            scrollPane.setSize(640 * (2/3f), 360 * (2/3f));
            scrollPane.setPosition(640 / 2f, 360 / 2f, Align.center);
            add(Align.center, scrollPane);
        }
        if (form == null && !formThread.isAlive() && !formRetrievalProcessed) {
            formRetrievalProcessed = true;
            // Error occured
            Log.info("Failed to retrieve feedback form :(");
            statusLabel.setText("Failed to retrieve feedback form :(");
        }

        super.render(delta);
    }

    private Thread getFormObtainThread() {
        return new Thread(() -> {
            try {
                form = Feedback.retrieveFeedbackForm();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}
