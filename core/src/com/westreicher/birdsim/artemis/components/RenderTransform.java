package com.westreicher.birdsim.artemis.components;

import com.artemis.PooledComponent;

/**
 * Created by david on 9/28/15.
 */
public class RenderTransform extends PooledComponent {
    public float x;
    public float y;
    public float z;
    public float radiant;

    @Override
    protected void reset() {
    }
}
