package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.westreicher.birdsim.artemis.components.Position2Component;
import com.westreicher.birdsim.artemis.components.Speed2Component;
import com.westreicher.birdsim.artemis.components.StaticTextComponent;
import com.westreicher.birdsim.artemis.factories.TextEntity;

/**
 * Created by david on 9/25/15.
 */
@Wire
public class TextRendering extends IteratingSystem {

    private TextEntity te;
    private SpriteBatch spritebatch;
    private BitmapFont font;

    ComponentMapper<Position2Component> positionMapper;
    ComponentMapper<Speed2Component> speedMapper;
    ComponentMapper<StaticTextComponent> textMapper;
    private float delta;

    public TextRendering() {
        super(Aspect.all(Position2Component.class, Speed2Component.class, StaticTextComponent.class));
    }

    @Override
    protected void initialize() {
        spritebatch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.RED);
        for (int i = 0; i < 100; i++) {
            float x = (float) Math.random() * 1200;
            float y = (float) Math.random() * 700;
            float xspeed = ((float) Math.random() - 0.5f);
            float yspeed = ((float) Math.random() - 0.5f);
            te.position2Component(x, y).speed2Component(xspeed, yspeed).staticTextComponent("Lives").create();
        }
    }

    @Override
    protected void begin() {
        spritebatch.begin();
        delta = world.getDelta();
    }

    @Override
    protected void process(int e) {
        Position2Component pos = positionMapper.get(e);
        Speed2Component speed = speedMapper.get(e);
        float drawx = pos.x + speed.x * delta;
        float drawy = pos.y + speed.y * delta;
        font.draw(spritebatch, textMapper.get(e).text, drawx, drawy);
    }

    @Override
    protected void end() {
        spritebatch.end();
    }


    @Override
    protected void dispose() {
        font.dispose();
        spritebatch.dispose();
    }
}
