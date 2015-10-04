package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.westreicher.birdsim.artemis.components.Position2;
import com.westreicher.birdsim.artemis.components.Speed2;
import com.westreicher.birdsim.artemis.components.StaticText;
import com.westreicher.birdsim.artemis.factories.TextEntity;

/**
 * Created by david on 9/25/15.
 */
@Wire
public class TextRendering extends IteratingSystem {

    private TextEntity te;
    private SpriteBatch spritebatch;
    private BitmapFont font;

    ComponentMapper<Position2> positionMapper;
    ComponentMapper<Speed2> speedMapper;
    ComponentMapper<StaticText> textMapper;
    private float delta;

    public TextRendering() {
        super(Aspect.all(Position2.class, Speed2.class, StaticText.class));
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
            te.position2(x, y).speed2(xspeed, yspeed).staticText("Lives").create();
        }
    }

    @Override
    protected void begin() {
        spritebatch.begin();
        delta = world.getDelta();
    }

    @Override
    protected void process(int e) {
        Position2 pos = positionMapper.get(e);
        Speed2 speed = speedMapper.get(e);
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
