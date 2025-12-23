package com.thomaskuenneth.tkweek.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import java.util.Calendar
import java.util.Date

@HiltViewModel
class MyDayViewModel @Inject constructor() : ViewModel() {
    var cal: Calendar = Calendar.getInstance()
        private set

    fun setCalendarTime(date: Date) {
        cal.time = date
    }

    fun setCalendarTime(time: Long) {
        setCalendarTime(Date(time))
    }
}
