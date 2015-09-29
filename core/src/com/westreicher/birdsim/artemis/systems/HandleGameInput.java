package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.westreicher.birdsim.artemis.FixedTimestepStrategy;
import com.westreicher.birdsim.artemis.components.InputComponent;
import com.westreicher.birdsim.artemis.components.MapCoordinate;
import com.westreicher.birdsim.artemis.components.Speed2;
import com.westreicher.birdsim.artemis.factories.UberFactory;
import com.westreicher.birdsim.input.InputHelper;

/**
 * Created by david on 9/29/15.
 */
@Wire
public class HandleGameInput extends EntityProcessingSystem {
    private ComponentMapper<Speed2> speedMapper;
    private ComponentMapper<MapCoordinate> posMapper;
    private ComponentMapper<InputComponent> inputMapper;
    private long tick;

    public HandleGameInput() {
        super(Aspect.all(InputComponent.class, MapCoordinate.class, Speed2.class));
    }

    @Override
    protected void begin() {
        tick = ((FixedTimestepStrategy) world.getInvocationStrategy()).currenttick;
    }

    @Override
    protected void process(Entity e) {
        MapCoordinate pos = posMapper.get(e);
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
        if (playerinput.secondPointer.update() && tick % 1 == 0) {
            float rad = playerinput.secondPointer.getRadiant();
            UberFactory.shoot(world, pos.x, pos.y, (float) Math.cos(rad), (float) Math.sin(rad));
        }
    }
}
