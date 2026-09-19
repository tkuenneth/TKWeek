/*
 * TKWeekViewModel.kt
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
package com.thomaskuenneth.tkweek.viewmodel

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thomaskuenneth.tkweek.TKWeekModule
import com.thomaskuenneth.tkweek.preference.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class TKWeekUiState(
    val avoidHinge: Boolean = false,
    val isListScrolled: Boolean = false,
    val isDetailScrolled: Boolean = false,
    val shouldShowProgressIndicator: Boolean = false,
)

data class AppBarAction(
    @param:StringRes val title: Int,
    @param:DrawableRes val icon: Int? = null,
    @param:StringRes val contentDescription: Int = title,
    val onClick: () -> Unit,
    val isVisible: Boolean = true
)

// topLevel = true discards every nested screen and makes module the detail pane's root;
// topLevel = false pushes module on top of whatever is already showing.
data class NavigationRequest(
    val module: TKWeekModule,
    val date: Long? = null,
    val topLevel: Boolean,
)

@HiltViewModel
class TKWeekViewModel @Inject constructor(
    preferenceManager: PreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TKWeekUiState())
    val uiState = _uiState.asStateFlow()

    private val _appBarActions = MutableStateFlow<List<AppBarAction>>(emptyList())
    val appBarActions = _appBarActions.asStateFlow()

    // SharedFlow, not Channel: a Channel delivers to exactly one collector, which could be one
    // that's being torn down across an activity recreation, silently swallowing the request.
    private val _navigationRequests = MutableSharedFlow<NavigationRequest>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val navigationRequests: SharedFlow<NavigationRequest> = _navigationRequests.asSharedFlow()

    init {
        preferenceManager.avoidHinge.onEach { avoidHinge ->
            _uiState.update { it.copy(avoidHinge = avoidHinge) }
        }.launchIn(viewModelScope)
    }

    fun setListScrolled(isScrolled: Boolean) {
        _uiState.update { it.copy(isListScrolled = isScrolled) }
    }

    fun setDetailScrolled(isScrolled: Boolean) {
        _uiState.update { it.copy(isDetailScrolled = isScrolled) }
    }

    fun setAppBarActions(actions: List<AppBarAction>) {
        _appBarActions.update { actions }
    }

    fun requestNavigation(module: TKWeekModule, date: Long? = null, topLevel: Boolean) {
        _navigationRequests.tryEmit(
            NavigationRequest(module = module, date = date, topLevel = topLevel)
        )
    }

    fun setShouldShowProgressIndicator(shouldShowProgressIndicator: Boolean) {
        _uiState.update { it.copy(shouldShowProgressIndicator = shouldShowProgressIndicator) }
    }
}
