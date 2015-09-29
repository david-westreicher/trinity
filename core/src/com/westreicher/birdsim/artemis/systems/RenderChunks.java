package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.managers.TagManager;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.westreicher.birdsim.Chunk;
import com.westreicher.birdsim.ChunkManager;
import com.westreicher.birdsim.Config;
import com.westreicher.birdsim.artemis.Artemis;
import com.westreicher.birdsim.artemis.components.CameraComponent;
import com.westreicher.birdsim.artemis.managers.ShaderManager;
import com.westreicher.birdsim.util.BatchShaderProgram;

/**
 * Created by david on 9/28/15.
 */
@Wire
public class RenderChunks extends EntityProcessingSystem {
    private static final float[] tmpfloat = new float[3];
    ComponentMapper<ChunkManager> chunkMapper;

    public RenderChunks() {
        super(Aspect.all(ChunkManager.class));
    }

    @Override
    protected void process(Entity e) {
        Camera cam = world.getManager(TagManager.class).getEntity(Artemis.VIRTUAL_CAM_TAG).getComponent(CameraComponent.class).cam;
        ChunkManager cm = chunkMapper.get(e);
        BatchShaderProgram shader = world.getManager(ShaderManager.class).getShader(ShaderManager.Shaders.CHUNK);

        Gdx.gl20.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl20.glEnable(GL20.GL_VERTEX_PROGRAM_POINT_SIZE);
        shader.begin();
        shader.setUniformMatrix("u_projTrans", cam.combined);
        if (Config.POST_PROCESSING) {
            shader.setUniformf("virtualcam", cam.position.x, cam.position.y);
            shader.setUniformf("maxdstsqinv", 1f / (140f * 140f));
        }
        shader.setUniformf("pointsize", cm.pointsize);
        shader.setUniformf("chunksize", Config.TILES_PER_CHUNK);
        shader.setUniformf("heightscale", 2.5f * Config.TERRAIN_HEIGHT / Config.TILES_PER_CHUNK);
        shader.bind();
        for (int x = 0; x < Config.CHUNKNUMS; x++) {
            for (int y = 0; y < Config.CHUNKNUMS; y++) {
                Chunk mi = cm.chunks[x][y];
                if (mi.shouldDraw) {
                    tmpfloat[0] = (x - (Config.CHUNKNUMS / 2)) * Config.TILES_PER_CHUNK - Config.TILES_PER_CHUNK / 2;
                    tmpfloat[1] = (y - (Config.CHUNKNUMS / 2)) * Config.TILES_PER_CHUNK - Config.TILES_PER_CHUNK / 2;
                    shader.setUniform3fv("trans", tmpfloat, 0, 3);
                    mi.m.render(shader, GL20.GL_POINTS);
                }
            }
        }
        shader.unbind();
        shader.end();
        Gdx.gl20.glDisable(GL20.GL_VERTEX_PROGRAM_POINT_SIZE);
        Gdx.gl20.glDisable(GL20.GL_DEPTH_TEST);
    }
}
