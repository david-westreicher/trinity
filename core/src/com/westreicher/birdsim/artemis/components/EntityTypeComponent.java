package com.westreicher.birdsim.artemis.components;

import com.artemis.PooledComponent;

/**
 * Created by david on 10/1/15.
 */
public class EntityTypeComponent extends PooledComponent {
    public Types type = Types.PLAYER;

    @Override
    protected void reset() {

    }

    public enum Types {PLAYER, BULLET, ITEM, ENEMY}
}
