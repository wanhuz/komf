package org.snd.metadata.providers.kodansha

import okhttp3.HttpUrl.Companion.toHttpUrl
import org.snd.metadata.MetadataProvider
import org.snd.metadata.NameSimilarityMatcher
import org.snd.metadata.model.*
import org.snd.metadata.providers.kodansha.model.*

class KodanshaMetadataProvider(
    private val client: KodanshaClient,
    private val metadataMapper: KodanshaMetadataMapper,
    private val nameMatcher: NameSimilarityMatcher,
) : MetadataProvider {

    override fun providerName(): Provider {
        return Provider.KODANSHA
    }

    override fun getSeriesMetadata(seriesId: ProviderSeriesId): ProviderSeriesMetadata {
        val series = getSeries(KodanshaSeriesId(seriesId.id))
        val thumbnail = getThumbnail(series.coverUrl)
        return metadataMapper.toSeriesMetadata(series, thumbnail)
    }

    override fun getBookMetadata(seriesId: ProviderSeriesId, bookId: ProviderBookId): ProviderBookMetadata {
        val bookMetadata = client.getBook(KodanshaBookId(bookId.id))
        val thumbnail = getThumbnail(bookMetadata.coverUrl)

        return metadataMapper.toBookMetadata(bookMetadata, thumbnail)
    }

    override fun searchSeries(seriesName: String, limit: Int): Collection<SeriesSearchResult> {
        val searchResults = client.searchSeries(seriesName.take(400)).take(limit)
        return searchResults.map { it.toSeriesSearchResult() }
    }

    override fun matchSeriesMetadata(seriesName: String): ProviderSeriesMetadata? {
        val searchResults = client.searchSeries(seriesName.take(400))

        return searchResults.firstOrNull { nameMatcher.matches(seriesName, it.title) }
            ?.let {
                val series = getSeries(it.seriesId)
                val thumbnail = getThumbnail(series.coverUrl)
                metadataMapper.toSeriesMetadata(series, thumbnail)
            }
    }

    private fun getThumbnail(url: String?): Image? = url?.toHttpUrl()?.let { client.getThumbnail(it) }

    private fun getSeries(seriesId: KodanshaSeriesId): KodanshaSeries {
        val series = client.getSeries(seriesId)
        return if (series.books.size == 30) {
            val allBooks = getAllBooks(series)
            series.copy(books = allBooks)
        } else series
    }

    private fun getAllBooks(series: KodanshaSeries): Collection<KodanshaSeriesBook> {
        return generateSequence(client.getAllSeriesBooks(series.id, 1)) {
            if (it.page == it.totalPages) null
            else client.getAllSeriesBooks(series.id, it.page + 1)
        }.flatMap { it.books }.toList()
    }
}
