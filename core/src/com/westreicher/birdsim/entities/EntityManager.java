package com.westreicher.birdsim.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;
import com.westreicher.birdsim.*;
import com.westreicher.birdsim.util.InputHelper;
import com.westreicher.birdsim.util.MaxArray;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by david on 9/25/15.
 */
public class EntityManager {
    public enum Type {ENEMY, BULLET, ITEM, PLAYER}

    public enum ColorAttr {
        RED(new Color(1, 0, 0, 1)), VIOLET(new Color(1, 0, 1, 1)), YELLOW(new Color(0.9f, 1, 0, 1)), TEAL(new Color(1, 0.2f, 0.2f, 1)), GOLD(new Color(1, 0.5f, 0.5f, 1)), BLUE(new Color(0, 0, 1, 1));
        public final ColorAttribute attr;

        ColorAttr(Color col) {
            this.attr = new ColorAttribute(ColorAttribute.Diffuse, col);
        }
    }

    private static final ColorAttr[] colors = ColorAttr.values();

    public final MaxArray.MaxArrayEntity aliveplayers = new MaxArray.MaxArrayEntity(4);
    public final MaxArray.MaxArrayEntity players = new MaxArray.MaxArrayEntity(4);
    private final MaxArray.MaxArrayEntity tmpplayersarr = new MaxArray.MaxArrayEntity(4);
    private final ArrayList<Entity> aliveents = new ArrayList<Entity>();
    private final Pool<Entity> ents = new Pool<Entity>() {
        @Override
        protected Entity newObject() {
            return new Entity();
        }
    };
    private Model playerModel;
    private Model itemModel;
    private Model bulletModel;

    public EntityManager() {
        playerModel = new ObjLoader().loadModel(Gdx.files.internal("player.obj"));
        itemModel = new ModelBuilder().createBox(1, 1, 1, new Material(ColorAttribute.createDiffuse(1, 1, 1, 0)), VertexAttributes.Usage.Position);
        bulletModel = new ObjLoader().loadModel(Gdx.files.internal("rocket.obj"));
        Entity.manager = this;
    }

    public void render(ModelBatch mb, float interp) {
        //TODO Use custom rendering instead of modelinstances?
        for (int i = 0; i < aliveents.size(); i++) {
            Entity ent = aliveents.get(i);
            ent.setModelInstance(interp);
            if (ent.visible)
                mb.render(ent.modelInstance);
        }
    }

    public void updateall(long tick, Vector3 cam) {
        //DEBUG RESPAWN
        if (Gdx.input.isKeyPressed(Input.Keys.R) || InputHelper.players.get(0).thirdPointer.update()) {
            respawnOnePlayer(cam);
        }

        MaxArray.MaxArrayEntity slowmoplayers = tmpplayersarr;
        slowmoplayers.reset();
        for (int i = 0; i < aliveplayers.size(); i++) {
            Entity player = aliveplayers.arr[i];
            if (player.specialitySlot.type == SlotSystem.Specialty.SLOWMO)
                slowmoplayers.add(player);
        }
        for (int i = 0; i < aliveents.size(); i++) {
            Entity ent = aliveents.get(i);
            boolean belongsToSlowMoPlayer = slowmoplayers.contains(ent) || (ent.type == Type.BULLET && slowmoplayers.contains(ent.parent));
            if (slowmoplayers.size() > 0 && !belongsToSlowMoPlayer) {
                if (tick % 5 == 0)
                    ent.updateLogic(tick / 5);
            } else ent.updateLogic(tick);

            ent.updateZ(cam);
            ent.collisions.reset();
        }
        for (int i = 0; i < aliveents.size() - 1; i++) {
            Entity one = aliveents.get(i);
            for (int j = i + 1; j < aliveents.size(); j++) {
                Entity two = aliveents.get(j);
                double xdist = one.pos.x - two.pos.x;
                double ydist = one.pos.y - two.pos.y;
                double dist = Math.sqrt(xdist * xdist + ydist * ydist);
                if (dist < (one.scale + two.scale) / 2.0)
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
                if (e.type != Type.PLAYER)
                    ents.free(e);
                else
                    aliveplayers.remove(e);
            }
        }
    }

    public boolean respawnOnePlayer(Vector3 pos) {
        tmpplayersarr.reset();
        for (int i = 0; i < players.size(); i++) {
            if (players.arr[i].dead) {
                tmpplayersarr.add(players.arr[i]);
                break;
            }
        }
        if (tmpplayersarr.size() > 0) {
            Entity player = tmpplayersarr.arr[(int) (Math.random() * tmpplayersarr.size())];
            player.init(EntityManager.Type.PLAYER, pos.x, pos.y, player.col, 0, 0, playerModel, null);
            aliveplayers.add(player);
            return true;
        }
        return false;
    }


    public void translateAll(float x, float y) {
        for (int i = 0; i < aliveents.size(); i++) {
            aliveents.get(i).translate(x, y);
        }
    }

    public void dispose() {
        aliveents.clear();
        ents.clear();
        playerModel.dispose();
        itemModel.dispose();
        bulletModel.dispose();
        aliveplayers.reset();
        tmpplayersarr.reset();
    }

    public void spawn(float x, float y, Random rand) {
        if (!Config.SPAWN_STUFF || rand.nextDouble() > 0.3)
            return;
        Entity e = ents.obtain();
        Type t = rand.nextDouble() > 0.5 ? Type.ITEM : Type.ENEMY;
        e.init(t, x, y, ColorAttr.YELLOW, 0, 0, t == Type.ITEM ? itemModel : playerModel, null);
        aliveents.add(e);
    }

    public Entity spawnPlayer(int id) {
        Entity e = ents.obtain();
        e.init(Type.PLAYER, 0, 0, colors[id], 0, 0, playerModel, null);
        aliveents.add(e);
        e.id = id;
        aliveplayers.add(e);
        players.add(e);
        return e;
    }

    public void shoot(float x, float y, float xspeed, float yspeed, ColorAttr col, Entity parent) {
        Entity e = ents.obtain();
        e.init(Type.BULLET, x, y, col, xspeed, yspeed, bulletModel, parent);
        aliveents.add(e);
    }
}
