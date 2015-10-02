package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.westreicher.birdsim.Config;
import com.westreicher.birdsim.artemis.components.AnimationComponent;
import com.westreicher.birdsim.artemis.components.Collidable;
import com.westreicher.birdsim.artemis.components.Health;
import com.westreicher.birdsim.artemis.components.MapCoordinate;
import com.westreicher.birdsim.artemis.components.Speed2;
import com.westreicher.birdsim.artemis.components.TerrainCollision;

/**
 * Created by david on 9/29/15.
 */
@Wire
public class DeleteEntities extends EntityProcessingSystem {
    private static final float EDGE = Config.TILES_PER_CHUNK * Config.CHUNKNUMS / 2;
    private ComponentMapper<Health> healthMapper;
    private ComponentMapper<MapCoordinate> posMapper;

    public DeleteEntities() {
        super(Aspect.all(Health.class, MapCoordinate.class));
    }

    @Override
    protected void process(Entity e) {
        Health health = healthMapper.get(e);
        MapCoordinate pos = posMapper.get(e);
        if (health.health <= 0) {
            e.edit().
                    remove(Health.class).
                    remove(Speed2.class).
                    remove(Collidable.class).
                    remove(TerrainCollision.class).
                    create(AnimationComponent.class);
            //world.deleteEntity(e);
            return;
        }
        if (Math.abs(pos.x) > EDGE || Math.abs(pos.y) > EDGE)
            world.deleteEntity(e);
    }
}
