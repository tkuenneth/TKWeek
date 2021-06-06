/*
 * TKWeekFragment.kt
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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.preference.PreferenceManager
import com.thomaskuenneth.tkweek.adapter.TKWeekFragmentListAdapter
import com.thomaskuenneth.tkweek.databinding.TkweekfragmentBinding

const val CLAZZ = "clazz"
const val TITLE = "title"
const val PAYLOAD = "payload"
const val DATE = "date"
const val TAG_MODULE_FRAGMENT = "moduleFragment"

private const val KEY_LAST_SELECTED = "lastSelected"

class TKWeekFragment : TKWeekBaseFragment<TkweekfragmentBinding>() {

    private val binding get() = backing!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        backing = TkweekfragmentBinding.inflate(inflater, container, false)
        binding.listView.adapter =
            TKWeekFragmentListAdapter(context)
        binding.listView.setOnItemClickListener { _, _, position, _ ->
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                .edit()
                .putInt(KEY_LAST_SELECTED, position)
                .apply()
            launchModule(TKWeekFragmentListAdapter.get(position), null)
        }
        return binding.root
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        activity?.run {
            if (isTwoColumnMode(this)) {
                binding.listView.choiceMode = ListView.CHOICE_MODE_SINGLE
                binding.listView.performItemClick(
                    binding.listView,
                    PreferenceManager.getDefaultSharedPreferences(requireContext()).getInt(
                        KEY_LAST_SELECTED, 0
                    ),
                    0L
                )
            } else {
                binding.listView.choiceMode = ListView.CHOICE_MODE_NONE
            }
        }
    }
}
