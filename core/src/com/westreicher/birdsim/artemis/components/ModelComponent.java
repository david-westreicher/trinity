package com.westreicher.birdsim.artemis.components;

import com.artemis.Component;
import com.artemis.PooledComponent;
import com.westreicher.birdsim.artemis.managers.ModelManager;

/**
 * Created by david on 9/28/15.
 */
public class ModelComponent extends PooledComponent {
    public boolean visible;
    public float scale = 5f;
    public ModelManager.Models type;

    @Override
    protected void reset() {
    }
}
