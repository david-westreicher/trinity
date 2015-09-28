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
import com.westreicher.birdsim.artemis.components.RenderPosition;

/**
 * Created by david on 9/28/15.
 */
@Wire
public class AdjustHeight extends EntityProcessingSystem {
    private ChunkManager cm;
    private Vector3 cam;
    private ComponentMapper<RenderPosition> interpMapper;
    private ComponentMapper<ModelComponent> modelMapper;
    private float delta;

    public AdjustHeight() {
        super(Aspect.all(RenderPosition.class, ModelComponent.class));
    }

    @Override
    protected void begin() {
        cm = world.getManager(TagManager.class).getEntity(Artemis.CHUNKMANAGER_TAG).getComponent(ChunkManager.class);
        cam = world.getManager(TagManager.class).getEntity(Artemis.VIRTUAL_CAM_TAG).getComponent(CameraComponent.class).cam.position;
        delta = world.getDelta();
    }

    @Override
    protected void process(Entity e) {
        RenderPosition interp = interpMapper.get(e);
        ModelComponent model = modelMapper.get(e);
        float orig = cm.getVal(interp.x, interp.y) * Config.TERRAIN_HEIGHT;
        float toZ = 0;
        if (Config.POST_PROCESSING) {
            //TODO optimize Z projection
            float dstx = interp.x - cam.x;
            float dsty = interp.y - cam.y;
            float dstsq = dstx * dstx + dsty * dsty;
            float dstfrac = (dstsq / (140f * 140f));
            model.visible = dstfrac <= 1;
            toZ = (orig + (1.0f - dstfrac) * 140.0f + 5);
        } else {
            model.visible = true;
            toZ = (orig + 5);
        }
        interp.z = toZ;
    }
}
