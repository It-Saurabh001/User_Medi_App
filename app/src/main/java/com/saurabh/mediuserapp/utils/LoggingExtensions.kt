package com.saurabh.mediuserapp.utils

import com.saurabh.mediuserapp.common.ResultState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

/**
 * Extension functions to add logging to existing logic flows 
 * with minimal modification to the original code.
 */

/**
 * Logs the start, items, and completion of a Flow.
 */
fun <T> Flow<T>.logFlow(name: String, tag: String = AppLogger.TAG_VIEWMODEL): Flow<T> = this
    .onStart { AppLogger.d(tag, "Flow [$name] started") }
    .onEach { AppLogger.d(tag, "Flow [$name] emitted: ${it.toString().take(100)}") }
    .onCompletion { cause ->
        if (cause != null) AppLogger.e(tag, "Flow [$name] failed", cause)
        else AppLogger.d(tag, "Flow [$name] completed")
    }

/**
 * Logs ResultState transitions in ViewModels or Repositories.
 */
fun <T> ResultState<T>.logState(operation: String, tag: String = AppLogger.TAG_VIEWMODEL): ResultState<T> {
    when (this) {
        is ResultState.Loading -> AppLogger.d(tag, "State [$operation]: LOADING")
        is ResultState.Success -> AppLogger.d(tag, "State [$operation]: SUCCESS -> ${this.data.toString().take(100)}")
        is ResultState.Error -> AppLogger.e(tag, "State [$operation]: ERROR -> ${this.exception.message}")
    }
    return this
}

/**
 * Utility for logging actions in a chainable way.
 */
fun <T> T.logAction(message: String, tag: String = AppLogger.TAG_UI): T = this.also {
    AppLogger.d(tag, "$message: $it")
}

/**
 * Specifically for OTP logging in debug environments.
 */
fun String.logAsOTP(context: String): String = this.also {
    AppLogger.d(AppLogger.TAG_OTP, "DEBUG OTP [$context]: $it (Keep this private in production!)")
}
