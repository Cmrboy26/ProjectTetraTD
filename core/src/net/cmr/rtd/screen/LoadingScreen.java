package net.cmr.rtd.screen;

import java.util.Objects;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Queue;

import net.cmr.util.AbstractScreenEX;
import net.cmr.util.Sprites;

public class LoadingScreen extends AbstractScreenEX {

    LogList messageSupplier;

    public LoadingScreen() {
        super(INITIALIZE_ALL);
    }

    public LoadingScreen(LogList messageSupplier) {
        this();
        this.messageSupplier = messageSupplier;
        messageSupplier.screen = this;
    }

    TextButton exitButton = null;
    TextButton continueButton = null;
    Label loadingLabel = null;
    boolean alreadyShown = false;
    Action dotDotDot;

    @Override
    public void show() {
        if (alreadyShown) {
            return;
        }
        alreadyShown = true;
        super.show();

        loadingLabel = new Label("Loading", Sprites.skin(), "small");
        loadingLabel.setAlignment(Align.bottomRight);
        float delay = .5f;
        dotDotDot = Actions.forever(Actions.sequence(
            Actions.delay(delay),
            Actions.run(() -> loadingLabel.setText(loadingLabel.getText()+".")),
            Actions.delay(delay),
            Actions.run(() -> loadingLabel.setText(loadingLabel.getText()+".")),
            Actions.delay(delay),
            Actions.run(() -> loadingLabel.setText(loadingLabel.getText()+".")),
            Actions.delay(delay),
            Actions.run(() -> loadingLabel.setText(loadingLabel.getText().toString().replaceAll("[.]", "")))
        ));
        loadingLabel.addAction(dotDotDot);
        loadingLabel.pack();
        loadingLabel.setPosition(640-10, 10, Align.bottomRight);

        add(Align.bottomRight, loadingLabel);

        Label messageLabel = new Label("", Sprites.skin(), "small") {
            public void act(float delta) {
                if (messageSupplier == null) return;
                String message = null;
                // first ever do while loop in java
                do {
                    message = messageSupplier.get();
                    if (message != null) {
                        setText(getText()+"\n"+message);
                    }
                } while (message != null);
            };
        };
        messageLabel.setFontScale(.25f);
        messageLabel.setWidth(300);
        messageLabel.setOrigin(Align.bottomLeft);
        messageLabel.setAlignment(Align.bottomLeft);
        messageLabel.setPosition(10, 20 + loadingLabel.getHeight(), Align.bottomLeft);

        add(Align.bottomLeft, messageLabel);
    }

    Object waitLock;
    Thread pausedThread = null;

    public void pauseThread() {
        if (pausedThread != null) return;
        waitLock = new Object();
        pausedThread = Thread.currentThread();

        continueButton = new TextButton("Continue", Sprites.skin(), "small");
        continueButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                resumeThread();
            }
        });
        continueButton.pack();
        continueButton.setWidth(140);
        continueButton.setPosition(10 + 140 + 10, 10, Align.bottomLeft);
        showExitButton();
        add(Align.bottomLeft, continueButton);

        synchronized (waitLock) {
            try {
                waitLock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void resumeThread() {
        if (pausedThread == null) return;
        synchronized (waitLock) {
            waitLock.notifyAll();
        }
        pausedThread = null;
        continueButton.setVisible(false);
        exitButton.setVisible(false);
    }

    public void showExitButton() {
        if (exitButton != null) return;
        exitButton = new TextButton("Back", Sprites.skin(), "small");
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new MainMenuScreen());
            }
        });
        exitButton.pack();
        exitButton.setWidth(140);
        exitButton.setPosition(10, 10, Align.bottomLeft);
        add(Align.bottomLeft, exitButton);

        loadingLabel.setText("Error!");
        loadingLabel.removeAction(dotDotDot);
    }

    public static class LogList extends Queue<String> {

        Object lock = new Object();
        LoadingScreen screen;

        public void update(String string) {
            synchronized (lock) {
                addLast(string);
            }
        }

        public String get() {
            synchronized (lock) {
                if (size > 0) {
                    return removeFirst();
                }
                return null;
            }
        }
        
        public LoadingScreen getLoadingScreen() {
            return screen;
        }

    }
    
}
