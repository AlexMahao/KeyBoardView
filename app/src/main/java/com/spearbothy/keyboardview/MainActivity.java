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
    private EditText mNormalEt2;
    private EditText mSpecialEt2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNormalEt = (EditText) findViewById(R.id.normal);
        mNormalEt1 = (EditText) findViewById(R.id.normal1);
        mNormalEt2 = (EditText) findViewById(R.id.normal2);
        mSpecialEt = (EditText) findViewById(R.id.special);
        mSpecialEt2 = (EditText) findViewById(R.id.special2);
        mSpecialEt1 = (EditText) findViewById(R.id.special1);

        mRootView = (LinearLayout) findViewById(R.id.root_view);
        mScrollView = (ScrollView) findViewById(R.id.scroll_view);
        initKeyBoard();
    }

    @Override
    public void onBackPressed() {
        if (mKeyboardUtil.isShow) {
            mKeyboardUtil.hideKeyboardLayout();
            return;
        }
        super.onBackPressed();
    }

    private void initKeyBoard() {
        mKeyboardUtil = new KeyboardUtil(this, mRootView, mScrollView);
        mKeyboardUtil.setNormalEditTextTouchListener(mNormalEt, mNormalEt1);
        mKeyboardUtil.setSpecialListener(mSpecialEt, KeyboardView.TYPE_NUMBER);
        mKeyboardUtil.setSpecialListener(mSpecialEt1, KeyboardView.TYPE_NUMBER);
        mKeyboardUtil.setSpecialListener(mSpecialEt2, KeyboardView.TYPE_NUMBER);
    }
}
