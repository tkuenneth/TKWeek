/*
 * TKWeekModuleContainer.kt
 *
 * Copyright 2022 - 2026 Thomas Künneth
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
package com.thomaskuenneth.tkweek.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import com.thomaskuenneth.tkweek.TKWeekModule

@Composable
fun TKWeekModuleContainer(
    module: TKWeekModule,
    arguments: Bundle?
) {
    val context = LocalContext.current
    val fragmentManager = (context as AppCompatActivity).supportFragmentManager
    val containerId = remember { View.generateViewId() }

    DisposableEffect(module, arguments) {
        val moduleName = module.clazz.name
        // Cleanup any existing fragment with this tag (e.g. restored from saved state)
        // to ensure we don't have duplicates or "zombies" attached to old view IDs.
        fragmentManager.findFragmentByTag(moduleName)?.let {
            fragmentManager.beginTransaction().remove(it).commitNow()
        }

        val fragment = module.clazz.getConstructor().newInstance() as Fragment
        fragment.arguments = arguments
        fragmentManager.beginTransaction()
            .replace(containerId, fragment, moduleName)
            .commitNow()

        onDispose {
            fragmentManager.findFragmentByTag(moduleName)?.let {
                // Safe to use allowingStateLoss here because if the state is lost,
                // the fragment will be restored and then cleaned up by the block above
                // when the composable re-enters.
                fragmentManager.beginTransaction().remove(it).commitNowAllowingStateLoss()
            }
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            FragmentContainerView(it).apply {
                id = containerId
            }
        }
    )
}
