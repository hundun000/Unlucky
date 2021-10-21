package com.unlucky.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.unlucky.event.BattleData;
import com.unlucky.event.WorldState;
import com.unlucky.main.Unlucky;
import com.unlucky.map.worldData;
import com.unlucky.parallax.Background;
import com.unlucky.resource.ResourceManager;
import com.unlucky.screen.game.WorldDialogUI;
import com.unlucky.screen.game.LevelUpUI;
import com.unlucky.screen.game.ScreenTransitionUI;
import com.unlucky.ui.battleui.BattleUI;
import com.unlucky.ui.Hud;

/**
 * Handles all gameplay.
 *
 * @author Ming Li
 */
public class WorldScreen extends AbstractScreen {

    public WorldState worldState;

    public worldData worldData;
    public Hud hud;
    
    // sub-UIs
    public BattleUI battleUI;
    public ScreenTransitionUI transitionUI;
    public LevelUpUI levelUpUI;
    public WorldDialogUI worldDialogUI;

    // input
    public InputMultiplexer multiplexer;

    // battle background
    private Background[] backgrounds;

    // key
    private int worldIndex;
    private int levelIndex;

    // whether or not to reset the game map on show
    // used for transitioning between screen during a pause
    public boolean resetGameAfterFadeInShow = true;

