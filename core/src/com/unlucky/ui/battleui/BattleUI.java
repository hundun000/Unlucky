package com.unlucky.ui.battleui;

import com.badlogic.gdx.math.MathUtils;
import com.unlucky.entity.enemy.Boss;
import com.unlucky.entity.enemy.Enemy;
import com.unlucky.battle.StatusEffect;
import com.unlucky.entity.Player;
import com.unlucky.event.*;
import com.unlucky.inventory.Item;
import com.unlucky.map.TileMap;
import com.unlucky.resource.ResourceManager;
import com.unlucky.resource.Util;
import com.unlucky.screen.WorldScreen;
import com.unlucky.screen.game.WorldUI;
import com.unlucky.ui.UI;
import com.unlucky.ui.dialog.DialogUI;
import com.unlucky.ui.dialog.IDialogResultHandler;

/**
 * Handles all UI for battle scenes
 *
 * @author Ming Li
 */
public class BattleUI extends WorldUI implements IDialogResultHandler<BattlePhase> {

    public BattleMoveUI battleMoveUI;
    public BattleRoomUI battleRoomUI;
    public final DialogUI<BattlePhase> dialogUI;
    
    // battle
    public BattleScreenState currentState;
    public BattleCoreLogic battleCoreLogic;
    
    public BattleUI(WorldScreen worldScreen, TileMap tileMap, Player player, ResourceManager rm) {
        super(worldScreen.getGame(), worldScreen, player, rm);
        
        currentState = BattleScreenState.NONE;
        
        battleCoreLogic = new BattleCoreLogic(worldScreen, tileMap, player);
        
        battleRoomUI = new BattleRoomUI(worldScreen, player, battleCoreLogic, this, stage, rm);
        battleMoveUI = new BattleMoveUI(worldScreen, player, battleCoreLogic, this, stage, rm);
        this.dialogUI = new DialogUI<>(this, worldScreen, player, stage, rm);
        
        battleMoveUI.toggleMoveAndOptionUI(false);
        this.dialogUI.endDialogList();
        
    }

    public void update(float dt) {
        if (currentState == BattleScreenState.MOVE) battleMoveUI.update(dt);
        if (currentState == BattleScreenState.DIALOG) dialogUI.update(dt);
        battleRoomUI.update(dt);
    }

