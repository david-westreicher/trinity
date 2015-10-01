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
import com.westreicher.birdsim.artemis.Artemis;
import com.westreicher.birdsim.artemis.components.AIComponent;
import com.westreicher.birdsim.artemis.components.CameraComponent;
import com.westreicher.birdsim.artemis.components.Health;
import com.westreicher.birdsim.artemis.components.InputComponent;
import com.westreicher.birdsim.artemis.components.MapCoordinate;
import com.westreicher.birdsim.artemis.components.ModelComponent;
import com.westreicher.birdsim.artemis.components.RenderTransform;
import com.westreicher.birdsim.artemis.components.Speed2;
import com.westreicher.birdsim.artemis.components.TerrainCollision;
import com.westreicher.birdsim.artemis.managers.ModelManager;
import com.westreicher.birdsim.util.ColorAttr;

/**
 * Created by david on 9/28/15.
 */
@Wire
public class UberFactory extends Manager {
    private ComponentMapper<MapCoordinate> coordMapper;
    private ComponentMapper<InputComponent> inputMapper;
    private ComponentMapper<TerrainCollision> tcMapper;
    private ComponentMapper<ModelComponent> modelMapper;
    private ComponentMapper<Speed2> speedMapper;
    private EntityTransmuter playerCreator;
    private EntityTransmuter enemyCreator;
    private EntityTransmuter itemCreator;
    private EntityTransmuter bulletCreator;

    @Override
    protected void initialize() {
        playerCreator = new EntityTransmuterFactory(world).
                add(MapCoordinate.class).
                add(Speed2.class).
                add(RenderTransform.class).
                add(InputComponent.class).
                add(ModelComponent.class).
                add(TerrainCollision.class).
                add(Health.class).
                build();

        enemyCreator = new EntityTransmuterFactory(world).
                add(MapCoordinate.class).
                add(Speed2.class).
                add(RenderTransform.class).
                add(AIComponent.class).
                add(ModelComponent.class).
                add(TerrainCollision.class).
                add(Health.class).
                build();
        bulletCreator = new EntityTransmuterFactory(world).
                add(MapCoordinate.class).
                add(Speed2.class).
                add(RenderTransform.class).
                add(AIComponent.class).
                add(ModelComponent.class).
                add(TerrainCollision.class).
                add(Health.class).
                build();

        itemCreator = new EntityTransmuterFactory(world).
                add(MapCoordinate.class).
                add(RenderTransform.class).
                add(ModelComponent.class).
                add(TerrainCollision.class).
                add(Health.class).
                build();
    }

    public Entity createPlayer(World w, int id) {
        Entity e = w.createEntity();
        playerCreator.transmute(e);
        w.getManager(GroupManager.class).add(e, Artemis.PLAYER_GROUP);
        inputMapper.get(e).id = id;
        ModelComponent model = modelMapper.get(e);
        model.type = ModelManager.Models.PLAYER;
        model.col = ColorAttr.RED;
        model.scale = 10;
        tcMapper.get(e).type = TerrainCollision.Types.PLAYER;
        return e;
    }

    public static CameraComponent createCam(World w) {
        Entity e = w.createEntity();
        EntityEdit edit = e.edit();
        CameraComponent cc = edit.create(CameraComponent.class);
        edit.create(MapCoordinate.class);
        edit.create(Speed2.class);
        edit.create(RenderTransform.class);
        w.getManager(TagManager.class).register(Artemis.VIRTUAL_CAM_TAG, e);
        return cc;
    }

    public Entity createEnemy(World w, float x, float y) {
        Entity e = w.createEntity();
        enemyCreator.transmute(e);
        MapCoordinate coord = coordMapper.get(e);
        coord.x = x;
        coord.y = y;
        Speed2 speed = speedMapper.get(e);
        speed.x = (float) (Math.random() - 0.5);
        speed.y = (float) (Math.random() - 0.5);
        coord.x += speed.x;
        coord.y += speed.y;
        ModelComponent model = modelMapper.get(e);
        model.type = ModelManager.Models.PLAYER;
        model.col = ColorAttr.random();
        model.scale = 8;
        tcMapper.get(e).type = TerrainCollision.Types.ENEMY;
        return e;
    }

    public Entity shoot(World w, float x, float y, float xspeed, float yspeed) {
        Entity e = w.createEntity();
        bulletCreator.transmute(e);

        MapCoordinate coord = coordMapper.get(e);
        coord.x = x;
        coord.y = y;
        Speed2 speed = speedMapper.get(e);
        speed.x = xspeed;
        speed.y = yspeed;
        ModelComponent model = modelMapper.get(e);
        model.type = ModelManager.Models.BULLET;
        model.col = ColorAttr.random();
        model.scale = 5;
        tcMapper.get(e).type = TerrainCollision.Types.BULLET;
        return e;
    }

    public Entity createItem(World w, float x, float y) {
        Entity e = w.createEntity();
        itemCreator.transmute(e);
        MapCoordinate coord = coordMapper.get(e);
        coord.x = x;
        coord.y = y;
        ModelComponent model = modelMapper.get(e);
        model.type = ModelManager.Models.ITEM;
        model.col = ColorAttr.random();
        model.scale = (float) (Math.random() * 5 + 1);
        return e;
    }
}
