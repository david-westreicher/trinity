package com.westreicher.birdsim.artemis.components;

import com.artemis.Component;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Created by david on 9/28/15.
 */
public class CameraComponent extends Component {
    public Camera cam = new PerspectiveCamera(90, 1, 1);
    public Viewport viewport;

    public CameraComponent() {
        this.viewport = new ScreenViewport(cam);
    }
}
