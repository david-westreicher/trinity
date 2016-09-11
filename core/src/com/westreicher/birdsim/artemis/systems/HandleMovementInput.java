package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.westreicher.birdsim.Config;
import com.westreicher.birdsim.artemis.components.InputComponent;
import com.westreicher.birdsim.artemis.components.Speed2Component;

/**
 * Created by david on 9/29/15.
 */
@Wire
public class HandleMovementInput extends IteratingSystem {
    private ComponentMapper<Speed2Component> speedMapper;
    private ComponentMapper<InputComponent> inputMapper;

    public HandleMovementInput() {
        super(Aspect.all(InputComponent.class, Speed2Component.class));
    }

    @Override
    protected void process(int e) {
        Speed2Component speed = speedMapper.get(e);
        InputComponent input = inputMapper.get(e);
        if (input.isMoving) {
            float rad = input.moveRadiant;
            speed.x = (float) Math.cos(rad) * Config.MOVE_SPEED;
            speed.y = (float) Math.sin(rad) * Config.MOVE_SPEED;
        } else {
            speed.x = 0;
            speed.y = 0;
        }
    }
}
