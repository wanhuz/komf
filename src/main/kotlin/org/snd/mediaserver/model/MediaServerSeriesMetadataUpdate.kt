package org.snd.mediaserver.model

import org.snd.metadata.model.ReadingDirection
import org.snd.metadata.model.SeriesStatus

data class MediaServerSeriesMetadataUpdate(
    val status: SeriesStatus? = null,
    val title: String? = null,
    val titleSort: String? = null,
    val summary: String? = null,
    val readingDirection: ReadingDirection? = null,
    val publisher: String? = null,
    val alternativePublishers: Collection<String>? = null,
    val ageRating: Int? = null,
    val language: String? = null,
    val genres: Collection<String>? = null,
    val tags: Collection<String>? = null,
    val totalBookCount: Int? = null,
    val authors: Collection<MediaServerAuthor>? = null,

    val statusLock: Boolean? = null,
    val titleLock: Boolean? = null,
    val titleSortLock: Boolean? = null,
    val summaryLock: Boolean? = null,
    val readingDirectionLock: Boolean? = null,
    val publisherLock: Boolean? = null,
    val ageRatingLock: Boolean? = null,
    val languageLock: Boolean? = null,
    val genresLock: Boolean? = null,
    val tagsLock: Boolean? = null,
    val totalBookCountLock: Boolean? = null,
    val authorsLock: Boolean? = null,
)