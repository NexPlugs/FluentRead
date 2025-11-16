package com.example.fluentread.features.audio

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.example.fluentread.service.audio.record.AudioMediaRecorder
import com.example.fluentread.service.screenRecord.ScreenRecorder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class AudioController @Inject constructor(
    private val audiRecorder: AudioMediaRecorder,
): ViewModel() {

    fun setAudioUrl(url: String) { }

    fun play(url: String) { }

    fun startRecord() {}

    fun stopRecord() {
        ScreenRecorder.getInstance()?.stopRecording()
    }

    fun startRecording() {
        audiRecorder.startAudioRecording()
    }

    fun stopRecording() {
        audiRecorder.stopRecording()
    }

}