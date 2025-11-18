package com.example.fluentread.features.audio

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fluentread.features.components.BuildButton
import com.example.fluentread.service.audio.compose.AudioRecordCircleCompose
import com.example.fluentread.service.audio.models.AudioConfig
import com.example.fluentread.service.screenRecord.models.ScreenRecordConfig
import com.example.fluentread.utils.launchAudiRecord
import com.example.fluentread.utils.launchScreenRecorder

@Composable
fun AudioPlayScreen(
    modifier: Modifier,
    controller: AudioController = hiltViewModel<AudioController>()
) {
    val context = LocalContext.current

    val uiState: AudioControllerState = controller.uiState.collectAsState().value

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center
    ) {
        BuildButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 16.dp)
                .align(Alignment.CenterHorizontally),
            color = MaterialTheme.colorScheme.primary,
            onPress = {
                Log.d("TAG", "AudioPlayScreen: Playing audio")
                controller.setAudioUrl("https://commondatastorage.googleapis.com/codeskulptor-demos/DDR_assets/Kangaroo_MusiQue_-_The_Neverwritten_Role_Playing_Game.mp3")
            }
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text =  "Start audio play",
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        BuildButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 16.dp)
                .align(Alignment.CenterHorizontally),
            color = MaterialTheme.colorScheme.primary,
            onPress = {
                Log.d("TAG", "AudioPlayScreen: Playing audio")
                controller.play("https://commondatastorage.googleapis.com/codeskulptor-demos/DDR_assets/Kangaroo_MusiQue_-_The_Neverwritten_Role_Playing_Game.mp3")
            }
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Set Audio", textAlign = TextAlign.Center
            )
        }


        BuildButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 16.dp)
                .align(Alignment.CenterHorizontally),
            color = MaterialTheme.colorScheme.primary,
            onPress = {
                Log.d("TAG", "AudioPlayScreen: Pausing audio")
                controller.startRecording()
            }
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Start audi recording", textAlign = TextAlign.Center
            )
        }

        AudioRecordCircleCompose(
            isRecording = uiState.isRecording,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(16.dp),
            amplitude = uiState.amplitudeTracking.toInt()
        )

        BuildButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 16.dp)
                .align(Alignment.CenterHorizontally),
            color = MaterialTheme.colorScheme.primary,
            onPress = {
                Log.d("TAG", "AudioPlayScreen: Stopping audio")
                controller.stopRecording()
            }
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Stop audio recording", textAlign = TextAlign.Center
            )
        }

        BuildButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 16.dp)
                .align(Alignment.CenterHorizontally),
            color = MaterialTheme.colorScheme.primary,
            onPress = { context.launchScreenRecorder(ScreenRecordConfig()) }
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Initial Screen Service ", textAlign = TextAlign.Center)
        }

        BuildButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 16.dp)
                .align(Alignment.CenterHorizontally),
            color = MaterialTheme.colorScheme.primary,
            onPress = { controller.startRecord() }
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Start Screen Record", textAlign = TextAlign.Center
            )
        }

        BuildButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 16.dp)
                .align(Alignment.CenterHorizontally),
            color = MaterialTheme.colorScheme.primary,
            onPress = { controller.stopRecord() }
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Stop Screen Record", textAlign = TextAlign.Center
            )
        }
    }
}