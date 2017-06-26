package com.spearbothy.keyboardview;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by mahao on 17-6-16.
 */

public class KeyboardUtil implements KeyboardView.OnKeyboardActionListener {

    public static final int MODE_ADJUST_PAN = 0x20;
    public static final int MODE_ADJUST_RESIZE = 0x10;

    private static final int MESSAGE_SHOW = 1;

    private final Activity mActivity;

    private Context mContext;

    private ViewGroup mContentView;

    private View mKeyBoardLayout;

    private View mRootView;

    private EditText mEditText;

    private boolean isShow = false;

    private int mInputType = -1;

    private KeyboardView mKeyboardView;

    private int mMode;

    private int mMoveHeight;

    private View mMoveTargetView;

    private ObjectAnimator mStartAnim;

    private ObjectAnimator mEndAnim;

    private int mKeyBoardHeight;

    private Rect mDisplayRect;

    private Rect mOldDisplayRect;

    private boolean isDelayShowKeyboard;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_SHOW:
                    show(true);
                    break;
                default:
                    break;
            }
        }
    };
    private int mRootHeightInSystemKeyboard;
    private ScrollView mScrollView;

    public KeyboardUtil(Context ctx, Window window, int mode) {
        mContext = ctx;
        mMode = mode;
        mActivity = (Activity) ctx;
        mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN | mode);
        mContentView = (ViewGroup) window.getDecorView().findViewById(android.R.id.content);
        mRootView = mContentView.getChildAt(0);

        // 初始化键盘布局及位置
        initKeyBoard();
        initKeyBoardLocation();
    }

    /**
     * 加载自定义键盘布局,并添加到ContentView 中
     */
    private void initKeyBoard() {
        mKeyBoardLayout = LayoutInflater.from(mContext).inflate(R.layout.layout_keyboard, mContentView, false);
        mKeyBoardLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        View topLayoutView = mKeyBoardLayout.findViewById(R.id.top_layout);
        topLayoutView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getFractionByHeight(mContext, 0.063021f)));
        ((TextView) mKeyBoardLayout.findViewById(R.id.tip)).setTextSize(TypedValue.COMPLEX_UNIT_PX, getFractionByHeight(mContext, 0.01875f));
        mKeyboardView = (KeyboardView) mKeyBoardLayout.findViewById(R.id.keyboard);
        mKeyboardView.setOnKeyboardActionListener(this);
        TextView closeView = (TextView) mKeyBoardLayout.findViewById(R.id.close);
        closeView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getFractionByHeight(mContext, 0.021875f));
        closeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
            }
        });
        mKeyBoardLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        mKeyBoardHeight = mKeyBoardLayout.getMeasuredHeight();
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout
                .LayoutParams.MATCH_PARENT, mKeyBoardHeight);
        mContentView.addView(mKeyBoardLayout, params);
    }

    /**
     * 初始化键盘位置，获取屏幕可显示rect
     */
    private void initKeyBoardLocation() {
        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect display = new Rect();
                mRootView.getWindowVisibleDisplayFrame(display);
                if (mRootView.getMeasuredHeight() > display.bottom) {
                    return;
                }
                mDisplayRect = display;
                mRootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mKeyBoardLayout.setY(mDisplayRect.bottom);
                if (mMode == MODE_ADJUST_RESIZE) {
                    registerSystemKeyboardListener();
                }
                if (isDelayShowKeyboard) {
                    mHandler.sendEmptyMessageDelayed(MESSAGE_SHOW, 200);
                }
            }
        });
    }

    /**
     * 设置普通的输入框的触摸监听,用以隐藏自定义键盘
     */
    public void setNormalEditTextTouchListener(EditText... editTexts) {
        for (EditText editText : editTexts) {
            editText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        mEditText = (EditText) v;
                        hideKeyboardLayout(true);
                    }
                    return false;
                }
            });
        }
    }

    /**
     * 设置editText使用自定义键盘
     * @param editText       目标的editText
     * @param type           显示键盘的类型，{@link com.huli.keyboard.KeyboardView}
     * @param targetMoveView 偏移目标，可以为null， null时k以editText为目标
     */
    public void setSpecialListener(EditText editText, int type, View targetMoveView) {
        setEditTextInputFocus(editText, false);
        editText.setOnTouchListener(new KeyboardTouchListener(this, type, targetMoveView));
    }


    /**
     * 显示自定义键盘
     * @param editText       目标editText
     * @param keyBoardType   显示键盘的类型，{@link com.huli.keyboard.KeyboardView}
     * @param targetMoveView 偏移目标，可以为null， null时k以editText为目标
     */
    public void showKeyBoardLayout(final EditText editText, int keyBoardType, View targetMoveView) {
        if (isShow && editText.equals(mEditText)) {
            // 正在显示，且是相同editText ，不做处理
            return;
        } else if (isShow && !editText.equals(mEditText)) {
            // 正在显示，但输入框变化，则切换键盘类型
            switchKeyBoardType(editText, keyBoardType);
            return;
        }
        mMoveTargetView = targetMoveView;
        isShow = true;
        mInputType = keyBoardType;
        mEditText = editText;
        mEditText.requestFocus();
        mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        if (hideSystemKeyboard(editText)) {
            mHandler.sendEmptyMessageDelayed(MESSAGE_SHOW, 200);
        } else {
            show(false);
        }
    }

    /**
     * 当自定义键盘正在显示且切换editText时，切换自定义键盘类型
     * @param editText 获取焦点的editText
     * @param type     显示键盘的类型，{@link com.huli.keyboard.KeyboardView}
     */
    private void switchKeyBoardType(EditText editText, int type) {
        mEditText = editText;
        mKeyboardView.replace(type);
        mEditText.requestFocus();
    }

    /**
     * 显示键盘的方法
     */
    private void show(boolean isHideSystemKeyboard) {
        if (!isShow) {
            return;
        }
        if (mDisplayRect == null) {
            isDelayShowKeyboard = true;
            return;
        }
        if (mKeyBoardLayout != null) {
            startAnim();
            if (mMode == MODE_ADJUST_RESIZE && !isHideSystemKeyboard) {
                resizeView();
            } else if (mMode == MODE_ADJUST_PAN) {
                // adjustPan 模式下，设置rootView内部偏移
                mMoveHeight = getMoveHeight();
                if (mMoveHeight > 0) {
                    mRootView.scrollBy(0, mMoveHeight);
                }
            }
        }
        mKeyboardView.replace(mInputType);
    }

    /**
     * 获取需要偏移的高度
     * 仅在adjust_pan 模式使用
     */
    public int getMoveHeight() {
        int moveHeight = 0;
        int[] location = new int[2];
        if (mMoveTargetView == null) {
            mEditText.getLocationOnScreen(location);
            moveHeight = location[1] + mKeyBoardLayout.getMeasuredHeight() + mEditText.getHeight() - mDisplayRect.bottom;
        } else {
            mMoveTargetView.getLocationOnScreen(location);
            moveHeight = location[1] + mMoveTargetView.getMeasuredHeight() + mKeyBoardLayout.getMeasuredHeight() - mDisplayRect.bottom;
        }
        return moveHeight;
    }

    /**
     * 自定义键盘显示动画
     */
    private void startAnim() {
        if (mEndAnim == null || !mEndAnim.isRunning()) {
            mStartAnim = ObjectAnimator.ofFloat(mKeyBoardLayout, "y", mDisplayRect.bottom, mDisplayRect.bottom - mKeyBoardHeight - mDisplayRect.top);
        } else {
            mEndAnim.cancel();
            mStartAnim = ObjectAnimator.ofFloat(mKeyBoardLayout, "y", mKeyBoardLayout.getY(), mDisplayRect.bottom - mKeyBoardHeight - mDisplayRect.top);
        }

        mStartAnim.setDuration(200L);
        mStartAnim.start();
    }

    /**
     * 自定义键盘隐藏动画
     */
    private void endAnim() {
        if (mStartAnim == null || !mStartAnim.isRunning()) {
            mEndAnim = ObjectAnimator.ofFloat(mKeyBoardLayout, "y", mDisplayRect.bottom - mKeyBoardHeight - mDisplayRect.top, mDisplayRect.bottom);
        } else {
            mStartAnim.cancel();
            mEndAnim = ObjectAnimator.ofFloat(mKeyBoardLayout, "y", mKeyBoardLayout.getY(), mDisplayRect.bottom);
        }
        mEndAnim.setDuration(200L);
        mEndAnim.start();
    }

    /**
     * 隐藏系统输入法，如果当前从显示到隐藏，返回true
     */
    public boolean hideSystemKeyboard(EditText edit) {
        boolean flag = false;
        InputMethodManager imm = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            if (imm.hideSoftInputFromWindow(edit.getWindowToken(), 0)) {
                // 如果隐藏了输入法，adjust_resize 模式需要设置根节点高度
                if (mMode == MODE_ADJUST_RESIZE) {
                    resizeView();
                }
                flag = true;
            }
        }
        return flag;
    }

    public void resizeView() {
        setRootHeight(mDisplayRect.bottom - mDisplayRect.top - mKeyBoardLayout.getMeasuredHeight());
        if(mScrollView != null && mMoveTargetView != null) {
            mScrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mScrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mScrollView.smoothScrollTo(0, (int) (mMoveTargetView.getY() + mMoveTargetView.getMeasuredHeight()) - mScrollView.getMeasuredHeight());
                }
            });
        }
    }

    /**
     * 设置rootView 的高度，仅在adjust_resize时使用
     */
    public void setRootHeight(int height) {
        ViewGroup.LayoutParams params = mRootView.getLayoutParams();
        params.height = height;
        mRootView.setLayoutParams(params);
    }


    /**
     * 隐藏自定义输入法
     * @param isShowSystemKeyboard 是否显示系统输入法
     */
    void hideKeyboardLayout(boolean isShowSystemKeyboard) {
        if (isShow) {
            isShow = false;
            mActivity.getWindow().setSoftInputMode(mMode);
            mMoveTargetView = null;
            if (mMode == MODE_ADJUST_PAN) {
                endAnim();
                if (mMoveHeight > 0) {
                    mContentView.getChildAt(0).scrollBy(0, -mMoveHeight);
                }
            } else if (mMode == MODE_ADJUST_RESIZE) {
                if (mStartAnim != null && mStartAnim.isRunning()) {
                    mStartAnim.cancel();
                }
                mKeyBoardLayout.setY(mDisplayRect.bottom);
                if (!isShowSystemKeyboard) {
                    endAnim();
                    setRootHeight(mDisplayRect.bottom - mDisplayRect.top);
                }
            }

        }
    }

    /**
     * 隐藏自定义输入法
     * @return true 隐藏， false 未隐藏成功（当前自定义键盘未显示）
     */
    public boolean hideKeyboard() {
        if (isShow) {
            mHandler.removeMessages(MESSAGE_SHOW);
            hideKeyboardLayout(false);
            return true;
        }
        return false;
    }

    @Override
    public void onPress(Keyboard.Key key) {}

    @Override
    public void onRelease(Keyboard.Key key) {
        Editable editable = mEditText.getText();
        int start = mEditText.getSelectionStart();
        editable.insert(start, key.text);
    }

    @Override
    public void onDelete() {
        Editable editable = mEditText.getText();
        if (TextUtils.isEmpty(editable.toString())) {
            return;
        }
        int start = mEditText.getSelectionStart();
        if (start == 0) {
            return;
        }
        String temp = editable.subSequence(0, start - 1) + "" + editable.subSequence(start, editable.length());
        mEditText.setText(temp);
        mEditText.setSelection(start - 1);
    }

    /**
     * 监听系统键盘的显示隐藏，用以设置rootView的高度
     * 仅在adjust_resize时使用
     */
    public void registerSystemKeyboardListener() {
        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect display = new Rect();
                mRootView.getWindowVisibleDisplayFrame(display);
                if (mRootHeightInSystemKeyboard <= 0) {
                    if (mDisplayRect.bottom - display.bottom > 0) {
                        mRootHeightInSystemKeyboard = display.bottom - display.top;
                    }
                }
                if (mOldDisplayRect == null) {
                    mOldDisplayRect = display;
                    return;
                }
                if (mOldDisplayRect.bottom < mDisplayRect.bottom && display.bottom == mDisplayRect.bottom) {
                    // 系统键盘隐藏
                    if (!isShow) {
                        setRootHeight(mDisplayRect.bottom - mDisplayRect.top);
                    }
                }
                if (mOldDisplayRect.bottom == mDisplayRect.bottom && display.bottom < mDisplayRect.bottom) {
                    // 系统键盘显示
                    setRootHeight(mRootHeightInSystemKeyboard);
                }
                mOldDisplayRect = display;
            }
        });
    }

    /**
     * 禁止目标editText弹出系统输入法
     */
    public void setEditTextInputFocus(EditText editText, boolean show) {
        if (Build.VERSION.SDK_INT >= 21) {
            editText.setShowSoftInputOnFocus(false);
            return;
        }
        String methodName = null;
        if (Build.VERSION.SDK_INT >= 16) {
            methodName = "setShowSoftInputOnFocus";
        }
        Class<EditText> cls = EditText.class;
        Method setShowSoftInputOnFocus;
        try {
            setShowSoftInputOnFocus = cls.getMethod(methodName,
                    boolean.class);
            setShowSoftInputOnFocus.setAccessible(true);
            setShowSoftInputOnFocus.invoke(editText, show);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }


    public static int getFractionByWidth(Context context, float fraction) {
        return Math.round(fraction * context.getResources().getDisplayMetrics().widthPixels);
    }

    public static int getFractionByHeight(Context context, float fraction) {
        return Math.round(fraction * context.getResources().getDisplayMetrics().heightPixels);
    }

    /**
     * 当前键盘的目标editText，仅在显示自定义键盘时做比较
     */
    public void setEditText(EditText editText) {
        mEditText = editText;
    }

    /**
     * 设置用于滚动的ScrollView
     */
    public void setScrollView(ScrollView scrollView) {
        this.mScrollView = scrollView;
    }
}
