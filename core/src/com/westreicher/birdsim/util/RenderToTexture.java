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

import javax.xml.soap.Text;

/**
 * Created by david on 9/6/15.
 */
public class RenderToTexture {

    private final FrameBuffer m_fbo;
    private final TextureRegion m_fboRegion;

    private RenderToTexture(int width, int height) {
        this(width, height, false);
    }

    private RenderToTexture(int width, int height, boolean hasDepth) {
        Gdx.app.log("", width + "+" + height);
        m_fbo = new FrameBuffer(Pixmap.Format.RGB888, width, height, hasDepth);
        Texture colbuf = m_fbo.getColorBufferTexture();
        //colbuf.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        m_fboRegion = new TextureRegion(colbuf);
        m_fboRegion.flip(false, true);
    }

    public void begin() {
        //Gdx.app.log("bind", m_fbo.getFramebufferHandle() + "");
        m_fbo.begin();
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
        private final int texSizeLoc;
        private final float[] texSizeVal = new float[2];
        private final ShaderProgram gathershader;
        public ArrayList<RenderToTexture> texs = new ArrayList<RenderToTexture>();
        private final SpriteBatch downsampleBatch = new SpriteBatch();

        public static ShaderProgram createDefaultShader() {
            String vertexShader = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
                    + "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
                    + "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
                    + "uniform mat4 u_projTrans;\n" //
                    + "varying vec4 v_color;\n" //
                    + "varying vec2 v_texCoords;\n" //
                    + "\n" //
                    + "void main()\n" //
                    + "{\n" //
                    + "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
                    + "   v_color.a = v_color.a * (255.0/254.0);\n" //
                    + "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
                    + "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
                    + "}\n";
            String fragmentShader = "#ifdef GL_ES\n" //
                    + "#define LOWP lowp\n" //
                    + "precision mediump float;\n" //
                    + "#else\n" //
                    + "#define LOWP \n" //
                    + "#endif\n" //
                    + "varying LOWP vec4 v_color;\n" //
                    + "varying vec2 v_texCoords;\n" //
                    + "uniform sampler2D u_texture;\n" //
                    + "uniform vec2 texSize;\n" //
                    + "uniform int thresh;\n" //
                    + "void main()\n"//
                    + "{\n" //
                    + "  vec4 col = vec4(0,0,0,0);" //
                    + "  col+=texture2D(u_texture,v_texCoords+vec2(-1,-1)/texSize);" //
                    + "  col+=texture2D(u_texture,v_texCoords+vec2(0,-1)/texSize);" //
                    + "  col+=texture2D(u_texture,v_texCoords+vec2(1,-1)/texSize);" //
                    + "  col+=texture2D(u_texture,v_texCoords+vec2(1,0)/texSize);" //
                    + "  col+=texture2D(u_texture,v_texCoords+vec2(1,1)/texSize);" //
                    + "  col+=texture2D(u_texture,v_texCoords+vec2(0,1)/texSize);" //
                    + "  col+=texture2D(u_texture,v_texCoords+vec2(-1,1)/texSize);" //
                    + "  col+=texture2D(u_texture,v_texCoords+vec2(-1,0)/texSize);" //
                    + "  col+=texture2D(u_texture,v_texCoords);" //
                    + "  col/=vec4(9);" //
                    + "  if(thresh<1 && length(col)<1.3)col=vec4(0,0,0,0);" //
                    + "  gl_FragColor = v_color * col ;\n" //
                    + "}";

            ShaderProgram shader = new ShaderProgram(vertexShader, fragmentShader);
            if (shader.isCompiled() == false)
                throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());
            return shader;
        }

        public DownSampler(int width, int height) {
            gathershader = createDefaultShader();
            texSizeLoc = downsampleBatch.getShader().getUniformLocation("texSize");
            downsampleBatch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE);
            while (width > 2 && height > 2) {
                RenderToTexture rt = new RenderToTexture(width, height, false);
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
                texSizeVal[0] = toDraw.m_fbo.getWidth();
                texSizeVal[1] = toDraw.m_fbo.getHeight();
                //texSizeVal[0] = screenWidth;
                //texSizeVal[1] = screenHeight;
                gathershader.setUniform2fv("texSize", texSizeVal, 0, 2);
                gathershader.setUniformi("thresh", i == 1 ? 0 : 1);
                toDraw.draw(downsampleBatch, screenWidth, screenHeight, true);
            }
            texs.get(0).end();

            downsampleBatch.enableBlending();
            downsampleBatch.setShader(null);
            if (true)
                for (RenderToTexture tex : texs)
                    tex.draw(downsampleBatch, screenWidth, screenHeight, false);
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
