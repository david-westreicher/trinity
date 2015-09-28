package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.managers.TagManager;
import com.artemis.systems.EntityProcessingSystem;
import com.westreicher.birdsim.artemis.Artemis;
import com.westreicher.birdsim.artemis.components.MapCoordinate;
import com.westreicher.birdsim.artemis.components.Position2;
import com.westreicher.birdsim.artemis.components.Speed2;

/**
 * Created by david on 9/25/15.
 */
@Wire
public class MovementSystem extends EntityProcessingSystem {
    public MovementSystem() {
        super(Aspect.all(Position2.class, Speed2.class));
    }

    ComponentMapper<Position2> positionMapper;
    ComponentMapper<Speed2> speedMapper;


    @Override
    protected void process(Entity e) {
        Position2 pos = positionMapper.get(e);
        Speed2 speed = speedMapper.get(e);
        pos.x += speed.x;
        pos.y += speed.y;
    }

    @Override
    protected void end() {
        Entity camentity = world.getManager(TagManager.class).getEntity(Artemis.VIRTUAL_CAM_TAG);
        Speed2 speed = camentity.getComponent(Speed2.class);
        MapCoordinate coord = camentity.getComponent(MapCoordinate.class);
        speed.x = 1;
        coord.x += speed.x;
        coord.y += speed.y;
    }
}
