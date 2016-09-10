package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.managers.GroupManager;
import com.artemis.managers.TagManager;
import com.artemis.systems.EntityProcessingSystem;
import com.westreicher.birdsim.artemis.Artemis;
import com.westreicher.birdsim.artemis.FixedTimestepStrategy;
import com.westreicher.birdsim.artemis.components.GameComponent;
import com.westreicher.birdsim.artemis.components.InputComponent;
import com.westreicher.birdsim.artemis.managers.InputManager;
import com.westreicher.birdsim.input.AbstractInput;

import java.util.ArrayList;

/**
 * Created by david on 9/29/15.
 */
public class HandlePause extends EntityProcessingSystem {

    protected GameComponent gameComponent;
    protected ComponentMapper<GameComponent> mGame;


    public HandlePause() {
        super(Aspect.all(GameComponent.class));
    }

    @Override
    protected void begin() {
        gameComponent = world.getManager(TagManager.class).getEntity(Artemis.GAME_TAG).getComponent(GameComponent.class);
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

        GameComponent gameComponent = mGame.get(en);
        FixedTimestepStrategy strat = (FixedTimestepStrategy) world.getInvocationStrategy();
        gameComponent.isPaused = strat.isPaused;
        boolean shouldToggle = shouldpause != gameComponent.isPaused;
        if (shouldToggle) {
            if (gameComponent.isPaused)
                strat.continueLogic();
            else
                strat.pauseLogic();
        }
    }
}
