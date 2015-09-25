package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.westreicher.birdsim.artemis.components.Position2;
import com.westreicher.birdsim.artemis.components.StaticText;

/**
 * Created by david on 9/25/15.
 */
@Wire
public class TextRendering extends EntityProcessingSystem {

    private com.westreicher.birdsim.artemis.factories.TextEntity te;
    private SpriteBatch spritebatch;
    private BitmapFont font;

    ComponentMapper<Position2> positionMapper;
    ComponentMapper<StaticText> textMapper;

    public TextRendering() {
        super(Aspect.all(Position2.class, StaticText.class));
    }

    @Override
    protected void initialize() {
        spritebatch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.RED);
        for (int i = 0; i < 100; i++) {
            float x = (float) Math.random() * 1200;
            float y = (float) Math.random() * 700;
            te.position2(x, y).staticText("Lives").create();
        }
    }

    @Override
    protected void begin() {
        spritebatch.begin();
    }

    @Override
    protected void process(Entity e) {
        Position2 pos = positionMapper.get(e);
        font.draw(spritebatch, textMapper.get(e).text, pos.x, pos.y);
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
