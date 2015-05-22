package com.hwajung.ksa.calligraphyhelper.object;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import com.hwajung.ksa.calligraphyhelper.R;

/**
 * Created by Jaemin on 2015-04-25.
 */
public class Letter {

    private static final int BLACK_CONST = 75;
    private static int[] BITMAP_ID;
    private static Context context;
    private int id; // 글자의 고유 ID
    private int resolution;
    private float size = 1; // 글자가 그려지는 배율
    private float degree = 0; // 글자가 회전한 각도
    private Point point; // 글자의 위치

    private Bitmap bitmap; // 비트맵 데이터

    private Letter(int id, Point point) throws OutOfMemoryError {
        this.id = id;
        this.point = point;

        try {
            Bitmap primaryBitmap = BitmapFactory.decodeStream(context.openFileInput(
                    context.getResources().getString(R.string.fileName_letterResource) + id));
            int bitmapWidth = primaryBitmap.getWidth();
            int bitmapHeight = primaryBitmap.getHeight();

            int[] colors = new int[bitmapHeight * bitmapWidth];
            primaryBitmap.getPixels(colors, 0, bitmapWidth, 0, 0, bitmapWidth, bitmapHeight);
            primaryBitmap = null;

            for (int i = 0; i < colors.length; i++)
                if (Color.red(colors[i]) + Color.green(colors[i]) + Color.blue(colors[i]) > BLACK_CONST * 3)
                    colors[i] = Color.argb(0, 255, 255, 255);
                else
                    colors[i] = Color.argb(255, 0, 0, 0);

            bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(colors, 0, bitmapWidth, 0, 0, bitmapWidth, bitmapHeight);
        } catch (Exception e) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        }
    }

    public static Letter getLetter(int id, Point point) {
        Letter letter = null;
        try {
            letter = new Letter(id, point);
        } catch (OutOfMemoryError oom) {
        }
        return letter;
    }

    public static Letter getLetter(float[] arr) {
        Letter letter = null;
        try {
            letter = new Letter((int) arr[0], new Point(arr[3], arr[4]));
            letter.setSize(arr[1]);
            letter.setDegree(arr[2]);
        } catch (OutOfMemoryError oom) {
        }
        return letter;
    }

    public static void setResources(Context context) {
        Letter.context = context;
    }

    public float[] toFloatArray() {
        float[] arr = new float[5];
        arr[0] = id;
        arr[1] = size;
        arr[2] = degree;
        arr[3] = point.x;
        arr[4] = point.y;
        return arr;
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
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
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
}