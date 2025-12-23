package com.thomaskuenneth.tkweek.viewmodel

import androidx.lifecycle.ViewModel
import java.util.Calendar
import java.util.Date

class MyDayViewModel : ViewModel() {
    var cal: Calendar = Calendar.getInstance()
        private set

    fun setCalendarTime(date: Date) {
        cal.time = date
    }

    fun setCalendarTime(time: Long) {
        setCalendarTime(Date(time))
    }
}
