/*
 * AnnualEventsFabMenu.kt
 *
 * Copyright 2026 Thomas Künneth
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

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowDpSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.core.layout.WindowSizeClass
import com.thomaskuenneth.tkweek.R
import com.thomaskuenneth.tkweek.viewmodel.AnnualEventsDialog
import com.thomaskuenneth.tkweek.viewmodel.AnnualEventsViewModel
import kotlin.math.roundToInt

private const val ICON_SWAP_PROGRESS = 0.5f
private val LIST_FAB_GAP = 16.dp
private val FAB_MENU_MARGIN = 16.dp
private val FAB_MENU_MARGIN_LARGE_WINDOW = 24.dp
private val LARGE_WINDOW_WIDTH = WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND.dp

private data class FabMenuItem(
    @param:StringRes val label: Int,
    @param:DrawableRes val icon: Int,
    val testTag: String,
    val dialog: AnnualEventsDialog
)

private val FAB_MENU_ITEMS = listOf(
    FabMenuItem(
        label = R.string.annual_event_backup_restore,
        icon = R.drawable.ic_baseline_backup_24,
        testTag = TKWeekTestTags.FAB_MENU_BACKUP_RESTORE,
        dialog = AnnualEventsDialog.BACKUP_RESTORE
    ),
    FabMenuItem(
        label = R.string.new_event,
        icon = R.drawable.ic_baseline_add_24,
        testTag = TKWeekTestTags.FAB_MENU_NEW_EVENT,
        dialog = AnnualEventsDialog.NEW_EVENT
    )
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AnnualEventsFabMenu(
    viewModel: AnnualEventsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val expanded by viewModel.isFabMenuExpanded.collectAsStateWithLifecycle()
    val gap = with(LocalDensity.current) { LIST_FAB_GAP.roundToPx() }
    val extraMargin = if (currentWindowDpSize().width >= LARGE_WINDOW_WIDTH) {
        FAB_MENU_MARGIN_LARGE_WINDOW - FAB_MENU_MARGIN
    } else {
        0.dp
    }
    Box(modifier = modifier.fillMaxSize()) {
        FloatingActionButtonMenu(
            expanded = expanded,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .safeDrawingPadding()
                .padding(extraMargin)
                .onGloballyPositioned { coordinates ->
                    val rootHeight = coordinates.findRootCoordinates().size.height
                    val top = coordinates.boundsInRoot().top
                    viewModel.setFabMenuClearance((rootHeight - top).roundToInt() + gap)
                },
            button = {
                ToggleFloatingActionButton(
                    checked = expanded,
                    onCheckedChange = viewModel::setFabMenuExpanded,
                    containerColor = ToggleFloatingActionButtonDefaults.containerColor(
                        initialColor = MaterialTheme.colorScheme.primaryContainer,
                        finalColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .testTag(TKWeekTestTags.FAB_MENU_TOGGLE)
                ) {
                    val showClose = checkedProgress > ICON_SWAP_PROGRESS
                    val iconColor = ToggleFloatingActionButtonDefaults.iconColor(
                        initialColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        finalColor = MaterialTheme.colorScheme.onPrimary
                    )
                    Icon(
                        painter = painterResource(
                            if (showClose) R.drawable.ic_baseline_close_24
                            else R.drawable.ic_baseline_add_24
                        ),
                        contentDescription = stringResource(
                            if (showClose) R.string.hide_actions else R.string.more_options
                        ),
                        tint = iconColor(checkedProgress)
                    )
                }
            }
        ) {
            FAB_MENU_ITEMS.forEach { item ->
                FloatingActionButtonMenuItem(
                    onClick = { viewModel.requestDialog(item.dialog) },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.testTag(item.testTag),
                    icon = {
                        Icon(painter = painterResource(item.icon), contentDescription = null)
                    },
                    text = { Text(text = stringResource(item.label)) }
                )
            }
        }
    }
}
