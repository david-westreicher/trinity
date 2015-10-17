package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.managers.GroupManager;
import com.artemis.managers.TagManager;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.Gdx;
import com.westreicher.birdsim.artemis.Artemis;
import com.westreicher.birdsim.artemis.FixedTimestepStrategy;
import com.westreicher.birdsim.artemis.components.Game;
import com.westreicher.birdsim.artemis.components.InputComponent;
import com.westreicher.birdsim.artemis.managers.InputManager;
import com.westreicher.birdsim.input.AbstractInput;

import java.util.ArrayList;

/**
 * Created by david on 9/29/15.
 */
public class HandlePause extends EntityProcessingSystem {

    protected Game game;
    protected ComponentMapper<Game> mGame;


    public HandlePause() {
        super(Aspect.all(Game.class));
    }

    @Override
    protected void begin() {
        game = world.getManager(TagManager.class).getEntity(Artemis.GAME_TAG).getComponent(Game.class);
    }

    @Override
    protected void process(Entity en) {
        boolean shouldpause = false;
        ArrayList<AbstractInput> players = world.getSystem(InputManager.class).players;
        for (Entity e : world.getSystem(GroupManager.class).getEntities(Artemis.PLAYER_GROUP)) {
            InputComponent input = e.getComponent(InputComponent.class);
            AbstractInput control = players.get(input.id);
            control.update();
            if (control.isPaused()) {
                shouldpause = true;
                break;
            }
        }

        Game game = mGame.get(en);
        FixedTimestepStrategy strat = (FixedTimestepStrategy) world.getInvocationStrategy();
        game.isPaused = strat.isPaused;
        boolean shouldToggle = shouldpause != game.isPaused;
        if (shouldToggle) {
            if (game.isPaused)
                strat.continueLogic();
            else
                strat.pauseLogic();
        }
    }
}
