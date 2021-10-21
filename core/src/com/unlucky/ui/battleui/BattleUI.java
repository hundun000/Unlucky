package com.unlucky.ui.battleui;

import com.badlogic.gdx.math.MathUtils;
import com.unlucky.entity.enemy.Boss;
import com.unlucky.entity.enemy.Enemy;
import com.unlucky.entity.Player;
import com.unlucky.event.*;
import com.unlucky.map.TileMap;
import com.unlucky.resource.ResourceManager;
import com.unlucky.resource.Util;
import com.unlucky.screen.WorldScreen;
import com.unlucky.screen.game.WorldUI;
import com.unlucky.ui.UI;

/**
 * Handles all UI for battle scenes
 *
 * @author Ming Li
 */
public class BattleUI extends WorldUI {

    public BattleMoveUI battleMoveUI;
    public BattleDialogResultHandleUI battleDialogResultHandleUI;
    public BattleRoomUI battleRoomUI;

    // battle
    public BattleScreenState currentState;
    public BattleData battleData;
    
    public BattleUI(WorldScreen worldScreen, TileMap tileMap, Player player, ResourceManager rm) {
        super(worldScreen.getGame(), worldScreen, player, rm);
        
        currentState = BattleScreenState.NONE;
        
        battleData = new BattleData(worldScreen, tileMap, player);
        
        battleRoomUI = new BattleRoomUI(worldScreen, player, battleData, this, stage, rm);
        battleMoveUI = new BattleMoveUI(worldScreen, player, battleData, this, stage, rm);
        battleDialogResultHandleUI = new BattleDialogResultHandleUI(worldScreen, player, battleData, this, stage, rm);

        battleMoveUI.toggleMoveAndOptionUI(false);
        battleDialogResultHandleUI.dialogUI.endDialogList();
        
    }

    public void update(float dt) {
        if (currentState == BattleScreenState.MOVE) battleMoveUI.update(dt);
        if (currentState == BattleScreenState.DIALOG) battleDialogResultHandleUI.update(dt);
        battleRoomUI.update(dt);
    }

    public void render(float dt) {
        battleRoomUI.render(dt);

        stage.act(dt);
        stage.draw();

        if (currentState == BattleScreenState.MOVE) battleMoveUI.render(dt);
        if (currentState == BattleScreenState.DIALOG) battleDialogResultHandleUI.render(dt);
    }

    /**
     * When the player first encounters the enemy and engages in battle
     * There's a 1% chance that the enemy doesn't want to fight
     *
     * @param enemy
     */
    public void engage(Enemy enemy) {
        player.setDead(false);
        battleMoveUI.init();
        battleRoomUI.resetPositions();
        battleRoomUI.toggle(true);
        currentState = BattleScreenState.DIALOG;

        String[] intro;
        boolean saved = Util.isSuccess(Util.SAVED_FROM_BATTLE);

        if (enemy.isElite()) {
            player.stats.eliteEncountered++;
        }
        else if (enemy.isBoss()) {
            player.stats.bossEncountered++;
        }

        if (enemy.isBoss()) {
            if (MathUtils.randomBoolean()) {
                intro = new String[] {
                        "you encountered the boss " + enemy.getId() + "!",
                        "its power is far greater than any regular enemy.",
                        "Passive: " + ((Boss) enemy).getPassiveDescription()
                };
                battleDialogResultHandleUI.dialogUI.startDialog(intro, BattlePhase.NONE, BattlePhase.PLAYER_TURN);
            } else {
                intro = new String[] {
                        "you encountered the boss " + enemy.getId() + "!",
                        "its power is far greater than any regular enemy.",
                        "Passive: " + ((Boss) enemy).getPassiveDescription(),
                        enemy.getId() + " strikes first!"
                };
                battleDialogResultHandleUI.dialogUI.startDialog(intro, BattlePhase.NONE, BattlePhase.ENEMY_TURN);
            }
        }
        else {
            if (saved) {
                intro = new String[]{
                        "you encountered " + enemy.getId() + "! " +
                                "maybe there's a chance it doesn't want to fight...",
                        "the enemy stares at you and decides to flee the battle."
                };
                battleDialogResultHandleUI.dialogUI.startDialog(intro, BattlePhase.NONE, BattlePhase.END_BATTLE);
            } else {
                // 50-50 chance for first attack from enemy or player
                if (MathUtils.randomBoolean()) {
                    intro = new String[]{
                            "you encountered " + enemy.getId() + "! " +
                                    "maybe there's a chance it doesn't want to fight...",
                            "the enemy glares at you and decides to engage in battle!"
                    };
                    battleDialogResultHandleUI.dialogUI.startDialog(intro, BattlePhase.NONE, BattlePhase.PLAYER_TURN);
                } else {
                    intro = new String[]{
                            "you encountered " + enemy.getId() + "! " +
                                    "maybe there's a chance it doesn't want to fight...",
                            "the enemy glares at you and decides to engage in battle!",
                            enemy.getId() + " attacks first!"
                    };
                    battleDialogResultHandleUI.dialogUI.startDialog(intro, BattlePhase.NONE, BattlePhase.ENEMY_TURN);
                }
            }
        }
    }

    public void beginBattle(Enemy opponent) {
        battleData.begin(opponent);
    }

    public void endBattle() {
        battleData.end();
    }

    public void setTileMapToData(TileMap tileMap) {
        battleData.tileMap = tileMap;
    }

}
