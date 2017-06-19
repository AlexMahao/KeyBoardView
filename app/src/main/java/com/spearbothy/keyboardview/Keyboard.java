

package com.spearbothy.keyboardview;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.support.annotation.XmlRes;
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

    private CharSequence mLabel;

    private int mDefaultHorizontalGap;

    private int mDefaultWidth;

    private int mDefaultHeight;

    private int mDefaultVerticalGap;

    private int mKeyWidth;

    private int mKeyHeight;

    private int mTotalHeight;

    private int mTotalWidth;

    private int mDisplayWidth;

    private int mDisplayHeight;

    public List<Key> mKeys = new ArrayList<>();

    public ArrayList<Row> rows = new ArrayList<Row>();

    public static class Row {
        public int defaultWidth;
        public int defaultHeight;
        public int defaultHorizontalGap;
        public int verticalGap;
        public int y;
        ArrayList<Key> mKeys = new ArrayList<Key>();


        private Keyboard parent;

        public Row(Keyboard parent) {
            this.parent = parent;
        }

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
            a.recycle();
            this.x += gap;

            a = res.obtainAttributes(Xml.asAttributeSet(parser), R.styleable.Keyboard_External_Key);

            code = a.getInteger(R.styleable.Keyboard_External_Key_keycode, 0);

            style = a.getInt(R.styleable.Keyboard_External_Key_keyStyle, STYLE_NORMAL);

            checked = a.getBoolean(R.styleable.Keyboard_External_Key_keyChecked, false);
            a = res.obtainAttributes(Xml.asAttributeSet(parser), R.styleable.Keyboard_Key);

            icon = a.getDrawable(
                    R.styleable.Keyboard_Key_keyIcon);
            if (icon != null) {
                icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
            }
            label = a.getText(R.styleable.Keyboard_Key_keyLabel);
            a.recycle();
        }

        @Override
        public String toString() {
            return "Key{" +
                    " label=" + label +
                    ", width=" + width +
                    ", height=" + height +
                    ", gap=" + gap +
                    ", x=" + x +
                    ", y=" + y +
                    '}';
        }
    }


    /**
     * Creates a keyboard from the given xml key layout file. Weeds out rows
     * that have a keyboard mode defined but don't match the specified mode.
     *
     * @param context        the application or service context
     * @param xmlLayoutResId the resource file that contains the keyboard layout and keys.
     * @param modeId         keyboard mode identifier
     */
    public Keyboard(Context context, @XmlRes int xmlLayoutResId) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        mDisplayWidth = dm.widthPixels;
        mDisplayHeight = dm.heightPixels;
        //Log.v(TAG, "keyboard's display metrics:" + dm);

        mDefaultHorizontalGap = 0;
        mDefaultWidth = mDisplayWidth / 10;
        mDefaultVerticalGap = 0;
        mDefaultHeight = mDefaultWidth;
        loadKeyboard(context, context.getResources().getXml(xmlLayoutResId));
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

    /**
     * Returns the total height of the keyboard
     *
     * @return the total height of the keyboard
     */
    public int getHeight() {
        return mTotalHeight;
    }

    public int getMinWidth() {
        return mTotalWidth;
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
        int row = 0;
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
                        rows.add(currentRow);
                    } else if (TAG_KEY.equals(tag)) {
                        inKey = true;
                        key = createKeyFromXml(res, currentRow, x, y, parser);
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
                        row++;
                    } else {
                        // TODO: error or extend?
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
        a.recycle();
    }

    static int getDimensionOrFraction(TypedArray a, int index, int base, int defValue) {
        TypedValue value = a.peekValue(index);
        if (value == null) return defValue;
        if (value.type == TypedValue.TYPE_DIMENSION) {
            return a.getDimensionPixelOffset(index, defValue);
        } else if (value.type == TypedValue.TYPE_FRACTION) {
            // Round it to avoid values like 47.9999 from getting truncated
            return Math.round(a.getFraction(index, base, base, defValue));
        }
        return defValue;
    }
}
