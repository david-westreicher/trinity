package com.westreicher.birdsim.util;

import com.badlogic.gdx.math.Vector3;

/**
 * Created by david on 9/22/15.
 */
public class InterpVec3 {
    public static final Vector3 TMP_VEC = new Vector3();
    public Vector3 oldpos = new Vector3();
    public Vector3 pos = new Vector3();
    public Vector3 interppos = new Vector3();

    public Vector3 getInterppos(float interp) {
        interppos.set(oldpos);
        TMP_VEC.set(pos).sub(oldpos).scl(interp);
        interppos.add(TMP_VEC);
        return interppos;
    }

    public void resetOldPos() {
        oldpos.set(pos);
    }
}
