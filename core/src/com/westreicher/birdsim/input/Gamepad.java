package com.westreicher.birdsim.input;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.controllers.mappings.Ouya;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by david on 9/30/15.
 */
public class Gamepad extends InputHelper {

    private final Controller ctrl;

    public Gamepad(Controller ctrl) {
        this.ctrl = ctrl;
        ctrl.addListener(new ControllerListener() {
            @Override
            public void connected(Controller controller) {
                Gdx.app.log("controller", "connected");
            }

            @Override
            public void disconnected(Controller controller) {
                Gdx.app.log("controller", "disconnected");
            }

            @Override
            public boolean buttonDown(Controller controller, int buttonCode) {
                //TODO hacky: logitech start button has id 9
                if (buttonCode == 9 || buttonCode == Ouya.BUTTON_MENU)
                    isPaused = !isPaused;
                return false;
            }

            @Override
            public boolean buttonUp(Controller controller, int buttonCode) {
                return false;
            }

            @Override
            public boolean axisMoved(Controller controller, int axisCode, float value) {
                return false;
            }

            @Override
            public boolean povMoved(Controller controller, int povCode, PovDirection value) {
                return false;
            }

            @Override
            public boolean xSliderMoved(Controller controller, int sliderCode, boolean value) {
                return false;
            }

            @Override
            public boolean ySliderMoved(Controller controller, int sliderCode, boolean value) {
                return false;
            }

            @Override
            public boolean accelerometerMoved(Controller controller, int accelerometerCode, Vector3 value) {
                return false;
            }
        });
    }

    @Override
    public void update() {
        int axe = 0;
        movx = (int) (ctrl.getAxis(axe * 2) * 100);
        movy = (int) (ctrl.getAxis(axe * 2 + 1) * 100);
        ismoving = Math.abs(movx) > 20 || Math.abs(movy) > 20;
        axe = 1;
        shootx = (int) (ctrl.getAxis(axe * 2) * 100);
        shooty = (int) (ctrl.getAxis(axe * 2 + 1) * 100);
        isshooting = Math.abs(shootx) > 20 || Math.abs(shooty) > 20;
    }

}
