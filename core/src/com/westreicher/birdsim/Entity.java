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
import com.westreicher.birdsim.util.InputHelper;
import com.westreicher.birdsim.util.MaxArray;
import com.westreicher.birdsim.util.SoundPlayer;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by david on 9/4/15.
 */
public class Entity {


    private int lives;
    private int id;

    private enum Type {ENEMY, BULLET, ITEM, PLAYER}

    ;

    public enum ColorAttr {
        RED(new Color(1, 0, 0, 1)), YELLOW(new Color(1, 1, 0, 1)), VIOLET(new Color(1, 1, 0, 1)), TEAL(new Color(1, 0.2f, 0.2f, 1)), GOLD(new Color(1, 0.5f, 0.5f, 1));
        private final ColorAttribute attr;

        ColorAttr(Color col) {
            this.attr = new ColorAttribute(ColorAttribute.Diffuse, col);
        }
    }

    ;

    private static final float EDGE = Config.TILES_PER_CHUNK * Config.CHUNKNUMS / 2;
    private static final ColorAttr[] colors = ColorAttr.values();
    private static final Vector3 TMP_VEC3 = new Vector3();
    private static Model playerModel;
    private static Model itemModel;
    private static Model bulletModel;
    private Vector3 speed = new Vector3();
    public Vector3 pos = new Vector3();
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
    public static MaxArray.MaxArrayEntity players = new MaxArray.MaxArrayEntity(4);
    private static Pool<Entity> ents = new Pool<Entity>() {
        @Override
        protected Entity newObject() {
            return new Entity();
        }
    };

    public static void spawn(float x, float y, Random rand) {
        if (!Config.SPAWN_STUFF || rand.nextDouble() > 0.1)
            return;
        Entity e = ents.obtain();
        e.init(rand.nextDouble() > 0.2 ? Type.ITEM : Type.ENEMY, x, y, colors[(int) (Math.random() * (colors.length - 1)) + 1], 0, 0, null);
    }

    public static Entity spawnPlayer(int id) {
        Entity e = ents.obtain();
        e.init(Type.PLAYER, 0, 0, colors[0], 0, 0, null);
        e.id = id;
        players.add(e);
        return e;
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
            case PLAYER:
                this.speed.set(0, 0, 0);
                scale = 1f;
                size = 8;
                m = playerModel;
                break;
        }
        //TODO don't allocate new modelinstances
        this.modelInstance = new ModelInstance(m);
        modelInstance.materials.get(0).set(col.attr);
        aliveents.add(this);
        update(0);
    }

    private void update(long tick) {
        ChunkManager chunkManager = MyGdxGame.single.chunkManager;
        switch (type) {
            case ENEMY:
                if (!tryToMove(speed.x, speed.y, true)) {
                    speed.x = (float) (Math.random() - 0.5f);
                    speed.y = (float) (Math.random() - 0.5f);
                }
                radiant = -(float) Math.atan2(speed.y, speed.x);
                if (tick % 50 == 0) {
                    for (int i = 0; i < players.size(); i++) {
                        TMP_VEC3.set(players.arr[i].pos).sub(pos);
                        TMP_VEC3.nor();
                        TMP_VEC3.scl(1f);
                        shoot(pos.x, pos.y, TMP_VEC3.x, TMP_VEC3.y, col, this);
                    }
                }
                if (lives == 0)
                    dead = true;
                break;
            case BULLET:
                pos.mulAdd(speed, 3);
                if (chunkManager.getVal(pos) > 0) {
                    dead = true;
                    MyGdxGame.single.playSound(SoundPlayer.Sounds.SHOT2, pos);
                    chunkManager.explode2(pos, 10);
                }
                break;
            case PLAYER:
                InputHelper firstPointer = InputHelper.players.get(id).firstPointer;
                InputHelper secondPointer = InputHelper.players.get(id).secondPointer;
                if (firstPointer.update()) {
                    float rad = firstPointer.getRadiant();
                    this.radiant = rad;
                    //movePlayer(mousex * delta * Config.MOVE_SPEED, mousey * delta * Config.MOVE_SPEED);
                    tryToMove((float) Math.cos(rad) * Config.MOVE_SPEED, -(float) Math.sin(rad) * Config.MOVE_SPEED, false);
                }
                //Gdx.app.log("game", "" +"");
                if (secondPointer.update()) {
                    float radiant = secondPointer.getRadiant();
                    float xspeed = (float) Math.cos(radiant);
                    float yspeed = (float) Math.sin(radiant);
                    if (tick % 10 == 0) {
                        Entity.shoot(pos.x, pos.y, xspeed, yspeed, Entity.ColorAttr.RED, null);
                    }
                }
                break;
        }


        float orig = chunkManager.getVal(pos) * Config.TERRAIN_HEIGHT;
        if (Config.POST_PROCESSING) {
            //TODO optimize Z projection
            float dstx = pos.x - MyGdxGame.single.virtualcam.x;
            float dsty = pos.y - MyGdxGame.single.virtualcam.y;
            float dstsq = dstx * dstx + dsty * dsty;
            float dstfrac = (dstsq / (140f * 140f));
            visible = dstfrac <= 1;
            pos.z = orig + (1.0f - dstfrac) * 140.0f + 5;
        } else {
            visible = true;
            pos.z = orig + 5;
        }
        if (Math.abs(pos.x) > EDGE || Math.abs(pos.y) > EDGE)
            dead = true;
        this.modelInstance.transform.setToTranslation(pos);
        this.modelInstance.transform.scl(scale);
        this.modelInstance.transform.rotateRad(Config.UPAXIS, radiant);
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
            case ITEM:
                for (int i = 0; i < collisions.size(); i++) {
                    Entity other = collisions.arr[i];
                    switch (other.type) {
                        case PLAYER:
                            collideItemPlayer(this, other);
                    }
                }
                break;
            case PLAYER:
                for (int i = 0; i < collisions.size(); i++) {
                    Entity other = collisions.arr[i];
                    switch (other.type) {
                        case ITEM:
                            collideItemPlayer(other, this);
                    }
                }
                break;
        }
    }

    private void collideItemPlayer(Entity item, Entity player) {
        item.dead = true;
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
        //TODO Use custom rendering instead of modelinstances?
        for (int i = 0; i < aliveents.size(); i++) {
            Entity ent = aliveents.get(i);
            if (ent.visible)
                mb.render(ent.modelInstance);
        }
    }

    public static void updateall(long tick) {
        for (int i = 0; i < aliveents.size(); i++) {
            Entity ent = aliveents.get(i);
            ent.update(tick);
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
        for (int i = 0; i < aliveents.size(); i++) {
            aliveents.get(i).translate(x, y);
        }
    }

    private void translate(float x, float y) {
        this.pos.add(x, y, 0);
    }

}
