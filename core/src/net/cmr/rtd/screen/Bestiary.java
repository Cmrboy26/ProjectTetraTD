package net.cmr.rtd.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Align;

import net.cmr.rtd.game.world.EnemyFactory;
import net.cmr.rtd.game.world.EnemyFactory.EnemyType;
import net.cmr.rtd.game.world.World;
import net.cmr.rtd.game.world.entities.EnemyEntity;
import net.cmr.util.Sprites;

public class Bestiary {
    
    // TODO: Implement bestiary entries
    public static Window getBestiaryEntry(EnemyType type) {
        World world = new World();
        EnemyFactory factory = new EnemyFactory(0, 0, 0, world);
        type.createEnemy(factory);
        final EnemyEntity enemy = (EnemyEntity) world.getEntities().stream().findFirst().get();

        Window window = new Window("", Sprites.skin());
        window.setModal(true);
        window.setMovable(true);
        window.add(new Label(type.name(), Sprites.skin(), "small")).row();
        Actor enemyActor = new Actor() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                enemy.render(null, batch, Gdx.graphics.getDeltaTime());
            }
        };
        window.add(enemyActor).size(100, 100).align(Align.center).row();
        //System.out.println(enemyActor.getX() + " " + enemyActor.getY());
        Vector2 pos = new Vector2(window.getWidth() / 2, window.getHeight() / 2);

        enemy.setPosition(pos.x, pos.y);

        window.debugAll();
        return window;
    }

}
