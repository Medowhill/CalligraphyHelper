package com.hwajung.ksa.calligraphyhelper.object;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import com.hwajung.ksa.calligraphyhelper.R;

import java.io.FileNotFoundException;

/**
 * Created by Jaemin on 2015-04-25.
 */
public class Letter {

    private static final int TRANSPARENCY = Color.argb(0, 255, 255, 255);
    private static final int BLACK = 75;

    private static int[] BITMAP_ID;
    private static Context context;
    private int id; // 글자의 고유 ID
    private int resolution;
    private float size = 1; // 글자가 그려지는 배율
    private float degree = 0; // 글자가 회전한 각도
    private Point point; // 글자의 위치
    private int color = Color.BLACK;

    private Bitmap bitmap; // 비트맵 데이터

    public Letter(int id) {
        this.id = id;

        loadBitmap();
    }

    public Letter(int id, int color) {
        this.id = id;
        this.color = color;

        loadBitmap();
    }

    public static void setResources(Context context) {
        Letter.context = context;
    }

    public void loadBitmap() {
        if (bitmap != null)
            bitmap.recycle();
        try {
            Bitmap primaryBitmap = BitmapFactory.decodeStream(
                    context.openFileInput(context.getString(R.string.fileName_letterResource) + id));

            int bitmapWidth = primaryBitmap.getWidth();
            int bitmapHeight = primaryBitmap.getHeight();

            int[] colors = new int[bitmapHeight * bitmapWidth];
            primaryBitmap.getPixels(colors, 0, bitmapWidth, 0, 0, bitmapWidth, bitmapHeight);

            for (int i = 0; i < colors.length; i++)
                if (Color.red(colors[i]) + Color.green(colors[i]) + Color.blue(colors[i]) < BLACK * 3)
                    colors[i] = color;
                else
                    colors[i] = TRANSPARENCY;

            bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(colors, 0, bitmapWidth, 0, 0, bitmapWidth, bitmapHeight);
        } catch (FileNotFoundException e) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e1) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        }
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public float getDegree() {
        return degree;
    }

    public void setDegree(float degree) {
        this.degree = degree;
        if (this.degree < 0)
            this.degree += 360;
        else
            this.degree %= 360;
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        if (size < 0.1)
            size = 0.1f;
        else if (size > 5)
            size = 5;
        this.size = size;
    }

    public int getWidth() {
        return bitmap.getWidth();
    }

    public int getHeight() {
        return bitmap.getHeight();
    }

    public int getId() {
        return id;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}