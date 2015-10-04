package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.managers.TagManager;
import com.artemis.systems.EntityProcessingSystem;
import com.westreicher.birdsim.ChunkManager;
import com.westreicher.birdsim.Config;
import com.westreicher.birdsim.SlotSystem;
import com.westreicher.birdsim.artemis.Artemis;
import com.westreicher.birdsim.artemis.components.AnimationComponent;
import com.westreicher.birdsim.artemis.components.Collidable;
import com.westreicher.birdsim.artemis.components.EntityType;
import com.westreicher.birdsim.artemis.components.Health;
import com.westreicher.birdsim.artemis.components.MapCoordinate;
import com.westreicher.birdsim.artemis.components.SlotComponent;
import com.westreicher.birdsim.artemis.components.TerrainCollision;
import com.westreicher.birdsim.artemis.factories.UberFactory;

/**
 * Created by david on 9/29/15.
 */
@Wire
public class DeleteEntities extends EntityProcessingSystem {
    private static final float EDGE = Config.TILES_PER_CHUNK * Config.CHUNKNUMS / 2;
    private ComponentMapper<Health> healthMapper;
    private ComponentMapper<MapCoordinate> posMapper;
    protected ComponentMapper<EntityType> mEntityType;
    protected ComponentMapper<SlotComponent> mSlotComponent;
    private ChunkManager cm;

    public DeleteEntities() {
        super(Aspect.all(Health.class, MapCoordinate.class, EntityType.class));
    }

    @Override
    protected void begin() {
        cm = world.getSystem(TagManager.class).getEntity(Artemis.CHUNKMANAGER_TAG).getComponent(ChunkManager.class);
    }

    @Override
    protected void process(Entity e) {
        Health health = healthMapper.get(e);
        MapCoordinate pos = posMapper.get(e);
        if (health.health <= 0) {
            switch (mEntityType.get(e).type) {
                case BULLET:
                    SlotComponent slot = mSlotComponent.get(e);
                    int worlddamage = slot.gunType.type.worlddamage * slot.gunSpecial.getMultiplier(SlotSystem.GunSpecialty.DAMAGE);
                    if (worlddamage > 0)
                        cm.explode2(pos.x, pos.y, worlddamage);
                    world.deleteEntity(e);
                    return;
                case ENEMY:
                    world.getSystem(UberFactory.class).createItem(world, pos.x, pos.y, null);
                case ITEM:
                    e.edit().
                            remove(Health.class).
                            remove(Collidable.class).
                            remove(TerrainCollision.class).
                            create(AnimationComponent.class);
                    break;
                default:
                    world.deleteEntity(e);
            }
            return;
        }
        if (Math.abs(pos.x) > EDGE || Math.abs(pos.y) > EDGE)
            world.deleteEntity(e);
    }
}