    public WorldScreen(final Unlucky game, final ResourceManager rm) {
        super(game, rm);

        worldState = WorldState.MOVING;

        worldData = new worldData(this, game.player, rm);
        
        hud = new Hud(this, worldData.player, rm);
        battleUI = new BattleUI(this, worldData.tileMap, worldData.player, rm);
        transitionUI = new ScreenTransitionUI(this, battleUI, worldData.tileMap, hud, worldData.player, rm);
        levelUpUI = new LevelUpUI(this, worldData.player, rm);
        worldDialogUI = new WorldDialogUI(this, worldData.player, rm);

        // create bg
        backgrounds = new Background[2];
        // sky
        backgrounds[0] = new Background((OrthographicCamera) battleUI.getStage().getCamera(), new Vector2(0.3f, 0));
        // field
        backgrounds[1] = new Background((OrthographicCamera) battleUI.getStage().getCamera(), new Vector2(0, 0));


        // input multiplexer
        multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(hud.getStage());
        multiplexer.addProcessor(battleUI.getStage());
        multiplexer.addProcessor(levelUpUI.getStage());
        multiplexer.addProcessor(worldDialogUI.getStage());
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
            worldData.init(worldIndex, levelIndex);
            worldData.player.moving = -1;
            
            //hud.setTileMap(gamePublicData.tileMap);
            battleUI.setTileMapToData(worldData.tileMap);
            //levelUpUI.setTileMap(gamePublicData.tileMap);
            //worldDialogUI.setTileMap(gamePublicData.tileMap);

            // update bg
            createBackground(worldData.worldIndex);

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
            backgrounds[i].setImage(images[i]);
        }
        // set background movement for the specific worlds
        if (bgIndex == 0) {
            backgrounds[0].setVector(40, 0);
        }
        else if (bgIndex == 1) {
            backgrounds[0].setVector(0, 0);
        }
        else if (bgIndex == 2) {
            backgrounds[0].setVector(40, 0);
        }
        backgrounds[1].setVector(0, 0);
    }

    /**
     * When the player dies it shows a "click to continue" message along with what they lost
     */
    public void die() {
        // reset player's hp after dying
        worldData.player.setHp(worldData.player.getMaxHp());
        setWorldState(WorldState.DEATH);
        hud.toggle(false);
        hud.deathGroup.setVisible(true);
    }

    /**
     * Updates the camera position to follow the player unless he's on the edges of the map
     */
    public void updateCamera() {
        boolean playerNearLeftBottom = worldData.player.getPosition().x < 6 * 16;
        boolean playerNearRightBottom = worldData.player.getPosition().x > worldData.tileMap.mapWidth * 16 - 7 * 16;
        boolean playerNearDownBottom =  worldData.player.getPosition().y < 4 * 16 - 8;
        boolean playerNearTopBottom = worldData.player.getPosition().y > worldData.tileMap.mapHeight * 16 - 4 * 16;
        // camera directs on the player
        if (!playerNearRightBottom && !playerNearLeftBottom) {
            cam.position.x = worldData.player.getPosition().x + 8;
        }
        if (!playerNearTopBottom && !playerNearDownBottom) {
            cam.position.y = worldData.player.getPosition().y + 4;
        }
        cam.update();

        if (playerNearLeftBottom) {
            cam.position.x = 104;
        }
        if (playerNearDownBottom) {
            cam.position.y = 60.5f;
        }
        if (playerNearRightBottom) {
            cam.position.x = (worldData.tileMap.mapWidth * 16 - 7 * 16) + 8;
        }
        if (playerNearTopBottom) {
            cam.position.y = (worldData.tileMap.mapHeight * 16 - 4 * 16) + 4;
        }
    }

    public void update(float dt) {
        if (worldState != WorldState.PAUSE) {
            // update game time
            worldData.time += dt;
        }

        if (worldState == WorldState.MOVING) {
            updateCamera();

            worldData.update(dt);
            hud.update(dt);
        }

        if (worldState == WorldState.BATTLING) {
            // update bg
            for (int i = 0; i < backgrounds.length; i++) {
                backgrounds[i].update(dt);
            }
            battleUI.update(dt);
        }

        if (worldState == WorldState.TRANSITION) transitionUI.update(dt);
        if (worldState == WorldState.LEVEL_UP) levelUpUI.update(dt);
        if (worldState == WorldState.IN_TILE_EVENT) worldDialogUI.update(dt);
        if (worldState == WorldState.INVENTORY) game.inventoryUI.update(dt);
    }

    public void render(float dt) {
        update(dt);

        // clear screen
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (renderBatch) {
            game.batch.begin();

            // fix fading
            if (batchFade) game.batch.setColor(Color.WHITE);

            if (worldState == WorldState.BATTLING || transitionUI.renderBattle) {
                // bg camera
                game.batch.setProjectionMatrix(battleUI.getStage().getCamera().combined);
                for (int i = 0; i < backgrounds.length; i++) {
                    backgrounds[i].render(game.batch);
                }
            }

            if (worldState == WorldState.MOVING || worldState == WorldState.INVENTORY ||
                transitionUI.renderMap || worldState == WorldState.IN_TILE_EVENT ||
                worldState == WorldState.DEATH || worldState == WorldState.PAUSE) {
                // map camera
                game.batch.setProjectionMatrix(cam.combined);
                // render map and player
                worldData.render(dt, game.batch, cam);
            }

            game.batch.end();
        }

        if (worldState == WorldState.MOVING || worldState == WorldState.DEATH || worldState == WorldState.PAUSE)
            hud.render(dt);
        if (worldState == WorldState.BATTLING || transitionUI.renderBattle)
            battleUI.render(dt);
        if (worldState == WorldState.LEVEL_UP || transitionUI.renderLevelUp)
            levelUpUI.render(dt);
        if (worldState == WorldState.IN_TILE_EVENT) worldDialogUI.render(dt);
        if (worldState == WorldState.INVENTORY) game.inventoryUI.render(dt);
        if (worldState == WorldState.TRANSITION) transitionUI.render(dt);

        //game.profile("GameScreen");
    }

    public void dispose() {
        super.dispose();
        hud.dispose();
        battleUI.dispose();
        worldDialogUI.dispose();
        levelUpUI.dispose();
    }

    /**
     * @TODO: Add some sort of transitioning between events
     * @param event
     */
    public void setWorldState(WorldState event) {
        this.worldState = event;
    }

}