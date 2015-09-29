package com.westreicher.birdsim.input;

import com.badlogic.gdx.controllers.Controller;

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
        x = (int) (ctrl.getAxis(axe * 2) * 100);
        y = (int) (ctrl.getAxis(axe * 2 + 1) * 100);
        return (Math.abs(x) > 20 || Math.abs(y) > 20);
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
