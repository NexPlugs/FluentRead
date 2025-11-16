package com.example.fluentread.features.audio

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.example.fluentread.service.audio.record.AudioMediaRecorder
import com.example.fluentread.service.screenRecord.ScreenRecorder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AudioController @Inject constructor(
    private val audiRecorder: AudioMediaRecorder,
): ViewModel() {

    fun setAudioUrl(url: String) {
//        audioPlayer.prepare(url, 1)
    }

    fun play(url: String) {
//        audioPlayer.play(url , 1)
    }

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