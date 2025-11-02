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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fluentread.features.components.BuildButton

@Composable
fun AudioPlayScreen(
    modifier: Modifier,
    controller: AudioController = hiltViewModel<AudioController>()
) {
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
                controller.setAudioUrl("https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3")
            }
        ) {
            Text(text = "Set Audio", textAlign = TextAlign.Center)
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
                controller.play("https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3")
            }
        ) {
            Text(text = "Set Audio", textAlign = TextAlign.Center)
        }

    }
}