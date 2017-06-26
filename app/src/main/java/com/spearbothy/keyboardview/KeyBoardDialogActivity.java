package com.spearbothy.keyboardview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by mahao on 17-6-20.
 */

public abstract class KeyBoardDialogActivity extends AppCompatActivity {

    private FrameLayout mLayout;
    protected KeyboardUtil mKeyboardUtil;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_dialog);
        mLayout = (FrameLayout) findViewById(R.id.layout);
        View view = LayoutInflater.from(this).inflate(getLayoutId(), mLayout, false);
        mLayout.setPadding(0,
                KeyboardUtil.getFractionByHeight(getApplicationContext(), 0.24f),
                0,
                KeyboardUtil.getFractionByHeight(getApplicationContext(), 0.02f));
        mLayout.addView(view);
        mKeyboardUtil = new KeyboardUtil(this, getWindow(), KeyboardUtil.MODE_ADJUST_PAN);
        init();
    }

    protected View getTargetMoveView() {
        return mLayout;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mKeyboardUtil.hideKeyboard()) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    // 获取布局文件
    public abstract int getLayoutId();

    // 初始化方法
    public abstract void init();
}
