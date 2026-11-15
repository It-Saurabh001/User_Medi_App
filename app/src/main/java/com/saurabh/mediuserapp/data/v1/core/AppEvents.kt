package com.saurabh.mediuserapp.data.v1.core

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed interface AppEvent {
    data class UpgradeRequired(val minVersion: String?) : AppEvent
    data object SessionInvalid : AppEvent
    data object AccountBlocked : AppEvent
    data object RateLimited : AppEvent
}

/** App-wide one-shot events raised by the network layer (v1 paths only). */
@Singleton
class AppEvents @Inject constructor() {
    private val _events = MutableSharedFlow<AppEvent>(replay = 0, extraBufferCapacity = 16)
    val events: SharedFlow<AppEvent> = _events.asSharedFlow()

    fun emit(event: AppEvent): Boolean = _events.tryEmit(event)
}
