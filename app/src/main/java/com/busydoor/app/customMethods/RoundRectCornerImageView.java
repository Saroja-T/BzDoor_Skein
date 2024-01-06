package com.busydoor.app.customMethods;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

public class RoundRectCornerImageView extends AppCompatImageView {

    private float radius = 18.0f;
    private Path path;
    private RectF rect;

    public RoundRectCornerImageView(Context context) {
        super(context);
        init();
    }

    public RoundRectCornerImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RoundRectCornerImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        path = new Path();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        rect = new RectF(0, this.getWidth(), this.getWidth(), 0);

        //RectF rect = new RectF(0, 0, this.getWidth(), this.getHeight() + 12.0f);
        path.addRoundRect(rect, radius, radius, Path.Direction.CW);


       // path.addRoundRect(0f,radius,radius,0f, , Path.Direction.CW);
        canvas.clipPath(path);
        super.onDraw(canvas);
    }
}
