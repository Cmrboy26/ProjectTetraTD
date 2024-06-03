package net.cmr.rtd.mobile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class Joystick extends Actor {
    
    Drawable knob, background;
    float deadzone;
    Vector2 joystickDirection = new Vector2(); // 0,0 is center. The direction should be clamped to a circle
    float knobRadius = 5;
    boolean clickStartedInJoystick = false;

    public Joystick(Drawable knob, Drawable background, float deadzone) {
        this.knob = knob;
        this.background = background;
        this.deadzone = deadzone;
    }

    public Joystick(Drawable knob, Drawable background) {
        this(knob, background, 0);
    }

    public void setRadius(float radius) {
        setSize(radius * 2, radius * 2);
    }

    public void setKnobRadius(float radius) {
        knobRadius = radius;
    }

    @Override
    public void act(float delta) {
        // See if the joystick is the actor being interacted with
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            Vector2 mouseScreenPosition = new Vector2(Gdx.input.getX(), Gdx.input.getY());
            Vector2 mouseLocalPosition = screenToLocalCoordinates(mouseScreenPosition);
            clickStartedInJoystick = hit(mouseLocalPosition.x, mouseLocalPosition.y, false) != null;
        }

        // If nothing is being pressed, the joystick should not do anything
        if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            joystickDirection.set(0, 0);
            clickStartedInJoystick = false;
            return;
        }

        // If the click didn't start in the joystick, don't do anything
        if (!clickStartedInJoystick) {
            return;
        }

        Vector2 joystickCenter = new Vector2(getX() + (getWidth() / 2), getY() + (getHeight() / 2));
        Vector2 inputVector = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        Vector2 unprojectedVector2 = screenToLocalCoordinates(inputVector);

        joystickDirection.x = unprojectedVector2.x - (joystickCenter.x / 2f);
        joystickDirection.y = unprojectedVector2.y - (joystickCenter.y / 2f);
        joystickDirection.scl(2 / getWidth(), 2 / getWidth());

        // Clamp the joystick direction to a circle
        if (joystickDirection.len() < deadzone) {
            joystickDirection.set(0, 0);
        }
        if (joystickDirection.len() > 1) {
            joystickDirection.nor();
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        
        background.draw(batch, getX(), getY(), getWidth(), getWidth());
        float knobSize = knobRadius * 2;
        knob.draw(batch, getX() + (1 + joystickDirection.x) * (getWidth() / 2) - (knobSize / 2), getY() + (1 + joystickDirection.y) * (getWidth() / 2) - (knobSize / 2), knobSize, knobSize);
    }

    public float getInputX() {
        return joystickDirection.x;
    }
    public float getInputY() {
        return joystickDirection.y;
    }

}
