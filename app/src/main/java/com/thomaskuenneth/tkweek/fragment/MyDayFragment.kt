/*
 * MyDayFragment.kt
 *
 * Copyright 2021 MATHEMA GmbH
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
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.*
import android.provider.CalendarContract
import android.util.Log
import android.view.*
import android.widget.*
import androidx.preference.PreferenceManager
import com.thomaskuenneth.tkweek.EnterTaskActivity
import com.thomaskuenneth.tkweek.R
import com.thomaskuenneth.tkweek.activity.TKWeekActivity
import com.thomaskuenneth.tkweek.adapter.AnnualEventsListAdapter
import com.thomaskuenneth.tkweek.databinding.MydayBinding
import com.thomaskuenneth.tkweek.fragment.CalendarFragment.Companion.isDayOff
import com.thomaskuenneth.tkweek.fragment.WeekFragment.Companion.prepareCalendar
import com.thomaskuenneth.tkweek.types.Event
import com.thomaskuenneth.tkweek.types.Namenstage
import com.thomaskuenneth.tkweek.types.Task
import com.thomaskuenneth.tkweek.types.Zodiac
import com.thomaskuenneth.tkweek.util.CalendarContractUtils
import com.thomaskuenneth.tkweek.util.DateUtilities
import com.thomaskuenneth.tkweek.util.TKWeekUtils
import com.thomaskuenneth.tkweek.util.TKWeekUtils.linkToSettings
import com.thomaskuenneth.tkweek.util.TasksUtils
import java.text.DateFormat
import java.text.MessageFormat
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "MyDayFragment"
private const val RQ_ADD_TASK = 1234
private const val SHOW_COMPLETED_TASKS = "show_completed_tasks"

class MyDayFragment : TKWeekBaseFragment<MydayBinding>(),
    CompoundButton.OnCheckedChangeListener {

    private val binding get() = backing!!

    private val handler = UpdateTasksHandler(this)
    private val handlerTaskCompleted: Handler = TasksCompletedHandler(this)

    private var eventsLoader: AsyncTask<Void, Void, AnnualEventsListAdapter>? = null

    private var cal: Calendar? = null
    private var tasksNeedUpdate = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        backing = MydayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        eventsLoader = null
        binding.mydaySymbolNotes.setOnClickListener {
            val fragment = EditNotesFragment(binding.mydayNotes.text.toString()) { note ->
                saveNoteAndUpdateUI(note)
            }
            fragment.show(
                parentFragmentManager,
                EditNotesFragment.TAG
            )
        }
        binding.mydaySymbolDelete.setOnClickListener {
            saveNoteAndUpdateUI("")
        }
        cal = Calendar.getInstance()
        arguments?.run {
            val time = getLong(DATE)
            if (time > 0)
                cal?.time = Date(time)
        }
        val permissins = ArrayList<String>()
        if (shouldShowBirthdays() && !TKWeekUtils.canReadContacts(requireContext())
            && !shouldShowPermissionReadContactsRationale()
        ) {
            permissins.add(Manifest.permission.READ_CONTACTS)
        }
        if (shouldShowAppointments() && !TKWeekUtils.canReadCalendar(requireContext())
            && !shouldShowPermissionReadCalendarRationale()
        ) {
            permissins.add(Manifest.permission.READ_CALENDAR)
        }
        if (permissins.size > 0) {
            val l = arrayOfNulls<String>(permissins.size)
            permissins.toArray(l)
            requestPermissions(l, 0)
        }
        updateViews()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (!grantResults.indices.isEmpty())
            prepareEventsLoader()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_today, menu)
        inflater.inflate(R.menu.menu_new_appointment, menu)
        if (hasGoogleAccount()) {
            inflater.inflate(R.menu.menu_add_task, menu)
        }
        inflater.inflate(R.menu.menu_goto_date, menu)
        inflater.inflate(R.menu.menu_lookup_in_wikipedia, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.show_or_hide_done_tasks)?.run {
            setTitle(if (isShowCompletedTasks()) R.string.hide_completed_tasks else R.string.show_completed_tasks)
        }
        menu.findItem(R.id.today)?.run {
            isVisible = !DateUtilities.isToday(cal)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.today -> {
                cal?.time = Date()
                updateViews()
                requireActivity().invalidateOptionsMenu()
                return true
            }
            R.id.look_up_in_wikipedia -> {
                lookUpInWikipedia()
                return true
            }
            R.id.add_task -> {
                val i = Intent(requireContext(), EnterTaskActivity::class.java)
                startActivityForResult(i, RQ_ADD_TASK)
                return true
            }
            R.id.show_or_hide_done_tasks -> {
                toggleShowCompletedTasks()
                prepareEventsLoader()
                return true
            }
            R.id.mi_new_appointment -> {
                val i2 = Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI)
                i2.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, cal?.timeInMillis)
                try {
                    startActivity(i2)
                } catch (e: ActivityNotFoundException) {
                    Log.e(TAG, "keine passende Activity", e)
                }
                return true
            }
            R.id.goto_date -> {
                val datePickerFragment =
                    DatePickerFragment { _, year, month, dayOfMonth ->
                        cal?.set(Calendar.YEAR, year)
                        cal?.set(Calendar.MONTH, month)
                        cal?.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                        updateViews()
                        requireActivity().invalidateOptionsMenu()
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

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        val o = buttonView.tag
        if (o is Task) {
            val u = TasksUtils.getInstance(handlerTaskCompleted, requireActivity())
            u?.markFinished(o.selfLink, buttonView.parent.parent, isChecked)
        }
    }

    override fun preferencesFinished(resultCode: Int, data: Intent?) {
        updateViews()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                RQ_ADD_TASK -> {
//                    startActivity(getIntent())
//                    finish()
//                    if (data != null) {
//                        val note = data.getStringExtra(EnterNoteActivity.EXTRA_NOTES)
//                        if (TKWeekUtils.save(this, getNameForNotes(), note)) {
//                            notes.setText(note)
//                        }
//                    }
                }
            }
        }
    }

    private fun cancelEventsLoader() {
        if (eventsLoader != null) {
            eventsLoader?.cancel(true)
            eventsLoader = null
        }
    }

    private fun hasGoogleAccount(): Boolean {
        return false // TasksUtils.hasGoogleAccount(requireActivity())
    }

    private fun prepareEventsLoader() {
        cancelEventsLoader()
        eventsLoader = object : AsyncTask<Void, Void, AnnualEventsListAdapter>() {
            override fun onPreExecute() {}
            override fun doInBackground(vararg params: Void): AnnualEventsListAdapter? {
                if (Looper.myLooper() == null) {
                    Looper.prepare()
                }
                val u = TasksUtils.getInstance(handler, requireActivity())
                // update only necessary if there was an error while reading tasks;
                // otherwise updateTasks() will be called from the handler
                tasksNeedUpdate = u != null && !u.getTasksAsync(isShowCompletedTasks())
                return AnnualEventsListAdapter(
                    requireContext(),
                    cal,
                    cal,
                    true,
                    null
                )
            }

            override fun onPostExecute(result: AnnualEventsListAdapter?) {
                eventsLoader = null
                updateEvents(result!!)
                if (tasksNeedUpdate) {
                    updateTasks(null)
                }
            }
        }
        eventsLoader?.execute()
    }

    private fun updateViews() {
        val prefs = PreferenceManager
            .getDefaultSharedPreferences(requireContext())
        val hide = prefs.getBoolean("hide_nameday", false)
        binding.mydayLayoutNameday.visibility = if (hide) View.GONE else View.VISIBLE
        val hideAstrologicalSign = prefs.getBoolean("hide_astrological_sign", false)
        binding.mydayLayoutAstrologicalSign.visibility =
            if (hideAstrologicalSign) View.GONE else View.VISIBLE
        prepareCalendar(cal!!, requireContext(), binding.mydayLabelWeekNumber, true)
        DateUtilities.clearTimeRelatedFields(cal)
        val weeknr = cal!!.get(Calendar.WEEK_OF_YEAR)
        val maxWeekNumber = cal!!.getActualMaximum(Calendar.WEEK_OF_YEAR)
        binding.mydayWeekNumber.text = getString(
            R.string.day_of_year, weeknr,
            maxWeekNumber, maxWeekNumber - weeknr
        )
        val strDate = if (DateUtilities.isToday(cal)) {
            getString(
                R.string.string1_string2,
                TKWeekActivity.FORMAT_FULL.format(cal!!.time),
                getString(R.string.today)
            )
        } else {
            TKWeekActivity.FORMAT_FULL.format(cal!!.time)
        }
        if (isDayOff(requireContext(), cal!!.time)) {
            binding.mydayDate.text = getString(
                R.string.string1_dash_string2, strDate,
                getString(R.string.day_off)
            )
        } else {
            binding.mydayDate.text = strDate
        }
        val date = cal!!.time
        val current = cal!!.get(Calendar.DAY_OF_YEAR)
        val max = cal!!.getActualMaximum(Calendar.DAY_OF_YEAR)
        binding.mydayDayInYear.text = getString(
            R.string.day_of_year, current, max, max
                    - current
        )
        binding.mydayIsLeapYear.text = if (DateUtilities.isSchaltjahr(cal!!.get(Calendar.YEAR)))
            getString(R.string.yes)
        else getString(
            R.string.no
        )
        binding.mydayAstrologicalSign.text = Zodiac.getSign(requireContext(), date)
        binding.mydayNameday.text = Namenstage.getNameDays(requireContext(), date)
        prepareEventsLoader()
        updateNotes()
    }

    private fun updateTasks(list: List<Task>?) {
        val inflater = layoutInflater
        binding.mydayTasks.removeAllViews()
        if (list != null) {
            for (position in list.indices) {
                val t = list[position]
                val parent: View =
                    inflater.inflate(R.layout.two_line_item, binding.mydayTasks, false)
                if (position > 0) {
                    val divider = parent.findViewById<View>(R.id.divider)
                    divider.visibility = View.VISIBLE
                }
                val cb = parent.findViewById<CheckBox>(R.id.checkbox)
                cb.tag = t
                cb.visibility = View.VISIBLE
                cb.isChecked = t.completed
                cb.setOnCheckedChangeListener(this)
                val text1 = parent.findViewById<TextView>(R.id.text1)
                var title = TKWeekUtils.getStringNotNull(t.title)
                if (title.isEmpty()) {
                    title = getString(R.string.no_title)
                }
                text1.text = title
                val text2 = parent.findViewById<TextView>(R.id.text2)
                val notes = TKWeekUtils.getStringNotNull(t.notes)
                if (notes.isNotEmpty()) {
                    text2.visibility = View.VISIBLE
                    text2.text = notes
                } else {
                    text2.visibility = View.GONE
                }
                val dateDue = t.due
                val text3 = parent.findViewById<TextView>(R.id.text3)
                if (dateDue != null) {
                    val today = Calendar.getInstance()
                    val date = DateUtilities.getCalendar(dateDue)
                    text3.visibility = View.VISIBLE
                    text3.text = AnnualEventsListAdapter.getDaysAsString(
                        inflater, today, date
                    )
                } else {
                    text3.visibility = View.GONE
                }
                val text4 = parent.findViewById<TextView>(R.id.text4)
                text4.text = TKWeekUtils.getStringNotNull(t.listTitle)
                text4.visibility = View.VISIBLE
                binding.mydayTasks.addView(parent)
            }
        }
        if (hasGoogleAccount()) {
            maybeAddNone(inflater, binding.mydayTasks)
            binding.mydayLabelTasks.visibility = View.VISIBLE
            binding.mydayTasks.visibility = View.VISIBLE
        } else {
            binding.mydayLabelTasks.visibility = View.GONE
            binding.mydayTasks.visibility = View.GONE
        }
    }

    private fun updateEvents(adapter: AnnualEventsListAdapter) {
        layoutInflater.run {
            binding.mydayEvents.removeAllViews()
            if (shouldShowBirthdays() && !TKWeekUtils.canReadContacts(requireContext())
                && shouldShowPermissionReadContactsRationale()
            ) {
                val layout =
                    inflate(
                        R.layout.message_link_to_settings,
                        binding.mydayEvents,
                        false
                    ) as RelativeLayout
                linkToSettings(layout, requireActivity(), R.string.str_permission_read_contacts)
                val button = layout.findViewById<Button>(R.id.button)
                button.setOnClickListener {
                    requestReadContacts()
                }
                binding.mydayEvents.addView(layout)
            }
            for (position in 0 until adapter.count) {
                val event = adapter.getItem(position) as Event
                val temp = DateUtilities.getCalendar(
                    event.year,
                    event.month,
                    event.day
                )
                if (event.annuallyRepeating) {
                    temp[Calendar.YEAR] = cal!!.get(Calendar.YEAR)
                }
                if (DateUtilities.diffDayPeriods(cal, temp) == 0L) {
                    val descr = adapter.getDescription(event, requireContext())
                    val parent =
                        inflate(R.layout.string_one_line2, binding.mydayEvents, false)
                    binding.mydayEvents.addView(parent)
                    val str = parent
                        .findViewById<TextView>(R.id.string_one_line2_text)
                    str.text = descr
                    val color = parent.findViewById<View>(R.id.string_one_line2_color)
                    color.setBackgroundColor(event.color)
                }
            }
            maybeAddNone(this, binding.mydayEvents)
            updateAppointments(this)
        }
    }

    private fun updateAppointments(inflater: LayoutInflater) {
        binding.mydayAppointments.removeAllViews()
        val show = shouldShowAppointments()
        var showIfNoPermission = true
        if (show && !TKWeekUtils.canReadCalendar(requireContext())
            && shouldShowPermissionReadCalendarRationale()
        ) {
            val layout =
                inflater.inflate(
                    R.layout.message_link_to_settings,
                    binding.mydayAppointments,
                    false
                ) as RelativeLayout
            linkToSettings(layout, requireActivity(), R.string.str_permission_read_calendar)
            val button = layout.findViewById<Button>(R.id.button)
            button.setOnClickListener {
                requestReadCalendar()
            }
            binding.mydayAppointments.addView(layout)
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
                        binding.mydayAppointments,
                        false
                    )
                binding.mydayAppointments.addView(parent)
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
                    TKWeekActivity.FORMAT_DATE_TIME_SHORT
                } else {
                    TKWeekActivity.FORMAT_TIME_SHORT
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
            maybeAddNone(inflater, binding.mydayAppointments)
        } else if (showIfNoPermission) {
            addNoPermission(inflater, binding.mydayAppointments)
        }
        binding.mydayLabelAppointments.visibility = if (show) View.VISIBLE else View.GONE
        binding.mydayAppointments.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun appendTime(minutes: Int, sb: StringBuilder): Int {
        var mins = minutes
        if (sb.isNotEmpty()) {
            sb.append(", ")
        } else {
            sb.append("\n")
        }
        when {
            mins >= TKWeekActivity.MINUTES_PER_DAY -> {
                val days = mins / TKWeekActivity.MINUTES_PER_DAY
                sb.append(days)
                sb.append(" ")
                sb.append(this.getString(if (days == 1) R.string.day else R.string.days))
                mins %= TKWeekActivity.MINUTES_PER_DAY
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
        var pattern = "http://en.m.wikipedia.org/wiki/{0}_{1}"
        var loc = Locale.ENGLISH
        try {
            val l = Locale.getDefault()
            if (Locale.GERMAN.language == l.language) {
                pattern = "http://de.m.wikipedia.org/wiki/{1}._{0}#_"
                loc = l
            }
        } catch (t: Throwable) {
            Log.e(TAG, "lookUpInWikipedia()", t)
        }
        val df = SimpleDateFormat("MMMM", loc)
        val month = df.format(cal!!.time)
        val day = cal!!.get(Calendar.DAY_OF_MONTH)
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

    private fun isShowCompletedTasks(): Boolean {
        val prefs = requireContext().getSharedPreferences(TAG, Context.MODE_PRIVATE)
        return prefs.getBoolean(SHOW_COMPLETED_TASKS, false)
    }

    private fun toggleShowCompletedTasks() {
        val prefs = requireContext().getSharedPreferences(TAG, Context.MODE_PRIVATE)
        val toggle = !prefs.getBoolean(SHOW_COMPLETED_TASKS, false)
        val e = prefs.edit()
        e.putBoolean(SHOW_COMPLETED_TASKS, toggle)
        e.apply()
    }

    private fun updateNotes() {
        val note = TKWeekUtils.load(requireContext(), getNameForNotes())
        updateNoteAndDeleteButton(note)
    }

    private fun getNameForNotes(): String {
        return "Note_" + TKWeekActivity.FORMAT_YYYYMMDD.format(cal!!.time)
    }

    private class UpdateTasksHandler(val a: MyDayFragment) : Handler() {
        override fun handleMessage(msg: Message) {
            val r =
                Runnable {
                    a.updateTasks(msg.obj as List<Task>)
                }
            a.requireActivity().runOnUiThread(r)
        }
    }

    private class TasksCompletedHandler(val a: MyDayFragment) : Handler() {
        override fun handleMessage(msg: Message) {
            val o = msg.obj
            val r = Runnable {
                if (!a.isShowCompletedTasks()) {
                    if (o is View) {
                        var v: View? = o
                        val first = v == a.binding.mydayTasks.getChildAt(0)
                        val cb = v!!.findViewById<CheckBox>(R.id.checkbox)
                        if (cb.isChecked) {
                            Toast.makeText(
                                a.requireContext(),
                                R.string.toast_task_completed,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        a.binding.mydayTasks.removeView(o)
                        if (first) {
                            v = a.binding.mydayTasks.getChildAt(0)
                            if (v != null) {
                                val divider = v.findViewById<View>(R.id.divider)
                                divider.visibility = View.GONE
                            }
                        }
                    }
                }
            }
            a.requireActivity().runOnUiThread(r)
        }
    }

    private fun saveNoteAndUpdateUI(note: String) {
        if (TKWeekUtils.save(requireContext(), getNameForNotes(), note)) {
            updateNoteAndDeleteButton(note)
        }
    }

    private fun updateNoteAndDeleteButton(note: String) {
        binding.mydayNotes.text = note
        binding.mydaySymbolDelete.isEnabled = note.isNotEmpty()
    }
}