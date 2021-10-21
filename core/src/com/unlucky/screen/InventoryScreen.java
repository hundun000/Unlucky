package com.unlucky.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.unlucky.Unlucky;
import com.unlucky.resource.ResourceManager;
import com.unlucky.ui.inventory.InventoryUI;

/**
 * Screen that shows the player's inventory, equips, and stats
 * and allows the player to enchant, sell, and manage items.
 *
 * @author Ming Li
 */
public class InventoryScreen extends MenuExtensionScreen {

 // UI shared between Screens
    public InventoryUI inventoryUI;
    
    public InventoryScreen(final Unlucky game, final ResourceManager rm) {
        super(game, rm);
    }

    @Override
    public void show() {
        game.fps.setPosition(2, 2);
        stage.addActor(game.fps);

        Gdx.input.setInputProcessor(stage);
        renderBatch = true;
        batchFade = true;

        stage.addAction(Actions.sequence(Actions.moveTo(-Unlucky.V_WIDTH, 0),
            Actions.moveTo(0, 0, 0.3f),
            Actions.run(new Runnable() {
                @Override
                public void run() {
                    game.inventoryUI.renderHealthBars = true;
                }
            })));

        game.inventoryUI.initForNewScreen(true);
        game.inventoryUI.start();
    }

    public void update(float dt) {
        game.inventoryUI.update(dt);
    }

    public void render(float dt) {
        super.render(dt);
        if (renderBatch) game.inventoryUI.render(dt);
    }

}
