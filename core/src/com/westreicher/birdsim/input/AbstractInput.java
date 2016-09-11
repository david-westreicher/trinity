package com.westreicher.birdsim.input;

import com.artemis.World;
import com.westreicher.birdsim.artemis.components.SlotStateComponent;

/**
 * Created by david on 9/21/15.
 */
public abstract class AbstractInput {

    protected boolean isPaused;
    protected float movx;
    protected float movy;
    protected float shootx;
    protected float shooty;
    protected boolean ismoving;
    protected boolean isshooting;
    protected SlotStateComponent.STATE slot = SlotStateComponent.STATE.BULLET;

    public abstract void update();

    public boolean isPaused() {
        return isPaused;
    }

    public boolean isMoving() {
        return ismoving;
    }

    public boolean isShooting() {
        return isshooting;
    }

    public float getMoveRadiant() {
        return (float) Math.atan2(-movy, movx);
    }

    public float getShootRadiant() {
        return (float) Math.atan2(-shooty, shootx);
    }

    public SlotStateComponent.STATE getSlot() { return slot; }

    public void resize(World w) {
    }
}
