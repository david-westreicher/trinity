package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.managers.TagManager;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.westreicher.birdsim.UI.MenuAction;
import com.westreicher.birdsim.UI.MenuBuilder;
import com.westreicher.birdsim.artemis.Artemis;
import com.westreicher.birdsim.artemis.components.GameComponent;

/**
 * Created by juanolon on 03/10/15.
 */
@Wire
public class MenuUI extends EntityProcessingSystem {
    Stage stage;

    protected GameComponent gameComponent;
    protected ComponentMapper<GameComponent> mGame;

    public MenuUI() {
        super(Aspect.all(GameComponent.class));
    }

    @Override
    protected void begin() {
        gameComponent = world.getManager(TagManager.class).getEntity(Artemis.GAME_TAG).getComponent(GameComponent.class);
    }

    @Override
    protected void initialize() {
        stage = new Stage();

        // TODO check controller support
        InputProcessor input = Gdx.input.getInputProcessor();
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        if (input != null)
            inputMultiplexer.addProcessor(input);
        inputMultiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(inputMultiplexer);

        MenuBuilder menu = new MenuBuilder(stage);
        menu
                .addButton("Debug", new MenuAction() {
                    @Override
                    public void act() {
                        Artemis.settings.setDebug(!Artemis.settings.isDebug()).flush();
                        world.getSystem(RenderProfiler.class).setEnabled(Artemis.settings.isDebug());
                    }
                }, Artemis.settings.isDebug())
                .addButton("Exit", new MenuAction() {
                    @Override
                    public void act() {
                        closeMenu();
                    }
                }, false)
        ;
    }

    @Override
    protected void process(Entity e) {
        GameComponent gameComponent = mGame.get(e);

        if (gameComponent.isPaused) {
            stage.act(Gdx.graphics.getDeltaTime());
            stage.draw();
        }
    }

    @Override
    public void dispose() {
        stage.dispose();
        super.dispose();
    }

    private void closeMenu() {
        // get entity gameComponent, set status to pause
        gameComponent.isPaused = false;
        Gdx.app.exit();
    }
}
