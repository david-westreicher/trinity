package com.westreicher.birdsim.artemis.components;

import com.artemis.PooledComponent;

/**
 * Created by david on 10/5/15.
 */
public class ParticleComponent extends PooledComponent {
    public static final int PARTICLE_NUM = 200;
    public float[] speed = new float[PARTICLE_NUM * 3];
    public float[] pos = new float[PARTICLE_NUM * 3];
    public float[] col = new float[PARTICLE_NUM * 3];
    public int[] lives = new int[PARTICLE_NUM];
    public int freeParticlePointer = 0;

    @Override
    protected void reset() {
    }
}
