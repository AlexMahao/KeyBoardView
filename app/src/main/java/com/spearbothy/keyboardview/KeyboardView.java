package com.spearbothy.keyboardview;

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
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * Created by mahao on 17-6-14.
 */

public class KeyboardView extends View {
    public static final String TAG = KeyboardView.class.getSimpleName();

    public static final int TYPE_NUMBER = 0;
    public static final int TYPE_ENGLISH_LOWER = 1;
    public static final int TYPE_ENGLISH_UPPER = 2;
    public static final int TYPE_SYMBOL = 3;
    private NinePatch mKeyPressPressPatch;
    private PopupWindow mPreviewPopup;
    private TextView mPreviewText;
    private int mPreviewOffsetHeight;
    private int mPreviewOffsetWidth;

    private int mType;

    private Keyboard mKeyboard;

    private Paint mTextPaint;

    private NinePatch mKeyPressLightPatch;

    private NinePatch mKeyPressNormalPatch;

    private Keyboard.Key mPressKey;

    private OnKeyboardActionListener mOnKeyboardActionListener;

    private int mPreviewHeight;

    private int mPreviewWidth;

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
        mType = a.getInt(R.styleable.KeyboardView_type, 0);
        a.recycle();

        replace(mType);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.keyboard_press_normal);
        mKeyPressNormalPatch = new NinePatch(bitmap, bitmap.getNinePatchChunk(), null);

        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.keyboard_press_light);
        mKeyPressLightPatch = new NinePatch(bitmap, bitmap.getNinePatchChunk(), null);

        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.keyboard_press_press);
        mKeyPressPressPatch = new NinePatch(bitmap, bitmap.getNinePatchChunk(), null);

        mTextPaint = new Paint();
        mTextPaint.setStrokeWidth(3);
        mTextPaint.setTextSize(getFractionByHeight(0.025f));
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        mPreviewText = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.layout_preview_key, null);
        mPreviewText.setPadding(0, getFractionByHeight(0.015625f), 0, 0);
        mPreviewText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getFractionByHeight(0.0375f));
        mPreviewPopup = new PopupWindow(context);
        mPreviewPopup.setContentView(mPreviewText);

        Log.i(TAG, mKeyboard.getKeys().toString());
    }

    public void replace(int type) {
        if (type == TYPE_NUMBER) {
            mKeyboard = new Keyboard(getContext(), R.xml.number_keyboard);
        } else if (type == TYPE_ENGLISH_LOWER) {
            mKeyboard = new Keyboard(getContext(), R.xml.english_lower_keyboard);
        } else if (type == TYPE_SYMBOL) {
            mKeyboard = new Keyboard(getContext(), R.xml.symbol_keyboard);
        } else if (type == TYPE_ENGLISH_UPPER) {
            mKeyboard = new Keyboard(getContext(), R.xml.english_upper_keyboard);
        }
        invalidate();
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
        final int drawableX = (key.width - key.icon.getIntrinsicWidth()) / 2 + key.x;
        final int drawableY = (key.height - key.icon.getIntrinsicHeight()) / 2 + key.y;
        canvas.translate(drawableX, drawableY);
        key.icon.draw(canvas);
        canvas.restore();
    }

    private void drawKeyBg(Keyboard.Key key, Canvas canvas) {
        RectF rectF = new RectF(key.x, key.y, key.x + key.width, key.y + key.height);
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
        int baseline = key.y + (key.height - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;
        canvas.drawText(String.valueOf(key.label), key.x + key.width / 2 - bounds.width() / 2, baseline, mTextPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.i(TAG, "x:" + event.getX() + " y:" + event.getY());
                mPressKey = calculateLocation(event.getX(), event.getY());
                if (mPressKey == null) {
                    return true;
                }

                onPress(mPressKey);
                mPressKey.pressed = true;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mPressKey == null) {
                    return true;
                }
                onRelease(mPressKey);
                mPressKey.pressed = false;
                break;
        }
        invalidate();
        return true;
    }

    public void onPress(Keyboard.Key key) {
        showPopupWindow(key);
        if (mOnKeyboardActionListener != null) {
            mOnKeyboardActionListener.onPress(key);
        }
    }

    public void showPopupWindow(Keyboard.Key key) {
        mPreviewText.setText(key.label);
        int[] mCoordinates = new int[2];
        getLocationInWindow(mCoordinates);
        int x = 0;
        int y = 0;
        switch (key.previewDir) {
            case Keyboard.Key.PREVIEW_DIR_LEFT:
                mPreviewHeight = getFractionByHeight(0.164584f);
                mPreviewWidth = getFractionByWidth(0.162963f);
                mPreviewOffsetHeight = getFractionByHeight(0.007813f);
                mPreviewOffsetWidth = getFractionByWidth(0.0138889f);
                mPreviewPopup.setBackgroundDrawable(getResources().getDrawable(R.drawable.keyboard_preview_left));
                mPreviewPopup.setWidth(mPreviewWidth);
                mPreviewPopup.setHeight(mPreviewHeight);
                y = mCoordinates[1] + key.y - (mPreviewHeight - mKeyboard.getKeyHeight()) + mPreviewOffsetHeight;
                x = key.x - mPreviewOffsetWidth;
                mPreviewPopup.showAtLocation(this, Gravity.NO_GRAVITY, x, y);
                break;
            case Keyboard.Key.PREVIEW_DIR_CENTER:
                mPreviewHeight = getFractionByHeight(0.164584f);
                mPreviewWidth = getFractionByWidth(0.178704f);
                mPreviewOffsetHeight = getFractionByHeight(0.007813f);
                mPreviewOffsetWidth = getFractionByWidth(0.046297f);
                mPreviewPopup.setBackgroundDrawable(getResources().getDrawable(R.drawable.keyboard_preview_center));
                mPreviewPopup.setWidth(mPreviewWidth);
                mPreviewPopup.setHeight(mPreviewHeight);
                y = mCoordinates[1] + key.y - (mPreviewHeight - mKeyboard.getKeyHeight()) + mPreviewOffsetHeight;
                x = key.x - mPreviewOffsetWidth;
                mPreviewPopup.showAtLocation(this, Gravity.NO_GRAVITY, x, y);
                break;
            case Keyboard.Key.PREVIEW_DIR_RIGHT:
                mPreviewHeight = getFractionByHeight(0.164584f);
                mPreviewWidth = getFractionByWidth(0.162963f);
                mPreviewOffsetHeight = getFractionByHeight(0.007813f);
                mPreviewOffsetWidth = getFractionByWidth(0.063889f);
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
            }
        }
    }

    private void processKey(int code) {
        switch (code) {
            case Keyboard.KEYCODE_MODE_CHANGE_TO_ENGLISH_LOWER:
                replace(TYPE_ENGLISH_LOWER);
                break;
            case Keyboard.KEYCODE_MODE_CHANGE_TO_UPPER:
                replace(TYPE_ENGLISH_UPPER);
                break;
            case Keyboard.KEYCODE_MODE_CHANGE_TO_NUMBER:
                replace(TYPE_NUMBER);
                break;
            case Keyboard.KEYCODE_MODE_CHANGE_TO_SYMBOL:
                replace(TYPE_SYMBOL);
                break;
        }
        invalidate();
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

    public int getFractionByWidth(float fraction) {
        return Math.round(fraction * getResources().getDisplayMetrics().widthPixels);
    }

    public int getFractionByHeight(float fraction) {
        return Math.round(fraction * getResources().getDisplayMetrics().heightPixels);
    }

    public interface OnKeyboardActionListener {

        void onPress(Keyboard.Key key);

        void onRelease(Keyboard.Key key);

        void onDelete();
    }

}
