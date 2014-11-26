package com.text;

/*
 * Copyright 2014 Mathew Kurian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * -------------------------------------------------------------------------
 *
 * DocumentView.java
 * @author Mathew Kurian
 *
 * From TextJustify-Android Library v2.0
 * https://github.com/bluejamesbond/TextJustify-Android
 *
 * Please report any issues
 * https://github.com/bluejamesbond/TextJustify-Android/issues
 *
 * Date: 10/27/14 1:36 PM
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

@SuppressWarnings("unused")
public class DocumentView extends View {

    public static final int PLAIN_TEXT = 0;
    public static final int FORMATTED_TEXT = 1;

    private DocumentLayout layout;
    private TextPaint paint;

    // Caching content
    private boolean cacheEnabled = false;
    private Bitmap cacheBitmap = null;

    public DocumentView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(PLAIN_TEXT);
    }

    public DocumentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(PLAIN_TEXT);
    }

    public DocumentView(Context context) {
        super(context);
        init(PLAIN_TEXT);
    }

    public DocumentView(Context context, int type) {
        super(context);
        init(type);
    }

    private void init(int type) {
        this.paint = new TextPaint();

        // Initialize paint
        initPaint(this.paint);

        // Get default layout
        this.layout = getDocumentLayoutInstance(type, paint);

        this.setPadding(0, 0, 0, 0);
    }

    public void setTextSize(float textSize) {
        paint.setTextSize(textSize);
    }

    public void setColor(int textColor) {
        paint.setColor(textColor);
    }

    public void setTypeface(Typeface typeface) {
        paint.setTypeface(typeface);
    }

    protected void initPaint(Paint paint) {
        paint.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        paint.setTextSize(34);
        paint.setAntiAlias(true);
    }

    public DocumentLayout getDocumentLayoutInstance(int type, TextPaint paint) {
        switch (type) {
            case FORMATTED_TEXT:
                return new SpannedDocumentLayout(paint);
            default:
            case PLAIN_TEXT:
                return new DocumentLayout(paint);
        }
    }

    @Override
    public void setDrawingCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    public void setText(CharSequence text, boolean justify) {
        this.layout.setText(text);
        requestLayout();
    }

    public CharSequence getText() {
        return this.layout.getText();
    }

    public DocumentLayout.LayoutParams getDocumentLayoutParams() {
        return this.layout.getLayoutParams();
    }

    public DocumentLayout getLayout() {
        return this.layout;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        this.layout.getLayoutParams().setParentWidth(width);
        this.layout.measure();
        this.setMeasuredDimension(width, this.layout.getMeasuredHeight());
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {

        // Active canas needs to be set
        // based on cacheEnabled
        Canvas activeCanvas;

        // Set the active canvas based on
        // whether cache is enabled
        if (cacheEnabled) {
            if (cacheBitmap != null) {
                // Draw to the OS provided canvas
                // if the cache is not empty
                canvas.drawBitmap(cacheBitmap, 0, 0, paint);
                return;
            } else {
                // Create a bitmap and set the activeCanvas
                // to the one derived from the bitmap
                cacheBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_4444);
                activeCanvas = new Canvas(cacheBitmap);
            }
        } else {
            // Active canvas is the OS
            // provided canvas
            activeCanvas = canvas;
        }

        this.layout.draw(activeCanvas);

        if (cacheEnabled) {
            // Draw the cache onto the OS provided
            // canvas.
            canvas.drawBitmap(cacheBitmap, 0, 0, paint);
        }
    }
}