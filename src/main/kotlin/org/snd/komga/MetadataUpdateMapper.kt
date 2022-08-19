package org.snd.komga

import org.snd.config.MetadataUpdateConfig
import org.snd.komga.model.dto.KomgaAuthor
import org.snd.komga.model.dto.KomgaBookMetadata
import org.snd.komga.model.dto.KomgaBookMetadataUpdate
import org.snd.komga.model.dto.KomgaSeriesMetadata
import org.snd.komga.model.dto.KomgaSeriesMetadataUpdate
import org.snd.komga.model.dto.KomgaWebLink
import org.snd.metadata.model.BookMetadata
import org.snd.metadata.model.SeriesMetadata

class MetadataUpdateMapper(
    private val metadataUpdateConfig: MetadataUpdateConfig,
) {

    fun toBookMetadataUpdate(bookMetadata: BookMetadata?, seriesMetadata: SeriesMetadata, komgaMetadata: KomgaBookMetadata): KomgaBookMetadataUpdate =
        with(komgaMetadata) {
            val authors = (bookMetadata?.authors ?: seriesMetadata.authors)?.map { author -> KomgaAuthor(author.name, author.role) }
            KomgaBookMetadataUpdate(
                summary = getIfNotLocked(bookMetadata?.summary, summaryLock),
                releaseDate = getIfNotLocked(bookMetadata?.releaseDate, releaseDateLock),
                authors = getIfNotLocked(authors, authorsLock),
                tags = getIfNotLocked(bookMetadata?.tags, tagsLock),
                isbn = getIfNotLocked(bookMetadata?.isbn, isbnLock),
                links = getIfNotLocked(bookMetadata?.links?.map { KomgaWebLink(it.label, it.url) }, linksLock)
            )
        }

    fun toSeriesMetadataUpdate(patch: SeriesMetadata, metadata: KomgaSeriesMetadata): KomgaSeriesMetadataUpdate =
        with(metadata) {
            KomgaSeriesMetadataUpdate(
                status = getIfNotLocked(patch.status?.toString(), statusLock),
                title = if (metadataUpdateConfig.seriesTitle) getIfNotLocked(patch.title, titleLock) else null,
                titleSort = if (metadataUpdateConfig.seriesTitle) getIfNotLocked(patch.titleSort, titleSortLock) else null,
                summary = getIfNotLocked(patch.summary, summaryLock),
                publisher = getIfNotLocked(patch.publisher, publisherLock),
                readingDirection = getIfNotLocked(patch.readingDirection?.toString(), readingDirectionLock),
                ageRating = getIfNotLocked(patch.ageRating, ageRatingLock),
                language = getIfNotLocked(patch.language, languageLock),
                genres = getIfNotLocked(patch.genres, genresLock),
                tags = getIfNotLocked(patch.tags, tagsLock),
                totalBookCount = getIfNotLocked(patch.totalBookCount, totalBookCountLock),
            )
        }

    private fun <T> getIfNotLocked(patched: T?, lock: Boolean): T? =
        if (patched != null && !lock) patched
        else null
}
