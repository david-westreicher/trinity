package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.managers.TagManager;
import com.artemis.systems.IteratingSystem;
import com.westreicher.birdsim.ChunkManager;
import com.westreicher.birdsim.Config;
import com.westreicher.birdsim.SlotSystem;
import com.westreicher.birdsim.artemis.Artemis;
import com.westreicher.birdsim.artemis.components.AnimationComponent;
import com.westreicher.birdsim.artemis.components.Collidable;
import com.westreicher.birdsim.artemis.components.EntityType;
import com.westreicher.birdsim.artemis.components.Health;
import com.westreicher.birdsim.artemis.components.MapCoordinate;
import com.westreicher.birdsim.artemis.components.ModelComponent;
import com.westreicher.birdsim.artemis.components.SlotComponent;
import com.westreicher.birdsim.artemis.components.TerrainCollision;
import com.westreicher.birdsim.artemis.factories.UberFactory;

import java.util.Random;

/**
 * Created by david on 9/29/15.
 */
@Wire
public class DeleteEntities extends IteratingSystem {
    private static final Random TMP_RAND = new Random();
    private static final float EDGE = Config.TILES_PER_CHUNK * Config.CHUNKNUMS / 2;
    private ComponentMapper<Health> healthMapper;
    private ComponentMapper<MapCoordinate> posMapper;
    protected ComponentMapper<ModelComponent> mModelComponent;
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
    protected void process(int e) {
        Health health = healthMapper.get(e);
        MapCoordinate pos = posMapper.get(e);
        EntityType.Types type = mEntityType.get(e).type;
        if (health.health <= 0) {
            switch (type) {
                case BULLET:
                    SlotComponent slot = mSlotComponent.get(e);
                    int worlddamage = slot.gunType.type.worlddamage * slot.gunSpecial.getMultiplier(SlotSystem.GunSpecialty.DAMAGE);
                    for (int i = 0; i < worlddamage * 3 + 1; i++)
                        world.getSystem(AnimateParticles.class).spawnParticle(pos.x + (float) TMP_RAND.nextGaussian() * worlddamage / 4, pos.y + (float) TMP_RAND.nextGaussian() * worlddamage / 4);
                    if (worlddamage > 0)
                        cm.explode2(pos.x, pos.y, worlddamage);
                    break;
                case ENEMY:
                    for (int i = 0; i < Math.max(1, mModelComponent.get(e).scale); i++)
                        world.getSystem(AnimateParticles.class).spawnParticle(pos.x, pos.y);
                    world.getSystem(UberFactory.class).createItem(world, pos.x, pos.y, null);
                    deathAnim(e);
                    return;
                case ITEM:
                    deathAnim(e);
                    return;
            }
            //TODO delete bullet entities is expensive
            world.delete(e);
            return;
        }
        if (Math.abs(pos.x) > EDGE || Math.abs(pos.y) > EDGE)
            world.delete(e);
    }

    private void deathAnim(int e) {
        world.edit(e).
                remove(Health.class).
                remove(Collidable.class).
                remove(TerrainCollision.class).
                create(AnimationComponent.class);
    }
}
