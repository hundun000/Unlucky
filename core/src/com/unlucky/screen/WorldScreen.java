package com.unlucky.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.unlucky.Unlucky;
import com.unlucky.event.BattleCoreLogic;
import com.unlucky.event.WorldState;
import com.unlucky.map.Tile;
import com.unlucky.map.WorldCoreLogic;
import com.unlucky.parallax.BackgroundUI;
import com.unlucky.resource.ResourceManager;
import com.unlucky.screen.game.LevelUpUI;
import com.unlucky.screen.game.ScreenTransitionUI;
import com.unlucky.ui.battleui.BattleUI;
import com.unlucky.ui.dialog.DialogUI;
import com.unlucky.ui.dialog.IDialogResultHandler;
import com.unlucky.ui.Hud;

/**
 * Handles all gameplay.
 *
 * @author Ming Li
 */
public class WorldScreen extends AbstractScreen implements IDialogResultHandler<WorldState> {

    public WorldState worldState;

    public final WorldCoreLogic worldCoreLogic;
    public final Hud hud;
    
    // sub-UIs
    public final BattleUI battleUI;
    public final ScreenTransitionUI transitionUI;
    public final LevelUpUI levelUpUI;
    public final DialogUI<WorldState> dialogUI;
    
    // input
    public final InputMultiplexer multiplexer;

    // battle background
    private BackgroundUI[] backgroundUIs;

    // key
    private int worldIndex;
    private int levelIndex;

    // whether or not to reset the game map on show
    // used for transitioning between screen during a pause
    public boolean resetGameAfterFadeInShow = true;

    public WorldScreen(final Unlucky game, final ResourceManager rm) {
        super(game, rm);

        worldState = WorldState.MOVING;

        worldCoreLogic = new WorldCoreLogic(this, game.player, rm);
        
        hud = new Hud(this, worldCoreLogic.player, rm);
        battleUI = new BattleUI(this, worldCoreLogic.tileMap, worldCoreLogic.player, rm);
        transitionUI = new ScreenTransitionUI(this, battleUI, worldCoreLogic.tileMap, hud, worldCoreLogic.player, rm);
        levelUpUI = new LevelUpUI(this, worldCoreLogic.player, rm);
        this.dialogUI = new DialogUI<>(this, this, worldCoreLogic.player, stage, rm);
        
        // create bg
        backgroundUIs = new BackgroundUI[2];
        // sky
        backgroundUIs[0] = new BackgroundUI(batch, (OrthographicCamera) battleUI.getStage().getCamera(), new Vector2(0.3f, 0), shapeRenderer);
        // field
        backgroundUIs[1] = new BackgroundUI(batch, (OrthographicCamera) battleUI.getStage().getCamera(), new Vector2(0, 0), shapeRenderer);


        // input multiplexer
        multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(hud.getStage());
        multiplexer.addProcessor(battleUI.getStage());
        multiplexer.addProcessor(levelUpUI.getStage());
        multiplexer.addProcessor(dialogUI.getStage());
    }

