package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.westreicher.birdsim.Config;
import com.westreicher.birdsim.SlotSystem;
import com.westreicher.birdsim.artemis.FixedTimestepStrategy;
import com.westreicher.birdsim.artemis.components.InputComponent;
import com.westreicher.birdsim.artemis.components.MapCoordinate;
import com.westreicher.birdsim.artemis.components.SlotComponent;
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
    protected ComponentMapper<SlotComponent> mSlotComponent;
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
        SlotComponent slot = mSlotComponent.get(e);
        AbstractInput playerinput = players.get(input.id);
        playerinput.update();
        if (playerinput.isMoving()) {
            float rad = playerinput.getMoveRadiant();
            float movspeed = Config.MOVE_SPEED * (slot.special.type == SlotSystem.Specialty.FASTER ? 2 : 1);
            speed.x = (float) Math.cos(rad) * movspeed;
            speed.y = (float) Math.sin(rad) * movspeed;
        } else {
            speed.x = 0;
            speed.y = 0;
        }
        if (playerinput.isShooting() && tick % (slot.gunType.type.frequency / slot.gunSpecial.getMultiplier(SlotSystem.GunSpecialty.FREQUENCY)) == 0) {
            float rad = playerinput.getShootRadiant();
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
