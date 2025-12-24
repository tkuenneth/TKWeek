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
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.CallLog.Calls
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import androidx.core.view.isEmpty
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.PreferenceManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.thomaskuenneth.tkweek.R
import com.thomaskuenneth.tkweek.adapter.AnnualEventsListAdapter
import com.thomaskuenneth.tkweek.databinding.MydayBinding
import com.thomaskuenneth.tkweek.fragment.CalendarFragment.Companion.isDayOff
import com.thomaskuenneth.tkweek.fragment.WeekFragment.Companion.prepareCalendar
import com.thomaskuenneth.tkweek.types.Call
import com.thomaskuenneth.tkweek.types.Event
import com.thomaskuenneth.tkweek.types.Zodiac
import com.thomaskuenneth.tkweek.util.CalendarContractUtils
import com.thomaskuenneth.tkweek.util.DateUtilities
import com.thomaskuenneth.tkweek.util.Helper
import com.thomaskuenneth.tkweek.util.Helper.DATE
import com.thomaskuenneth.tkweek.util.TKWeekUtils
import com.thomaskuenneth.tkweek.util.TKWeekUtils.linkToSettings
import com.thomaskuenneth.tkweek.viewmodel.AppBarAction
import com.thomaskuenneth.tkweek.viewmodel.MyDayViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import java.text.MessageFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val TAG = "MyDayFragment"

class MyDayFragment : TKWeekBaseFragment<MydayBinding>() {

    private val binding get() = backing!!

    private val loadTrigger = Channel<Unit>(Channel.CONFLATED)

    private val myDayViewModel: MyDayViewModel by activityViewModels()

    private val cal: Calendar
        get() = myDayViewModel.cal

