package com.spearbothy.keyboardview;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by mahao on 17-6-16.
 */

public class KeyboardUtil implements KeyboardView.OnKeyboardActionListener {

    private final Context mContext;

    private final Activity mActivity;

    private View mKeyBoardLayout;

    private ScrollView mScrollView;

    private LinearLayout mRootView;


    private EditText mEditText;

    private int mScrollTo = 0;

    public boolean isShow = false;

    private int mInputType = -1;

    private KeyboardView mKeyboardView;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == mEditText.getId()) {
             /*   if (mScrollView != null)
                   // mScrollView.smoothScrollTo(0, mScrollTo);*/
            }
        }
    };

    public KeyboardUtil(Context ctx, LinearLayout rootView, ScrollView scrollView) {
        mContext = ctx;
        mActivity = (Activity) mContext;
        mScrollView = scrollView;
        mRootView = rootView;

        // 加载键盘布局,并添加到根试图
        mKeyBoardLayout = LayoutInflater.from(mContext).inflate(R.layout.layout_keyboard, rootView, false);
        mKeyBoardLayout.setVisibility(View.GONE);
        mRootView.addView(mKeyBoardLayout);
        mKeyboardView = (KeyboardView) mKeyBoardLayout.findViewById(R.id.keyboard);
        mKeyboardView.setOnKeyboardActionListener(this);
        mKeyBoardLayout.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditText = null;
                hideKeyboardLayout();
            }
        });
    }

    /**
     * 设置普通的输入框的触摸监听,
     *
     * @param editTexts
     */
    public void setNormalEditTextTouchListener(EditText... editTexts) {
        for (EditText editText : editTexts) {
            editText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        mEditText = (EditText) v;
                        hideKeyboardLayout();
                    }
                    return false;
                }
            });
        }
    }

    public void showKeyBoardLayout(final EditText editText, int keyBoardType) {

        if (isShow && editText.equals(mEditText)) {
            // 正在显示，且是相同editText ，不做处理
            return;
        } else if (isShow && !editText.equals(mEditText)) {
            // 正在显示，但输入框变化，则切换键盘类型
            switchKeyBoardType(editText, keyBoardType);
            return;
        }
        mInputType = keyBoardType;
        mEditText = editText;
        mScrollTo = calculateMoveHeight();

        if (hideSystemKeyboard(editText)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    show();
                }
            }, 400);
        } else {
            //直接显示
            show();
        }
    }

    public int calculateMoveHeight() {
        int[] location = new int[2];
        mEditText.getLocationOnScreen(location);
        return (location[1] + mKeyboardView.getMeasuredHeight() + mEditText.getHeight()) - mScrollView.getMeasuredHeight();
    }

    public void switchKeyBoardType(EditText editText, int type) {
        mEditText = editText;
        mScrollTo = calculateMoveHeight();
        mKeyboardView.replace(type);
        if (mScrollTo >= 0) {
            keyBoardScroll();
        }
    }

    private void show() {
        isShow = true;
        if (mKeyBoardLayout != null) {
            mKeyBoardLayout.setVisibility(View.VISIBLE);
        }
        mKeyboardView.replace(mInputType);
        //用于滑动
        if (mScrollTo >= 0) {
            keyBoardScroll();
        }
    }

    //滚动editText
    private void keyBoardScroll() {
        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Message msg = new Message();
                msg.what = mEditText.getId();
                mHandler.sendMessageDelayed(msg, 500);
                // // 防止多次促发
                mRootView.getViewTreeObserver()
                        .removeGlobalOnLayoutListener(this);
            }
        });
    }

    /**
     * 隐藏系统输入法，如果当前从显示到隐藏，返回true
     */
    public boolean hideSystemKeyboard(EditText edit) {
        boolean flag = false;
        InputMethodManager imm = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean isOpen = imm.isActive();// isOpen若返回true，则表示输入法打开
        if (isOpen) {
            if (imm.hideSoftInputFromWindow(edit.getWindowToken(), 0))
                flag = true;
        }
        return flag;
    }

    //新的隐藏方法
    public void hideKeyboardLayout() {
        if (isShow) {
            if (mKeyBoardLayout != null) {
                mKeyBoardLayout.setVisibility(View.GONE);
            }
            isShow = false;
        }
    }

    @Override
    public void onPress(Keyboard.Key key) {}

    @Override
    public void onRelease(Keyboard.Key key) {
        Editable editable = mEditText.getText();
        int start = mEditText.getSelectionStart();
        String temp = editable.subSequence(0, start) + key.label.toString() + editable.subSequence(start, editable.length());
        mEditText.setText(temp);
        mEditText.setSelection(start + 1);
    }

    @Override
    public void onDelete() {
        Editable editable = mEditText.getText();
        if (TextUtils.isEmpty(editable.toString())) {
            return;
        }
        int start = mEditText.getSelectionStart();
        String temp = editable.subSequence(0, start - 1) + "" + editable.subSequence(start, editable.length());
        mEditText.setText(temp);
        mEditText.setSelection(start - 1);
    }

    public void setSpecialListener(EditText editText, int type) {
        setEditTextInputFocus(editText, false);
        editText.setOnTouchListener(new KeyboardTouchListener(this, type));
    }

    /**
     * 禁止系统输入法
     */
    public void setEditTextInputFocus(EditText editText, boolean show) {
        if (Build.VERSION.SDK_INT >= 21) {
            editText.setShowSoftInputOnFocus(false);
            return;
        }
        int currentVersion = android.os.Build.VERSION.SDK_INT;
        String methodName = null;
        if (currentVersion >= 16) {
            // 4.2
            methodName = "setShowSoftInputOnFocus";
        } else if (currentVersion >= 14) {
            // 4.0
            methodName = "setSoftInputShownOnFocus";
        }
        Class<EditText> cls = EditText.class;
        Method setShowSoftInputOnFocus;
        try {
            setShowSoftInputOnFocus = cls.getMethod(methodName,
                    boolean.class);
            setShowSoftInputOnFocus.setAccessible(true);
            setShowSoftInputOnFocus.invoke(editText, show);
        } catch (NoSuchMethodException e) {
            editText.setInputType(InputType.TYPE_NULL);
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
