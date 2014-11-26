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
 * LineBreakTest.java
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
import android.text.SpannableStringBuilder;
import android.text.Spanned;

import com.text.DocumentView;
import com.text.style.JustifiedSpan;
import com.text.demo.helper.MyLeadingMarginSpan2;
import com.text.demo.helper.TestActivity;

public class LineBreakTest extends TestActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SpannableStringBuilder result = new SpannableStringBuilder();
        result.append(testName + "\n");
        result.append("SpaceBug\n");
        result.append(
                "现代计算机中内存空间都是按照byte划分的，从理论上讲似乎对任何类型的变量的访问可以从任何地址开始，<font color=0xFFC801>现代计算机中内存空间都是按照byte划分的，从理论上讲似乎对任何类型的变量的访问可以从任何地址开始</font>，" +
                        "但实际情况是在访问特定变量的时候经一定的规则在空间上排列，而不是顺序的一个接一个的排放，这就是对齐。现代计算机中内存空间都是按照byte划分的，从理论上讲似乎对任何类型的变量的访问可以从任何地址开始，但实际情况是在访问特定变量的时候" +
                        "经一定的规则在空间上排列，而不是顺序的一个接一个的排放，这就是对齐。\n");
        result.append("现代计算机\n");
        result.setSpan(new JustifiedSpan(), 0, result.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        result.setSpan(new MyLeadingMarginSpan2(2, 100), 0, result.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        addDocumentView(result, DocumentView.FORMATTED_TEXT);

    }
}
