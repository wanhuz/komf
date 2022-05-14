package org.snd.metadata.mangaupdates.model

import org.snd.metadata.Provider.MANGA_UPDATES
import org.snd.metadata.ProviderSeriesId
import org.snd.metadata.mangaupdates.model.Status.*
import org.snd.metadata.model.AuthorRole.*
import org.snd.metadata.model.SeriesMetadata
import org.snd.metadata.model.Thumbnail
import java.net.URI
import java.time.Year

data class Series(
    val id: String,
    val title: String,
    val description: String?,
    val type: Type?,
    val relatedSeries: Collection<RelatedSeries>,
    val associatedNames: Collection<String>,
    val status: Status?,
    val image: URI?,
    val genres: Collection<String>,
    val categories: Collection<Category>,
    val authors: Collection<Author>,
    val artists: Collection<Author>,
    val year: Year?,
    val originalPublisher: Publisher?,
    val englishPublishers: Collection<Publisher>
)

data class RelatedSeries(
    val id: String,
    val name: String,
    val relation: String?,
)

data class Category(
    val name: String,
    val score: Int
)

data class Author(
    val id: String?,
    val name: String
)

data class Publisher(
    val id: String?,
    val name: String
)

enum class Status {
    COMPLETE,
    ONGOING,
    CANCELLED,
    HIATUS,
}

enum class Type {
    MANGA,
    DOUJINSHI,
    MANHWA,
    MANHUA,
}

fun Series.toSeriesMetadata(thumbnail: Thumbnail? = null): SeriesMetadata {
    val status = when (status) {
        COMPLETE -> SeriesMetadata.Status.ENDED
        ONGOING -> SeriesMetadata.Status.ONGOING
        CANCELLED -> SeriesMetadata.Status.ABANDONED
        HIATUS -> SeriesMetadata.Status.HIATUS
        else -> SeriesMetadata.Status.ONGOING
    }

    val artistRoles = listOf(
        PENCILLER,
        INKER,
        COLORIST,
        LETTERER,
        COVER
    )

    val authors = authors.map { org.snd.metadata.model.Author(it.name, WRITER.name) } +
            artists.flatMap { artist ->
                artistRoles.map { role -> org.snd.metadata.model.Author(artist.name, role.name) }
            }

    val tags = categories.map { it.name }

    return SeriesMetadata(
        status = status,
        title = title,
        titleSort = title,
        summary = description ?: "",
        publisher = originalPublisher?.name ?: "",
        genres = genres,
        tags = tags,
        authors = authors,
        thumbnail = thumbnail,

        id = ProviderSeriesId(id),
        provider = MANGA_UPDATES
    )
}
