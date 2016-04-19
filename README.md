[![Gittip](http://img.shields.io/gittip/bluejamesbond.svg)](https://gratipay.com/bluejamesbond/) [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-TextJustify--Android-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/1318) [![Build Status](https://travis-ci.org/bluejamesbond/TextJustify-Android.svg?branch=master)](https://travis-ci.org/bluejamesbond/TextJustify-Android) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.bluejamesbond/textjustify-android/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/com.github.bluejamesbond/textjustify-android)  
![Logo](https://raw.githubusercontent.com/bluejamesbond/TextJustify-Android/master/misc/logo.png?)
=======

**Android Full Justification** 

#About
This library will provide you a way to justify text. It supports both plain text and Spannables. Additionally, the library can auto-hyphentate your displayed content (thanks to [@muriloandrade](https://github.com/muriloandrade)).

*Compatible for Android 2.2 to 5.X*

#Screenshot
![Preview](http://i.imgur.com/k6bAWd0.jpg)

#Demo
[![Imgur](http://i.imgur.com/hSGF1fV.png)](https://play.google.com/store/apps/details?id=com.bluejamesbond.text.sample)

#Recent
**01/11/2015** ► Added support for very long documents with fading and progress listener  
**01/10/2015** ► Refractored / renamed classes  
**01/04/2015** ► Improved caching support which allows for smooth scrolling  
**01/02/2015** ► Added XML attributes for `DocumentView`

#Wiki
For examples, tests, and API refer to the [Android-TextJustify Wiki](https://github.com/bluejamesbond/TextJustify-Android/wiki/1-%C2%B7-Home).

#Install
Just add to your `build.gradle`
```gradle
dependencies {
    compile 'com.github.bluejamesbond:textjustify-android:2.1.6'
}
```

#Known Issues
| Status| Issues    |
| :------------:    |:---------------|
|  **`CLOSED`**     | Scroll caching for very large documents i.e. > 4000 paragaphs |
|  **`OPEN`**       | Add letter-spacing feature like CSS |
|  **`OPEN`**       | Improve text strike-through |
|  **`OPEN`**       | Improve text underline  |
|  **`CLOSED`**     | Support more features like `TextView` in terms of `Paint` settings  |
