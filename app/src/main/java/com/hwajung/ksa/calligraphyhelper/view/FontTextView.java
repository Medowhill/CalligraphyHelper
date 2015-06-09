package com.hwajung.ksa.calligraphyhelper.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * (C) 2015. Jaemin Hong all rights reserved.
 */
public class FontTextView extends TextView {

    private static Typeface typeface;

    public FontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setTypeface(typeface);
    }

    public FontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeface(typeface);
    }

    public FontTextView(Context context) {
        super(context);
    }

    public static void setClassTypeface(Typeface typeface) {
        FontTextView.typeface = typeface;
    }
}
