package com.unlucky.ui.battleui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.unlucky.entity.Player;
import com.unlucky.event.BattleData;
import com.unlucky.map.TileMap;
import com.unlucky.resource.ResourceManager;
import com.unlucky.screen.WorldScreen;
import com.unlucky.screen.game.WorldUI;
import com.unlucky.ui.UI;

/**
 * Superclass for all UI related to battle events
 *
 * @author Ming Li
 */
public abstract class SubBattleUI extends WorldUI {

    protected BattleData battleData;
    protected BattleUI uiHandler;

    public SubBattleUI(WorldScreen worldScreen, Player player, BattleData battleData,
                    BattleUI uiHandler, Stage stage, ResourceManager rm) {
        super(worldScreen.getGame(), worldScreen, player, rm);
        this.battleData = battleData;
        this.uiHandler = uiHandler;
        this.stage = stage;
    }

}
