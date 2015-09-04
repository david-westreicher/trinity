package com.westreicher.birdsim;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;

public class Bullet implements Pool.Poolable {

    public final ModelInstance modelInstance;
    public Vector3 position;
    public Vector3 speed;
    private static final Model bullet = new ModelBuilder().createBox(0.4f, 0.4f, 0.4f, new Material(ColorAttribute.createDiffuse(1, 0, 0, 0)), VertexAttributes.Usage.Position);

    /**
     * Bullet constructor. Just initialize variables.
     */
    public Bullet() {
        this.position = new Vector3();
        this.speed = new Vector3();
        this.modelInstance = new ModelInstance(bullet);
    }

    /**
     * Initialize the bullet. Call this method after getting a bullet from the pool.
     */
    public void init(float posX, float posY, float speedx, float speedy, float degree) {
        position.x = posX;
        position.y = posY;
        speed.x = speedx;
        speed.y = speedy;
        modelInstance.transform.setToRotation(MyGdxGame.UPAXIS, degree);
        modelInstance.transform.setTranslation(position);
    }

    /**
     * Callback method when the object is freed. It is automatically called by Pool.free()
     * Must reset every meaningful field of this bullet.
     */
    @Override
    public void reset() {
    }

    /**
     * Method called each frame, which updates the bullet.
     */
    public void update(float delta) {
        position.x += speed.x;
        position.y += speed.y;
        // Gdx.app.log("bullet", position.toString());
        modelInstance.transform.setTranslation(position);
    }
}