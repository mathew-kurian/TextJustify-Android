![Logo](https://raw.github.com/bluejamesbond/TextJustify-Android/master/textjustify%20design%20logo%20%5Ba%5D.png)
=======
**Simple Android Full Justification**
Quick Setup
=======
This is very simple - not complicated at all. What you want to do is get the library itself and add it to your Android project. Then you want to use the following code:

```js
public static final int					FinallwidthDp  = 320 ;
public static final int					widthJustify  = 223 ;
public int  PaddingLeft , PaddingRight , MarginLeft , MarginRight;

DisplayMetrics metrics = new DisplayMetrics();
getWindowManager().getDefaultDisplay().getMetrics(metrics);
int widthPixels = metrics.widthPixels;
Log.d("size is width pixel", String.valueOf(widthPixels+" "));
	     
loat scaleFactor = metrics.density;
float widthDp = (widthPixels / scaleFactor) ;

TextView tv = (TextView) findViewById(R.id.textView1);
ViewGroup.MarginLayoutParams lp1 = (ViewGroup.MarginLayoutParams) tv.getLayoutParams();
	     
tv.setText(text);
TextJustify.run(tv,widthDp / FinallwidthDp * widthJustify , 
		tv.getPaddingLeft() ,tv.getPaddingRight() , 
		lp1.leftMargin, 
		lp1.rightMargin);

//Start from a small number like 150f and move up from there to get the exact width. 
//I haven't fixed this problem yet. 305f works best for me in this case.

```
Examples
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
