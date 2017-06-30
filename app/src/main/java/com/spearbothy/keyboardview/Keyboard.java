package com.spearbothy.keyboardview;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.support.annotation.XmlRes;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.util.Xml;

import java.util.ArrayList;
import java.util.List;

public class Keyboard {
    static final String TAG = "Keyboard";

    // Keyboard XML Tags
    private static final String TAG_KEYBOARD = "Keyboard";
    private static final String TAG_ROW = "Row";
    private static final String TAG_KEY = "Key";

    public static final int KEYCODE_MODE_CHANGE_TO_ENGLISH_LOWER = -1;
    public static final int KEYCODE_MODE_CHANGE_TO_UPPER = -2;
    public static final int KEYCODE_MODE_CHANGE_TO_NUMBER = -3;
    public static final int KEYCODE_MODE_CHANGE_TO_SYMBOL = -4;
    public static final int KEYCODE_DELETE = 0;

    private int mDefaultHorizontalGap;

    private int mDefaultWidth;

    private int mDefaultHeight;

    private int mDefaultVerticalGap;

    private int mTotalHeight;

    private int mTotalWidth;

    private int mDisplayWidth;

    private int mDisplayHeight;

    private boolean mIsRandom;

    private int mRandomKeyCount;

    private boolean mIsAnimation;

    private boolean mIsClickRandom;

    private List<Key> mKeys = new ArrayList<>();

    private ArrayList<Row> mRows = new ArrayList<Row>();

    public void random() {
        if (mIsRandom && mRandomKeyCount > 0) {
            // 打乱顺序
            randomKeys();
        }
    }

    public boolean isRandom() {
        return mIsRandom;
    }

    public boolean isAnimation() {
        return mIsAnimation;
    }

    public boolean isClickRandom() {
        return mIsClickRandom;
    }

    public static class Row {
        public int defaultWidth;
        public int defaultHeight;
        public int defaultHorizontalGap;
        public int verticalGap;
        public int y;
        public ArrayList<Key> mKeys = new ArrayList<Key>();

        private Keyboard parent;

        public Row(Resources res, Keyboard parent, XmlResourceParser parser) {
            this.parent = parent;
            TypedArray a = res.obtainAttributes(Xml.asAttributeSet(parser),
                    R.styleable.Keyboard);
            defaultWidth = getDimensionOrFraction(a,
                    R.styleable.Keyboard_keyWidth,
                    parent.mDisplayWidth, parent.mDefaultWidth);
            defaultHeight = getDimensionOrFraction(a,
                    R.styleable.Keyboard_keyHeight,
                    parent.mDisplayHeight, parent.mDefaultHeight);
            defaultHorizontalGap = getDimensionOrFraction(a,
                    R.styleable.Keyboard_horizontalGap,
                    parent.mDisplayWidth, parent.mDefaultHorizontalGap);
            verticalGap = getDimensionOrFraction(a,
                    R.styleable.Keyboard_verticalGap,
                    parent.mDisplayHeight, parent.mDefaultVerticalGap);
            a.recycle();
        }
    }

    public int getDisplayWidth() {
        return mDisplayWidth;
    }

    public static class Key {

        public static final int STYLE_NORMAL = 0;
        public static final int STYLE_LIGHT = 1;

        public static final int PREVIEW_DIR_LEFT = 1;
        public static final int PREVIEW_DIR_CENTER = 2;
        public static final int PREVIEW_DIR_RIGHT = 3;
        public static final int PREVIEW_NOT_SHOW = 0;

        public int code;
        public CharSequence label;
        public Drawable icon;
        public int width;
        public int height;
        public int gap;
        public int x;
        public int y;
        public boolean pressed;
        public int style;
        public boolean checked;
        public int previewDir = PREVIEW_NOT_SHOW;
        public boolean isRandom;
        public boolean isRepeat;
        public CharSequence text;
        public int fromX = 0;
        public int fromY = 0;
        public int drawX = 0;
        public int drawY = 0;

        private Keyboard keyboard;

        public Key(Row parent) {
            keyboard = parent.parent;
            height = parent.defaultHeight;
            width = parent.defaultWidth;
            gap = parent.defaultHorizontalGap;
        }

