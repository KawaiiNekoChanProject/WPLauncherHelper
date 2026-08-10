package net.nekocurit.utils

import io.ktor.client.*
import io.ktor.client.engine.*

fun <T : HttpClientEngineConfig> newPrivateHttpClient(
    engine: HttpClientEngineFactory<T>,
    engineConfig: T.() -> Unit = { },
    internal: HttpClientConfig<T>.() -> Unit = { }
) = HttpClient(engine) {
    engine {
        engineConfig()
    }
    internal()

}

fun newPrivateHttpClient(
    internal: HttpClientConfig<*>.() -> Unit = { }
) = HttpClient {
    internal()
}