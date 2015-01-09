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
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.opengl.GLES10;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

import com.bluejamesbond.text.style.TextAlignment;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

@SuppressWarnings("unused")
public class DocumentView extends ScrollView {

    public static final int PLAIN_TEXT = 0;
    public static final int FORMATTED_TEXT = 1;
    private static Integer bitmapHeight = -1;
    private static int MAX_TEXTURE_SIZE = 0;
    private DocumentLayout mLayout;
    private TextPaint mPaint;
    private View mView;
    // Caching content
    private CacheConfig mCacheConfig = CacheConfig.NO_CACHE;

    private CacheBitmap mCacheBitmapTop;
    private CacheBitmap mCacheBitmapBottom;

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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DocumentView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, PLAIN_TEXT);
    }

    private static int getMaxTextureSize() {
        // Code from
        // http://stackoverflow.com/a/26823209/1100536

        // Safe minimum default size
        final int GL_MAX_TEXTURE_SIZE = 2048;

        // Get EGL Display
        EGL10 egl = (EGL10) EGLContext.getEGL();
        EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

        // Initialise
        int[] version = new int[2];
        egl.eglInitialize(display, version);

        // Query total number of configurations
        int[] totalConfigurations = new int[1];
        egl.eglGetConfigs(display, null, 0, totalConfigurations);

        // Query actual list configurations
        EGLConfig[] configurationsList = new EGLConfig[totalConfigurations[0]];
        egl.eglGetConfigs(display, configurationsList, totalConfigurations[0], totalConfigurations);

        int[] textureSize = new int[1];
        int maximumTextureSize = 0;

        // Iterate through all the configurations to located the maximum texture size
        for (int i = 0; i < totalConfigurations[0]; i++) {
            // Only need to check for width since opengl textures are always squared
            egl.eglGetConfigAttrib(display, configurationsList[i], EGL10.EGL_MAX_PBUFFER_WIDTH, textureSize);

            // Keep track of the maximum texture size
            if (maximumTextureSize < textureSize[0])
                maximumTextureSize = textureSize[0];
        }

        // Release
        egl.eglTerminate(display);

        // Return largest texture size found, or default
        return Math.max(maximumTextureSize, GL_MAX_TEXTURE_SIZE);
    }

    private void init(Context context, AttributeSet attrs, int type) {

        synchronized (bitmapHeight) {
            if (DocumentView.bitmapHeight.equals(-1)) {
                DocumentView.bitmapHeight = Math.min(context.getResources().getDisplayMetrics().heightPixels * 7 / 6, getMaxTextureSize());
            }
        }

        this.mPaint = new TextPaint();
        this.mView = new View(context);

        // Initialize mPaint
        initPaint(this.mPaint);

        // Set default padding
        setPadding(0, 0, 0, 0);

        addView(mView);

        if (MAX_TEXTURE_SIZE == 0) {
            int[] maxSize = new int[1];
            GLES10.glGetIntegerv(GL10.GL_MAX_TEXTURE_SIZE, maxSize, 0);
            MAX_TEXTURE_SIZE = maxSize[0];
        }

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
        if (mCacheBitmapTop != null) {
            mCacheBitmapTop.recycle();
            mCacheBitmapTop = null;
        }

        if (mCacheBitmapBottom != null) {
            mCacheBitmapBottom.recycle();
            mCacheBitmapBottom = null;
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
                return new SpannedDocumentLayout(getContext(), paint);
            default:
            case PLAIN_TEXT:
                return new DocumentLayout(getContext(), paint);
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
    public void requestLayout() {
        if (this.mLayout != null) {
            this.mLayout.getLayoutParams().invalidate();
        }
        super.requestLayout();
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {

        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int height = MeasureSpec.getSize(heightMeasureSpec);

        mLayout.getLayoutParams().setParentWidth((float) width);
        mLayout.measure();
        mView.setMinimumHeight(mLayout.getMeasuredHeight());
        mView.setMinimumWidth(width);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
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

        if (cacheEnabled) {

            if (mCacheBitmapTop == null) {
                mCacheBitmapTop = new CacheBitmap(getWidth(), bitmapHeight, mCacheConfig.getConfig());
            }

            if (mCacheBitmapBottom == null) {
                mCacheBitmapBottom = new CacheBitmap(getWidth(), bitmapHeight, mCacheConfig.getConfig());
            }

            int scrollTop = getScrollY();
            int scrollBottom = scrollTop + getHeight();

            CacheBitmap top = scrollTop % (bitmapHeight * 2) < bitmapHeight ? mCacheBitmapTop : mCacheBitmapBottom;
            CacheBitmap bottom = scrollBottom % (bitmapHeight * 2) >= bitmapHeight ? mCacheBitmapBottom : mCacheBitmapTop;

            if (top == bottom) {
                int startTop = scrollTop - (scrollTop % (bitmapHeight * 2)) + (top == mCacheBitmapTop ? 0 : bitmapHeight);

                if (startTop != top.getStart()) {
                    Canvas bitCanvas = new Canvas(bottom.getBitmap());
                    bitCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    top.setStart(startTop);
                    mLayout.draw(bitCanvas, startTop, startTop + bitmapHeight);
                    debugCache(bitCanvas);
                }

                canvas.drawBitmap(top.getBitmap(), 0, startTop, mPaint);

            } else {

                int startTop = scrollTop - (scrollTop % (bitmapHeight * 2)) + (top == mCacheBitmapTop ? 0 : bitmapHeight);
                int startBottom = startTop + bitmapHeight;

                if (startTop != top.getStart()) {
                    Canvas bitCanvas = new Canvas(top.getBitmap());
                    bitCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    top.setStart(startTop);
                    mLayout.draw(bitCanvas, startTop, startTop + bitmapHeight);
                    debugCache(bitCanvas);
                }

                if (startBottom != bottom.getStart()) {
                    Canvas bitCanvas = new Canvas(bottom.getBitmap());
                    bitCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    bottom.setStart(startBottom);
                    mLayout.draw(bitCanvas, startBottom, startBottom + bitmapHeight);
                    debugCache(bitCanvas);
                }

                canvas.drawBitmap(top.getBitmap(), 0, startTop, mPaint);
                canvas.drawBitmap(bottom.getBitmap(), 0, startBottom, mPaint);
            }

        } else {
            mLayout.draw(canvas, 0, mLayout.getMeasuredHeight());
        }
    }

    private void debugCache(Canvas canvas){
        if (mLayout.isDebugging()) {
            int lastColor = mPaint.getColor();
            float lastStrokeWidth = mPaint.getStrokeWidth();
            Paint.Style lastStyle = mPaint.getStyle();
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(8);
            mPaint.setColor(Color.GREEN);
            canvas.drawRect(0, 0, getWidth(), canvas.getHeight(), mPaint);
            mPaint.setStrokeWidth(lastStrokeWidth);
            mPaint.setColor(lastColor);
            mPaint.setStyle(lastStyle);
        }

    }

    public static enum CacheConfig {
        NO_CACHE(null, 0), AUTO_QUALITY(Config.ARGB_4444, 1), LOW_QUALITY(Config.RGB_565, 2), HIGH_QUALITY(Config.ARGB_8888, 3), GRAYSCALE(Config.ALPHA_8, 4);

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
                case 4:
                    return GRAYSCALE;
            }
        }

        private Config getConfig() {
            return mConfig;
        }

        public int getId() {
            return mId;
        }
    }

    private class CacheBitmap {

        Bitmap mBitmap;
        int mStart;
        int mHeight;

        public CacheBitmap(int width, int height, Bitmap.Config config) {
            mBitmap = Bitmap.createBitmap(width, height, config);
            mStart = -1;
            mHeight = -1;
        }

        public Bitmap getBitmap() {
            return mBitmap;
        }

        public void setBitmap(Bitmap bitmap) {
            this.mBitmap = bitmap;
        }

        public int getStart() {
            return mStart;
        }

        public void setStart(int start) {
            this.mStart = start;
        }

        public int getHeight() {
            return mHeight;
        }

        public void recycle() {
            mBitmap.recycle();
            mBitmap = null;
        }
    }
}