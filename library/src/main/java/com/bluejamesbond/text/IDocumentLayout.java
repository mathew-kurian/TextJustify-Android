package com.bluejamesbond.text;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.Toast;

import com.bluejamesbond.text.hyphen.IHyphenator;
import com.bluejamesbond.text.style.TextAlignment;

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
 * IDocumentLayout.java
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

@SuppressWarnings("unused")
public abstract class IDocumentLayout {

    // Main content
    protected CharSequence text;
    protected int lineCount;
    protected int measuredHeight;
    protected boolean textChange;
    protected LayoutParams params;
    protected TextPaint paint;
    private Toast toast;
    private DisplayMetrics displayMetrics;

    @SuppressLint("ShowToast")
    public IDocumentLayout(Context context, TextPaint textPaint) {
        paint = textPaint;
        text = "";
        measuredHeight = 0;
        lineCount = 0;
        textChange = false;
        displayMetrics = context.getResources().getDisplayMetrics();
        toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);

        params = new LayoutParams();
        params.setLineHeightMultiplier(1.0f);
        params.setHyphenated(false);
        params.setReverse(false);
    }

    protected void showToast(String s) {
        toast.setText(s);
        toast.show();
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
        text = text == null ? new SpannableString("") : new SpannableString(text);

        if (this.text.equals(text)) {
            return;
        }

        this.text = text;
        this.textChange = true;

        onTextChange();
    }

    public int getMeasuredHeight() {
        return measuredHeight;
    }

    protected void onTextNull() {
        params.changed = false;
        measuredHeight = (int) (params.insetPaddingTop + params.insetPaddingBottom);
    }

    public int getLineCount() {
        return lineCount;
    }

    public boolean measure(IProgress<Float> progress, ICancel<Boolean> cancelled) {

        if (!params.changed && !textChange) {
            return true;
        }

        params.loadToPaint(paint);

        if (text == null) {
            text = new SpannableString("");
        } else if (!(text instanceof Spannable)) {
            text = new SpannableString(text);
        }

        return onMeasure(progress, cancelled);
    }

    protected abstract boolean onMeasure(IProgress<Float> progress, ICancel<Boolean> cancelled);

    public void draw(Canvas canvas, int startTop, int startBottom) {

        params.loadToPaint(paint);

        onDraw(canvas, startTop, startBottom);
    }

    protected abstract void onDraw(Canvas canvas, int startTop, int startBottom);

    public abstract float getTokenAscent(int tokenIndex);

    public abstract float getTokenDescent(int tokenIndex);

    public abstract int getTokenForVertical(float y, TokenPosition position);

    public abstract int getLineForToken(int tokenIndex);

    public abstract int getTokenStart(int tokenIndex);

    public abstract int getTokenEnd(int tokenIndex);

    public abstract float getTokenTopAt(int tokenIndex);

    public abstract CharSequence getTokenTextAt(int index);

    public abstract boolean isTokenized();

    public abstract void onLayoutParamsChange();

    public abstract void onTextChange();

    public static enum TokenPosition {
        START_OF_LINE, END_OF_LINE
    }

    public static interface IProgress<T> {
        public void onUpdate(T val);
    }

    public static interface ICancel<T> {
        public T isCancelled();
    }

    public class LayoutParams {

        /**
         * All the customizable parameters
         */
        protected IHyphenator hyphenator = null;
        protected Float insetPaddingLeft = 0.0f;
        protected Float insetPaddingTop = 0.0f;
        protected Float insetPaddingBottom = 0.0f;
        protected Float insetPaddingRight = 0.0f;
        protected Float parentWidth = 800.0f;
        protected Float offsetX = 0.0f;
        protected Float offsetY = 0.0f;

        protected Boolean debugging = false;
        protected Float wordSpacingMultiplier = 1.0f;
        protected Float lineHeightMultiplier = 0.0f;
        protected Boolean hyphenated = false;
        protected Boolean reverse = false;
        protected Boolean subpixelText = false;
        protected Boolean antialias = false;
        protected Integer maxLines = Integer.MAX_VALUE;
        protected String hyphen = "-";
        protected TextAlignment textAlignment = TextAlignment.LEFT;

        protected Boolean textUnderline = false;
        protected Boolean textStrikeThru = false;
        protected Boolean textFakeBold = false;
        protected Typeface textTypeface = Typeface.DEFAULT;
        protected Float rawTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, displayMetrics);
        protected Integer textColor = Color.BLACK;
        protected Integer textLinkColor = Color.parseColor("#ff05c5cf");

        /**
         * If any settings have changed.
         */
        protected boolean changed = false;

        public Integer getTextLinkColor() {
            return textLinkColor;
        }

        public void setTextLinkColor(Integer textLinkColor) {
            this.textLinkColor = textLinkColor;
        }

        public void loadToPaint(Paint paint) {
            paint.setTextSize(rawTextSize);
            paint.setFakeBoldText(textFakeBold);
            paint.setStrikeThruText(textStrikeThru);
            paint.setColor(textColor);
            paint.setTypeface(textTypeface);
            paint.setUnderlineText(textUnderline);
            paint.setAntiAlias(antialias);
            paint.setSubpixelText(subpixelText);
        }

        public Float getWordSpacingMultiplier() {
            return wordSpacingMultiplier;
        }

        public void setWordSpacingMultiplier(float wordSpacingMultiplier) {
            if (this.wordSpacingMultiplier.equals(wordSpacingMultiplier)) {
                return;
            }

            this.wordSpacingMultiplier = wordSpacingMultiplier;
            invalidate();
        }

        public TextAlignment getTextAlignment() {
            return textAlignment;
        }

        public void setTextAlignment(/*@NotNull*/ TextAlignment textAlignment) {
            if (this.textAlignment == textAlignment) {
                return;
            }

            this.textAlignment = textAlignment;
            invalidate();
        }

        public IHyphenator getHyphenator() {
            return hyphenator;
        }

        public void setHyphenator(IHyphenator hyphenator) {
            if (hyphenator == null) {
                return;
            }

            if (this.hyphenator != null && this.hyphenator.equals(hyphenator)) {
                return;
            }

            this.hyphenator = hyphenator;
            invalidate();
        }

        public float getInsetPaddingLeft() {
            return insetPaddingLeft;
        }

        public void setInsetPaddingLeft(float insetPaddingLeft) {
            if (this.insetPaddingLeft.equals(insetPaddingLeft)) {
                return;
            }

            this.insetPaddingLeft = insetPaddingLeft;
            invalidate();
        }

        public float getInsetPaddingTop() {
            return insetPaddingTop;
        }

        public void setInsetPaddingTop(float insetPaddingTop) {
            if (this.insetPaddingTop.equals(insetPaddingTop)) {
                return;
            }

            this.insetPaddingTop = insetPaddingTop;
            invalidate();
        }

        public float getInsetPaddingBottom() {
            return insetPaddingBottom;
        }

        public void setInsetPaddingBottom(float insetPaddingBottom) {
            if (this.insetPaddingBottom.equals(insetPaddingBottom)) {
                return;
            }

            this.insetPaddingBottom = insetPaddingBottom;
            invalidate();
        }

        public float getInsetPaddingRight() {
            return insetPaddingRight;
        }

        public void setInsetPaddingRight(float insetPaddingRight) {
            if (this.insetPaddingRight.equals(insetPaddingRight)) {
                return;
            }

            this.insetPaddingRight = insetPaddingRight;
            invalidate();
        }

        public float getParentWidth() {
            return parentWidth;
        }

        public void setParentWidth(float parentWidth) {
            if (this.parentWidth.equals(parentWidth)) {
                return;
            }

            this.parentWidth = parentWidth;
            invalidate();
        }

        public float getOffsetX() {
            return offsetX;
        }

        public void setOffsetX(float offsetX) {
            this.offsetX = offsetX;
        }

        public float getOffsetY() {
            return offsetY;
        }

        public void setOffsetY(float offsetY) {
            this.offsetY = offsetY;
        }

        public float getLineHeightMultiplier() {
            return lineHeightMultiplier;
        }

        public void setLineHeightMultiplier(float lineHeightMultiplier) {
            if (this.lineHeightMultiplier.equals(lineHeightMultiplier)) {
                return;
            }

            this.lineHeightMultiplier = lineHeightMultiplier;
            invalidate();
        }

        public boolean isHyphenated() {
            return hyphenated;
        }

        public void setHyphenated(boolean hyphenated) {
            if (this.hyphenated.equals(hyphenated)) {
                return;
            }

            this.hyphenated = hyphenated && hyphenator != null;
            invalidate();
        }

        public boolean isReverse() {
            return reverse;
        }

        public void setReverse(boolean reverse) {
            if (this.reverse.equals(reverse)) {
                return;
            }

            if (reverse) {
                textAlignment = TextAlignment.RIGHT;
            }

            this.reverse = reverse;
            invalidate();
        }

        public int getMaxLines() {
            return maxLines;
        }

        public void setMaxLines(int maxLines) {
            if (this.maxLines.equals(maxLines)) {
                return;
            }

            this.maxLines = maxLines;
            invalidate();
        }

        public String getHyphen() {
            return hyphen;
        }

        public void setHyphen(/*@NotNull*/ String hyphen) {
            if (this.hyphen.equals(hyphen)) {
                return;
            }

            this.hyphen = hyphen;
            invalidate();
        }

        public boolean hasChanged() {
            return this.changed;
        }

        public void invalidate() {
            this.changed = true;
            onLayoutParamsChange();
        }

        public boolean isTextUnderline() {
            return textUnderline;
        }

        public void setTextUnderline(boolean underline) {
            if (this.textUnderline.equals(underline)) {
                return;
            }

            this.textUnderline = underline;
            onLayoutParamsChange();
        }

        public boolean isTextStrikeThru() {
            return textStrikeThru;
        }

        public void setTextStrikeThru(boolean strikeThru) {
            if (this.textStrikeThru.equals(strikeThru)) {
                return;
            }

            this.textStrikeThru = strikeThru;
            onLayoutParamsChange();
        }

        public boolean isTextFakeBold() {
            return textFakeBold;
        }

        public void setTextFakeBold(boolean fakeBold) {
            if (this.textFakeBold.equals(fakeBold)) {
                return;
            }

            this.textFakeBold = fakeBold;
            invalidate();
        }

        public Typeface getTextTypeface() {
            return textTypeface;
        }

        public void setTextTypeface(Typeface typeface) {
            if (this.textTypeface.equals(typeface)) {
                return;
            }

            this.textTypeface = typeface;
            invalidate();
        }

        public void setTextSize(int unit, float size) {
            setRawTextSize(TypedValue.applyDimension(unit, size, displayMetrics));
        }

        public float getTextSize() {
            return rawTextSize;
        }

        public void setTextSize(float size) {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        }

        public void setRawTextSize(float textSize) {
            if (this.rawTextSize.equals(textSize)) {
                return;
            }

            this.rawTextSize = textSize;
            invalidate();
        }

        public int getTextColor() {
            return textColor;
        }

        public void setTextColor(int textColor) {
            if (this.textColor.equals(textColor)) {
                return;
            }

            this.textColor = textColor;
            onLayoutParamsChange();
        }

        public boolean isDebugging() {
            return debugging;
        }

        public void setDebugging(Boolean debugging) {
            if (this.debugging.equals(debugging)) {
                return;
            }

            this.debugging = debugging;
            onLayoutParamsChange();
        }

        public boolean isTextSubPixel() {
            return subpixelText;
        }

        public void setTextSubPixel(boolean subpixelText) {

            if (this.subpixelText.equals(subpixelText)) {
                return;
            }

            this.subpixelText = subpixelText;
        }

        public boolean isAntiAlias() {
            return antialias;
        }

        public void setAntialias(boolean antialias) {

            if (this.antialias.equals(antialias)) {
                return;
            }

            this.antialias = antialias;
        }
    }
}
