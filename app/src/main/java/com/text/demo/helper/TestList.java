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
 * TestList.java
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

package com.text.demo.helper;

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

import com.text.demo.test.ChineseCharacterTest;
import com.text.demo.test.FormattedTextTest;
import com.text.demo.test.LeadingMarginSpan2Test;
import com.text.demo.test.LineBreakTest;
import com.text.demo.test.PlainTextTest;
import com.text.demo.test.QuoteSpanTest;
import com.text.demo.R;
import com.text.demo.test.RTLTest;

public class TestList extends TestActivity {

    private Class[] tests = new Class[]{
            ChineseCharacterTest.class,
            FormattedTextTest.class,
            LeadingMarginSpan2Test.class,
            LineBreakTest.class,
            PlainTextTest.class,
            QuoteSpanTest.class,
            RTLTest.class
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set default app font
        // FontsOverride.setDefaultFont(this, "DEFAULT", "fonts/notosans.ttf");

        // Set layout
        setContentView(R.layout.testlist);

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
