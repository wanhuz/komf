@file:OptIn(ExperimentalSerializationApi::class)

package org.snd.config

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.EncodeDefault.Mode.NEVER
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import org.snd.mediaserver.UpdateMode
import org.snd.mediaserver.UpdateMode.API
import org.snd.metadata.NameMatchingMode
import org.snd.metadata.NameMatchingMode.CLOSEST_MATCH
import org.snd.metadata.model.ReadingDirection
import org.snd.metadata.model.TitleType

@Serializable
data class AppConfig(
    @EncodeDefault(NEVER) val komga: KomgaConfig = KomgaConfig(),
    @EncodeDefault(NEVER) val kavita: KavitaConfig = KavitaConfig(),
    @EncodeDefault(NEVER) val discord: DiscordConfig = DiscordConfig(),
    @EncodeDefault(NEVER) val database: DatabaseConfig = DatabaseConfig(),
    val metadataProviders: MetadataProvidersConfig = MetadataProvidersConfig(),
    val server: ServerConfig = ServerConfig(),
    val logLevel: String = "INFO"
)

@Serializable
data class KomgaConfig(
    val baseUri: String = "http://localhost:8080",
    val komgaUser: String = "admin@example.org",
    val komgaPassword: String = "admin",
    val eventListener: EventListenerConfig = EventListenerConfig(),
    val notifications: NotificationConfig = NotificationConfig(),
    val metadataUpdate: MetadataUpdateConfig = MetadataUpdateConfig(),
    val aggregateMetadata: Boolean = false,
)

@Serializable
data class KavitaConfig(
    val baseUri: String = "http://localhost:5000",
    val apiKey: String = "",
    val eventListener: EventListenerConfig = EventListenerConfig(enabled = false),
    val notifications: NotificationConfig = NotificationConfig(),
    val metadataUpdate: MetadataUpdateConfig = MetadataUpdateConfig(),
    val aggregateMetadata: Boolean = false,
)

@Serializable
data class NotificationConfig(
    val libraries: Collection<String> = emptyList()
)

@Serializable
data class MetadataUpdateConfig(
    val bookThumbnails: Boolean = false,
    val seriesThumbnails: Boolean = true,
    val seriesTitle: Boolean = false,
    val titleType: TitleType = TitleType.LOCALIZED,
    val readingDirectionValue: ReadingDirection? = null,
    val languageValue: String? = null,
    val orderBooks: Boolean = false,
    val modes: Set<UpdateMode> = setOf(API),
)

@Serializable
data class DatabaseConfig(
    val file: String = "./database.sqlite"
)

@Serializable
data class EventListenerConfig(
    val enabled: Boolean = false,
    val libraries: Collection<String> = emptyList()
)

@Serializable
data class MetadataProvidersConfig(
    @EncodeDefault(NEVER) val malClientId: String = "",
    val nameMatchingMode: NameMatchingMode = CLOSEST_MATCH,
    val defaultProviders: ProvidersConfig = ProvidersConfig(),
    @EncodeDefault(NEVER) val libraryProviders: Map<String, ProvidersConfig> = emptyMap(),

    @Deprecated("moved to default providers config")
    @EncodeDefault(NEVER) val mangaUpdates: ProviderConfig? = null,

    @Deprecated("moved to default providers config")
    @EncodeDefault(NEVER) val mal: ProviderConfig? = null,

    @Deprecated("moved to default providers config")
    @EncodeDefault(NEVER) val nautiljon: ProviderConfig? = null,

    @Deprecated("moved to default providers config")
    @EncodeDefault(NEVER) val aniList: ProviderConfig? = null,

    @Deprecated("moved to default providers config")
    @EncodeDefault(NEVER) val yenPress: ProviderConfig? = null,

    @Deprecated("moved to default providers config")
    @EncodeDefault(NEVER) val kodansha: ProviderConfig? = null,

    @Deprecated("moved to default providers config")
    @EncodeDefault(NEVER) val viz: ProviderConfig? = null,

    @Deprecated("moved to default providers config")
    @EncodeDefault(NEVER) val bookWalker: ProviderConfig? = null,

    @Deprecated("moved to default providers config")
    @EncodeDefault(NEVER) val bookWalkerJp: ProviderConfig? = null,
)

