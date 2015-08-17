package com.bluejamesbond.text;

/*
 * Copyright 2015 Mathew Kurian
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
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
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
 * Date: 1/27/15 3:35 AM
 */

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import com.bluejamesbond.text.style.TextAlignment;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

@SuppressWarnings("unused")
public class DocumentView extends ScrollView {

    public static final int PLAIN_TEXT = 0;
    public static final int FORMATTED_TEXT = 1;
    private static final ITween LINEAR_EASE_IN;

    private static final Object eglBitmapHeightLock;
    private static int eglBitmapHeight;
    protected ILayoutProgressListener layoutProgressListener;

    static {
        eglBitmapHeightLock = new Object();
        eglBitmapHeight = -1;
        LINEAR_EASE_IN = new ITween() {
            @Override
            public float get(float t, float b, float c, float d) {
                return c * t / d + b;
            }
        };
    }

    private IDocumentLayout layout;
    private TextPaint paint;
    private TextPaint cachePaint;
    private View viewportView;
    private ITween fadeInTween;
    private int fadeInDuration = 250;
    private int fadeInAnimationStepDelay = 35;
    private volatile MeasureTask measureTask;
    private volatile MeasureTaskState measureState;
    private int minimumHeight;
    private int orientation;
    private CacheConfig cacheConfig;
    private CacheBitmap cacheBitmapTop;
    private CacheBitmap cacheBitmapBottom;
    private boolean disallowInterceptTouch;

