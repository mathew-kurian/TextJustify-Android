package com.text;

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
 * Console.java
 * @author Mathew Kurian
 *
 * From TextJustify-Android Library v2.0
 * https://github.com/bluejamesbond/TextJustify-Android
 *
 * Please report any issues
 * https://github.com/bluejamesbond/TextJustify-Android/issues
 *
 * Date: 10/27/14 1:36 PM
 */

@SuppressWarnings("unused")
public class Console {

    public static void log(String tag, String s) {
        android.util.Log.d(tag, s);
    }

    public static void log(String tag, int s) {
        android.util.Log.d(tag, s + "");
    }

    public static void log(String tag, long s) {
        android.util.Log.d(tag, s + "");
    }

    public static void log(String tag, double s) {
        android.util.Log.d(tag, s + "");
    }

    public static void log(String tag, float s) {
        android.util.Log.d(tag, s + "");
    }

    public static void log(String tag, boolean s) {
        android.util.Log.d(tag, s + "");
    }


    public static void log(String s) {
        android.util.Log.d("", s);
    }

    public static void log(int s) {
        android.util.Log.d("", s + "");
    }

    public static void log(long s) {
        android.util.Log.d("", s + "");
    }

    public static void log(double s) {
        android.util.Log.d("", s + "");
    }

    public static void log(float s) {
        android.util.Log.d("", s + "");
    }

    public static void log(boolean s) {
        android.util.Log.d("", s + "");
    }
}
