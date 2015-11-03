package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.managers.GroupManager;
import com.artemis.managers.TagManager;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.westreicher.birdsim.SlotSystem;
import com.westreicher.birdsim.artemis.Artemis;
import com.westreicher.birdsim.artemis.components.CameraComponent;
import com.westreicher.birdsim.artemis.components.InputComponent;
import com.westreicher.birdsim.artemis.components.SlotComponent;
import com.westreicher.birdsim.artemis.managers.InputManager;
import com.westreicher.birdsim.artemis.managers.TextureManager;
import com.westreicher.birdsim.input.AbstractInput;

import java.util.ArrayList;

/**
 * Created by juanolon on 21/10/15.
 */
public class GameUI extends IteratingSystem {

    protected ComponentMapper<SlotComponent> mSlot;
    protected ComponentMapper<InputComponent> mInput;
    private SpriteBatch spritebatch;
    private Viewport v;

    public GameUI() {
        super(Aspect.all(SlotComponent.class, InputComponent.class));
    }

    @Override
    protected void begin() {
        spritebatch.begin();
        v = world.getSystem(TagManager.class).getEntity(Artemis.VIRTUAL_CAM_TAG).getComponent(CameraComponent.class).viewport;
    }


    @Override
    protected void dispose() {
        spritebatch.dispose();
    }

    @Override
    protected void end() {
        spritebatch.end();
    }

    @Override
    protected void initialize() {
        this.spritebatch = new SpriteBatch();

    }


    @Override
    protected void process(int e) {
        SlotComponent slot = mSlot.get(e);
        InputComponent input = mInput.get(e);

        renderSpecial(slot.special, input.id);
        renderGun(slot.gunType, input.id);
        renderGunSpecial(slot.gunSpecial, input.id);
    }

    private void renderSpecial(SlotSystem.Slot<SlotSystem.Specialty> special, int index){
//        Texture specialtyTexture = world.getSystem(TextureManager.class).getSpecialtyTexture(special.type);
//        draw(specialtyTexture, 100f, 100f);
//        Gdx.app.log("drawing", "special: " + special.toString());
    }

    private void renderGun(SlotSystem.Slot<SlotSystem.GunType> gunType, int index){
        Texture gunTexture = world.getSystem(TextureManager.class).getGunTexture(gunType.type);
        draw(gunTexture, 200f, 100f, gunType.multiplier);
        if (Gdx.graphics.getFrameId()%100 == 0) Gdx.app.log("drawing", "gun: " + gunType.toString());
    }

    private void renderGunSpecial(SlotSystem.Slot<SlotSystem.GunSpecialty> gunSpecial, int index){
//        Texture gunSpecialityTexture = world.getSystem(TextureManager.class).getGunSpecialityTexture(gunSpecial.type);
//        draw(gunSpecialityTexture, 200f, 100f);
//        Gdx.app.log("drawing", "gun special: " + gunSpecial.toString());
    }

    private void draw(Texture t, float x, float y, int mult) {
        spritebatch.draw(t,
                x - t.getWidth(),
                y - t.getHeight(),
                t.getWidth() * 2,
                t.getHeight() * 2,
                0, 0, 1, 1);
//        spritebatch.
    }
}
