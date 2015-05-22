package com.hwajung.ksa.calligraphyhelper.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
    private ArrayList<Integer> entireFileName;
    private Context context;

    private boolean loaded = false;

    public LetterAdapter(Context context) {
        this.context = context;
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

        fileName = new ArrayList[context.getResources().getInteger(R.integer.letter_category_num)];
        for (int i = 0; i < fileName.length; i++)
            fileName[i] = new ArrayList<>();
        entireFileName = new ArrayList<>();

        for (int i = 0; i < data.length / 3; i++) {
            int file = data[i * 3] * 128 + data[i * 3 + 1];
            int category = data[i * 3 + 2];
            fileName[category].add(file);
            entireFileName.add(file);
        }

        loaded = true;
    }

    @Override
    public int getCount() {
        if (loaded)
            return entireFileName.size();
        else
            return 0;
    }

    @Override
    public Object getItem(int i) {
        return entireFileName.get(i);
    }

    @Override
    public long getItemId(int i) {
        return entireFileName.get(i);
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
                    context.openFileInput(context.getString(R.string.fileName_previewResource) + entireFileName.get(i))));
        } catch (IOException ioe) {
            imageView.setBackgroundColor(Color.GRAY);
        }

        return imageView;
    }
}
