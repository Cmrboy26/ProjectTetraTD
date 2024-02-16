package net.cmr.util;

import java.util.Collection;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Stages extends InputMultiplexer implements Disposable {

    private final static String ERROR_NOT_REGISTERED = "Stage %s has not been registered. Did you register the stage with registerStage()?";

    /**
     * Renders the stages from left to right, top to bottom. 
     */
    public final static int[] SEQUENTIAL_PROCEDURE = {Align.topRight, Align.top, Align.topLeft, 
        Align.left, Align.center, Align.right, 
        Align.bottomLeft, Align.bottom, Align.bottomRight};

    private final HashMap<Integer, Stage> stages;
    private float zoom = 1f;
    private float worldWidth, worldHeight;
    private int[] renderOrder;

    /**
     * Creates a new StageManager with the given world width and height.
     * The world width and height are used to set the world size of the viewports.
     * @param worldWidth
     * @param worldHeight
     */
    public Stages(float worldWidth, float worldHeight) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.stages = new HashMap<>();
        this.renderOrder = null;
    }

    /**
     * @see #StageManager(float, float)
     * @param renderOrder The render order of the stages. If null, the order will be determined by {@link HashMap#keySet()}.
     */
    public Stages(float worldWidth, float worldHeight, int[] renderOrder) {
        this(worldWidth, worldHeight);
        this.renderOrder = renderOrder;
    }

    /**
     * Registers a stage with the given align. The stage will be created with a new {@link FitViewport} with the world width and height.
     * @param align The align of the stage to register.
     */
    public void registerStage(int align) {
        registerStage(new FitViewport(worldWidth, worldHeight), align);
    }

    /**
     * Registers a stage with the given align and viewport.
     * Note: The viewport will be updated with the world width and height of the StageManager.
     * @param viewport The viewport to register.
     * @param align The align of the stage to register.
     */
    public void registerStage(Viewport viewport, int align) {
        Stage stage = new Stage(viewport);
        stages.put(align, stage);
        resizeStage(align, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        addProcessor(stage);
    }

    /**
     * Gets the stage with the given align. Used for adding actors to the stage.
     * @param align The align of the stage to get.
     * @return The stage with the given align.
     * @throws NullPointerException If the stage with the given align has not been registered.
     */
    public Stage get(int align) {
        Stage at = stages.get(align);
        if (at == null) {
            throw new NullPointerException(String.format(ERROR_NOT_REGISTERED, Align.toString(align)));
        }
        return at;
    }

    /**
     * Calls {@link Stage#act(float)} on the stage with the given align.
     * @param align The align of the stage to act.
     * @param delta The delta time to act with.
     * @throws NullPointerException If the stage with the given align has not been registered.
     */
    public void act(int align, float delta) {
        Stage stage = stages.get(align);
        if (stage == null) {
            throw new NullPointerException(String.format(ERROR_NOT_REGISTERED, Align.toString(align)));
        }
        stage.act(delta);
    }

    /**
     * Calls {@link Stage#act(float)} on all stages.
     * @param delta The delta time to act with.
     */
    public void actAll(float delta) {
        for (Integer integer : stages.keySet()) {
            Stage stage = stages.get(integer);
            stage.act(delta);
        }
    }

    /**
     * Draws all stages in the order they were registered. This method will call dynamically call {@link Batch#begin()} and always call {@link Batch#end()}.
     * @param batch The batch to draw with.
     */
    public void drawAll(Batch batch) {
        if (!batch.isDrawing()) {
            batch.begin();
        }

        for (Integer integer : stages.keySet()) {
            Stage stage = stages.get(integer);
            stage.getViewport().apply();
            batch.setProjectionMatrix(stage.getCamera().combined);
            stage.draw();
        }

        batch.end();
    }

    /**
     * Draws the stage with the given align. This method will call dynamically call {@link Batch#begin()} and always call {@link Batch#end()}.
     * @param batch The batch to draw with.
     * @param align The align of the stage to draw.
     * @throws NullPointerException If the stage with the given align has not been registered.
     */
    public void draw(Batch batch, int align) {
        if (!batch.isDrawing()) {
            batch.begin();
        }

        Stage stage = stages.get(align);
        if (stage == null) {
            throw new NullPointerException(String.format(ERROR_NOT_REGISTERED, Align.toString(align)));
        }
        stage.getViewport().apply();
        batch.setProjectionMatrix(stage.getCamera().combined);
        stage.draw();

        batch.end();
    }

    /**
     * Runs {@link resize(int, int)} with the current width and height of the screen.
     * Used internally by {@link #setZoom(float)} and {@link #setWorldSize(float, float)} to
     * update the viewports.
     * @see resize(int, int)
     */
    public void updateViewports() {
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    /**
     * Updates all stages with the given width and height.
     * @param width The width to update the stages with.
     * @param height The height to update the stages with.
     */
    public void resize(int width, int height) {
        if (renderOrder != null) {
            for (int align : renderOrder) {
                resizeStage(align, width, height);
            }
        } else {
            for (Integer align : stages.keySet()) {
                resizeStage(align, width, height);
            }
        }
    }

    /**
     * Updates the stage with the given align with the given width and height.
     * @param align The align of the stage to update.
     * @param width The width to update the stage with.
     * @param height The height to update the stage with.
     */
    private void resizeStage(int align, int width, int height) {
        Stage stage = stages.get(align);
        Viewport viewport = stage.getViewport();
        viewport.update(width, height, true);
        ((OrthographicCamera) viewport.getCamera()).zoom = zoom;

        int screenWidth;
        int screenHeight;
        if(viewport instanceof FitViewport) {
            screenWidth = viewport.getScreenWidth();
            screenHeight = viewport.getScreenHeight();
        } else {
            Vector2 screen = viewport.project(new Vector2(worldWidth, worldHeight));
            screenWidth = Math.round(screen.x);
            screenHeight = Math.round(screen.y);
        }

        if (Align.isLeft(align)) {
            viewport.setScreenX(0);
        } else if ((Align.isRight(align))) {
            viewport.setScreenX(width - screenWidth);
        } else if (Align.isCenterHorizontal(align)) {
            viewport.setScreenX((width - screenWidth) / 2);
        }

        if ((Align.isTop(align))) {
            viewport.setScreenY(height - screenHeight);
        } else if (Align.isBottom(align)) {
            viewport.setScreenY(0);
        } else if (Align.isCenterVertical(align)) {
            viewport.setScreenY((height - screenHeight) / 2);
        }
    }

    @Override
    public void dispose() {
        for (Integer integer : stages.keySet()) {
            Stage stage = stages.get(integer);
            stage.dispose();
        }
    }

    /**
     * Sets the zoom of the viewports and updates them.
     * @see #updateViewports()
     * @param zoom
     */
    public void setZoom(float zoom) {
        this.zoom = zoom;
        updateViewports();
    }
    public float getZoom() { return zoom; }

    /**
     * Note: this does NOT return a copy of the map. 
     * @return A map of all stages registered with their align.
     */
    public HashMap<Integer, Stage> getStageMap() { return stages; }
    public Collection<Stage> getRegisteredStages() { return stages.values(); }
    public Collection<Integer> getInitializedAligns() { return stages.keySet(); }

    /**
     * Sets the world width and height and updates the viewports.
     * Use this method if you're setting both the world width and height.
     * @param worldWidth The world width to set.
     * @param worldHeight The world height to set.
     */
    public void setWorldSize(float worldWidth, float worldHeight) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        updateViewports();
    }

    /**
     * Sets the world width and updates the viewports.
     * Note: If you're setting both the world width and height, use {@link #setWorldSize(float, float)} instead.
     * @param worldHeight The world width to set.
     */
    public void setWorldWidth(float worldWidth) {
        this.worldWidth = worldWidth;
        updateViewports();
    }

    /**
     * Sets the world height and updates the viewports.
     * Note: If you're setting both the world width and height, use {@link #setWorldSize(float, float)} instead.
     * @param worldHeight The world height to set.
     */
    public void setWorldHeight(float worldHeight) {
        this.worldHeight = worldHeight;
        updateViewports();
    }
    public float getWorldWidth() { return worldWidth; }
    public float getWorldHeight() { return worldHeight; }

    /**
     * Sets the render order of the stages. If null, the order will be determined by {@link HashMap#keySet()}.
     * @param renderOrder The render order to set.
     */
    public void setRenderOrder(int[] renderOrder) { this.renderOrder = renderOrder; }
    /**
     * @return The render order of the stages. If null, it means the render order is determined by {@link HashMap#keySet()}.
     */
    public int[] getRenderOrder() { return renderOrder; }

    public boolean isMouseOverUI(int inputX, int inputY) {
        return mouseOverUI(inputX, inputY) != null;
    }

    public Actor mouseOverUI(int inputX, int inputY) {

        for (Integer integer : stages.keySet()) {
            Stage stage = stages.get(integer);
            Vector2 mouseLocalPosition = stage.screenToStageCoordinates(new Vector2(inputX, inputY));
            Actor actor = stage.hit(mouseLocalPosition.x, mouseLocalPosition.y, true);
            if (actor != null) {
                return actor;
            }
        }
        return null;
    }

}
