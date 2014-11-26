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
 * PlainTextTest.java
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

package com.text.demo.test;

import android.os.Bundle;

import com.text.DocumentView;
import com.text.demo.helper.TestActivity;

public class PlainTextTest extends TestActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addDocumentView(new StringBuilder()
                .append(testName + "\n")
                .append("Document view now supports both String and Spannables. To support this, there are two (2) types of layouts: (a) DocumentLayout and (b) SpannedDocumentLayout. " +
                        "DocumentLayout supports just plain Strings just like the text you are reading. However, Spannables require the " +
                        "constructor to have SpannedDocumentLayout.class as a parameter. For now, DocumentLayout will offer significant speed improvements " +
                        "compared to SpannedDocumentLayout, so use each class accordingly. DocumentLayout also supports hyphenation. To learn more about" +
                        "these layouts and what they have to offer visit the link in the titlebar above. And please report all the issues on GitHub!")
                .toString(), DocumentView.PLAIN_TEXT);
    }
}
