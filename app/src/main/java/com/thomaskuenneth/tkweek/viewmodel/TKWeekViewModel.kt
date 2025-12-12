package com.thomaskuenneth.tkweek.viewmodel

import android.os.Bundle
import androidx.lifecycle.ViewModel
import com.thomaskuenneth.tkweek.TKWeekModule
import com.thomaskuenneth.tkweek.types.FragmentInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
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

    private val _navigationTrigger = Channel<Unit>(Channel.CONFLATED)
    val navigationTrigger = _navigationTrigger.receiveAsFlow()

    private val _fragmentScrollDelta = MutableSharedFlow<Float>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val fragmentScrollDelta = _fragmentScrollDelta.asSharedFlow()

    private val _resetScrollTrigger = Channel<Unit>(Channel.CONFLATED)
    val resetScrollTrigger = _resetScrollTrigger.receiveAsFlow()

    fun onFragmentScrolled(deltaY: Float) {
        _fragmentScrollDelta.tryEmit(deltaY)
    }

    fun resetScroll() {
        _resetScrollTrigger.trySend(Unit)
    }

    fun selectModule(module: TKWeekModule, arguments: Bundle?, replace: Boolean) {
        _uiState.update {
            it.copy(
                modules = (if (replace) emptyList() else it.modules) + FragmentInfo(
                    module = module,
                    arguments = arguments
                )
            )
        }
        if (replace) _navigationTrigger.trySend(Unit)
    }

    fun popModule() {
        _uiState.update {
            it.copy(
                modules = it.modules.subList(0, it.modules.size - 1)
            )
        }
    }
}
