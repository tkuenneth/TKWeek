/*
 * TKWeekBackStack.kt
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
package com.thomaskuenneth.tkweek.navigation

import androidx.navigation3.runtime.NavKey
import com.thomaskuenneth.tkweek.TKWeekModule
import com.thomaskuenneth.tkweek.types.TKWeekDestination
import com.thomaskuenneth.tkweek.viewmodel.NavigationRequest

val DEFAULT_MODULE: TKWeekModule = TKWeekModule.Week

fun initialTKWeekBackStack(): List<NavKey> = listOf(TKWeekDestination.ModuleList)

// A top-level request always drops every entry above the list entry and pushes the requested one,
// even when re-selecting the module already active - never checks structural equality, since that's
// what let reselecting a nested-into module silently do nothing.
fun MutableList<NavKey>.applyNavigation(request: NavigationRequest) {
    require(isNotEmpty()) { "Back stack must never be empty." }
    if (request.topLevel) {
        while (size > 1) {
            removeAt(lastIndex)
        }
    }
    add(TKWeekDestination.Detail(module = request.module, date = request.date))
}
