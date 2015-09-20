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
import com.westreicher.birdsim.util.MaxArray;
import com.westreicher.birdsim.util.SoundPlayer;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by david on 9/4/15.
 */
public class Entity {


    private int lives;

    public enum Type {ENEMY, BULLET, ITEM}

    ;

    public enum ColorAttr {
        RED(new Color(1, 0, 0, 1)), YELLOW(new Color(1, 1, 0, 1)), VIOLET(new Color(1, 1, 0, 1)), TEAL(new Color(1, 0.2f, 0.2f, 1)), GOLD(new Color(1, 0.5f, 0.5f, 1));
        private final ColorAttribute attr;

        ColorAttr(Color col) {
            this.attr = new ColorAttribute(ColorAttribute.Diffuse, col);
        }
    }

    ;

    private static final Vector3 TMPVEC = new Vector3();
    private static final float EDGE = Config.TILES_PER_CHUNK * Config.CHUNKNUMS / 2;
    private static Model playerModel;
    private static Model itemModel;
    private static Model bulletModel;
    private Vector3 speed = new Vector3();
    private Vector3 pos = new Vector3();
    private float scale = 1f;
    private boolean visible;
    private ModelInstance modelInstance;
    private boolean dead = false;
    private Type type;
    private ColorAttr col;
    private float radiant;
    private static ArrayList<Entity> aliveents = new ArrayList<Entity>();
    private Entity parent;
    private float size = 1;
    private MaxArray.MaxArrayEntity collisions = new MaxArray.MaxArrayEntity(10);
    public static Pool<Entity> ents = new Pool<Entity>() {
        @Override
        protected Entity newObject() {
            return new Entity();
        }
    };

    public static void spawn(Vector3 pos, Random rand) {
        if (rand.nextDouble() > 0.1)
            return;
        Entity e = ents.obtain();
        e.init(rand.nextDouble() > 0.2 ? Type.ITEM : Type.ENEMY, pos.x, pos.y, ColorAttr.values()[(int) (Math.random() * (ColorAttr.values().length - 1)) + 1], 0, 0, null);
    }

    public static void shoot(float x, float y, float xspeed, float yspeed, ColorAttr col, Entity parent) {
        Entity e = Entity.ents.obtain();
        //bulletColor = Util.randomColor();
        e.init(Entity.Type.BULLET, x, y, col, xspeed, yspeed, parent);
    }

    public static void init() {
        playerModel = new ObjLoader().loadModel(Gdx.files.internal("player.obj"));
        itemModel = new ModelBuilder().createBox(1, 1, 1, new Material(ColorAttribute.createDiffuse(1, 1, 1, 0)), VertexAttributes.Usage.Position);
        bulletModel = new ModelBuilder().createBox(1, 1, 1, new Material(ColorAttribute.createDiffuse(1, 1, 1, 0)), VertexAttributes.Usage.Position);
    }

    public static void dispose() {
        aliveents.clear();
        ents.clear();
        playerModel.dispose();
        itemModel.dispose();
        bulletModel.dispose();
    }

    public void init(Type type, float posx, float posy, ColorAttr col, float xspeed, float yspeed, Entity parent) {
        this.dead = false;
        this.type = type;
        this.pos.set(posx, posy, 0);
        this.col = col;
        this.parent = parent;
        this.lives = 3;
        Model m = null;
        //if (ChunkManager.isStuck(pos, 1)) type = Type.ITEM;
        switch (type) {
            case ENEMY:
                this.speed.set((float) Math.random() - 0.5f, (float) Math.random() - 0.5f, 0);
                m = playerModel;
                size = 8;
                scale = 1f;
                break;
            case BULLET:
                this.speed.set(xspeed, yspeed, 0);
                scale = 2f;
                size = 5;
                m = bulletModel;
                MyGdxGame.single.playSound(SoundPlayer.Sounds.SHOT1, pos);
                //radiant = -(float) Math.atan2(speed.y, speed.x);
                break;
            case ITEM:
                this.speed.set(0, 0, 0);
                scale = 1f;
                size = 3;
                m = itemModel;
                break;
        }
        this.modelInstance = new ModelInstance(m);
        modelInstance.materials.get(0).set(col.attr);
        aliveents.add(this);
        update(0);
    }

