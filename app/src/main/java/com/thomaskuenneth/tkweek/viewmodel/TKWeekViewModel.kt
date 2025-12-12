package com.thomaskuenneth.tkweek.viewmodel

import android.os.Bundle
import androidx.lifecycle.ViewModel
import com.thomaskuenneth.tkweek.activity.TKWeekModule
import com.thomaskuenneth.tkweek.types.FragmentInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class UiState(
    val modules: List<FragmentInfo>
)

@HiltViewModel
class TKWeekViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(
        UiState(
            modules = listOf(
                FragmentInfo(
                    module = TKWeekModule.Week,
                    arguments = null
                )
            )
        )
    )
    val uiState = _uiState.asStateFlow()

    private val _navigationTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val navigationTrigger = _navigationTrigger.asSharedFlow()

    fun selectModule(module: TKWeekModule, arguments: Bundle?, replace: Boolean) {
        _uiState.update {
            it.copy(
                modules = (if (replace) emptyList() else it.modules) + FragmentInfo(
                    module = module,
                    arguments = arguments
                )
            )
        }
        _navigationTrigger.tryEmit(Unit)
    }

    fun popModule() {
        _uiState.update {
            it.copy(
                modules = it.modules.subList(0, it.modules.size - 1)
            )
        }
    }
}
