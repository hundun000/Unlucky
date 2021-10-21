package com.unlucky.screen.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.unlucky.entity.Player;
import com.unlucky.event.WorldState;
import com.unlucky.map.Tile;
import com.unlucky.map.TileMap;
import com.unlucky.resource.ResourceManager;
import com.unlucky.resource.Util;
import com.unlucky.screen.WorldScreen;
import com.unlucky.ui.UI;
import com.unlucky.ui.dialog.DialogUI;
import com.unlucky.ui.dialog.IDialogResultHandler;

/**
 * Puts in a dialog box that handles events from the event state.
 * Basically the same as the BattleEventHandler but for map events
 *
 * @author Ming Li
 */
public class WorldDialogUI extends WorldUI implements IDialogResultHandler<WorldState> {

    public final DialogUI<WorldState> dialogUI;

    public WorldDialogUI(WorldScreen worldScreen, Player player, final ResourceManager rm) {
        super(worldScreen.getGame(), worldScreen, player, rm);

        this.dialogUI = new DialogUI<>(this, worldScreen, player, stage, rm);
    }

    @Override
    public void handleDialogResult(WorldState event) {
        switch (event) {
            case MOVING:
                player.finishTileInteraction();
                TextureRegion none = null;
                worldScreen.worldData.tileMap.setTile(worldScreen.worldData.tileMap.toTileCoords(player.getPosition()),
                        new Tile(-1, none, worldScreen.worldData.tileMap.toTileCoords(player.getPosition())));
                // player died from tile
                if (player.getHp() <= 0) {
                    worldScreen.worldData.setDeath();
                    worldScreen.die();
                    return;
                }
                worldScreen.setWorldState(WorldState.MOVING);
                worldScreen.hud.toggle(true);
                break;
            default:
        }
    }


    @Override
    public void update(float dt) {
        
    }


    @Override
    public void render(float dt) {
        // TODO Auto-generated method stub
        
    }

}