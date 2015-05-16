package com.hwajung.ksa.calligraphyhelper.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * Created by Jaemin on 2015-05-06.
 */
public class MultiButton extends LinearLayout {

    private Button[] buttons;

    public MultiButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setDrawableID(int[] drawableID) {

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        buttons = new Button[drawableID.length];

        for (int i = 0; i < drawableID.length; i++) {
            buttons[i] = new Button(getContext());
            buttons[i].setBackground(getContext().getResources().getDrawable(drawableID[i]));
            addView(buttons[i], layoutParams);
        }
    }

    public void setOnClickListener(View.OnClickListener onClickListener, int index) {
        buttons[index].setOnClickListener(onClickListener);
    }

}
