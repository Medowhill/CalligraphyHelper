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

    private ImageButton[] buttons;
    private int[] drawableId;

    public MultiButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setDrawableID(int[] drawableID) {

        this.drawableId = drawableID;

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = getResources().getDimensionPixelSize(R.dimen.sketch_margin_button);
        layoutParams.rightMargin = getResources().getDimensionPixelSize(R.dimen.sketch_margin_button);
        layoutParams.bottomMargin = getResources().getDimensionPixelSize(R.dimen.sketch_margin_button);
        layoutParams.leftMargin = getResources().getDimensionPixelSize(R.dimen.sketch_margin_button);

        buttons = new ImageButton[drawableID.length];

        for (int i = 0; i < drawableID.length; i++) {
            buttons[i] = new ImageButton(getContext());
            buttons[i].setBackground(getContext().getResources().getDrawable(drawableID[i]));
            addView(buttons[i], layoutParams);
        }
    }

    public void setOnClickListener(View.OnClickListener onClickListener, int id) {
        for (int i = 0; i < drawableId.length; i++) {
            if (drawableId[i] == id) {
                buttons[i].setOnClickListener(onClickListener);
                break;
            }
        }
    }

}
