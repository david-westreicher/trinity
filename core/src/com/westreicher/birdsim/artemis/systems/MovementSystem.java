package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.managers.GroupManager;
import com.artemis.managers.TagManager;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.westreicher.birdsim.artemis.Artemis;
import com.westreicher.birdsim.artemis.components.MapCoordinate;
import com.westreicher.birdsim.artemis.components.Speed2;

/**
 * Created by david on 9/25/15.
 */
@Wire
public class MovementSystem extends EntityProcessingSystem {
    public MovementSystem() {
        super(Aspect.all(MapCoordinate.class, Speed2.class));
    }

    ComponentMapper<MapCoordinate> coordMapper;
    ComponentMapper<Speed2> speedMapper;

    @Override
    protected void process(Entity e) {
        MapCoordinate pos = coordMapper.get(e);
        Speed2 speed = speedMapper.get(e);
        pos.x += speed.x;
        pos.y += speed.y;
    }

}
