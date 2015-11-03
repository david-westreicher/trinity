package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.managers.TagManager;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.westreicher.birdsim.ChunkManager;
import com.westreicher.birdsim.Config;
import com.westreicher.birdsim.artemis.Artemis;
import com.westreicher.birdsim.artemis.components.CameraComponent;
import com.westreicher.birdsim.artemis.components.ParticleComponent;
import com.westreicher.birdsim.artemis.factories.UberFactory;
import com.westreicher.birdsim.artemis.managers.ShaderManager;
import com.westreicher.birdsim.util.BatchShaderProgram;
import com.westreicher.birdsim.util.MaxArray;

/**
 * Created by david on 10/5/15.
 */
public class RenderParticles extends IteratingSystem {

    private static final float[] TMP_FLOAT = new float[]{0, 0, 0};
    private MaxArray.MaxArrayFloat verts;
    private Mesh particleMesh;
    protected ComponentMapper<ParticleComponent> mParticleComponent;
    private BatchShaderProgram shader;

    public RenderParticles() {
        super(Aspect.all(ParticleComponent.class));
    }

    @Override
    protected void initialize() {
        verts = new MaxArray.MaxArrayFloat(ParticleComponent.PARTICLE_NUM * 4);
        particleMesh = new Mesh(false, verts.maxSize(), 0,
                new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 3, ShaderProgram.COLOR_ATTRIBUTE));
        world.getSystem(UberFactory.class).createParticleSystem(world);
    }

    @Override
    protected void process(int e) {
        float delta = world.getDelta();
        ParticleComponent parts = mParticleComponent.get(e);
        int particlenum = 0;
        verts.reset();
        for (int i = 0; i < ParticleComponent.PARTICLE_NUM; i++) {
            if (parts.lives[i] <= 0)
                continue;
            int ix = i * 3 + 0;
            int iy = i * 3 + 1;
            int iz = i * 3 + 2;
            float posx = parts.pos[ix] + parts.speed[ix] * delta;
            float posy = parts.pos[iy] + parts.speed[iy] * delta;
            float posz = parts.pos[iz] + parts.speed[iz] * delta;
            verts.add(posx, posy, posz);
            verts.add(Color.toFloatBits(parts.col[ix], parts.col[iy], parts.col[iz], 1));
            particlenum++;
        }
        particleMesh.setVertices(verts.arr, 0, particlenum * 4);
        if (particlenum > 0)
            draw();
    }

    private void draw() {
        ChunkManager cm = world.getSystem(TagManager.class).getEntity(Artemis.CHUNKMANAGER_TAG).getComponent(ChunkManager.class);
        Camera cam = world.getSystem(TagManager.class).getEntity(Artemis.VIRTUAL_CAM_TAG).getComponent(CameraComponent.class).cam;
        shader = world.getSystem(ShaderManager.class).getShader(ShaderManager.Shaders.CHUNK_SPRITES);

        Gdx.gl20.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl20.glEnable(GL20.GL_VERTEX_PROGRAM_POINT_SIZE);
        Gdx.gl20.glDepthFunc(GL20.GL_LEQUAL);
        shader.begin();
        shader.setUniformMatrix("u_projTrans", cam.combined);
        if (Config.POST_PROCESSING)
            shader.setUniformf("virtualcam", cam.position.x, cam.position.y);
        shader.setUniformf("chunksize", 1);
        shader.setUniform3fv("trans", TMP_FLOAT, 0, 3);
        shader.setUniformf("pointsize", cm.pointsize);
        shader.setUniformf("heightscale", 1);
        shader.bind();
        particleMesh.render(shader, GL20.GL_POINTS);
        shader.unbind();
        shader.end();
        Gdx.gl20.glDepthFunc(GL20.GL_LESS);
        Gdx.gl20.glDisable(GL20.GL_VERTEX_PROGRAM_POINT_SIZE);
        Gdx.gl20.glDisable(GL20.GL_DEPTH_TEST);
    }

    @Override
    protected void dispose() {
        particleMesh.dispose();
    }
}
