package com.westreicher.birdsim.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.westreicher.birdsim.Config;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by david on 9/14/15.
 */
public class ManagedRessources {

    private static ManagedRessources singl;

    public static void init() {
        singl = new ManagedRessources();
    }

    private HashMap<Shaders, BatchShaderProgram> shadermap = new HashMap<Shaders, BatchShaderProgram>();

    public static BatchShaderProgram getShader(Shaders chunk) {
        return singl.getShaderProgram(chunk);
    }

    public enum Shaders {CHUNK}

    ;

    public BatchShaderProgram getShaderProgram(Shaders s) {
        if (shadermap.get(s) == null) {
            String vert = "";
            String frag = "";
            switch (s) {
                case CHUNK:
                    vert = getChunkVert();
                    frag = getChunkFrag();
                    //logShader(vert, frag);
                    break;
            }
            BatchShaderProgram sp = new BatchShaderProgram(vert, frag);
            if (sp.isCompiled() == false)
                throw new IllegalArgumentException("Error compiling shader: " + sp.getLog());
            shadermap.put(s, sp);
        }
        return shadermap.get(s);
    }

    private void logShader(String vert, String frag) {
        Gdx.app.log("shader", "#########vert##########");
        int linenum = 0;
        for (String line : vert.split("\n")) {
            linenum++;
            Gdx.app.log("shader", linenum + ":\t" + line);
        }
    }

    private String getChunkVert() {
        return ""//
                + "attribute vec3 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
                + "attribute vec3 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
                + "varying vec3 col;\n" //
                + "uniform mat4 u_projTrans;\n" //
                + "uniform vec3 trans;\n" //
                + (Config.POST_PROCESSING ? ""//
                + "  uniform vec3 virtualcam;\n"//
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
                + "    float dst = length(pos.xy-virtualcam.xy);\n" //
                + "    float dstfrac = (dst*dst*maxdstsqinv);\n" //
                + "    pos.z+=(1.0-dstfrac)*140.0;\n" : "")//
                + "  gl_Position =  u_projTrans * vec4(pos,1.0);\n" //
                + "  gl_PointSize = pointsize;\n"//
                + "  col =  " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
                + "}\n";
    }

    private String getChunkFrag() {
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

    public static void dispose() {
        if (singl == null)
            return;
        for (Map.Entry<Shaders, BatchShaderProgram> entries : singl.shadermap.entrySet()) {
            entries.getValue().dispose();
        }
        singl.shadermap.clear();
    }
}
