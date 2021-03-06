package com.hwajung.ksa.calligraphyhelper.object;

/**
 * Created by Jaemin on 2015-04-26.
 */
public class Point {

    public float x, y;

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Point transform(float scale, float xPivot, float yPivot, float xShift, float yShift) {
        return new Point((x - xPivot) / scale + xPivot - xShift, (y - yPivot) / scale + yPivot - yShift);
    }

    public static float distance(Point p1, Point p2) {
        return (float) Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
    }

    public static float degree(Point p1, Point p2) {
        return (float) (Math.atan2(p2.y - p1.y, p2.x - p1.x) / Math.PI * 180);
    }


}
