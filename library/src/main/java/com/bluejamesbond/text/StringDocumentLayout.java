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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

@SuppressWarnings("unused")
public class StringDocumentLayout extends IDocumentLayout {

    // Parsing objects
    private Token[] tokens;
    private ConcurrentModifiableLinkedList<String> chunks;

    public StringDocumentLayout(Context context, TextPaint paint) {
        super(context, paint);
        tokens = new Token[0];
        chunks = new ConcurrentModifiableLinkedList<>();
    }

    private float getFontAscent() {
        return -paint.ascent() * params.lineHeightMultiplier;
    }

    private float getFontDescent() {
        return paint.descent() * params.lineHeightMultiplier;
    }

    @Override
    public boolean onMeasure(IProgress<Float> progress, ICancel<Boolean> cancelled) {

        boolean done = true;
        String text = this.text.toString();

        if (textChange) {
            chunks.clear();

            int start = 0;

            while (start > -1) {
                int next = text.indexOf('\n', start);

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

        // Empty out any existing tokens
        List<Token> tokensList = new ConcurrentModifiableLinkedList<>();

        Paint paint = getPaint();
        paint.setTextAlign(Paint.Align.LEFT);

        // Get basic settings widget properties
        int lineNumber = 0;
        float width = params.parentWidth - params.insetPaddingRight - params.insetPaddingLeft;
        float lineHeight = getFontAscent() + getFontDescent();
        float x, prog = 0, chunksLen = chunks.size();
        float y = params.insetPaddingTop + getFontAscent();
        float spaceOffset = paint.measureText(" ") * params.wordSpacingMultiplier;

        main:
        for (String paragraph : chunks) {

            if (cancelled.isCancelled()) {
                done = false;
                break;
            }

            progress.onUpdate(prog++ / chunksLen);

            if (lineNumber >= params.maxLines) {
                break;
            }

            // Start at x = 0 for drawing text
            x = params.insetPaddingLeft;

            String trimParagraph = paragraph.trim();

            // If the line contains only spaces or line breaks
            if (trimParagraph.length() == 0) {
                tokensList.add(new LineBreak(lineNumber++, y));
                y += lineHeight;
                continue;
            }

            float wrappedWidth = paint.measureText(trimParagraph);

            // Line fits, then don't wrap
            if (wrappedWidth < width) {
                // activeCanvas.drawText(paragraph, x, y, paint);
                tokensList.add(new SingleLine(lineNumber++, x, y, trimParagraph));
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

                x = params.insetPaddingLeft;

                // Line doesn't fit, then apply wrapping
                LineAnalysis format = fit(justifyIterator, start, spaceOffset, width);
                int tokenCount = format.end - format.start;
                boolean leftOverTokens = justifyIterator.hasNext();

                if (tokenCount == 0 && leftOverTokens) {
                    new PlainDocumentException("Cannot fit word(s) into one line. Font size too large?")
                            .printStackTrace();
                    done = false;
                    break main;
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

                    // Add to all tokens
                    tokensList.add(unit);
                }

                // Increment to next line
                y += lineHeight;

                // Next line
                lineNumber++;

                // Chcek cancelled
                if (cancelled.isCancelled()) {
                    done = false;
                    break;
                }

                // If there are more tokens leftover,
                // continue
                if (leftOverTokens) {

                    // Next start index for tokens
                    start = format.end;

                    continue;
                }

                // If all fit, then continue to next
                // paragraph
                break;
            }
        }

        Token[] tokensArr = new Token[tokensList.size()];
        tokensList.toArray(tokensArr);
        tokensList.clear();

        lineCount = lineNumber;
        tokens = tokensArr;
        params.changed = !done;
        measuredHeight = (int) (y - getFontAscent() + params.insetPaddingBottom);
        return done;
    }

    @Override
    public void onDraw(Canvas canvas, int startTop, int startBottom) {

        int tokenStart = getTokenIndex(startTop, TokenPosition.START_OF_LINE);
        int tokenEnd = getTokenIndex(startBottom, TokenPosition.END_OF_LINE);

        for (int i = Math.max(0, tokenStart - 25); i < tokenEnd + 25 && i < tokens.length; i++) {
            Token token = tokens[i];
            token.draw(canvas, -startTop, paint, params);
            if (debugging) {
                if (token instanceof LineBreak) {
                    int lastColor = paint.getColor();
                    boolean lastFakeBold = paint.isFakeBoldText();
                    Paint.Style lastStyle = paint.getStyle();
                    Paint.Align lastAlign = paint.getTextAlign();

                    paint.setColor(Color.YELLOW);
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawRect(params.insetPaddingLeft, token.y - startTop - getFontAscent(), params.parentWidth - params.insetPaddingRight, token.y - startTop + getFontDescent(), paint);

                    paint.setColor(Color.BLACK);
                    paint.setFakeBoldText(true);
                    paint.setTextAlign(Paint.Align.CENTER);
                    canvas.drawText("LINEBREAK", params.insetPaddingLeft + (params.parentWidth - params.insetPaddingRight - params.insetPaddingLeft) / 2, token.y - startTop, paint);

                    paint.setStyle(lastStyle);
                    paint.setColor(lastColor);
                    paint.setTextAlign(lastAlign);
                    paint.setFakeBoldText(lastFakeBold);
                }
            }
        }
    }

    @Override
    public int getTokenIndex(float y, TokenPosition position) {
        int high = Math.max(0, tokens.length - 1);
        int low = 0;

        while (low + 1 < high) {
            int mid = (high + low) / 2;
            float fY = tokens[mid].getY();

            if (fY > y) {
                high = mid;
            } else {
                low = mid;
            }
        }

        switch (position) {
            default:
            case START_OF_LINE: {
                for (int s = low; s > 0 && tokens[s].getY() >= y; s--) {
                    low--;
                }
                return low;
            }
            case END_OF_LINE: {
                for (int s = high; s < tokens.length && tokens[s].getY() <= y; s++) {
                    high++;
                }
                return high;
            }
        }
    }

    @Override
    public float getTokenTopAt(int index) {
        return tokens[index].getY();
    }

    @Override
    public CharSequence getTokenTextAt(int index) {
        return tokens[index].toString();
    }

    @Override
    public boolean isTokenized() {
        return tokens != null;
    }

    private ConcurrentModifiableLinkedList<Unit> tokenize(String s) {

        ConcurrentModifiableLinkedList<Unit> units = new ConcurrentModifiableLinkedList<>();

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
                            // was even onUpdate
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

    private static abstract class Token {

        public int lineNumber;
        public float y;

        public Token(int lineNumber, float y) {
            this.lineNumber = lineNumber;
            this.y = y;
        }

        public float getY() {
            return y;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        abstract void draw(Canvas canvas, float offsetY, Paint paint, LayoutParams params);
    }

    private static class Unit extends Token {

        public float x;
        public String unit;

        public Unit(String unit) {
            super(0, 0);
            this.unit = unit;
        }

        public Unit(int lineNumber, float x, float y, String unit) {
            super(lineNumber, y);
            this.x = x;
            this.unit = unit;
        }

        @Override
        void draw(Canvas canvas, float offsetY, Paint paint, LayoutParams params) {
            canvas.drawText(unit, x + params.getOffsetX(), y + params.getOffsetY() + offsetY, paint);
        }

        @Override
        public String toString() {
            return unit;
        }
    }

    private static class LineBreak extends Token {
        public LineBreak(int lineNumber, float y) {
            super(lineNumber, y);
        }

        @Override
        void draw(Canvas canvas, float offsetY, Paint paint, LayoutParams params) {
        }

        @Override
        public String toString() {
            return "\n";
        }
    }

    private static class SingleLine extends Unit {
        public SingleLine(int lineNumber, float x, float y, String unit) {
            super(lineNumber, x, y, unit);
        }
    }

    @SuppressWarnings("serial")
    class PlainDocumentException extends Exception {
        public PlainDocumentException(String message) {
            super(message);
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