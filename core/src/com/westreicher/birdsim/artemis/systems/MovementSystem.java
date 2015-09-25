package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.westreicher.birdsim.artemis.components.Position2;

/**
 * Created by david on 9/25/15.
 */
@Wire
public class MovementSystem extends EntityProcessingSystem {
    public MovementSystem() {
        super(Aspect.all(Position2.class));
    }

    ComponentMapper<Position2> positionMapper;

    @Override
    protected void process(Entity e) {
        Position2 pos = positionMapper.get(e);
        pos.x += Math.random() - 0.5f;
        pos.y += Math.random() - 0.5f;
    }
}
