package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.managers.GroupManager;
import com.artemis.managers.TagManager;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.ImmutableBag;
import com.westreicher.birdsim.artemis.Artemis;
import com.westreicher.birdsim.artemis.components.MapCoordinateComponent;
import com.westreicher.birdsim.artemis.components.ShipComponent;

/**
 * Created by david on 9/11/16.
 */
@Wire
public class PositionCamSwarm extends IteratingSystem {
    ComponentMapper<MapCoordinateComponent> coordMapper;
    private float midx;
    private float midy;
    private int num;

    public PositionCamSwarm() {
        super(Aspect.all(ShipComponent.class));
    }

    @Override
    protected void begin() {
        this.midx = 0;
        this.midy = 0;
        this.num = 0;
    }

    @Override
    protected void process(int e) {
        MapCoordinateComponent pos = coordMapper.get(e);
        midx += pos.x;
        midy += pos.y;
        num++;
    }

    @Override
    protected void end() {
        if (num == 0) return;
        midx = midx / num;
        midy = midy / num;


        ImmutableBag<Entity> players = world.getSystem(GroupManager.class).getEntities(Artemis.PLAYER_GROUP);
        for (int p = 0; p < players.size(); p++) {
            Entity player = players.get(p);
            MapCoordinateComponent playerpos = player.getComponent(MapCoordinateComponent.class);
            playerpos.x = midx;
            playerpos.y = midy;
        }

        Entity camentity = world.getSystem(TagManager.class).getEntity(Artemis.VIRTUAL_CAM_TAG);
        MapCoordinateComponent campos = camentity.getComponent(MapCoordinateComponent.class);
        campos.x += (midx - campos.x) / 10;
        campos.y += (midy - campos.y) / 10;
    }
}
