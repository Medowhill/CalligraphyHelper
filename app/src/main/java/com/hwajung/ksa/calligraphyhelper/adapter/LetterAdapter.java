package com.hwajung.ksa.calligraphyhelper.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.hwajung.ksa.calligraphyhelper.R;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Jaemin on 2015-05-16.
 */
public class LetterAdapter extends BaseAdapter {

    private ArrayList<Integer>[] fileName;
    private Context context;

    private int category = 0;

    private boolean loaded = false;

    public LetterAdapter(Context context) {
        this.context = context;
    }

    public void load(byte[] data) {
        Log.i("TEST", "load");

        fileName = new ArrayList[context.getResources().getInteger(R.integer.letter_category_num)];
        for (int i = 0; i < fileName.length; i++)
            fileName[i] = new ArrayList<>();

        for (int i = 0; i < data.length; i++) {
            int category = data[i];
            fileName[category].add(i);
        }

        loaded = true;
    }

    public void load() {
        // Load existing file name list
        byte[] data = null;

        try {
            FileInputStream fis = context.openFileInput(context.getString(R.string.fileName_letterResource));
            data = new byte[fis.available()];
            fis.read(data);
            fis.close();
        } catch (IOException ioException) {
            return;
        }

        load(data);
    }

    @Override
    public int getCount() {
        if (loaded)
            return fileName[category].size();
        else
            return 0;
    }

    @Override
    public Object getItem(int i) {
        return fileName[category].get(i);
    }

    @Override
    public long getItemId(int i) {
        return fileName[category].get(i);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ImageView imageView;
        if (view == null) {
            imageView = new ImageView(context);
            imageView.setBackgroundColor(Color.WHITE);
            imageView.setLayoutParams(new GridView.LayoutParams(100, 100));

            //imageView.setAdjustViewBounds(false);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        } else {
            imageView = (ImageView) view;
        }

        try {
            imageView.setImageBitmap(BitmapFactory.decodeStream(
                    context.openFileInput(context.getString(R.string.fileName_previewResource) + fileName[category].get(i))));
        } catch (IOException ioe) {
            imageView.setBackgroundColor(Color.GRAY);
        }

        return imageView;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public boolean isLoaded() {
        return loaded;
    }
}
