package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.managers.TagManager;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector3;
import com.westreicher.birdsim.ChunkManager;
import com.westreicher.birdsim.artemis.Artemis;
import com.westreicher.birdsim.artemis.components.EntityTypeComponent;
import com.westreicher.birdsim.artemis.components.HealthComponent;
import com.westreicher.birdsim.artemis.components.MapCoordinateComponent;
import com.westreicher.birdsim.artemis.components.Speed2Component;
import com.westreicher.birdsim.artemis.components.TerrainCollisionComponent;

/**
 * Created by david on 9/30/15.
 */
@Wire
public class CollideTerrain extends IteratingSystem {
    private static final int MOVEMENT_TESTS = 4;
    ComponentMapper<TerrainCollisionComponent> tcMapper;
    ComponentMapper<MapCoordinateComponent> posMapper;
    ComponentMapper<Speed2Component> speedMapper;
    protected ComponentMapper<HealthComponent> mHealth;
    protected ComponentMapper<EntityTypeComponent> mEntityType;
    private ChunkManager cm;

    public CollideTerrain() {
        super(Aspect.all(TerrainCollisionComponent.class, Speed2Component.class, MapCoordinateComponent.class, EntityTypeComponent.class));
    }

    @Override
    protected void begin() {
        cm = world.getSystem(TagManager.class).getEntity(Artemis.CHUNKMANAGER_TAG).getComponent(ChunkManager.class);
    }

    @Override
    protected void process(int e) {
        MapCoordinateComponent pos = posMapper.get(e);
        EntityTypeComponent.Types type = mEntityType.get(e).type;
        float val = cm.getVal(pos.x, pos.y);
        if (val <= 0) return;
        if (type == EntityTypeComponent.Types.BULLET) {
            mHealth.get(e).health = 0;
        } else {
            //PLAYER/ENEMY
            Speed2Component speed = speedMapper.get(e);
            pos.x -= speed.x;
            pos.y -= speed.y;
            if (!tryToMove(pos, speed)) {
                if (type == EntityTypeComponent.Types.PLAYER) {
                    speed.x = 0;
                    speed.y = 0;
                } else if (type == EntityTypeComponent.Types.ENEMY) {
                    speed.x = (float) (Math.random() - 0.5);
                    speed.y = (float) (Math.random() - 0.5);
                }
            }
        }
    }

    private boolean tryToMove(MapCoordinateComponent pos, Speed2Component speed) {
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
