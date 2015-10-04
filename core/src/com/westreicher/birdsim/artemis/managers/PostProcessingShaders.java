package com.westreicher.birdsim.artemis.managers;

import com.artemis.BaseSystem;
import com.westreicher.birdsim.util.RenderToTexture;

/**
 * Created by david on 10/2/15.
 */
public class PostProcessingShaders extends BaseSystem {
    private RenderToTexture.DownSampler down;
    private int width;
    private int height;

    @Override
    protected void processSystem() {
    }

    public void beginDraw() {
        down.begin();
    }

    public void endDraw() {
        down.end();
        down.draw(width, height);
    }


    public void resize(int width, int height) {
        if (down != null)
            down.dispose();
        down = new RenderToTexture.DownSampler(width, height);
        this.width = width;
        this.height = height;
    }

    @Override
    protected void dispose() {
        if (down != null) down.dispose();
    }
}
