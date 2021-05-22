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

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.FragmentTransaction
import com.thomaskuenneth.tkweek.ActivityDescription
import com.thomaskuenneth.tkweek.R
import com.thomaskuenneth.tkweek.activity.ModuleContainerActivity
import com.thomaskuenneth.tkweek.adapter.TKWeekFragmentListAdapter
import com.thomaskuenneth.tkweek.databinding.TkweekfragmentBinding

const val CLAZZ = "clazz"
const val TITLE = "title"
const val PAYLOAD = "payload"
const val DATE = "date"
const val TAG_MODULE_FRAGMENT = "moduleFragment"

const val STR_LAST_SELECTED = "lastSelected"

class TKWeekFragment : TKWeekBaseFragment<TkweekfragmentBinding>() {

    private val binding get() = backing!!

    private var twoColumnMode = false
    private var lastSelected = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        backing = TkweekfragmentBinding.inflate(inflater, container, false)
        binding.listView.adapter =
            TKWeekFragmentListAdapter(context)
        binding.listView.setOnItemClickListener { _, _, position, _ -> showModule(position, null) }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.run {
            lastSelected = getInt(STR_LAST_SELECTED, 0)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STR_LAST_SELECTED, lastSelected)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        activity?.run {
            twoColumnMode =
                findViewById<ViewGroup>(R.id.module_container) != null
            if (twoColumnMode) {
                binding.listView.choiceMode = ListView.CHOICE_MODE_SINGLE
                showModule(lastSelected, null)
            } else {
                binding.listView.choiceMode = ListView.CHOICE_MODE_NONE
            }
        }
    }

    fun showModule(index: Int, payload: Bundle?) {
        lastSelected = index
        val item = binding.listView.adapter.getItem(index) as ActivityDescription
        val fragment = item.fragment.newInstance()
        fragment.arguments = payload
        if (twoColumnMode) {
            binding.listView.setItemChecked(index, true)
            parentFragmentManager.run {
                beginTransaction()
                    .replace(
                        R.id.module_container,
                        fragment,
                        TAG_MODULE_FRAGMENT
                    )
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit()
            }
        } else {
            val intent = Intent()
            intent.setClass(
                requireActivity(),
                ModuleContainerActivity::class.java
            )
            intent.putExtra(CLAZZ, item.fragment)
            intent.putExtra(TITLE, item.text1)
            startActivity(intent)
        }
    }
}
