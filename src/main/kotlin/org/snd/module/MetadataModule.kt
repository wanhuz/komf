package org.snd.module

import io.github.resilience4j.ratelimiter.RateLimiterConfig
import mu.KotlinLogging
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.snd.config.MetadataProvidersConfig
import org.snd.infra.HttpClient
import org.snd.metadata.Provider
import org.snd.metadata.mal.MalClient
import org.snd.metadata.mal.MalClientInterceptor
import org.snd.metadata.mal.MalMetadataProvider
import org.snd.metadata.mangaupdates.MangaUpdatesClient
import org.snd.metadata.mangaupdates.MangaUpdatesMetadataProvider
import org.snd.metadata.nautiljon.NautiljonClient
import org.snd.metadata.nautiljon.NautiljonMetadataProvider
import java.time.Duration


class MetadataModule(
    config: MetadataProvidersConfig,
    jsonModule: JsonModule
) {
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor { message ->
            KotlinLogging.logger {}.debug { message }
        }.setLevel(HttpLoggingInterceptor.Level.BASIC))
        .build()

    private val malHttpClient = config.mal.let {
        if (it.enabled)
            HttpClient(
                client = okHttpClient.newBuilder()
                    .addInterceptor(MalClientInterceptor(it.clientId))
                    .build(),
                name = "MAL",
                rateLimiterConfig = RateLimiterConfig.custom()
                    .limitRefreshPeriod(Duration.ofSeconds(5))
                    .limitForPeriod(10)
                    .timeoutDuration(Duration.ofSeconds(5))
                    .build()
            )
        else null
    }

    private val malClient = malHttpClient?.let { MalClient(client = it, moshi = jsonModule.moshi) }

    private val malMetadataProvider = malClient?.let { MalMetadataProvider(it) }

    private val mangaUpdatesClient = config.mangaUpdates.let {
        if (it.enabled)
            MangaUpdatesClient(
                HttpClient(
                    client = okHttpClient.newBuilder().build(),
                    name = "MangaUpdates",
                    rateLimiterConfig = RateLimiterConfig.custom()
                        .limitRefreshPeriod(Duration.ofSeconds(5))
                        .limitForPeriod(5)
                        .timeoutDuration(Duration.ofSeconds(5))
                        .build()
                )
            )
        else null
    }
    private val mangaUpdatesMetadataProvider = mangaUpdatesClient?.let { MangaUpdatesMetadataProvider(it) }

    private val nautiljonMetadataProvider = config.nautiljon.let {
        if (it.enabled) {
            NautiljonMetadataProvider(
                NautiljonClient(
                    HttpClient(
                        client = okHttpClient.newBuilder().build(),
                        name = "nautiljon",
                        rateLimiterConfig = RateLimiterConfig.custom()
                            .limitRefreshPeriod(Duration.ofSeconds(5))
                            .limitForPeriod(3)
                            .timeoutDuration(Duration.ofSeconds(5))
                            .build()
                    )
                ),
                it.fetchBookMetadata
            )
        } else null
    }

    val metadataProviders = run {
        val malPriority = config.mal.priority
        val mangaUpdatesPriority = config.mangaUpdates.priority
        val nautiljonPriority = config.nautiljon.priority

        val malProvider = malMetadataProvider?.let { Provider.MAL to (it to malPriority) }
        val mangaUpdatesProvider = mangaUpdatesMetadataProvider?.let { Provider.MANGA_UPDATES to (it to mangaUpdatesPriority) }
        val nautiljonProvider = nautiljonMetadataProvider?.let { Provider.NAUTILJON to (it to nautiljonPriority) }

        sequenceOf(malProvider, mangaUpdatesProvider, nautiljonProvider).filterNotNull()
            .sortedBy { it.second.second }
            .map { it.first to it.second.first }
            .toMap()
    }
}
