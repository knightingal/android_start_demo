package com.example.jianming.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;


public class DownloadProcessBar extends View{
    public DownloadProcessBar(Context context) {
        super(context);
        paint = new Paint();
    }

    public DownloadProcessBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
    }

    public DownloadProcessBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint();
    }

    private Paint paint;

    int length = 0;
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setColor(Color.GRAY);
        canvas.drawRect(0, 0, this.width, 2, paint);

        paint.setColor(Color.GREEN);
        canvas.drawRect(0, 0, length, 2, paint);


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    private int measureHeight(int heightMeasureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(heightMeasureSpec);
        int specSize = MeasureSpec.getSize(heightMeasureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = 10;
        }

        return result;
    }

    private int width = 0;
    private int stepCount = 0;
    private int currCount = 0;
    private int measureWidth(int widthMeasureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        int specSize = MeasureSpec.getSize(widthMeasureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = 10;
        }
        this.width = result;
        return result;
    }

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }

    public void clear() {
        currCount = 0;
        length = 0;
        invalidate();
    }

    public void longer() {
        currCount++;
        length = this.width * currCount / this.stepCount;
        invalidate();
    }
}