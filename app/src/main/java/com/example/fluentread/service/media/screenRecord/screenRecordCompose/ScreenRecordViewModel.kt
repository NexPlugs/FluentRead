package com.example.fluentread.service.media.screenRecord.screenRecordCompose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fluentread.service.media.screenRecord.ScreenRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


data class ScreenRecordDataView(
    val isRecording: Boolean = false,
    val recordingTime: Long = 0L
)

class ScreenRecordViewModel @Inject constructor(): ViewModel() {

    private val _uiState: MutableStateFlow<ScreenRecordDataView> = MutableStateFlow(ScreenRecordDataView())
    val state: StateFlow<ScreenRecordDataView> = _uiState


    fun toggleRecord() {
        viewModelScope.launch(Dispatchers.IO) {
            val isRecording = _uiState.value.isRecording

            if(isRecording) {
                ScreenRecorder.getInstance()?.stopRecording()
            } else {
                ScreenRecorder.getInstance()?.startRecording()
            }

            _uiState.update { it.copy(isRecording = !isRecording) }
        }
    }

}