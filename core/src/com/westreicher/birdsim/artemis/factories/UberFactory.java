package com.westreicher.birdsim.artemis.factories;

import com.artemis.Entity;
import com.artemis.World;
import com.artemis.utils.EntityBuilder;
import com.westreicher.birdsim.artemis.components.InputComponent;
import com.westreicher.birdsim.artemis.components.RenderPosition;
import com.westreicher.birdsim.artemis.components.MapCoordinate;
import com.westreicher.birdsim.artemis.components.ModelComponent;
import com.westreicher.birdsim.artemis.components.Speed2;
import com.westreicher.birdsim.artemis.managers.ModelManager;

/**
 * Created by david on 9/28/15.
 */
public final class UberFactory {
    public static Entity createPlayer(World w, int id) {
        Entity e = new EntityBuilder(w)
                .with(new MapCoordinate((float) Math.random() * 100 - 50, (float) Math.random() * 100 - 50))
                .with(new Speed2((float) Math.random() - 0.5f, (float) Math.random() - 0.5f))
                .with(new RenderPosition())
                .with(new InputComponent(id))
                .with(new ModelComponent(ModelManager.Models.PLAYER))
                .build();
        return e;
    }
}
