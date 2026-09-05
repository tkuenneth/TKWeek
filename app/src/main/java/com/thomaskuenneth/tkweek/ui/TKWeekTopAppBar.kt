/*
 * TKWeekTopAppBar.kt
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

import androidx.compose.animation.animateColorAsState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.thomaskuenneth.tkweek.R
import com.thomaskuenneth.tkweek.viewmodel.AppBarAction
import com.thomaskuenneth.tkweek.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TKWeekTopAppBar(
    uiState: UiState,
    detailVisible: Boolean,
    activeModuleTitleRes: Int,
    appBarActions: List<AppBarAction>,
    canNavigateBack: Boolean,
    onNavigateBack: () -> Unit
) {
    val isScrolled = uiState.isListScrolled || uiState.isDetailScrolled
    val topAppBarColors = TopAppBarDefaults.topAppBarColors()
    val containerColor by animateColorAsState(
        targetValue = if (isScrolled)
            topAppBarColors.scrolledContainerColor
        else
            topAppBarColors.containerColor,
        label = "containerColor"
    )
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(
                    if (!detailVisible) {
                        R.string.app_name
                    } else {
                        activeModuleTitleRes
                    }
                ),
                modifier = Modifier.testTag(TKWeekTestTags.TOP_APP_BAR_TITLE)
            )
        },
        navigationIcon = {
            if (canNavigateBack) {
                BackArrow(onClick = onNavigateBack)
            }
        },
        actions = {
            if (detailVisible) {
                TKWeekAppBarActions(appBarActions)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor
        )
    )
}
