package org.snd.metadata.mangaupdates.model

import org.snd.metadata.Provider
import org.snd.model.SeriesSearchResult
import java.time.Year

data class SearchResult(
    val id: Int,
    val title: String,
    val summary: String?,
    val thumbnail: String?,
    val genres: Collection<String>,
    val year: Year?,
    val rating: Double?,
    val isAdult: Boolean
)

fun SearchResult.toSeriesSearchResult(): SeriesSearchResult {
    return SeriesSearchResult(
        imageUrl = thumbnail,
        title = title,
        provider = Provider.MANGA_UPDATES,
        resultId = id.toString()
    )
}