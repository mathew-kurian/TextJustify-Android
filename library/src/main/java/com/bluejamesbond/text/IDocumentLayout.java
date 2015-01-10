package com.bluejamesbond.text;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.SpannableString;
import android.text.TextPaint;
import android.widget.Toast;

import com.bluejamesbond.text.hyphen.Hyphenator;
import com.bluejamesbond.text.style.TextAlignment;

import java.util.Arrays;

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
 * IDocumentLayout.java
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

@SuppressWarnings("unused")
public abstract class IDocumentLayout {

    // Main content
    protected CharSequence text;
    protected int mLineCount = 0;
    protected int measuredHeight = 0;
    protected boolean debugging = false;
    protected boolean textChange = true;
    protected LayoutParams params;
    protected TextPaint paint;
    private Toast toast;

    public IDocumentLayout(Context context, TextPaint textPaint) {
        paint = textPaint;
        text = "";

        toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);

        params = new LayoutParams();
        params.setLineHeightMulitplier(1.0f);
        params.setHyphenated(false);
        params.setReverse(false);

        measuredHeight = 0;
    }

    protected void showToast(String s){
        toast.setText(s);
        toast.show();
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
        this.text = text == null ? new SpannableString("") : text;
        this.textChange = true;
    }

    public int getMeasuredHeight() {
        return measuredHeight;
    }

    protected void onTextNull() {
        params.changed = false;
        measuredHeight = (int) (params.paddingTop + params.paddingBottom);
    }

    public int getLineCount() {
        return mLineCount;
    }

    public abstract void measure();

    public abstract void draw(Canvas canvas, int startTop, int startBottom);

    public abstract int getTokenIndex(float y, TokenPosition position);

    public abstract float getTokenTopAt(int index);

    public abstract String getTokenStringAt(int index);

    public abstract boolean isTokenized();

    public static enum TokenPosition {
        FIRST_OCCURRENCE, LAST_OCCURRENCE
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
}
