package com.bluejamesbond.text;

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
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.bluejamesbond.text.style.TextAlignment;

@SuppressWarnings("unused")
public class DocumentView extends View {

    public static final int PLAIN_TEXT = 0;
    public static final int FORMATTED_TEXT = 1;
    private DocumentLayout mLayout;
    private TextPaint mPaint;

    // Caching content
    private CacheConfig mCacheConfig = CacheConfig.AUTO_QUALITY;
    private Bitmap mCacheBitmap = null;

    public DocumentView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, PLAIN_TEXT);
    }

    public DocumentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, PLAIN_TEXT);
    }

    public DocumentView(Context context) {
        super(context);
        init(context, null, PLAIN_TEXT);
    }

    public DocumentView(Context context, int type) {
        super(context);
        init(context, null, type);
    }

    private void init(Context context, AttributeSet attrs, int type) {
        this.mPaint = new TextPaint();

        // Initialize mPaint
        initPaint(this.mPaint);

        // Set default padding
        setPadding(0, 0, 0, 0);

        if (attrs != null && !isInEditMode()) {
            TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.DocumentView);

            final int N = a.getIndexCount();
            boolean layoutSet = false;

            // find and set project mLayout
            for (int i = 0; i < N; ++i) {
                int attr = a.getIndex(i);
                if (R.styleable.DocumentView_textFormat == attr) {
                    this.mLayout = getDocumentLayoutInstance(a.getInt(attr, DocumentView.PLAIN_TEXT), mPaint);
                    layoutSet = true;
                    break;
                }
            }

            if (!layoutSet) {
                this.mLayout = getDocumentLayoutInstance(DocumentView.PLAIN_TEXT, mPaint);
            }

            DocumentLayout.LayoutParams layoutParams = this.mLayout.getLayoutParams();

            for (int i = 0; i < N; ++i) {

                int attr = a.getIndex(i);

                if (attr == R.styleable.DocumentView_padding) {
                    Float pad = a.getDimension(attr, 0f);
                    layoutParams.setPaddingLeft(pad);
                    layoutParams.setPaddingBottom(pad);
                    layoutParams.setPaddingRight(pad);
                    layoutParams.setPaddingTop(pad);
                } else if (attr == R.styleable.DocumentView_paddingLeft) {
                    layoutParams.setPaddingLeft(a.getDimension(attr, 0f));
                } else if (attr == R.styleable.DocumentView_paddingBottom) {
                    layoutParams.setPaddingBottom(a.getDimension(attr, 0f));
                } else if (attr == R.styleable.DocumentView_paddingRight) {
                    layoutParams.setPaddingRight(a.getDimension(attr, 0f));
                } else if (attr == R.styleable.DocumentView_paddingTop) {
                    layoutParams.setPaddingTop(a.getDimension(attr, 0f));
                } else if (attr == R.styleable.DocumentView_offsetX) {
                    layoutParams.setOffsetX(a.getDimension(attr, 0f));
                } else if (attr == R.styleable.DocumentView_offsetY) {
                    layoutParams.setOffsetY(a.getDimension(attr, 0f));
                } else if (attr == R.styleable.DocumentView_hypen) {
                    layoutParams.setHyphen(a.getString(attr));
                } else if (attr == R.styleable.DocumentView_maxLines) {
                    layoutParams.setMaxLines(a.getInt(attr, Integer.MAX_VALUE));
                } else if (attr == R.styleable.DocumentView_lineHeightMultiplier) {
                    layoutParams.setLineHeightMulitplier(a.getFloat(attr, 1.0f));
                } else if (attr == R.styleable.DocumentView_textAlignment) {
                    layoutParams.setTextAlignment(TextAlignment.getById(a.getInt(attr, TextAlignment.LEFT.getId())));
                } else if (attr == R.styleable.DocumentView_reverse) {
                    layoutParams.setReverse(a.getBoolean(attr, false));
                } else if (attr == R.styleable.DocumentView_wordSpacingMultiplier) {
                    layoutParams.setWordSpacingMultiplier(a.getFloat(attr, 1.0f));
                } else if (attr == R.styleable.DocumentView_textColor) {
                    setColor(a.getColor(attr, Color.BLACK));
                } else if (attr == R.styleable.DocumentView_textSize) {
                    setTextSize(a.getDimension(attr, mPaint.getTextSize()));
                } else if (attr == R.styleable.DocumentView_textStyle) {
                    int style = a.getInt(attr, 0);
                    mPaint.setFakeBoldText((style & 1) > 0);
                    mPaint.setUnderlineText(((style >> 1) & 1) > 0);
                    mPaint.setStrikeThruText(((style >> 2) & 1) > 0);
                } else if (attr == R.styleable.DocumentView_textTypefacePath) {
                    setTypeface(Typeface.createFromAsset(getResources().getAssets(), a.getString(attr)));
                } else if (attr == R.styleable.DocumentView_antialias) {
                    mPaint.setAntiAlias(a.getBoolean(attr, true));
                } else if (attr == R.styleable.DocumentView_textSubpixel) {
                    mPaint.setSubpixelText(a.getBoolean(attr, true));
                } else if (attr == R.styleable.DocumentView_text) {
                    mLayout.setText(a.getString(attr));
                } else if (attr == R.styleable.DocumentView_cacheConfig) {
                    setCacheConfig(CacheConfig.getById(a.getInt(attr, CacheConfig.AUTO_QUALITY.getId())));
                }
            }

            a.recycle();

        } else {
            this.mLayout = getDocumentLayoutInstance(type, mPaint);
        }

    }

    public void destroyCache() {
        if (mCacheBitmap != null) {
            mCacheBitmap.recycle();
            mCacheBitmap = null;
        }
    }

    public void setTextSize(float textSize) {
        mPaint.setTextSize(textSize);
    }

    public void setColor(int textColor) {
        mPaint.setColor(textColor);
    }

    public void setTypeface(Typeface typeface) {
        mPaint.setTypeface(typeface);
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

    public CharSequence getText() {
        return this.mLayout.getText();
    }

    public void setText(CharSequence text) {
        this.mLayout.setText(text);
        requestLayout();
    }

    public DocumentLayout.LayoutParams getDocumentLayoutParams() {
        return this.mLayout.getLayoutParams();
    }

    public DocumentLayout getLayout() {
        return this.mLayout;
    }

    public CacheConfig getCacheConfig() {
        return mCacheConfig;
    }

    public void setCacheConfig(CacheConfig quality) {
        mCacheConfig = quality;
    }

    @Override
    public void invalidate() {
        destroyCache();
        super.invalidate();
    }

    @Override
    public void postInvalidate() {
        destroyCache();
        super.postInvalidate();
    }

    @Override
    public void requestLayout() {
        this.mLayout.getLayoutParams().invalidate();
        super.requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        this.mLayout.getLayoutParams().setParentWidth((float) width);
        this.mLayout.measure();
        this.setMeasuredDimension(width, this.mLayout.getMeasuredHeight());
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected final void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Android studio render
        if (isInEditMode()) {
            return;
        }

        boolean cacheEnabled = mCacheConfig != CacheConfig.NO_CACHE;

        // Active canas needs to be set
        // based on mCacheEnabled
        Canvas activeCanvas;

        // Set the active canvas based on
        // whether cache is enabled
        if (cacheEnabled) {
            if (mCacheBitmap != null) {
                // Draw to the OS provided canvas
                // if the cache is not empty
                canvas.drawBitmap(mCacheBitmap, 0, 0, mPaint);
                return;
            } else {
                // Create a bitmap and set the activeCanvas
                // to the one derived from the bitmap
                mCacheBitmap = Bitmap.createBitmap(getWidth(), getHeight(), mCacheConfig.getConfig());
                activeCanvas = new Canvas(mCacheBitmap);
            }
        } else {
            // Active canvas is the OS
            // provided canvas
            activeCanvas = canvas;
        }

        onLayoutDraw(activeCanvas);

        if (cacheEnabled) {
            // Draw the cache onto the OS provided
            // canvas.
            canvas.drawBitmap(mCacheBitmap, 0, 0, mPaint);
        }
    }

    protected void onLayoutDraw(Canvas canvas) {
        this.mLayout.draw(canvas);
    }

    public static enum CacheConfig {
        NO_CACHE(null, 0), AUTO_QUALITY(Config.ARGB_4444, 1), LOW_QUALITY(Config.RGB_565, 2), HIGH_QUALITY(Config.ARGB_4444, 3);

        private final Config mConfig;
        private final int mId;

        private CacheConfig(Config config, int id) {
            mConfig = config;
            mId = id;
        }

        public static CacheConfig getById(int id) {
            switch (id) {
                default:
                case 0:
                    return NO_CACHE;
                case 1:
                    return AUTO_QUALITY;
                case 2:
                    return LOW_QUALITY;
                case 3:
                    return HIGH_QUALITY;
            }
        }

        private Config getConfig() {
            return mConfig;
        }

        public int getId() {
            return mId;
        }
    }
}