    public void init(int worldIndex, int levelIndex) {
        this.worldIndex = worldIndex;
        this.levelIndex = levelIndex;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(multiplexer);
        batchFade = renderBatch = true;

        // fade in animation
        hud.getStage().addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0.5f)));

        if (resetGameAfterFadeInShow) {
            // init tile map
            setWorldState(WorldState.MOVING);
            hud.deathGroup.setVisible(false);
            worldCoreLogic.init(worldIndex, levelIndex);
            worldCoreLogic.player.moving = -1;
            
            //hud.setTileMap(gamePublicData.tileMap);
            battleUI.setTileMapToData(worldCoreLogic.tileMap);
            //levelUpUI.setTileMap(gamePublicData.tileMap);
            //worldDialogUI.setTileMap(gamePublicData.tileMap);

            // update bg
            createBackground(worldCoreLogic.worldIndex);

            hud.toggle(true);
            hud.touchDown = false;
            hud.shade.setVisible(false);
            hud.startLevelDescriptor();
        }
    }

    /**
     * Creates the dynamic background
     * @param bgIndex is the theme of bg
     */
    private void createBackground(int bgIndex) {
        // background image array is ordered by depth
        TextureRegion[] images = rm.battleBackgrounds400x240[bgIndex];
        for (int i = 0; i < 2; i++) {
            backgroundUIs[i].setImage(images[i]);
        }
        // set background movement for the specific worlds
        if (bgIndex == 0) {
            backgroundUIs[0].setVector(40, 0);
        }
        else if (bgIndex == 1) {
            backgroundUIs[0].setVector(0, 0);
        }
        else if (bgIndex == 2) {
            backgroundUIs[0].setVector(40, 0);
        }
        backgroundUIs[1].setVector(0, 0);
    }

    /**
     * When the player dies it shows a "click to continue" message along with what they lost
     */
    public void die() {
        // reset player's hp after dying
        worldCoreLogic.player.setHp(worldCoreLogic.player.getMaxHp());
        setWorldState(WorldState.DEATH);
        hud.toggle(false);
        hud.deathGroup.setVisible(true);
    }

    /**
     * Updates the camera position to follow the player unless he's on the edges of the map
     */
    public void updateCamera() {
        boolean playerNearLeftBottom = worldCoreLogic.player.getPosition().x < 6 * 16;
        boolean playerNearRightBottom = worldCoreLogic.player.getPosition().x > worldCoreLogic.tileMap.mapWidth * 16 - 7 * 16;
        boolean playerNearDownBottom =  worldCoreLogic.player.getPosition().y < 4 * 16 - 8;
        boolean playerNearTopBottom = worldCoreLogic.player.getPosition().y > worldCoreLogic.tileMap.mapHeight * 16 - 4 * 16;
        // camera directs on the player
        if (!playerNearRightBottom && !playerNearLeftBottom) {
            cam.position.x = worldCoreLogic.player.getPosition().x + 8;
        }
        if (!playerNearTopBottom && !playerNearDownBottom) {
            cam.position.y = worldCoreLogic.player.getPosition().y + 4;
        }
        cam.update();

        if (playerNearLeftBottom) {
            cam.position.x = 104;
        }
        if (playerNearDownBottom) {
            cam.position.y = 60.5f;
        }
        if (playerNearRightBottom) {
            cam.position.x = (worldCoreLogic.tileMap.mapWidth * 16 - 7 * 16) + 8;
        }
        if (playerNearTopBottom) {
            cam.position.y = (worldCoreLogic.tileMap.mapHeight * 16 - 4 * 16) + 4;
        }
    }

    public void update(float dt) {
        if (worldState != WorldState.PAUSE) {
            // update game time
            worldCoreLogic.time += dt;
        }

        if (worldState == WorldState.MOVING) {
            updateCamera();

            worldCoreLogic.update(dt);
            hud.update(dt);
        }

        if (worldState == WorldState.BATTLING) {
            // update bg
            for (int i = 0; i < backgroundUIs.length; i++) {
                backgroundUIs[i].update(dt);
            }
            battleUI.update(dt);
        }

        if (worldState == WorldState.TRANSITION) transitionUI.update(dt);
        if (worldState == WorldState.LEVEL_UP) levelUpUI.update(dt);
        if (worldState == WorldState.IN_TILE_EVENT) dialogUI.update(dt);
        if (worldState == WorldState.INVENTORY) game.inventoryUI.update(dt);
    }

    @Override
    public void render(float dt) {
        update(dt);

        // clear screen
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        batch.begin();
        
        if (renderBatch) {
            

            // fix fading
            if (batchFade) batch.setColor(Color.WHITE);

            if (worldState == WorldState.BATTLING || transitionUI.renderBattle) {
                // bg camera
                batch.setProjectionMatrix(battleUI.getStage().getCamera().combined);
                for (int i = 0; i < backgroundUIs.length; i++) {
                    backgroundUIs[i].render(dt);
                }
            }

            if (worldState == WorldState.MOVING || worldState == WorldState.INVENTORY ||
                transitionUI.renderMap || worldState == WorldState.IN_TILE_EVENT ||
                worldState == WorldState.DEATH || worldState == WorldState.PAUSE) {
                // map camera
                batch.setProjectionMatrix(cam.combined);
                // render map and player
                worldCoreLogic.render(dt, batch, cam);
            }
            batch.end();
            
        }

        if (worldState == WorldState.MOVING || worldState == WorldState.DEATH || worldState == WorldState.PAUSE) {
            hud.render(dt);
        }
        if (worldState == WorldState.BATTLING || transitionUI.renderBattle) {
            battleUI.render(dt);
        }
        if (worldState == WorldState.LEVEL_UP || transitionUI.renderLevelUp) {
            levelUpUI.render(dt);
        }
        if (worldState == WorldState.IN_TILE_EVENT) {
            dialogUI.render(dt);
        }
        if (worldState == WorldState.INVENTORY) {
            game.inventoryUI.render(dt);
        }
        if (worldState == WorldState.TRANSITION) {
            transitionUI.render(dt);
        }
        
        
        //game.profile("GameScreen");
    }

    @Override
    public void dispose() {
        super.dispose();
        hud.dispose();
        battleUI.dispose();
        dialogUI.dispose();
        levelUpUI.dispose();
    }

    /**
     * @TODO: Add some sort of transitioning between events
     * @param event
     */
    public void setWorldState(WorldState event) {
        this.worldState = event;
    }

    @Override
    public void handleDialogResult(WorldState event) {
        switch (event) {
            case MOVING:
                worldCoreLogic.player.finishTileInteraction();
                TextureRegion none = null;
                this.worldCoreLogic.tileMap.setTile(this.worldCoreLogic.tileMap.toTileCoords(worldCoreLogic.player.getPosition()),
                        new Tile(-1, none, this.worldCoreLogic.tileMap.toTileCoords(worldCoreLogic.player.getPosition())));
                // player died from tile
                if (worldCoreLogic.player.getHp() <= 0) {
                    this.worldCoreLogic.setDeath();
                    this.die();
                    return;
                }
                this.setWorldState(WorldState.MOVING);
                this.hud.toggle(true);
                break;
        default:
    }
    }

}