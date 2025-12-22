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
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.content.edit
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.search.SearchView
import com.thomaskuenneth.tkweek.AlarmReceiver
import com.thomaskuenneth.tkweek.R
import com.thomaskuenneth.tkweek.adapter.AnnualEventsListAdapter
import com.thomaskuenneth.tkweek.adapter.AnnualEventsListAdapter.getUserEventsFile
import com.thomaskuenneth.tkweek.databinding.EventsBinding
import com.thomaskuenneth.tkweek.types.Event
import com.thomaskuenneth.tkweek.types.IContactId
import com.thomaskuenneth.tkweek.util.DateUtilities
import com.thomaskuenneth.tkweek.util.Helper
import com.thomaskuenneth.tkweek.util.Helper.DATE
import com.thomaskuenneth.tkweek.util.TKWeekUtils
import com.thomaskuenneth.tkweek.viewmodel.AnnualEventsViewModel
import com.thomaskuenneth.tkweek.viewmodel.AppBarAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileWriter
import java.util.Date

private const val TAG = "AnnualEventsFragment"

private val MENU_DELETE = R.string.menu_delete
private val MENU_DAYS_BETWEEN_DATES = R.string.days_between_dates_activity_text1
private val MENU_MARK_AS_DAY_OFF = R.string.mark_as_day_off
private val MENU_REMOVE_DAY_OFF_TAG = R.string.remove_day_off_tag
private val MENU_MARK_AS_HOLIDAY = R.string.mark_as_holiday
private val MENU_REMOVE_HOLIDAY_TAG = R.string.remove_holiday_tag

@Suppress("DEPRECATION")
class AnnualEventsFragment : TKWeekBaseFragment<EventsBinding>(), AdapterView.OnItemClickListener {

    private val binding get() = backing!!
    private val annualEventsViewModel: AnnualEventsViewModel by activityViewModels()

    private var loadEventsJob: Job? = null

