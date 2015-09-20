package com.westreicher.birdsim.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.westreicher.birdsim.MyGdxGame;

import java.util.ArrayList;

/**
 * Created by david on 9/6/15.
 */
public class RenderToTexture {

    private final FrameBuffer m_fbo;
    private final TextureRegion m_fboRegion;
    private boolean hasDepth;

    private RenderToTexture(int width, int height, boolean hasDepth) {
        Gdx.app.log("", width + "+" + height);
        this.hasDepth = hasDepth;
        m_fbo = new FrameBuffer(Pixmap.Format.RGB565, width, height, hasDepth);
        Texture colbuf = m_fbo.getColorBufferTexture();
        //colbuf.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        m_fboRegion = new TextureRegion(colbuf);
        m_fboRegion.flip(false, true);
    }

    public void begin() {
        m_fbo.begin();
        if (hasDepth) {
            Gdx.gl.glClearColor(0, 0, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        }
    }

    public void end() {
        m_fbo.end();
    }

    public void draw(SpriteBatch sb, int screenWidth, int screenHeight, boolean flush) {
        sb.draw(m_fboRegion, 0, 0, screenWidth, screenHeight);
        if (flush)
            sb.flush();
    }

    public void dispose() {
        m_fbo.dispose();
        Gdx.app.log("dispose", m_fbo.getWidth() + "+" + m_fbo.getHeight());
    }

    public static class DownSampler {
        private final ShaderProgram gathershader;
        public ArrayList<RenderToTexture> texs = new ArrayList<RenderToTexture>();
        private final SpriteBatch downsampleBatch = new SpriteBatch();

        public static ShaderProgram createDefaultShader() {
            String vertexShader = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
                    + "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
                    + "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
                    + "uniform mat4 u_projTrans;\n" //
                    + "varying vec2 v_texCoords;\n" //
                    + "\n" //
                    + "void main()\n" //
                    + "{\n" //
                    + "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
                    + "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
                    + "}\n";
            String fragmentShader = "#ifdef GL_ES\n" //
                    + "#define LOWP lowp\n" //
                    + "precision mediump float;\n" //
                    + "#else\n" //
                    + "#define LOWP \n" //
                    + "#endif\n" //
                    + "varying vec2 v_texCoords;\n" //
                    + "uniform sampler2D u_texture;\n" //
                    + "uniform int thresh;\n" //
                    + "void main()\n"//
                    + "{\n" //
                    + "  vec4 col = texture2D(u_texture,v_texCoords);\n" //
                    + "  if(thresh<1 && col.r<1.0){gl_FragColor = vec4(0,0,0,0);}\n" //
                    + "  else{gl_FragColor = col;}\n" //
                    + "}";

            ShaderProgram shader = new ShaderProgram(vertexShader, fragmentShader);
            if (shader.isCompiled() == false)
                throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());
            return shader;
        }

        public DownSampler(int width, int height) {
            gathershader = createDefaultShader();
            downsampleBatch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE);
            for (int i = 0; i < (MyGdxGame.isDesktop ? 9 : 3); i++) {
                RenderToTexture rt = new RenderToTexture(width, height, i == 0);
                width /= 2;
                height /= 2;
                texs.add(rt);
            }
        }

        public void dispose() {
            for (RenderToTexture tex : texs)
                tex.dispose();
        }

        public void begin() {
            texs.get(0).begin();
        }

        public void end() {
            texs.get(0).end();
        }

        public void draw(int screenWidth, int screenHeight) {
            downsampleBatch.begin();
            downsampleBatch.disableBlending();
            downsampleBatch.setShader(gathershader);
            for (int i = 1; i < texs.size(); i++) {
                texs.get(i).begin();
                RenderToTexture toDraw = texs.get(i - 1);
                gathershader.setUniformi("thresh", i - 1);
                toDraw.draw(downsampleBatch, screenWidth, screenHeight, true);
            }
            texs.get(0).end();

            downsampleBatch.enableBlending();
            downsampleBatch.setShader(null);
            if (true)
                for (int i = 0; i < texs.size(); i++)
                    texs.get(i).draw(downsampleBatch, screenWidth, screenHeight, false);
            else {
                int tex = ((int) Gdx.graphics.getFrameId() / 100) % texs.size();
                if (Gdx.graphics.getFrameId() % 100 == 1)
                    Gdx.app.log("tex", tex + ", " + texs.get(tex).m_fbo.getWidth() + "x" + texs.get(tex).m_fbo.getHeight());
                texs.get(tex).draw(downsampleBatch, screenWidth, screenHeight, false);
            }
            downsampleBatch.end();
        }
    }

}
