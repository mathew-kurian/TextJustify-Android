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
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ScrollView;

import com.bluejamesbond.text.style.TextAlignment;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

@SuppressWarnings("unused")
public class DocumentView extends ScrollView {

    public static final float FADE_IN_DURATION_MS = 250f;
    public static final int FADE_IN_STEP_MS = 35;

    public static final int PLAIN_TEXT = 0;
    public static final int FORMATTED_TEXT = 1;

    private static Lock eglBitmapHeightLock;
    private static int eglBitmapHeight;

    private IDocumentLayout layout;
    private TextPaint paint;
    private View dummyView;
    private volatile MeasureTask measureTask;
    private volatile MeasureTaskState measureState;

    // Caching content
    private CacheConfig cacheConfig;
    private CacheBitmap cacheBitmapTop;
    private CacheBitmap cacheBitmapBottom;

    static {
        eglBitmapHeightLock = new ReentrantLock();
        eglBitmapHeight = -1;
    }

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

        try {
            eglBitmapHeightLock.lock();

            if (DocumentView.eglBitmapHeight == -1) {
                DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                DocumentView.eglBitmapHeight = Math.min(Math.max(metrics.heightPixels, metrics.widthPixels) * 7 / 6, getMaxTextureSize());
            }

        } catch (Exception e) {
            eglBitmapHeightLock.unlock();
            e.printStackTrace();
        }

        cacheConfig = CacheConfig.AUTO_QUALITY;
        paint = new TextPaint();
        dummyView = new View(context);
        measureState = MeasureTaskState.START;

        // Initialize paint
        initPaint(this.paint);

        // Set default padding
        setPadding(0, 0, 0, 0);

        addView(dummyView);

        if (attrs != null && !isInEditMode()) {
            TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.DocumentView);

            final int N = a.getIndexCount();
            boolean layoutSet = false;

            // find and set project layout
            for (int i = 0; i < N; ++i) {
                int attr = a.getIndex(i);
                if (R.styleable.DocumentView_textFormat == attr) {
                    this.layout = getDocumentLayoutInstance(a.getInt(attr, DocumentView.PLAIN_TEXT), paint);
                    layoutSet = true;
                    break;
                }
            }

            if (!layoutSet) {
                this.layout = getDocumentLayoutInstance(DocumentView.PLAIN_TEXT, paint);
            }

