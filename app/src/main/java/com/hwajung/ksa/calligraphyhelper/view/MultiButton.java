package com.hwajung.ksa.calligraphyhelper.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.hwajung.ksa.calligraphyhelper.R;

/**
 * Created by Jaemin on 2015-05-06.
 */
public class MultiButton extends LinearLayout {

    private ImageButton[] imageButtons;

    public MultiButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setDrawableID(int[] drawableID) {

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        imageButtons = new ImageButton[drawableID.length];

        for (int i = 0; i < drawableID.length; i++) {
            imageButtons[i] = new ImageButton(getContext());
            imageButtons[i].setBackground(getContext().getResources().getDrawable(drawableID[i]));
            addView(imageButtons[i], layoutParams);
        }
    }

    public void setOnClickListener(View.OnClickListener onClickListener, int index) {
        imageButtons[index].setOnClickListener(onClickListener);
    }

}
