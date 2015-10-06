package com.westreicher.birdsim.artemis.systems;

import com.artemis.BaseSystem;
import com.artemis.Entity;
import com.artemis.managers.GroupManager;
import com.badlogic.gdx.Gdx;
import com.westreicher.birdsim.artemis.Artemis;
import com.westreicher.birdsim.artemis.FixedTimestepStrategy;
import com.westreicher.birdsim.artemis.components.InputComponent;
import com.westreicher.birdsim.artemis.managers.InputManager;
import com.westreicher.birdsim.input.AbstractInput;

import java.util.ArrayList;

/**
 * Created by david on 9/29/15.
 */
public class HandlePause extends BaseSystem {

    @Override
    protected void processSystem() {
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
        FixedTimestepStrategy strat = (FixedTimestepStrategy) world.getInvocationStrategy();
        boolean shouldToggle = shouldpause != strat.isPaused;
        if (shouldToggle) {
            if (strat.isPaused)
                strat.continueLogic();
            else
                strat.pauseLogic();
        }
    }

}
