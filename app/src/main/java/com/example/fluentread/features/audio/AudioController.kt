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
//    private val audioPlayer: AppAudioPlayer = AppAudioPlayer(
//        coreMediaPlayer = CoreMediaPlayerImpl(
//            mediaBuilder = { MediaPlayer() }
//        ),
//        audioScope = CoroutineScope(Dispatchers.IO),
//        autoPlay = true
//    ),
): ViewModel() {

    fun setAudioUrl(url: String) {
//        audioPlayer.prepare(url, 1)
    }

    fun play(url: String) {
//        audioPlayer.play(url , 1)
    }

    fun startRecord() {
        ScreenRecorder.getInstance()?.startRecording(
            recordingName = "FluentRead_Audio_Record",
            data = Intent()
        )
    }

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