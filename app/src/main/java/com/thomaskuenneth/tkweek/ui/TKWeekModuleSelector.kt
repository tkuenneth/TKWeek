/*
 * TKWeekModuleSelector.kt
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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.thomaskuenneth.tkweek.R
import com.thomaskuenneth.tkweek.TKWeekModule
import com.thomaskuenneth.tkweek.util.BottomSpace
import com.thomaskuenneth.tkweek.util.TopSpace

@Composable
fun TKWeekModuleSelector(
    selectedModule: TKWeekModule?,
    onModuleSelected: (TKWeekModule) -> Unit,
    belowTheStatusBar: Boolean,
    modifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyListState()
    LaunchedEffect(selectedModule) {
        val index = selectedModule?.let { module -> TKWeekModule.entries.indexOf(module) } ?: return@LaunchedEffect
        val isVisible = lazyListState.layoutInfo.visibleItemsInfo.any { it.index == index }
        if (!isVisible) {
            lazyListState.animateScrollToItem(index)
        }
    }
    LazyColumn(
        state = lazyListState,
        modifier = modifier
            .fadingEdge(lazyListState)
            .testTag(TKWeekTestTags.MODULE_LIST)
    ) {
        item {
            TopSpace()
            if (belowTheStatusBar) {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.content_padding)))
            }
        }
        items(TKWeekModule.entries, key = { it }) { entry ->
            val selected = entry == selectedModule
            ListItem(
                supportingContent = { Text(text = stringResource(id = entry.descriptionRes)) },
                modifier = Modifier
                    .testTag(TKWeekTestTags.moduleListItem(entry.name))
                    .clip(MaterialTheme.shapes.large)
                    .clickable { onModuleSelected(entry) },
                colors = ListItemDefaults.colors(
                    containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                    headlineColor = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                    supportingColor = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(text = stringResource(id = entry.titleRes))
            }
        }
        item {
            BottomSpace()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TKWeekModuleSelectorPreview() {
    MaterialTheme(colorScheme = colorScheme()) {
        TKWeekModuleSelector(
            selectedModule = TKWeekModule.Calendar,
            onModuleSelected = {},
            belowTheStatusBar = true
        )
    }
}
