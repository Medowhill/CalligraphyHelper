package com.hwajung.ksa.calligraphyhelper.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * (C) 2015. Jaemin Hong all rights reserved.
 */
public class PenPreView extends View {

    private int height, width;

    private float thickness = 10, degree = 0;
    private boolean circle = true;

    private Paint paint, paint_border;

    public PenPreView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(5);

        paint_border = new Paint();
        paint_border.setStyle(Paint.Style.STROKE);
        paint_border.setColor(Color.BLACK);
        paint_border.setStrokeWidth(10);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        width = w;
        height = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawColor(Color.WHITE);
        canvas.drawRect(0, 0, width, height, paint_border);

        if (circle) {
            canvas.drawCircle(width / 2, height / 2, thickness, paint);
        } else {
            canvas.drawLine(width / 2 + thickness * (float) Math.sin(degree), height / 2 - thickness * (float) Math.cos(degree),
                    width / 2 - thickness * (float) Math.sin(degree), height / 2 + thickness * (float) Math.cos(degree), paint);
        }

    }

    public void setCircle(boolean circle) {
        this.circle = circle;
        invalidate();
    }

    public void setDegree(float degree) {
        this.degree = degree;
        invalidate();
    }

    public void setThickness(float thickness) {
        this.thickness = thickness;
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }
}