            StringDocumentLayout.LayoutParams layoutParams = this.layout.getLayoutParams();

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
                    layoutParams.setLineHeightMultiplier(a.getFloat(attr, 1.0f));
                } else if (attr == R.styleable.DocumentView_textAlignment) {
                    layoutParams.setTextAlignment(TextAlignment.getById(a.getInt(attr, TextAlignment.LEFT.getId())));
                } else if (attr == R.styleable.DocumentView_reverse) {
                    layoutParams.setReverse(a.getBoolean(attr, false));
                } else if (attr == R.styleable.DocumentView_wordSpacingMultiplier) {
                    layoutParams.setWordSpacingMultiplier(a.getFloat(attr, 1.0f));
                } else if (attr == R.styleable.DocumentView_textColor) {
                    setColor(a.getColor(attr, Color.BLACK));
                } else if (attr == R.styleable.DocumentView_textSize) {
                    setTextSize(a.getDimension(attr, paint.getTextSize()));
                } else if (attr == R.styleable.DocumentView_textStyle) {
                    int style = a.getInt(attr, 0);
                    paint.setFakeBoldText((style & 1) > 0);
                    paint.setUnderlineText(((style >> 1) & 1) > 0);
                    paint.setStrikeThruText(((style >> 2) & 1) > 0);
                } else if (attr == R.styleable.DocumentView_textTypefacePath) {
                    setTypeface(Typeface.createFromAsset(getResources().getAssets(), a.getString(attr)));
                } else if (attr == R.styleable.DocumentView_antialias) {
                    paint.setAntiAlias(a.getBoolean(attr, true));
                } else if (attr == R.styleable.DocumentView_textSubpixel) {
                    paint.setSubpixelText(a.getBoolean(attr, true));
                } else if (attr == R.styleable.DocumentView_text) {
                    layout.setText(a.getString(attr));
                } else if (attr == R.styleable.DocumentView_cacheConfig) {
                    setCacheConfig(CacheConfig.getById(a.getInt(attr, CacheConfig.AUTO_QUALITY.getId())));
                }
            }

            a.recycle();

        } else {
            this.layout = getDocumentLayoutInstance(type, paint);
        }
    }

    public void destroyCache() {
        if (cacheBitmapTop != null) {
            cacheBitmapTop.recycle();
            cacheBitmapTop = null;
        }

        if (cacheBitmapBottom != null) {
            cacheBitmapBottom.recycle();
            cacheBitmapBottom = null;
        }
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

    public IDocumentLayout getDocumentLayoutInstance(int type, TextPaint paint) {
        switch (type) {
            case FORMATTED_TEXT:
                return new SpannableDocumentLayout(getContext(), paint);
            default:
            case PLAIN_TEXT:
                return new StringDocumentLayout(getContext(), paint);
        }
    }

    public CharSequence getText() {
        return this.layout.getText();
    }

    public void setText(CharSequence text) {
        this.layout.setText(text);
        requestLayout();
    }

    public StringDocumentLayout.LayoutParams getDocumentLayoutParams() {
        return this.layout.getLayoutParams();
    }

    public IDocumentLayout getLayout() {
        return this.layout;
    }

    public CacheConfig getCacheConfig() {
        return cacheConfig;
    }

    public void setCacheConfig(CacheConfig quality) {
        cacheConfig = quality;
    }

    @Override
    public void requestLayout() {
        measureState = MeasureTaskState.START;
        super.requestLayout();
    }

    @SuppressWarnings("DrawAllocation")
    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int width = MeasureSpec.getSize(widthMeasureSpec);

        switch (measureState) {
            case AWAIT:
                break;
            case FINISH:
                dummyView.setMinimumWidth(width);
                dummyView.setMinimumHeight(layout.getMeasuredHeight());
                measureState = MeasureTaskState.AWAIT;
                break;
            case START:
                if (measureTask != null) {
                    measureTask.cancel(true);
                    measureTask = null;
                }
                measureTask = new MeasureTask(width);
                measureTask.execute();
                measureState = MeasureTaskState.AWAIT;
                break;

        }
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected final void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Android studio render
        if (isInEditMode()) {
            return;
        }

        boolean cacheEnabled = cacheConfig != CacheConfig.NO_CACHE && layout.getMeasuredHeight() > getHeight();

        if (cacheEnabled) {

            if (cacheBitmapTop == null) {
                cacheBitmapTop = new CacheBitmap(getWidth(), eglBitmapHeight, cacheConfig.getConfig());
            }

            if (cacheBitmapBottom == null) {
                cacheBitmapBottom = new CacheBitmap(getWidth(), eglBitmapHeight, cacheConfig.getConfig());
            }

            final int scrollTop = getScrollY();
            final int scrollBottom = scrollTop + getHeight();

            final CacheBitmap top = scrollTop % (eglBitmapHeight * 2) < eglBitmapHeight ? cacheBitmapTop : cacheBitmapBottom;
            final CacheBitmap bottom = scrollBottom % (eglBitmapHeight * 2) >= eglBitmapHeight ? cacheBitmapBottom : cacheBitmapTop;

            final int startTop = scrollTop - (scrollTop % (eglBitmapHeight * 2)) + (top == cacheBitmapTop ? 0 : eglBitmapHeight);

            boolean postInvalidate;

            if (top == bottom) {

                if (startTop != top.getStart()) {
                    top.setStart(startTop);
                    top.drawInBackground(new Runnable() {
                        @Override
                        public void run() {
                            drawLayout(new Canvas(bottom.getBitmap()), startTop, startTop + eglBitmapHeight, true);
                        }
                    });
                }

                postInvalidate = drawCacheToView(canvas, top, startTop);

            } else {

                final int startBottom = startTop + eglBitmapHeight;

                if (startTop != top.getStart()) {
                    top.setStart(startTop);
                    top.drawInBackground(new Runnable() {
                        @Override
                        public void run() {
                            drawLayout(new Canvas(top.getBitmap()), startTop, startTop + eglBitmapHeight, true);
                        }
                    });
                }

                if (startBottom != bottom.getStart()) {
                    bottom.setStart(startBottom);
                    bottom.drawInBackground(new Runnable() {
                        @Override
                        public void run() {
                            drawLayout(new Canvas(bottom.getBitmap()), startBottom, startBottom + eglBitmapHeight, true);
                        }
                    });
                }

                postInvalidate = drawCacheToView(canvas, top, startTop) | drawCacheToView(canvas, bottom, startBottom);
            }

            if (postInvalidate) {
                postInvalidateDelayed(FADE_IN_STEP_MS);
            }

        } else {
            drawLayout(canvas, 0, layout.getMeasuredHeight(), false);
        }
    }

    protected void drawLayout(Canvas canvas, int startY, int endY, boolean isCache) {
        if (isCache) {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            if (layout.isDebugging()) {
                int lastColor = paint.getColor();
                float lastStrokeWidth = paint.getStrokeWidth();
                Paint.Style lastStyle = paint.getStyle();
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(8);
                paint.setColor(Color.GREEN);
                canvas.drawRect(0, 0, getWidth(), canvas.getHeight(), paint);
                paint.setStrokeWidth(lastStrokeWidth);
                paint.setColor(lastColor);
                paint.setStyle(lastStyle);
            }
        }

        layout.draw(canvas, startY, endY);
    }

    protected boolean drawCacheToView(Canvas canvas, CacheBitmap cache, int y) {
        if (cache.isReady()) {
            int lastAlpha = paint.getAlpha();
            paint.setAlpha(cache.getAlpha());
            canvas.drawBitmap(cache.getBitmap(), 0, y, paint);
            paint.setAlpha(lastAlpha);
            return cache.getAlpha() < 255;
        }

        return false;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (measureTask != null) {
            measureTask.cancel(true);
            measureTask = null;
            measureState = MeasureTaskState.START;
        }

        if (cacheBitmapTop != null) {
            cacheBitmapTop.recycle();
            cacheBitmapTop = null;
        }

        if (cacheBitmapBottom != null) {
            cacheBitmapBottom.recycle();
            cacheBitmapBottom = null;
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

    enum MeasureTaskState {
        AWAIT, FINISH, START
    }

    private class MeasureTask extends AsyncTask<Void, Void, Void> {
        public MeasureTask(float parentWidth) {
            layout.getLayoutParams().setParentWidth(parentWidth);
        }

        @Override
        protected Void doInBackground(Void... params) {
            layout.measure();
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            measureTask = null;
            measureState = MeasureTaskState.FINISH;
            DocumentView.super.requestLayout();
        }
    }

    private class CacheBitmap {

        private long drawFadeInStartTime;
        private Bitmap bitmap;
        private int start;
        private volatile boolean drawCompleted;
        private volatile CacheDrawTask drawTask;
        private volatile int alpha;

        public CacheBitmap(int w, int h, Bitmap.Config config) {
            bitmap = Bitmap.createBitmap(w, h, config);
            start = -1;
            drawCompleted = false;
        }

        public int getAlpha() {
            return (int) Math.min(255f * (float) (System.currentTimeMillis() - drawFadeInStartTime) / FADE_IN_DURATION_MS, 255f);
        }

        public void drawInBackground(Runnable runnable) {
            if (drawTask != null) {
                drawTask.cancel(true);
                drawTask = null;
            }

            drawCompleted = false;
            alpha = 0;
            drawTask = new CacheDrawTask(runnable);
            drawTask.execute();
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public void setBitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public boolean isReady() {
            return drawCompleted;
        }

        public void recycle() {
            if (drawTask != null) {
                drawTask.cancel(true);
                drawTask = null;
                drawCompleted = false;
            }

            bitmap.recycle();
            bitmap = null;
        }

        private class CacheDrawTask extends AsyncTask<Void, Void, Void> {
            private Runnable drawRunnable;

            public CacheDrawTask(Runnable runnable) {
                drawRunnable = runnable;
            }

            @Override
            protected Void doInBackground(Void... params) {
                drawRunnable.run();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                drawFadeInStartTime = System.currentTimeMillis();
                drawCompleted = true;
                invalidate();
            }
        }
    }
}