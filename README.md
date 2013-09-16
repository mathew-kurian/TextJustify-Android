![Logo](https://raw.github.com/bluejamesbond/TextJustify-Android/master/textjustify%20design%20logo%20%5Ba%5D.png)
=======
**Simple Android Full Justification**
Quick Setup
=======
This is very simple - not complicated at all. What you want to do is get the library itself and add it to your Android project. Then you want to use the following code:

```js

((TextView)findViewById(R.id.textview)).setText(input);
TextJustify.run(((TextView)findViewById(R.id.textview)), 305f); 
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
HTML formatting and this class will not have the expected results.

Contributors
=======

```js
bluejamesbond
```
