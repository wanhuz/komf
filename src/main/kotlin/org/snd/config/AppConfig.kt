package org.snd.config

import kotlinx.serialization.Serializable
import org.snd.metadata.model.SeriesMetadata

@Serializable
data class AppConfig(
    val metadataProviders: MetadataProvidersConfig = MetadataProvidersConfig(),
    val komga: KomgaConfig = KomgaConfig(),
    val discord: DiscordConfig = DiscordConfig(),
    val database: DatabaseConfig = DatabaseConfig(),
    val server: ServerConfig = ServerConfig(),
    val logLevel: String = "INFO"
)

@Serializable
data class KomgaConfig(
    val baseUri: String = "http://localhost:8080",
    val komgaUser: String = "admin@example.org",
    val komgaPassword: String = "admin",
    val eventListener: KomgaEventListenerConfig = KomgaEventListenerConfig(),
    val metadataUpdate: MetadataUpdateConfig = MetadataUpdateConfig(),
    @Deprecated("moved to its own config")
    val webhooks: Collection<String>? = null,
    val aggregateMetadata: Boolean = false
)

@Serializable
data class MetadataUpdateConfig(
    val bookThumbnails: Boolean = false,
    val seriesThumbnails: Boolean = true,
    val seriesTitle: Boolean = false,
    val readingDirectionValue: SeriesMetadata.ReadingDirection? = null
)

@Serializable
data class DatabaseConfig(
    val file: String = "./database.sqlite"
)

@Serializable
data class KomgaEventListenerConfig(
    val enabled: Boolean = true,
    val libraries: Collection<String> = emptyList()
)

@Serializable
data class MetadataProvidersConfig(
    val mal: MalConfig = MalConfig(),
    val mangaUpdates: MangaUpdatesConfig = MangaUpdatesConfig(),
    val nautiljon: NautiljonConfig = NautiljonConfig(),
    val aniList: AniListConfig = AniListConfig(),
    val yenPress: YenPressConfig = YenPressConfig(),
    val kodansha: KodanshaConfig = KodanshaConfig(),
)

@Serializable
data class MangaUpdatesConfig(
    val priority: Int = 10,
    val enabled: Boolean = true,
    val seriesMetadata: SeriesMetadataConfig = SeriesMetadataConfig(),
    val bookMetadata: BookMetadataConfig = BookMetadataConfig(),
)

@Serializable
data class MalConfig(
    val clientId: String = "",
    val priority: Int = 20,
    val enabled: Boolean = false,
    val seriesMetadata: SeriesMetadataConfig = SeriesMetadataConfig(),
    val bookMetadata: BookMetadataConfig = BookMetadataConfig(),
)

@Serializable
data class NautiljonConfig(
    val priority: Int = 30,
    val enabled: Boolean = false,
    @Deprecated("no longer used")
    val fetchBookMetadata: Boolean? = null,
    val useOriginalPublisher: Boolean = false,
    val originalPublisherTag: String? = null,
    val frenchPublisherTag: String? = null,
    val seriesMetadata: SeriesMetadataConfig = SeriesMetadataConfig(),
    val bookMetadata: BookMetadataConfig = BookMetadataConfig(),
)

@Serializable
data class AniListConfig(
    val priority: Int = 40,
    val enabled: Boolean = false,
    val seriesMetadata: SeriesMetadataConfig = SeriesMetadataConfig(),
    val bookMetadata: BookMetadataConfig = BookMetadataConfig(),
)

@Serializable
data class YenPressConfig(
    val priority: Int = 50,
    val enabled: Boolean = false,
    val seriesMetadata: SeriesMetadataConfig = SeriesMetadataConfig(),
    val bookMetadata: BookMetadataConfig = BookMetadataConfig(),
)

@Serializable
data class KodanshaConfig(
    val priority: Int = 60,
    val enabled: Boolean = false,
    val seriesMetadata: SeriesMetadataConfig = SeriesMetadataConfig(),
    val bookMetadata: BookMetadataConfig = BookMetadataConfig(),
)

@Serializable
data class DiscordConfig(
    val webhooks: Collection<String>? = null,
    val templatesDirectory: String = "./",
)

@Serializable
data class ServerConfig(
    val port: Int = 8085
)

@Serializable
data class SeriesMetadataConfig(
    val status: Boolean = true,
    val title: Boolean = true,
    val titleSort: Boolean = true,
    val summary: Boolean = true,
    val publisher: Boolean = true,
    val readingDirection: Boolean = true,
    val ageRating: Boolean = true,
    val language: Boolean = true,
    val genres: Boolean = true,
    val tags: Boolean = true,
    val totalBookCount: Boolean = true,
    val authors: Boolean = true,
    val thumbnail: Boolean = true,
    val books: Boolean = true
)

@Serializable
data class BookMetadataConfig(
    val title: Boolean = true,
    val summary: Boolean = true,
    val number: Boolean = true,
    val numberSort: Boolean = true,
    val releaseDate: Boolean = true,
    val authors: Boolean = true,
    val tags: Boolean = true,
    val isbn: Boolean = true,
    val links: Boolean = true,
    val thumbnail: Boolean = true,
)
