package com.westreicher.birdsim.artemis.systems;

import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.managers.GroupManager;
import com.artemis.managers.TagManager;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.math.Vector2;
import com.westreicher.birdsim.artemis.Artemis;
import com.westreicher.birdsim.artemis.components.MapCoordinate;
import com.westreicher.birdsim.artemis.components.Speed2;

/**
 * Created by david on 9/30/15.
 */
@Wire
public class PositionCam extends BaseSystem {
    private static final Vector2 TMP_VEC = new Vector2();
    private static final Vector2 TMP_VEC2 = new Vector2();
    ComponentMapper<MapCoordinate> coordMapper;
    ComponentMapper<Speed2> speedMapper;

    @Override
    protected void processSystem() {
        Entity camentity = world.getManager(TagManager.class).getEntity(Artemis.VIRTUAL_CAM_TAG);
        MapCoordinate campos = camentity.getComponent(MapCoordinate.class);
        Speed2 camspeed = camentity.getComponent(Speed2.class);
        ImmutableBag<Entity> players = world.getManager(GroupManager.class).getEntities(Artemis.PLAYER_GROUP);
        TMP_VEC.set(0, 0);
        TMP_VEC2.set(0, 0);
        for (Entity e : players) {
            MapCoordinate pos = coordMapper.get(e);
            Speed2 speed = speedMapper.get(e);
            TMP_VEC.add(pos.x, pos.y);
            TMP_VEC2.add(speed.x, speed.y);
        }
        if (players.size() > 0) {
            TMP_VEC.scl(1.0f / players.size());
            TMP_VEC2.scl(1.0f / players.size());
        }
        campos.x = TMP_VEC.x;
        campos.y = TMP_VEC.y;
        camspeed.x = TMP_VEC2.x;
        camspeed.y = TMP_VEC2.y;
    }
}
