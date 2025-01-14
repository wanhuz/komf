package org.snd.mediaserver.komga.model.dto

import com.squareup.moshi.JsonClass
import org.snd.mediaserver.model.MediaServerAlternativeTitle
import org.snd.mediaserver.model.MediaServerLibraryId
import org.snd.mediaserver.model.MediaServerSeries
import org.snd.mediaserver.model.MediaServerSeriesId
import org.snd.mediaserver.model.MediaServerSeriesMetadata
import org.snd.mediaserver.model.MediaServerSeriesSearch
import org.snd.metadata.model.ReadingDirection.valueOf
import org.snd.metadata.model.SeriesStatus
import org.snd.metadata.model.WebLink

@JsonClass(generateAdapter = true)
data class KomgaSeries(
    val id: String,
    val libraryId: String,
    val name: String,
    val url: String,
    val booksCount: Int,
    val booksReadCount: Int,
    val booksUnreadCount: Int,
    val booksInProgressCount: Int,
    val metadata: KomgaSeriesMetadata,
    val deleted: Boolean,
) {
    fun seriesId(): KomgaSeriesId = KomgaSeriesId(id)
}

@JsonClass(generateAdapter = true)
data class KomgaSeriesMetadata(
    val status: String,
    val statusLock: Boolean,
    val title: String,
    val alternateTitles: Collection<KomgaAlternativeTitle>,
    val alternateTitlesLock: Boolean,
    val titleLock: Boolean,
    val titleSort: String,
    val titleSortLock: Boolean,
    val summary: String,
    val summaryLock: Boolean,
    @ReadingDirection
    val readingDirection: String?,
    val readingDirectionLock: Boolean,
    val publisher: String,
    val publisherLock: Boolean,
    val ageRating: Int?,
    val ageRatingLock: Boolean,
    val language: String,
    val languageLock: Boolean,
    val genres: Set<String>,
    val genresLock: Boolean,
    val tags: Set<String>,
    val tagsLock: Boolean,
    val totalBookCount: Int?,
    val totalBookCountLock: Boolean,
    val links: Collection<KomgaWebLink>,
    val linksLock: Boolean,
)

fun KomgaSeries.mediaServerSeries(): MediaServerSeries {
    return MediaServerSeries(
        id = MediaServerSeriesId(id),
        libraryId = MediaServerLibraryId(libraryId),
        name = name,
        booksCount = booksCount,
        metadata = metadata.mediaServerSeriesMetadata(),
        url = url,
        deleted = deleted,
    )
}

fun KomgaSeries.mediaServerSeriesSearch(): MediaServerSeriesSearch {
    return MediaServerSeriesSearch(
        id = MediaServerSeriesId(id),
        libraryId = MediaServerLibraryId(libraryId),
        name = name,
    )
}

fun KomgaSeriesMetadata.mediaServerSeriesMetadata() = MediaServerSeriesMetadata(
    status = SeriesStatus.valueOf(status),
    title = title,
    titleSort = titleSort,
    alternativeTitles = alternateTitles.map { (label, title) -> MediaServerAlternativeTitle(label, title) },
    summary = summary,
    readingDirection = readingDirection?.let { valueOf(it) },
    publisher = publisher,
    alternativePublishers = emptySet(),
    ageRating = ageRating,
    language = language,
    genres = genres,
    tags = tags,
    totalBookCount = totalBookCount,
    authors = emptyList(), //TODO take authors from book metadata?,
    releaseYear = null, //TODO take from book metadata?,
    links = links.map { WebLink(it.label, it.url) },

    statusLock = statusLock,
    titleLock = titleLock,
    titleSortLock = titleSortLock,
    alternativeTitlesLock = alternateTitlesLock,
    summaryLock = summaryLock,
    readingDirectionLock = readingDirectionLock,
    publisherLock = publisherLock,
    ageRatingLock = ageRatingLock,
    languageLock = languageLock,
    genresLock = genresLock,
    tagsLock = tagsLock,
    totalBookCountLock = totalBookCountLock,
    authorsLock = false,
    releaseYearLock = false,
    linksLock = linksLock
)