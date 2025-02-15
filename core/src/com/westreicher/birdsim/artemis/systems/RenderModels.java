package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.managers.TagManager;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Quaternion;
import com.westreicher.birdsim.Config;
import com.westreicher.birdsim.artemis.Artemis;
import com.westreicher.birdsim.artemis.components.CameraComponent;
import com.westreicher.birdsim.artemis.components.ModelComponent;
import com.westreicher.birdsim.artemis.components.RenderTransformComponent;

/**
 * Created by david on 9/28/15.
 */
@Wire
public class RenderModels extends IteratingSystem {
    private static final Quaternion TMP_QUAT = new Quaternion();
    private ComponentMapper<RenderTransformComponent> interpMapper;
    private ComponentMapper<ModelComponent> modelMapper;
    protected ModelBatch mb;
    //TODO hacky, maybe use nicer pool
    private Material[] materialpool = new Material[1000];
    private int entindex;
    private Environment env;
    private Camera cam;

    public RenderModels() {
        super(Aspect.all(RenderTransformComponent.class, ModelComponent.class));
    }

    @Override
    protected void initialize() {
        env = new Environment();
        env.set(new ColorAttribute(ColorAttribute.AmbientLight, new Color(0x1e90ffff)));
        env.add(new DirectionalLight().set(new Color(0xb67e5bff), 1, -1, -1));
        mb = new ModelBatch();
        for (int i = 0; i < materialpool.length; i++)
            materialpool[i] = new Material();
    }

    @Override
    protected void begin() {
        cam = world.getSystem(TagManager.class).getEntity(Artemis.VIRTUAL_CAM_TAG).getComponent(CameraComponent.class).cam;
        mb.begin(cam);
        entindex = 0;
    }


    @Override
    protected void process(int e) {
        ModelComponent model = modelMapper.get(e);
        if (!model.visible) return;
        ModelInstance mi = model.type.modelinst;
        Material mat = materialpool[entindex++];
        mat.set(model.col.attr);
        model.type.part.material = mat;
        RenderTransformComponent transform = interpMapper.get(e);
        mi.transform.setToTranslation(transform.x, transform.y, transform.z);
        mi.transform.scl(model.scale);

        if (Config.POST_PROCESSING) {
            float yaw = -(float) Math.atan((cam.position.x - transform.x) / transform.dist);
            float pitch = (float) Math.atan((cam.position.y - transform.y) / transform.dist);
            TMP_QUAT.setEulerAnglesRad(yaw, pitch, transform.radiant);
        } else {
            TMP_QUAT.setEulerAnglesRad(0, 0, transform.radiant);
        }
        mi.transform.rotate(TMP_QUAT);
        //TODO observation through apitrace:
        //TODO   env is not efficiently batched -> light direction, ambient uniforms, normal matrix, ... are set per instance :(
        //TODO   => implement large VBO with many instances approach (uniform matrix4[instances] posInst; gl_Position = proj*posInst[pos.w]*posAttr;)
        mb.render(mi, env);
        //mb.render(mi);
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
