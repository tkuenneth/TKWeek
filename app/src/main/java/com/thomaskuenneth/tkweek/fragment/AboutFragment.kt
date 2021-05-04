/*
 * AboutFragment.kt
 *
 * TKWeek (c) Thomas Künneth 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek.fragment

import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.thomaskuenneth.tkweek.R
import com.thomaskuenneth.tkweek.databinding.AboutBinding
import com.thomaskuenneth.tkweek.util.TKWeekUtils
import java.util.*

private const val TAG = "AboutFragment"

class AboutFragment : TKWeekBaseFragment<AboutBinding>() {

    private val binding get() = backing!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        backing = AboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val pm = activity?.application?.packageManager
        var vn = getString(R.string.unknown)
        try {
            activity?.packageName?.let { pm?.getPackageInfo(it, 0) }?.run {
                vn = versionName
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "getPackageInfo()", e)
        }
        binding.aboutVersionName.text = vn
        val locale = Locale.getDefault()
        binding.aboutLanguage.text = locale.displayLanguage.let {
            if (it.isNotEmpty())
                it
            else
                getString(R.string.unknown)
        }
        binding.aboutCountry.text = locale.displayCountry.let {
            if (it.isNotEmpty())
                it
            else
                getString(R.string.unknown)
        }
        val tz = TimeZone.getDefault()
        var tzn = tz.getDisplayName(
            tz.inDaylightTime(Date()),
            TimeZone.LONG
        )
        if (tzn == null || tzn.isEmpty()) {
            tzn = getString(R.string.unknown)
        }
        binding.aboutTimezone.text = tzn
        binding.aboutCalendarClass.text = Calendar.getInstance().javaClass.simpleName
        val email = getString(R.string.my_email)
        val welcome = getString(R.string.welcome_text, email)
        val pos = welcome.indexOf(email)
        val spannable: Spannable = SpannableString(welcome)
        spannable.setSpan(object : URLSpan("mailto:$email") {
            override fun onClick(widget: View) {
                try {
                    super.onClick(widget)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(
                        activity,
                        R.string.action_could_not_be_completed,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }, pos, pos + email.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.aboutInfo.movementMethod = LinkMovementMethod.getInstance()
        binding.aboutInfo.text = spannable
        val metrics = resources.displayMetrics
        binding.aboutDensity.text =
            TKWeekUtils.integerToString(metrics.densityDpi)
        val sizeInPixels =
            "${metrics.widthPixels} ${getString(R.string.times_symbol)} ${metrics.heightPixels}"
        val sizeInDp =
            "${(metrics.widthPixels / metrics.density).toInt()} ${getString(R.string.times_symbol)} ${(metrics.heightPixels / metrics.density).toInt()}"
        binding.aboutSizeInPixel.text = getString(
            R.string.four_strings_two_lines,
            sizeInPixels, getString(R.string.pixel), sizeInDp, getString(R.string.dp)
        )
    }
}