package com.westreicher.birdsim.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerAdapter;
import com.badlogic.gdx.controllers.PovDirection;

/**
 * Created by juanolon on 21/09/15.
 */
public class CController extends InputHelper {

    private final int axe;
    private final Controller ctrl;

    private int x;
    private int y;

    public CController(int axe, Controller ctrl) {
        this.axe = axe;
        this.ctrl = ctrl;
    }

    @Override
    public boolean update() {
        int currentx = (int)(ctrl.getAxis(axe*2) * 10);
        int currenty = (int)(ctrl.getAxis(axe*2+1) * 10);
        if (Math.abs(currentx) > 1|| Math.abs(currenty) > 1) {
            x = currentx;
            y = currenty;
            return true;
        }
        return false;
    }

    @Override
    public int relx() {
        return x;
    }

    @Override
    public int rely() {
        return y;
    }
}
