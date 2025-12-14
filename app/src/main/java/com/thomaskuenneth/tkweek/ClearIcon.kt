package com.thomaskuenneth.tkweek

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable

@Composable
fun ClearIcon(onClick: () -> Unit) {
    IconWithTooltip(
        imageVector = Icons.Default.Clear,
        contentDescription = R.string.clear,
        onClick = onClick
    )
}
