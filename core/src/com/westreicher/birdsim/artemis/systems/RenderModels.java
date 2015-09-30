package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.managers.TagManager;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.utils.Pool;
import com.westreicher.birdsim.Config;
import com.westreicher.birdsim.artemis.Artemis;
import com.westreicher.birdsim.artemis.components.CameraComponent;
import com.westreicher.birdsim.artemis.components.ModelComponent;
import com.westreicher.birdsim.artemis.components.RenderTransform;

/**
 * Created by david on 9/28/15.
 */
@Wire
public class RenderModels extends EntityProcessingSystem {
    private ComponentMapper<RenderTransform> interpMapper;
    private ComponentMapper<ModelComponent> modelMapper;
    private ModelBatch mb;
    //TODO hacky, maybe use nicer pool
    private Material[] materialpool = new Material[1000];
    private int entindex;
    private Environment env;

    public RenderModels() {
        super(Aspect.all(RenderTransform.class, ModelComponent.class));
    }

    @Override
    protected void initialize() {
        env = new Environment();
        env.add(new DirectionalLight().set(Color.WHITE, 1, -1, -1));
        mb = new ModelBatch();
        for (int i = 0; i < materialpool.length; i++)
            materialpool[i] = new Material();
    }

    @Override
    protected void begin() {
        Camera cam = world.getManager(TagManager.class).getEntity(Artemis.VIRTUAL_CAM_TAG).getComponent(CameraComponent.class).cam;
        mb.begin(cam);
        entindex = 0;
    }


    @Override
    protected void process(Entity e) {
        ModelComponent model = modelMapper.get(e);
        if (!model.visible) return;
        ModelInstance mi = model.type.modelinst;
        Material mat = materialpool[entindex++];
        mat.set(model.col.attr);
        for (Node n : mi.nodes) {
            for (NodePart p : n.parts)
                p.material = mat;
        }
        RenderTransform transform = interpMapper.get(e);
        mi.transform.setToTranslation(transform.x, transform.y, transform.z);
        mi.transform.scl(model.scale);
        mi.transform.rotateRad(Config.UPAXIS, transform.radiant);
        mb.render(mi, env);
    }

    @Override
    protected void end() {
        mb.end();
    }

    @Override
    protected void dispose() {
        mb.dispose();
    }
}
