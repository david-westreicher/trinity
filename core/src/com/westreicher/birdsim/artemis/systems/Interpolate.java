package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.managers.TagManager;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.westreicher.birdsim.Config;
import com.westreicher.birdsim.artemis.Artemis;
import com.westreicher.birdsim.artemis.components.CameraComponent;
import com.westreicher.birdsim.artemis.components.MapCoordinateComponent;
import com.westreicher.birdsim.artemis.components.RenderTransformComponent;
import com.westreicher.birdsim.artemis.components.Speed2Component;

/**
 * Created by david on 9/28/15.
 */
@Wire
public class Interpolate extends IteratingSystem {
    private ComponentMapper<MapCoordinateComponent> coordMapper;
    private ComponentMapper<Speed2Component> speedMapper;
    private ComponentMapper<RenderTransformComponent> transformMapper;
    private float delta;

    public Interpolate() {
        super(Aspect.all(MapCoordinateComponent.class, RenderTransformComponent.class));
    }

    @Override
    protected void begin() {
        this.delta = world.getDelta() - 1;
    }

    @Override
    protected void process(int e) {
        MapCoordinateComponent pos = coordMapper.get(e);
        RenderTransformComponent transform = transformMapper.get(e);
        if (speedMapper.has(e)) {
            Speed2Component speed = speedMapper.get(e);
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
        Entity camentity = world.getSystem(TagManager.class).getEntity(Artemis.VIRTUAL_CAM_TAG);
        RenderTransformComponent pos = camentity.getComponent(RenderTransformComponent.class);
        Camera cam = camentity.getComponent(CameraComponent.class).cam;
        if (Config.FIRST_PERSON) {
            cam.position.set(pos.x, pos.y - 20, cam.position.z);
            cam.lookAt(cam.position.x, cam.position.y + 100, 0);
            cam.up.set(0, 0, 1);
        } else {
            cam.position.set(pos.x, pos.y, cam.position.z);
        }
        cam.update();
    }
}
