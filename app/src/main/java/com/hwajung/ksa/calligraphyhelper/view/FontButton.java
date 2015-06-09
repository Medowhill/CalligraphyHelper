package com.hwajung.ksa.calligraphyhelper.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * (C) 2015. Jaemin Hong all rights reserved.
 */
public class FontButton extends Button {

    private static Typeface typeface;

    public FontButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setTypeface(typeface);
    }

    public FontButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeface(typeface);
    }

    public FontButton(Context context) {
        super(context);
    }

    public static void setClassTypeface(Typeface typeface) {
        FontButton.typeface = typeface;
    }
}
