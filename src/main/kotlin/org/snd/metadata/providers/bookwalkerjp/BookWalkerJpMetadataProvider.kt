package org.snd.metadata.providers.bookwalkerjp

import okhttp3.HttpUrl.Companion.toHttpUrl
import org.snd.metadata.MetadataProvider
import org.snd.metadata.NameSimilarityMatcher
import org.snd.metadata.model.Image
import org.snd.metadata.model.Provider
import org.snd.metadata.model.Provider.BOOK_WALKER
import org.snd.metadata.model.ProviderBookId
import org.snd.metadata.model.ProviderBookMetadata
import org.snd.metadata.model.ProviderSeriesId
import org.snd.metadata.model.ProviderSeriesMetadata
import org.snd.metadata.model.SeriesSearchResult
import org.snd.metadata.providers.bookwalkerjp.model.BookWalkerJpBook
import org.snd.metadata.providers.bookwalkerjp.model.BookWalkerJpBookId
import org.snd.metadata.providers.bookwalkerjp.model.BookWalkerJpSearchResult
import org.snd.metadata.providers.bookwalkerjp.model.BookWalkerJpSeriesBook
import org.snd.metadata.providers.bookwalkerjp.model.BookWalkerJpSeriesId
import org.snd.metadata.providers.bookwalkerjp.model.toSeriesSearchResult

class BookWalkerJpMetadataProvider(
    private val client: BookWalkerJpClient,
    private val metadataMapper: BookWalkerJpMapper,
    private val nameMatcher: NameSimilarityMatcher,
) : MetadataProvider {
    override fun providerName(): Provider = BOOK_WALKER

    override fun getSeriesMetadata(seriesId: ProviderSeriesId): ProviderSeriesMetadata {
        val books = getAllBooks(BookWalkerJpSeriesId(seriesId.id))
        val firstBook = getFirstBook(books)
        val thumbnail = getThumbnail(firstBook.imageUrl)
        return metadataMapper.toSeriesMetadata(BookWalkerJpSeriesId(seriesId.id), firstBook, books, thumbnail)
    }

    override fun getBookMetadata(seriesId: ProviderSeriesId, bookId: ProviderBookId): ProviderBookMetadata {
        val bookMetadata = client.getBook(BookWalkerJpBookId(bookId.id))
        val thumbnail = getThumbnail(bookMetadata.imageUrl)

        return metadataMapper.toBookMetadata(bookMetadata, thumbnail)
    }

    override fun searchSeries(seriesName: String, limit: Int): Collection<SeriesSearchResult> {
        val searchResults = client.searchSeries(seriesName.take(100)).take(limit)
        return searchResults.mapNotNull {
            getSeriesId(it)?.let { seriesId -> it.toSeriesSearchResult(seriesId) }
        }
    }

    override fun matchSeriesMetadata(seriesName: String): ProviderSeriesMetadata? {
        val searchResults = client.searchSeries(seriesName.take(100))

        return searchResults
            .firstOrNull { nameMatcher.matches(seriesName, it.seriesName) }
            ?.let {
                getSeriesId(it)?.let { seriesId ->
                    val books = getAllBooks(seriesId)
                    val firstBook = getFirstBook(books)
                    val thumbnail = getThumbnail(firstBook.imageUrl)
                    metadataMapper.toSeriesMetadata(seriesId, firstBook, books, thumbnail)
                }
            }
    }

    private fun getSeriesId(searchResult: BookWalkerJpSearchResult): BookWalkerJpSeriesId? {
        return searchResult.seriesId ?: searchResult.bookId?.let { client.getBook(it).seriesId }
    }

    private fun getThumbnail(url: String?): Image? = url?.toHttpUrl()?.let { client.getThumbnail(it) }

    private fun getFirstBook(books: Collection<BookWalkerJpSeriesBook>): BookWalkerJpBook {
        val firstBook = books.sortedWith(compareBy(nullsLast()) { it.number?.start }).first()
        return client.getBook(firstBook.id)
    }

    private fun getAllBooks(series: BookWalkerJpSeriesId): Collection<BookWalkerJpSeriesBook> {
        return generateSequence(client.getSeriesBooks(series, 1)) {
            if (it.page == it.totalPages) null
            else client.getSeriesBooks(series, it.page + 1)
        }.flatMap { it.books }.toList()
    }
}

