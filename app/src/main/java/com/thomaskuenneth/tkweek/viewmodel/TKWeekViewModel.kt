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
    val currentFragmentInfo: FragmentInfo
)

data class NavigationEvent(
    val fragmentInfo: FragmentInfo,
    val topLevel: Boolean
)

@HiltViewModel
class TKWeekViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(
        UiState(
            currentFragmentInfo =
                FragmentInfo(
                    module = TKWeekModule.Week,
                    arguments = null
                )
        )
    )
    val uiState = _uiState.asStateFlow()

    private val _navigationTrigger = Channel<NavigationEvent>(Channel.CONFLATED)
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

    fun selectModule(module: TKWeekModule, arguments: Bundle?, topLevel: Boolean) {
        val fragmentInfo = FragmentInfo(
            module = module,
            arguments = arguments
        )
        _uiState.update {
            it.copy(
                currentFragmentInfo = fragmentInfo
            )
        }
        _navigationTrigger.trySend(
            NavigationEvent(
                fragmentInfo = fragmentInfo,
                topLevel = topLevel
            )
        )
    }
}
