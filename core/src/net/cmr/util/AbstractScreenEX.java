package net.cmr.util;

import java.util.Objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Null;

import net.cmr.util.Audio.GameMusic;

public abstract class AbstractScreenEX extends ScreenAdapter {

    /**
     * An array of all the aligns. Used to initialize the GameScreen with all available aligns.
     */
    public static final int[] INITIALIZE_ALL = {Align.topRight, Align.top, Align.topLeft, 
        Align.left, Align.center, Align.right, 
        Align.bottomLeft, Align.bottom, Align.bottomRight};
        
    public static final ClickListener BUTTON_AUDIO_LISTENER = new ClickListener() {

        @Override
        public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
            if (pointer == -1) {
                Audio.getInstance().playSFX(Audio.GameSFX.BUTTON_CLICK, 1f);
            }
        }

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            super.touchDown(event, x, y, pointer, button);
            Audio.getInstance().playSFX(Audio.GameSFX.BUTTON_PRESS, 1f);
            return true;
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            super.touchUp(event, x, y, pointer, button);
            Audio.getInstance().playSFX(Audio.GameSFX.BUTTON_RELEASE, 1f);
        }

    };

    public static final ClickListener CHECK_BOX_AUDIO_LISTENER = new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            super.clicked(event, x, y);
            if (event.getListenerActor() instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) event.getListenerActor();
                if (checkBox.isChecked()) {
                    Audio.getInstance().playSFX(Audio.GameSFX.BUTTON_HOVER, 1f);
                } else {
                    Audio.getInstance().playSFX(Audio.GameSFX.BUTTON_UNHOVER, 1f);
                }
            }
        }
    };

    public static final InputListener TEXT_FIELD_AUDIO_LISTENER = new InputListener() {
        @Override
        public boolean keyTyped(InputEvent event, char character) {
            Audio.getInstance().playSFX(Audio.GameSFX.BUTTON_CLICK, 1f);
            return super.keyTyped(event, character);
        };

        public boolean keyDown(InputEvent event, int keycode) {
            if (keycode == Keys.ESCAPE) {
                if (CMRGame.getInstance().getScreen() instanceof AbstractScreenEX) {
                    ((AbstractScreenEX)CMRGame.getInstance().getScreen()).deselectKeyboard();
                }
            }
            return super.keyDown(event, keycode);
        };
    };

    public static final ClickListener CHECK_BOX_DESELECT_KEYBOARD_LISTENER = new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            super.clicked(event, x, y);
            if (CMRGame.getInstance().getScreen() instanceof AbstractScreenEX) {
                ((AbstractScreenEX)CMRGame.getInstance().getScreen()).deselectKeyboard();
            }
        }
    };

    public final CMRGame game;
    public final SpriteBatch batch;
    public final Stages stages;
    public final InputMultiplexer inputMultiplexer;

    /**
     * Creates a new GameScreen with the given aligns.
     * @param initializeStages The aligns to initialize the screen with. (reduces boilerplate)
     * @see #INITIALIZE_ALL
     */
    public AbstractScreenEX(int... initializeStages) {
        this.game = CMRGame.getInstance();  
        this.inputMultiplexer = new InputMultiplexer();
        this.batch = game.batch();
        this.stages = new Stages(640, 360);  
        for(int align : initializeStages) {
            stages.registerStage(align);
        }
        Gdx.input.setInputProcessor(stages);
    }

    public AbstractScreenEX fadeIn(float duration, Interpolation interpolation) {
        for(Stage stage : stages.getRegisteredStages()) {
            stage.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(duration, interpolation)));
        }
        return this;
    }

    /**
     * Optional utility method for calling {@link Stages#actAll(float) actAll} and {@link Stages#drawAll(Batch) drawAll} 
     * on every stage. This method should be called in the {@link AbstractScreenEX#render(float)} method of the screen if implemented. 
     * @param delta The delta time passed in by the render method.
     */
    public final void actDrawAll(float delta) {
        stages.actAll(delta);
        stages.drawAll(batch);
    }

    /**
     * Adds an actor to the stage with the given align.
     * @param align The align of the stage to add the actor to.
     * @param actor The actor to add to the stage.
     */
    public void add(int align, Actor actor) {
        if(stages.get(align) == null) {
            stages.registerStage(align);
        }
        stages.get(align).addActor(actor);
    }

    /**
     * If an actor (such as a {@link com.badlogic.gdx.scenes.scene2d.ui.TextField TextField}) has 
     * keyboard focus, this method will deselect it in all registered stages.
     */
    public void deselectKeyboard() {
        for(Stage stage : stages.getRegisteredStages()) {
            if(stage.getKeyboardFocus() != null) {
                stage.setKeyboardFocus(null);
            }
        }
    }

    /**
     * Dispose method for screens.
     * If overriding, remember to call super.dispose()!
     */
    @Override
    public void dispose() {
        stages.dispose();
    }

    /**
     * Called when the screen should render itself.
     * If overriding, remember to call super.resize()!
     * @param width The width of the screen.
     * @param height The height of the screen.
     */
    @Override
    public void resize(int width, int height) {
        stages.resize(width, height);
    }

    boolean fading = false;
    /**
     * Fades out the screen and (optionally) fades in the new screen from black.
     * @param newScreen The new screen to fade in.
     * @param duration The duration of the fade.
     * @param interpolation The interpolation type of the fade.
     * @param fadeIn Whether or not to fade in the new screen from black.
     */
    public void fadeToScreen(AbstractScreenEX newScreen, float duration, Interpolation interpolation, boolean fadeIn) {
        Objects.requireNonNull(newScreen);
        // TODO: Allow a fading in screen to cancel the current fade and fade out.
        if (fading) {
            return;
        }
        fading = true;
        for (Stage stage : stages.getRegisteredStages()) {
            stage.addAction(Actions.sequence(Actions.fadeOut(duration, interpolation), Actions.run(() -> {
                if (newScreen.getClass().isInstance(game.getScreen())) {
                    return;
                }
                game.setScreen(newScreen);
                if (fadeIn) {
                    for (Stage newScreenStage : newScreen.stages.getRegisteredStages()) {
                        newScreenStage.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(duration, interpolation)));
                    }
                }
            })));
        }
    }

    public @Null GameMusic getScreenMusic() {
        return null;
    }

}