package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.westreicher.birdsim.Config;
import com.westreicher.birdsim.SlotSystem;
import com.westreicher.birdsim.artemis.FixedTimestepStrategy;
import com.westreicher.birdsim.artemis.components.InputComponent;
import com.westreicher.birdsim.artemis.components.MapCoordinateComponent;
import com.westreicher.birdsim.artemis.components.SlotComponent;
import com.westreicher.birdsim.artemis.components.Speed2Component;
import com.westreicher.birdsim.artemis.factories.UberFactory;
import com.westreicher.birdsim.artemis.managers.InputManager;
import com.westreicher.birdsim.input.AbstractInput;

import java.util.ArrayList;

/**
 * Created by david on 9/29/15.
 */
@Wire
public class HandleGameInput extends IteratingSystem {
    private ComponentMapper<Speed2Component> speedMapper;
    private ComponentMapper<MapCoordinateComponent> posMapper;
    private ComponentMapper<InputComponent> inputMapper;
    protected ComponentMapper<SlotComponent> mSlotComponent;
    private long tick;
    private ArrayList<AbstractInput> players;
    private UberFactory factory;

    public HandleGameInput() {
        super(Aspect.all(InputComponent.class, MapCoordinateComponent.class, Speed2Component.class));
    }

    @Override
    protected void begin() {
        tick = ((FixedTimestepStrategy) world.getInvocationStrategy()).currenttick;
        players = world.getSystem(InputManager.class).players;
    }

    @Override
    protected void process(int e) {
        MapCoordinateComponent pos = posMapper.get(e);
        Speed2Component speed = speedMapper.get(e);
        InputComponent input = inputMapper.get(e);
        SlotComponent slot = mSlotComponent.get(e);
        if (input.isMoving) {
            float rad = input.moveRadiant;
            float movspeed = Config.MOVE_SPEED * (slot.special.type == SlotSystem.Specialty.FASTER ? 1.5f : 1);
            speed.x = (float) Math.cos(rad) * movspeed;
            speed.y = (float) Math.sin(rad) * movspeed;
        } else {
            speed.x = 0;
            speed.y = 0;
        }

        if (input.isShooting && tick % Math.max(1, (slot.gunType.type.frequency / slot.gunSpecial.getMultiplier(SlotSystem.GunSpecialty.FREQUENCY))) == 0) {
            float rad = input.shootRadiant;
            float tmpradiant = rad;
            for (int i = 0; i < slot.gunType.multiplier; i++) {
                float bullspeed = slot.gunSpecial.getMultiplier(SlotSystem.GunSpecialty.SPEED) * slot.gunType.type.speed;
                float xspeed = (float) Math.cos(tmpradiant) * bullspeed;
                float yspeed = (float) Math.sin(tmpradiant) * bullspeed;
                factory.shoot(world, pos.x, pos.y, xspeed, yspeed, slot);
                tmpradiant += Math.PI * 2 / slot.gunType.multiplier;
            }
        }
    }
}
