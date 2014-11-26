package com.text.hyphen;

import android.annotation.SuppressLint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * @file   Hyphenator.java
 * @author Murilo Andrade
 * @date   2014-10-20
 */

/**
 * Hyphenator.java is an adaptation of Bram Steins hypher.js-Project:
 * https://github.com/bramstein/Hypher
 * <p/>
 * Code from this project belongs to the following license:
 * <p/>
 * Copyright (c) 2011, Bram Stein All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p/>
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

@SuppressLint("UseSparseArrays")
public class Hyphenator {

    HyphenPattern hyphenationPattern;
    TrieNode trie;
    int leftMin;
    int rightMin;
    public Hyphenator(HyphenPattern pattern) {

        this.hyphenationPattern = pattern;

        this.trie = this.createTrie(this.hyphenationPattern.patternObject);
        this.leftMin = hyphenationPattern.leftMin;
        this.rightMin = hyphenationPattern.rightMin;
    }

    private TrieNode createTrie(Map<Integer, String> patternObject) {

        int i = 0, c = 0, p = 0, codePoint;

        String[] chars, points;
        TrieNode t, tree = new TrieNode();
        ArrayList<String> patterns = new ArrayList<String>();

        for (Map.Entry<Integer, String> entry : patternObject.entrySet()) {
            Matcher matcher = Pattern.compile(".{1," + String.valueOf(entry.getKey()) + "}")
                    .matcher(entry.getValue());
            while (matcher.find()) {
                patterns.add(matcher.group(0));
            }

            for (i = 0; i < patterns.size(); i++) {
                chars = patterns.get(i).replaceAll("[0-9]", "").split("");
                points = patterns.get(i).split("\\D");

                t = tree;

                for (c = 0; c < chars.length; c++) {
                    if (chars[c].isEmpty()) {
                        continue;
                    }

                    codePoint = chars[c].codePointAt(0);

                    if (t.codePoint.get(codePoint) == null) {
                        t.codePoint.put(codePoint, new TrieNode());
                    }

                    t = t.codePoint.get(codePoint);
                }

                t._points = new ArrayList<Integer>();

                for (p = 0; p < points.length; p++) {
                    try {
                        t._points.add(Integer.parseInt(points[p]));
                    } catch (NumberFormatException e) {
                        t._points.add(0);
                    }
                }
            }
        }

        return tree;
    }

    public ArrayList<String> hyphenate(String word) {

        int i, j, k, wordLength, nodePointsLength;
        ArrayList<Integer> nodePoints;
        ArrayList<String> characters = new ArrayList<String>();
        ArrayList<Integer> characterPoints = new ArrayList<Integer>();
        ArrayList<String> originalCharacters = new ArrayList<String>();
        ArrayList<Integer> points = new ArrayList<Integer>();
        ArrayList<String> result = new ArrayList<String>();
        TrieNode node, trie = this.trie;

        result.add("");

        word = "_" + word + "_";

        for (char character : word.toCharArray()) {
            characters.add((String.valueOf(character)).toLowerCase());
            originalCharacters.add(String.valueOf(character));
        }

        wordLength = characters.size();

        for (i = 0; i < wordLength; i++) {
            points.add(i, 0);
            characterPoints.add(i, characters.get(i).codePointAt(0));
        }

        for (i = 0; i < wordLength; i++) {
            node = trie;

            for (j = i; j < wordLength; j++) {
                node = node.codePoint.get(characterPoints.get(j));

                if (node != null) {
                    nodePoints = node._points;

                    if (nodePoints != null) {
                        for (k = 0, nodePointsLength =
                                nodePoints.size(); k < nodePointsLength; k++) {
                            points.set(i + k, Math.max(points.get(i + k), nodePoints.get(k)));
                        }
                    }
                } else {
                    break;
                }
            }
        }

        for (i = 1; i < wordLength - 1; i++) {
            if (i > this.leftMin && i < (wordLength - this.rightMin) && points.get(i) % 2 > 0) {
                result.add(originalCharacters.get(i));
            } else {
                result.set(result.size() - 1,
                        result.get(result.size() - 1) + originalCharacters.get(i));
            }
        }

        return result;
    }

    public enum Language {
        EN_US, PT
    }

    private class TrieNode {
        Map<Integer, TrieNode> codePoint = new HashMap<Integer, TrieNode>();
        ArrayList<Integer> _points;
    }
}
