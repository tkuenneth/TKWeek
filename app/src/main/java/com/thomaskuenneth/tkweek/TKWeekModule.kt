package com.thomaskuenneth.tkweek

import androidx.annotation.StringRes
import com.thomaskuenneth.tkweek.fragment.AboutFragment
import com.thomaskuenneth.tkweek.fragment.AboutYearFragment
import com.thomaskuenneth.tkweek.fragment.AnnualEventsFragment
import com.thomaskuenneth.tkweek.fragment.CalendarFragment
import com.thomaskuenneth.tkweek.fragment.DateCalculatorFragment
import com.thomaskuenneth.tkweek.fragment.DaysBetweenDatesFragment
import com.thomaskuenneth.tkweek.fragment.MyDayFragment
import com.thomaskuenneth.tkweek.fragment.PreferencesFragment
import com.thomaskuenneth.tkweek.fragment.WeekFragment

enum class TKWeekModule(
    @param:StringRes val titleRes: Int,
    @param:StringRes val descriptionRes: Int,
    val clazz: Class<*>
) {
    Week(
        R.string.week_activity_text1,
        R.string.week_activity_text2,
        WeekFragment::class.java
    ),
    MyDay(
        R.string.myday_activity_text1,
        R.string.myday_activity_text2,
        MyDayFragment::class.java
    ),
    DaysBetweenDates(
        R.string.days_between_dates_activity_text1,
        R.string.days_between_dates_activity_text2,
        DaysBetweenDatesFragment::class.java
    ),
    DateCalculator(
        R.string.date_calculator_activity_text1,
        R.string.date_calculator_activity_text2,
        DateCalculatorFragment::class.java
    ),
    AnnualEvents(
        R.string.annual_events_activity_text1,
        R.string.annual_events_activity_text2,
        AnnualEventsFragment::class.java
    ),
    AboutAYear(
        R.string.about_a_year_activity_text1,
        R.string.about_a_year_activity_text2,
        AboutYearFragment::class.java
    ),
    Calendar(
        R.string.calendar_activity_text1,
        R.string.calendar_activity_text2,
        CalendarFragment::class.java
    ),
    Prefs(
        R.string.settings,
        R.string.set_alarm_description,
        PreferencesFragment::class.java
    ),
    About(
        R.string.about_activity_text1,
        R.string.about_activity_text2,
        AboutFragment::class.java
    )
}