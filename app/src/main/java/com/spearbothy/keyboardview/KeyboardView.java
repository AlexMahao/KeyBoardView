package com.spearbothy.keyboardview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mahao on 17-6-14.
 */

public class KeyboardView extends View {
    public static final String TAG = KeyboardView.class.getSimpleName();

    public static final int TYPE_NUMBER = 0;
    public static final int TYPE_ENGLISH_LOWER = 1;
    public static final int TYPE_ENGLISH_UPPER = 2;
    public static final int TYPE_SYMBOL = 3;
    public static final int TYPE_IDENTITY_CARD = 4;

    private SparseArray<Boolean> mDelayRandom = new SparseArray<>();

    private Map<Integer, Keyboard> mKeyboardMap = new HashMap<>();

    private static final int MSG_REPEAT = 2;
    private static final int MSG_REPEAT_DELAY = 400;

    private static final int REPEAT_MIN_TIME = 50;
    private static final int REPEAT_TIME_UNIT = 50;
    private static final int REPEAT_MAX_TIME = 200;

    private NinePatch mKeyPressLightPatch;
    private NinePatch mKeyPressNormalPatch;
    private NinePatch mKeyPressPressPatch;
    // preview View
    private PopupWindow mPreviewPopup;
    private TextView mPreviewText;
    private int mPreviewOffsetHeight;
    private int mPreviewOffsetWidth;
    private int mPreviewHeight;
    private int mPreviewWidth;

    private Keyboard mKeyboard;

    private Paint mTextPaint;
    private Keyboard.Key mPressKey;
    private int mType;

