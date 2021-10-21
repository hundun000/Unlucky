package com.unlucky.screen.game;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.unlucky.Unlucky;
import com.unlucky.entity.Player;
import com.unlucky.resource.ResourceManager;
import com.unlucky.screen.WorldScreen;
import com.unlucky.ui.UI;

/**
 * for all UI which need member worldScreen
 * @author hundun
 * Created on 2021/10/22
 */
public abstract class WorldUI extends UI {

    protected WorldScreen worldScreen;
    protected final Player player;
    
    public WorldUI(Unlucky game, WorldScreen worldScreen, Player player, ResourceManager rm) {
        this(game, worldScreen, player, new Stage(new StretchViewport(Unlucky.V_WIDTH, Unlucky.V_HEIGHT, new OrthographicCamera()), worldScreen.getBatch()), rm);
    }

    public WorldUI(Unlucky game, WorldScreen worldScreen, Player player, Stage stage, ResourceManager rm) {
        super(game, game.getBatch(), stage, rm, game.getShapeRenderer());
        this.worldScreen = worldScreen;
        this.player = player;
    }

}
