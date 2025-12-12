/*
 * TKWeekActivity.kt
 *
 * Copyright 2021 MATHEMA GmbH
 *           2022 Thomas Künneth
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
package com.thomaskuenneth.tkweek.activity

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.window.core.ExperimentalWindowApi
import androidx.window.layout.FoldingFeature
import androidx.window.layout.FoldingFeature.Orientation.Companion.VERTICAL
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowMetricsCalculator
import com.thomaskuenneth.tkweek.R
import com.thomaskuenneth.tkweek.databinding.TkweekBinding
import com.thomaskuenneth.tkweek.fragment.CLAZZ
import com.thomaskuenneth.tkweek.fragment.TKWeekFragment
import kotlinx.coroutines.launch

class TKWeekActivity : TKWeekBaseActivity() {

    private var backing: TkweekBinding? = null
    private val binding get() = backing!!

    private lateinit var tracker: WindowInfoTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        backing = TkweekBinding.inflate(layoutInflater, null, false)
        setContentView(binding.root)
        configureActionBar()
        // Clean up some settings from older versions
        val prefs =
            getSharedPreferences("PickCountriesPreference", MODE_PRIVATE)
        prefs.edit {
            prefs.all.forEach { (key: String?, _: Any?) -> remove(key) }
        }
        tracker = WindowInfoTracker.getOrCreate(this)
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                configureHinge(this@TKWeekActivity)
            }
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        intent?.let {
            it.getSerializableExtra(CLAZZ)?.let { module ->
                it.removeExtra(CLAZZ)
                (supportFragmentManager.findFragmentByTag(getString(R.string.tag_module_selection)) as? TKWeekFragment)?.run {
                    selectModule(module as Class<*>, Bundle())
                }
            }
        }
    }

    override fun wantsHomeItem() = false

    @OptIn(ExperimentalWindowApi::class)
    private fun configureHinge(activity: TKWeekActivity) {
        val metrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(activity)
        binding.root.findViewById<View>(R.id.gap)?.let { gap ->
            lifecycleScope.launch {
                gap.visibility = View.GONE
                gap.layoutParams.width = 0
                var weightLeft = 0.4F
                var weightRight = 0.6F
                var layoutOrientationHorizontal: Boolean
                tracker
                    .windowLayoutInfo(activity).collect {
                        var foldWidth: Int
                        var foldHeight: Int
                        var foldAdjusted: Boolean
                        // Surface Duo and Duo 2 width/height (depending on orientation) with hinge
                        val widthOrHeight = listOf(2784, 2754)
                        it.displayFeatures.forEach { displayFeature ->
                            (displayFeature as FoldingFeature).run {
                                val separating = isSurfaceDuo || isSeparating
                                foldAdjusted =
                                    isSurfaceDuo && (bounds.width() == 0 || bounds.height() == 0)
                                foldWidth = bounds.width()
                                foldHeight = bounds.height()
                                if (separating ||
                                    occlusionType == FoldingFeature.OcclusionType.FULL
                                ) {
                                    weightLeft = 0.5F
                                    weightRight = 0.5F
                                }
                                layoutOrientationHorizontal = (orientation == VERTICAL)
                            }
                            lifecycleScope.launch {
                                val root = binding.contentRoot as LinearLayout
                                gap.visibility = View.VISIBLE
                                if (layoutOrientationHorizontal) {
                                    if (widthOrHeight.contains(metrics.bounds.width())) {
                                        if (foldAdjusted) foldWidth =
                                            if (foldHeight == 1800) 84 else 66
                                    }
                                    gap.layoutParams.width = foldWidth
                                    gap.layoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT
                                    binding.moduleSelection.layoutParams =
                                        LinearLayout.LayoutParams(
                                            0,
                                            LinearLayout.LayoutParams.MATCH_PARENT, weightLeft
                                        )
                                    binding.moduleContainer?.layoutParams =
                                        LinearLayout.LayoutParams(
                                            0,
                                            LinearLayout.LayoutParams.MATCH_PARENT, weightRight
                                        )
                                } else {
                                    if (widthOrHeight.contains(metrics.bounds.height())) {
                                        if (foldAdjusted) foldHeight =
                                            if (foldWidth == 1800) 84 else 66
                                    }
                                    val lowerHalf =
                                        (metrics.bounds.height() / 2) - (foldHeight / 2)
                                    gap.layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT
                                    gap.layoutParams.height = foldHeight
                                    binding.moduleSelection.layoutParams =
                                        LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0F
                                        )
                                    binding.moduleContainer?.layoutParams =
                                        LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT, lowerHalf
                                        )
                                }
                                root.orientation =
                                    if (layoutOrientationHorizontal) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL
                                root.invalidate()
                            }
                        }
                    }
            }
        }
    }
}

private val isSurfaceDuo: Boolean =
    "${Build.MANUFACTURER} ${Build.MODEL}".contains("Microsoft Surface Duo")
