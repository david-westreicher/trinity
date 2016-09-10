package com.westreicher.birdsim.artemis.components;

/**
 * Created by juanolon on 9/10/16.
 */
public class SlotStateComponent {
     public enum STATE {BULLET, DRONE, AIM}

     public STATE state = STATE.BULLET;

     protected void reset() {
          state = STATE.BULLET;
     }
}
