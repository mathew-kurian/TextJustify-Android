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

package com.bluejamesbond.text.sample.helper;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bluejamesbond.text.DocumentView;
import com.bluejamesbond.text.sample.R;
import com.bluejamesbond.text.style.TextAlignment;

public class TestActivity extends Activity {

    public String testName;
    private boolean debugging = false;
    private int cacheConfig = 0;

    protected int getContentView() {
        return R.layout.test_activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getContentView());

        testName = Utils.splitCamelCase(getClass().getSimpleName());

        TextView titleBar = ((TextView) findViewById(R.id.titlebar));

        if (titleBar != null) {
            titleBar.setText(testName);
        }
    }

    public DocumentView addDocumentView(CharSequence article, int type, boolean rtl) {
        final DocumentView documentView = new DocumentView(this, type);
        documentView.setTextColor(0xffffffff);
        documentView.setTypeface(Typeface.DEFAULT);
        documentView.setTextSize(30);
        documentView.getDocumentLayoutParams().setTextAlignment(TextAlignment.JUSTIFIED);
        documentView.getDocumentLayoutParams().setPaddingLeft(0f);
        documentView.getDocumentLayoutParams().setPaddingRight(0f);
        documentView.getDocumentLayoutParams().setPaddingTop(0f);
        documentView.getDocumentLayoutParams().setPaddingBottom(0f);
        documentView.getDocumentLayoutParams().setLineHeightMultiplier(1f);
        documentView.getDocumentLayoutParams().setReverse(rtl);
        documentView.getLayout().setDebugging(debugging);
        documentView.setText(article);
        documentView.setProgressBar((ProgressBar) findViewById(R.id.progressBar));
        documentView.setFadeInDuration(800);
        documentView.setFadeInAnimationStepDelay(30);
        documentView.setFadeInTween(new DocumentView.ITween() {
            @Override
            public float get(float t, float b, float c, float d) {
                return c * (t /= d) * t * t + b;
            }
        });

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT));
        linearLayout.addView(documentView);

        LinearLayout articleList = (LinearLayout) findViewById(R.id.articleList);
        articleList.addView(linearLayout);

        debugging = documentView.getLayout().isDebugging();
        cacheConfig = documentView.getCacheConfig().getId();

        final TextView debugButton = (TextView) findViewById(R.id.debugButton);

        if (debugButton != null) {
            debugButton.setText((debugging ? "DISABLE" : "ENABLE") + " DEBUG");
            debugButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    debugging = !debugging;
                    debugButton.setText((debugging ? "DISABLE" : "ENABLE") + " DEBUG");
                    documentView.getLayout().setDebugging(debugging);
                    documentView.invalidate();
                }
            });
        }

        final TextView cacheButton = (TextView) findViewById(R.id.cacheButton);
        final Toast cacheConfigToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        if (cacheButton != null) {
            cacheConfigToast.setText("Activated " + documentView.getCacheConfig().name());
            cacheConfigToast.show();
            cacheButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cacheConfig = (cacheConfig + 1) % 5;
                    DocumentView.CacheConfig newCacheConfig = DocumentView.CacheConfig.getById(cacheConfig);
                    cacheConfigToast.setText("Activated " + newCacheConfig.name());
                    cacheConfigToast.show();
                    documentView.setCacheConfig(newCacheConfig);
                    documentView.destroyCache();
                    documentView.invalidate();
                }
            });
        }
        return documentView;
    }

    public DocumentView addDocumentView(CharSequence article, int type) {
        return addDocumentView(article, type, false);
    }
}
