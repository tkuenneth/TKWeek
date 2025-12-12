package com.thomaskuenneth.tkweek.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.thomaskuenneth.tkweek.TKWeekModule
import com.thomaskuenneth.tkweek.util.BottomSpace
import com.thomaskuenneth.tkweek.viewmodel.UiState

@Composable
fun TKWeekModuleSelector(
    uiState: UiState,
    onModuleSelected: (TKWeekModule) -> Unit,
    detailVisible: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(TKWeekModule.entries) { entry ->
            with(uiState.modules.last()) {
                val selected = detailVisible && module == entry
                key(entry) {
                    ListItem(
                        headlineContent = { Text(text = stringResource(id = entry.titleRes)) },
                        supportingContent = { Text(text = stringResource(id = entry.descriptionRes)) },
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.large)
                            .clickable { onModuleSelected(entry) },
                        colors = ListItemDefaults.colors(
                            containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                            headlineColor = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                            supportingColor = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
        item {
            BottomSpace()
        }
    }
}
