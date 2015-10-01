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
import com.westreicher.birdsim.artemis.managers.InputManager;
import com.westreicher.birdsim.input.AbstractInput;

import java.util.ArrayList;

/**
 * Created by david on 9/29/15.
 */
@Wire
public class HandleGameInput extends EntityProcessingSystem {
    private ComponentMapper<Speed2> speedMapper;
    private ComponentMapper<MapCoordinate> posMapper;
    private ComponentMapper<InputComponent> inputMapper;
    private long tick;
    private ArrayList<AbstractInput> players;
    private UberFactory factory;

    public HandleGameInput() {
        super(Aspect.all(InputComponent.class, MapCoordinate.class, Speed2.class));
    }

    @Override
    protected void begin() {
        tick = ((FixedTimestepStrategy) world.getInvocationStrategy()).currenttick;
        players = world.getManager(InputManager.class).players;
    }

    @Override
    protected void process(Entity e) {
        MapCoordinate pos = posMapper.get(e);
        Speed2 speed = speedMapper.get(e);
        InputComponent input = inputMapper.get(e);
        AbstractInput playerinput = players.get(input.id);
        playerinput.update();
        if (playerinput.isMoving()) {
            float rad = playerinput.getMoveRadiant();
            speed.x = (float) Math.cos(rad);
            speed.y = (float) Math.sin(rad);
        } else {
            speed.x = 0;
            speed.y = 0;
        }
        if (playerinput.isShooting() && tick % 10 == 0) {
            float rad = playerinput.getShootRadiant();
            factory.shoot(world, pos.x, pos.y, (float) Math.cos(rad) * 5, (float) Math.sin(rad) * 5);
        }
    }
}
