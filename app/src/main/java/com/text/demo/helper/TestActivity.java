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
 * TestActivity.java
 * @author Mathew Kurian
 *
 * From TextJustify-Android Library v2.0
 * https://github.com/bluejamesbond/TextJustify-Android
 *
 * Please report any issues
 * https://github.com/bluejamesbond/TextJustify-Android/issues
 *
 * Date: 11/1/14 3:03 PM
 */

package com.text.demo.helper;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.text.DocumentView;
import com.text.style.TextAlignment;
import com.text.demo.R;

public class TestActivity extends Activity {

    public String testName;
    private boolean debugging = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        testName = Utils.splitCamelCase(getClass().getSimpleName());

        ActionBar bar = getActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#e74c3c")));
        bar.setTitle("Samples");

        setContentView(R.layout.testlayout);
    }

    public void addDocumentView(CharSequence article, int type, boolean rtl){
        final DocumentView documentView = new DocumentView(this, type);
        documentView.setColor(0xffffffff);
        documentView.setTypeface(Typeface.DEFAULT);
        documentView.setTextSize(33);
        documentView.getDocumentLayoutParams().setTextAlignment(TextAlignment.JUSTIFIED);
        documentView.getDocumentLayoutParams().setPaddingLeft(50);
        documentView.getDocumentLayoutParams().setPaddingRight(50);
        documentView.getDocumentLayoutParams().setPaddingTop(50);
        documentView.getDocumentLayoutParams().setPaddingBottom(50);
        documentView.getDocumentLayoutParams().setLineHeightAdd(1);
        documentView.getDocumentLayoutParams().setReverse(rtl);
        documentView.getLayout().setDebugging(debugging);
        documentView.setText(article, true); // true: enable justification

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(documentView);

        LinearLayout articleList = (LinearLayout) findViewById(R.id.articleList);
        articleList.addView(linearLayout);

        Button debugButton = (Button) findViewById(R.id.debugButton);

        if (debugButton != null) {
            debugButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    debugging = !debugging;
                    documentView.getLayout().setDebugging(debugging);
                    documentView.postInvalidate();
                }
            });
        }
    }

    public void addDocumentView(CharSequence article, int type) {
        addDocumentView(article, type, false);
    }
}
