package com.westreicher.birdsim.artemis.components;

import com.artemis.PooledComponent;

/**
 * Created by david on 9/28/15.
 */
public class RenderTransformComponent extends PooledComponent {
    public float x;
    public float y;
    public float z;
    public float radiant;
    public float dist;

    @Override
    protected void reset() {
    }
}
