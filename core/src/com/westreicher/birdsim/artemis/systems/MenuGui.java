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
import com.westreicher.birdsim.UI.Settings;
import com.westreicher.birdsim.artemis.Artemis;
import com.westreicher.birdsim.artemis.components.Game;
import com.westreicher.birdsim.UI.MenuBuilder;

/**
 * Created by juanolon on 03/10/15.
 */
@Wire
public class MenuGui extends EntityProcessingSystem {
    Stage stage;

    protected Game game;
    protected ComponentMapper<Game> mGame;

    public MenuGui() {
        super(Aspect.all(Game.class));
    }

    @Override
    protected void begin() {
        game = world.getManager(TagManager.class).getEntity(Artemis.GAME_TAG).getComponent(Game.class);
    }

    @Override
    protected void initialize() {
        stage = new Stage();

        // TODO check controller support
        InputProcessor input = Gdx.input.getInputProcessor();
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(input);
        inputMultiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(inputMultiplexer);


        final Settings settings = new Settings();

        MenuBuilder menu = new MenuBuilder(stage);
        menu
        .addButton("Debug", new MenuAction() {
            @Override
            public void act() {
                settings.setDebug(!settings.isDebug()).flush();
                world.getSystem(RenderProfiler.class).setEnabled(settings.isDebug());
            }
        }, settings.isDebug())
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
        Game game = mGame.get(e);

        if (game.isPaused){
            stage.act(Gdx.graphics.getDeltaTime());
            stage.draw();
        }
    }

    @Override
    public void dispose () {
        stage.dispose();
        super.dispose();
    }

    private void closeMenu(){
        // get entity game, set status to pause
        game.isPaused = false;
        Gdx.app.exit();
    }
}
