package com.thomaskuenneth.tkweek.viewmodel

import android.os.Bundle
import androidx.lifecycle.ViewModel
import com.thomaskuenneth.tkweek.activity.TKWeekModule
import com.thomaskuenneth.tkweek.types.FragmentInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class UiState(
    val selectedModule: FragmentInfo = FragmentInfo(
        module = TKWeekModule.Week,
        arguments = null
    )
)

@HiltViewModel
class TKWeekViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    fun setModule(module: TKWeekModule, arguments: Bundle? = null) {
        _uiState.update {
            it.copy(
                selectedModule = FragmentInfo(
                    module = module,
                    arguments = arguments
                )
            )
        }
    }
}
