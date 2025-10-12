package com.example.fluentread.service.mlk

sealed class DecoratorListener{
    object OnStart: DecoratorListener()
    object OnStop: DecoratorListener()
    data class OnError(val error: String): DecoratorListener()
}