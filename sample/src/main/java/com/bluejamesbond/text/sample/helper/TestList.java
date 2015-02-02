package com.bluejamesbond.text.sample.helper;

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
 * TestList.java
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

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bluejamesbond.text.sample.R;
import com.bluejamesbond.text.sample.test.ChineseCharacterTest;
import com.bluejamesbond.text.sample.test.ClickableSpanTest;
import com.bluejamesbond.text.sample.test.ComplexLayoutTest;
import com.bluejamesbond.text.sample.test.CustomHyphenatorTest;
import com.bluejamesbond.text.sample.test.HyphenatedTest;
import com.bluejamesbond.text.sample.test.LeadingMarginSpan2Test;
import com.bluejamesbond.text.sample.test.LineBreakTest;
import com.bluejamesbond.text.sample.test.ListViewTest;
import com.bluejamesbond.text.sample.test.LongFormattedTextTest;
import com.bluejamesbond.text.sample.test.LongPlainTextTest;
import com.bluejamesbond.text.sample.test.MixedRTLTest;
import com.bluejamesbond.text.sample.test.NewLineTest;
import com.bluejamesbond.text.sample.test.QuoteSpanTest;
import com.bluejamesbond.text.sample.test.RTLTest;
import com.bluejamesbond.text.sample.test.ShortFormattedTextTest;
import com.bluejamesbond.text.sample.test.TextUpdateTest;
import com.bluejamesbond.text.sample.test.TextViewTest;
import com.bluejamesbond.text.sample.test.WordSpacingTest;
import com.bluejamesbond.text.sample.test.XMLTest;

public class TestList extends TestActivity {

    private Class[] tests = new Class[]{
            ChineseCharacterTest.class,
            LongFormattedTextTest.class,
            NewLineTest.class,
            LeadingMarginSpan2Test.class,
            LineBreakTest.class,
            LongPlainTextTest.class,
            QuoteSpanTest.class,
            HyphenatedTest.class,
            WordSpacingTest.class,
            RTLTest.class,
            XMLTest.class,
            TextViewTest.class,
            CustomHyphenatorTest.class,
            ClickableSpanTest.class,
            MixedRTLTest.class,
            ListViewTest.class,
            ComplexLayoutTest.class,
            ShortFormattedTextTest.class,
            TextUpdateTest.class
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set default app font
        // FontsOverride.setDefaultFont(this, "DEFAULT", "fonts/notosans.ttf");

        // Set layout
        setContentView(R.layout.testlist_activity);

        // Create list of simple test names
        String[] testNames = new String[tests.length];

        for (int i = 0; i < testNames.length; i++) {
            testNames[i] = Utils.splitCamelCase(tests[i].getSimpleName()).toUpperCase();
        }

        // Get listView
        ListView lv = (ListView) findViewById(R.id.list);

        // Assign adapter to List
        lv.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, testNames) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setTypeface(Typeface.DEFAULT_BOLD);
                textView.setBackgroundColor(Color.parseColor("#111111"));
                textView.setTextColor(Color.WHITE);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                return view;
            }
        });

        // For each click
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                startActivity(new Intent(TestList.this, tests[i]));
            }
        });
    }
}