    private var hasTelephony = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(RESULT_NOTES) { _, bundle ->
            saveNoteAndUpdateUI(
                bundle.getString(
                    ARGS_NOTES, ""
                )
            )
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
        super.onViewCreated(view, savedInstanceState)
        val pm = requireContext().packageManager
        hasTelephony = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                launch {
                    loadTrigger.receiveAsFlow().collectLatest {
                        val context = requireContext()
                        val calClone = cal.clone() as Calendar
                        val adapter = runInterruptible(Dispatchers.IO) {
                            AnnualEventsListAdapter(
                                context,
                                calClone,
                                calClone,
                                true,
                                null
                            )
                        }
                        updateEvents(adapter)
                        updateMissedCalls()
                    }
                }
                triggerLoad()
            }
        }
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
        arguments?.run {
            val time = getLong(DATE)
            if (time > 0) {
                myDayViewModel.setCalendarTime(time)
            }
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
        if (isShowMissedCalls()) {
            if (!TKWeekUtils.canReadCallLog(requireContext()) && !shouldShowPermissionReadCallLogRationale()) {
                permissions.add(Manifest.permission.READ_CALL_LOG)
            }
        }
        if (permissions.isNotEmpty()) {
            val l = arrayOfNulls<String>(permissions.size)
            permissions.toArray(l)
            requestMultiplePermissions(l.requireNoNulls())
        }
        binding.myDayDate.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setSelection(cal.timeInMillis)
                .build()
            picker.addOnPositiveButtonClickListener { selection ->
                myDayViewModel.setCalendarTime(selection)
                updateViews()
                updateAppBarActions()
            }
            picker.show(parentFragmentManager, "date_picker")
        }
        binding.myDayToday.setOnClickListener {
            myDayViewModel.setCalendarTime(Date())
            updateViews()
            updateAppBarActions()
        }
        updateViews()
        linkToSettings(binding.keyValueContainer, requireActivity(), R.string.go_to_settings)
    }

    override fun onReadContactsPermissionResult(isGranted: Boolean) {
        triggerLoad()
    }

    override fun onReadCalendarPermissionResult(isGranted: Boolean) {
        triggerLoad()
    }

    override fun onReadCallLogPermissionResult(isGranted: Boolean) {
        triggerLoad()
    }

    override fun onMultiplePermissionsResult(results: Map<String, Boolean>) {
        if (results.isNotEmpty()) {
            triggerLoad()
        }
    }

    override fun updateAppBarActions() {
        val actions = listOf(
            AppBarAction(
                title = R.string.look_up_in_wikipedia,
                onClick = {
                    lookUpInWikipedia()
                }
            ),
            AppBarAction(
                title = R.string.new_appointment,
                onClick = {
                    val i2 = Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI)
                    i2.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, cal.timeInMillis)
                    try {
                        startActivity(i2)
                    } catch (e: ActivityNotFoundException) {
                        Log.e(TAG, "no activity found", e)
                    }
                }
            )
        )
        viewModel.setAppBarActions(actions)
    }

    private fun triggerLoad() {
        loadTrigger.trySend(Unit)
    }

    private fun updateViews() {
        val prefs = PreferenceManager
            .getDefaultSharedPreferences(requireContext())
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
        val strDate = Helper.FORMAT_EEE_D_MMM_YYYY.format(cal.time)
        if (isDayOff(requireContext(), cal.time)) {
            binding.myDayDate.text = getString(
                R.string.string1_dash_string2, strDate,
                getString(R.string.day_off)
            )
        } else {
            binding.myDayDate.text = strDate
        }
        binding.myDayToday.isEnabled = !DateUtilities.isToday(cal)
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
        triggerLoad()
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
                    val description = adapter.getDescription(event, requireContext())
                    val parent =
                        inflate(R.layout.string_one_line2, binding.myDayEvents, false)
                    binding.myDayEvents.addView(parent)
                    val str = parent
                        .findViewById<TextView>(R.id.string_one_line2_text)
                    str.text = description
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
                val calFrom = DateUtilities.getCalendar(from)
                val calTo = DateUtilities.getCalendar(to)
                val dateFormat = if (DateUtilities.diffDayPeriods(calFrom, calTo) != 0L) {
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
                    R.string.from_to, dateFormat.format(from),
                    dateFormat.format(to), sb.toString()
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
            url.toUri()
        )
        startActivity(viewIntent)
    }

    private fun maybeAddNone(inflater: LayoutInflater, layout: LinearLayout) {
        if (layout.isEmpty()) {
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

    private fun isShowMissedCalls(): Boolean {
        var show = hasTelephony
        if (show) {
            val prefs = PreferenceManager
                .getDefaultSharedPreferences(requireContext())
            show = !prefs.getBoolean("hide_missed_calls", false)
        }
        return show
    }

    private suspend fun updateMissedCalls() {
        if (!isAdded) return
        val show = isShowMissedCalls()
        binding.missedCallsContainer.visibility = if (show) View.VISIBLE else View.GONE
        binding.dividerMissedCalls.visibility = if (show) View.VISIBLE else View.GONE
        if (show) {
            binding.myDayMissedCalls.removeAllViews()
            val inflater = layoutInflater
            var showIfNoPermission = true
            if (!TKWeekUtils.canReadCallLog(requireContext()) && shouldShowPermissionReadCallLogRationale()) {
                val layout =
                    inflater.inflate(
                        R.layout.message_link_to_settings,
                        binding.myDayMissedCalls,
                        false
                    ) as ConstraintLayout
                linkToSettings(layout, requireActivity(), R.string.missing_permission_call_log)
                val button = layout.findViewById<Button>(R.id.button)
                button.setOnClickListener {
                    requestReadCallLog()
                }
                binding.myDayMissedCalls.addView(layout)
                showIfNoPermission = false
            }
            if (TKWeekUtils.canReadCallLog(requireContext())) {
                val list = runInterruptible(Dispatchers.IO) {
                    getMissedCalls()
                }
                val now = Calendar.getInstance()
                val current = Calendar.getInstance()
                for (position in list.indices) {
                    val parent =
                        inflater.inflate(R.layout.two_line_item, binding.myDayMissedCalls, false)
                    binding.myDayMissedCalls.addView(parent)
                    parent.setPadding(0, 0, 0, 0)
                    val divider = parent.findViewById<View>(R.id.divider)
                    divider.visibility = if (position > 0) View.VISIBLE else View.GONE
                    val text1 = parent.findViewById<TextView>(R.id.text1)
                    val text2 = parent.findViewById<TextView>(R.id.text2)
                    val text3 = parent.findViewById<TextView>(R.id.text3)
                    val text4 = parent.findViewById<TextView>(R.id.text4)
                    val call = list[position]
                    parent.setOnClickListener {
                        val uri = Uri.withAppendedPath(
                            Calls.CONTENT_URI,
                            call._id.toString()
                        )
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, Calls.CONTENT_TYPE)
                        }
                        try {
                            startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            Log.e(TAG, "updateMissedCalls()", e)
                            Toast.makeText(
                                requireContext(),
                                R.string.error_call_log, Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    var number = call.number
                    number = if ("-1" == number) {
                        getString(R.string.unknown)
                    } else {
                        PhoneNumberUtils.formatNumber(number, Locale.getDefault().country) ?: number
                    }
                    if (call.name.isNotEmpty()) {
                        text1.text = call.name
                        text2.text = getString(
                            R.string.string1_string2,
                            call.label, number
                        )
                    } else {
                        text1.text = number
                        text2.visibility = View.GONE
                    }
                    current.timeInMillis = call.date
                    val time = current.time
                    val days = DateUtilities.diffDayPeriods(
                        now,
                        current
                    )
                    text3.text = AnnualEventsListAdapter.getDaysAsString(
                        inflater,
                        days
                    )
                    text4.text = Helper.FORMAT_TIME_SHORT.format(time)
                    text4.visibility = View.VISIBLE
                }
                maybeAddNone(inflater, binding.myDayMissedCalls)
            } else if (showIfNoPermission) {
                addNoPermission(inflater, binding.myDayMissedCalls)
            }
        }
    }

    private fun getMissedCalls(): List<Call> {
        val missedCalls: MutableList<Call> = ArrayList()
        val projection = arrayOf(
            Calls.NUMBER, Calls.DATE, Calls.CACHED_NAME,
            Calls.CACHED_NUMBER_TYPE, Calls.CACHED_NUMBER_LABEL, Calls._ID
        )
        val selection = Calls.TYPE + " = ?"
        val selectionArgs = arrayOf(Calls.MISSED_TYPE.toString())
        var c: Cursor? = null
        try {
            c = requireActivity().contentResolver.query(
                Calls.CONTENT_URI, projection,
                selection, selectionArgs, Calls.DEFAULT_SORT_ORDER
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "getMissedCalls()", e)
        }
        if (c != null) {
            val idxNumber = c.getColumnIndex(Calls.NUMBER)
            val idxDate = c.getColumnIndex(Calls.DATE)
            val idxCachedName = c.getColumnIndex(Calls.CACHED_NAME)
            val idxCachedNumberType = c
                .getColumnIndex(Calls.CACHED_NUMBER_TYPE)
            val idxCachedNumberLabel = c
                .getColumnIndex(Calls.CACHED_NUMBER_LABEL)
            val idxID = c.getColumnIndex(Calls._ID)
            while (c.moveToNext()) {
                var number = ""
                if (number.isEmpty()) {
                    number = TKWeekUtils.getStringNotNull(
                        c
                            .getString(idxNumber)
                    )
                }
                val date = c.getLong(idxDate)
                val name = TKWeekUtils.getStringNotNull(
                    c
                        .getString(idxCachedName)
                )
                var label = TKWeekUtils.getStringNotNull(
                    c
                        .getString(idxCachedNumberLabel)
                )
                if (label.isEmpty()) {
                    val type = c.getInt(idxCachedNumberType)
                    val resId = ContactsContract.CommonDataKinds.Phone
                        .getTypeLabelResource(type)
                    label = getString(resId)
                }
                val idx = c.getInt(idxID)
                val call = Call(number, date, name, label, idx)
                missedCalls.add(call)
            }
            c.close()
        }
        return missedCalls
    }
}
