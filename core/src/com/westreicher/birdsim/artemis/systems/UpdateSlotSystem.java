package com.westreicher.birdsim.artemis.systems;

import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.managers.GroupManager;
import com.artemis.utils.ImmutableBag;
import com.westreicher.birdsim.SlotSystem;
import com.westreicher.birdsim.artemis.Artemis;
import com.westreicher.birdsim.artemis.components.SlotComponent;

/**
 * Created by david on 10/3/15.
 */
public class UpdateSlotSystem extends BaseSystem {
    protected ComponentMapper<SlotComponent> mSlotComponent;

    @Override
    protected void processSystem() {
        ImmutableBag<Entity> players = world.getManager(GroupManager.class).getEntities(Artemis.PLAYER_GROUP);
        for (Entity player : players) {
            SlotSystem.Slot<SlotSystem.Specialty> s = mSlotComponent.get(player).special;
            s.multiplier--;
            if (s.multiplier <= 0)
                s.type = null;
        }
    }
}
