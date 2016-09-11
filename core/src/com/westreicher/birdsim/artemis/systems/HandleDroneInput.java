package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.westreicher.birdsim.artemis.FixedTimestepStrategy;
import com.westreicher.birdsim.artemis.components.InputComponent;
import com.westreicher.birdsim.artemis.components.MapCoordinateComponent;
import com.westreicher.birdsim.artemis.components.SlotStateComponent;
import com.westreicher.birdsim.artemis.factories.UberFactory;

/**
 * Created by juanolon on 9/11/16.
 */
public class HandleDroneInput extends IteratingSystem {
    private long tick;
    private ComponentMapper<MapCoordinateComponent> posMapper;
    private ComponentMapper<InputComponent> inputMapper;
    private ComponentMapper<SlotStateComponent> slotMapper;
    private UberFactory factory;

    public HandleDroneInput() {
        super(Aspect.all(InputComponent.class, SlotStateComponent.class));
    }
    
    @Override
    protected void begin() {
        tick = ((FixedTimestepStrategy) world.getInvocationStrategy()).currenttick;
    }
    @Override
    protected void process(int e) {
        SlotStateComponent slot = slotMapper.get(e);
        if(slot.state == SlotStateComponent.STATE.DRONE) {
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
}
