package com.westreicher.birdsim;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;

import java.util.ArrayList;

/**
 * Created by david on 9/4/15.
 */
public class Entity {

    public enum Type {ENEMY, BULLET, ITEM}

    ;

    private static final Vector3 TMPVEC = new Vector3();
    private static Model playerModel;
    private static Model itemModel;
    private static Model bulletModel;
    private Vector3 speed = new Vector3();
    private Vector3 pos = new Vector3();
    private float scale = 1f;
    private ModelInstance modelInstance;
    private boolean dead = false;
    private Type type;
    private float radiant;
    private static ArrayList<Entity> aliveents = new ArrayList<Entity>();
    public static Pool<Entity> ents = new Pool<Entity>() {
        @Override
        protected Entity newObject() {
            return new Entity();
        }
    };
    private static Color bulletColor = new Color(1, 1, 0, 0);

    public static void spawn(Vector3 pos) {
        if (Math.random() > 0.5)
            return;
        Entity e = ents.obtain();
        e.init(Math.random() > 0.2 ? Type.ITEM : Type.ENEMY, pos, Util.randomColor(), new Vector3());
    }

    public static void shoot(float x, float y, float xspeed, float yspeed) {
        Entity e = Entity.ents.obtain();
        //bulletColor = Util.randomColor();
        e.init(Entity.Type.BULLET, new Vector3(x, y, 0), bulletColor, new Vector3(xspeed, yspeed, 0));
    }

    public static void init() {
        playerModel = new ObjLoader().loadModel(Gdx.files.internal("player.obj"));
        itemModel = new ModelBuilder().createBox(0.2f, 0.5f, 0.2f, new Material(ColorAttribute.createDiffuse(1, 1, 1, 0)), VertexAttributes.Usage.Position);
        bulletModel = new ModelBuilder().createBox(1, 1, 1, new Material(ColorAttribute.createDiffuse(1, 1, 1, 0)), VertexAttributes.Usage.Position);
    }

    public static void dispose() {
        aliveents.clear();
        ents.clear();
        playerModel.dispose();
        itemModel.dispose();
        bulletModel.dispose();
    }

    public void init(Type type, Vector3 pos, Color col, Vector3 speed) {
        this.dead = false;
        this.type = type;
        this.pos.set(pos);
        scale = 1f;
        Model m = null;
        //if (ChunkManager.isStuck(pos, 1)) type = Type.ITEM;
        switch (type) {
            case ENEMY:
                this.speed.set((float) Math.random() - 0.5f, (float) Math.random() - 0.5f, 0);
                m = playerModel;
                scale = 0.4f;
                break;
            case BULLET:
                this.speed.set(speed);
                m = bulletModel;
                //radiant = -(float) Math.atan2(speed.y, speed.x);
                break;
            case ITEM:
                this.speed.set(0, 0, 0);
                m = itemModel;
                col = Color.BLUE;
                break;
        }
        this.modelInstance = new ModelInstance(m);
        modelInstance.materials.get(0).set(ColorAttribute.createDiffuse(col));
        aliveents.add(this);
        update(0);
    }

    private void update(float delta) {
        switch (type) {
            case ENEMY:
                if (!tryToMove(speed.x, speed.y, true)) {
                    speed.x = (float) (Math.random() - 0.5f) * 0.1f;
                    speed.y = (float) (Math.random() - 0.5f) * 0.1f;
                }
                radiant = -(float) Math.atan2(speed.y, speed.x);
                if (Gdx.graphics.getFrameId() % 100 == 0) {
                    Vector3 dir = MyGdxGame.playerTransform.position.cpy().sub(pos);
                    dir.nor();
                    dir.scl(0.1f);
                    shoot(pos.x, pos.y, dir.x, dir.y);
                }
                break;
            case BULLET:
                pos.mulAdd(speed, delta * 80f);
                if (MyGdxGame.single.chunkManager.isStuck(pos, 0)) {
                    dead = true;
                    MyGdxGame.single.chunkManager.explode(pos, 4);
                }
                break;
        }
        if (Math.abs(pos.x) > MyGdxGame.SIZE * ChunkManager.CHUNKNUMS / 2 ||
                Math.abs(pos.y) > MyGdxGame.SIZE * ChunkManager.CHUNKNUMS / 2)
            dead = true;
        this.modelInstance.transform.setToTranslation(pos);
        this.modelInstance.transform.scl(scale);
        this.modelInstance.transform.rotateRad(MyGdxGame.UPAXIS, radiant);
    }

    private boolean tryToMove(float x, float y, boolean simple) {
        pos.x += x;
        pos.y -= y;
        if (!MyGdxGame.single.chunkManager.isStuck(pos, 1))
            return true;
        pos.y += y;
        if (simple) {
            pos.x -= x;
            return false;
        }
        if (!MyGdxGame.single.chunkManager.isStuck(pos, 1))
            return true;
        pos.x -= x;
        pos.y -= y;
        if (!MyGdxGame.single.chunkManager.isStuck(pos, 1))
            return true;
        pos.y += y;
        return false;
    }

    public static void render(ModelBatch mb) {
        for (Entity e : aliveents) {
            mb.render(e.modelInstance);
        }
    }

    public static void updateall(float delta) {
        for (int i = 0; i < aliveents.size(); i++)
            aliveents.get(i).update(delta);
        for (int i = 0; i < aliveents.size(); i++) {
            Entity e = aliveents.get(i);
            if (e.dead) {
                aliveents.remove(i);
                ents.free(e);
            }
        }

    }

    public static void translateAll(float x, float y) {
        for (Entity e : aliveents)
            e.translate(x, y);
    }

    private void translate(float x, float y) {
        this.pos.add(x, y, 0);
    }

}
