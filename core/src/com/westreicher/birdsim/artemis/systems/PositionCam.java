package com.westreicher.birdsim.artemis.systems;

import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.managers.GroupManager;
import com.artemis.managers.TagManager;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.math.Vector2;
import com.westreicher.birdsim.Config;
import com.westreicher.birdsim.artemis.Artemis;
import com.westreicher.birdsim.artemis.components.InputComponent;
import com.westreicher.birdsim.artemis.components.MapCoordinate;
import com.westreicher.birdsim.artemis.components.Speed2;
import com.westreicher.birdsim.artemis.managers.InputManager;
import com.westreicher.birdsim.input.AbstractInput;

/**
 * Created by david on 9/30/15.
 */
@Wire
public class PositionCam extends BaseSystem {
    private static final Vector2 TMP_VEC = new Vector2();
    private static final Vector2 TMP_VEC2 = new Vector2();
    ComponentMapper<MapCoordinate> coordMapper;
    ComponentMapper<Speed2> speedMapper;
    ComponentMapper<InputComponent> inputMapper;

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
            AbstractInput input = world.getManager(InputManager.class).players.get(inputMapper.get(e).id);
            TMP_VEC.add(pos.x, pos.y);
            if (input.isShooting()) {
                float rad = input.getShootRadiant();
                TMP_VEC2.add((float) Math.cos(rad), (float) Math.sin(rad));
            }// else
            TMP_VEC2.add(speed.x * 2, speed.y * 2);
        }
        if (players.size() > 0) {
            TMP_VEC.scl(1.0f / players.size());
            TMP_VEC2.scl(1.0f / players.size());
        }
        if (Config.FIXED_CAM) {
            campos.x = TMP_VEC.x;
            campos.y = TMP_VEC.y;
            return;
        }
        camspeed.x = (TMP_VEC.x + TMP_VEC2.x * 25 - campos.x) / 80.0f;
        camspeed.y = (TMP_VEC.y + TMP_VEC2.y * 25 - campos.y) / 80.0f;
        campos.x += camspeed.x;
        campos.y += camspeed.y;
    }
}
