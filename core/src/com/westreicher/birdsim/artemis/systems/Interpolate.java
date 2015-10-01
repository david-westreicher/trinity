package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.managers.TagManager;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.westreicher.birdsim.artemis.Artemis;
import com.westreicher.birdsim.artemis.components.CameraComponent;
import com.westreicher.birdsim.artemis.components.MapCoordinate;
import com.westreicher.birdsim.artemis.components.RenderTransform;
import com.westreicher.birdsim.artemis.components.Speed2;

/**
 * Created by david on 9/28/15.
 */
@Wire
public class Interpolate extends EntityProcessingSystem {
    private ComponentMapper<MapCoordinate> coordMapper;
    private ComponentMapper<Speed2> speedMapper;
    private ComponentMapper<RenderTransform> transformMapper;
    private float delta;

    public Interpolate() {
        super(Aspect.all(MapCoordinate.class, RenderTransform.class));
    }

    @Override
    protected void begin() {
        this.delta = world.getDelta() - 1;
    }

    @Override
    protected void process(Entity e) {
        MapCoordinate pos = coordMapper.get(e);
        RenderTransform transform = transformMapper.get(e);
        if (speedMapper.has(e)) {
            Speed2 speed = speedMapper.get(e);
            transform.x = pos.x + speed.x * delta;
            transform.y = pos.y + speed.y * delta;
            if (speed.x != 0 || speed.y != 0) {
                //float toRot = Util.interpolateRot(transform.radiant, -(float) Math.atan2(-speed.y, speed.x), 0.05f);
                //transform.radiant += toRot;
                transform.radiant = -(float) Math.atan2(-speed.y, speed.x);
            }
        } else {
            transform.x = pos.x;
            transform.y = pos.y;
        }
    }

    @Override
    protected void end() {
        interpolateCam();
    }

    private void interpolateCam() {
        Entity camentity = world.getManager(TagManager.class).getEntity(Artemis.VIRTUAL_CAM_TAG);
        RenderTransform pos = camentity.getComponent(RenderTransform.class);
        Camera cam = camentity.getComponent(CameraComponent.class).cam;
        cam.position.set(pos.x, pos.y, 200);
        cam.far = 500;
        cam.update();
    }
}
