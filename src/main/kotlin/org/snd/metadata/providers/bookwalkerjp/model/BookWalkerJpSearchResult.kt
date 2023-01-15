package org.snd.metadata.providers.bookwalkerjp.model

import org.snd.metadata.model.Provider.BOOK_WALKER
import org.snd.metadata.model.SeriesSearchResult

data class BookWalkerJpSearchResult(
    val seriesId: BookWalkerJpSeriesId?,
    val bookId: BookWalkerJpBookId?,
    val seriesName: String,
    val imageUrl: String?,
)

fun BookWalkerJpSearchResult.toSeriesSearchResult(seriesId: BookWalkerJpSeriesId): SeriesSearchResult {
    return SeriesSearchResult(
        imageUrl = imageUrl,
        title = seriesName,
        provider = BOOK_WALKER,
        resultId = seriesId.id
    )
}