    private void update(float delta) {
        ChunkManager chunkManager = MyGdxGame.single.chunkManager;
        switch (type) {
            case ENEMY:
                if (!tryToMove(speed.x, speed.y, true)) {
                    speed.x = (float) (Math.random() - 0.5f);
                    speed.y = (float) (Math.random() - 0.5f);
                }
                radiant = -(float) Math.atan2(speed.y, speed.x);
                if (Gdx.graphics.getFrameId() % 100 == 0) {
                    Vector3 dir = MyGdxGame.playerTransform.position.cpy().sub(pos);
                    dir.nor();
                    dir.scl(1f);
                    shoot(pos.x, pos.y, dir.x, dir.y, col, this);
                }
                if (lives == 0)
                    dead = true;
                break;
            case BULLET:
                pos.mulAdd(speed, delta * 160f);
                if (chunkManager.getVal(pos) > 0) {
                    dead = true;
                    MyGdxGame.single.playSound(SoundPlayer.Sounds.SHOT2, pos);
                    chunkManager.explode2(pos, 10);
                }
                break;
        }

        //TODO optimize Z projection

        float orig = chunkManager.getVal(pos);
        float dstx = pos.x - MyGdxGame.single.virtualcam.x;
        float dsty = pos.y - MyGdxGame.single.virtualcam.y;
        float dstsq = dstx * dstx + dsty * dsty;
        float dstfrac = (dstsq / (140f * 140f));
        visible = dstfrac <= 1;
        pos.z = orig + (1.0f - dstfrac) * 140.0f + 5;
        if (Math.abs(pos.x) > EDGE || Math.abs(pos.y) > EDGE)
            dead = true;
        this.modelInstance.transform.setToTranslation(pos);
        this.modelInstance.transform.scl(scale);
        this.modelInstance.transform.rotateRad(MyGdxGame.UPAXIS, radiant);
    }


    private void resolveCollisions() {
        switch (type) {
            case ENEMY:
                for (int i = 0; i < collisions.size(); i++) {
                    Entity other = collisions.arr[i];
                    switch (other.type) {
                        case ENEMY:
                            break;
                        case BULLET:
                            collideBulletEnemy(other, this);
                            break;
                    }
                }
                break;
            case BULLET:
                for (int i = 0; i < collisions.size(); i++) {
                    Entity other = collisions.arr[i];
                    switch (other.type) {
                        case ENEMY:
                            collideBulletEnemy(this, other);
                            break;
                        case BULLET:
                            break;
                    }
                }
                break;
        }
    }

    private static void collideBulletEnemy(Entity bullet, Entity enemy) {
        if (bullet.parent != enemy) {
            enemy.lives--;
            bullet.dead = true;
        }
    }

    private boolean tryToMove(float x, float y, boolean simple) {
        ChunkManager chunkManager = MyGdxGame.single.chunkManager;
        pos.x += x;
        pos.y -= y;
        if (chunkManager.getVal(pos) <= 0)
            return true;
        pos.y += y;
        if (simple) {
            pos.x -= x;
            return false;
        }
        if (chunkManager.getVal(pos) <= 0)
            return true;
        pos.x -= x;
        pos.y -= y;
        if (chunkManager.getVal(pos) <= 0)
            return true;
        pos.y += y;
        return false;
    }

    public static void render(ModelBatch mb) {
        for (Entity e : aliveents) {
            if (e.visible)
                mb.render(e.modelInstance);
        }
    }

    public static void updateall(float delta) {
        for (int i = 0; i < aliveents.size(); i++) {
            Entity ent = aliveents.get(i);
            ent.update(delta);
            ent.collisions.reset();
        }
        for (int i = 0; i < aliveents.size() - 1; i++) {
            Entity one = aliveents.get(i);
            for (int j = i + 1; j < aliveents.size(); j++) {
                Entity two = aliveents.get(j);
                double xdist = one.pos.x - two.pos.x;
                double ydist = one.pos.y - two.pos.y;
                double dist = Math.sqrt(xdist * xdist + ydist * ydist);
                if (dist < (one.size + two.size) / 2.0)
                    one.collisions.add(two);
            }
        }
        for (int i = 0; i < aliveents.size(); i++) {
            aliveents.get(i).resolveCollisions();
        }
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
