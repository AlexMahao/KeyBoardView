package com.spearbothy.keyboardview;

import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

/**
 * Created by xuanweijian on 2016/3/31.
 */
public class KeyboardTouchListener implements View.OnTouchListener {
    private KeyboardUtil keyboardUtil;
    private int keyboardType = 1;
    private int scrollTo = -1;

    public KeyboardTouchListener(KeyboardUtil util, int keyboardType, int scrollTo) {
        this.keyboardUtil = util;
        this.keyboardType = keyboardType;
        this.scrollTo = scrollTo;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            //
            if(keyboardUtil!=null){
                keyboardUtil.showKeyBoardLayout((EditText) v, keyboardType, scrollTo);
            }
        }
        return false;
    }
}
