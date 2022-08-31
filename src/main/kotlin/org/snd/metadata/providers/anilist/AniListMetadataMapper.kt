package org.snd.metadata.providers.anilist

import org.jsoup.Jsoup
import org.snd.config.SeriesMetadataConfig
import org.snd.fragment.AniListManga
import org.snd.metadata.MetadataConfigApplier
import org.snd.metadata.model.Author
import org.snd.metadata.model.AuthorRole
import org.snd.metadata.model.Provider
import org.snd.metadata.model.ProviderSeriesId
import org.snd.metadata.model.ProviderSeriesMetadata
import org.snd.metadata.model.SeriesMetadata
import org.snd.metadata.model.SeriesSearchResult
import org.snd.metadata.model.Thumbnail
import org.snd.type.MediaStatus

class AniListMetadataMapper(
    private val metadataConfig: SeriesMetadataConfig,
) {
    private val allowedRoles = listOf("Story & Art", "Story", "Original Story", "Art", "Illustration")

    private val artistRoles = listOf(
        AuthorRole.PENCILLER,
        AuthorRole.INKER,
        AuthorRole.COLORIST,
        AuthorRole.LETTERER,
        AuthorRole.COVER
    )

    fun toSeriesMetadata(series: AniListManga, thumbnail: Thumbnail? = null): ProviderSeriesMetadata {
        val status = when (series.status) {
            MediaStatus.FINISHED -> SeriesMetadata.Status.ENDED
            MediaStatus.RELEASING -> SeriesMetadata.Status.ONGOING
            MediaStatus.NOT_YET_RELEASED -> SeriesMetadata.Status.ONGOING
            MediaStatus.CANCELLED -> SeriesMetadata.Status.ABANDONED
            MediaStatus.HIATUS -> SeriesMetadata.Status.HIATUS
            else -> null
        }

        val authors = series.staff?.edges
            ?.mapNotNull { extractNameAndRole(it) }
            ?.filter { (_, role) -> allowedRoles.contains(role) }
            ?.flatMap { (authorName, aniListRole) ->
                when (aniListRole) {
                    "Story & Art" -> {
                        artistRoles.map { role -> Author(authorName, role) } + Author(authorName, AuthorRole.WRITER)
                    }
                    "Story" -> {
                        listOf(Author(authorName, AuthorRole.WRITER))
                    }
                    "Art", "Illustration" -> {
                        artistRoles.map { role -> Author(authorName, role) }
                    }
                    else -> {
                        emptyList()
                    }
                }
            }
        val tags = series.tags?.asSequence()
            ?.filterNotNull()
            ?.mapNotNull { if (it.rank == null) null else it.name to it.rank }
            ?.sortedByDescending { it.second }
            ?.take(15)
            ?.map { it.first }
            ?.toList()
            ?: emptyList()

        val title = series.title?.english ?: series.title?.romaji ?: series.title?.native


        val metadata = SeriesMetadata(
            status = status,
            title = title,
            titleSort = title,
            summary = series.description?.let { Jsoup.parse(it).wholeText() },
            genres = series.genres?.filterNotNull() ?: emptyList(),
            tags = tags,
            authors = authors ?: emptyList(),
            thumbnail = thumbnail,
            totalBookCount = series.volumes,
            alternativeTitles = series.title?.let { listOfNotNull(it.english, it.romaji, it.native) } ?: emptyList()
        )
        return MetadataConfigApplier.apply(
            ProviderSeriesMetadata(id = ProviderSeriesId(series.id.toString()), provider = Provider.ANILIST, metadata = metadata),
            metadataConfig
        )
    }

    fun toSearchResult(search: AniListManga): SeriesSearchResult {
        val title = search.title?.english ?: search.title?.romaji ?: search.title?.native
        return SeriesSearchResult(
            imageUrl = search.coverImage?.large,
            title = title!!,
            provider = Provider.ANILIST,
            resultId = search.id.toString()
        )
    }

    private fun extractNameAndRole(edge: AniListManga.Edge?): Pair<String, String>? {
        if (edge == null) return null

        val role = edge.role?.replace("\\([^)]*\\)".toRegex(), "")?.trim()
        val name = edge.node?.name?.full

        return if (role == null || name == null) null
        else name to role
    }
}
