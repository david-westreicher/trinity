package com.westreicher.birdsim.UI;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.westreicher.birdsim.artemis.Artemis;

/**
 * Created by juanolon on 17/10/15.
 */
public class MenuBuilder {
    Skin skin;
    Table root;
    Stage stage;

    public MenuBuilder(Stage stage){

        root = new Table();
        root.setFillParent(true);

        skin = new Skin();

        // Generate a 1x1 white texture and store it in the skin named "white".
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("white", new Texture(pixmap));

        // TODO define trinity style
        skin.add("default", new BitmapFont());

        // Create a button with the "default" TextButtonStyle. A 3rd parameter can be used to specify a name other than "default".
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = skin.newDrawable("white", Color.DARK_GRAY);
        textButtonStyle.down = skin.newDrawable("white", Color.DARK_GRAY);
        textButtonStyle.checked = skin.newDrawable("white", Color.BLUE);
        textButtonStyle.over = skin.newDrawable("white", Color.LIGHT_GRAY);
        textButtonStyle.font = skin.getFont("default");
        skin.add("default", textButtonStyle);

        if (Artemis.settings.isDebug()) root.setDebug(true);

        stage.addActor(root);
    }

    public MenuBuilder addButton(String text, final MenuAction action, boolean active){
        TextButton button = new TextButton(text, skin);
        button.setChecked(active);

        button.addListener(new ChangeListener() {
            public void changed (ChangeEvent event, Actor actor) {
                action.act();
            }
        });

        root.add(button).width(600).height(60);
        root.row();

        return this;
    }

}
