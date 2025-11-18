package com.example.fluentread.features.audio

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fluentread.service.audio.models.AudioConfig
import com.example.fluentread.service.audio.record.AudioMediaRecorder
import com.example.fluentread.service.media.screenRecord.ScreenRecorder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AudioControllerState(
    val isRecording: Boolean = false,
    val amplitudeTracking: Float = 0f,
)

@HiltViewModel
class AudioController @Inject constructor(
    @ApplicationContext context: Context
): ViewModel() {

    private var _uiState: MutableStateFlow<AudioControllerState> = MutableStateFlow(AudioControllerState())
    val uiState: StateFlow<AudioControllerState> = _uiState

    companion object {
        const val TAG = "AudioController"
    }

    private val audioRecorder = AudioMediaRecorder(context, audioConfig = AudioConfig()).apply {
        setOnPollAmplitudeListener {
            viewModelScope.launch {
                val currentState = _uiState.value
                _uiState.value = currentState.copy(amplitudeTracking = it)
            }
        }
        setOnErrorListener { _, error, message ->
            Log.d(TAG, "AudioRecorder Error: $error, Message: $message")
        }
        setOnRecordStartedListener {
            viewModelScope.launch {
                val currentState = _uiState.value
                _uiState.value = currentState.copy(isRecording = true)
            }
        }
        setOnRecordStoppedListener {
            Log.d(TAG, "AudioRecord uri result : ${it?.filePath ?: "No data"}")

            viewModelScope.launch {
                val currentState = _uiState.value
                _uiState.value = currentState.copy(isRecording = false, amplitudeTracking = 0f)
            }
        }

    }

    fun setAudioUrl(url: String) { }

    fun play(url: String) { }

    fun startRecord() {}

    fun stopRecord() {
        ScreenRecorder.getInstance()?.stopRecording()
    }

    fun startRecording() {
        audioRecorder.startAudioRecording()
    }

    fun stopRecording() {
        audioRecorder.stopRecording()
    }

}