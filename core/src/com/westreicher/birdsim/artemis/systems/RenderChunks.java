package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.managers.TagManager;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.westreicher.birdsim.ChunkManager;
import com.westreicher.birdsim.artemis.Artemis;
import com.westreicher.birdsim.artemis.components.CameraComponent;
import com.westreicher.birdsim.artemis.managers.ShaderManager;
import com.westreicher.birdsim.util.BatchShaderProgram;

/**
 * Created by david on 9/28/15.
 */
@Wire
public class RenderChunks extends EntityProcessingSystem {
    ComponentMapper<ChunkManager> chunkMapper;

    public RenderChunks() {
        super(Aspect.all(ChunkManager.class));
    }

    @Override
    protected void process(Entity e) {
        Camera cam = world.getManager(TagManager.class).getEntity(Artemis.VIRTUAL_CAM_TAG).getComponent(CameraComponent.class).cam;
        ChunkManager cm = chunkMapper.get(e);
        BatchShaderProgram shader = world.getManager(ShaderManager.class).getShader(ShaderManager.Shaders.CHUNK);
        cm.render(cam, shader);
    }
}
