package com.unlucky.ui.dialog;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.unlucky.Unlucky;
import com.unlucky.entity.Player;
import com.unlucky.event.BattleCoreLogic;
import com.unlucky.event.BattlePhase;
import com.unlucky.event.WorldState;
import com.unlucky.resource.ResourceManager;
import com.unlucky.resource.Util;
import com.unlucky.screen.WorldScreen;
import com.unlucky.screen.game.WorldUI;
import com.unlucky.ui.battleui.BattleUI;
import com.unlucky.ui.dialog.DialogContent.TextCycleState;

/**
 * @author hundun
 * Created on 2021/10/22
 */
public class DialogUI<T> extends WorldUI {

    final IDialogResultHandler<T> resultHandler;
    
    // the ui for displaying text
    private Image ui;
    // Label for text animation
    private Label textLabel;
    // invisible Label for clicking the window
    private Label clickLabel;

    // text animation
    private DialogContent dialogContent;

//    private boolean beginCycle = false;
//    private boolean endCycle = false;
    public T prevEvent;
    public T nextEvent;

    // creates the blinking triangle effect when text is done animating
    private boolean posSwitch = false;
    private float posTime = 0;

    public DialogUI(IDialogResultHandler<T> resultHandler, WorldScreen worldScreen, Player player, Stage stage, final ResourceManager rm) {
        super(worldScreen.getGame(), worldScreen, player, rm);

        this.resultHandler = resultHandler;
        // create main UI
        ui = new Image(rm.dialogBox400x80);
        ui.setSize(200, 40);
        ui.setPosition(0, 0);
        ui.setTouchable(Touchable.disabled);

        stage.addActor(ui);

        // create Labels
        BitmapFont bitmapFont = rm.pixel10;
        Label.LabelStyle font = new Label.LabelStyle(bitmapFont, new Color(0, 0, 0, 255));

        textLabel = new Label("", font);
        textLabel.setWrap(true);
        textLabel.setTouchable(Touchable.disabled);
        textLabel.setFontScale(1.7f / 2);
        textLabel.setPosition(8, 6);
        textLabel.setSize(175, 26);
        textLabel.setAlignment(Align.topLeft);
        stage.addActor(textLabel);

        clickLabel = new Label("", font);
        clickLabel.setSize(200, 120);
        clickLabel.setPosition(0, 0);

        final Player p = player;
        clickLabel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (dialogContent == null) {
                    return;
                }
                switch (dialogContent.getCycleState()) {
                    case DONE:
                        if (!dialogContent.hasNext()) {
                            if (!p.settings.muteSfx) {
                                rm.textprogression.play(p.settings.sfxVolume);
                            }
                            // the text animation has run through every element of the text array
                            endDialogList();
                            resultHandler.handleDialogResult(nextEvent);
                        // after a cycle of text animation ends, clicking the UI goes to the next cycle
                        } else {
                            if (!p.settings.muteSfx) {
                                rm.textprogression.play(p.settings.sfxVolume);
                            }
                            dialogContent.startNext();
                            
                        }
                        break;
                    // clicking on the box during a text animation completes it early
                    case CYCLING: 
                        dialogContent.completeShowCurrentDialog();
                        textLabel.setText(dialogContent.getResultingText());
                    default:
                        break;
                }
            }
        });
        stage.addActor(clickLabel);
    }

    /**
     * Starts the text animation process given an array of Strings
     * Also takes in a BattleEvent that is called after the dialog is done
     *
     * @param dialog
     * @param next
     */
    public void startDialog(String[] dialog, T prev, T next) {
        ui.setVisible(true);
        textLabel.setVisible(true);
        clickLabel.setVisible(true);
        clickLabel.setTouchable(Touchable.enabled);

        dialogContent = new DialogContent(dialog, Util.TEXT_SPEED);

        prevEvent = prev;
        nextEvent = next;
        
    }

    public void endDialogList() {
        dialogContent = null;
        textLabel.setText("");
        ui.setVisible(false);
        textLabel.setVisible(false);
        clickLabel.setVisible(false);
        clickLabel.setTouchable(Touchable.disabled);
    }



    public void update(float dt) {
        if (dialogContent != null) {
            dialogContent.update(dt);
            textLabel.setText(dialogContent.getResultingText());
        }
    }

    public void render(float dt) {
        // special ui for State.DONE
        if (dialogContent != null && dialogContent.getCycleState() == TextCycleState.DONE) {
            // blinking indicator
            posTime += dt;
            if (posTime >= 0.5f) {
                posTime = 0;
                posSwitch = !posSwitch;
            }

            this.stage.getBatch().setProjectionMatrix(stage.getCamera().combined);
            this.stage.getBatch().begin();
            // render red arrow to show when a text animation cycle is complete
            if (posSwitch) {
                this.stage.getBatch().draw(rm.redarrow10x9, 182, 10);
            }
            else {
                this.stage.getBatch().draw(rm.redarrow10x9, 182, 12);
            }
            this.stage.getBatch().end();
        }
    }
    
}
