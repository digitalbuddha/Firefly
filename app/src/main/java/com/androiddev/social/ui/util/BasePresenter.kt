package com.androiddev.social.ui.util

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

interface BasePresenter

abstract class Presenter<Event, Model, Effect>(
    initialState: Model,
) : BasePresenter {
    var model: Model by mutableStateOf(initialState)

    val events: MutableSharedFlow<Event> =
        MutableSharedFlow(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    val effects: MutableSharedFlow<Effect> = MutableSharedFlow(extraBufferCapacity = 1)

    fun handle(event:Event) = events.tryEmit(event)
    suspend fun start() {
        events.collect {
            eventHandler(it)
        }
    }

    abstract suspend fun eventHandler(event: Event)


}