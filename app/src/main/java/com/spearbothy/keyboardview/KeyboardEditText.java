package com.spearbothy.keyboardview;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.AppCompatEditText;
import android.text.InputType;
import android.util.AttributeSet;
import android.widget.EditText;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by mahao on 17-6-16.
 */

public class KeyboardEditText extends AppCompatEditText {
    public KeyboardEditText(Context context) {
        super(context);
        setEditTextInputFocus();
    }

    public KeyboardEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEditTextInputFocus();
    }

    public KeyboardEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (Build.VERSION.SDK_INT >= 21) {
            setShowSoftInputOnFocus(false);
            return;
        }
        setEditTextInputFocus();
    }

    /**
     * 禁止系统输入法
     */
    public void setEditTextInputFocus() {
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
            setShowSoftInputOnFocus.invoke(this, false);
        } catch (NoSuchMethodException e) {
            this.setInputType(InputType.TYPE_NULL);
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