    private var listAdapter: AnnualEventsListAdapter? = null

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
            val description = bundle.getString(DESCR, "")
            val annuallyRepeating = bundle.getBoolean(ANNUALLY_REPEATING, false)
            val date = bundle.getSerializable(DATE)
            val event = Event(description, date as Date, annuallyRepeating)
            lifecycleScope.launch {
                val context = requireContext()
                val adapter = listAdapter ?: withContext(Dispatchers.IO) {
                    AnnualEventsListAdapter.create(context, null)
                }
                adapter.addEventNoCheck(event)
                withContext(Dispatchers.IO) {
                    adapter.save(context)
                }
                setListAdapterLoadEvents(false, annualEventsViewModel.searchQuery.value)
            }
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
        super.onViewCreated(view, savedInstanceState)
        loadEventsJob = null
        binding.listView.onItemClickListener = this
        binding.listView.setOnCreateContextMenuListener(this)
        binding.searchView.setupWithSearchBar(binding.searchBar)
        val lifecycleOwner = viewLifecycleOwner
        binding.searchView
            .editText
            .setOnEditorActionListener { _, _, _ ->
                if (lifecycleOwner.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)) {
                    annualEventsViewModel.setSearchQuery(binding.searchView.text.toString())
                }
                false
            }
        binding.searchView.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (lifecycleOwner.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)) {
                    annualEventsViewModel.setSearchQuery(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        binding.searchView.addTransitionListener { _, _, newState ->
            if (lifecycleOwner.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)) {
                annualEventsViewModel.setSearchOpen(newState == SearchView.TransitionState.SHOWING || newState == SearchView.TransitionState.SHOWN)
            }
        }
        binding.searchListView.onItemClickListener = this
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
            requestMultiplePermissions(l.requireNoNulls())
        }
        updateAll()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                combine(
                    annualEventsViewModel.isSearchOpen,
                    annualEventsViewModel.searchQuery
                ) { isOpen, query ->
                    isOpen to query
                }
                    .onEach { (isOpen, query) ->
                        if (isOpen) {
                            binding.searchView.show()
                        } else {
                            binding.searchView.hide()
                        }
                        val currentText = binding.searchView.text.toString()
                        if (currentText != query) {
                            binding.searchView.setText(query)
                            if (query != null) {
                                binding.searchView.editText.setSelection(query.length)
                            }
                        }
                        setListAdapterLoadEvents(false, if (isOpen) query else null, isOpen)
                    }.launchIn(this)
            }
        }
    }

    override fun onReadContactsPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            setListAdapterLoadEvents(false, annualEventsViewModel.searchQuery.value)
        }
        updatePermissionInfo()
    }

    override fun onPostNotificationsPermissionResult(isGranted: Boolean) {
        updatePermissionInfo()
    }

    override fun onReadCalendarPermissionResult(isGranted: Boolean) {
        updatePermissionInfo()
    }

    override fun onMultiplePermissionsResult(results: Map<String, Boolean>) {
        if (results[Manifest.permission.READ_CONTACTS] == true) {
            setListAdapterLoadEvents(false, annualEventsViewModel.searchQuery.value)
        }
        updatePermissionInfo()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            lifecycleScope.launch {
                var success = false
                val context = requireContext()
                when (requestCode) {
                    BACKUP -> {
                        data?.data?.also { uri ->
                            val adapter = listAdapter ?: withContext(Dispatchers.IO) {
                                AnnualEventsListAdapter.create(context, null)
                            }
                            success = withContext(Dispatchers.IO) {
                                try {
                                    context.contentResolver.openFileDescriptor(uri, "w")?.use {
                                        adapter.saveUserEvents(FileWriter(it.fileDescriptor))
                                    } ?: false
                                } catch (_: Exception) {
                                    false
                                }
                            }
                        }
                    }

                    RESTORE -> {
                        data?.data?.also { uri ->
                            success = withContext(Dispatchers.IO) {
                                try {
                                    context.contentResolver.openFileDescriptor(uri, "r")?.use {
                                        FileInputStream(it.fileDescriptor).use { fis ->
                                            FileOutputStream(getUserEventsFile(context)).use { fos ->
                                                var current: Int
                                                while (true) {
                                                    current = fis.read()
                                                    if (current == -1) {
                                                        break
                                                    }
                                                    fos.write(current)
                                                }
                                            }
                                        }
                                    }
                                    true
                                } catch (_: Exception) {
                                    false
                                }
                            }
                            setListAdapterLoadEvents(true, annualEventsViewModel.searchQuery.value)
                        }
                    }
                }
                if (!success) showError()
            }
        }
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
        if (isHoliday(requireContext(), item)) {
            menu.add(Menu.NONE, MENU_REMOVE_HOLIDAY_TAG, Menu.NONE, MENU_REMOVE_HOLIDAY_TAG)
        } else {
            menu.add(Menu.NONE, MENU_MARK_AS_HOLIDAY, Menu.NONE, MENU_MARK_AS_HOLIDAY)
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
                selectModule(DaysBetweenDatesFragment::class.java, payload)
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

            MENU_MARK_AS_HOLIDAY -> {
                setHoliday(requireContext(), event, true)
                updateItemAtPosition(mi.position)
                true
            }

            MENU_REMOVE_HOLIDAY_TAG -> {
                setHoliday(requireContext(), event, false)
                updateItemAtPosition(mi.position)
                true
            }

            else -> super.onContextItemSelected(item)
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val adapter = if (parent == binding.searchListView) {
            binding.searchListView.adapter as AnnualEventsListAdapter
        } else {
            listAdapter
        }
        val o = adapter?.getItem(position)
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
            selectModule(MyDayFragment::class.java, payload)
        }
    }

    override fun updateAppBarActions() {
        val actions = listOf(
            AppBarAction(
                icon = R.drawable.ic_baseline_add_24,
                contentDescription = R.string.new_event,
                title = R.string.new_event,
                onClick = {
                    showDialog(NewEventFragment())
                }
            ),
            AppBarAction(
                icon = R.drawable.ic_baseline_backup_24,
                contentDescription = R.string.annual_event_backup_restore,
                title = R.string.annual_event_backup_restore,
                onClick = {
                    showDialog(BackupRestoreDialogFragment())
                }
            )
        )
        viewModel.setAppBarActions(actions)
    }

    private fun setListAdapterLoadEvents(
        restore: Boolean, search: String?, isSearch: Boolean = false
    ) {
        loadEventsJob?.cancel()
        loadEventsJob = viewLifecycleOwner.lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                AnnualEventsListAdapter.create(
                    requireContext(), search
                )
            }
            if (backing != null) {
                if (isSearch) {
                    binding.searchListView.adapter = result
                } else {
                    binding.listView.adapter = result.also { listAdapter = it }
                    if (listAdapter != null && restore) {
                        listAdapter?.save(requireContext())
                    }
                    listAdapter?.updateEventsListWidgets(requireContext())
                    binding.header.text = getString(
                        R.string.string1_dash_string2,
                        Helper.FORMAT_DEFAULT.format(listAdapter?.from?.time ?: Date()),
                        Helper.FORMAT_DEFAULT.format(listAdapter?.to?.time ?: Date())
                    )
                }
            }
        }
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
                "AnnualEvents_${Helper.FORMAT_YYYYMMDD.format(Date())}.txt"
            )
        }
        startActivityForResult(intent, BACKUP)
    }

    private fun updateAll() {
        setListAdapterLoadEvents(false, annualEventsViewModel.searchQuery.value)
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

    private fun updatePermissionInfo() {
        binding.messageLinkToSettingsContacts.root.visibility =
            if (shouldShowBirthdays() && shouldShowPermissionReadContactsRationale()) View.VISIBLE else View.GONE
        binding.messageLinkToSettingsCalendar.root.visibility =
            if (shouldShowAllDayEvents() && shouldShowPermissionReadCalendarRationale()) View.VISIBLE else View.GONE
        binding.messageNotifications.root.visibility =
            if (shouldShowPermissionPostNotificationsRationale()) View.VISIBLE else View.GONE
    }

//    private fun updateListAndOptionsMenu() {
//        setListAdapterLoadEvents(false, searchString)
//        updateAppBarActions()
//    }

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

    companion object {
        @JvmStatic
        fun isHoliday(context: Context, event: Event): Boolean {
            val prefs = context.getSharedPreferences(
                TAG, Context.MODE_PRIVATE
            )
            return prefs.getBoolean(getPreferencesKey(event), false)
        }

        @JvmStatic
        fun setHoliday(context: Context, event: Event, holiday: Boolean) {
            val prefs = context.getSharedPreferences(
                TAG, Context.MODE_PRIVATE
            )
            prefs.edit {
                putBoolean(getPreferencesKey(event), holiday)
            }
        }

        private fun getPreferencesKey(event: Event): String {
            val description = TKWeekUtils.getStringNotNull(event.descr)
            return "holiday_$description"
        }
    }
}
