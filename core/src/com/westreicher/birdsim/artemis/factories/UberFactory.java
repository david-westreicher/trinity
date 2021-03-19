package com.westreicher.birdsim.artemis.factories;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntityEdit;
import com.artemis.EntityTransmuter;
import com.artemis.EntityTransmuterFactory;
import com.artemis.Manager;
import com.artemis.World;
import com.artemis.annotations.Wire;
import com.artemis.managers.GroupManager;
import com.artemis.managers.TagManager;
import com.westreicher.birdsim.SlotSystem;
import com.westreicher.birdsim.artemis.Artemis;
import com.westreicher.birdsim.artemis.components.AIComponent;
import com.westreicher.birdsim.artemis.components.CameraComponent;
import com.westreicher.birdsim.artemis.components.CollidableComponent;
import com.westreicher.birdsim.artemis.components.EntityTypeComponent;
import com.westreicher.birdsim.artemis.components.HealthComponent;
import com.westreicher.birdsim.artemis.components.InputComponent;
import com.westreicher.birdsim.artemis.components.MapCoordinateComponent;
import com.westreicher.birdsim.artemis.components.ModelComponent;
import com.westreicher.birdsim.artemis.components.ParticleComponent;
import com.westreicher.birdsim.artemis.components.RenderTransformComponent;
import com.westreicher.birdsim.artemis.components.ShipComponent;
import com.westreicher.birdsim.artemis.components.SlotComponent;
import com.westreicher.birdsim.artemis.components.Speed2Component;
import com.westreicher.birdsim.artemis.components.TerrainCollisionComponent;
import com.westreicher.birdsim.artemis.managers.ModelManager;
import com.westreicher.birdsim.util.ColorAttr;

import java.util.Random;

/**
 * Created by david on 9/28/15.
 */
@Wire
public class UberFactory extends Manager {
    private ComponentMapper<MapCoordinateComponent> coordMapper;
    private ComponentMapper<InputComponent> inputMapper;
    private ComponentMapper<TerrainCollisionComponent> tcMapper;
    private ComponentMapper<ModelComponent> modelMapper;
    private ComponentMapper<ShipComponent> shipMapper;
    private ComponentMapper<Speed2Component> speedMapper;
    protected ComponentMapper<HealthComponent> mHealth;
    private ComponentMapper<CollidableComponent> collidableMapper;
    protected ComponentMapper<EntityTypeComponent> mEntityType;
    protected ComponentMapper<SlotComponent> mSlotComponent;
    private EntityTransmuter playerCreator;
    private EntityTransmuter enemyCreator;
    private EntityTransmuter itemCreator;
    private EntityTransmuter bulletCreator;
    private EntityTransmuter particleCreator;
    private EntityTransmuter shipCreator;

    @Override
    protected void initialize() {
        playerCreator = new EntityTransmuterFactory(world).
                add(MapCoordinateComponent.class).
                add(Speed2Component.class).
                add(RenderTransformComponent.class).
                add(InputComponent.class).
                add(ModelComponent.class).
                add(TerrainCollisionComponent.class).
                add(CollidableComponent.class).
                add(EntityTypeComponent.class).
                add(HealthComponent.class).
                add(SlotComponent.class).
                build();

        shipCreator = new EntityTransmuterFactory(world).
                add(MapCoordinateComponent.class).
                add(Speed2Component.class).
                add(RenderTransformComponent.class).
                add(ModelComponent.class).
                add(HealthComponent.class).
                add(EntityTypeComponent.class).
                add(ShipComponent.class).
                build();

        enemyCreator = new EntityTransmuterFactory(world).
                add(MapCoordinateComponent.class).
                add(Speed2Component.class).
                add(RenderTransformComponent.class).
                add(AIComponent.class).
                add(ModelComponent.class).
                add(TerrainCollisionComponent.class).
                add(CollidableComponent.class).
                add(EntityTypeComponent.class).
                add(HealthComponent.class).
                build();
        bulletCreator = new EntityTransmuterFactory(world).
                add(MapCoordinateComponent.class).
                add(Speed2Component.class).
                add(RenderTransformComponent.class).
                add(AIComponent.class).
                add(ModelComponent.class).
                add(TerrainCollisionComponent.class).
                add(CollidableComponent.class).
                add(EntityTypeComponent.class).
                add(HealthComponent.class).
                add(SlotComponent.class).
                build();

        itemCreator = new EntityTransmuterFactory(world).
                add(MapCoordinateComponent.class).
                add(RenderTransformComponent.class).
                add(ModelComponent.class).
                add(TerrainCollisionComponent.class).
                add(CollidableComponent.class).
                add(EntityTypeComponent.class).
                add(HealthComponent.class).
                build();

        particleCreator = new EntityTransmuterFactory(world).
                add(ParticleComponent.class).
                build();
    }

