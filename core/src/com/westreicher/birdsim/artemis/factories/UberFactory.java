package com.westreicher.birdsim.artemis.factories;

import com.artemis.Entity;
import com.artemis.EntityEdit;
import com.artemis.Manager;
import com.artemis.World;
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
public class UberFactory extends Manager {

    g

    public static Entity createPlayer(World w, int id) {
        Entity e = w.createEntity();
        EntityEdit edit = e.edit();
        MapCoordinate coord = edit.create(MapCoordinate.class);
        coord.x = 0;
        coord.y = 0;
        Speed2 speed = edit.create(Speed2.class);
        RenderTransform transform = edit.create(RenderTransform.class);
        InputComponent input = edit.create(InputComponent.class);
        input.id = id;
        ModelComponent model = edit.create(ModelComponent.class);
        model.type = ModelManager.Models.PLAYER;
        model.col = ColorAttr.random();
        model.scale = 10;
        w.getManager(GroupManager.class).add(e, Artemis.PLAYER_GROUP);
        Health health = edit.create(Health.class);
        health.health = 10;
        TerrainCollision tc = edit.create(TerrainCollision.class);
        tc.type = TerrainCollision.Types.PLAYER;
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

    public static Entity createEnemy(World w, float x, float y) {
        Entity e = w.createEntity();
        EntityEdit edit = e.edit();
        MapCoordinate coord = edit.create(MapCoordinate.class);
        coord.x = x;
        coord.y = y;
        Speed2 speed = edit.create(Speed2.class);
        speed.x = (float) (Math.random() - 0.5);
        speed.y = (float) (Math.random() - 0.5);
        coord.x += speed.x;
        coord.y += speed.y;
        RenderTransform transform = edit.create(RenderTransform.class);
        ModelComponent model = edit.create(ModelComponent.class);
        AIComponent ai = edit.create(AIComponent.class);
        model.type = ModelManager.Models.PLAYER;
        model.col = ColorAttr.random();
        Health health = edit.create(Health.class);
        TerrainCollision tc = edit.create(TerrainCollision.class);
        tc.type = TerrainCollision.Types.ENEMY;
        health.health = 10;
        return e;
    }

    public static Entity shoot(World w, float x, float y, float xspeed, float yspeed) {
        Entity e = w.createEntity();
        EntityEdit edit = e.edit();
        MapCoordinate coord = edit.create(MapCoordinate.class);
        coord.x = x;
        coord.y = y;
        Speed2 speed = edit.create(Speed2.class);
        speed.x = xspeed;
        speed.y = yspeed;
        RenderTransform transform = edit.create(RenderTransform.class);
        ModelComponent model = edit.create(ModelComponent.class);
        model.type = ModelManager.Models.BULLET;
        model.col = ColorAttr.random();
        Health health = edit.create(Health.class);
        health.health = 10;
        TerrainCollision tc = edit.create(TerrainCollision.class);
        tc.type = TerrainCollision.Types.BULLET;
        return e;
    }

    public static Entity createItem(World w, float x, float y) {
        Entity e = w.createEntity();
        EntityEdit edit = e.edit();
        MapCoordinate coord = edit.create(MapCoordinate.class);
        coord.x = x;
        coord.y = y;
        RenderTransform transform = edit.create(RenderTransform.class);
        ModelComponent model = edit.create(ModelComponent.class);
        model.type = ModelManager.Models.ITEM;
        model.col = ColorAttr.random();
        model.scale = (float) (Math.random() * 5 + 1);
        return e;
    }
}
