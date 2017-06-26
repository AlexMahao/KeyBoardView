package com.spearbothy.keyboardview;

import org.junit.Test;

import java.text.DecimalFormat;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    private static final String FORMAT_NUMBER_DECIMAL_1 = "00";
    @Test
    public void addition_isCorrect() throws Exception {
        String format = new DecimalFormat(FORMAT_NUMBER_DECIMAL_1).format(100.1);
        System.out.printf(format);
    }
}