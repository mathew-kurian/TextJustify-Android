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
 * FormattedTextTest.java
 * @author Mathew Kurian
 *
 * From TextJustify-Android Library v2.0
 * https://github.com/bluejamesbond/TextJustify-Android
 *
 * Please report any issues
 * https://github.com/bluejamesbond/TextJustify-Android/issues
 *
 * Date: 11/1/14 3:21 AM
 */

package com.bluejamesbond.text.sample.test;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

import com.bluejamesbond.text.DocumentView;
import com.bluejamesbond.text.sample.helper.ArticleBuilder;
import com.bluejamesbond.text.sample.helper.TestActivity;
import com.bluejamesbond.text.style.LeftSpan;

public class NewLineTest extends TestActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArticleBuilder ab = new ArticleBuilder()
                .append(testName,
                        false, new RelativeSizeSpan(2f), new StyleSpan(Typeface.BOLD),
                        new LeftSpan())
                .append("There will be 6 lines following this messsage:",
                        true, new RelativeSizeSpan(0.8f), new StyleSpan(Typeface.BOLD))
                .append("\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "\n",
                        false, new RelativeSizeSpan(0.6f), new StyleSpan(Typeface.BOLD));

        addDocumentView(ab,
                DocumentView.FORMATTED_TEXT);
    }
}
