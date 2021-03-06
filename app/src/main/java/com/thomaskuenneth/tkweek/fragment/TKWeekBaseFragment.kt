/*
 * TKWeekBaseFragment.kt
 *
 * TKWeek (c) Thomas Künneth 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
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
        // Dummyimplementierung tut nichts
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
                            TAG_MODULE_FRAGMENT
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

    fun requestReadContacts() {
        requestPermissions(
            arrayOf(Manifest.permission.READ_CONTACTS),
            RQ_READ_CONTACTS
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