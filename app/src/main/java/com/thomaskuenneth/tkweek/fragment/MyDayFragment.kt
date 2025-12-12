/*
 * myDayFragment.kt
 *
 * Copyright 2021 MATHEMA GmbH
 *           2022 - 2025 Thomas Künneth
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.thomaskuenneth.tkweek.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Looper
import android.provider.CalendarContract
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.setFragmentResultListener
import androidx.preference.PreferenceManager
import com.thomaskuenneth.tkweek.R
import com.thomaskuenneth.tkweek.util.Helper
import com.thomaskuenneth.tkweek.adapter.AnnualEventsListAdapter
import com.thomaskuenneth.tkweek.databinding.MydayBinding
import com.thomaskuenneth.tkweek.fragment.CalendarFragment.Companion.isDayOff
import com.thomaskuenneth.tkweek.fragment.WeekFragment.Companion.prepareCalendar
import com.thomaskuenneth.tkweek.types.Event
import com.thomaskuenneth.tkweek.types.Namenstage
import com.thomaskuenneth.tkweek.types.Zodiac
import com.thomaskuenneth.tkweek.util.CalendarContractUtils
import com.thomaskuenneth.tkweek.util.DateUtilities
import com.thomaskuenneth.tkweek.util.TKWeekUtils
import com.thomaskuenneth.tkweek.util.TKWeekUtils.linkToSettings
import java.text.DateFormat
import java.text.MessageFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val TAG = "MyDayFragment"

class MyDayFragment : TKWeekBaseFragment<MydayBinding>() {

    private val binding get() = backing!!

    private var eventsLoader: AsyncTask<Void, Void, AnnualEventsListAdapter>? = null

    private lateinit var cal: Calendar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(RESULT_NOTES) { _, bundle ->
            saveNoteAndUpdateUI(
                bundle.getString(
                    ARGS_NOTES, ""
                )
            )
        }
        setFragmentResultListener(RESULT_DATEPICKER) { _, bundle ->
            cal.set(Calendar.YEAR, bundle.getInt(ARGS_YEAR))
            cal.set(Calendar.MONTH, bundle.getInt(ARGS_MONTH))
            cal.set(Calendar.DAY_OF_MONTH, bundle.getInt(ARGS_DAY_OF_MONTH))
            updateViews()
            requireActivity().invalidateOptionsMenu()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        backing = MydayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        eventsLoader = null
        binding.myDaySymbolNotes.setOnClickListener {
            val fragment = EditNotesFragment().also {
                it.arguments = Bundle().also { bundle ->
                    bundle.putString(ARGS_NOTES, binding.myDayNotes.text.toString())
                }
            }
            fragment.show(
                parentFragmentManager,
                EditNotesFragment.TAG
            )
        }
        binding.myDaySymbolDelete.setOnClickListener {
            saveNoteAndUpdateUI("")
        }
        cal = Calendar.getInstance()
        arguments?.run {
            val time = getLong(DATE)
            if (time > 0)
                cal.time = Date(time)
        }
        val permissions = ArrayList<String>()
        if (shouldShowBirthdays() && !TKWeekUtils.canReadContacts(requireContext())
            && !shouldShowPermissionReadContactsRationale()
        ) {
            permissions.add(Manifest.permission.READ_CONTACTS)
        }
        if (shouldShowAppointments() && !TKWeekUtils.canReadCalendar(requireContext())
            && !shouldShowPermissionReadCalendarRationale()
        ) {
            permissions.add(Manifest.permission.READ_CALENDAR)
        }
        if (permissions.isNotEmpty()) {
            val l = arrayOfNulls<String>(permissions.size)
            permissions.toArray(l)
            requestPermissions(l, 0)
        }
        updateViews()
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (!grantResults.indices.isEmpty())
            prepareEventsLoader()
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_today, menu)
        inflater.inflate(R.menu.menu_new_appointment, menu)
        inflater.inflate(R.menu.menu_goto_date, menu)
        inflater.inflate(R.menu.menu_lookup_in_wikipedia, menu)
    }

    @Deprecated("Deprecated in Java")
    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.today)?.run {
            isVisible = !DateUtilities.isToday(cal)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.today -> {
                cal.time = Date()
                updateViews()
                requireActivity().invalidateOptionsMenu()
                return true
            }

            R.id.look_up_in_wikipedia -> {
                lookUpInWikipedia()
                return true
            }

            R.id.mi_new_appointment -> {
                val i2 = Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI)
                i2.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, cal.timeInMillis)
                try {
                    startActivity(i2)
                } catch (e: ActivityNotFoundException) {
                    Log.e(TAG, "no activity found", e)
                }
                return true
            }

            R.id.goto_date -> {
                val datePickerFragment = DatePickerFragment().also {
                    it.arguments = Bundle().also { bundle ->
                        bundle.putInt(ARGS_YEAR, cal.get(Calendar.YEAR))
                        bundle.putInt(ARGS_MONTH, cal.get(Calendar.MONTH))
                        bundle.putInt(ARGS_DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH))
                    }
                }
                datePickerFragment.show(
                    parentFragmentManager,
                    DatePickerFragment.TAG
                )
                requireActivity().invalidateOptionsMenu()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        prepareEventsLoader()
    }

    override fun onPause() {
        cancelEventsLoader()
        super.onPause()
    }

    override fun preferencesFinished(resultCode: Int, data: Intent?) {
        updateViews()
    }

    private fun cancelEventsLoader() {
        if (eventsLoader != null) {
            eventsLoader?.cancel(true)
            eventsLoader = null
        }
    }

    private fun prepareEventsLoader() {
        cancelEventsLoader()
        eventsLoader = @SuppressLint("StaticFieldLeak")
        object : AsyncTask<Void, Void, AnnualEventsListAdapter>() {
            @Deprecated("Deprecated in Java")
            override fun onPreExecute() {
            }

            @Deprecated("Deprecated in Java")
            override fun doInBackground(vararg params: Void): AnnualEventsListAdapter? {
                if (Looper.myLooper() == null) {
                    Looper.prepare()
                }
                return AnnualEventsListAdapter(
                    requireContext(),
                    cal,
                    cal,
                    true,
                    null
                )
            }

            @Deprecated("Deprecated in Java")
            override fun onPostExecute(result: AnnualEventsListAdapter?) {
                eventsLoader = null
                updateEvents(result!!)
            }
        }
        eventsLoader?.execute()
    }

    private fun updateViews() {
        val prefs = PreferenceManager
            .getDefaultSharedPreferences(requireContext())
        val hide = prefs.getBoolean("hide_nameday", false)
        binding.myDayNameDay.visibility = if (hide) View.GONE else View.VISIBLE
        binding.myDayLabelNameDay.visibility = if (hide) View.GONE else View.VISIBLE
        val hideAstrologicalSign = prefs.getBoolean("hide_astrological_sign", false)
        binding.myDayAstrologicalSign.visibility =
            if (hideAstrologicalSign) View.GONE else View.VISIBLE
        binding.myDayLabelAstrologicalSign.visibility =
            if (hideAstrologicalSign) View.GONE else View.VISIBLE
        prepareCalendar(cal, requireContext(), binding.myDayLabelWeekNumber, true)
        DateUtilities.clearTimeRelatedFields(cal)
        val weekNumber = cal.get(Calendar.WEEK_OF_YEAR)
        val maxWeekNumber = cal.getActualMaximum(Calendar.WEEK_OF_YEAR)
        binding.myDayWeekNumber.text = getString(
            R.string.day_of_year, weekNumber,
            maxWeekNumber, maxWeekNumber - weekNumber
        )
        val strDate = if (DateUtilities.isToday(cal)) {
            getString(
                R.string.string1_string2,
                Helper.FORMAT_FULL.format(cal.time),
                getString(R.string.today)
            )
        } else {
            Helper.FORMAT_FULL.format(cal.time)
        }
        if (isDayOff(requireContext(), cal.time)) {
            binding.myDayDate.text = getString(
                R.string.string1_dash_string2, strDate,
                getString(R.string.day_off)
            )
        } else {
            binding.myDayDate.text = strDate
        }
        val date = cal.time
        val current = cal.get(Calendar.DAY_OF_YEAR)
        val max = cal.getActualMaximum(Calendar.DAY_OF_YEAR)
        binding.myDayDayInYear.text = getString(
            R.string.day_of_year, current, max, max
                    - current
        )
        binding.myDayIsLeapYear.text = if (DateUtilities.isSchaltjahr(cal.get(Calendar.YEAR)))
            getString(R.string.yes)
        else getString(
            R.string.no
        )
        binding.myDayAstrologicalSign.text = Zodiac.getSign(requireContext(), date)
        binding.myDayNameDay.text = Namenstage.getNameDays(requireContext(), date)
        prepareEventsLoader()
        updateNotes()
    }

    private fun updateEvents(adapter: AnnualEventsListAdapter) {
        if (!isAdded) return
        layoutInflater.run {
            binding.myDayEvents.removeAllViews()
            if (shouldShowBirthdays() && !TKWeekUtils.canReadContacts(requireContext())
                && shouldShowPermissionReadContactsRationale()
            ) {
                val layout =
                    inflate(
                        R.layout.message_link_to_settings,
                        binding.myDayEvents,
                        false
                    ) as ConstraintLayout
                linkToSettings(layout, requireActivity(), R.string.str_permission_read_contacts)
                val button = layout.findViewById<Button>(R.id.button)
                button.setOnClickListener {
                    requestReadContacts()
                }
                binding.myDayEvents.addView(layout)
            }
            for (position in 0 until adapter.count) {
                val event = adapter.getItem(position) as Event
                val temp = DateUtilities.getCalendar(
                    event.year,
                    event.month,
                    event.day
                )
                if (event.annuallyRepeating) {
                    temp[Calendar.YEAR] = cal.get(Calendar.YEAR)
                }
                if (DateUtilities.diffDayPeriods(cal, temp) == 0L) {
                    val descr = adapter.getDescription(event, requireContext())
                    val parent =
                        inflate(R.layout.string_one_line2, binding.myDayEvents, false)
                    binding.myDayEvents.addView(parent)
                    val str = parent
                        .findViewById<TextView>(R.id.string_one_line2_text)
                    str.text = descr
                    val color = parent.findViewById<View>(R.id.string_one_line2_color)
                    color.setBackgroundColor(event.color)
                }
            }
            maybeAddNone(this, binding.myDayEvents)
            updateAppointments(this)
        }
    }

    private fun updateAppointments(inflater: LayoutInflater) {
        binding.myDayAppointments.removeAllViews()
        val show = shouldShowAppointments()
        var showIfNoPermission = true
        if (show && !TKWeekUtils.canReadCalendar(requireContext())
            && shouldShowPermissionReadCalendarRationale()
        ) {
            val layout =
                inflater.inflate(
                    R.layout.message_link_to_settings,
                    binding.myDayAppointments,
                    false
                ) as ConstraintLayout
            linkToSettings(layout, requireActivity(), R.string.str_permission_read_calendar)
            val button = layout.findViewById<Button>(R.id.button)
            button.setOnClickListener {
                requestReadCalendar()
            }
            binding.myDayAppointments.addView(layout)
            showIfNoPermission = false
        }
        if (TKWeekUtils.canReadCalendar(requireContext())) {
            val now = Date()
            val list = CalendarContractUtils.getAppointments(
                requireContext(),
                cal
            )
            for (appointment in list) {
                val parent =
                    inflater.inflate(
                        R.layout.appointment_two_line,
                        binding.myDayAppointments,
                        false
                    )
                binding.myDayAppointments.addView(parent)
                val from = Date(appointment.dtstart)
                val to = Date(appointment.dtend)
                val isFutureOrOngoing = now.before(to)
                val line1 = parent
                    .findViewById<TextView>(R.id.appointment_two_line_1)
                var title = TKWeekUtils.getStringNotNull(appointment.title)
                if (title.isEmpty()) {
                    title = getString(R.string.no_title)
                }
                line1.isEnabled = isFutureOrOngoing
                line1.text = title
                val line2 = parent
                    .findViewById<TextView>(R.id.appointment_two_line_2)
                var dateformat: DateFormat
                val calFrom = DateUtilities.getCalendar(from)
                val calTo = DateUtilities.getCalendar(to)
                dateformat = if (DateUtilities.diffDayPeriods(calFrom, calTo) != 0L) {
                    Helper.FORMAT_DATE_TIME_SHORT
                } else {
                    Helper.FORMAT_TIME_SHORT
                }
                val sb = StringBuilder()
                var duration = ((appointment.dtend - appointment.dtstart) / 60000).toInt()
                while (duration > 0) {
                    duration = appendTime(duration, sb)
                }
                val description = appointment.description
                if (description != null && description.isNotEmpty()) {
                    sb.append('\n')
                    sb.append(description)
                }
                line2.text = getString(
                    R.string.from_to, dateformat.format(from),
                    dateformat.format(to), sb.toString()
                )
                line2.isEnabled = isFutureOrOngoing
                val color = parent.findViewById<View>(R.id.appointment_two_line_color)
                color.setBackgroundColor(appointment.color)
            }
            maybeAddNone(inflater, binding.myDayAppointments)
        } else if (showIfNoPermission) {
            addNoPermission(inflater, binding.myDayAppointments)
        }
        binding.myDayLabelAppointments.visibility = if (show) View.VISIBLE else View.GONE
        binding.myDayAppointments.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun appendTime(minutes: Int, sb: StringBuilder): Int {
        var mins = minutes
        if (sb.isNotEmpty()) {
            sb.append(", ")
        } else {
            sb.append("\n")
        }
        when {
            mins >= Helper.MINUTES_PER_DAY -> {
                val days = mins / Helper.MINUTES_PER_DAY
                sb.append(days)
                sb.append(" ")
                sb.append(this.getString(if (days == 1) R.string.day else R.string.days))
                mins %= Helper.MINUTES_PER_DAY
            }

            mins >= 60 -> {
                val hours = mins / 60
                sb.append(hours)
                sb.append(" ")
                sb.append(this.getString(if (hours == 1) R.string.hour else R.string.hours))
                mins %= 60
            }

            else -> {
                sb.append(mins)
                sb.append(" ")
                sb.append(this.getString(if (mins == 1) R.string.minute else R.string.minutes))
                mins = 0
            }
        }
        return mins
    }

    private fun lookUpInWikipedia() {
        var pattern = "https://en.m.wikipedia.org/wiki/{0}_{1}"
        var loc = Locale.ENGLISH
        try {
            val l = Locale.getDefault()
            if (Locale.GERMAN.language == l.language) {
                pattern = "https://de.m.wikipedia.org/wiki/{1}._{0}#_"
                loc = l
            }
        } catch (t: Throwable) {
            Log.e(TAG, "lookUpInWikipedia()", t)
        }
        val df = SimpleDateFormat("MMMM", loc)
        val month = df.format(cal.time)
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val url = MessageFormat.format(pattern, month, day)
        val viewIntent = Intent(
            "android.intent.action.VIEW",
            Uri.parse(url)
        )
        startActivity(viewIntent)
    }

    private fun maybeAddNone(inflater: LayoutInflater, layout: LinearLayout) {
        if (layout.childCount == 0) {
            val tv = inflater.inflate(
                R.layout.string_one_line,
                layout, false
            ) as TextView
            tv.setText(R.string.none)
            layout.addView(tv)
        }
    }

    private fun addNoPermission(inflater: LayoutInflater, layout: LinearLayout) {
        val tv = inflater.inflate(
            R.layout.string_one_line,
            layout, false
        ) as TextView
        tv.setText(R.string.no_permission)
        layout.addView(tv)
    }

    private fun updateNotes() {
        val note = TKWeekUtils.load(requireContext(), getNameForNotes())
        updateNoteAndDeleteButton(note)
    }

    private fun getNameForNotes(): String {
        return "Note_" + Helper.FORMAT_YYYYMMDD.format(cal.time)
    }

    private fun saveNoteAndUpdateUI(note: String) {
        if (TKWeekUtils.save(requireActivity(), getNameForNotes(), note)) {
            updateNoteAndDeleteButton(note)
        }
    }

    private fun updateNoteAndDeleteButton(note: String) {
        binding.myDayNotes.text = note
        binding.myDaySymbolDelete.isEnabled = note.isNotEmpty()
    }
}
