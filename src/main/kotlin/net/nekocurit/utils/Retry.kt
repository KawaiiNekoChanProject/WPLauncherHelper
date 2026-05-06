package net.nekocurit.utils

import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

suspend fun <T> retry(limit: Int = 2, retryDelay: Duration = 0.seconds, block: suspend () -> T): T? {
    repeat(limit) {
        runCatching { return block() }
            .onFailure { delay(retryDelay) }
    }
    return null
}