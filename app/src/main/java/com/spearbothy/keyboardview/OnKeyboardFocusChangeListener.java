package com.spearbothy.keyboardview;

import android.view.View;
import android.widget.EditText;

/**
 * Created by mahao on 17-6-20.
 */

public class OnKeyboardFocusChangeListener implements View.OnFocusChangeListener {

    // 安全键盘
    public static final int TYPE_SPECIAL = 1;
    // 系统键盘
    public static final int TYPE_NORMAL = 2;

    private int type;

    private KeyboardUtil keyboardUtil;

    private View mTargetMoveView;

    private int mKeyBoardViewType;

    public static OnKeyboardFocusChangeListener getNormalListener(KeyboardUtil keyboardUtil, EditText editText) {
        return new OnKeyboardFocusChangeListener(TYPE_NORMAL, keyboardUtil, editText, -1, null);
    }

    public static OnKeyboardFocusChangeListener getSpecialListener(KeyboardUtil keyboardUtil, EditText editText, int keyboardType) {
        return new OnKeyboardFocusChangeListener(TYPE_SPECIAL, keyboardUtil, editText, keyboardType, null);
    }

    public static OnKeyboardFocusChangeListener getSpecialListener(KeyboardUtil keyboardUtil, EditText editText, int keyboardType, View targetMoveView) {
        return new OnKeyboardFocusChangeListener(TYPE_SPECIAL, keyboardUtil, editText, keyboardType, targetMoveView);
    }

    public OnKeyboardFocusChangeListener(int type, KeyboardUtil keyboardUtil, EditText editText, int keyboardType, View targetMoveView) {
        this.type = type;
        this.keyboardUtil = keyboardUtil;
        mKeyBoardViewType = keyboardType;
        mTargetMoveView = targetMoveView;
        if (type == TYPE_SPECIAL) {
            keyboardUtil.setSpecialListener(editText, keyboardType, mTargetMoveView);
        } else {
            keyboardUtil.setNormalEditTextTouchListener(editText);
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            if (type == TYPE_SPECIAL) {
                keyboardUtil.showKeyBoardLayout((EditText) v, mKeyBoardViewType, mTargetMoveView);
            } else {
                keyboardUtil.setEditText((EditText) v);
                keyboardUtil.hideKeyboardLayout(true);
            }
        }
    }
}
