package com.example.tetris;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class BgShape extends View {

    static Paint fill = new Paint();
    static Paint stroke = new Paint();
    static Rect msquare = new Rect();


    public BgShape(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        fill.setColor(Color.BLACK);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setColor(Color.WHITE);
        stroke.setStrokeWidth(3);



        msquare.left = 0;
        msquare.top = 0;
        msquare.right = msquare.left + 50;
        msquare.bottom = msquare.top + 50;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(msquare, fill);
        canvas.drawRect(msquare, stroke);


    }

    @Override
    protected void onMeasure(int widthMeasure, int heightMeasure) {
        int width = msquare.width() ;
        int height = msquare.height();
        setMeasuredDimension(width, height);
    }


}