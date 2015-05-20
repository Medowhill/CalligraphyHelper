package com.hwajung.ksa.calligraphyhelper.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.hwajung.ksa.calligraphyhelper.R;

/**
 * Created by Jaemin on 2015-05-16.
 */
public class LetterAdapter extends BaseAdapter {

    private final int[] DRAWABLE_ID;
    private Context context;

    public LetterAdapter(Context context) {
        this.context = context;

        TypedArray typedArray = context.getResources().obtainTypedArray(R.array.letters_drawable_id_array);
        DRAWABLE_ID = new int[typedArray.length()];
        for (int i = 0; i < typedArray.length(); i++)
            DRAWABLE_ID[i] = typedArray.getResourceId(i, -1);
    }

    @Override
    public int getCount() {
        return DRAWABLE_ID.length;
    }

    @Override
    public Object getItem(int i) {
        return DRAWABLE_ID[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ImageView imageView;
        if (view == null) {
            imageView = new ImageView(context);
            imageView.setBackgroundColor(Color.WHITE);
            imageView.setLayoutParams(new GridView.LayoutParams(100, 100));

            //imageView.setAdjustViewBounds(false);
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        } else {
            imageView = (ImageView) view;
        }

        imageView.setImageResource(DRAWABLE_ID[i]);

        return imageView;
    }
}
