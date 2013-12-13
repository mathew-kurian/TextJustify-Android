![Logo](https://raw.github.com/bluejamesbond/TextJustify-Android/master/textjustify%20design%20logo%20%5Ba%5D.png)
=======
**Simple Android Full Justification**
Overview
=======
This is very simple - not complicated at all. What you want to do is get the library itself and add it to your Android project. Then you want to use the following code:

Option 1 - Setup
=======
Looks very accurate and neat. Small issues may arise but can be easily sorted out if users post their issues on Github. To use it, all you have to do is include the `TextViewEx.java` in your project. Then you may use as you would the built-in  `TextView`.

```xml

// XML Layout Sample
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:paddingBottom="@dimen/activity_vertical_margin"
    android:paddingLeft="@dimen/activity_horizontal_margin"
    android:paddingRight="@dimen/activity_horizontal_margin"
    android:paddingTop="@dimen/activity_vertical_margin"
    tools:context=".MainActivity" >
    
    <com.textjustify.TextViewEx
        android:id="@+id/textview"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
        
</ScrollView>

```
Option 1 - Result
=======
**Comparison**

![Logo](http://i.imgur.com/2H8iRzb.png)


Option 2 - Setup
=======
This would be considered a fallback option in the case the previous option did not work for you. To use this, include `TextJustify.java` and follow the sample code below.

```js

int FinallwidthDp  = 320 ;
int widthJustify  = 223 ;
int PaddingLeft , PaddingRight , MarginLeft , MarginRight;

DisplayMetrics metrics = new DisplayMetrics();
getWindowManager().getDefaultDisplay().getMetrics(metrics);
int widthPixels = metrics.widthPixels;
  
float scaleFactor = metrics.density;
float widthDp = (widthPixels / scaleFactor) ;

TextView tv = (TextView) findViewById(R.id.textView1);
ViewGroup.MarginLayoutParams lp1 = (ViewGroup.MarginLayoutParams) tv.getLayoutParams();

TextJustify.run(tv,widthDp / FinallwidthDp * widthJustify , tv.getPaddingLeft() ,tv.getPaddingRight() , lp1.leftMargin, lp1.rightMargin);

// If this doesn't work, then start from a small number for widthJustify like 150 and move up from there to get the exact width. 

```
Option 2 - Result
=======
**Before**

![Logo](http://i.stack.imgur.com/ck0bY.png)

**After**

![Logo](http://i.stack.imgur.com/dujWm.png)

Notes
=======
HTML formatting will cause you to have expected results.

Contributors
=======

```js
bluejamesbond
```
