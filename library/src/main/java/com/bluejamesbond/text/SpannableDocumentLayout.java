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
 * SpannedDocumentLayout.java
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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Layout;
import android.text.Spannable;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.LeadingMarginSpan;

import com.bluejamesbond.text.style.TextAlignment;
import com.bluejamesbond.text.style.TextAlignmentSpan;

import junit.framework.Assert;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

public class SpannableDocumentLayout extends IDocumentLayout {

    private static final int TOKEN_START = 0;
    private static final int TOKEN_END = 1;
    private static final int TOKEN_X = 2;
    private static final int TOKEN_Y = 3;
    private static final int TOKEN_ASCENT = 4;
    private static final int TOKEN_DESCENT = 5;
    private static final int TOKEN_LENGTH = 6;
    private TextPaint workPaint;
    private LinkedList<LeadingMarginSpanDrawParameters> mLeadMarginSpanDrawEvents;
    private int[] tokens;

    public SpannableDocumentLayout(Context context, TextPaint paint) {
        super(context, paint);
        workPaint = new TextPaint(paint);
        tokens = new int[0];
    }

    private static int pushToken(int[] tokens, int index, int start, int end, float x, float y,
                                 float ascent, float descent) {

        Assert.assertTrue(index % TOKEN_LENGTH == 0);

        tokens[index + TOKEN_START] = start;
        tokens[index + TOKEN_END] = end;
        tokens[index + TOKEN_X] = (int) x;
        tokens[index + TOKEN_Y] = (int) y;
        tokens[index + TOKEN_ASCENT] = (int) ascent;
        tokens[index + TOKEN_DESCENT] = (int) descent;
        return index + TOKEN_LENGTH;
    }

    private static int[] ammortizeArray(int[] array, int index) {
        if (index >= array.length) {
            int[] newArray = new int[array.length * 2];
            Arrays.fill(newArray, Integer.MAX_VALUE);
            System.arraycopy(array, 0, newArray, 0, array.length);
            return newArray;
        }
        return array;
    }

