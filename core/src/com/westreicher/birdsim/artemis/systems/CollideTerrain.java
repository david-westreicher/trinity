package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.managers.GroupManager;
import com.artemis.managers.TagManager;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.math.Vector3;
import com.westreicher.birdsim.ChunkManager;
import com.westreicher.birdsim.artemis.Artemis;
import com.westreicher.birdsim.artemis.components.MapCoordinate;
import com.westreicher.birdsim.artemis.components.Speed2;
import com.westreicher.birdsim.artemis.components.TerrainCollision;

/**
 * Created by david on 9/30/15.
 */
@Wire
public class CollideTerrain extends EntityProcessingSystem {
    private static final int MOVEMENT_TESTS = 4;
    ComponentMapper<TerrainCollision> tcMapper;
    ComponentMapper<MapCoordinate> posMapper;
    ComponentMapper<Speed2> speedMapper;
    private ChunkManager cm;
    private GroupManager groupmanager;

    public CollideTerrain() {
        super(Aspect.all(TerrainCollision.class, Speed2.class, MapCoordinate.class));
    }

    @Override
    protected void begin() {
        cm = world.getManager(TagManager.class).getEntity(Artemis.CHUNKMANAGER_TAG).getComponent(ChunkManager.class);
        groupmanager = world.getManager(GroupManager.class);
    }

    @Override
    protected void process(Entity e) {
        MapCoordinate pos = posMapper.get(e);
        TerrainCollision.Types type = tcMapper.get(e).type;
        float val = cm.getVal(pos.x, pos.y);
        if (val <= 0) return;
        if (type == TerrainCollision.Types.BULLET) {
            cm.explode2(pos.x, pos.y, 10);
            world.deleteEntity(e);
        } else {
            //PLAYER/ENEMY
            Speed2 speed = speedMapper.get(e);
            pos.x -= speed.x;
            pos.y -= speed.y;
            if (!tryToMove(pos, speed)) {
                if (type == TerrainCollision.Types.PLAYER) {
                    speed.x = 0;
                    speed.y = 0;
                } else if (type == TerrainCollision.Types.ENEMY) {
                    speed.x = (float) (Math.random() - 0.5);
                    speed.y = (float) (Math.random() - 0.5);
                }
            }
        }
    }

    private boolean tryToMove(MapCoordinate pos, Speed2 speed) {
        float speedlength = Vector3.dst(0, 0, 0, speed.x, speed.y, 0);
        float radiant = (float) Math.atan2(speed.y, speed.x);
        float frac = (float) ((Math.PI / 2.0) / MOVEMENT_TESTS);
        for (int i = 1; i <= MOVEMENT_TESTS; i++) {
            for (int j = 0; j < 2; j++) {
                float offset = i * frac * (j == 0 ? -1 : 1);
                float rotatedx = (float) (Math.cos(radiant + offset) * speedlength);
                float rotatedy = (float) (Math.sin(radiant + offset) * speedlength);
                if (cm.getVal(pos.x + rotatedx, pos.y + rotatedy) <= 0) {
                    pos.x += rotatedx;
                    pos.y += rotatedy;
                    speed.x = rotatedx;
                    speed.y = rotatedy;
                    return true;
                }
            }
        }
        return false;
    }
}