    public void render(float dt) {
        battleRoomUI.render(dt);

        stage.act(dt);
        stage.draw();

        if (currentState == BattleScreenState.MOVE) battleMoveUI.render(dt);
        if (currentState == BattleScreenState.DIALOG) dialogUI.render(dt);
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
                this.dialogUI.startDialog(intro, BattlePhase.NONE, BattlePhase.PLAYER_TURN);
            } else {
                intro = new String[] {
                        "you encountered the boss " + enemy.getId() + "!",
                        "its power is far greater than any regular enemy.",
                        "Passive: " + ((Boss) enemy).getPassiveDescription(),
                        enemy.getId() + " strikes first!"
                };
                this.dialogUI.startDialog(intro, BattlePhase.NONE, BattlePhase.ENEMY_TURN);
            }
        }
        else {
            if (saved) {
                intro = new String[]{
                        "you encountered " + enemy.getId() + "! " +
                                "maybe there's a chance it doesn't want to fight...",
                        "the enemy stares at you and decides to flee the battle."
                };
                this.dialogUI.startDialog(intro, BattlePhase.NONE, BattlePhase.END_BATTLE);
            } else {
                // 50-50 chance for first attack from enemy or player
                if (MathUtils.randomBoolean()) {
                    intro = new String[]{
                            "you encountered " + enemy.getId() + "! " +
                                    "maybe there's a chance it doesn't want to fight...",
                            "the enemy glares at you and decides to engage in battle!"
                    };
                    this.dialogUI.startDialog(intro, BattlePhase.NONE, BattlePhase.PLAYER_TURN);
                } else {
                    intro = new String[]{
                            "you encountered " + enemy.getId() + "! " +
                                    "maybe there's a chance it doesn't want to fight...",
                            "the enemy glares at you and decides to engage in battle!",
                            enemy.getId() + " attacks first!"
                    };
                    this.dialogUI.startDialog(intro, BattlePhase.NONE, BattlePhase.ENEMY_TURN);
                }
            }
        }
    }
    
    @Override
    public void handleDialogResult(BattlePhase event) {
        switch (event) {
            case NONE:
                return;
            case END_BATTLE:
                // update battle stats
                player.stats.updateMax(player.stats.maxDamageSingleBattle, battleCoreLogic.cumulativeDamage);
                player.stats.updateMax(player.stats.maxHealSingleBattle, battleCoreLogic.cumulativeHealing);
                battleCoreLogic.cumulativeDamage = battleCoreLogic.cumulativeHealing = 0;
                rm.battleTheme.stop();

                player.resetShield();
                battleCoreLogic.resetBuffs();
                player.statusEffects.clear();
                worldScreen.setWorldState(WorldState.TRANSITION);
                worldScreen.transitionUI.start(WorldState.BATTLING, WorldState.MOVING);
                break;
            case PLAYER_TURN:
                this.battleMoveUI.toggleMoveAndOptionUI(true);
                this.currentState = BattleScreenState.MOVE;

                // sacrifice move sets player hp to 1
                if (battleCoreLogic.buffs[Util.SACRIFICE]) {
                    battleCoreLogic.psacrifice = ((player.getHp() - 1) / (float) player.getMaxHp()) + 1;
                    player.hit(player.getHp() - 1);
                    player.applyDamage();
                    worldScreen.battleUI.battleRoomUI.playerHudLabel.setText("HP: " + player.getHp() + "/" + player.getMaxHp());
                }

                if (dialogUI.prevEvent == BattlePhase.ENEMY_TURN) {
                    player.statusEffects.clear();
                    if (battleCoreLogic.opponent.statusEffects.contains(StatusEffect.DMG_RED))
                        battleCoreLogic.opponent.statusEffects.clearAllButMultiTurnEffects();
                    else
                        battleCoreLogic.opponent.statusEffects.clear();

                    if (battleCoreLogic.buffs[Util.REFLECT]) {
                        battleCoreLogic.resetBuffs();
                        // double heal
                        if (battleCoreLogic.opponent.getPrevMoveUsed() != null) {
                            battleCoreLogic.opponent.applyHeal();
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
                        battleCoreLogic.opponent.applyHeal();
                    }
                }
                break;
            case ENEMY_TURN:
                if (dialogUI.prevEvent == BattlePhase.PLAYER_TURN) {
                    // shield
                    if (battleCoreLogic.buffs[Util.SHIELD]) {
                        player.setShield((int) ((Util.P_SHIELD / 100f) * (float) player.getMaxHp()));
                    }
                    if (battleCoreLogic.opponent.statusEffects.contains(StatusEffect.DMG_RED))
                        battleCoreLogic.opponent.statusEffects.clearAllButSingleTurnEffects();
                    if (applyEnemyDamage()) return;
                    player.applyHeal();
                }
                String[] dialog = battleCoreLogic.enemyTurn();
                dialogUI.startDialog(dialog, BattlePhase.ENEMY_TURN, BattlePhase.PLAYER_TURN);
                break;
            case LEVEL_UP:
                // update battle stats
                player.stats.updateMax(player.stats.maxDamageSingleBattle, battleCoreLogic.cumulativeDamage);
                player.stats.updateMax(player.stats.maxHealSingleBattle, battleCoreLogic.cumulativeHealing);
                battleCoreLogic.cumulativeDamage = battleCoreLogic.cumulativeHealing = 0;
                player.resetShield();
                player.statusEffects.clear();
                worldScreen.setWorldState(WorldState.LEVEL_UP);
                worldScreen.levelUpUI.start();
                rm.battleTheme.stop();
                break;
            case PLAYER_DEAD:
                // update battle stats
                player.stats.updateMax(player.stats.maxDamageSingleBattle, battleCoreLogic.cumulativeDamage);
                player.stats.updateMax(player.stats.maxHealSingleBattle, battleCoreLogic.cumulativeHealing);
                battleCoreLogic.cumulativeDamage = battleCoreLogic.cumulativeHealing = 0;
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
            battleCoreLogic.opponent.setPrevMoveUsed(null);
            battleCoreLogic.opponent.setMoveUsed(null);
            player.resetShield();
            battleCoreLogic.resetBuffs();
            player.statusEffects.clear();

            this.battleMoveUI.toggleMoveAndOptionUI(false);
            this.currentState = BattleScreenState.DIALOG;
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
                worldScreen.worldCoreLogic.setDeath();

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
        battleCoreLogic.opponent.applyDamage();
        // enemy dead
        if (battleCoreLogic.opponent.isDead()) {
            // reset animation
            player.setPrevMoveUsed(null);
            player.setMoveUsed(null);
            player.statusEffects.clear();
            battleCoreLogic.resetBuffs();

            this.battleMoveUI.toggleMoveAndOptionUI(false);
            this.currentState = BattleScreenState.DIALOG;

            if (bossDeathEvents()) return true;

            // 1% chance for enemy revival (bosses can't revive)
            if (Util.isSuccess(Util.REVIVAL) && !battleCoreLogic.opponent.isBoss()) {
                dialogUI.startDialog(new String[] {
                        "The enemy took fatal damage and died!",
                        "Oh no, it looks like the enemy has been revived!"
                }, BattlePhase.ENEMY_TURN, BattlePhase.ENEMY_TURN);
                battleCoreLogic.opponent.setHp(battleCoreLogic.opponent.getMaxHp());
                battleCoreLogic.opponent.setDead(false);
                return true;
            }
            // defeated enemy and gained experience and gold
            // maybe the player gets an item
            else {
                if (!player.settings.muteSfx) rm.death.play(player.settings.sfxVolume);

                int expGained = battleCoreLogic.getBattleExp();
                int goldGained = battleCoreLogic.getGoldGained();
                Item itemGained = battleCoreLogic.getItemObtained(rm);

                if (itemGained != null) {
                    player.stats.numItemsFromMonsters++;
                    if (itemGained.rarity == 0) player.stats.numCommonItems++;
                    else if (itemGained.rarity == 1) player.stats.numRareItems++;
                    else if (itemGained.rarity == 2) player.stats.numEpicItems++;
                    else if (itemGained.rarity == 3) player.stats.numLegendaryItems++;
                }

                // add things obtained to map record
                worldScreen.worldCoreLogic.expObtained += expGained;
                worldScreen.worldCoreLogic.goldObtained += goldGained;

                player.addGold(goldGained);
                player.stats.cumulativeGold += goldGained;
                player.stats.cumulativeExp += expGained;

                if (battleCoreLogic.opponent.isElite()) player.stats.elitesDefeated++;
                else if (battleCoreLogic.opponent.isBoss()) player.stats.bossesDefeated++;
                else player.stats.enemiesDefeated++;

                // level up occurs
                if (player.getExp() + expGained >= player.getMaxExp()) {
                    int remainder = (player.getExp() + expGained) - player.getMaxExp();
                    player.levelUp(remainder);
                    dialogUI.startDialog(new String[] {
                            "You defeated " + battleCoreLogic.opponent.getId() + "!",
                            "You obtained " + goldGained + " gold.",
                            battleCoreLogic.getItemDialog(itemGained),
                            "You gained " + expGained + " experience.",
                            "You leveled up!"
                    }, BattlePhase.ENEMY_TURN, BattlePhase.LEVEL_UP);
                    return true;
                }
                else {
                    player.addExp(expGained);
                    dialogUI.startDialog(new String[] {
                            "You defeated " + battleCoreLogic.opponent.getId() + "!",
                            "You obtained " + goldGained + " gold.",
                            battleCoreLogic.getItemDialog(itemGained),
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
        if (battleCoreLogic.opponent.isBoss()) {
            Boss b = (Boss) battleCoreLogic.opponent;

            // king slime respawn
            if (b.bossId == 0) {
                if (battleCoreLogic.opponent.numRespawn + 1 < 4) {
                    battleCoreLogic.opponent.numRespawn++;
                    // shrink king slime
                    battleCoreLogic.opponent.battleSize -= 8;
                    battleCoreLogic.opponent.setOnlyMaxHp((int) Math.ceil(battleCoreLogic.opponent.getMaxHp() / 2));
                    battleCoreLogic.opponent.setPreviousHp(0);
                    battleCoreLogic.opponent.setHp(battleCoreLogic.opponent.getMaxHp());
                    battleCoreLogic.opponent.setDead(false);

                    dialogUI.startDialog(new String[] {
                            "King Slime respawned with half its health points!",
                            "It will respawn " + (3 - battleCoreLogic.opponent.numRespawn) + " more time(s)!"
                    }, BattlePhase.ENEMY_TURN, BattlePhase.ENEMY_TURN);
                    return true;
                }
            }
        }
        return false;
    }



    public void beginBattle(Enemy opponent) {
        battleCoreLogic.begin(opponent);
    }

    public void endBattle() {
        battleCoreLogic.end();
    }

    public void setTileMapToData(TileMap tileMap) {
        battleCoreLogic.tileMap = tileMap;
    }

}
