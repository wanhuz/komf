package org.snd.metadata

import org.snd.config.BookMetadataConfig
import org.snd.config.SeriesMetadataConfig
import org.snd.metadata.model.BookMetadata
import org.snd.metadata.model.ProviderBookMetadata
import org.snd.metadata.model.ProviderSeriesMetadata
import org.snd.metadata.model.SeriesMetadata
import org.snd.metadata.model.SeriesTitle

object MetadataConfigApplier {

    fun apply(providerMetadata: ProviderSeriesMetadata, config: SeriesMetadataConfig): ProviderSeriesMetadata {
        return with(providerMetadata) {
            ProviderSeriesMetadata(
                id = id,
                books = getIfEnabled(books, config.books) ?: emptyList(),
                metadata = SeriesMetadata(
                    status = getIfEnabled(metadata.status, config.status),
                    titles = seriesTitles(metadata.titles, config.title),
                    summary = getIfEnabled(metadata.summary, config.summary),
                    publisher = getIfEnabled(metadata.publisher, config.publisher),
                    alternativePublishers = getIfEnabled(metadata.alternativePublishers, config.publisher) ?: emptySet(),
                    readingDirection = getIfEnabled(metadata.readingDirection, config.readingDirection),
                    ageRating = getIfEnabled(metadata.ageRating, config.ageRating),
                    language = getIfEnabled(metadata.language, config.language),
                    genres = getIfEnabled(metadata.genres, config.genres) ?: emptyList(),
                    tags = getIfEnabled(metadata.tags, config.tags) ?: emptyList(),
                    totalBookCount = getIfEnabled(metadata.totalBookCount, config.totalBookCount),
                    authors = getIfEnabled(metadata.authors, config.authors) ?: emptyList(),
                    releaseDate = getIfEnabled(metadata.releaseDate, config.releaseDate),
                    thumbnail = getIfEnabled(metadata.thumbnail, config.thumbnail),
                    links = getIfEnabled(metadata.links, config.links) ?: emptyList()
                )
            )
        }
    }

    fun apply(providerMetadata: ProviderBookMetadata, config: BookMetadataConfig): ProviderBookMetadata {
        return with(providerMetadata) {
            ProviderBookMetadata(
                id = id,
                metadata = BookMetadata(
                    title = getIfEnabled(metadata.title, config.title),
                    summary = getIfEnabled(metadata.summary, config.summary),
                    number = getIfEnabled(metadata.number, config.number),
                    numberSort = getIfEnabled(metadata.numberSort, config.numberSort),
                    releaseDate = getIfEnabled(metadata.releaseDate, config.releaseDate),
                    authors = getIfEnabled(metadata.authors, config.authors) ?: emptyList(),
                    tags = getIfEnabled(metadata.tags, config.tags) ?: emptySet(),
                    isbn = getIfEnabled(metadata.isbn, config.isbn),
                    links = getIfEnabled(metadata.links, config.links) ?: emptyList(),
                    thumbnail = getIfEnabled(metadata.thumbnail, config.thumbnail),
                    chapters = metadata.chapters,
                    startChapter = metadata.startChapter,
                    endChapter = metadata.endChapter,
                )
            )
        }
    }

    private fun <T> getIfEnabled(value: T?, enabled: Boolean): T? =
        if (value != null && enabled) value
        else null

    private fun seriesTitles(titles: Collection<SeriesTitle>, enabled: Boolean): Collection<SeriesTitle> {
        return if (!enabled) {
            titles.map { it.copy(type = null) }
        } else titles

    }
}