        public Key(Resources res, Row parent, int x, int y, XmlResourceParser parser) {
            this(parent);

            this.x = x;
            this.y = y;
            TypedArray a = res.obtainAttributes(Xml.asAttributeSet(parser),
                    R.styleable.Keyboard);
            width = getDimensionOrFraction(a,
                    R.styleable.Keyboard_keyWidth,
                    keyboard.mDisplayWidth, parent.defaultWidth);
            height = getDimensionOrFraction(a,
                    R.styleable.Keyboard_keyHeight,
                    keyboard.mDisplayHeight, parent.defaultHeight);
            gap = getDimensionOrFraction(a,
                    R.styleable.Keyboard_horizontalGap,
                    keyboard.mDisplayWidth, parent.defaultHorizontalGap);
            isRandom = a.getBoolean(R.styleable.Keyboard_isRandom, false);
            a.recycle();

            this.x += gap;

            a = res.obtainAttributes(Xml.asAttributeSet(parser), R.styleable.Keyboard_External_Key);
            code = a.getInteger(R.styleable.Keyboard_External_Key_keycode, 0);
            style = a.getInt(R.styleable.Keyboard_External_Key_keyStyle, STYLE_NORMAL);
            checked = a.getBoolean(R.styleable.Keyboard_External_Key_keyChecked, false);
            previewDir = a.getInt(R.styleable.Keyboard_External_Key_keyPreviewDir, PREVIEW_DIR_CENTER);
            isRepeat = a.getBoolean(R.styleable.Keyboard_External_Key_isRepeat, false);
            a.recycle();

            a = res.obtainAttributes(Xml.asAttributeSet(parser), R.styleable.Keyboard_Key);
            icon = a.getDrawable(
                    R.styleable.Keyboard_Key_keyIcon);
            if (icon != null) {
                icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
            }
            label = a.getText(R.styleable.Keyboard_Key_keyLabel);
            text = a.getText(R.styleable.Keyboard_Key_keyOutputText);
            if (TextUtils.isEmpty(text)) {
                text = label;
            }
            a.recycle();
            drawX = this.x;
            drawY = this.y;
        }

        @Override
        public String toString() {
            return "Key{" +
                    "code=" + code +
                    ", label=" + label +
                    ", icon=" + icon +
                    ", width=" + width +
                    ", height=" + height +
                    ", gap=" + gap +
                    ", x=" + x +
                    ", y=" + y +
                    ", pressed=" + pressed +
                    ", style=" + style +
                    ", checked=" + checked +
                    ", previewDir=" + previewDir +
                    '}';
        }
    }

