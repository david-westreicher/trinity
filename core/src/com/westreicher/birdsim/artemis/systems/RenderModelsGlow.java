package com.westreicher.birdsim.artemis.systems;

import com.westreicher.birdsim.artemis.managers.PostProcessingShaders;

/**
 * Created by david on 10/2/15.
 */
public class RenderModelsGlow extends RenderModels {
    @Override
    protected void end() {
        world.getSystem(PostProcessingShaders.class).beginDraw();
        super.end();
        world.getSystem(PostProcessingShaders.class).endDraw();
    }
}
