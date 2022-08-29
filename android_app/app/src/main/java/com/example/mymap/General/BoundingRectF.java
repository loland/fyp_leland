package com.example.mymap.General;

import android.graphics.RectF;
import android.util.Log;

public class BoundingRectF extends RectF {
    public float x;
    public float y;

    public BoundingRectF(float left, float top, float right, float bottom, float x, float y) {
        super(left, top, right, bottom);
        Log.d("BoundingRectF", String.format("%s, %s, %s, %s", left, top, right, bottom));
        this.x = x;
        this.y = y;
    }
}
