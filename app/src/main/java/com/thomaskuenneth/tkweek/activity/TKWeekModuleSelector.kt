package com.thomaskuenneth.tkweek.activity

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.thomaskuenneth.tkweek.util.BottomSpace

@Composable
fun TKWeekModuleSelector(
    selectedModule: TKWeekModule,
    onModuleSelected: (TKWeekModule) -> Unit,
    detailVisible: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(TKWeekModule.entries) { module ->
            val selected = detailVisible && module == selectedModule
            ListItem(
                headlineContent = { Text(text = stringResource(id = module.titleRes)) },
                supportingContent = { Text(text = stringResource(id = module.descriptionRes)) },
                modifier = Modifier
                    .clip(MaterialTheme.shapes.large)
                    .clickable { onModuleSelected(module) },
                colors = ListItemDefaults.colors(
                    containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                    headlineColor = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                    supportingColor = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
        item {
            BottomSpace()
        }
    }
}
