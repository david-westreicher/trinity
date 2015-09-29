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
import com.westreicher.birdsim.artemis.components.RenderTransform;
import com.westreicher.birdsim.artemis.components.Speed2;

/**
 * Created by david on 9/28/15.
 */
@Wire
public class RenderModels extends EntityProcessingSystem {
    private ComponentMapper<RenderTransform> interpMapper;
    private ComponentMapper<ModelComponent> modelMapper;
    private ModelBatch mb;

    public RenderModels() {
        super(Aspect.all(RenderTransform.class, ModelComponent.class));
    }

    @Override
    protected void initialize() {
        mb = new ModelBatch();
    }

    @Override
    protected void begin() {
        Camera cam = world.getManager(TagManager.class).getEntity(Artemis.VIRTUAL_CAM_TAG).getComponent(CameraComponent.class).cam;
        mb.begin(cam);
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
        RenderTransform transform = interpMapper.get(e);
        mi.materials.get(0).set(model.col.attr);
        mi.transform.setToTranslation(transform.x, transform.y, transform.z);
        mi.transform.scl(model.scale);
        mi.transform.rotateRad(Config.UPAXIS, transform.radiant);
        mb.render(mi);
    }

    @Override
    protected void dispose() {
        mb.dispose();
    }
}
