package com.spearbothy.keyboardview.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import com.spearbothy.keyboardview.KeyboardUtil;
import com.spearbothy.keyboardview.KeyboardView;
import com.spearbothy.keyboardview.OnKeyboardFocusChangeListener;
import com.spearbothy.keyboardview.R;

public class MainActivity extends AppCompatActivity {
    private EditText mNormalEt;
    private EditText mSpecialEt;
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
        initKeyBoard();
    }

    @Override
    public void onBackPressed() {
        if (mKeyboardUtil.hideKeyboard()) {
            return;
        }
        super.onBackPressed();
    }

    private void initKeyBoard() {
        mKeyboardUtil = new KeyboardUtil(this, getWindow(), KeyboardUtil.MODE_ADJUST_RESIZE);
        mNormalEt.setOnFocusChangeListener(
                OnKeyboardFocusChangeListener.getNormalListener(mKeyboardUtil, mNormalEt)
        );
        mNormalEt1.setOnFocusChangeListener(
                OnKeyboardFocusChangeListener.getNormalListener(mKeyboardUtil, mNormalEt1)
        );
        mNormalEt2.setOnFocusChangeListener(
                OnKeyboardFocusChangeListener.getNormalListener(mKeyboardUtil, mNormalEt2)
        );
        mSpecialEt.setOnFocusChangeListener(
                OnKeyboardFocusChangeListener.getSpecialListener(mKeyboardUtil, mSpecialEt, KeyboardView.TYPE_NUMBER)
        );
        mSpecialEt1.setOnFocusChangeListener(
                OnKeyboardFocusChangeListener.getSpecialListener(mKeyboardUtil, mSpecialEt1, KeyboardView.TYPE_NUMBER)
        );
        mSpecialEt2.setOnFocusChangeListener(
                OnKeyboardFocusChangeListener.getSpecialListener(mKeyboardUtil, mSpecialEt2, KeyboardView.TYPE_NUMBER)
        );
    }

}