@Serializable
data class ProvidersConfig(
    val mangaUpdates: ProviderConfig = ProviderConfig(),
    val mal: ProviderConfig = ProviderConfig(),
    val nautiljon: ProviderConfig = ProviderConfig(),
    val aniList: ProviderConfig = ProviderConfig(),
    val yenPress: ProviderConfig = ProviderConfig(),
    val kodansha: ProviderConfig = ProviderConfig(),
    val viz: ProviderConfig = ProviderConfig(),
    val bookWalker: ProviderConfig = ProviderConfig(),
    val bookWalkerJp: ProviderConfig = ProviderConfig(),
)

@Serializable
data class ProviderConfig(
    @Deprecated("moved to separate config")
    @EncodeDefault(NEVER) val clientId: String = "",
    val priority: Int = 10,
    val enabled: Boolean = false,
    @EncodeDefault(NEVER) val seriesMetadata: SeriesMetadataConfig = SeriesMetadataConfig(),
    @EncodeDefault(NEVER) val bookMetadata: BookMetadataConfig = BookMetadataConfig(),
    @EncodeDefault(NEVER) val nameMatchingMode: NameMatchingMode? = null,
)

@Serializable
data class DiscordConfig(
    val webhooks: Collection<String>? = null,
    val seriesCover: Boolean = false,
    val imgurClientId: String? = null,
    val templatesDirectory: String = "./",
)

@Serializable
data class ServerConfig(
    val port: Int = 8085
)

@Serializable
data class SeriesMetadataConfig(
    @EncodeDefault(NEVER) val status: Boolean = true,
    @EncodeDefault(NEVER) val title: Boolean = true,
    @EncodeDefault(NEVER) val titleSort: Boolean = true,
    @EncodeDefault(NEVER) val summary: Boolean = true,
    @EncodeDefault(NEVER) val publisher: Boolean = true,
    @EncodeDefault(NEVER) val readingDirection: Boolean = true,
    @EncodeDefault(NEVER) val ageRating: Boolean = true,
    @EncodeDefault(NEVER) val language: Boolean = true,
    @EncodeDefault(NEVER) val genres: Boolean = true,
    @EncodeDefault(NEVER) val tags: Boolean = true,
    @EncodeDefault(NEVER) val totalBookCount: Boolean = true,
    @EncodeDefault(NEVER) val authors: Boolean = true,
    @EncodeDefault(NEVER) val releaseDate: Boolean = true,
    @EncodeDefault(NEVER) val thumbnail: Boolean = true,
    @EncodeDefault(NEVER) val books: Boolean = true,

    @EncodeDefault(NEVER) val useOriginalPublisher: Boolean = false,
    @EncodeDefault(NEVER) val originalPublisherTagName: String? = null,
    @EncodeDefault(NEVER) val englishPublisherTagName: String? = null,
    @EncodeDefault(NEVER) val frenchPublisherTagName: String? = null,
)

@Serializable
data class BookMetadataConfig(
    @EncodeDefault(NEVER) val title: Boolean = true,
    @EncodeDefault(NEVER) val summary: Boolean = true,
    @EncodeDefault(NEVER) val number: Boolean = true,
    @EncodeDefault(NEVER) val numberSort: Boolean = true,
    @EncodeDefault(NEVER) val releaseDate: Boolean = true,
    @EncodeDefault(NEVER) val authors: Boolean = true,
    @EncodeDefault(NEVER) val tags: Boolean = true,
    @EncodeDefault(NEVER) val isbn: Boolean = true,
    @EncodeDefault(NEVER) val links: Boolean = true,
    @EncodeDefault(NEVER) val thumbnail: Boolean = true,
)
