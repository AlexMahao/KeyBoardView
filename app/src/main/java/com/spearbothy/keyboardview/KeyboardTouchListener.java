package com.spearbothy.keyboardview;

import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

/**
 * Created by mahao on 17-6-14.
 */
public class KeyboardTouchListener implements View.OnTouchListener {
    private KeyboardUtil keyboardUtil;
    private int keyboardType = 1;
    private View targetMoveView;

    public KeyboardTouchListener(KeyboardUtil util, int keyboardType, View targetMoveView) {
        this.keyboardUtil = util;
        this.keyboardType = keyboardType;
        this.targetMoveView = targetMoveView;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (keyboardUtil != null) {
                keyboardUtil.showKeyBoardLayout((EditText) v, keyboardType, targetMoveView);
            }
        }
        return false;
    }
}
