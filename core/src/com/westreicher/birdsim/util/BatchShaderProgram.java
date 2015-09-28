package com.westreicher.birdsim.util;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * Optimizes the Drawing of many chunks.
 * Because the number of vertex attributes doesn't change we shouldn't enable/disable after every chunk.
 * Created by david on 9/18/15.
 */
public class BatchShaderProgram extends ShaderProgram {
    private boolean shouldbind = false;
    private int savedlocs[] = new int[10];
    private int savedlocspoint = 0;

    public BatchShaderProgram(String vert, String frag) {
        super(vert, frag);
    }

    @Override
    public void enableVertexAttribute(int location) {
        if (shouldbind) {
            savedlocs[savedlocspoint++] = location;
            super.enableVertexAttribute(location);
        }
    }

    @Override
    public void disableVertexAttribute(int location) {
        shouldbind = false;
    }

    public void unbind() {
        for (int i = 0; i < savedlocspoint; i++)
            super.disableVertexAttribute(savedlocs[i]);
    }

    public void bind() {
        shouldbind = true;
        savedlocspoint = 0;
    }

    @Override
    public void enableVertexAttribute(String name) {
        //TODO implement !!!! for older systems
        throw new RuntimeException("TODO IMPLEMENT! BatchShaderProgram");
    }

    @Override
    public void disableVertexAttribute(String name) {
        shouldbind = false;
    }
}
