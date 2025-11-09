package com.example.fluentread.features.audio

import android.content.Intent
import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import com.example.fluentread.service.audio.player.AppAudioPlayer
import com.example.fluentread.service.audio.player.CoreMediaPlayerImpl
import com.example.fluentread.service.screenRecord.ScreenRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class AudioController @Inject constructor(
    val audioPlayer: AppAudioPlayer = AppAudioPlayer(
        coreMediaPlayer = CoreMediaPlayerImpl(
            mediaBuilder = { MediaPlayer() }
        ),
        audioScope = CoroutineScope(Dispatchers.IO),
        autoPlay = true
    ),
): ViewModel() {

    fun setAudioUrl(url: String) {
        audioPlayer.prepare(url, 1)
    }

    fun play(url: String) {
        audioPlayer.play(url , 1)
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

}