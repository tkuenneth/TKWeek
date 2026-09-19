/*
 * TKWeekPaneVisibility.kt
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

fun isDetailPaneVisible(isTwoPane: Boolean, backStackSize: Int): Boolean =
    isTwoPane || backStackSize > 1

fun isListPaneVisible(isTwoPane: Boolean, backStackSize: Int): Boolean =
    isTwoPane || backStackSize <= 1

// In two-pane, list and detail are both always visible, so the top-level Detail entry that
// selects what the detail pane shows isn't a back-target - only genuine nesting beyond it is.
fun canNavigateBack(isTwoPane: Boolean, backStackSize: Int): Boolean =
    if (isTwoPane) backStackSize > 2 else backStackSize > 1
