package com.westreicher.birdsim.util;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by david on 9/14/15.
 */
public class ManagedRessources {
    private static ManagedRessources singl;

    public static void init() {
        if (singl == null)
            singl = new ManagedRessources();
        else
            throw new RuntimeException("whaat");
    }

    private HashMap<Shaders, ShaderProgram> shadermap = new HashMap<Shaders, ShaderProgram>();

    public static ShaderProgram getShader(Shaders chunk) {
        return singl.getShaderProgram(chunk);
    }

    public enum Shaders {CHUNK}

    ;

    public ShaderProgram getShaderProgram(Shaders s) {
        if (shadermap.get(s) == null) {
            String vert = "";
            String frag = "";
            switch (s) {
                case CHUNK:
                    vert = getChunkVert();
                    frag = getChunkFrag();
                    break;
            }
            ShaderProgram sp = new ShaderProgram(vert, frag);
            if (sp.isCompiled() == false)
                throw new IllegalArgumentException("Error compiling shader: " + sp.getLog());
            shadermap.put(s, sp);
        }
        return shadermap.get(s);
    }

    private String getChunkVert() {
        return ""//
                + "attribute vec3 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
                + "attribute vec3 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
                + "varying vec3 col;\n" //
                + "uniform mat4 u_projTrans;\n" //
                + "uniform vec3 trans;\n" //
                + "\n" //
                + "void main()\n" //
                + "{\n" //
                + "   gl_Position =  u_projTrans * vec4(" + ShaderProgram.POSITION_ATTRIBUTE + "+trans,1);\n" //
                + "   col =  " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
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
                + "void main()\n"//
                + "{\n" //
                + "  gl_FragColor = vec4(col,1);\n" //
                + "}";
    }

    public void dispose() {
        for (Map.Entry<Shaders, ShaderProgram> entries : shadermap.entrySet()) {
            entries.getValue().dispose();
        }
        shadermap.clear();
    }
}
