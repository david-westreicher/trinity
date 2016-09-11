package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.westreicher.birdsim.artemis.FixedTimestepStrategy;
import com.westreicher.birdsim.artemis.components.InputComponent;
import com.westreicher.birdsim.artemis.components.MapCoordinateComponent;
import com.westreicher.birdsim.artemis.factories.UberFactory;

/**
 * Created by juanolon on 9/11/16.
 */
public class HandleBulletInput extends IteratingSystem {
    private long tick;
    private ComponentMapper<MapCoordinateComponent> posMapper;
    private ComponentMapper<InputComponent> inputMapper;
    private UberFactory factory;

    public HandleBulletInput() {
        super(Aspect.all(InputComponent.class));
    }

    @Override
    protected void begin() {
        tick = ((FixedTimestepStrategy) world.getInvocationStrategy()).currenttick;
    }
    @Override
    protected void process(int e) {
        MapCoordinateComponent pos = posMapper.get(e);
        InputComponent input = inputMapper.get(e);

        if (input.isShooting && tick % 2 == 0) {
            float rad = input.shootRadiant;
            float bullspeed = 2;
            float xspeed = (float) Math.cos(rad) * bullspeed;
            float yspeed = (float) Math.sin(rad) * bullspeed;
            factory.shoot(world, pos.x, pos.y, xspeed, yspeed);
        }
    }
}
