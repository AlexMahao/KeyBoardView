package com.spearbothy.keyboardview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class MainActivity extends AppCompatActivity {

    private EditText mNormalEt;
    private EditText mSpecialEt;
    private LinearLayout mRootView;
    private ScrollView mScrollView;
    private KeyboardUtil mKeyboardUtil;
    private EditText mNormalEt1;
    private EditText mSpecialEt1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNormalEt = (EditText) findViewById(R.id.normal);
        mNormalEt1 = (EditText) findViewById(R.id.normal1);
        mSpecialEt = (EditText) findViewById(R.id.special);
        mSpecialEt1 = (EditText) findViewById(R.id.special1);

        mRootView = (LinearLayout) findViewById(R.id.root_view);
        mScrollView = (ScrollView) findViewById(R.id.scroll_view);
        initKeyBoard();
    }


    @Override
    public void onBackPressed() {
        if(mKeyboardUtil.isShow) {
            mKeyboardUtil.hideKeyboardLayout();
            return;
        }
        super.onBackPressed();
    }

    private void initKeyBoard() {
        mKeyboardUtil = new KeyboardUtil(this, mRootView, mScrollView);
        mKeyboardUtil.setNormalEditTextTouchListener(mNormalEt, mNormalEt1);
        mSpecialEt.setOnTouchListener(new KeyboardTouchListener(mKeyboardUtil, KeyboardView.TYPE_NUMBER, -1));
        mSpecialEt1.setOnTouchListener(new KeyboardTouchListener(mKeyboardUtil, KeyboardView.TYPE_ENGLISH_LOWER, -1));
    }
}
