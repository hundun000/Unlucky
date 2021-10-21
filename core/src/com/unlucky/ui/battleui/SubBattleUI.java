package com.unlucky.ui.battleui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.unlucky.entity.Player;
import com.unlucky.event.BattleCoreLogic;
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

    protected BattleCoreLogic battleCoreLogic;
    protected BattleUI uiHandler;

    public SubBattleUI(WorldScreen worldScreen, Player player, BattleCoreLogic battleCoreLogic,
                    BattleUI uiHandler, Stage stage, ResourceManager rm) {
        super(worldScreen.getGame(), worldScreen, player, stage, rm);
        this.battleCoreLogic = battleCoreLogic;
        this.uiHandler = uiHandler;
    }

}
