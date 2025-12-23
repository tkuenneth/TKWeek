package com.thomaskuenneth.tkweek.viewmodel

import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thomaskuenneth.tkweek.TKWeekModule
import com.thomaskuenneth.tkweek.preference.PreferenceManager
import com.thomaskuenneth.tkweek.types.TKWeekModuleWithArguments
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class UiState(
    val topLevelModuleWithArguments: TKWeekModuleWithArguments,
    val avoidHinge: Boolean = false,
    val isListScrolled: Boolean = false,
    val isDetailScrolled: Boolean = false,
    val shouldShowProgressIndicator: Boolean = false,
)

data class AppBarAction(
    @param:DrawableRes val icon: Int?,
    @param:StringRes val contentDescription: Int,
    @param:StringRes val title: Int?,
    val onClick: () -> Unit,
    val isVisible: Boolean = true
)

data class NavigationEvent(
    val moduleWithArguments: TKWeekModuleWithArguments, val topLevel: Boolean
)

@HiltViewModel
class TKWeekViewModel @Inject constructor(
    preferenceManager: PreferenceManager
) : ViewModel() {

    private val _uiState = with(
        TKWeekModuleWithArguments(
            module = TKWeekModule.Week,
            arguments = null,
        )
    ) {
        MutableStateFlow(
            UiState(
                topLevelModuleWithArguments = this,
            )
        )
    }

    val uiState = _uiState.asStateFlow()

    private val _appBarActions = MutableStateFlow<List<AppBarAction>>(emptyList())
    val appBarActions = _appBarActions.asStateFlow()

    private val _navigationTrigger = Channel<NavigationEvent>(Channel.CONFLATED)
    val navigationTrigger = _navigationTrigger.receiveAsFlow()

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

    fun selectModuleWithArguments(
        module: TKWeekModule,
        arguments: Bundle?,
        topLevel: Boolean
    ) {
        val moduleWithArguments = TKWeekModuleWithArguments(
            module = module,
            arguments = arguments
        )
        _uiState.update {
            it.copy(
                topLevelModuleWithArguments = if (topLevel) moduleWithArguments else it.topLevelModuleWithArguments,
            )
        }
        _navigationTrigger.trySend(
            NavigationEvent(
                moduleWithArguments = moduleWithArguments,
                topLevel = topLevel
            )
        )
    }

    fun setShouldShowProgressIndicator(shouldShowProgressIndicator: Boolean) {
        _uiState.update { it.copy(shouldShowProgressIndicator = shouldShowProgressIndicator) }
    }
}
