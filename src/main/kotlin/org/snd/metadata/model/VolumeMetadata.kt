package org.snd.metadata.model

import java.time.LocalDate


data class VolumeMetadata(
    val title: String? = null,
    val summary: String? = null,
    val number: Int? = null,
    val numberSort: Float? = null,
    val releaseDate: LocalDate? = null,
    val authors: List<Author>? = null,
    val tags: Set<String>? = null,
    val isbn: String? = null,
    val links: List<WebLink>? = null,
    val chapters: Collection<Chapter>? = null,

    val startChapter: Int?,
    val endChapter: Int?,

    val thumbnail: Thumbnail? = null,
)

data class Chapter(
    val name: String?,
    val number: Int
)

data class WebLink(
    val label: String,
    val url: String,
)
