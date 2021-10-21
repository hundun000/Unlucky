package com.unlucky.screen.game;

import com.unlucky.entity.Player;
import com.unlucky.main.Unlucky;
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
    
    public WorldUI(Unlucky game, WorldScreen worldScreen, Player player, ResourceManager rm) {
        super(game, player, worldScreen.getBatch(), rm);
        this.worldScreen = worldScreen;
    }



}
