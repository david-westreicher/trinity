package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.westreicher.birdsim.artemis.components.InputComponent;
import com.westreicher.birdsim.artemis.components.Speed2;
import com.westreicher.birdsim.util.InputHelper;

/**
 * Created by david on 9/29/15.
 */
@Wire
public class HandleInput extends EntityProcessingSystem {
    private ComponentMapper<Speed2> speedMapper;
    private ComponentMapper<InputComponent> inputMapper;

    public HandleInput() {
        super(Aspect.all(InputComponent.class, Speed2.class));
    }

    @Override
    protected void process(Entity e) {
        Speed2 speed = speedMapper.get(e);
        InputComponent input = inputMapper.get(e);
        InputHelper.PlayerInput playerinput = InputHelper.players.get(input.id);
        if (playerinput.firstPointer.update()) {
            float rad = playerinput.firstPointer.getRadiant();
            speed.x = (float) Math.cos(rad);
            speed.y = (float) Math.sin(rad);
        } else {
            speed.x = 0;
            speed.y = 0;
        }
    }
}
