package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.managers.GroupManager;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.ImmutableBag;
import com.westreicher.birdsim.SlotSystem;
import com.westreicher.birdsim.artemis.Artemis;
import com.westreicher.birdsim.artemis.FixedTimestepStrategy;
import com.westreicher.birdsim.artemis.components.MapCoordinate;
import com.westreicher.birdsim.artemis.components.SlotComponent;
import com.westreicher.birdsim.artemis.components.Speed2;

/**
 * Created by david on 9/25/15.
 */
@Wire
public class MovementSystem extends IteratingSystem {
    private boolean hasSlowmo;

    public MovementSystem() {
        super(Aspect.all(MapCoordinate.class, Speed2.class));
    }

    ComponentMapper<MapCoordinate> coordMapper;
    ComponentMapper<Speed2> speedMapper;
    protected ComponentMapper<SlotComponent> mSlotComponent;

    @Override
    protected boolean checkProcessing() {
        long currentTick = ((FixedTimestepStrategy) world.getInvocationStrategy()).currenttick;
        if (currentTick % 2 == 0)
            return true;
        ImmutableBag<Entity> players = world.getSystem(GroupManager.class).getEntities(Artemis.PLAYER_GROUP);
        hasSlowmo = false;
        for (Entity player : players) {
            if (mSlotComponent.get(player).special.type == SlotSystem.Specialty.SLOWMO) {
                hasSlowmo = true;
                break;
            }
        }
        return !hasSlowmo;
    }

    @Override
    protected void process(int e) {
        MapCoordinate pos = coordMapper.get(e);
        Speed2 speed = speedMapper.get(e);
        pos.x += speed.x;
        pos.y += speed.y;
    }

}
