package com.unlucky.ui.battleui;


/**
 * @author hundun
 * Created on 2021/10/21
 */
public class DialogList {
    
    public enum TextCycleState {
        IDLE,
        CYCLING,
        DONE,
        ;
    }
    
    private float appendFrameCount = 0;
    
    private String currentText = "";
    private String[] currentDialogList;
    private int dialogIndex = 0;
    
    private String resultingText = "";
    private int resultingEndIndex = 0;
    
    private TextCycleState cycleState;
    float speed;
    
    public DialogList(String[] dialog, float speed) {
        this.currentDialogList = dialog;
        currentText = currentDialogList[0];
        cycleState = TextCycleState.CYCLING;
        this.speed = speed;
    }

    public boolean hasNext() {
        return dialogIndex + 1 < currentDialogList.length;
    }

    public void completeShowCurrentDialog() {
        resultingText = currentText;
        cycleState = TextCycleState.DONE;
    }
    
    public void update(float dt) {

        if (cycleState == TextCycleState.CYCLING) {
            if (appendFrameCount > speed) {
                appendFrameCount = 0;
                resultingText = currentText.substring(0, resultingEndIndex);
                resultingEndIndex++;
            } else {
                appendFrameCount += dt;
            }
            
            if (resultingEndIndex >= currentText.length()) {
                cycleState = TextCycleState.DONE;
            }
        }

    }

    public void startNext() {
        dialogIndex++;
        appendFrameCount = 0;
        resultingText = "";
        resultingEndIndex = 0;
        currentText = currentDialogList[dialogIndex];
        cycleState = TextCycleState.CYCLING;
    }

    public TextCycleState getCycleState() {
        return cycleState;
    }
    
    public String getResultingText() {
        return resultingText;
    }
    
    
}
