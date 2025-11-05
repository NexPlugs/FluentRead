package com.example.fluentread.service.overlay.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fluentread.ui.theme.FluentReadTheme


@Composable
fun ToggleCompose() {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primary)
            .width(40.dp)
            .height(200.dp)
    ) {

    }
}

@Preview
@Composable
fun ToggleComposePreview() {
    FluentReadTheme {
        ToggleCompose()
    }
}