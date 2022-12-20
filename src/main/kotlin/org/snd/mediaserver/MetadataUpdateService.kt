package org.snd.mediaserver

import org.snd.config.MetadataUpdateConfig
import org.snd.mediaserver.UpdateMode.API
import org.snd.mediaserver.UpdateMode.COMIC_INFO
import org.snd.mediaserver.model.*
import org.snd.mediaserver.repository.BookThumbnail
import org.snd.mediaserver.repository.BookThumbnailsRepository
import org.snd.mediaserver.repository.SeriesThumbnail
import org.snd.mediaserver.repository.SeriesThumbnailsRepository
import org.snd.metadata.BookNameParser
import org.snd.metadata.comicinfo.ComicInfoWriter
import org.snd.metadata.model.BookMetadata
import org.snd.metadata.model.Image
import org.snd.metadata.model.SeriesMetadata
import java.nio.file.Path

class MetadataUpdateService(
    private val mediaServerClient: MediaServerClient,
    private val seriesThumbnailsRepository: SeriesThumbnailsRepository,
    private val bookThumbnailsRepository: BookThumbnailsRepository,
    private val metadataUpdateConfig: MetadataUpdateConfig,
    private val metadataUpdateMapper: MetadataUpdateMapper,
    private val comicInfoWriter: ComicInfoWriter,
) {
    private val requireMetadataRefresh = setOf(COMIC_INFO)

    fun updateMetadata(series: MediaServerSeries, metadata: SeriesAndBookMetadata) {
        if (metadata.seriesMetadata != null) updateSeriesMetadata(series, metadata.seriesMetadata)
        updateBookMetadata(metadata.bookMetadata, metadata.seriesMetadata)

        if (metadataUpdateConfig.modes.any { it in requireMetadataRefresh })
            mediaServerClient.refreshMetadata(series.id)
    }

    private fun updateSeriesMetadata(series: MediaServerSeries, metadata: SeriesMetadata) {
        metadataUpdateConfig.modes.forEach {
            when (it) {
                API -> {
                    val metadataUpdate = metadataUpdateMapper.toSeriesMetadataUpdate(metadata, series.metadata)
                    mediaServerClient.updateSeriesMetadata(series.id, metadataUpdate)
                }

                COMIC_INFO -> {}
            }
        }

        val newThumbnail = if (metadataUpdateConfig.seriesThumbnails) metadata.thumbnail else null
        val thumbnailId = replaceSeriesThumbnail(series.id, newThumbnail)

        if (thumbnailId == null) {
            seriesThumbnailsRepository.delete(series.id)
        } else {
            seriesThumbnailsRepository.save(
                SeriesThumbnail(
                    seriesId = series.id,
                    thumbnailId = thumbnailId,
                )
            )
        }
    }

    private fun updateBookMetadata(bookMetadata: Map<MediaServerBook, BookMetadata?>, seriesMetadata: SeriesMetadata?) {
        val bookIdToWriteSeriesMetadata = bookToWriteSeriesMetadata(bookMetadata)
        bookMetadata.forEach { (book, metadata) ->
            updateBookMetadata(book, metadata, seriesMetadata, book.id == bookIdToWriteSeriesMetadata)
        }
    }

    private fun updateBookMetadata(
        book: MediaServerBook,
        metadata: BookMetadata?,
        seriesMeta: SeriesMetadata?,
        writeSeriesMetadata: Boolean
    ) {
        metadataUpdateConfig.modes.forEach { mode ->
            when (mode) {
                API -> metadataUpdateMapper.toBookMetadataUpdate(metadata, seriesMeta, book)
                    .let { mediaServerClient.updateBookMetadata(book.id, it) }


                COMIC_INFO -> {
                    if (book.deleted.not()) {
                        if (writeSeriesMetadata) seriesMeta?.let {
                            val comicInfo = metadataUpdateMapper.toSeriesComicInfo(it)
                            comicInfoWriter.writeMetadata(Path.of(book.url), comicInfo)
                        }
                        else metadataUpdateMapper.toComicInfo(metadata, seriesMeta)
                            ?.let { comicInfoWriter.writeMetadata(Path.of(book.url), it) }
                    }
                }
            }
        }

        val newThumbnail = if (metadataUpdateConfig.bookThumbnails) metadata?.thumbnail else null
        val thumbnailId = replaceBookThumbnail(book.id, newThumbnail)

        if (thumbnailId == null) {
            bookThumbnailsRepository.delete(book.id)
        } else {
            bookThumbnailsRepository.save(
                BookThumbnail(
                    seriesId = book.seriesId,
                    bookId = book.id,
                    thumbnailId = thumbnailId,
                )
            )
        }
    }

    private fun replaceBookThumbnail(bookId: MediaServerBookId, thumbnail: Image?): MediaServerThumbnailId? {
        val existingMatch = bookThumbnailsRepository.findFor(bookId)
        val thumbnails = mediaServerClient.getBookThumbnails(bookId)

        val uploadedThumbnail = thumbnail?.let {
            mediaServerClient.uploadBookThumbnail(
                bookId = bookId,
                thumbnail = thumbnail,
                selected = thumbnails.all { it.type == "GENERATED" || it.id == existingMatch?.thumbnailId }
            )
        }

        existingMatch?.thumbnailId?.let { thumb ->
            if (thumbnails.any { it.id == thumb }) {
                mediaServerClient.deleteBookThumbnail(bookId, thumb)
            }
        }

        return uploadedThumbnail?.id
    }

    private fun replaceSeriesThumbnail(seriesId: MediaServerSeriesId, thumbnail: Image?): MediaServerThumbnailId? {
        val matchedSeries = seriesThumbnailsRepository.findFor(seriesId)
        val thumbnails = mediaServerClient.getSeriesThumbnails(seriesId)

        val uploadedThumbnail = thumbnail?.let {
            mediaServerClient.uploadSeriesThumbnail(
                seriesId = seriesId,
                thumbnail = thumbnail,
                selected = thumbnails.isEmpty()
            )
        }

        matchedSeries?.thumbnailId?.let { thumb ->
            if (thumbnails.any { it.id == thumb }) {
                mediaServerClient.deleteSeriesThumbnail(seriesId, thumb)
            }
        }

        return uploadedThumbnail?.id
    }

    fun resetLibraryMetadata(libraryId: MediaServerLibraryId) {
        mediaServerClient.getSeries(libraryId).forEach { resetSeriesMetadata(it) }
    }

    fun resetSeriesMetadata(seriesId: MediaServerSeriesId) {
        val series = mediaServerClient.getSeries(seriesId)
        resetSeriesMetadata(series)
    }


    private fun resetSeriesMetadata(series: MediaServerSeries) {
        mediaServerClient.resetSeriesMetadata(series.id, series.name)

        mediaServerClient.getBooks(series.id)
            .forEach { resetBookMetadata(it) }

        replaceSeriesThumbnail(series.id, null)
        seriesThumbnailsRepository.delete(series.id)
    }

    private fun resetBookMetadata(book: MediaServerBook) {
        mediaServerClient.resetBookMetadata(book.id, book.name)

        replaceBookThumbnail(book.id, null)
        bookThumbnailsRepository.delete(book.id)
    }

    private fun bookToWriteSeriesMetadata(bookMetadata: Map<MediaServerBook, BookMetadata?>): MediaServerBookId? {
        return if (metadataUpdateConfig.modes.any { it == COMIC_INFO }
            && bookMetadata.all { it.value == null }) {
            val books = bookMetadata.keys.sortedBy { it.name.lowercase() }
            return books.asSequence()
                .mapNotNull { book -> BookNameParser.getVolumes(book.name)?.let { book to it } }
                .map { (book, number) -> book to number.start }
                .filter { (_, number) -> number.toInt() == 1 }
                .map { (book, _) -> book.id }
                .firstOrNull() ?: books[0].id

        } else null
    }
}