    public Keyboard(Context context, @XmlRes int xmlLayoutResId) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        mDisplayWidth = dm.widthPixels;
        mDisplayHeight = dm.heightPixels;
        mDefaultHorizontalGap = 0;
        mDefaultWidth = mDisplayWidth / 10;
        mDefaultVerticalGap = 0;
        mDefaultHeight = mDefaultWidth;
        loadKeyboard(context, context.getResources().getXml(xmlLayoutResId));
    }

    private void randomKeys() {
        for (int i = 0; i < mKeys.size(); i++) {
            Key key = mKeys.get(i);
            if (key.isRandom) {
                Key targetKey = mKeys.get((int) (Math.random() * mKeys.size()));
                if (targetKey.isRandom) {
                    swapKey(key, targetKey);
                }
            }
        }
    }

    private void swapKey(Key source, Key target) {
        CharSequence tempLabel = source.label;
        int tempCode = source.code;
        int tempStyle = source.style;
        boolean tempCheck = source.checked;
        int tempPreviewDir = source.previewDir;
        boolean tempIsRepeat = source.isRepeat;
        CharSequence tempText = source.text;
        Drawable tempIcon = source.icon;
        source.label = target.label;
        source.code = target.code;
        source.style = target.style;
        source.checked = target.checked;
        source.previewDir = target.previewDir;
        source.isRepeat = target.isRepeat;
        source.text = target.text;
        source.icon = target.icon;
        source.fromX = target.x;
        source.fromY = target.y;
        target.label = tempLabel;
        target.code = tempCode;
        target.style = tempStyle;
        target.checked = tempCheck;
        target.previewDir = tempPreviewDir;
        target.isRepeat = tempIsRepeat;
        target.text = tempText;
        target.icon = tempIcon;
        target.fromX = source.x;
        target.fromY = source.y;
    }

    protected Row createRowFromXml(Resources res, XmlResourceParser parser) {
        return new Row(res, this, parser);
    }

    protected Key createKeyFromXml(Resources res, Row parent, int x, int y,
                                   XmlResourceParser parser) {
        return new Key(res, parent, x, y, parser);
    }

    private void loadKeyboard(Context context, XmlResourceParser parser) {
        boolean inKey = false;
        boolean inRow = false;
        int x = 0;
        int y = 0;
        Key key = null;
        Row currentRow = null;
        Resources res = context.getResources();
        try {
            int event;
            while ((event = parser.next()) != XmlResourceParser.END_DOCUMENT) {
                if (event == XmlResourceParser.START_TAG) {
                    String tag = parser.getName();
                    if (TAG_ROW.equals(tag)) {
                        inRow = true;
                        x = 0;
                        currentRow = createRowFromXml(res, parser);
                        y += currentRow.verticalGap;
                        currentRow.y = y;
                        mRows.add(currentRow);
                    } else if (TAG_KEY.equals(tag)) {
                        inKey = true;
                        key = createKeyFromXml(res, currentRow, x, y, parser);
                        if (key.isRandom) {
                            mRandomKeyCount++;
                        }
                        mKeys.add(key);
                        currentRow.mKeys.add(key);
                    } else if (TAG_KEYBOARD.equals(tag)) {
                        parseKeyboardAttributes(res, parser);
                    }
                } else if (event == XmlResourceParser.END_TAG) {
                    if (inKey) {
                        inKey = false;
                        x += key.gap + key.width;
                        if (x > mTotalWidth) {
                            mTotalWidth = x;
                        }
                    } else if (inRow) {
                        inRow = false;
                        y += currentRow.defaultHeight;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Parse error:" + e);
            e.printStackTrace();
        }
        mTotalHeight = y + mDefaultVerticalGap;
    }

    private void parseKeyboardAttributes(Resources res, XmlResourceParser parser) {
        TypedArray a = res.obtainAttributes(Xml.asAttributeSet(parser),
                R.styleable.Keyboard);

        mDefaultWidth = getDimensionOrFraction(a,
                R.styleable.Keyboard_keyWidth,
                mDisplayWidth, mDisplayWidth / 10);
        mDefaultHeight = getDimensionOrFraction(a,
                R.styleable.Keyboard_keyHeight,
                mDisplayHeight, 50);
        mDefaultHorizontalGap = getDimensionOrFraction(a,
                R.styleable.Keyboard_horizontalGap,
                mDisplayWidth, 0);
        mDefaultVerticalGap = getDimensionOrFraction(a,
                R.styleable.Keyboard_verticalGap,
                mDisplayHeight, 0);
        mIsRandom = a.getBoolean(R.styleable.Keyboard_isRandom, true);
        mIsAnimation = a.getBoolean(R.styleable.Keyboard_isAnimation, true);
        mIsClickRandom = a.getBoolean(R.styleable.Keyboard_isClickRandom, false);
        a.recycle();
    }

    static int getDimensionOrFraction(TypedArray a, int index, int base, int defValue) {
        TypedValue value = a.peekValue(index);
        if (value == null) return defValue;
        if (value.type == TypedValue.TYPE_DIMENSION) {
            return a.getDimensionPixelOffset(index, defValue);
        } else if (value.type == TypedValue.TYPE_FRACTION) {
            return Math.round(a.getFraction(index, base, base, defValue));
        }
        return defValue;
    }

    protected int getHorizontalGap() {
        return mDefaultHorizontalGap;
    }

    protected void setHorizontalGap(int gap) {
        mDefaultHorizontalGap = gap;
    }

    protected int getVerticalGap() {
        return mDefaultVerticalGap;
    }

    protected void setVerticalGap(int gap) {
        mDefaultVerticalGap = gap;
    }

    protected int getKeyHeight() {
        return mDefaultHeight;
    }

    protected void setKeyHeight(int height) {
        mDefaultHeight = height;
    }

    protected int getKeyWidth() {
        return mDefaultWidth;
    }

    protected void setKeyWidth(int width) {
        mDefaultWidth = width;
    }

    public int getHeight() {
        return mTotalHeight;
    }

    public int getMinWidth() {
        return mTotalWidth;
    }

    public List<Key> getKeys() {
        return mKeys;
    }

    public ArrayList<Row> getRows() {
        return mRows;
    }
}
