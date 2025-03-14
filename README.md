# Fortuna

An open-source neuroscientific Android app based on the [Hedonist philosophy](https://en.wikipedia.org/wiki/Hedonism),
using which you will score your mood every day in your desired calendar system. You can also set an emoji for a day
or a month and enter some notes!

<p>
  <img src="about/Screenshot_20230120-044811_Fortuna.jpg" width="32%" />
  <img src="about/Screenshot_20230120-044823_Fortuna.jpg" width="32%" />
  <img src="about/Screenshot_20230120-044843_Fortuna.jpg" width="32%" />
</p>

## How it works

This app is designed based on the Hedonist philosophy!
It's used to calculate the amount of pleasure and pain one senses in their life.

You can enter the quality of your life in a scale between -3 to +3 for each day.

Alternatively you can estimate the number of a whole month using the field right to the year field
at the top of the page.

Then you can see how much pleasure and pain you've sensed overall in your life or in a particular
month.

## Download & Install

Currently only two calendars are supported, but this app can be adapted to new calendars so easily
by adding new product flavours.

- In Gregorian calendar:
  [Install from Google Play](
  https://play.google.com/store/apps/details?id=ir.mahdiparastesh.fortuna.gregorian)
- In [Humanist Iranian](
  https://gist.github.com/62264825004f0ba83020c11db15567eb) calendar:
  [Contact me](mailto:fulcrum1378@gmail.com)
- In any other calendar:
  Either [add it yourself](#add-your-own-calendar) or [contact me](mailto:fulcrum1378@gmail.com).

## VITA Markup Language

Vita means *life* in Latin. Fortuna reads and writes its data in **\*.vita** plain text file format.
It defines data separated by months and every month is separated using a line break;\
At the beginning of each month, there is a "**@**" symbol and then year and month number; for example: **@2022.03**\
You can optionally enter a "**~**" symbol and define an estimated score for the whole month
which will apply only on days with no specific score; for example **@2022.03~3**\
After a line break, there come scores for each date.
By default, first line indicates day 1 in that month and line 2 indicates day 2; for example: **0**\
Except if you want to skip some days and jump to another day,
then you'll have to explicitly specify the number of that day; for example: **5:0**\
Note that entering each day is optional, and you can even define a month with no days.\
After each day and even the month itself you can optionally enter two more values:

1. An **emoji** for that day or month after a "**;**" symbol.
2. Some **descriptions** for that day or month after ANOTHER "**;**" symbol.

Here is a complete example:

```
@2021.09~-0.5;⛓;Spent the whole month in the military...
8:-0.5;;The idea of a developing such an app came to my mind and I named it "Hedonometer" which I later called it "Fortuna".

@2022.03~0
24:1.5;🧠;Started Fortuna Android project at 10:32:21!
2
2
2
1.5
2
2.5
2;🧠;FORTUNA IS READY!!! (it used JSON to store its data)

@2022.08
3:1;;Invented VITA file format and then migrated Fortuna to it.

```

## Structure of the Source Code

- [**Dialogues.kt**](app\src\kotlin\ir\mahdiparastesh\fortuna\Dialogues.kt) : contains all the
  DialogFragment instances, mostly used in in the navigation drawer.
- [**Grid.kt**](app\src\kotlin\ir\mahdiparastesh\fortuna\Grid.kt) : controls the calendar table
  and the dialogues that might pop up while interacting with it.
- [**Main.kt**](app\src\kotlin\ir\mahdiparastesh\fortuna\Main.kt) : the main and only Activity instance in this app.
- [**Nyx.kt**](app\src\kotlin\ir\mahdiparastesh\fortuna\Nyx.kt) : a BroadcastReceiver that takes some necessary
  or optional actions at 12 AM; including a Vita backup (local+cloud), reminding users to score their day
  and updating views according to the current new date.
- [**Vita.kt**](app\src\kotlin\ir\mahdiparastesh\fortuna\Vita.kt) : reads and writes Vita files and all related
  utilities.
- [**misc**](app\src\kotlin\ir\mahdiparastesh\fortuna\misc) subpackage : miscellaneous add-ons and utilities.
- [**util**](app\src\kotlin\ir\mahdiparastesh\fortuna\util) subpackage : general-purpose utilities.

### Add your own Calendar

If you don't use the Gregorian calendar, you can use Fortuna in your regional calendar system.
Fortuna needs a subclass of [android.icu.util.Calendar](
https://developer.android.com/reference/android/icu/util/Calendar) to work based on it.
Google has already developed implementations of some different calendars,
if you don't find your calendar in the [**android.icu.util**](
https://android.googlesource.com/platform/external/icu/+/refs/heads/master/android_icu4j/src/main/java/android/icu/util/)
package, you should develop it yourself.
In Fortuna, [build flavours](https://developer.android.com/build/build-variants) represent calendar systems,
so all you need to do is to:

1. Add a new build flavour for Gradle
2. Create "app/src/res_CALENDAR" (e.g. res_indian) and inside it:
    - *drawable/today_widget_preview.png* : a preview
      for [TodayWidget](app/src/kotlin/ir/mahdiparastesh/fortuna/misc/TodayWidget.kt)
    - *values/strings.xml* : month names as *<string-array name="luna"/>*
3. Attribute that build flavour to your Calendar class in Kit.kt.

#### **[build.gradle.kts](app/build.gradle.kts)**

```kotlin
android {
    ...
    productFlavors {
        ...
        create("indian") { applicationIdSuffix = ".indian" }
    }
    ...
    sourceSets.getByName("indian") {
        res.setSrcDirs(listOf("src/res", "src/res_indian"))
    }
    ...
}
```

#### **[Kit.kt](app/src/kotlin/ir/mahdiparastesh/fortuna/Kit.kt#:~:text=val%20calType)**

```kotlin
val calType = when (BuildConfig.FLAVOR) {
    ...
    "indian" -> android.icu.util.IndianCalendar::class.java
    ...
}
```

#### res_indian/values/string.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string-array name="luna">
        <item>Chaitra</item>
        <item>Vaisakha</item>
        <item>Jyeshtha</item>
        <item>Ashadha</item>
        <item>Shravana</item>
        <item>Bhadra</item>
        <item>Ashvin</item>
        <item>Kartika</item>
        <item>Agrahayana</item>
        <item>Pausha</item>
        <item>Magha</item>
        <item>Phalguna</item>
    </string-array>
</resources>
```

> :warning: **Do NOT implement [Lunisolar](https://en.wikipedia.org/wiki/Lunisolar_calendar) calendars**;
> their structure is so irregular in *android.icu.util* and they also get problematic with *Vita* structure!

## License

```
Copyright © Mahdi Parastesh - All Rights Reserved.
```
