package com.thomaskuenneth.tkweek.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import com.thomaskuenneth.tkweek.R

@Composable
fun ClearIcon(onClick: () -> Unit) {
    IconWithTooltip(
        imageVector = Icons.Default.Clear,
        contentDescription = R.string.clear,
        onClick = onClick
    )
}
