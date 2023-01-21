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
    @Deprecated("moved to MetadataUpdateConfig")
    @EncodeDefault(NEVER) val aggregateMetadata: Boolean? = null,
)

@Serializable
data class KavitaConfig(
    val baseUri: String = "http://localhost:5000",
    val apiKey: String = "",
    val eventListener: EventListenerConfig = EventListenerConfig(enabled = false),
    val notifications: NotificationConfig = NotificationConfig(),
    val metadataUpdate: MetadataUpdateConfig = MetadataUpdateConfig(),
    @Deprecated("moved to MetadataUpdateConfig")
    @EncodeDefault(NEVER) val aggregateMetadata: Boolean? = null,
)

@Serializable
data class NotificationConfig(
    val libraries: Collection<String> = emptyList()
)

@Serializable
data class MetadataUpdateConfig(
    @EncodeDefault(NEVER) val default: MetadataProcessingConfig = MetadataProcessingConfig(),
    @EncodeDefault(NEVER) val library: Map<String, MetadataProcessingConfig> = emptyMap(),

    @Deprecated("moved to default processing config")
    @EncodeDefault(NEVER) val bookThumbnails: Boolean? = null,
    @Deprecated("moved to default processing config")
    @EncodeDefault(NEVER) val seriesThumbnails: Boolean? = null,
    @Deprecated("moved to default processing config")
    @EncodeDefault(NEVER) val seriesTitle: Boolean? = null,
    @Deprecated("moved to default processing config")
    @EncodeDefault(NEVER) val titleType: TitleType? = null,
    @Deprecated("moved to default processing config")
    @EncodeDefault(NEVER) val readingDirectionValue: ReadingDirection? = null,
    @Deprecated("moved to default processing config")
    @EncodeDefault(NEVER) val languageValue: String? = null,
    @Deprecated("moved to default processing config")
    @EncodeDefault(NEVER) val orderBooks: Boolean? = null,
    @Deprecated("moved to default processing config")
    @EncodeDefault(NEVER) val modes: Set<UpdateMode>? = null,
)

@Serializable
data class MetadataProcessingConfig(
    val aggregate: Boolean = false,

    val bookCovers: Boolean = false,
    val seriesCovers: Boolean = false,
    val updateModes: Set<UpdateMode> = setOf(API),

    @EncodeDefault(NEVER) val postProcessing: MetadataPostProcessingConfig = MetadataPostProcessingConfig()
)

@Serializable
data class MetadataPostProcessingConfig(
    val seriesTitle: Boolean = false,
    val titleType: TitleType = TitleType.LOCALIZED,
    val alternativeSeriesTitles: Boolean = false,
    val orderBooks: Boolean = false,
    @EncodeDefault(NEVER) val readingDirectionValue: ReadingDirection? = null,
    @EncodeDefault(NEVER) val languageValue: String? = null,
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
    @EncodeDefault(NEVER) val nameMatchingMode: NameMatchingMode = CLOSEST_MATCH,
    @EncodeDefault(NEVER) val defaultProviders: ProvidersConfig = ProvidersConfig(),
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
)

@Serializable
data class ProvidersConfig(
    @EncodeDefault(NEVER) val mangaUpdates: ProviderConfig = ProviderConfig(),
    @EncodeDefault(NEVER) val mal: ProviderConfig = ProviderConfig(),
    @EncodeDefault(NEVER) val nautiljon: ProviderConfig = ProviderConfig(),
    @EncodeDefault(NEVER) val aniList: ProviderConfig = ProviderConfig(),
    @EncodeDefault(NEVER) val yenPress: ProviderConfig = ProviderConfig(),
    @EncodeDefault(NEVER) val kodansha: ProviderConfig = ProviderConfig(),
    @EncodeDefault(NEVER) val viz: ProviderConfig = ProviderConfig(),
    @EncodeDefault(NEVER) val bookWalker: ProviderConfig = ProviderConfig(),
)

@Serializable
data class ProviderConfig(
    @Deprecated("moved to separate config")
    @EncodeDefault(NEVER) val clientId: String = "",
    @EncodeDefault(NEVER) val priority: Int = 10,
    @EncodeDefault(NEVER) val enabled: Boolean = false,
    @EncodeDefault(NEVER) val seriesMetadata: SeriesMetadataConfig = SeriesMetadataConfig(),
    @EncodeDefault(NEVER) val bookMetadata: BookMetadataConfig = BookMetadataConfig(),
    @EncodeDefault(NEVER) val nameMatchingMode: NameMatchingMode? = null,
)

@Serializable
data class DiscordConfig(
    val webhooks: Collection<String>? = null,
    val seriesCover: Boolean = false,
    val imgurClientId: String? = null,
    @EncodeDefault(NEVER) val templatesDirectory: String = "./",
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
    @EncodeDefault(NEVER) val links: Boolean = true,

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
