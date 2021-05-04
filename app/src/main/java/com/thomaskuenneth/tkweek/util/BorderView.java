/*
 * BorderView.java
 *
 * TKWeek (c) Thomas Künneth 2012 - 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.material.color.MaterialColors;
import com.thomaskuenneth.tkweek.R;

/**
 * Diese Klasse stellt eine einfache View zur Verfügung, die an allen Seiten
 * einen 1 Pixel breiten farbigen Rahmen hat.
 *
 * @author Thomas Künneth
 */
public class BorderView extends View {

    private final Paint paint;

    public BorderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(MaterialColors.getColor(context, R.attr.colorOnBackground, Color.GREEN));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(getPaddingLeft() + 1, getPaddingTop() + 1, getWidth()
                        - getPaddingRight() - 1, getHeight() - getPaddingBottom() - 1,
                paint);
    }
}
