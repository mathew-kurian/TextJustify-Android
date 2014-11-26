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

package com.text.demo.test;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

import com.text.DocumentView;
import com.text.style.CenterSpan;
import com.text.style.JustifiedSpan;
import com.text.style.LeftSpan;
import com.text.style.RightSpan;
import com.text.demo.helper.ArticleBuilder;
import com.text.demo.helper.TestActivity;

public class FormattedTextTest extends TestActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addDocumentView(new ArticleBuilder()
                .append(testName,
                        false, new RelativeSizeSpan(2f), new StyleSpan(Typeface.BOLD),
                        new LeftSpan())
                .append("<font color=0xFFC801>Justin Worland</font><font color=0x888888> @justinworland  Oct. 25, 2014</font>",
                        false, new RelativeSizeSpan(0.8f), new StyleSpan(Typeface.BOLD))
                .append("<font color=0x888888>Updated: Oct. 25, 2014 2:34 PM</font>".toUpperCase(),
                        true, new RelativeSizeSpan(0.6f), new StyleSpan(Typeface.BOLD))
                .append("State health department staff will be on the ground at state airports",
                        true, new RelativeSizeSpan(1.2f), new StyleSpan(Typeface.BOLD),
                        new StyleSpan(Typeface.ITALIC))
                .append("Healthcare workers returning to New York or New Jersey after treating Ebola patients in West Africa will be placed under a mandatory quarantine, officials announced Friday, one day after a Doctors Without Borders doctor was diagnosed with the virus in New York City. Illinois announced a similar policy Saturday, meaning it will be enforced in states with three of the five airports through which passengers traveling from the Ebola-stricken West African countries must enter the United States.",
                        true, new RelativeSizeSpan(1f), new JustifiedSpan())
                .append("N.J. Gov. Chris Christie and N.Y. Gov. Andrew Cuomo made the announcement as part of a broader procedural plan to help protect the densely packed, highly populated area from any further spread of the disease.",
                        true, new RelativeSizeSpan(1f), new RightSpan())
                .append("“Since taking office, I have erred on the side of caution when it comes to the safety and protection of New Yorkers, and the current situation regarding Ebola will be no different,” Gov. Cuomo said. “The steps New York and New Jersey are taking today will strengthen our safeguards to protect our residents against this disease and help ensure those that may be infected by Ebola are treated with the highest precautions.”",
                        true, new RelativeSizeSpan(1f), new CenterSpan(),
                        new StyleSpan(Typeface.ITALIC))
                .append("New York and New Jersey state health department staff will be present on the ground at John F. Kennedy International Airport in New York and Newark Liberty Airport in New Jersey. In addition to implementing the mandatory quarantine of health care workers and others who had direct contact with Ebola patients, health department officials in each state will determine whether others should travelers should be hospitalized or quarantined.",
                        true, new RelativeSizeSpan(1f), new JustifiedSpan(),
                        new StyleSpan(Typeface.ITALIC))
                .append("“The announcements mark a dramatic escalation in measures designed to prevent the spread of Ebola in the United States. Previously, only individuals with symptoms of Ebola would be quarantined upon entry to the U.S. under a federal rule from the Centers for Diseases Control and the Department of Homeland Security.”",
                        false, new RelativeSizeSpan(1f), new CenterSpan()),
                DocumentView.FORMATTED_TEXT);
    }
}
