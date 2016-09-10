package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.westreicher.birdsim.artemis.FixedTimestepStrategy;
import com.westreicher.birdsim.artemis.components.GameComponent;
import com.westreicher.birdsim.artemis.components.InputComponent;
import com.westreicher.birdsim.artemis.managers.InputManager;
import com.westreicher.birdsim.input.AbstractInput;

import java.util.ArrayList;

/**
 * Created by juanolon on 04/10/15.
 */
public class HandleMenuInput extends EntityProcessingSystem {


    FixedTimestepStrategy strat;

    private ComponentMapper<InputComponent> inputMapper;
    private ArrayList<AbstractInput> players;

    public HandleMenuInput() {
        super(Aspect.all(InputComponent.class, GameComponent.class));
    }

    @Override
    protected void begin() {
        if (strat == null)
            strat = world.getInvocationStrategy();
        players = world.getManager(InputManager.class).players;
    }

    @Override
    protected void process(Entity e) {
        // navigation on menu eg. up/down..
//        InputComponent input = inputMapper.get(e);
//        AbstractInput playerinput = players.get(input.id);
//        playerinput.update();
    }
}
