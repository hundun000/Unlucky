package com.unlucky;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.unlucky.entity.Player;
import com.unlucky.parallax.BackgroundUI;
import com.unlucky.resource.ResourceManager;
import com.unlucky.save.SaveManager;
import com.unlucky.screen.*;
import com.unlucky.screen.game.VictoryScreen;
import com.unlucky.ui.inventory.InventoryUI;

/**
 * "Unlucky" is a RPG/Dungeon Crawler based on RNG
 * The player will go through various levels with numerous enemies
 * and attempt to complete each level by reaching the end tile.
 *
 * @author Ming Li
 */
public class Unlucky extends Game {

    public static final String VERSION = "1.0";
    public static final String TITLE = "Unlucky Version " + VERSION;

    // Links
    public static final String GITHUB = "https://github.com/mingli1/Unlucky";
    public static final String YOUTUBE = "https://www.youtube.com/channel/UC-oA-vkeYrgEy23Sq2PLC8w/videos?shelf_id=0&sort=dd&view=0";

    // Desktop screen dimensions
    public static final int V_WIDTH = 200;
    public static final int V_HEIGHT = 120;
    public static final int V_SCALE = 6;

    // frame-component shared between Screens
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    
    // game-data shared between Screens
    private ResourceManager rm;
    public Player player;
    public SaveManager saveManager;

    // Screens
    public MenuScreen menuScreen;
    public WorldScreen worldScreen;
    public WorldSelectScreen worldSelectScreen;
    public LevelSelectScreen levelSelectScreen;
    public InventoryScreen inventoryScreen;
    public ShopScreen shopScreen;
    public SpecialMoveScreen smoveScreen;
    public StatisticsScreen statisticsScreen;
    public VictoryScreen victoryScreen;
    public SettingsScreen settingsScreen;

    // UI shared between Screens
    public InventoryUI inventoryUI;
    public BackgroundUI[] menuBackground;

    // debugging
    public Label fps;

	public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        rm = new ResourceManager();
        player = new Player("player", rm);

        saveManager = new SaveManager(player, "save.json");
        saveManager.load(rm);

        // debugging
        fps = new Label("", new Label.LabelStyle(rm.pixel10, Color.RED));
        fps.setFontScale(0.5f);
        fps.setVisible(player.settings.showFps);

        
        menuScreen = new MenuScreen(this, rm);
        worldScreen = new WorldScreen(this, rm);
        worldSelectScreen = new WorldSelectScreen(this, rm);
        levelSelectScreen = new LevelSelectScreen(this, rm);
        inventoryScreen = new InventoryScreen(this, rm);
        shopScreen = new ShopScreen(this, rm);
        smoveScreen = new SpecialMoveScreen(this, rm);
        statisticsScreen = new StatisticsScreen(this, rm);
        victoryScreen = new VictoryScreen(this, rm);
        settingsScreen = new SettingsScreen(this, rm);

        inventoryUI = new InventoryUI(this, worldScreen, player, rm, inventoryScreen.getStage());
        
        // create parallax background
        menuBackground = new BackgroundUI[3];

        // ordered by depth
        // sky
        menuBackground[0] = new BackgroundUI(batch, rm.titleScreenBackground[0],
            menuScreen.getStage().getCamera(), new Vector2(0, 0), shapeRenderer);
        menuBackground[0].setVector(0, 0);
        // back clouds
        menuBackground[1] = new BackgroundUI(batch, rm.titleScreenBackground[2],
            menuScreen.getStage().getCamera(), new Vector2(0.3f, 0), shapeRenderer);
        menuBackground[1].setVector(20, 0);
        // front clouds
        menuBackground[2] = new BackgroundUI(batch, rm.titleScreenBackground[1],
            menuScreen.getStage().getCamera(), new Vector2(0.3f, 0), shapeRenderer);
        menuBackground[2].setVector(60, 0);

        // profiler
        GLProfiler.enable();

        this.setScreen(menuScreen);
	}

	public void render() {
        fps.setText(Gdx.graphics.getFramesPerSecond() + " fps");
        super.render();
    }

	public void dispose() {
        batch.dispose();
        super.dispose();

        rm.dispose();
        menuScreen.dispose();
        worldScreen.dispose();
        worldSelectScreen.dispose();
        levelSelectScreen.dispose();
        inventoryScreen.dispose();
        shopScreen.dispose();
        statisticsScreen.dispose();
        inventoryUI.dispose();
        victoryScreen.dispose();
        settingsScreen.dispose();

        GLProfiler.disable();
	}

    /**
     * Logs profile for SpriteBatch calls
     */
	public void profile(String source) {
        System.out.println("Profiling " + source + "..." + "\n" +
            "  Drawcalls: " + GLProfiler.drawCalls +
            ", Calls: " + GLProfiler.calls +
            ", TextureBindings: " + GLProfiler.textureBindings +
            ", ShaderSwitches:  " + GLProfiler.shaderSwitches +
            " vertexCount: " + GLProfiler.vertexCount.value);
        GLProfiler.reset();
    }
	
	
	public SpriteBatch getBatch() {
        return batch;
    }
	
	public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }


}
