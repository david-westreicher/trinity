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

/**
 * Created by david on 9/29/15.
 */
public class TouchInput extends AbstractInput {

    private float startMovX;
    private float startMovY;
    private float startShootX;
    private float startShootY;


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
    public void update() {
        // joysticks
        movx = 0;
        movy = 0;
        shootx = 0;
        shooty = 0;
        for (int index = 0; index < 2; index++) {
            if (Gdx.input.isTouched(index)) {
                int currentx = Gdx.input.getX(index);
                int currenty = Gdx.input.getY(index);
                boolean isAMover = distance(currentx, currenty, startMovX, startMovY) < distance(currentx, currenty, startShootX, startShootY);
                if (isAMover) {
                    movx = currentx - startMovX;
                    movy = currenty - startMovY;
                } else {
                    shootx = currentx - startShootX;
                    shooty = currenty - startShootY;
                }
            }
        }
        isshooting = shootx != 0 || shooty != 0;
        ismoving = movx != 0 || movy != 0;

        // slot buttons
        
    }

    private float distance(float x1, float y1, float x2, float y2) {
        float xDist = x1 - x2;
        float yDist = y1 - y2;
        return (float) Math.sqrt(xDist * xDist + yDist * yDist);
    }

    @Override
    public void resize(World w) {
        Viewport v = w.getSystem(TagManager.class).getEntity(Artemis.VIRTUAL_CAM_TAG).getComponent(CameraComponent.class).viewport;
        startMovX = v.getScreenWidth() * 0.15f;
        startMovY = v.getScreenHeight() * 0.7f;
        startShootX = v.getScreenWidth() * 0.85f;
        startShootY = v.getScreenHeight() * 0.7f;
    }

}
