package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.westreicher.birdsim.artemis.components.AnimationComponent;
import com.westreicher.birdsim.artemis.components.ModelComponent;
import com.westreicher.birdsim.util.ColorAttr;

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
        switch (animation.type) {
            case DEATH:
                model.scale *= 0.92;
                model.col = ColorAttr.WHITE;
                if (model.scale < 0.1)
                    world.delete(entityId);
                break;
            case WHITE:
                model.col = ColorAttr.WHITE;
                if (animation.duration-- <= 0) {
                    mAnimationComponent.remove(entityId);
                    model.col = animation.savedcol;
                }
                break;
        }
    }
}