    public DocumentView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initDocumentView(context, attrs, PLAIN_TEXT);
    }

    public DocumentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initDocumentView(context, attrs, PLAIN_TEXT);
    }

    public DocumentView(Context context) {
        super(context);
        initDocumentView(context, null, PLAIN_TEXT);
    }

    public DocumentView(Context context, int type) {
        super(context);
        initDocumentView(context, null, type);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DocumentView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initDocumentView(context, attrs, PLAIN_TEXT);
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

    public boolean isDisallowInterceptTouch() {
        return disallowInterceptTouch;
    }

    public void setDisallowInterceptTouch(boolean disallowInterceptTouch) {
        this.disallowInterceptTouch = disallowInterceptTouch;
    }

    protected synchronized void drawLayout(Canvas canvas, int startY, int endY, boolean isCache) {

        if (isCache) {
            // clear canvas
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        }

        layout.draw(canvas, startY, endY);

        // onDraw border around
        if (getDocumentLayoutParams().isDebugging()) {
            IDocumentLayout.LayoutParams params = getDocumentLayoutParams();
            int lastColor = paint.getColor();
            float lastStrokeWidth = paint.getStrokeWidth();
            Paint.Style lastStyle = paint.getStyle();

            // border
            paint.setColor(Color.MAGENTA);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(4);

            float left = params.insetPaddingLeft;
            float top = params.insetPaddingTop >= startY && params.insetPaddingTop < endY ? params.insetPaddingTop : 0;
            float right = params.parentWidth - params.insetPaddingRight;
            float bottom = (bottom = layout.getMeasuredHeight() - params.insetPaddingBottom) >= startY && bottom < endY ? bottom - startY : canvas.getHeight();

            canvas.drawRect(left, top, right, bottom, paint);

            paint.setStrokeWidth(lastStrokeWidth);
            paint.setColor(lastColor);
            paint.setStyle(lastStyle);
        }
    }

    public int getFadeInAnimationStepDelay() {
        return fadeInAnimationStepDelay;
    }

    public void setFadeInAnimationStepDelay(int delay) {
        fadeInAnimationStepDelay = delay;
    }

    public int getFadeInDuration() {
        return fadeInDuration;
    }

    public void setFadeInDuration(int duration) {
        fadeInDuration = duration;
    }

    public ITween getFadeInTween() {
        return fadeInTween;
    }

    public void setFadeInTween(ITween tween) {
        fadeInTween = tween;
    }

    private void initDocumentView(Context context, AttributeSet attrs, int type) {

        synchronized (eglBitmapHeightLock) {
            if (DocumentView.eglBitmapHeight == -1) {
                DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                DocumentView.eglBitmapHeight = Math.min(Math.max(metrics.heightPixels, metrics.widthPixels) * 7 / 6, getMaxTextureSize());
            }
        }

        disallowInterceptTouch = false;
        fadeInTween = LINEAR_EASE_IN;
        cacheConfig = CacheConfig.AUTO_QUALITY;
        paint = new TextPaint();
        cachePaint = new TextPaint();
        viewportView = new View(context);
        measureState = MeasureTaskState.START;

        // Initialize paint
        initPaint(paint);

        // Set default padding
        setPadding(0, 0, 0, 0);

        addView(viewportView);

        if (attrs != null && !isInEditMode()) {
            TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.DocumentView);

            final int N = a.getIndexCount();

            // find and onUpdate project layout
            layout = getDocumentLayoutInstance(a.getInt(R.styleable.DocumentView_documentView_textFormat, DocumentView.PLAIN_TEXT), paint);

            IDocumentLayout.LayoutParams layoutParams = layout.getLayoutParams();

            for (int i = 0; i < N; ++i) {

                int attr = a.getIndex(i);

                if (attr == R.styleable.DocumentView_documentView_insetPadding) {
                    Float pad = a.getDimension(attr, 0f);
                    layoutParams.setInsetPaddingLeft(pad);
                    layoutParams.setInsetPaddingBottom(pad);
                    layoutParams.setInsetPaddingRight(pad);
                    layoutParams.setInsetPaddingTop(pad);
                } else if (attr == R.styleable.DocumentView_documentView_insetPaddingLeft) {
                    layoutParams.setInsetPaddingLeft(a.getDimension(attr, layoutParams.getInsetPaddingLeft()));
                } else if (attr == R.styleable.DocumentView_documentView_insetPaddingBottom) {
                    layoutParams.setInsetPaddingBottom(a.getDimension(attr, layoutParams.getInsetPaddingBottom()));
                } else if (attr == R.styleable.DocumentView_documentView_insetPaddingRight) {
                    layoutParams.setInsetPaddingRight(a.getDimension(attr, layoutParams.getInsetPaddingRight()));
                } else if (attr == R.styleable.DocumentView_documentView_insetPaddingTop) {
                    layoutParams.setInsetPaddingTop(a.getDimension(attr, layoutParams.getInsetPaddingTop()));
                } else if (attr == R.styleable.DocumentView_documentView_offsetX) {
                    layoutParams.setOffsetX(a.getDimension(attr, layoutParams.getOffsetX()));
                } else if (attr == R.styleable.DocumentView_documentView_offsetY) {
                    layoutParams.setOffsetY(a.getDimension(attr, layoutParams.getOffsetY()));
                } else if (attr == R.styleable.DocumentView_documentView_hyphen) {
                    layoutParams.setHyphen(a.getString(attr));
                } else if (attr == R.styleable.DocumentView_documentView_maxLines) {
                    layoutParams.setMaxLines(a.getInt(attr, layoutParams.getMaxLines()));
                } else if (attr == R.styleable.DocumentView_documentView_lineHeightMultiplier) {
                    layoutParams.setLineHeightMultiplier(a.getFloat(attr, layoutParams.getLineHeightMultiplier()));
                } else if (attr == R.styleable.DocumentView_documentView_textAlignment) {
                    layoutParams.setTextAlignment(TextAlignment.getById(a.getInt(attr, layoutParams.getTextAlignment().getId())));
                } else if (attr == R.styleable.DocumentView_documentView_reverse) {
                    layoutParams.setReverse(a.getBoolean(attr, layoutParams.isReverse()));
                } else if (attr == R.styleable.DocumentView_documentView_wordSpacingMultiplier) {
                    layoutParams.setWordSpacingMultiplier(a.getFloat(attr, layoutParams.getWordSpacingMultiplier()));
                } else if (attr == R.styleable.DocumentView_documentView_textColor) {
                    layoutParams.setTextColor(a.getColor(attr, layoutParams.getTextColor()));
                } else if (attr == R.styleable.DocumentView_documentView_textSize) {
                    layoutParams.setRawTextSize(a.getDimension(attr, layoutParams.getTextSize()));
                } else if (attr == R.styleable.DocumentView_documentView_textStyle) {
                    int style = a.getInt(attr, 0);
                    layoutParams.setTextFakeBold((style & 1) > 0);
                    layoutParams.setTextUnderline(((style >> 1) & 1) > 0);
                    layoutParams.setTextStrikeThru(((style >> 2) & 1) > 0);
                } else if (attr == R.styleable.DocumentView_documentView_textTypefacePath) {
                    layoutParams.setTextTypeface(Typeface.createFromAsset(getResources().getAssets(), a.getString(attr)));
                } else if (attr == R.styleable.DocumentView_documentView_antialias) {
                    layoutParams.setAntialias(a.getBoolean(attr, layoutParams.isAntiAlias()));
                } else if (attr == R.styleable.DocumentView_documentView_textSubPixel) {
                    layoutParams.setTextSubPixel(a.getBoolean(attr, layoutParams.isTextSubPixel()));
                } else if (attr == R.styleable.DocumentView_documentView_text) {
                    layout.setText(a.getString(attr));
                } else if (attr == R.styleable.DocumentView_documentView_cacheConfig) {
                    setCacheConfig(CacheConfig.getById(a.getInt(attr, CacheConfig.AUTO_QUALITY.getId())));
                } else if (attr == R.styleable.DocumentView_documentView_progressBar) {
                    setProgressBar(a.getResourceId(R.styleable.DocumentView_documentView_progressBar, 0));
                } else if (attr == R.styleable.DocumentView_documentView_fadeInAnimationStepDelay) {
                    setFadeInAnimationStepDelay(a.getInteger(attr, getFadeInAnimationStepDelay()));
                } else if (attr == R.styleable.DocumentView_documentView_fadeInDuration) {
                    setFadeInDuration(a.getInteger(attr, getFadeInDuration()));
                } else if (attr == R.styleable.DocumentView_documentView_disallowInterceptTouch) {
                    setDisallowInterceptTouch(a.getBoolean(attr, isDisallowInterceptTouch()));
                }
            }

            a.recycle();

        } else {
            this.layout = getDocumentLayoutInstance(type, paint);
        }
    }

    protected void initPaint(Paint paint) {
        paint.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        paint.setTextSize(34);
        paint.setAntiAlias(true);
    }

    @Override
    protected void onAttachedToWindow() {
        orientation = getResources().getConfiguration().orientation;
        super.onAttachedToWindow();
    }

    public IDocumentLayout getDocumentLayoutInstance(int type, TextPaint paint) {
        switch (type) {
            case FORMATTED_TEXT:
                return new SpannableDocumentLayout(getContext(), paint) {
                    @Override
                    public void onLayoutParamsChange() {
                        invalidateCache();
                        postInvalidate();
                    }

                    @Override
                    public void onTextChange() {
                        invalidateCache();
                        requestLayout();
                    }
                };
            default:
            case PLAIN_TEXT:
                return new StringDocumentLayout(getContext(), paint) {
                    @Override
                    public void onLayoutParamsChange() {
                        invalidateCache();
                        postInvalidate();
                    }

                    @Override
                    public void onTextChange() {
                        invalidateCache();
                        requestLayout();
                    }
                };
        }
    }

    public void invalidateCache() {
        if (cacheBitmapTop != null) {
            cacheBitmapTop.setStart(-1);
        }

        if (cacheBitmapBottom != null) {
            cacheBitmapBottom.setStart(-1);
        }
    }

    public View getViewportView(){
        return viewportView;
    }

    public void setProgressBar(final int progressBarId) {
        setOnLayoutProgressListener(new DocumentView.ILayoutProgressListener() {

            private ProgressBar progressBar;

            @Override
            public void onCancelled() {
                progressBar.setProgress(progressBar.getMax());
                progressBar = null;
            }

            @Override
            public void onFinish() {
                progressBar.setProgress(progressBar.getMax());
                progressBar = null;
            }

            @Override
            public void onStart() {
                progressBar = (ProgressBar) ((Activity) getContext()).getWindow().getDecorView().findViewById(progressBarId);
                progressBar.setProgress(0);
            }

            @Override
            public void onProgressUpdate(float progress) {
                progressBar.setProgress((int) (progress * (float) progressBar.getMax()));
            }
        });
    }

    public void setOnLayoutProgressListener(ILayoutProgressListener listener) {
        layoutProgressListener = listener;
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

    @SuppressWarnings("DrawAllocation")
    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int width = MeasureSpec.getSize(widthMeasureSpec);

        switch (measureState) {
            case FINISH_AWAIT:
                break;
            case AWAIT:
                break;
            case FINISH:
                viewportView.setMinimumWidth(width);
                viewportView.setMinimumHeight(layout.getMeasuredHeight());
                measureState = MeasureTaskState.FINISH_AWAIT;

                if(cacheConfig != CacheConfig.NO_CACHE){
                    allocateResources();
                }

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

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        requestDisallowInterceptTouchEvent(disallowInterceptTouch);
        return super.onTouchEvent(ev);
    }

    @Override
    public void requestLayout() {
        measureState = MeasureTaskState.START;
        super.requestLayout();
    }

    @Override
    protected void onDetachedFromWindow() {
        freeResources();
        super.onDetachedFromWindow();
    }

    public void setProgressBar(final ProgressBar progressBar) {
        setOnLayoutProgressListener(new DocumentView.ILayoutProgressListener() {
            @Override
            public void onCancelled() {
                progressBar.setProgress(progressBar.getMax());
            }

            @Override
            public void onFinish() {
                progressBar.setProgress(progressBar.getMax());
            }

            @Override
            public void onStart() {
                progressBar.setProgress(0);
            }

            @Override
            public void onProgressUpdate(float progress) {
                progressBar.setProgress((int) (progress * (float) progressBar.getMax()));
            }
        });
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        if (orientation != newConfig.orientation) {
            orientation = newConfig.orientation;
            freeResources();
        }

        super.onConfigurationChanged(newConfig);
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

            allocateResources();

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

                postInvalidate = drawCacheToView(canvas, cachePaint, top, startTop);

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

                postInvalidate = drawCacheToView(canvas, cachePaint, top, startTop) | drawCacheToView(canvas, cachePaint, bottom, startBottom);
            }

            if (postInvalidate) {
                postInvalidateDelayed(fadeInAnimationStepDelay);
            }

        } else {
            drawLayout(canvas, 0, layout.getMeasuredHeight(), false);
        }
    }

    @Override
    public void setMinimumHeight(int minHeight) {
        minimumHeight = minHeight;
        viewportView.setMinimumHeight(minimumHeight);
    }

    public void allocateResources() {
        if (cacheBitmapTop == null) {
            cacheBitmapTop = new CacheBitmap(getWidth(), eglBitmapHeight, cacheConfig.getConfig());
        }

        if (cacheBitmapBottom == null) {
            cacheBitmapBottom = new CacheBitmap(getWidth(), eglBitmapHeight, cacheConfig.getConfig());
        }
    }

    protected boolean drawCacheToView(Canvas canvas, Paint paint, CacheBitmap cache, int y) {
        // onDraw only if cache is ready
        if (cache.isReady()) {
            int lastAlpha = paint.getAlpha();
            paint.setAlpha(cache.getAlpha());
            canvas.drawBitmap(cache.getBitmap(), 0, y, paint);
            paint.setAlpha(lastAlpha);
            return cache.getAlpha() < 255; // return true to invoke postInvalidateDelayed()
        }

        return false;
    }

    protected void freeResources() {
        viewportView.setMinimumHeight(minimumHeight);

        if (measureTask != null) {
            measureTask.cancel(true);
            measureTask = null;
            measureState = MeasureTaskState.START;
        }

        destroyCache();
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
        AWAIT, FINISH, START, FINISH_AWAIT
    }

    public static interface ILayoutProgressListener {
        public void onCancelled();

        public void onFinish();

        public void onStart();

        public void onProgressUpdate(float progress);
    }

    public static interface ITween {
        public float get(float t, float b, float c, float d);
    }

    public class MeasureTask extends AsyncTask<Void, Float, Boolean> {

        private IDocumentLayout.IProgress<Float> progress;
        private IDocumentLayout.ICancel<Boolean> cancelled;

        public MeasureTask(float parentWidth) {
            layout.getLayoutParams().setParentWidth(parentWidth);
            progress = new IDocumentLayout.IProgress<Float>() {
                @Override
                public void onUpdate(Float progress) {
                    if (layoutProgressListener != null) {
                        layoutProgressListener.onProgressUpdate(progress);
                    }
                }
            };
            cancelled = new IDocumentLayout.ICancel<Boolean>() {
                @Override
                public Boolean isCancelled() {
                    return MeasureTask.this.isCancelled();
                }
            };
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                return layout.measure(progress, cancelled);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPreExecute() {
            if (layoutProgressListener != null) {
                layoutProgressListener.onStart();
            }
        }


        @Override
        protected void onPostExecute(Boolean done) {
            if (!done || isCancelled()) {
                if (layoutProgressListener != null) {
                    layoutProgressListener.onCancelled();
                }
                return;
            }

            measureTask = null;
            measureState = MeasureTaskState.FINISH;
            DocumentView.super.requestLayout();

            if (layoutProgressListener != null) {
                layoutProgressListener.onFinish();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (layoutProgressListener != null) {
                layoutProgressListener.onCancelled();
            }
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
            return (int) Math.min(fadeInTween.get(System.currentTimeMillis() - drawFadeInStartTime, 0, 255f, fadeInDuration), 255f);
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

            if (bitmap != null) {
                bitmap.recycle();
                bitmap = null;
            }
        }

        public class CacheDrawTask extends AsyncTask<Void, Void, Void> {
            private Runnable drawRunnable;

            public CacheDrawTask(Runnable runnable) {
                drawRunnable = runnable;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
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
