package com.westreicher.birdsim.artemis.managers;

import com.artemis.Manager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.westreicher.birdsim.Config;
import com.westreicher.birdsim.util.BatchShaderProgram;

/**
 * Created by david on 9/29/15.
 */
public class ShaderManager extends Manager {
    public enum Shaders {
        CHUNK;

        private BatchShaderProgram sp;

        void reload() {
            String vert = "";
            String frag = "";
            switch (this) {
                case CHUNK:
                    vert = getChunkVert();
                    frag = getChunkFrag();
                    break;
            }
            sp = new BatchShaderProgram(vert, frag);
            if (sp.isCompiled() == false)
                throw new IllegalArgumentException("Error compiling shader: " + sp.getLog());
        }
    }

    public BatchShaderProgram getShader(Shaders sh) {
        return sh.sp;
    }

    @Override
    protected void initialize() {
        for (Shaders sh : Shaders.values()) {
            sh.reload();
        }
    }

    @Override
    protected void dispose() {
        for (Shaders sh : Shaders.values()) {
            sh.sp.dispose();
        }
    }

    private static String getChunkVert() {
        return ""//
                + "attribute vec3 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
                + "attribute vec3 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
                + "varying vec3 col;\n" //
                + "uniform mat4 u_projTrans;\n" //
                + "uniform vec3 trans;\n" //
                + (Config.POST_PROCESSING ? ""//
                + "  uniform vec2 virtualcam;\n"//
                //+ "  varying float dstfrac;"
                : "")//
                + "uniform float pointsize;\n" //
                + "uniform float maxdstsqinv;\n" //
                + "uniform float chunksize;\n" //
                + "uniform float heightscale;\n" //
                + "\n" //
                + "void main()\n" //
                + "{\n" //
                + "  vec3 pos = " + ShaderProgram.POSITION_ATTRIBUTE + "*chunksize + trans;\n" //
                + "  pos.z *= heightscale;\n" //
                + (Config.POST_PROCESSING ? ""//
                + "    float dst = length(pos.xy-virtualcam);\n" //
                + "    float dstfrac = (dst*dst*maxdstsqinv);\n" //
                + "    pos.z+=(1.0-dstfrac)*140.0;\n" : "")//
                + "  gl_Position =  u_projTrans * vec4(pos,1.0);\n" //
                + "  gl_PointSize = pointsize;\n"//
                + "  col =  " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
                + "}\n";
    }

    private static String getChunkFrag() {
        return ""//
                + "#ifdef GL_ES\n" //
                + "#define LOWP lowp\n" //
                + "precision mediump float;\n" //
                + "#else\n" //
                + "#define LOWP \n" //
                + "#endif\n" //
                + "varying vec3 col;\n" //
                //+ "varying float dstfrac;\n" //
                + "void main()\n"//
                + "{\n" //
                //+ "  if(dstfrac>1.0)discard;\n" //
                + "  gl_FragColor = vec4(col,1);\n" //
                + "}";
    }
}
