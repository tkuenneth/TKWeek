/*
 * TKWeekBaseFragment.kt
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
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceManager
import com.thomaskuenneth.tkweek.TKWeekModule
import com.thomaskuenneth.tkweek.util.TKWeekUtils
import com.thomaskuenneth.tkweek.viewmodel.TKWeekViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
abstract class TKWeekHiltBaseFragment : Fragment() {
    protected val viewModel: TKWeekViewModel by activityViewModels()
}

abstract class TKWeekBaseFragment<T> : TKWeekHiltBaseFragment() {

    protected var backing: T? = null

    private val requestReadContactsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            onReadContactsPermissionResult(isGranted)
        }

    private val requestPostNotificationsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            onPostNotificationsPermissionResult(isGranted)
        }

    private val requestReadCalendarLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            onReadCalendarPermissionResult(isGranted)
        }

    private val requestReadCallLogLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            onReadCallLogPermissionResult(isGranted)
        }

    private val requestMultiplePermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            onMultiplePermissionsResult(results)
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findScrollableContent(view)?.let { scrollable ->
            scrollable.setOnScrollChangeListener(
                NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
                    viewModel.setDetailScrolled(scrollY > 0)
                })
            viewModel.setDetailScrolled(scrollable.scrollY > 0)
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        updateAppBarActions()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.setDetailScrolled(false)
        backing = null
    }

    open fun updateAppBarActions() {
        viewModel.setAppBarActions(emptyList())
    }

    fun selectModule(module: Class<*>, payload: Bundle?) {
        TKWeekModule.find(module)?.let {
            viewModel.selectModuleWithArguments(module = it, arguments = payload, topLevel = false)
        }
    }

    fun showDialog(fragment: DialogFragment) {
        val ft = parentFragmentManager.beginTransaction()
        val prev = parentFragmentManager.findFragmentByTag("dialog")
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)
        fragment.show(ft, "dialog")
    }

    fun shouldShowBirthdays() =
        !PreferenceManager.getDefaultSharedPreferences(requireContext())
            .getBoolean("hide_birthdays", false)

    fun shouldShowPermissionReadContactsRationale() =
        TKWeekUtils.shouldShowRequestPermissionRationale(
            requireActivity(),
            Manifest.permission.READ_CONTACTS
        )

    fun shouldShowPermissionPostNotificationsRationale(): Boolean {
        if (Build.VERSION.SDK_INT >= 33) {
            return TKWeekUtils.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
        return false;
    }

    fun requestReadContacts() {
        requestReadContactsLauncher.launch(Manifest.permission.READ_CONTACTS)
    }

    open fun onReadContactsPermissionResult(isGranted: Boolean) {
    }

    fun requestPostNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPostNotificationsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    open fun onPostNotificationsPermissionResult(isGranted: Boolean) {
    }

    fun shouldShowAppointments() = !PreferenceManager.getDefaultSharedPreferences(requireContext())
        .getBoolean("hide_appointments", false)

    fun shouldShowAllDayEvents() = !PreferenceManager.getDefaultSharedPreferences(requireContext())
        .getBoolean("hide_allday_events", false)

    fun shouldShowPermissionReadCalendarRationale() =
        TKWeekUtils.shouldShowRequestPermissionRationale(
            requireActivity(),
            Manifest.permission.READ_CALENDAR
        )

    fun requestReadCalendar() {
        requestReadCalendarLauncher.launch(Manifest.permission.READ_CALENDAR)
    }

    open fun onReadCalendarPermissionResult(isGranted: Boolean) {
    }

    fun shouldShowPermissionReadCallLogRationale() =
        TKWeekUtils.shouldShowRequestPermissionRationale(
            requireActivity(),
            Manifest.permission.READ_CALL_LOG
        )

    fun requestReadCallLog() {
        requestReadCallLogLauncher.launch(Manifest.permission.READ_CALL_LOG)
    }

    open fun onReadCallLogPermissionResult(isGranted: Boolean) {
    }

    fun requestMultiplePermissions(permissions: Array<String>) {
        requestMultiplePermissionsLauncher.launch(permissions)
    }

    open fun onMultiplePermissionsResult(results: Map<String, Boolean>) {
    }

    private fun findScrollableContent(view: View): NestedScrollView? {
        if (view is NestedScrollView) {
            return view
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val result = findScrollableContent(view.getChildAt(i))
                if (result != null) {
                    return result
                }
            }
        }
        return null
    }
}
