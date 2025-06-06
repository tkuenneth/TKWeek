/*
 * AnnualEventsFragment.kt
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
@file:Suppress("DEPRECATION")

package com.thomaskuenneth.tkweek.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.ContactsContract
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.thomaskuenneth.tkweek.AlarmReceiver
import com.thomaskuenneth.tkweek.R
import com.thomaskuenneth.tkweek.activity.TKWeekActivity
import com.thomaskuenneth.tkweek.adapter.AnnualEventsListAdapter
import com.thomaskuenneth.tkweek.adapter.AnnualEventsListAdapter.getUserEventsFile
import com.thomaskuenneth.tkweek.databinding.EventsBinding
import com.thomaskuenneth.tkweek.types.Event
import com.thomaskuenneth.tkweek.types.IContactId
import com.thomaskuenneth.tkweek.util.DateUtilities
import com.thomaskuenneth.tkweek.util.TKWeekUtils
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileWriter
import java.util.Date

private const val TAG = "AnnualEventsFragment"

private val MENU_DELETE = R.string.menu_delete
private val MENU_DAYS_BETWEEN_DATES = R.string.days_between_dates_activity_text1
private val MENU_MARK_AS_DAY_OFF = R.string.mark_as_day_off
private val MENU_REMOVE_DAY_OFF_TAG = R.string.remove_day_off_tag

@Suppress("DEPRECATION")
class AnnualEventsFragment : TKWeekBaseFragment<EventsBinding>(), AdapterView.OnItemClickListener {

    private val binding get() = backing!!

    private var eventsLoader: AsyncTask<Void, Void, AnnualEventsListAdapter>? = null

    private var listAdapter: AnnualEventsListAdapter? = null

    private var searchString: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parentFragmentManager.setFragmentResultListener(
            REQUEST_KEY_BACKUP_RESTORE_FRAGMENT, this
        ) { _, bundle ->
            when (bundle.getInt(BACKUP_RESTORE)) {
                BACKUP -> backup()
                RESTORE -> restore()
            }
        }
        parentFragmentManager.setFragmentResultListener(
            REQUEST_KEY_NEW_EVENT_FRAGMENT, this
        ) { _, bundle ->
            val descr = bundle.getString(DESCR, "")
            val annuallyRepeating = bundle.getBoolean(ANNUALLY_REPEATING, false)
            val date = bundle.getSerializable(DATE)
            val event = Event(descr, date as Date, annuallyRepeating)
            listAdapter?.addEventNoCheck(event)
            listAdapter?.save(requireContext())
            setListAdapterLoadEvents(false, searchString)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        backing = EventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("InflateParams")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        eventsLoader = null
        binding.listView.onItemClickListener = this
        binding.listView.setOnCreateContextMenuListener(this)
        val permissions = ArrayList<String>()
        binding.messageNotifications.message.setText(R.string.str_permission_post_notifications)
        binding.messageNotifications.button.setOnClickListener {
            requestPostNotifications()
        }
        if (Build.VERSION.SDK_INT >= 33 && !TKWeekUtils.canPostNotifications(requireContext()) && !shouldShowPermissionPostNotificationsRationale()) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (shouldShowBirthdays() && !TKWeekUtils.canReadContacts(requireContext()) && !shouldShowPermissionReadContactsRationale()) {
            permissions.add(Manifest.permission.READ_CONTACTS)
        }
        TKWeekUtils.linkToSettings(
            binding.messageLinkToSettingsContacts.root,
            requireActivity(),
            R.string.str_permission_read_contacts
        )
        binding.messageLinkToSettingsContacts.button.setOnClickListener {
            requestReadContacts()
        }
        if (shouldShowAllDayEvents() && !TKWeekUtils.canReadCalendar(requireContext()) && !shouldShowPermissionReadCalendarRationale()) {
            permissions.add(Manifest.permission.READ_CALENDAR)
        }
        TKWeekUtils.linkToSettings(
            binding.messageLinkToSettingsCalendar.root,
            requireActivity(),
            R.string.str_permission_read_calendar2
        )
        binding.messageLinkToSettingsCalendar.button.setOnClickListener {
            requestReadCalendar()
        }
        if (permissions.isNotEmpty()) {
            val l = arrayOfNulls<String>(permissions.size)
            permissions.toArray(l)
            requestPermissions(l, 0)
        }
        updateAll()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            var success = false
            when (requestCode) {
                BACKUP -> {
                    data?.data?.also { uri ->
                        requireContext().run {
                            contentResolver.openFileDescriptor(uri, "w")?.use {
                                success = listAdapter?.saveUserEvents(FileWriter(it.fileDescriptor))
                                    ?: false
                            }
                        }
                    }
                }

                RESTORE -> {
                    data?.data?.also { uri ->
                        requireContext().contentResolver.openFileDescriptor(uri, "r")?.use {
                            FileInputStream(it.fileDescriptor).use { fis ->
                                FileOutputStream(getUserEventsFile(requireContext())).use { fos ->
                                    var current: Int
                                    while (true) {
                                        current = fis.read()
                                        if (current == -1) {
                                            break
                                        }
                                        fos.write(current)
                                    }
                                    success = true
                                }
                            }
                        }
                        setListAdapterLoadEvents(true, searchString)
                    }
                }
            }
            if (!success) showError()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if ((grantResults.isNotEmpty()) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            if (requestCode == RQ_READ_CONTACTS) {
                setListAdapterLoadEvents(false, searchString)
            }
        }
        updatePermissionInfo()
    }

    override fun onStart() {
        super.onStart()
        if ((requireActivity().intent?.getIntExtra(
                AlarmReceiver.KEY_CANCEL_NOTIFICATION, -1
            ) ?: -1) != -1
        ) {
            requireContext().getSystemService(NotificationManager::class.java)?.run {
                if (areNotificationsEnabled()) {
                    cancel(id)
                }
            }
        }
    }

    override fun onDestroy() {
        eventsLoader?.run {
            cancel(true)
        }
        super.onDestroy()
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_search, menu)
        val searchMenuItem = menu.findItem(R.id.search)
        (searchMenuItem.actionView as SearchView?)?.run {
            queryHint = requireContext().getString(R.string.search_hint)
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    searchString = query
                    searchMenuItem.collapseActionView()
                    updateListAndOptionsMenu()
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    return true
                }
            })
        }
        inflater.inflate(R.menu.menu_annual_events_activity, menu)
    }

    override fun onCreateContextMenu(
        menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val mi = menuInfo as AdapterView.AdapterContextMenuInfo
        val item = listAdapter?.getItem(mi.position) as Event
        val date = DateUtilities.getCalendar(item).time
        menu.setHeaderTitle(item.descr)
        if (!item.builtin) {
            menu.add(Menu.NONE, MENU_DELETE, Menu.NONE, MENU_DELETE)
        }
        menu.add(
            Menu.NONE, MENU_DAYS_BETWEEN_DATES, Menu.NONE, MENU_DAYS_BETWEEN_DATES
        )
        if (CalendarFragment.isDayOff(requireContext(), date)) {
            menu.add(Menu.NONE, MENU_REMOVE_DAY_OFF_TAG, Menu.NONE, MENU_REMOVE_DAY_OFF_TAG)
        } else {
            menu.add(Menu.NONE, MENU_MARK_AS_DAY_OFF, Menu.NONE, MENU_MARK_AS_DAY_OFF)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.clear)?.run {
            isVisible = searchString?.isNotEmpty() == true
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.clear -> {
                searchString = null
                updateListAndOptionsMenu()
                true
            }

            R.id.annual_event_backup_restore -> {
                showDialog(BackupRestoreDialogFragment())
                true
            }

            R.id.new_event -> {
                val f = NewEventFragment()
                showDialog(f)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val mi = item.menuInfo as AdapterView.AdapterContextMenuInfo
        val event = listAdapter?.getItem(mi.position) as Event
        val date = DateUtilities.getCalendar(event).time
        return when (item.itemId) {
            MENU_DELETE -> {
                val adapter = listAdapter as AnnualEventsListAdapter
                adapter.deleteSimilar(event)
                adapter.save(requireContext())
                true
            }

            MENU_DAYS_BETWEEN_DATES -> {
                val payload = Bundle()
                payload.putLong(DATE, date.time)
                launchModule(DaysBetweenDatesFragment::class.java, payload)
                true
            }

            MENU_MARK_AS_DAY_OFF -> {
                CalendarFragment.setDayOff(requireContext(), date, true)
                updateItemAtPosition(mi.position)
                true
            }

            MENU_REMOVE_DAY_OFF_TAG -> {
                CalendarFragment.setDayOff(requireContext(), date, false)
                updateItemAtPosition(mi.position)
                true
            }

            else -> super.onContextItemSelected(item)
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val o = listAdapter?.getItem(position)
        if (o as? IContactId? != null && o.contactId != null) {
            val intent = Intent(
                Intent.ACTION_VIEW, Uri.withAppendedPath(
                    ContactsContract.Contacts.CONTENT_URI, o.contactId
                )
            )
            startActivity(intent)
        } else if (o as? Event? != null) {
            val payload = Bundle()
            payload.putLong(DATE, DateUtilities.getCalendar(o).time.time)
            launchModule(MyDayFragment::class.java, payload)
        }
    }

    override fun preferencesFinished(resultCode: Int, data: Intent?) {
        updateAll()
    }

    private fun setListAdapterLoadEvents(
        restore: Boolean, search: String?
    ) {
        binding.indicator.visibility = View.VISIBLE
        eventsLoader = @SuppressLint("StaticFieldLeak") object :
            AsyncTask<Void, Void, AnnualEventsListAdapter>() {
            @Deprecated("Deprecated in Java")
            override fun doInBackground(vararg params: Void): AnnualEventsListAdapter {
                if (Looper.myLooper() == null) {
                    Looper.prepare()
                }
                return AnnualEventsListAdapter.create(
                    requireContext(), search
                )
            }

            @Deprecated("Deprecated in Java")
            override fun onPostExecute(result: AnnualEventsListAdapter) {
                eventsLoader = null
                binding.listView.adapter = result.also { listAdapter = it }
                if (listAdapter != null && restore) {
                    listAdapter?.save(requireContext())
                }
                listAdapter?.updateEventsListWidgets(requireContext())
                binding.header.text = getString(
                    R.string.string1_dash_string2,
                    TKWeekActivity.FORMAT_DEFAULT.format(listAdapter?.from?.time ?: Date()),
                    TKWeekActivity.FORMAT_DEFAULT.format(listAdapter?.to?.time ?: Date())
                )
                binding.indicator.visibility = View.GONE
            }
        }
        eventsLoader?.execute()
    }

    fun isHoliday(context: Context, event: Event): Boolean {
        val prefs = context.getSharedPreferences(
            TAG, Context.MODE_PRIVATE
        )
        return prefs.getBoolean(getPreferencesKey(event), false)
    }

    // FIXME: used to be used to mark a day off; maybe reimplement click handler
    fun setHoliday(context: Context, event: Event, holiday: Boolean) {
        val prefs = context.getSharedPreferences(
            TAG, Context.MODE_PRIVATE
        )
        val e = prefs.edit()
        e.putBoolean(getPreferencesKey(event), holiday)
        e.apply()
    }

    private fun restore() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
        }
        startActivityForResult(intent, RESTORE)
    }

    private fun backup() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TITLE,
                "AnnualEvents_${TKWeekActivity.FORMAT_YYYYMMDD.format(Date())}.txt"
            )
        }
        startActivityForResult(intent, BACKUP)
    }

    private fun updateAll() {
        setListAdapterLoadEvents(false, searchString)
        updatePermissionInfo()
    }

    private fun updateItemAtPosition(position: Int) {
        val mListView = binding.listView
        val visiblePosition = mListView.firstVisiblePosition
        val view = mListView.getChildAt(position - visiblePosition)
        if (view != null) {
            mListView.adapter.getView(position, view, mListView)
        }
    }

    private fun getPreferencesKey(event: Event): String {
        val description = TKWeekUtils.getStringNotNull(event.descr)
        return "holiday_$description"
    }

    private fun updatePermissionInfo() {
        binding.messageLinkToSettingsContacts.root.visibility =
            if (shouldShowBirthdays() && shouldShowPermissionReadContactsRationale()) View.VISIBLE else View.GONE
        binding.messageLinkToSettingsCalendar.root.visibility =
            if (shouldShowAllDayEvents() && shouldShowPermissionReadCalendarRationale()) View.VISIBLE else View.GONE
        binding.messageNotifications.root.visibility =
            if (shouldShowPermissionPostNotificationsRationale()) View.VISIBLE else View.GONE
    }

    private fun updateListAndOptionsMenu() {
        setListAdapterLoadEvents(false, searchString)
        (requireActivity() as AppCompatActivity).run {
            invalidateOptionsMenu()
            supportActionBar?.title = if (searchString?.isNotEmpty() == true) getString(
                R.string.string1_string2,
                getString(R.string.annual_events_activity_text1),
                getString(R.string.filtered)
            ) else getString(R.string.annual_events_activity_text1)
        }
    }

    private fun showError() {
        val message = getString(
            R.string.not_successful, getString(R.string.annual_event_backup_restore)
        )
        val fragment = MessageFragment().also {
            it.arguments = Bundle().also { bundle ->
                bundle.putString(ARGS_TITLE, getString(R.string.dialog_title_error))
                bundle.putString(ARGS_MESSAGE, message)
            }
        }
        fragment.show(parentFragmentManager, MessageFragment.TAG)
    }
}