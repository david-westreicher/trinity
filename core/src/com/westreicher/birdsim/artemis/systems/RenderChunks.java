package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
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
import com.badlogic.gdx.math.Vector2;
import com.westreicher.birdsim.Chunk;
import com.westreicher.birdsim.ChunkManager;
import com.westreicher.birdsim.Config;
import com.westreicher.birdsim.artemis.Artemis;
import com.westreicher.birdsim.artemis.components.CameraComponent;
import com.westreicher.birdsim.artemis.components.ModelComponent;
import com.westreicher.birdsim.artemis.components.RenderTransform;
import com.westreicher.birdsim.artemis.managers.ShaderManager;
import com.westreicher.birdsim.util.BatchShaderProgram;
import com.westreicher.birdsim.util.MaxArray;
import com.westreicher.birdsim.util.Spiral;

/**
 * Created by david on 9/28/15.
 */
@Wire
public class RenderChunks extends IteratingSystem {
    private static final float[] tmpfloat = new float[3];
    private static final Color TMP_COL = new Color();
    ComponentMapper<RenderTransform> transformMapper;
    ComponentMapper<ModelComponent> modelMapper;
    private Mesh shadowMesh;
    private MaxArray.MaxArrayFloat verts;
    private BatchShaderProgram shader;
    private ChunkManager cm;
    private Spiral spiral;


    public RenderChunks() {
        super(Aspect.all(RenderTransform.class, ModelComponent.class));
    }

    @Override
    protected void initialize() {
        verts = new MaxArray.MaxArrayFloat(800 * (3 + 1));
        shadowMesh = new Mesh(Mesh.VertexDataType.VertexBufferObjectSubData, false, verts.maxSize(), 0,
                new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 3, ShaderProgram.COLOR_ATTRIBUTE));
        spiral = new Spiral();
    }

    @Override
    protected void begin() {
        Camera cam = world.getSystem(TagManager.class).getEntity(Artemis.VIRTUAL_CAM_TAG).getComponent(CameraComponent.class).cam;
        cm = world.getSystem(TagManager.class).getEntity(Artemis.CHUNKMANAGER_TAG).getComponent(ChunkManager.class);
        shader = world.getSystem(ShaderManager.class).getShader(
                Config.CHUNK_RENDER_STYLE == Chunk.Renderstyle.SPRITE ?
                        ShaderManager.Shaders.CHUNK_SPRITES :
                        ShaderManager.Shaders.CHUNKS);

        Gdx.gl20.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl20.glEnable(GL20.GL_VERTEX_PROGRAM_POINT_SIZE);
        shader.begin();
        shader.setUniformMatrix("u_projTrans", cam.combined);
        if (Config.POST_PROCESSING)
            shader.setUniformf("virtualcam", cam.position.x, cam.position.y);
        if (Config.CHUNK_RENDER_STYLE == Chunk.Renderstyle.SPRITE)
            shader.setUniformf("pointsize", cm.pointsize);
        shader.setUniformf("chunksize", Config.TILES_PER_CHUNK);
        shader.setUniformf("heightscale", 2.5f * Config.TERRAIN_HEIGHT / Config.TILES_PER_CHUNK);
        shader.bind();
        spiral.reset();
        while (true) {
            Vector2 spos = spiral.next();
            int x = ((int) spos.x) + Config.CHUNKNUMS / 2;
            int y = ((int) spos.y) + Config.CHUNKNUMS / 2;
            if (Math.max(Math.abs(spos.x), Math.abs(spos.y)) > Config.CHUNK_DRAW_DISTANCE)
                break;
            Chunk mi = cm.chunks[x][y];
            if (mi.shouldDraw()) {
                tmpfloat[0] = (x - (Config.CHUNKNUMS / 2)) * Config.TILES_PER_CHUNK - Config.TILES_PER_CHUNK / 2;
                tmpfloat[1] = (y - (Config.CHUNKNUMS / 2)) * Config.TILES_PER_CHUNK - Config.TILES_PER_CHUNK / 2;
                shader.setUniform3fv("trans", tmpfloat, 0, 3);
                mi.m.render(shader, mi.renderStyle.getType());
            }
        }
        verts.reset();
    }

    @Override
    protected void process(int e) {
        if (!Config.DRAW_SHADOWS) return;
        RenderTransform rt = transformMapper.get(e);
        ModelComponent model = modelMapper.get(e);
        int scale = Math.max(1, (int) (model.scale / 2));
        float scaleSq = (scale - 1) * (scale - 1);
        for (int x = -scale; x <= scale; x++) {
            for (int y = -scale; y <= scale; y++) {
                if (verts.size() > verts.maxSize() - 4)
                    return;
                float realX = (int) (rt.x + x) + 2f;
                float realY = (int) (rt.y + y) - 2f;
                ChunkManager.TileResult tr = cm.setTileResult(realX, realY);
                if (tr == null)
                    continue;
                float dist = scaleSq - x * x - y * y;
                if (dist <= 0)
                    continue;
                float z = Math.max(0, tr.c.getVal(tr.innerx, tr.innery)) * Config.TERRAIN_HEIGHT / Config.TILES_PER_CHUNK + 0.02f;
                verts.add(realX / Config.TILES_PER_CHUNK, realY / Config.TILES_PER_CHUNK, z);
                TMP_COL.set(tr.c.colors[tr.innerx][tr.innery]);
                float asd = 1 - (float) (Math.sqrt(dist) / (scale * 2));
                TMP_COL.mul(asd);
                verts.add(Color.toFloatBits(TMP_COL.r, TMP_COL.g, TMP_COL.b, 1));
                //verts.add(Color.toFloatBits(0.2f, 0.2f, 0.2f, 1));
            }
        }
    }

    @Override
    protected void end() {
        if (Config.DRAW_SHADOWS) drawShadows();
        shader.unbind();
        shader.end();
        Gdx.gl20.glDisable(GL20.GL_VERTEX_PROGRAM_POINT_SIZE);
        Gdx.gl20.glDisable(GL20.GL_DEPTH_TEST);
    }

    private void drawShadows() {
        shader = world.getSystem(ShaderManager.class).getShader(ShaderManager.Shaders.CHUNK_SPRITES);
        Gdx.gl20.glDepthFunc(GL20.GL_LEQUAL);
        shadowMesh.setVertices(verts.arr, 0, verts.size());
        tmpfloat[0] = 0;
        tmpfloat[1] = 0;
        shader.setUniform3fv("trans", tmpfloat, 0, 3);
        shader.setUniformf("pointsize", cm.pointsize);
        shader.setUniformf("heightscale", 1);
        shadowMesh.render(shader, GL20.GL_POINTS);
        Gdx.gl20.glDepthFunc(GL20.GL_LESS);
    }
}
