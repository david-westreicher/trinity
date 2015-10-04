package com.westreicher.birdsim.artemis.managers;

import com.artemis.Manager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

/**
 * Created by david on 9/28/15.
 */
public class ModelManager extends Manager {
    public enum Models {
        PLAYER("player.obj"), ITEM(), BULLET("rocket.obj");

        private final String file;
        public ModelInstance modelinst;
        public Model m;
        public NodePart part;

        Models() {
            this(null);
        }

        Models(String file) {
            this.file = file;
        }

        void reload() {
            if (file != null)
                m = new ObjLoader().loadModel(Gdx.files.internal(file));
            else
                m = new ModelBuilder().createBox(1, 1, 1, new Material(ColorAttribute.createDiffuse(1, 1, 1, 0)), VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

            Gdx.app.log("modelinstance", file == null ? "null" : file);
            modelinst = new ModelInstance(m);
            //TODO export from blender with only one part!!!!
            part = modelinst.nodes.get(0).parts.get(0);
        }
    }

    public static final Models[] modelsarr = Models.values();

    @Override
    protected void initialize() {
        for (Models m : modelsarr)
            m.reload();
    }

    @Override
    protected void dispose() {
        for (Models m : modelsarr)
            m.m.dispose();
    }
}