    private OnKeyboardActionListener mOnKeyboardActionListener;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // 删除按钮事件
            switch (msg.what) {
                case MSG_REPEAT:
                    onRelease(mPressKey);
                    Message repeat = Message.obtain(this, MSG_REPEAT);
                    int time = (int) msg.obj;
                    int delayTime = time;
                    time -= REPEAT_TIME_UNIT;
                    time = time < REPEAT_MIN_TIME ? REPEAT_MIN_TIME : time;
                    repeat.obj = time;
                    sendMessageDelayed(repeat, delayTime);
                    break;
            }
        }
    };
    private ValueAnimator mRandomAnim;

    public KeyboardView(Context context) {
        this(context, null);
    }

    public KeyboardView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KeyboardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.KeyboardView);
        int type = a.getInt(R.styleable.KeyboardView_type, 0);
        a.recycle();
        // 初始化键盘类型
        replace(type, false);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.keyboard_press_normal);
        mKeyPressNormalPatch = new NinePatch(bitmap, bitmap.getNinePatchChunk(), null);

        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.keyboard_press_light);
        mKeyPressLightPatch = new NinePatch(bitmap, bitmap.getNinePatchChunk(), null);

        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.keyboard_press_press);
        mKeyPressPressPatch = new NinePatch(bitmap, bitmap.getNinePatchChunk(), null);

        mTextPaint = new Paint();
        mTextPaint.setStrokeWidth(3);
        mTextPaint.setTextSize(KeyboardUtil.getFractionByHeight(getContext(), 0.025f));
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        mPreviewText = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.layout_preview_key, null);
        mPreviewText.setPadding(0, KeyboardUtil.getFractionByHeight(getContext(), 0.019792f), 0, 0);
        mPreviewText.setTextSize(TypedValue.COMPLEX_UNIT_PX, KeyboardUtil.getFractionByHeight(getContext(), 0.0375f));
        mPreviewPopup = new PopupWindow(context);
        mPreviewPopup.setContentView(mPreviewText);
    }

    public void replace(int type) {
        replace(type, true);
    }

    public void replace(int type, boolean isShow) {
        if (isShow) {
            mDelayRandom.clear();
        }
        Keyboard keyboard = mKeyboardMap.get(type);
        if (keyboard == null) {
            if (type == TYPE_NUMBER) {
                keyboard = new Keyboard(getContext(), R.xml.number_keyboard);
            } else if (type == TYPE_ENGLISH_LOWER) {
                keyboard = new Keyboard(getContext(), R.xml.english_lower_keyboard);
            } else if (type == TYPE_SYMBOL) {
                keyboard = new Keyboard(getContext(), R.xml.symbol_keyboard);
            } else if (type == TYPE_ENGLISH_UPPER) {
                keyboard = new Keyboard(getContext(), R.xml.english_upper_keyboard);
            } else if (type == TYPE_IDENTITY_CARD) {
                keyboard = new Keyboard(getContext(), R.xml.identity_card_keyboard);
            }
            mKeyboardMap.put(type, keyboard);
        }
        mKeyboard = keyboard;
        if (mDelayRandom.get(type, true)) {
            mKeyboard.random();
            if (mKeyboard.isAnimation()) {
                randomAnim();
            } else {
                invalidate();
            }
            mDelayRandom.put(type, false);
        } else {
            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mKeyboard.getDisplayWidth(), mKeyboard.getHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Keyboard.Key key : mKeyboard.getKeys()) {
            drawKeyBg(key, canvas);
            if (key.icon != null) {
                drawIcon(key, canvas);
            } else {
                drawText(key, canvas);
            }
        }
    }

    private void drawIcon(Keyboard.Key key, Canvas canvas) {
        canvas.save();
        final int drawableX = (key.width - key.icon.getIntrinsicWidth()) / 2 + key.drawX;
        final int drawableY = (key.height - key.icon.getIntrinsicHeight()) / 2 + key.drawY;
        canvas.translate(drawableX, drawableY);
        key.icon.draw(canvas);
        canvas.restore();
    }

    private void drawKeyBg(Keyboard.Key key, Canvas canvas) {
        RectF rectF = new RectF(key.drawX, key.drawY, key.drawX + key.width, key.drawY + key.height);
        if (key.checked) {
            mKeyPressPressPatch.draw(canvas, rectF);
            return;
        }
        if (key.pressed) {
            mKeyPressPressPatch.draw(canvas, rectF);
        } else {
            if (key.style == Keyboard.Key.STYLE_NORMAL) {
                mKeyPressNormalPatch.draw(canvas, rectF);
            } else if (key.style == Keyboard.Key.STYLE_LIGHT) {
                mKeyPressLightPatch.draw(canvas, rectF);
            }
        }
    }

    public void drawText(Keyboard.Key key, Canvas canvas) {
        Rect bounds = new Rect();
        mTextPaint.getTextBounds(String.valueOf(key.label), 0, String.valueOf(key.label).length(), bounds);
        Paint.FontMetricsInt fontMetrics = mTextPaint.getFontMetricsInt();
        int baseline = key.drawY + (key.height - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;
        canvas.drawText(String.valueOf(key.label),
                key.drawX + key.width / 2 - bounds.width() / 2,
                baseline - KeyboardUtil.getFractionByHeight(getContext(), 0.002605f),
                mTextPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPressKey = calculateLocation(event.getX(), event.getY());
                if (mPressKey == null) {
                    return true;
                }
                if (mPressKey.isRepeat) {
                    Message msg = mHandler.obtainMessage(MSG_REPEAT);
                    msg.obj = REPEAT_MAX_TIME;
                    mHandler.sendMessageDelayed(msg, MSG_REPEAT_DELAY);
                    onRelease(mPressKey);
                } else {
                    onPress(mPressKey);
                }
                mPressKey.pressed = true;
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mPressKey == null) {
                    return true;
                }
                if (mPressKey.isRepeat) {
                    mHandler.removeMessages(MSG_REPEAT);
                } else {
                    onRelease(mPressKey);
                }

                mPressKey.pressed = false;
                mPressKey = null;
                invalidate();
                break;
        }
        return true;
    }

    public void onPress(Keyboard.Key key) {
        showPopupWindow(key);
        if (mOnKeyboardActionListener != null) {
            mOnKeyboardActionListener.onPress(key);
        }
    }

    public void onRelease(Keyboard.Key key) {
        if (mPreviewPopup != null && mPreviewPopup.isShowing()) {
            mPreviewPopup.dismiss();
        }
        if (key.code < 0) {
            processKey(key.code);
        } else if (key.code == 0) {
            if (mOnKeyboardActionListener != null) {
                mOnKeyboardActionListener.onDelete();
            }
        } else {
            if (mOnKeyboardActionListener != null) {
                mOnKeyboardActionListener.onRelease(key);
                if (mKeyboard.isClickRandom() && mKeyboard.isRandom()) {
                    mKeyboard.random();
                    if (mKeyboard.isAnimation()) {
                        randomAnim();
                    } else {
                        invalidate();
                    }
                } else {
                    invalidate();
                }

            }
        }
    }

    private void randomAnim() {
        if (mRandomAnim == null) {
            mRandomAnim = ValueAnimator.ofFloat(0, 200);
            mRandomAnim.setDuration(200);
            mRandomAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float fraction = animation.getAnimatedFraction();
                    Log.i("KeyBoardView", fraction + "");
                    for (Keyboard.Key key : mKeyboard.getKeys()) {
                        if (key.isRandom) {
                            key.drawX = (int) ((key.x - key.fromX) * fraction + key.fromX);
                            key.drawY = (int) ((key.y - key.fromY) * fraction + key.fromY);
                        }
                    }
                    invalidate();
                }
            });
        }
        if (mRandomAnim.isRunning()) {
            mRandomAnim.end();
        }
        mRandomAnim.start();
    }

    private void processKey(int code) {
        switch (code) {
            case Keyboard.KEYCODE_MODE_CHANGE_TO_ENGLISH_LOWER:
                replace(TYPE_ENGLISH_LOWER, false);
                break;
            case Keyboard.KEYCODE_MODE_CHANGE_TO_UPPER:
                replace(TYPE_ENGLISH_UPPER, false);
                break;
            case Keyboard.KEYCODE_MODE_CHANGE_TO_NUMBER:
                replace(TYPE_NUMBER, false);
                break;
            case Keyboard.KEYCODE_MODE_CHANGE_TO_SYMBOL:
                replace(TYPE_SYMBOL, false);
                break;
        }
        invalidate();
    }

    public void showPopupWindow(Keyboard.Key key) {
        mPreviewText.setText(key.label);
        int[] mCoordinates = new int[2];
        getLocationInWindow(mCoordinates);
        int x = 0;
        int y = 0;
        switch (key.previewDir) {
            case Keyboard.Key.PREVIEW_DIR_LEFT:
                mPreviewHeight = KeyboardUtil.getFractionByHeight(getContext(), 0.164584f);
                mPreviewWidth = KeyboardUtil.getFractionByWidth(getContext(), 0.162963f);
                mPreviewOffsetHeight = KeyboardUtil.getFractionByHeight(getContext(), 0.007813f);
                mPreviewOffsetWidth = KeyboardUtil.getFractionByWidth(getContext(), 0.0138889f);
                mPreviewPopup.setBackgroundDrawable(getResources().getDrawable(R.drawable.keyboard_preview_left));
                mPreviewPopup.setWidth(mPreviewWidth);
                mPreviewPopup.setHeight(mPreviewHeight);
                y = mCoordinates[1] + key.y - (mPreviewHeight - mKeyboard.getKeyHeight()) + mPreviewOffsetHeight;
                x = key.x - mPreviewOffsetWidth;
                mPreviewPopup.showAtLocation(this, Gravity.NO_GRAVITY, x, y);
                break;
            case Keyboard.Key.PREVIEW_DIR_CENTER:
                mPreviewHeight = KeyboardUtil.getFractionByHeight(getContext(), 0.164584f);
                mPreviewWidth = KeyboardUtil.getFractionByWidth(getContext(), 0.178704f);
                mPreviewOffsetHeight = KeyboardUtil.getFractionByHeight(getContext(), 0.007813f);
                mPreviewOffsetWidth = KeyboardUtil.getFractionByWidth(getContext(), 0.046297f);
                mPreviewPopup.setBackgroundDrawable(getResources().getDrawable(R.drawable.keyboard_preview_center));
                mPreviewPopup.setWidth(mPreviewWidth);
                mPreviewPopup.setHeight(mPreviewHeight);
                y = mCoordinates[1] + key.y - (mPreviewHeight - mKeyboard.getKeyHeight()) + mPreviewOffsetHeight;
                x = key.x - mPreviewOffsetWidth;
                mPreviewPopup.showAtLocation(this, Gravity.NO_GRAVITY, x, y);
                break;
            case Keyboard.Key.PREVIEW_DIR_RIGHT:
                mPreviewHeight = KeyboardUtil.getFractionByHeight(getContext(), 0.164584f);
                mPreviewWidth = KeyboardUtil.getFractionByWidth(getContext(), 0.162963f);
                mPreviewOffsetHeight = KeyboardUtil.getFractionByHeight(getContext(), 0.007813f);
                mPreviewOffsetWidth = KeyboardUtil.getFractionByWidth(getContext(), 0.063889f);
                mPreviewPopup.setBackgroundDrawable(getResources().getDrawable(R.drawable.keyboard_preview_right));
                mPreviewPopup.setWidth(mPreviewWidth);
                mPreviewPopup.setHeight(mPreviewHeight);
                y = mCoordinates[1] + key.y - (mPreviewHeight - mKeyboard.getKeyHeight()) + mPreviewOffsetHeight;
                x = key.x - mPreviewOffsetWidth;
                mPreviewPopup.showAtLocation(this, Gravity.NO_GRAVITY, x, y);
                break;
            default:
                break;
        }
    }


    public void setOnKeyboardActionListener(OnKeyboardActionListener onKeyboardActionListener) {
        this.mOnKeyboardActionListener = onKeyboardActionListener;
    }

    private Keyboard.Key calculateLocation(float x, float y) {
        Keyboard.Row row = null;
        Keyboard.Key key = null;
        float touchX = x;
        float touchY = y;
        for (int i = 0; i < mKeyboard.getRows().size(); i++) {
            row = mKeyboard.getRows().get(i);
            if (y < row.y + row.defaultHeight) {
                y = y - row.verticalGap / 2;
                if (y < row.y - row.verticalGap) {
                    if (i == 0) {
                        return null;
                    }
                    row = mKeyboard.getRows().get(i - 1);
                }
                break;
            }
        }
        for (int i = 0; i < row.mKeys.size(); i++) {
            key = row.mKeys.get(i);
            if (x < key.x + key.width) {
                x = x - key.gap / 2;
                if (x < key.x - key.gap) {
                    if (i == 0) {
                        return null;
                    }
                    key = row.mKeys.get(i - 1);
                }
                break;
            }
        }
        if (touchX < key.x - mKeyboard.getHorizontalGap() / 2 || touchX > key.x + key.width + mKeyboard.getHorizontalGap() / 2) {
            return null;
        }
        return key;
    }

    public interface OnKeyboardActionListener {

        void onPress(Keyboard.Key key);

        void onRelease(Keyboard.Key key);

        void onDelete();
    }
}
