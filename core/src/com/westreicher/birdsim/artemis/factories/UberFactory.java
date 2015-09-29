package com.westreicher.birdsim.artemis.factories;

import com.artemis.Entity;
import com.artemis.EntityEdit;
import com.artemis.Manager;
import com.artemis.World;
import com.artemis.managers.GroupManager;
import com.artemis.managers.TagManager;
import com.artemis.utils.EntityBuilder;
import com.westreicher.birdsim.artemis.Artemis;
import com.westreicher.birdsim.artemis.components.CameraComponent;
import com.westreicher.birdsim.artemis.components.InputComponent;
import com.westreicher.birdsim.artemis.components.RenderTransform;
import com.westreicher.birdsim.artemis.components.MapCoordinate;
import com.westreicher.birdsim.artemis.components.ModelComponent;
import com.westreicher.birdsim.artemis.components.Speed2;
import com.westreicher.birdsim.artemis.managers.ModelManager;

/**
 * Created by david on 9/28/15.
 */
public class UberFactory extends Manager {

    public static Entity createPlayer(World w, int id) {
        Entity e = w.createEntity();
        EntityEdit edit = e.edit();
        MapCoordinate coord = edit.create(MapCoordinate.class);
        coord.x = (float) Math.random() * 100 - 50;
        coord.y = (float) Math.random() * 100 - 50;
        Speed2 speed = edit.create(Speed2.class);
        RenderTransform transform = edit.create(RenderTransform.class);
        InputComponent input = edit.create(InputComponent.class);
        input.id = id;
        ModelComponent model = edit.create(ModelComponent.class);
        model.type = ModelManager.modelsarr[(int) (Math.random() * ModelManager.modelsarr.length)];
        w.getManager(GroupManager.class).add(e, Artemis.PLAYER_GROUP);
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
}
