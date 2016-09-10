package com.westreicher.birdsim.artemis.components;

import com.artemis.Component;

/**
 * Created by juanolon on 9/10/16.
 */
public class SlotStateComponent extends Component {
     public enum STATE {BULLET, DRONE, AIM}

     public STATE state = STATE.BULLET;

     protected void reset() {
          state = STATE.BULLET;
     }
}
