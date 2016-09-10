package com.westreicher.birdsim.input;

import com.artemis.World;
import com.artemis.managers.TagManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.westreicher.birdsim.artemis.Artemis;
import com.westreicher.birdsim.artemis.components.CameraComponent;
import com.westreicher.birdsim.artemis.components.SlotStateComponent;

/**
 * Created by david on 9/29/15.
 */
public class TouchInput extends AbstractInput {

    // position of joysticks
    private float moveAreaX;
    private float moveAreaY;
    private float shootAreaX;
    private float shootAreaY;

    // position of slot buttons
    private float slotBulletAreaX;
    private float slotDroneAreaX;
    private float slotAimAreaX;
    private float slotAreaY;

    // save indexes
    private int moveIndex = -1;
    private int shootIndex = -1;

    private int buttonSize = 50; // TODO: scale to display size

    public TouchInput() {
        Gdx.input.setCatchBackKey(true);
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown (int keycode) {
                if (keycode == Input.Keys.BACK) {
                    isPaused = !isPaused;
                }
                return true;
            }
        });
    }

    @Override
    public void resize(World w) {
        Viewport v = w.getSystem(TagManager.class).getEntity(Artemis.VIRTUAL_CAM_TAG).getComponent(CameraComponent.class).viewport;
        // TODO: make those positions global
        moveAreaX = v.getScreenWidth() * 0.15f;
        moveAreaY = v.getScreenHeight() * 0.7f;
        shootAreaX = v.getScreenWidth() * 0.85f;
        shootAreaY = v.getScreenHeight() * 0.7f;
        slotBulletAreaX = v.getScreenWidth() * 0.85f;
        slotDroneAreaX = v.getScreenWidth() * 0.75f;
        slotAimAreaX = v.getScreenWidth() * 0.65f;
        slotAreaY = v.getScreenWidth() * 0.15f - buttonSize;
    }

    @Override
    public void update() {
        // reset movement and shooting
        movx = 0;
        movy = 0;
        shootx = 0;
        shooty = 0;
        // check buttons
        for (int index = 0; index < 2; index++) {
            if (Gdx.input.isTouched(index)) {
                int currentx = Gdx.input.getX(index);
                int currenty = Gdx.input.getY(index);
                boolean found = checkMoveJoystick(currentx, currenty, index);
                if(!found) found = checkShootJoystick(currentx, currenty, index);
                if(!found) found = checkSlotButtons(currentx, currenty);
            }else{
                // delete saved indexes
                if(index == moveIndex)
                    moveIndex = -1;
                if(index == shootIndex)
                    shootIndex = -1;
            }
        }
        // set state
        isshooting = shootx != 0 || shooty != 0;
        ismoving = movx != 0 || movy != 0;
    }

    private boolean checkMoveJoystick(float x, float y, int index)
    {
        if((index == moveIndex) || (distance(x, y, moveAreaX, moveAreaY) < buttonSize)){
            movx = x - moveAreaX;
            movy = y - moveAreaY;
            moveIndex = index;
            return true;
        }
        return false;
    }

    private boolean checkShootJoystick(float x, float y, int index)
    {
        if((index == shootIndex) || (distance(x, y, shootAreaX, shootAreaY) < buttonSize)){
            shootx = x - shootAreaX;
            shooty = y - shootAreaY;
            shootIndex = index;
            return true;
        }
        return false;
    }

    private boolean checkSlotButtons(float x, float y)
    {
        if(distance(x, y, slotBulletAreaX, slotAreaY) < buttonSize){
            slot = SlotStateComponent.STATE.BULLET;
            return true;
        }
        if(distance(x, y, slotDroneAreaX, slotAreaY) < buttonSize){
            slot = SlotStateComponent.STATE.DRONE;
            return true;
        }
        if(distance(x, y, slotAimAreaX, slotAreaY) < buttonSize){
            slot = SlotStateComponent.STATE.AIM;
            return true;
        }
        return false;
    }

    private float distance(float x1, float y1, float x2, float y2) {
        float xDist = x1 - x2;
        float yDist = y1 - y2;
        return (float) Math.sqrt(xDist * xDist + yDist * yDist);
    }
}
