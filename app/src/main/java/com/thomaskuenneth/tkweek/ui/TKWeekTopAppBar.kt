package com.thomaskuenneth.tkweek.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
                )
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
