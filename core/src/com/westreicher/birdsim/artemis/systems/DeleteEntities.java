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
import com.westreicher.birdsim.artemis.components.CollidableComponent;
import com.westreicher.birdsim.artemis.components.EntityTypeComponent;
import com.westreicher.birdsim.artemis.components.HealthComponent;
import com.westreicher.birdsim.artemis.components.MapCoordinateComponent;
import com.westreicher.birdsim.artemis.components.ModelComponent;
import com.westreicher.birdsim.artemis.components.SlotComponent;
import com.westreicher.birdsim.artemis.components.TerrainCollisionComponent;
import com.westreicher.birdsim.artemis.factories.UberFactory;

import java.util.Random;

/**
 * Created by david on 9/29/15.
 */
@Wire
public class DeleteEntities extends IteratingSystem {
    private static final Random TMP_RAND = new Random();
    private static final float EDGE = Config.TILES_PER_CHUNK * Config.CHUNKNUMS / 2;
    private ComponentMapper<HealthComponent> healthMapper;
    private ComponentMapper<MapCoordinateComponent> posMapper;
    protected ComponentMapper<ModelComponent> mModelComponent;
    protected ComponentMapper<EntityTypeComponent> mEntityType;
    private ChunkManager cm;

    public DeleteEntities() {
        super(Aspect.all(HealthComponent.class, MapCoordinateComponent.class, EntityTypeComponent.class));
    }

    @Override
    protected void begin() {
        cm = world.getSystem(TagManager.class).getEntity(Artemis.CHUNKMANAGER_TAG).getComponent(ChunkManager.class);
    }

    @Override
    protected void process(int e) {
        HealthComponent healthComponent = healthMapper.get(e);
        MapCoordinateComponent pos = posMapper.get(e);
        EntityTypeComponent.Types type = mEntityType.get(e).type;
        if (healthComponent.health <= 0) {
            switch (type) {
                case BULLET:
                    // TODO old from slot
                    int worlddamage = 0;
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
                remove(HealthComponent.class).
                remove(CollidableComponent.class).
                remove(TerrainCollisionComponent.class).
                create(AnimationComponent.class);
    }
}
