package com.unlucky.ui;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.unlucky.Unlucky;
import com.unlucky.entity.Player;
import com.unlucky.map.TileMap;
import com.unlucky.resource.ResourceManager;
import com.unlucky.screen.WorldScreen;

/**
 * Superclass for all UI
 * Contains useful variables and references
 *
 * @author Ming Li
 */
public abstract class UI implements Disposable {

    protected final ResourceManager rm;
    
    //protected final WorldScreen worldScreen;
    protected final Unlucky game;
    //protected final Batch batch;
    // graphics
    protected final ShapeRenderer shapeRenderer;
    
    protected final Stage stage;


    public UI(final Unlucky game, Batch batch, Stage stage, ResourceManager rm, ShapeRenderer shapeRenderer) {
        this.game = game;
        //this.worldScreen = worldScreen;
        this.rm = rm;
        //this.batch = batch;
        this.stage = stage;
        this.shapeRenderer = shapeRenderer;
    }


    public abstract void update(float dt);

    public abstract void render(float dt);

    public Stage getStage() {
        return stage;
    }

    @Override
    public void dispose() {
        stage.dispose();
        shapeRenderer.dispose();
    }


}