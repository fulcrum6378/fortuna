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
- [**Kit.kt**](app\src\kotlin\ir\mahdiparastesh\fortuna\Kit.kt) : a set of miscellaneous static functions and utilities.
- [**Main.kt**](app\src\kotlin\ir\mahdiparastesh\fortuna\Main.kt) : the main and only Activity instance in this app.
- [**Nyx.kt**](app\src\kotlin\ir\mahdiparastesh\fortuna\Nyx.kt) : a BroadcastReceiver that takes some necessary
  or optional actions at 12 AM, including a Vita backup, reminding users to score their day and updating views
  according to the current new date.
- [**Vita.kt**](app\src\kotlin\ir\mahdiparastesh\fortuna\Vita.kt) : reads and writes Vita files and all related
  utilities.
- [**misc**](app\src\kotlin\ir\mahdiparastesh\fortuna\misc) subpackage : miscellaneous add-ons and utilities.

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
VIM LICENSE

I)  There are no restrictions on distributing unmodified copies of Fortuna
    except that they must include this license text.  You can also distribute
    unmodified parts of Fortuna, likewise unrestricted except that they must
    include this license text.  You are also allowed to include executables
    that you made from the unmodified Fortuna sources, plus your own usage
    examples.

II) It is allowed to distribute a modified (or extended) version of Fortuna,
    including executables and/or source code, when the following four
    conditions are met:
    1) This license text must be included unmodified.
    2) The modified Fortuna must be distributed in one of the following five
       ways:
       a) If you make changes to Fortuna yourself, you must clearly describe
          in the distribution how to contact you.  When the maintainer asks
          you (in any way) for a copy of the modified Fortuna you
          distributed, you must make your changes, including source code,
          available to the maintainer without fee.  The maintainer reserves
          the right to include your changes in the official version of
          Fortuna.  What the maintainer will do with your changes and under
          what license they will be distributed is negotiable.  If there has
          been no negotiation then this license, or a later version, also
          applies to your changes. The current maintainer is Mahdi Parastesh
          <fulcrum1378@gmail.com>.  If this changes it will be announced in
          appropriate places (most likely mahdiparastesh.ir).  When it is
          completely impossible to contact the maintainer, the obligation to
          send him your changes ceases.  Once the maintainer has confirmed
          that he has received your changes they will not have to be sent
          again.
       b) If you have received a modified Fortuna that was distributed as
          mentioned under a) you are allowed to further distribute it
          unmodified, as mentioned at I).  If you make additional changes the
          text under a) applies to those changes.
       c) Provide all the changes, including source code, with every copy of
          the modified Fortuna you distribute.  This may be done in the form
          of a context diff.  You can choose what license to use for new code
          you add.  The changes and their license must not restrict others
          from making their own changes to the official version of Fortuna.
       d) When you have a modified Fortuna which includes changes as
          mentioned under c), you can distribute it without the source code
          for the changes if the following three conditions are met:
          - The license that applies to the changes permits you to distribute
            the changes to the Fortuna maintainer without fee or restriction,
            and permits the Fortuna maintainer to include the changes in the
            official version of Fortuna without fee or restriction.
          - You keep the changes for at least three years after last
            distributing the corresponding modified Fortuna.  When the
            maintainer or someone who you distributed the modified Fortuna
            to asks you (in any way) for the changes within this period, you
            must make them available to him.
          - You clearly describe in the distribution how to contact you.  This
            contact information must remain valid for at least three years
            after last distributing the corresponding modified Fortuna, or
            as long as possible.
       e) When the GNU General Public License (GPL) applies to the changes,
          you can distribute the modified Fortuna under the GNU GPL version
          2 or any later version.
    3) A message must be added, at least in the intro screen, such that the
       user of the modified Fortuna is able to see that it was modified.
       When distributing as mentioned under 2)e) adding the message is only
       required for as far as this does not conflict with the license used
       for the changes.
    4) The contact information as required under 2)a) and 2)d) must not be
       removed or changed, except that the person himself can make
       corrections.

III) If you distribute a modified version of Fortuna, you are encouraged to
     use the Vim license for your changes and make them available to the
     maintainer, including the source code.  The preferred way to do this is
     by e-mail or by uploading the files to a server and e-mailing the URL. If
     the number of changes is small (e.g., a modified Makefile) e-mailing a
     context diff will do.  The e-mail address to be used is
     <fulcrum1378@gmail.com>

IV)  It is not allowed to remove this license from the distribution of the
     Fortuna sources, parts of it or from a modified version.  You may use
     this license for previous Fortuna releases instead of the license that
     they came with, at your option.

```
