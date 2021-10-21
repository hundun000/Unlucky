package com.unlucky.ui.battleui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.unlucky.battle.StatusEffect;
import com.unlucky.entity.Player;
import com.unlucky.entity.enemy.Boss;
import com.unlucky.event.BattleData;
import com.unlucky.event.BattlePhase;
import com.unlucky.event.WorldState;
import com.unlucky.inventory.Item;
import com.unlucky.map.TileMap;
import com.unlucky.map.WeatherType;
import com.unlucky.resource.ResourceManager;
import com.unlucky.resource.Util;
import com.unlucky.screen.WorldScreen;
import com.unlucky.ui.battleui.DialogContent.TextCycleState;
import com.unlucky.ui.dialog.DialogUI;
import com.unlucky.ui.dialog.IDialogResultHandler;
import com.unlucky.event.BattleScreenState;

/**
 * Renders a dialog box that creates text animations given text
 * Text animations are initiated with arrays of Strings where each element represents
 * one cycle of animation and a transition from one element to the next resets the animation.
 *
 * Handles battle events after text events are finished.
 *
 * @author Ming Li
 */
public class BattleDialogResultHandleUI extends SubBattleUI implements IDialogResultHandler<BattlePhase> {

    public final DialogUI<BattlePhase> dialogUI;
    
    public BattleDialogResultHandleUI(WorldScreen worldScreen, Player player, BattleData battleData, BattleUI battleUI,
            Stage stage, ResourceManager rm) {
        super(worldScreen, player, battleData, battleUI, stage, rm);
        this.dialogUI = new DialogUI<>(this, worldScreen, player, stage, rm);
    }

    @Override
    public void handleDialogResult(BattlePhase event) {
        switch (event) {
            case NONE:
                return;
            case END_BATTLE:
                // update battle stats
                player.stats.updateMax(player.stats.maxDamageSingleBattle, battleData.cumulativeDamage);
                player.stats.updateMax(player.stats.maxHealSingleBattle, battleData.cumulativeHealing);
                battleData.cumulativeDamage = battleData.cumulativeHealing = 0;
                rm.battleTheme.stop();

                player.resetShield();
                battleData.resetBuffs();
                player.statusEffects.clear();
                worldScreen.setWorldState(WorldState.TRANSITION);
                worldScreen.transitionUI.start(WorldState.BATTLING, WorldState.MOVING);
                break;
            case PLAYER_TURN:
                uiHandler.battleMoveUI.toggleMoveAndOptionUI(true);
                uiHandler.currentState = BattleScreenState.MOVE;

                // sacrifice move sets player hp to 1
                if (battleData.buffs[Util.SACRIFICE]) {
                    battleData.psacrifice = ((player.getHp() - 1) / (float) player.getMaxHp()) + 1;
                    player.hit(player.getHp() - 1);
                    player.applyDamage();
                    worldScreen.battleUI.battleRoomUI.playerHudLabel.setText("HP: " + player.getHp() + "/" + player.getMaxHp());
                }

                if (dialogUI.prevEvent == BattlePhase.ENEMY_TURN) {
                    player.statusEffects.clear();
                    if (battleData.opponent.statusEffects.contains(StatusEffect.DMG_RED))
                        battleData.opponent.statusEffects.clearAllButMultiTurnEffects();
                    else
                        battleData.opponent.statusEffects.clear();

                    if (battleData.buffs[Util.REFLECT]) {
                        battleData.resetBuffs();
                        // double heal
                        if (battleData.opponent.getPrevMoveUsed() != null) {
                            battleData.opponent.applyHeal();
                        }
                        // damage move
                        else {
                            player.setMoveUsed(player.getPrevMoveUsed());
                            player.setPrevMoveUsed(null);
                            if (applyEnemyDamage()) return;
                        }
                    }
                    else {
                        if (applyPlayerDamage()) return;
                        battleData.opponent.applyHeal();
                    }
                }
                break;
            case ENEMY_TURN:
                if (dialogUI.prevEvent == BattlePhase.PLAYER_TURN) {
                    // shield
                    if (battleData.buffs[Util.SHIELD]) {
                        player.setShield((int) ((Util.P_SHIELD / 100f) * (float) player.getMaxHp()));
                    }
                    if (battleData.opponent.statusEffects.contains(StatusEffect.DMG_RED))
                        battleData.opponent.statusEffects.clearAllButSingleTurnEffects();
                    if (applyEnemyDamage()) return;
                    player.applyHeal();
                }
                String[] dialog = battleData.enemyTurn();
                dialogUI.startDialog(dialog, BattlePhase.ENEMY_TURN, BattlePhase.PLAYER_TURN);
                break;
            case LEVEL_UP:
                // update battle stats
                player.stats.updateMax(player.stats.maxDamageSingleBattle, battleData.cumulativeDamage);
                player.stats.updateMax(player.stats.maxHealSingleBattle, battleData.cumulativeHealing);
                battleData.cumulativeDamage = battleData.cumulativeHealing = 0;
                player.resetShield();
                player.statusEffects.clear();
                worldScreen.setWorldState(WorldState.LEVEL_UP);
                worldScreen.levelUpUI.start();
                rm.battleTheme.stop();
                break;
            case PLAYER_DEAD:
                // update battle stats
                player.stats.updateMax(player.stats.maxDamageSingleBattle, battleData.cumulativeDamage);
                player.stats.updateMax(player.stats.maxHealSingleBattle, battleData.cumulativeHealing);
                battleData.cumulativeDamage = battleData.cumulativeHealing = 0;
                rm.battleTheme.stop();
                player.inMap = false;

                player.resetShield();
                player.statusEffects.clear();
                worldScreen.setWorldState(WorldState.TRANSITION);
                worldScreen.transitionUI.start(WorldState.BATTLING, WorldState.DEATH);
                break;
        }
    }

