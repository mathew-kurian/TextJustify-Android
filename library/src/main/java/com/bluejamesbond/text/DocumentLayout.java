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
 * DocumentLayout.java
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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.text.TextPaint;

import com.bluejamesbond.text.hyphen.Hyphenator;
import com.bluejamesbond.text.style.TextAlignment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

@SuppressWarnings("unused")
public class DocumentLayout {

    private static Integer bitmapHeight = -1;

    // Debugging
    protected boolean debugging = false;
    // Basic client-set properties
    protected LayoutParams params;
    protected boolean textChange = true;
    // Rendering
    protected TextPaint paint;
    // Measurement output
    protected int measuredHeight;
    // Main content
    private String text;
    // Parsing objects
    private Token[] mTokens;
    private ConcurrentModifiableLinkedList<String> chunks;

    private CacheBitmap mCacheBitmapTop;
    private CacheBitmap mCacheBitmapBottom;

    public DocumentLayout(Context context, TextPaint paint) {

        synchronized (bitmapHeight) {
            if (DocumentLayout.bitmapHeight.equals(-1)) {
                DocumentLayout.bitmapHeight = Math.min(context.getResources().getDisplayMetrics().heightPixels * 7 / 6, getMaxTextureSize());
            }
        }

        this.paint = paint;
        this.text = "";

        params = new LayoutParams();
        params.setLineHeightMulitplier(1.0f);
        params.setHyphenated(false);
        params.setReverse(false);

        measuredHeight = 0;

        mTokens = new Token[0];
        chunks = new ConcurrentModifiableLinkedList<String>();
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

    public boolean isDebugging() {
        return debugging;
    }

    public void setDebugging(boolean debugging) {
        this.debugging = debugging;
    }

    public Paint getPaint() {
        return paint;
    }

    public LayoutParams getLayoutParams() {
        return params;
    }

    public CharSequence getText() {
        return this.text;
    }

    public void setText(CharSequence text) {
        this.text = text == null ? "" : text.toString();
        this.textChange = true;
    }

    private float getFontAscent() {
        return -paint.ascent() * params.lineHeightMulitplier;
    }

    private float getFontDescent() {
        return paint.descent() * params.lineHeightMulitplier;
    }

    public int getMeasuredHeight() {
        return measuredHeight;
    }

    protected void onTextNull() {
        params.changed = false;
        measuredHeight = (int) (params.paddingTop + params.paddingBottom);
    }

    public void measure() {

        if (!params.changed && !textChange) {
            return;
        }

        if (textChange) {
            chunks.clear();

            int start = 0;

            while (start > -1) {
                int next = text.indexOf('\n', start + 1);

                if (next < 0) {
                    chunks.add(text.substring(start, text.length()));
                } else {
                    chunks.add(text.substring(start, next));
                    next += 1;
                }

                start = next;
            }

            textChange = false;
        }

        // Empty out any existing mTokens
        List<Token> tokens = new ConcurrentModifiableLinkedList<Token>();

        Paint paint = getPaint();
        paint.setTextAlign(Paint.Align.LEFT);

        // Get basic settings widget properties
        int lineNumber = 0;
        float width = params.parentWidth - params.paddingRight - params.paddingLeft;
        float lineHeight = getFontAscent() + getFontDescent();
        float x;
        float y = params.paddingTop + getFontAscent();
        float spaceOffset = paint.measureText(" ") * params.wordSpacingMultiplier;

        for (String paragraph : chunks) {

            if (lineNumber >= params.maxLines) {
                break;
            }

            // Start at x = 0 for drawing text
            x = params.paddingLeft;

            String trimParagraph = paragraph.trim();

            // If the line contains only spaces or line breaks
            if (trimParagraph.length() == 0) {
                tokens.add(new LineBreak(lineNumber++));
                y += lineHeight;
                continue;
            }

            float wrappedWidth = paint.measureText(trimParagraph);

            // Line fits, then don't wrap
            if (wrappedWidth < width) {
                // activeCanvas.drawText(paragraph, x, y, paint);
                tokens.add(new SingleLine(lineNumber++, x, y, trimParagraph));
                y += lineHeight;
                continue;
            }

            // Allow leading spaces
            int start = 0;
            int overallCounter = 0;

            ConcurrentModifiableLinkedList<Unit> units = tokenize(paragraph);
            ListIterator<Unit> unitIterator = units.listIterator();
            ListIterator<Unit> justifyIterator = units.listIterator();

            while (true) {

                x = params.paddingLeft;

                // Line doesn't fit, then apply wrapping
                LineAnalysis format = fit(justifyIterator, start, spaceOffset, width);
                int tokenCount = format.end - format.start;
                boolean leftOverTokens = justifyIterator.hasNext();

                if (tokenCount == 0 && leftOverTokens) {
                    new DocumentException("Cannot fit word(s) into one line. Font size too large?")
                            .printStackTrace();
                    return;
                }

                // Draw each word here
                float offset = 0;

                switch (params.textAlignment) {
                    case CENTER: {
                        x += format.remainWidth / 2;
                        break;
                    }
                    case RIGHT: {
                        x += format.remainWidth;
                        break;
                    }
                    case JUSTIFIED: {
                        offset = tokenCount > 2 && leftOverTokens ?
                                format.remainWidth / (tokenCount - 1) : 0;
                        break;
                    }
                    default: {
                        // LEFT
                    }
                }

                for (int i = format.start; i < format.end; i++) {
                    Unit unit = unitIterator.next();

                    unit.x = x;
                    unit.y = y;
                    unit.lineNumber = lineNumber;
                    x += offset + paint.measureText(unit.unit) + spaceOffset;

                    // Add to all mTokens
                    tokens.add(unit);
                }

                // Increment to next line
                y += lineHeight;

                // Next line
                lineNumber++;

                // If there are more mTokens leftover,
                // continue
                if (leftOverTokens) {

                    // Next start index for mTokens
                    start = format.end;

                    continue;
                }

                // If all fit, then continue to next
                // paragraph
                break;
            }
        }

        Token[] tokensArr = new Token[tokens.size()];
        tokens.toArray(tokensArr);
        tokens.clear();

        mTokens = tokensArr;
        params.changed = false;
        measuredHeight = (int) (y - getFontAscent() + params.paddingBottom);

        if (mCacheBitmapTop == null) {
            mCacheBitmapTop = new CacheBitmap((int) params.getParentWidth(), bitmapHeight, Bitmap.Config.ARGB_8888);
            mCacheBitmapBottom = new CacheBitmap((int) params.getParentWidth(), bitmapHeight, Bitmap.Config.ARGB_8888);
        }
    }

    public void draw(Canvas canvas, int scrollX, int scrollTop, int viewHeight) {

        int scrollBottom = scrollTop + viewHeight;

        CacheBitmap top = scrollTop % (bitmapHeight * 2) < bitmapHeight ? mCacheBitmapTop : mCacheBitmapBottom;
        CacheBitmap bottom = scrollBottom % (bitmapHeight * 2) >= bitmapHeight ? mCacheBitmapBottom : mCacheBitmapTop;

        if (top == bottom) {
            int startTop = scrollTop - (scrollTop % (bitmapHeight * 2)) + (top == mCacheBitmapTop ? 0 : bitmapHeight);

            if (startTop != top.getStart()) {
                top.setStart(startTop);
                drawTokens(top, -startTop, resolveTokenIndex(startTop), resolveTokenIndex(startTop + bitmapHeight));
            }

            canvas.drawBitmap(top.getBitmap(), 0, startTop, paint);

        } else {

            int startTop = scrollTop - (scrollTop % (bitmapHeight * 2)) + (top == mCacheBitmapTop ? 0 : bitmapHeight);
            int startBottom = startTop + bitmapHeight;

            if (startTop != top.getStart()) {
                top.setStart(startTop);
                drawTokens(top, -startTop, resolveTokenIndex(startTop), resolveTokenIndex(startTop + bitmapHeight));
            }

            if (startBottom != bottom.getStart()) {
                bottom.setStart(startBottom);
                drawTokens(bottom, -startBottom, resolveTokenIndex(startBottom), resolveTokenIndex(startBottom + bitmapHeight));
            }

            canvas.drawBitmap(top.getBitmap(), 0, startTop, paint);
            canvas.drawBitmap(bottom.getBitmap(), 0, startBottom, paint);
        }
    }

    private void drawTokens(CacheBitmap bitmap, float offsetY, int tokenStart, int tokenEnd) {

        Canvas canvas = new Canvas(bitmap.getBitmap());

        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        if (debugging) {
            int lastColor = paint.getColor();
            float lastStrokeWidth = paint.getStrokeWidth();
            Paint.Style lastStyle = paint.getStyle();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(8);
            paint.setColor(bitmap == mCacheBitmapTop ? Color.RED : Color.GREEN);
            canvas.drawRect(0, 0, params.parentWidth, bitmapHeight, paint);
            paint.setStrokeWidth(lastStrokeWidth);
            paint.setColor(lastColor);
            paint.setStyle(lastStyle);
        }
        for (int i = Math.max(0, tokenStart - 50); i < tokenEnd + 50 && i < mTokens.length; i++) {
            mTokens[i].draw(canvas, offsetY, paint, params);
        }
    }

    private int resolveTokenIndex(int y) {
        return (int) ((float) mTokens.length * (float) y / (float) getMeasuredHeight());
    }

    private ConcurrentModifiableLinkedList<Unit> tokenize(String s) {

        ConcurrentModifiableLinkedList<Unit> units = new ConcurrentModifiableLinkedList<Unit>();

        // If empty string, just return one group
        if (s.trim().length() <= 1) {
            units.add(new Unit(s));
            return units;
        }

        int start = 0;
        boolean charSearch = s.charAt(0) == ' ';

        for (int i = 1; i < s.length(); i++) {
            // If the end add the word group
            if (i + 1 == s.length()) {
                units.add(new Unit(s.substring(start, i + 1)));
                start = i + 1;
            }
            // Search for the start of non-space
            else if (charSearch && s.charAt(i) != ' ') {
                String substring = s.substring(start, i);
                if (substring.length() != 0) {
                    units.add(new Unit(s.substring(start, i)));
                }
                start = i;
                charSearch = false;
            }
            // Search for the end of non-space
            else if (!charSearch && s.charAt(i) == ' ') {
                units.add(new Unit(s.substring(start, i)));
                start = i + 1; // Skip the space
                charSearch = true;
            }
        }

        return units;
    }

    /**
     * Returns the length that the specified CharSequence would have if
     * spaces and control characters were trimmed from the start and end,
     * as by {@link String#trim}.
     */
    protected int getTrimmedLength(CharSequence s, int start, int end) {
        while (start < end && s.charAt(start) <= ' ') {
            start++;
        }

        int endCpy = end;
        while (endCpy > start && s.charAt(endCpy - 1) <= ' ') {
            endCpy--;
        }

        return endCpy - start;
    }

    /**
     * By contract, parameter "block" must not have any line breaks
     */

    private LineAnalysis fit(ListIterator<Unit> iterator, int startIndex, float spaceOffset,
                             float availableWidth) {

        int i = startIndex;

        // Greedy search to see if the word
        // can actually fit on a line
        while (iterator.hasNext()) {

            // Get word
            Unit unit = iterator.next();
            String word = unit.unit;
            float wordWidth = paint.measureText(word);
            float remainingWidth = availableWidth - wordWidth;

            // Word does not fit in line
            if (remainingWidth < 0 && word.trim().length() != 0) {

                // Handle hyphening in the event
                // the current word does not fit
                if (params.hyphenated) {

                    float lastFormattedPartialWidth = 0.0f;
                    String lastFormattedPartial = null;
                    String lastConcatPartial = null;
                    String concatPartial = "";

                    ArrayList<String> partials = params.hyphenator.hyphenate(word);

                    for (String partial : partials) {

                        concatPartial += partial;

                        // Create the hyphenated word
                        // aka. partial
                        String formattedPartial = concatPartial + params.hyphen;
                        float formattedPartialWidth = paint
                                .measureText(formattedPartial);

                        // See if the partial fits
                        if (availableWidth - formattedPartialWidth > 0) {
                            lastFormattedPartial = formattedPartial;
                            lastFormattedPartialWidth = formattedPartialWidth;
                            lastConcatPartial = concatPartial;
                        }
                        // If the partial doesn't fit
                        else {

                            // Check if the lastPartial
                            // was even set
                            if (lastFormattedPartial != null) {

                                unit.unit = lastFormattedPartial;
                                iterator.add(new Unit(word.substring(lastConcatPartial.length())));
                                availableWidth -= lastFormattedPartialWidth;

                                iterator.previous();

                                return new LineAnalysis(startIndex, i + 1, availableWidth);
                            }
                        }
                    }
                }

                // Redo this word on the next run
                iterator.previous();

                return new LineAnalysis(startIndex, i, availableWidth + spaceOffset);

            }
            // Word fits in the line
            else {

                availableWidth -= wordWidth + spaceOffset;

                // NO remaining space
                if (remainingWidth == 0) {
                    return new LineAnalysis(startIndex, i + 1, availableWidth
                            + spaceOffset);
                }
            }

            // Increment i
            i++;
        }

        return new LineAnalysis(startIndex, i, availableWidth + spaceOffset);
    }

    public static class LayoutParams {

        /**
         * All the customizable parameters
         */
        protected Hyphenator hyphenator = null;
        protected Float paddingLeft = 0.0f;
        protected Float paddingTop = 0.0f;
        protected Float paddingBottom = 0.0f;
        protected Float paddingRight = 0.0f;
        protected Float parentWidth = 800.0f;
        protected Float offsetX = 0.0f;
        protected Float offsetY = 0.0f;

        protected Float wordSpacingMultiplier = 1.0f;
        protected Float lineHeightMulitplier = 0.0f;
        protected Boolean hyphenated = false;
        protected Boolean reverse = false;
        protected Integer maxLines = Integer.MAX_VALUE;
        protected String hyphen = "-";
        protected TextAlignment textAlignment = TextAlignment.LEFT;

        /**
         * If any settings have changed.
         */
        protected boolean changed = false;

        public int hashCode() {
            return Arrays.hashCode(
                    new Object[]{hyphenator, paddingLeft, paddingTop, paddingBottom, paddingRight,
                            parentWidth, offsetX, offsetX,
                            lineHeightMulitplier, hyphenated, reverse, maxLines, hyphen, textAlignment, wordSpacingMultiplier});
        }

        public Float getWordSpacingMultiplier() {
            return wordSpacingMultiplier;
        }

        public void setWordSpacingMultiplier(Float wordSpacingMultiplier) {
            if (this.wordSpacingMultiplier.equals(wordSpacingMultiplier)) {
                return;
            }

            this.wordSpacingMultiplier = wordSpacingMultiplier;
            this.changed = true;
        }

        public TextAlignment getTextAlignment() {
            return textAlignment;
        }

        public void setTextAlignment(TextAlignment textAlignment) {
            if (this.textAlignment == textAlignment) {
                return;
            }

            this.textAlignment = textAlignment;
            this.changed = true;
        }

        public Hyphenator getHyphenator() {
            return hyphenator;
        }

        public void setHyphenator(Hyphenator hyphenator) {
            if (hyphenator == null) {
                return;
            }

            if (this.hyphenator != null && this.hyphenator.equals(hyphenator)) {
                return;
            }

            this.hyphenator = hyphenator;
            this.changed = true;
        }

        public float getPaddingLeft() {
            return paddingLeft;
        }

        public void setPaddingLeft(Float paddingLeft) {
            if (this.paddingLeft.equals(paddingLeft)) {
                return;
            }

            this.paddingLeft = paddingLeft;
            this.changed = true;
        }

        public float getPaddingTop() {
            return paddingTop;
        }

        public void setPaddingTop(Float paddingTop) {
            if (this.paddingTop.equals(paddingTop)) {
                return;
            }

            this.paddingTop = paddingTop;
            this.changed = true;
        }

        public float getPaddingBottom() {
            return paddingBottom;
        }

        public void setPaddingBottom(Float paddingBottom) {
            if (this.paddingBottom.equals(paddingBottom)) {
                return;
            }

            this.paddingBottom = paddingBottom;
            this.changed = true;
        }

        public float getPaddingRight() {
            return paddingRight;
        }

        public void setPaddingRight(Float paddingRight) {
            if (this.paddingRight.equals(paddingRight)) {
                return;
            }

            this.paddingRight = paddingRight;
            this.changed = true;
        }

        public float getParentWidth() {
            return parentWidth;
        }

        public void setParentWidth(Float parentWidth) {
            if (this.parentWidth.equals(parentWidth)) {
                return;
            }

            this.parentWidth = parentWidth;
            this.changed = true;
        }

        public float getOffsetX() {
            return offsetX;
        }

        public void setOffsetX(Float offsetX) {
            this.offsetX = offsetX;
        }

        public float getOffsetY() {
            return offsetY;
        }

        public void setOffsetY(Float offsetY) {
            this.offsetY = offsetY;
        }

        public float getLineHeightMulitplier() {
            return lineHeightMulitplier;
        }

        public void setLineHeightMulitplier(Float lineHeightMulitplier) {
            if (this.lineHeightMulitplier.equals(lineHeightMulitplier)) {
                return;
            }

            this.lineHeightMulitplier = lineHeightMulitplier;
            this.changed = true;
        }

        public boolean isHyphenated() {
            return hyphenated;
        }

        public void setHyphenated(Boolean hyphenated) {
            if (this.hyphenated.equals(hyphenated)) {
                return;
            }

            this.hyphenated = hyphenated && hyphenator != null;
            this.changed = true;
        }

        public boolean isReverse() {
            return reverse;
        }

        public void setReverse(Boolean reverse) {
            if (this.reverse.equals(reverse)) {
                return;
            }

            if (reverse) {
                textAlignment = TextAlignment.RIGHT;
            }

            this.reverse = reverse;
            this.changed = true;
        }

        public int getMaxLines() {
            return maxLines;
        }

        public void setMaxLines(Integer maxLines) {
            if (maxLines.equals(maxLines)) {
                return;
            }

            this.maxLines = maxLines;
            this.changed = true;
        }

        public String getHyphen() {
            return hyphen;
        }

        public void setHyphen(String hyphen) {
            if (this.hyphen.equals(hyphen)) {
                return;
            }

            this.hyphen = hyphen;
            this.changed = true;
        }

        public boolean hasChanged() {
            return this.changed;
        }

        public void invalidate() {
            this.changed = true;
        }
    }

    private static abstract class Token {

        public int lineNumber;

        public Token(int lineNumber) {
            this.lineNumber = lineNumber;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        abstract void draw(Canvas canvas, float offsetY, Paint paint, LayoutParams params);

        public boolean isVisible(int scrollX, int scrollY, int viewHeight) {
            return false;
        }
    }

    private static class Unit extends Token {

        public float x;
        public float y;
        public String unit;

        public Unit(String unit) {
            super(0);
            this.unit = unit;
        }

        public Unit(int lineNumber, float x, float y, String unit) {
            super(lineNumber);
            this.x = x;
            this.y = y;
            this.unit = unit;
        }

        @Override
        void draw(Canvas canvas, float offsetY, Paint paint, LayoutParams params) {
            canvas.drawText(unit, x + params.getOffsetX(), y + params.getOffsetY() + offsetY, paint);
        }

        @Override
        public boolean isVisible(int scrollX, int scrollY, int viewHeight) {
            return y >= scrollY && y <= scrollY + viewHeight;
        }
    }

    private static class LineBreak extends Token {
        public LineBreak(int lineNumber) {
            super(lineNumber);
        }

        @Override
        void draw(Canvas canvas, float offsetY, Paint paint, LayoutParams params) {
        }
    }

    private static class SingleLine extends Unit {
        public SingleLine(int lineNumber, float x, float y, String unit) {
            super(lineNumber, x, y, unit);
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
    }

    /**
     * Class and function to process wrapping Implements a greedy algorithm to
     * fit as many words as possible into one line
     */

    private class LineAnalysis {

        public int start;
        public int end;
        public float remainWidth;

        public LineAnalysis(int start, int end, float remainWidth) {
            this.start = start;
            this.end = end;
            this.remainWidth = remainWidth;
        }
    }
}

@SuppressWarnings("serial")
class DocumentException extends Exception {
    public DocumentException(String message) {
        super(message);
    }
}