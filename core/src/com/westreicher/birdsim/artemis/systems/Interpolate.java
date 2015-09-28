package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.westreicher.birdsim.artemis.components.RenderTransform;
import com.westreicher.birdsim.artemis.components.MapCoordinate;
import com.westreicher.birdsim.artemis.components.Speed2;

/**
 * Created by david on 9/28/15.
 */
@Wire
public class Interpolate extends EntityProcessingSystem {
    private ComponentMapper<MapCoordinate> coordMapper;
    private ComponentMapper<Speed2> speedMapper;
    private ComponentMapper<RenderTransform> transformMapper;
    private float delta;

    public Interpolate() {
        super(Aspect.all(MapCoordinate.class, Speed2.class, RenderTransform.class));
    }

    @Override
    protected void begin() {
        this.delta = world.getDelta();
    }

    @Override
    protected void process(Entity e) {
        MapCoordinate pos = coordMapper.get(e);
        Speed2 speed = speedMapper.get(e);
        RenderTransform transform = transformMapper.get(e);
        transform.x = pos.x + speed.x * delta;
        transform.y = pos.y + speed.y * delta;
        transform.radiant = (float) -Math.atan2(-speed.y, speed.x);
    }
}
