package com.thomaskuenneth.tkweek

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable

@Composable
fun BackArrow(
    @StringRes description: Int = R.string.navigate_up,
    onClick: () -> Unit
) {
    IconWithTooltip(
        imageVector = Icons.AutoMirrored.Default.ArrowBack,
        contentDescription = description,
        onClick = onClick
    )
}
