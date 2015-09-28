package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.managers.TagManager;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.westreicher.birdsim.Config;
import com.westreicher.birdsim.artemis.Artemis;
import com.westreicher.birdsim.artemis.components.CameraComponent;
import com.westreicher.birdsim.artemis.components.ModelComponent;
import com.westreicher.birdsim.artemis.components.RenderPosition;
import com.westreicher.birdsim.artemis.components.Speed2;

/**
 * Created by david on 9/28/15.
 */
@Wire
public class RenderModels extends EntityProcessingSystem {
    private ComponentMapper<RenderPosition> interpMapper;
    private ComponentMapper<Speed2> speedMapper;
    private ComponentMapper<ModelComponent> modelMapper;
    private ModelBatch mb;
    private float delta;

    public RenderModels() {
        super(Aspect.all(RenderPosition.class, Speed2.class, ModelComponent.class));
    }

    @Override
    protected void initialize() {
        mb = new ModelBatch();
    }

    @Override
    protected void begin() {
        Camera cam = world.getManager(TagManager.class).getEntity(Artemis.VIRTUAL_CAM_TAG).getComponent(CameraComponent.class).cam;
        mb.begin(cam);
        delta = world.getDelta();
    }

    @Override
    protected void end() {
        mb.end();
    }

    @Override
    protected void process(Entity e) {
        ModelComponent model = modelMapper.get(e);
        if (!model.visible) return;
        ModelInstance mi = model.type.modelinst;
        RenderPosition pos = interpMapper.get(e);
        Speed2 speed = speedMapper.get(e);
        mi.transform.setToTranslation(pos.x, pos.y, pos.z);
        mi.transform.scl(model.scale);
        mi.transform.rotateRad(Config.UPAXIS, (float) -Math.atan2(-speed.y, speed.x));
        mb.render(mi);
    }

    @Override
    protected void dispose() {
        mb.dispose();
    }
}
