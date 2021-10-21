package com.unlucky.ui;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.unlucky.entity.Player;
import com.unlucky.main.Unlucky;
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

    protected Stage stage;
    protected final Viewport viewport;

    protected final ResourceManager rm;
    protected final Player player;
    //protected final WorldScreen worldScreen;
    protected final Unlucky game;
    protected final Batch batch;
    // graphics
    protected final ShapeRenderer shapeRenderer;

//    public UI(final Unlucky game, GameScreen gameScreen, Player player, ResourceManager rm) {
//        this.game = game;
//        this.gameScreen = gameScreen;
//        this.player = player;
//        this.rm = rm;
//
//        viewport = new StretchViewport(Unlucky.V_WIDTH, Unlucky.V_HEIGHT, new OrthographicCamera());
//        stage = new Stage(viewport, game.batch);
//
//        shapeRenderer = new ShapeRenderer();
//    }

    
    public UI(final Unlucky game, Player player, Batch batch, ResourceManager rm) {
        this.game = game;
        //this.worldScreen = worldScreen;
        this.player = player;
        this.rm = rm;
        this.batch = batch;
        
        viewport = new StretchViewport(Unlucky.V_WIDTH, Unlucky.V_HEIGHT, new OrthographicCamera());
        stage = new Stage(viewport, batch);

        shapeRenderer = new ShapeRenderer();
    }
    

    public UI(final Unlucky game, Player player, ResourceManager rm) {
        this(game, player, game.batch, rm);
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