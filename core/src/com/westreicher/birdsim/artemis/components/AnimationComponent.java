package com.westreicher.birdsim.artemis.components;

import com.artemis.PooledComponent;
import com.westreicher.birdsim.util.ColorAttr;

/**
 * Created by david on 10/2/15.
 */
public class AnimationComponent extends PooledComponent {
    public Types type = Types.DEATH;
    public int duration = 0;
    public ColorAttr savedcol = ColorAttr.BLUE;

    @Override
    protected void reset() {
        type = Types.DEATH;
    }

    public enum Types {WHITE, DEATH}
}
