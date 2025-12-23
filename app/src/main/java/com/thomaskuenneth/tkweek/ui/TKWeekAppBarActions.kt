package com.thomaskuenneth.tkweek.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.thomaskuenneth.tkweek.R
import com.thomaskuenneth.tkweek.viewmodel.AppBarAction


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TKWeekAppBarActions(
    appBarActions: List<AppBarAction>,
) {
    var showMenu by remember { mutableStateOf(false) }
    appBarActions.forEach { action ->
        if (action.isVisible) {
            if (action.icon != null) {
                TooltipBox(
                    positionProvider =
                        TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                    tooltip = { PlainTooltip { Text(stringResource(id = action.contentDescription)) } },
                    state = rememberTooltipState()
                ) {
                    IconButton(onClick = action.onClick) {
                        Icon(
                            painter = painterResource(id = action.icon),
                            contentDescription = stringResource(id = action.contentDescription)
                        )
                    }
                }
            }
        }
    }
    val textActions =
        appBarActions.filter { it.isVisible && it.icon == null }
    if (textActions.isNotEmpty()) {
        Box {
            TooltipBox(
                positionProvider =
                    TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                tooltip = { PlainTooltip { Text(stringResource(R.string.more_options)) } },
                state = rememberTooltipState()
            ) {
                IconButton(onClick = { showMenu = !showMenu }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.more_options)
                    )
                }
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                textActions.forEach { action ->
                    DropdownMenuItem(
                        text = { Text(text = stringResource(id = action.title!!)) },
                        onClick = {
                            showMenu = false
                            action.onClick()
                        }
                    )
                }
            }
        }
    }
}
