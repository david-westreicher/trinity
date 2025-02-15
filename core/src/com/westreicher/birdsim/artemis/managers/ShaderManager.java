package com.westreicher.birdsim.artemis.managers;

import com.artemis.Manager;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.westreicher.birdsim.Config;
import com.westreicher.birdsim.util.BatchShaderProgram;

/**
 * Created by david on 9/29/15.
 */
public class ShaderManager extends Manager {
    public enum Shaders {
        CHUNKS, CHUNK_SPRITES;

        private BatchShaderProgram sp;

        void reload() {
            String vert = "";
            String frag = "";
            switch (this) {
                case CHUNK_SPRITES:
                    vert = getChunkVert(true);
                    frag = getChunkFrag();
                    break;
                case CHUNKS:
                    vert = getChunkVert(false);
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

    private static String getChunkVert(boolean pointsprites) {
        return ""//
                + ((Config.ALLOW_NEGATIVE_CHUNK_DATA) ?
                "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" :
                "attribute vec3 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n") //
                + "attribute vec3 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
                + "varying vec3 col;\n" //
                + "uniform mat4 u_projTrans;\n" //
                + "uniform vec3 trans;\n" //
                + (Config.POST_PROCESSING ? ""//
                + "  uniform vec2 virtualcam;\n" : "")//
                + (pointsprites ? "uniform float pointsize;\n" : "") //
                + "uniform float chunksize;\n" //
                + "uniform float heightscale;\n" //
                + "\n" //
                + "void main()\n" //
                + "{\n" //
                + "  vec3 pos = " + ShaderProgram.POSITION_ATTRIBUTE + ".xyz*chunksize + trans;\n" //
                + ((!Config.ALLOW_NEGATIVE_CHUNK_DATA) ? "" : ""
                + "  pos.z -= " + ShaderProgram.POSITION_ATTRIBUTE + ".w*chunksize;\n") //
                + "  pos.z *= heightscale;\n" //
                + "  float dotproduct = 1.0;\n" //
                + (Config.POST_PROCESSING ? ""//
                + "    vec2 dst = pos.xy-virtualcam;\n" //
                + "    float dist = " + Config.SPHERE_RADIUS_SQUARED + "-dst.x*dst.x-dst.y*dst.y;\n"//
                + "    if(dist>0.0){\n" //
                + "      float denom = sqrt(dist);\n"//
                + "      pos.z+=denom;\n"//
                + "      float denominv = 1.0/denom;\n"// take the dot product of normal (dst.x/denom,dst.y/denom,-(1/140)*denom) and lightvec(-1,1,-1)
                + "      dotproduct = (-dst.x+dst.y)*denominv+denom*" + (1.0 / Config.SPHERE_RADIUS) + ";\n"//
                + "    }else pos.z = 0.0;\n" : "")//
                + "  col =  " + ShaderProgram.COLOR_ATTRIBUTE + "*dotproduct;\n" //
                + "  gl_Position =  u_projTrans * vec4(pos,1.0);\n" //
                + (pointsprites ? "gl_PointSize = pointsize;\n" : "") //
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
                + "void main()\n"//
                + "{\n" //
                //+ "  if(dstfrac>1.0)discard;\n" //
                + "  gl_FragColor = vec4(col,1);\n" //
                + "}";
    }
}
