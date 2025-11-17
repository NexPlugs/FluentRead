package com.example.fluentread.features.audio

import androidx.lifecycle.ViewModel
import com.example.fluentread.service.screenRecord.ScreenRecorder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AudioController @Inject constructor(): ViewModel() {

    fun setAudioUrl(url: String) { }

    fun play(url: String) { }

    fun startRecord() {}

    fun stopRecord() {
        ScreenRecorder.getInstance()?.stopRecording()
    }

    fun startRecording() {}

    fun stopRecording() {}

}