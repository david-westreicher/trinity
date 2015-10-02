package com.westreicher.birdsim.artemis.components;

import com.artemis.PooledComponent;

/**
 * Created by david on 10/2/15.
 */
public class AnimationComponent extends PooledComponent {
    Types type = Types.DEATH;

    @Override
    protected void reset() {
        type = Types.DEATH;
    }

    public enum Types {DEATH}
}
