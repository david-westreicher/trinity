package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.managers.TagManager;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.math.Vector3;
import com.westreicher.birdsim.ChunkManager;
import com.westreicher.birdsim.Config;
import com.westreicher.birdsim.artemis.Artemis;
import com.westreicher.birdsim.artemis.components.CameraComponent;
import com.westreicher.birdsim.artemis.components.ModelComponent;
import com.westreicher.birdsim.artemis.components.RenderTransform;

/**
 * Created by david on 9/28/15.
 */
@Wire
public class AdjustHeight extends EntityProcessingSystem {
    private ChunkManager cm;
    private Vector3 cam;
    private ComponentMapper<RenderTransform> transformMapper;
    private ComponentMapper<ModelComponent> modelMapper;
    private float delta;

    public AdjustHeight() {
        super(Aspect.all(RenderTransform.class, ModelComponent.class));
    }

    @Override
    protected void begin() {
        cm = world.getSystem(TagManager.class).getEntity(Artemis.CHUNKMANAGER_TAG).getComponent(ChunkManager.class);
        cam = world.getSystem(TagManager.class).getEntity(Artemis.VIRTUAL_CAM_TAG).getComponent(CameraComponent.class).cam.position;
        delta = world.getDelta();
    }

    @Override
    protected void process(Entity e) {
        RenderTransform transform = transformMapper.get(e);
        ModelComponent model = modelMapper.get(e);
        float val = cm.getVal(transform.x, transform.y);
        if (val == ChunkManager.OUTSIDE) {
            model.visible = false;
            return;
        }
        float orig = val * Config.TERRAIN_HEIGHT;
        float dstx = transform.x - cam.x;
        float dsty = transform.y - cam.y;
        double dist = Config.SPHERE_RADIUS_SQUARED - dstx * dstx - dsty * dsty;
        model.visible = Config.POST_PROCESSING ? dist > 0 : true;
        if (Config.POST_PROCESSING) {
            transform.dist = (float) Math.sqrt(dist);
            transform.z = orig + transform.dist + model.scale;
        } else
            transform.z = (orig + model.scale);
    }
}