    /**
     * Applies damage dealt to player and checks if they are dead
     *
     * @return
     */
    private boolean applyPlayerDamage() {
        player.applyDamage();
        worldScreen.battleUI.battleRoomUI.playerHudLabel.setText("HP: " + player.getHp() + "/" + player.getMaxHp());
        // player dead
        if (player.isDead()) {
            // reset animation
            battleData.opponent.setPrevMoveUsed(null);
            battleData.opponent.setMoveUsed(null);
            player.resetShield();
            battleData.resetBuffs();
            player.statusEffects.clear();

            uiHandler.battleMoveUI.toggleMoveAndOptionUI(false);
            uiHandler.currentState = BattleScreenState.DIALOG;
            // 1% chance for revival after dead
            if (Util.isSuccess(Util.REVIVAL)) {
                dialogUI.startDialog(new String[] {
                        "You took fatal damage and died!",
                        "However, it looks like luck was on your side and you revived!"
                }, BattlePhase.PLAYER_TURN, BattlePhase.PLAYER_TURN);
                player.setHp(player.getMaxHp());
                player.setDead(false);
                return true;
            }
            else {
                if (!player.settings.muteSfx) rm.death.play(player.settings.sfxVolume);
                dialogUI.startDialog(new String[] {
                        "Oh no, you took fatal damage and died!",
                        "You will lose " + Util.DEATH_PENALTY +
                            "% of your exp and gold and all the items obtained in this level as a penalty."
                }, BattlePhase.PLAYER_TURN, BattlePhase.PLAYER_DEAD);
                worldScreen.worldData.setDeath();

                player.stats.numDeaths++;
                return true;
            }
        }
        return false;
    }

