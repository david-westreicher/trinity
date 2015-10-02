package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.westreicher.birdsim.artemis.components.AnimationComponent;
import com.westreicher.birdsim.artemis.components.ModelComponent;

/**
 * Created by david on 10/2/15.
 */
@Wire
public class Animation extends IteratingSystem {
    protected ComponentMapper<AnimationComponent> mAnimationComponent;
    protected ComponentMapper<ModelComponent> mModelComponent;

    public Animation() {
        super(Aspect.all(AnimationComponent.class, ModelComponent.class));
    }

    @Override
    protected void process(int entityId) {
        ModelComponent model = mModelComponent.get(entityId);
        AnimationComponent animation = mAnimationComponent.get(entityId);
        model.scale *= 0.9;
        if (model.scale < 0.1)
            world.delete(entityId);
    }
}
