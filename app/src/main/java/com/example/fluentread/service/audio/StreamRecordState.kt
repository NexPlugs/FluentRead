package com.example.fluentread.service.audio


data class StreamRecordState(
    val appMediaRecorder: AppMediaRecorder,
    val what: Int,
    val extra: Int
)