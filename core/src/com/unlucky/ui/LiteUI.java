package com.unlucky.ui;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.unlucky.Unlucky;
import com.unlucky.resource.ResourceManager;

/**
 * 
 * @author hundun
 * Created on 2021/10/23
 */
public abstract class LiteUI {

    
    protected final Batch batch;
    protected final Camera camera;
    protected final ShapeRenderer shapeRenderer;

    public LiteUI(Batch batch, Camera camera, ShapeRenderer shapeRenderer) {
        this.batch = batch;
        this.camera = camera;
        this.shapeRenderer = shapeRenderer;
    }


    public abstract void update(float dt);

    public abstract void render(float dt);


}
