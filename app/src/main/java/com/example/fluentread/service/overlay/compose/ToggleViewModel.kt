package com.example.fluentread.service.overlay.compose

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ToggleDataView(
    val isScrolling: Boolean = false,
    val eyeScrolling: Boolean = false
)


class ToggleViewModel @Inject constructor(): ViewModel() {

    private val _uiState: MutableStateFlow<ToggleDataView> = MutableStateFlow(ToggleDataView())
    val uiState: StateFlow<ToggleDataView> = _uiState

    fun setIsScrolling(isScrolling: Boolean) {
        _uiState.update { it.copy(isScrolling = isScrolling) }
    }

    fun setEyeScrolling(eyeScrolling: Boolean) {
        _uiState.update { it.copy(eyeScrolling = eyeScrolling) }
    }
}