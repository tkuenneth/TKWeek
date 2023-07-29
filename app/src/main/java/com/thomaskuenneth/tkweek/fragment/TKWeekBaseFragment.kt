/*
 * TKWeekBaseFragment.kt
 *
 * Copyright 2021 MATHEMA GmbH
 *           2022 - 2023 Thomas Künneth
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
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.preference.PreferenceManager
import com.thomaskuenneth.tkweek.ActivityDescription
import com.thomaskuenneth.tkweek.R
import com.thomaskuenneth.tkweek.activity.ModuleContainerActivity
import com.thomaskuenneth.tkweek.adapter.TKWeekFragmentListAdapter
import com.thomaskuenneth.tkweek.util.TKWeekUtils

const val RQ_READ_CONTACTS = 0
const val RQ_READ_CALENDAR = 1
const val RQ_POST_NOTIFICATIONS = 2

abstract class TKWeekBaseFragment<T> : Fragment() {

    protected var backing: T? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null)
            setHasOptionsMenu(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        backing = null
    }

    open fun preferencesFinished(resultCode: Int, data: Intent?) {
        // intentionally does nothing
    }

    fun launchModule(module: Class<*>, payload: Bundle?) {
        TKWeekFragmentListAdapter.find(module)?.let {
            launchModule(it, payload)
        }
    }

    fun launchModule(module: ActivityDescription, payload: Bundle?) {
        if (isTwoColumnMode(requireActivity())) {
            (parentFragmentManager.findFragmentByTag(getString(R.string.tag_module_selection)) as? TKWeekFragment)?.run {
                val fragment = module.fragment.newInstance()
                fragment.arguments = payload
                parentFragmentManager.run {
                    beginTransaction()
                        .replace(
                            R.id.module_container,
                            fragment,
                            getString(R.string.tag_module_fragment)
                        )
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .disallowAddToBackStack()
                        .commit()
                }
                updateSelection(TKWeekFragmentListAdapter.getPosition(module.fragment))
            }
        } else {
            val intent = Intent(context, ModuleContainerActivity::class.java)
            intent.putExtra(CLAZZ, module.fragment)
            intent.putExtra(TITLE, module.text1)
            intent.putExtra(PAYLOAD, payload)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            requireContext().startActivity(intent)
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
        requestPermissions(
            arrayOf(Manifest.permission.READ_CONTACTS),
            RQ_READ_CONTACTS
        )
    }

    fun requestPostNotifications() {
        requestPermissions(
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            RQ_POST_NOTIFICATIONS
        )
    }

    fun shouldShowPermissionReadCallLogRationale() =
        TKWeekUtils.shouldShowRequestPermissionRationale(
            requireActivity(),
            Manifest.permission.READ_CALL_LOG
        )

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
        requestPermissions(
            arrayOf(Manifest.permission.READ_CALENDAR),
            RQ_READ_CALENDAR
        )
    }

    fun isTwoColumnMode(activity: Activity) =
        activity.findViewById<ViewGroup>(R.id.module_container) != null
}