    public Entity createPlayer(World w, int id) {
        Entity e = w.createEntity();
        playerCreator.transmute(e);
        w.getSystem(GroupManager.class).add(e, Artemis.PLAYER_GROUP);
        inputMapper.get(e).id = id;
        ModelComponent model = modelMapper.get(e);
        model.type = ModelManager.Models.PLAYER;
        model.col = ColorAttr.RED;
        model.scale = 4;
        collidableMapper.get(e).scale = model.scale;
        mEntityType.get(e).type = EntityTypeComponent.Types.PLAYER;
        mSlotComponent.get(e).gunType.type = SlotSystem.GunType.MACHINEGUN;
        mSlotComponent.get(e).gunType.multiplier = 1;
        mSlotComponent.get(e).gunType.type = SlotSystem.GunType.ROCKETGUN;
        mSlotComponent.get(e).gunSpecial.type = SlotSystem.GunSpecialty.DAMAGE;
        mSlotComponent.get(e).gunSpecial.multiplier = 1;
        for (int i = 0; i < 10; i++)
            createShip(w, id);
        return e;
    }

    private Entity createShip(World w, int playerid) {
        Entity e = w.createEntity();
        shipCreator.transmute(e);
        MapCoordinateComponent coord = coordMapper.get(e);
        coord.x = (float) (Math.random() * 20 - 10);
        coord.y = (float) (Math.random() * 20 - 10);
        shipMapper.get(e).playerid = playerid;
        shipMapper.get(e).size = 5;
        ModelComponent model = modelMapper.get(e);
        model.type = ModelManager.Models.PLAYER;
        model.col = ColorAttr.BLUE;
        model.scale = 10;
        mEntityType.get(e).type = EntityTypeComponent.Types.SHIP;
        return e;
    }

    public static CameraComponent createCam(World w) {
        int e = w.create();
        EntityEdit edit = w.edit(e);
        CameraComponent cc = edit.create(CameraComponent.class);
        edit.create(MapCoordinateComponent.class);
        edit.create(Speed2Component.class);
        edit.create(RenderTransformComponent.class);
        w.getSystem(TagManager.class).register(Artemis.VIRTUAL_CAM_TAG, e);
        return cc;
    }

    public int createEnemy(World w, float x, float y, Random rand) {
        int e = w.create();
        enemyCreator.transmute(e);
        MapCoordinateComponent coord = coordMapper.get(e);
        coord.x = x;
        coord.y = y;
        Speed2Component speed = speedMapper.get(e);
        speed.x = (float) (Math.random() - 0.5);
        speed.y = (float) (Math.random() - 0.5);
        coord.x += speed.x;
        coord.y += speed.y;
        ModelComponent model = modelMapper.get(e);
        model.type = ModelManager.Models.PLAYER;
        model.col = ColorAttr.random(rand);
        model.scale = 8;
        collidableMapper.get(e).scale = model.scale;
        mEntityType.get(e).type = EntityTypeComponent.Types.ENEMY;
        mHealth.get(e).health = 10;
        return e;
    }

    public int shoot(World w, float x, float y, float xspeed, float yspeed, SlotComponent sc) {
        int e = w.create();
        bulletCreator.transmute(e);
        MapCoordinateComponent coord = coordMapper.get(e);
        coord.x = x;
        coord.y = y;
        Speed2Component speed = speedMapper.get(e);
        speed.x = xspeed;
        speed.y = yspeed;
        ModelComponent model = modelMapper.get(e);
        model.type = ModelManager.Models.BULLET;
        model.col = sc.gunType.type == SlotSystem.GunType.MACHINEGUN ? ColorAttr.TEAL : ColorAttr.RED;
        model.scale = sc.gunType.type.scale * sc.gunSpecial.getMultiplier(SlotSystem.GunSpecialty.DAMAGE);
        collidableMapper.get(e).scale = model.scale;
        mEntityType.get(e).type = EntityTypeComponent.Types.BULLET;
        mSlotComponent.get(e).set(sc);
        return e;
    }

    public int createItem(World w, float x, float y, Random rand) {
        int e = w.create();
        itemCreator.transmute(e);
        MapCoordinateComponent coord = coordMapper.get(e);
        coord.x = x;
        coord.y = y;
        ModelComponent model = modelMapper.get(e);
        model.type = ModelManager.Models.ITEM;
        model.col = ColorAttr.random(rand);
        model.scale = 2;
        collidableMapper.get(e).scale = model.scale;
        mEntityType.get(e).type = EntityTypeComponent.Types.ITEM;
        return e;
    }

    public int createParticleSystem(World w) {
        int e = w.create();
        particleCreator.transmute(e);
        w.getSystem(TagManager.class).register(Artemis.PARTICLE_SYS_TAG, e);
        return e;
    }
}