    /**
     * Applies damage dealt to enemy and check if they are dead
     *
     * @return
     */
    private boolean applyEnemyDamage() {
        battleData.opponent.applyDamage();
        // enemy dead
        if (battleData.opponent.isDead()) {
            // reset animation
            player.setPrevMoveUsed(null);
            player.setMoveUsed(null);
            player.statusEffects.clear();
            battleData.resetBuffs();

            uiHandler.battleMoveUI.toggleMoveAndOptionUI(false);
            uiHandler.currentState = BattleScreenState.DIALOG;

            if (bossDeathEvents()) return true;

            // 1% chance for enemy revival (bosses can't revive)
            if (Util.isSuccess(Util.REVIVAL) && !battleData.opponent.isBoss()) {
                dialogUI.startDialog(new String[] {
                        "The enemy took fatal damage and died!",
                        "Oh no, it looks like the enemy has been revived!"
                }, BattlePhase.ENEMY_TURN, BattlePhase.ENEMY_TURN);
                battleData.opponent.setHp(battleData.opponent.getMaxHp());
                battleData.opponent.setDead(false);
                return true;
            }
            // defeated enemy and gained experience and gold
            // maybe the player gets an item
            else {
                if (!player.settings.muteSfx) rm.death.play(player.settings.sfxVolume);

                int expGained = battleData.getBattleExp();
                int goldGained = battleData.getGoldGained();
                Item itemGained = battleData.getItemObtained(rm);

                if (itemGained != null) {
                    player.stats.numItemsFromMonsters++;
                    if (itemGained.rarity == 0) player.stats.numCommonItems++;
                    else if (itemGained.rarity == 1) player.stats.numRareItems++;
                    else if (itemGained.rarity == 2) player.stats.numEpicItems++;
                    else if (itemGained.rarity == 3) player.stats.numLegendaryItems++;
                }

                // add things obtained to map record
                worldScreen.worldData.expObtained += expGained;
                worldScreen.worldData.goldObtained += goldGained;

                player.addGold(goldGained);
                player.stats.cumulativeGold += goldGained;
                player.stats.cumulativeExp += expGained;

                if (battleData.opponent.isElite()) player.stats.elitesDefeated++;
                else if (battleData.opponent.isBoss()) player.stats.bossesDefeated++;
                else player.stats.enemiesDefeated++;

                // level up occurs
                if (player.getExp() + expGained >= player.getMaxExp()) {
                    int remainder = (player.getExp() + expGained) - player.getMaxExp();
                    player.levelUp(remainder);
                    dialogUI.startDialog(new String[] {
                            "You defeated " + battleData.opponent.getId() + "!",
                            "You obtained " + goldGained + " gold.",
                            battleData.getItemDialog(itemGained),
                            "You gained " + expGained + " experience.",
                            "You leveled up!"
                    }, BattlePhase.ENEMY_TURN, BattlePhase.LEVEL_UP);
                    return true;
                }
                else {
                    player.addExp(expGained);
                    dialogUI.startDialog(new String[] {
                            "You defeated " + battleData.opponent.getId() + "!",
                            "You obtained " + goldGained + " gold.",
                            battleData.getItemDialog(itemGained),
                            "You gained " + expGained + " experience."
                    }, BattlePhase.ENEMY_TURN, BattlePhase.END_BATTLE);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * A boss may have a special death event
     *
     * @return
     */
    private boolean bossDeathEvents() {
        if (battleData.opponent.isBoss()) {
            Boss b = (Boss) battleData.opponent;

            // king slime respawn
            if (b.bossId == 0) {
                if (battleData.opponent.numRespawn + 1 < 4) {
                    battleData.opponent.numRespawn++;
                    // shrink king slime
                    battleData.opponent.battleSize -= 8;
                    battleData.opponent.setOnlyMaxHp((int) Math.ceil(battleData.opponent.getMaxHp() / 2));
                    battleData.opponent.setPreviousHp(0);
                    battleData.opponent.setHp(battleData.opponent.getMaxHp());
                    battleData.opponent.setDead(false);

                    dialogUI.startDialog(new String[] {
                            "King Slime respawned with half its health points!",
                            "It will respawn " + (3 - battleData.opponent.numRespawn) + " more time(s)!"
                    }, BattlePhase.ENEMY_TURN, BattlePhase.ENEMY_TURN);
                    return true;
                }
            }
        }
        return false;
    }



    @Override
    public void update(float dt) {
        dialogUI.update(dt);
    }
    
    @Override
    public void render(float dt) {
        dialogUI.render(dt);
    }



}