    private static LinkedList<Integer> tokenize(CharSequence source,
                                                int start,
                                                int end) {

        LinkedList<Integer> units = new LinkedList<>();

        if (start >= end) {
            return units;
        }

        boolean charSearch = source.charAt(start) == ' ';

        for (int i = start; i < end; i++) {
            // If the end add the word group
            if (i + 1 == end) {
                units.add(i + 1);
                start = i + 1;
            }
            // Search for the start of non-space
            else if (charSearch && source.charAt(i) != ' ') {
                if ((i - start) > 0) {
                    units.add(i);
                }
                start = i;
                charSearch = false;
            }
            // Search for the end of non-space
            else if (!charSearch && source.charAt(i) == ' ') {
                units.add(i);
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

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean onMeasure(IProgress<Float> progress, ICancel<Boolean> cancelled) {

        boolean done = true;
        float parentWidth = params.getParentWidth();
        float boundWidth =
                params.getParentWidth() - params.getInsetPaddingLeft() - params.getInsetPaddingRight();

        mLeadMarginSpanDrawEvents = new LinkedList<>();

        StaticLayout staticLayout = new StaticLayout(getText(), (TextPaint) getPaint(),
                (int) boundWidth, Layout.Alignment.ALIGN_NORMAL, 1, 0, false);
        int[] newTokens = new int[TOKEN_LENGTH * 1000];
        LeadingMarginSpan[] activeLeadSpans = new LeadingMarginSpan[0];
        HashMap<LeadingMarginSpan, Integer> leadSpans = new HashMap<>();
        TextAlignment defAlign = params.textAlignment;
        Spannable textCpy = (Spannable) this.text;
        Paint.FontMetricsInt fmi = paint.getFontMetricsInt();

        int maxTextIndex = textCpy.length() - 1;
        int lines = staticLayout.getLineCount();
        int enableLineBreak = 0;
        int index = 0;
        int lineNumber;

        float x;
        float y = params.insetPaddingTop;
        float left = params.insetPaddingLeft;
        float right = params.insetPaddingRight;
        float lineHeightAdd = params.lineHeightMultiplier;
        float lastAscent;
        float lastDescent;

        boolean isParaStart = true;
        boolean isReverse = params.reverse;

        for (lineNumber = 0; lineNumber < lines; lineNumber++) {

            if (cancelled.isCancelled()) {
                done = false;
                break;
            }

            progress.onUpdate((float) lineNumber / (float) lines);

            newTokens = ammortizeArray(newTokens, index);

            int start = staticLayout.getLineStart(lineNumber);
            int end = staticLayout.getLineEnd(lineNumber);

            float realWidth = boundWidth;

            if (debugging) {
                Console.log(start + " => " + end + " :: " + " " + -staticLayout.getLineAscent(lineNumber)
                        + " " + staticLayout.getLineDescent(lineNumber) + " " + textCpy.subSequence(start, end)
                        .toString());
            }

            // start == end => end of textCpy
            if (start == end || lineNumber >= params.maxLines) {
                break;
            }

            // Get textCpy alignment for the line
            TextAlignmentSpan[] textAlignmentSpans =
                    textCpy.getSpans(start, end, TextAlignmentSpan.class);
            TextAlignment lineTextAlignment = textAlignmentSpans.length == 0 ? defAlign :
                    textAlignmentSpans[0].getTextAlignment();

            // Calculate components of line height
            lastAscent = -staticLayout.getLineAscent(lineNumber);
            lastDescent = staticLayout.getLineDescent(lineNumber) + lineHeightAdd;

            // Line is ONLY a <br/> or \n
            if (start + 1 == end &&
                    (Character.getNumericValue(textCpy.charAt(start)) == -1 ||
                            textCpy.charAt(start) == '\n')) {

                // Line break indicates a new paragraph
                // is next
                isParaStart = true;

                // Use the line-height of the next line
                y += enableLineBreak * (-staticLayout.getLineAscent(lineNumber + 1) + staticLayout
                        .getLineDescent(lineNumber + 1));

                // Don't ignore the next line breaks
                enableLineBreak = 1;

                continue;

            } else {
                // Ignore the next line break
                enableLineBreak = 0;
            }

            x = lineTextAlignment == TextAlignment.RIGHT ? right : left;
            y += lastAscent;

            // Line CONTAINS a \n
            boolean isParaEnd = end == maxTextIndex ||
                    textCpy.charAt(Math.min(end, maxTextIndex)) == '\n' ||
                    textCpy.charAt(end - 1) == '\n';

            if (isParaEnd) {
                enableLineBreak = 1;
            }

            // LeadingMarginSpan block
            if (isParaStart) {

                // Process LeadingMarginSpan
                activeLeadSpans = textCpy.getSpans(start, end, LeadingMarginSpan.class);

                // Set up all the spans
                if (activeLeadSpans.length > 0) {
                    for (LeadingMarginSpan leadSpan : activeLeadSpans) {
                        if (!leadSpans.containsKey(leadSpan)) {

                            // Default margin is everything
                            int marginLineCount = -1;

                            {
                                if (leadSpan instanceof LeadingMarginSpan.LeadingMarginSpan2) {
                                    LeadingMarginSpan.LeadingMarginSpan2 leadSpan2 =
                                            ((LeadingMarginSpan.LeadingMarginSpan2) leadSpan);
                                    marginLineCount = leadSpan2.getLeadingMarginLineCount();
                                }
                            }

                            leadSpans.put(leadSpan, marginLineCount);
                        }
                    }
                }
            }

            float totalMargin = 0.0f;

            int top = (int) (y - lastAscent);
            int baseline = (int) (y);
            int bottom = (int) (y + lastDescent);

            for (LeadingMarginSpan leadSpan : activeLeadSpans) {

                // TOKEN_X based on alignment
                float calcX = x;

                // LineAlignment
                int lineAlignmentVal = 1;

                if (lineTextAlignment == TextAlignment.RIGHT) {
                    lineAlignmentVal = -1;
                    calcX = parentWidth - x;
                }

                // Get current line count
                int spanLines = leadSpans.get(leadSpan);

                // Update only if the valid next valid
                if (spanLines > 0 || spanLines == -1) {
                    leadSpans.put(leadSpan, spanLines == -1 ? -1 : spanLines - 1);
                    mLeadMarginSpanDrawEvents
                            .add(new LeadingMarginSpanDrawParameters(leadSpan, (int) calcX,
                                    lineAlignmentVal, top, baseline,
                                    bottom, start, end, isParaStart));

                    // Is margin required?
                    totalMargin += leadSpan.getLeadingMargin(isParaStart);
                }
            }

            x += totalMargin;
            realWidth -= totalMargin;

            // Disable/enable new paragraph
            isParaStart = isParaEnd;

            // TextAlignmentSpan block
            if (isParaEnd && lineTextAlignment == TextAlignment.JUSTIFIED) {
                lineTextAlignment = isReverse ? TextAlignment.RIGHT : TextAlignment.LEFT;
            }

            if (debugging) {
                Console.log(String.format("Align: %s, X: %fpx, Y: %fpx, PWidth: %fpx",
                        lineTextAlignment, x, y, parentWidth));
            }

            switch (lineTextAlignment) {
                case RIGHT: {
                    float lineWidth = Styled.measureText(paint, workPaint, textCpy, start, end, fmi);
                    index = pushToken(newTokens, index, start, end, parentWidth - x - lineWidth, y,
                            lastAscent, lastDescent);
                    y += lastDescent;
                    continue;
                }
                case CENTER: {
                    float lineWidth = Styled.measureText(paint, workPaint, textCpy, start, end, fmi);
                    index = pushToken(newTokens, index, start, end, x + (realWidth - lineWidth) / 2,
                            y, lastAscent, lastDescent);
                    y += lastDescent;
                    continue;
                }
                case LEFT: {
                    index = pushToken(newTokens, index, start, end, x, y, lastAscent, lastDescent);
                    y += lastDescent;
                    continue;
                }
            }

            // FIXME: Space at the end of each line, possibly due to scrollbar offset
            LinkedList<Integer> tokenized = tokenize(textCpy, start, end - 1);

            // If one long word without any spaces
            if (tokenized.size() == 1) {
                int stop = tokenized.get(0);

                // If not all space, process
                // characters individually
                if (getTrimmedLength(textCpy, start, stop) != 0) {

                    float[] textWidths = new float[stop - start];
                    float sum = 0.0f, textsOffset = 0.0f, offset;
                    int m = 0;

                    Styled.getTextWidths(paint, workPaint, textCpy, start,
                            stop, textWidths, fmi);

                    for (float tw : textWidths) {
                        sum += tw;
                    }

                    offset = (realWidth - sum) / (textWidths.length - 1);

                    for (int k = start; k < stop; k++) {
                        index = pushToken(newTokens, index, k, k + 1,
                                x + textsOffset + (offset * m), y, lastAscent, lastDescent);
                        newTokens = ammortizeArray(newTokens, index);
                        textsOffset += textWidths[m++];
                    }
                }
            }
            //  Handle multiple words
            else {

                int m = 1;
                int indexOffset = 0;
                int startIndex = index;
                int reqSpaces = (tokenized.size() - 1) * TOKEN_LENGTH;
                int rtlZero = 0;
                float rtlRight = 0;
                float rtlMul = 1;
                float lineWidth = 0;
                float offset;

                if (isReverse) {
                    indexOffset = -2 * TOKEN_LENGTH;
                    rtlRight = parentWidth;
                    rtlMul = -1;
                    rtlZero = 1;

                    // reverse index
                    index += reqSpaces;
                }

                // more space
                newTokens = ammortizeArray(newTokens, index + reqSpaces);

                for (int stop : tokenized) {

                    float wordWidth = Styled.measureText(paint, workPaint, textCpy,
                            start, stop, fmi);

                    // add word
                    index = pushToken(newTokens, index, start, stop, rtlRight + rtlMul * (x + lineWidth + rtlZero * wordWidth), y, lastAscent,
                            lastDescent);

                    lineWidth += wordWidth;

                    start = stop + 1;

                    // based on if rtl
                    index += indexOffset;
                }

                if (isReverse) {
                    index = startIndex + reqSpaces + TOKEN_LENGTH;
                }

                offset =
                        (realWidth - lineWidth) / (float) (tokenized.size() - 1);

                if (isReverse) {
                    for (int pos = index - TOKEN_LENGTH * 2; pos >= startIndex; pos -= TOKEN_LENGTH) {
                        newTokens[pos + TOKEN_X] =
                                (int) (((float) newTokens[pos + TOKEN_X]) - (offset * (float) m++));
                    }
                } else {
                    for (int pos = startIndex + TOKEN_LENGTH; pos < index; pos += TOKEN_LENGTH) {
                        newTokens[pos + TOKEN_X] =
                                (int) (((float) newTokens[pos + TOKEN_X]) + (offset * (float) m++));
                    }
                }
            }

            y += lastDescent;
        }

        lineCount = lineNumber;
        tokens = newTokens;
        params.changed = false;
        textChange = !done;
        measuredHeight = (int) (y - lineHeightAdd + params.insetPaddingBottom);

        return done;
    }

    @Override
    public void onDraw(Canvas canvas, int scrollTop, int scrollBottom) {

        if(tokens.length < TOKEN_LENGTH){
            return;
        }

        int startIndex = getTokenIndex(scrollTop, TokenPosition.START_OF_LINE);
        int endIndex = getTokenIndex(scrollBottom, TokenPosition.END_OF_LINE);

        boolean isReverse = params.reverse;

        for (LeadingMarginSpanDrawParameters parameters : mLeadMarginSpanDrawEvents) {
            // FIXME sort by Y and break out of loop
            int top = parameters.top - scrollTop;
            int bottom = parameters.bottom - scrollTop;
            if (bottom < 0 || top > scrollBottom) continue;
            parameters.span.drawLeadingMargin(canvas, paint, parameters.x,
                    parameters.dir, top, parameters.baseline,
                    bottom, text, parameters.start,
                    parameters.end, parameters.first, null);
        }

        int lastEndIndexY = tokens[endIndex + TOKEN_Y];
        int diffEndIndexYCount = 1;

        // FIXME Find next pos-y
        for (int s = endIndex; diffEndIndexYCount > 0 && s < tokens.length; s += TOKEN_LENGTH) {
            endIndex += TOKEN_LENGTH;
            if (lastEndIndexY != tokens[s + TOKEN_Y]) {
                diffEndIndexYCount--;
                lastEndIndexY = tokens[s + TOKEN_Y];
            }
        }

        for (int index = startIndex; index < endIndex; index += TOKEN_LENGTH) {
            if (tokens[index + TOKEN_START] == Integer.MAX_VALUE) break;
            Styled.drawText(canvas, text, tokens[index + TOKEN_START],
                    tokens[index + TOKEN_END], Layout.DIR_LEFT_TO_RIGHT, isReverse,
                    tokens[index + TOKEN_X], 0,
                    tokens[index + TOKEN_Y] - scrollTop, 0, paint, workPaint, false);
            if (debugging) {
                int lastColor = paint.getColor();
                float lastStrokeWidth = paint.getStrokeWidth();
                paint.setStrokeWidth(2);
                paint.setColor(Color.GREEN);
                canvas.drawLine(0, tokens[index + TOKEN_Y] - tokens[index + TOKEN_ASCENT] - scrollTop,
                        params.parentWidth, tokens[index + TOKEN_Y] - tokens[index + TOKEN_ASCENT] - scrollTop,
                        paint);
                paint.setColor(Color.CYAN);
                canvas.drawLine(0, tokens[index + TOKEN_Y] + tokens[index + TOKEN_DESCENT] - scrollTop,
                        params.parentWidth, tokens[index + TOKEN_Y] + tokens[index + TOKEN_DESCENT] - scrollTop,
                        paint);
                paint.setColor(lastColor);
                paint.setStrokeWidth(lastStrokeWidth);
            }
        }
    }

    @Override
    public int getTokenIndex(float y, TokenPosition position) {
        int high = Math.max(0, tokens.length - 1);
        int low = 0;

        while (low + 1 < high) {
            int mid = (high + low) / 2;
            int midx = mid - (mid % TOKEN_LENGTH);
            int fY = tokens[midx + TOKEN_Y];

            if (fY > y) {
                high = mid;
            } else {
                low = mid;
            }
        }

        switch (position) {
            default:
            case START_OF_LINE: {
                low -= low % TOKEN_LENGTH;
                for (int s = low; s > 0 && tokens[s + TOKEN_Y] >= y; s -= TOKEN_LENGTH) {
                    low -= TOKEN_LENGTH;
                }
                return low;
            }
            case END_OF_LINE: {
                high -= high % TOKEN_LENGTH;
                for (int s = high; s + TOKEN_LENGTH < tokens.length && tokens[s + TOKEN_Y] <= y; s += TOKEN_LENGTH) {
                    high += TOKEN_LENGTH;
                }
                return high;
            }
        }
    }

    @Override
    public float getTokenTopAt(int index) {
        return tokens[index + TOKEN_Y];
    }

    @Override
    public CharSequence getTokenTextAt(int index) {
        return text.subSequence(tokens[index + TOKEN_START], tokens[index + TOKEN_END]);
    }

    @Override
    public boolean isTokenized() {
        return tokens != null;
    }

    /**
     * Class to handle onDrawLeadingSpanMargin
     */

    private class LeadingMarginSpanDrawParameters {

        public int x;
        public int top;
        public int baseline;
        public int bottom;
        public int dir;
        public int start;
        public int end;
        public boolean first;
        public LeadingMarginSpan span;

        public LeadingMarginSpanDrawParameters(LeadingMarginSpan span,
                                               int x,
                                               int dir,
                                               int top,
                                               int baseline,
                                               int bottom,
                                               int start,
                                               int end,
                                               boolean first) {
            this.span = span;
            this.x = x;
            this.dir = dir;
            this.top = top;
            this.baseline = baseline;
            this.bottom = bottom;
            this.start = start;
            this.end = end;
            this.first = first;
        }
    }
}
