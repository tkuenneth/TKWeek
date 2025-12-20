package com.thomaskuenneth.tkweek.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.res.stringResource
import com.thomaskuenneth.tkweek.R
import com.thomaskuenneth.tkweek.viewmodel.AppBarAction
import com.thomaskuenneth.tkweek.viewmodel.TKWeekViewModel
import com.thomaskuenneth.tkweek.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TKWeekSearchBar(
    uiState: UiState,
    viewModel: TKWeekViewModel,
    focusManager: FocusManager,
    canNavigateBack: Boolean,
    onNavigateBack: () -> Unit,
    appBarActions: List<AppBarAction>,
    modifier: Modifier = Modifier
) {
    SearchBar(
        modifier = modifier,
        inputField = {
            SearchBarDefaults.InputField(
                query = uiState.searchQuery,
                onQueryChange = {
                    viewModel.setSearchQuery(it)
                    if (!uiState.isSearchActive) {
                        viewModel.setSearchActive(true)
                    }
                },
                onSearch = {
                    focusManager.clearFocus()
                },
                expanded = uiState.isSearchActive,
                onExpandedChange = { viewModel.setSearchActive(it) },
                placeholder = { Text(stringResource(id = R.string.events)) },
                leadingIcon = {
                    if (uiState.isSearchActive) {
                        BackArrow(
                            onClick = {
                                viewModel.setSearchActive(false)
                                viewModel.setSearchQuery("")
                                focusManager.clearFocus()
                            },
                            description = R.string.close_search
                        )
                    } else {
                        if (canNavigateBack) {
                            BackArrow(onClick = onNavigateBack)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null
                            )
                        }
                    }
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (uiState.searchQuery.isNotEmpty()) {
                            ClearIcon {
                                viewModel.setSearchQuery("")
                            }
                        }
                        if (!uiState.isSearchActive) {
                            TKWeekAppBarActions(appBarActions)
                        }
                    }
                }
            )
        },
        expanded = false,
        onExpandedChange = {
            if (it) {
                viewModel.setSearchActive(true)
            }
        },
    ) {
    }
}
