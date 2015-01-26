package com.bluejamesbond.text.sample.test;

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
 * -------------------------------------------------------------------------
 *
 * ListViewTest.java
 * @author Mathew Kurian
 *
 * From TextJustify-Android Library v2.0
 * https://github.com/bluejamesbond/TextJustify-Android
 *
 * Please report any issues
 * https://github.com/bluejamesbond/TextJustify-Android/issues
 *
 * Date: 1/26/15 12:07 AM
 */

import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.bluejamesbond.text.DocumentView;
import com.bluejamesbond.text.sample.R;
import com.bluejamesbond.text.sample.helper.TestActivity;

import java.util.ArrayList;
import java.util.HashMap;

public class ListViewTest extends TestActivity {

    @Override
    protected int getContentView() {
        return R.layout.test_listview;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayList<HashMap<String, ?>> data = new ArrayList<HashMap<String, ?>>();
        data.add(new HashMap<String, Object>() {{
            put("Title", "Item 1");
        }});

        data.add(new HashMap<String, Object>() {{
            put("Title", "Item 2");
        }});

        data.add(new HashMap<String, Object>() {{
            put("Title", "Item 3");
        }});

        final ListView listview = (ListView) findViewById(R.id.list);
        SimpleAdapter adapter = new SimpleAdapter(this,
                data,
                R.layout.test_listview_item,
                new String[]{"Title"},
                new int[]{R.id.title});

        listview.setAdapter(adapter);

    }
}